package com.doctortrack;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;

/**
 * Modern, minimal animated loading screen displayed before entering the main workspace.
 * Features a 60 FPS vector medical pulse & ECG animation, smooth eased progress bar,
 * asynchronous database initialization, and a fade-out transition.
 */
public class SplashScreenWindow extends JWindow {

    private static final int WIDTH = 540;
    private static final int HEIGHT = 330;

    private float progress = 0.0f;       // 0.0 to 1.0
    private float targetProgress = 0.25f;
    private float phase = 0.0f;          // animation phase in radians
    private float opacity = 1.0f;
    private boolean dbInitialized = false;
    private boolean fadingOut = false;
    private String statusText = "Initializing modern interface...";
    private String dbErrorMessage = null;

    private final Timer animationTimer;
    private final SplashCanvas canvas;

    public SplashScreenWindow(Runnable onFinished) {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        canvas = new SplashCanvas();
        setContentPane(canvas);

        // Start background DB initialization in parallel with the splash animation
        Thread initThread = new Thread(() -> {
            try {
                Thread.sleep(350);
                SwingUtilities.invokeLater(() -> {
                    statusText = "Connecting to DoctorTrack database...";
                    targetProgress = 0.60f;
                });

                try (Connection ignored = DBConnection.getConnection()) {
                    // Connected to MySQL doctor_track_db
                }

                Thread.sleep(400);
                SwingUtilities.invokeLater(() -> {
                    statusText = "Loading doctors & patient schedules...";
                    targetProgress = 0.90f;
                });

                Thread.sleep(400);
                SwingUtilities.invokeLater(() -> {
                    dbInitialized = true;
                    statusText = "Ready — launching workspace...";
                    targetProgress = 1.0f;
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    dbErrorMessage = ex.getMessage();
                    dbInitialized = true;
                    statusText = "Launching workspace (offline/DB check)...";
                    targetProgress = 1.0f;
                });
            }
        }, "DoctorTrack-Init-Thread");
        initThread.setDaemon(true);
        initThread.start();

        // 60 FPS animation loop (16ms interval)
        animationTimer = new Timer(16, e -> {
            phase += 0.085f;

            // Smooth cubic-like easing toward targetProgress
            float diff = targetProgress - progress;
            progress += diff * 0.09f;
            if (dbInitialized && progress > 0.985f) {
                progress = 1.0f;
                fadingOut = true;
            }

            if (fadingOut) {
                opacity -= 0.07f;
                if (opacity <= 0.05f) {
                    opacity = 0.0f;
                    ((Timer) e.getSource()).stop();
                    setVisible(false);
                    dispose();
                    if (dbErrorMessage != null) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Database connection warning:\n" + dbErrorMessage +
                                "\n\nPlease verify MySQL is running on localhost:3306.",
                                "Database Notice",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                    onFinished.run();
                    return;
                }
                try {
                    setOpacity(Math.max(0.0f, Math.min(1.0f, opacity)));
                } catch (UnsupportedOperationException ignored) {
                    // Fallback if platform doesn't support window opacity
                }
            }

            canvas.repaint();
        });
    }

    public void start() {
        setVisible(true);
        animationTimer.start();
    }

    private class SplashCanvas extends JPanel {

        SplashCanvas() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();

