package com.doctortrack;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern 60 FPS Shimmer Skeleton Loader displayed before live components render.
 * Supports two layout-matched presets:
 * - DASHBOARD: 4 top KPI cards + left queue table card + right doctor status card.
 * - SPLIT_FORM_TABLE: left input form card + right filter bar & data table card.
 */
public class SkeletonLoaderPanel extends JPanel {

    public enum Mode {
        DASHBOARD,
        SPLIT_FORM_TABLE
    }

    private static final Color BASE_SHIMMER = new Color(226, 232, 240);      // Slate 200 #E2E8F0
    private static final Color HIGHLIGHT_SHIMMER = new Color(248, 250, 252); // Slate 50  #F8FAFC
    private static final Color SUBTLE_SHIMMER = new Color(241, 245, 249);    // Slate 100 #F1F5F9

    private Mode mode = Mode.DASHBOARD;
    private float shimmerPhase = -0.35f;
    private final Timer shimmerTimer;

    public SkeletonLoaderPanel() {
        setOpaque(false);
        shimmerTimer = new Timer(16, e -> {
            shimmerPhase += 0.032f;
            if (shimmerPhase > 1.35f) {
                shimmerPhase = -0.35f;
            }
            repaint();
        });
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        repaint();
    }

    public void startAnimation() {
        shimmerPhase = -0.25f;
        if (!shimmerTimer.isRunning()) {
            shimmerTimer.start();
        }
        repaint();
    }

