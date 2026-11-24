package com.doctortrack;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.text.DecimalFormat;

public final class UITheme {

    // Modern Clinical Palette
    public static final Color PRIMARY = new Color(13, 148, 136);       // Medical Teal #0D9488
    public static final Color PRIMARY_DARK = new Color(15, 118, 110);  // Deep Teal #0F766E
    public static final Color PRIMARY_SOFT = new Color(240, 253, 250);  // Mint Surface #F0FDFA

    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Slate 900 #0F172A
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate 800 #1E293B
    public static final Color SIDEBAR_TEXT = new Color(203, 213, 225); // Slate 300

    public static final Color BG_MAIN = new Color(241, 245, 249);      // Slate 100 #F1F5F9
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200 #E2E8F0

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500

    // Status & Action Colors
    public static final Color SUCCESS = new Color(5, 150, 105);        // Emerald 600
    public static final Color SUCCESS_BG = new Color(209, 250, 229);   // Emerald 100

    public static final Color WARNING = new Color(217, 119, 6);        // Amber 600
    public static final Color WARNING_BG = new Color(254, 243, 199);   // Amber 100

    public static final Color INFO = new Color(37, 99, 235);           // Blue 600
    public static final Color INFO_BG = new Color(219, 234, 254);      // Blue 100

    public static final Color DANGER = new Color(225, 29, 72);         // Rose 600
    public static final Color DANGER_BG = new Color(255, 228, 230);    // Rose 100

    public static final Color NEUTRAL_BG = new Color(241, 245, 249);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_KPI_VALUE = new Font("Segoe UI", Font.BOLD, 26);

    public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₱#,##0.00");
    public static final DecimalFormat PLAIN_DECIMAL = new DecimalFormat("0.00");

    private UITheme() {
    }

    public static JPanel createCardPanel(LayoutManager layout, int pad) {
        JPanel card = new JPanel(layout);
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(pad, pad, pad, pad)
        ));
        return card;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(new Color(51, 65, 85));
        return label;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setPreferredSize(new Dimension(0, 34));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setPreferredSize(new Dimension(0, 34));
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 36));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_BODY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(204, 251, 241));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
    }

    /**
     * Table cell renderer that draws modern rounded status pill badges.
     */
    public static DefaultTableCellRenderer createStatusBadgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                String status = value == null ? "" : value.toString();
                JPanel wrapper = new JPanel(new GridBagLayout());
                wrapper.setOpaque(true);
                wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

                Color pillBg;
                Color pillFg;

                switch (status) {
                    case "Available", "Completed" -> {
                        pillBg = SUCCESS_BG;
                        pillFg = SUCCESS;
                    }
                    case "In Consultation" -> {
                        pillBg = WARNING_BG;
                        pillFg = WARNING;
                    }
                    case "Scheduled" -> {
                        pillBg = INFO_BG;
                        pillFg = INFO;
                    }
                    case "On Leave", "Cancelled" -> {
                        pillBg = DANGER_BG;
                        pillFg = DANGER;
                    }
                    default -> {
                        pillBg = NEUTRAL_BG;
                        pillFg = TEXT_SECONDARY;
                    }
                }

                JLabel badge = new JLabel(status) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                        super.paintComponent(g2);
                        g2.dispose();
                    }
                };
                badge.setOpaque(false);
                badge.setBackground(pillBg);
                badge.setForeground(pillFg);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setBorder(new EmptyBorder(3, 10, 3, 10));

                wrapper.add(badge);
                return wrapper;
            }
        };
    }

    public static class RegexDocumentFilter extends DocumentFilter {
        private final String regex;

        public RegexDocumentFilter(String regex) {
            this.regex = regex;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + string + current.substring(offset);
            if (next.matches(regex)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String replacement = (text == null) ? "" : text;
            String next = current.substring(0, offset) + replacement + current.substring(offset + length);
            if (next.matches(regex)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