            // 1. Card Background with subtle slate gradient
            RoundRectangle2D cardShape = new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 28, 28);
            GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(15, 23, 42),
                    w, h, new Color(23, 37, 64)
            );
            g2.setPaint(bgGradient);
            g2.fill(cardShape);

            // Subtle ambient teal glow behind the center emblem
            int cx = w / 2;
            int cy = 104;
            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point(cx, cy),
                    115f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(20, 184, 166, 42), new Color(15, 23, 42, 0)}
            );
            g2.setPaint(glow);
            g2.fillOval(cx - 115, cy - 115, 230, 230);

            // Outer border
            g2.setColor(new Color(45, 212, 191, 65));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(cardShape);

            // 2. Animated Expanding Pulse Ring
            float pulseCycle = (phase * 0.45f) % 1.0f;
            float pulseRadius = 34f + pulseCycle * 30f;
            int pulseAlpha = (int) ((1.0f - pulseCycle) * 110);
            g2.setColor(new Color(45, 212, 191, Math.max(0, pulseAlpha)));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawOval(
                    Math.round(cx - pulseRadius),
                    Math.round(cy - pulseRadius),
                    Math.round(pulseRadius * 2),
                    Math.round(pulseRadius * 2)
            );

            // 3. Minimalist Orbiting Arc Spinner
            double angleDeg = (phase * 38.0) % 360.0;
            g2.setColor(new Color(20, 184, 166, 190));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Double(cx - 38, cy - 38, 76, 76, angleDeg, 105, Arc2D.OPEN));

            g2.setColor(new Color(148, 163, 184, 65));
            g2.draw(new Arc2D.Double(cx - 38, cy - 38, 76, 76, angleDeg + 150, 160, Arc2D.OPEN));

            // 4. Inner Emblem Circle & Medical Cross
            g2.setColor(new Color(19, 78, 74, 210));
            g2.fillOval(cx - 29, cy - 29, 58, 58);

            // Subtle Medical Cross silhouette inside emblem
            g2.setColor(new Color(45, 212, 191, 55));
            g2.fillRoundRect(cx - 4, cy - 14, 8, 28, 4, 4);
            g2.fillRoundRect(cx - 14, cy - 4, 28, 8, 4, 4);

            // 5. Animated Minimal ECG Heartbeat Wave across Emblem
            GeneralPath ecg = new GeneralPath();
            int startX = cx - 26;
            int endX = cx + 26;
            ecg.moveTo(startX, cy);
            for (int x = startX; x <= endX; x++) {
                float norm = (x - cx) / 26.0f; // -1 to 1
                float envelope = (float) Math.exp(-norm * norm * 3.5);
                float wave = (float) (Math.sin(norm * 8.0f - phase * 2.2f) * 11.0f * envelope);
                ecg.lineTo(x, cy + wave);
            }
            g2.setColor(new Color(94, 234, 212));
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(ecg);

            // 6. Brand Title & Subtitle
            g2.setFont(new Font("Segoe UI", Font.BOLD, 25));
            g2.setColor(new Color(248, 250, 252));
            String title = "DoctorTrack";
            FontMetrics fmTitle = g2.getFontMetrics();
            g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 190);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(45, 212, 191));
            String subtitle = "DOCTOR  &  PATIENT  SCHEDULING";
            FontMetrics fmSub = g2.getFontMetrics();
            g2.drawString(subtitle, (w - fmSub.stringWidth(subtitle)) / 2, 212);

            // 7. Minimalist Progress Bar & Status Footer
            int barX = 56;
            int barY = 274;
            int barW = w - 112;
            int barH = 5;

            // Status Text (Left)
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(148, 163, 184));
            g2.drawString(statusText, barX, barY - 12);

            // Percentage Text (Right)
            String pctText = Math.min(100, Math.round(progress * 100)) + "%";
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(94, 234, 212));
            FontMetrics fmPct = g2.getFontMetrics();
            g2.drawString(pctText, barX + barW - fmPct.stringWidth(pctText), barY - 12);

            // Track Background
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(barX, barY, barW, barH, barH, barH);

            // Filled Progress Pill
            int fillW = Math.max(barH, Math.round(barW * progress));
            GradientPaint barGrad = new GradientPaint(
                    barX, barY, new Color(13, 148, 136),
                    barX + fillW, barY, new Color(94, 234, 212)
            );
            g2.setPaint(barGrad);
            g2.fillRoundRect(barX, barY, fillW, barH, barH, barH);

            g2.dispose();
        }
    }
}