    public void stopAnimation() {
        if (shimmerTimer.isRunning()) {
            shimmerTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        // Create sweeping horizontal shimmer paint across the panel
        float center = shimmerPhase * w;
        float bandHalfWidth = Math.max(180f, w * 0.24f);
        LinearGradientPaint shimmerPaint = new LinearGradientPaint(
                center - bandHalfWidth, 0,
                center + bandHalfWidth, 0,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{BASE_SHIMMER, HIGHLIGHT_SHIMMER, BASE_SHIMMER}
        );

        if (mode == Mode.DASHBOARD) {
            paintDashboardSkeleton(g2, w, h, shimmerPaint);
        } else {
            paintSplitFormTableSkeleton(g2, w, h, shimmerPaint);
        }

        g2.dispose();
    }

    private void paintDashboardSkeleton(Graphics2D g2, int w, int h, Paint shimmerPaint) {
        int gap = 14;
        int kpiH = 100;
        int kpiCount = 4;
        int totalGap = gap * (kpiCount - 1);
        int kpiW = (w - totalGap) / kpiCount;

        // 1. Top 4 KPI Cards
        for (int i = 0; i < kpiCount; i++) {
            int kx = i * (kpiW + gap);
            if (i == kpiCount - 1) {
                kpiW = w - kx;
            }
            drawCardContainer(g2, kx, 0, kpiW, kpiH);

            // Left accent bar placeholder
            g2.setPaint(shimmerPaint);
            g2.fillRoundRect(kx, 0, 5, kpiH, 4, 4);

            // KPI Title bar
            g2.fill(new RoundRectangle2D.Double(kx + 18, 16, Math.min(120, kpiW - 36), 11, 6, 6));
            // KPI Big Number bar
            g2.fill(new RoundRectangle2D.Double(kx + 18, 38, Math.min(68, kpiW - 36), 26, 8, 8));
            // KPI Subtitle bar
            g2.fill(new RoundRectangle2D.Double(kx + 18, 74, Math.min(140, kpiW - 36), 10, 6, 6));
        }

        // 2. Bottom Split: Left Queue Card + Right Doctor Availability Card
        int bottomY = kpiH + 16;
        int bottomH = Math.max(120, h - bottomY);
        int rightW = 370;
        int leftW = Math.max(200, w - rightW - 16);

        // Left Card (Today's Appointment Queue)
        drawCardContainer(g2, 0, bottomY, leftW, bottomH);
        g2.setPaint(shimmerPaint);
        // Header title + 3 action buttons
        g2.fill(new RoundRectangle2D.Double(16, bottomY + 18, 230, 18, 8, 8));
        if (leftW > 520) {
            g2.fill(new RoundRectangle2D.Double(leftW - 365, bottomY + 14, 115, 28, 10, 10));
            g2.fill(new RoundRectangle2D.Double(leftW - 242, bottomY + 14, 130, 28, 10, 10));
            g2.fill(new RoundRectangle2D.Double(leftW - 104, bottomY + 14, 88, 28, 10, 10));
        }

        // Table Header Bar
        int tableY = bottomY + 54;
        g2.setColor(SUBTLE_SHIMMER);
        g2.fillRoundRect(16, tableY, leftW - 32, 34, 8, 8);

        // Table Shimmer Rows
        g2.setPaint(shimmerPaint);
        int rowY = tableY + 44;
        int rowStep = 38;
        int rowIdx = 0;
        while (rowY + 24 < bottomY + bottomH - 16) {
            int innerW = leftW - 48;
            g2.fill(new RoundRectangle2D.Double(24, rowY, 32, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(72, rowY, 68, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(158, rowY, 110 - (rowIdx % 2) * 18, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(290, rowY, 115, 13, 6, 6));
            if (innerW > 520) {
                g2.fill(new RoundRectangle2D.Double(425, rowY, innerW - 535, 13, 6, 6));
            }
            // Status Pill
            g2.fill(new RoundRectangle2D.Double(leftW - 116, rowY - 3, 92, 20, 14, 14));
            rowY += rowStep;
            rowIdx++;
        }

        // Right Card (Doctor Live Availability)
        int rightX = leftW + 16;
        drawCardContainer(g2, rightX, bottomY, rightW, bottomH);
        g2.setPaint(shimmerPaint);
        g2.fill(new RoundRectangle2D.Double(rightX + 16, bottomY + 18, 175, 18, 8, 8));
        g2.fill(new RoundRectangle2D.Double(rightX + rightW - 126, bottomY + 14, 110, 28, 10, 10));

        g2.setColor(SUBTLE_SHIMMER);
        g2.fillRoundRect(rightX + 16, tableY, rightW - 32, 34, 8, 8);

        g2.setPaint(shimmerPaint);
        int docRowY = tableY + 44;
        while (docRowY + 24 < bottomY + bottomH - 16) {
            g2.fill(new RoundRectangle2D.Double(rightX + 24, docRowY, 110, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(rightX + 148, docRowY, 85, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(rightX + rightW - 110, docRowY - 3, 86, 20, 14, 14));
            docRowY += rowStep;
        }
    }

    private void paintSplitFormTableSkeleton(Graphics2D g2, int w, int h, Paint shimmerPaint) {
        int formW = 350;
        int rightX = formW + 16;
        int rightW = Math.max(200, w - rightX);

        // 1. Left Form Card Skeleton
        drawCardContainer(g2, 0, 0, formW, h);
        g2.setPaint(shimmerPaint);
        // Form Title
        g2.fill(new RoundRectangle2D.Double(16, 18, 190, 18, 8, 8));

        // 6 Stacked Label + Input Box Skeletons
        int fieldY = 52;
        for (int i = 0; i < 6; i++) {
            if (fieldY + 60 > h - 95) {
                break;
            }
            int labelW = 95 + (i % 3) * 28;
            g2.fill(new RoundRectangle2D.Double(16, fieldY, labelW, 11, 6, 6));
            g2.fill(new RoundRectangle2D.Double(16, fieldY + 17, formW - 32, 33, 10, 10));
            fieldY += 60;
        }

        // Bottom 2x2 Button Grid Skeleton
        int btnW = (formW - 40) / 2;
        int btnY1 = h - 92;
        int btnY2 = h - 48;
        if (btnY1 > 80) {
            g2.fill(new RoundRectangle2D.Double(16, btnY1, btnW, 34, 10, 10));
            g2.fill(new RoundRectangle2D.Double(24 + btnW, btnY1, btnW, 34, 10, 10));
            g2.fill(new RoundRectangle2D.Double(16, btnY2, btnW, 34, 10, 10));
            g2.fill(new RoundRectangle2D.Double(24 + btnW, btnY2, btnW, 34, 10, 10));
        }

        // 2. Right Filter + Table Card Skeleton
        drawCardContainer(g2, rightX, 0, rightW, h);
        g2.setPaint(shimmerPaint);

        // Top Filter Bar Placeholders
        g2.fill(new RoundRectangle2D.Double(rightX + 16, 22, 55, 12, 6, 6));
        g2.fill(new RoundRectangle2D.Double(rightX + 80, 14, 145, 32, 10, 10));
        g2.fill(new RoundRectangle2D.Double(rightX + 240, 22, 55, 12, 6, 6));
        g2.fill(new RoundRectangle2D.Double(rightX + 305, 14, 155, 32, 10, 10));
        if (rightW > 640) {
            g2.fill(new RoundRectangle2D.Double(rightX + 475, 14, 175, 32, 10, 10));
        }

        // Table Header Bar
        int tableY = 58;
        g2.setColor(SUBTLE_SHIMMER);
        g2.fillRoundRect(rightX + 16, tableY, rightW - 32, 36, 8, 8);

        // Table Shimmer Rows
        g2.setPaint(shimmerPaint);
        int rowY = tableY + 48;
        int rowStep = 38;
        int rowIdx = 0;
        while (rowY + 24 < h - 18) {
            g2.fill(new RoundRectangle2D.Double(rightX + 24, rowY, 34, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(rightX + 76, rowY, 125 - (rowIdx % 2) * 20, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(rightX + 225, rowY, 95, 13, 6, 6));
            g2.fill(new RoundRectangle2D.Double(rightX + 345, rowY, 105, 13, 6, 6));
            if (rightW > 620) {
                g2.fill(new RoundRectangle2D.Double(rightX + 470, rowY, rightW - 600, 13, 6, 6));
            }
            // Right-end status pill or action placeholder
            g2.fill(new RoundRectangle2D.Double(rightX + rightW - 116, rowY - 3, 92, 20, 14, 14));
            rowY += rowStep;
            rowIdx++;
        }
    }

    private void drawCardContainer(Graphics2D g2, int x, int y, int w, int h) {
        RoundRectangle2D card = new RoundRectangle2D.Double(x, y, w - 1, h - 1, 10, 10);
        g2.setColor(UITheme.CARD_BG);
        g2.fill(card);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(card);
    }
}
