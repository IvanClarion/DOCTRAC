package com.doctortrack;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for DoctorTrack: Doctor and Patient Scheduling.
 * Features a dark-slate sidebar navigation and 4 synchronized modules:
 * 1. Dashboard Overview (KPI metrics, Today's Queue, Live Doctor Status)
 * 2. Appointments Scheduler (Conflict-checked booking, Date/Doctor/Status
 * filtering)
 * 3. Doctor Tracker (Specialties, shifts, consultation fees, live availability
 * toggle)
 * 4. Patient Registry (Patient profiles, blood types, contact details, medical
 * notes)
 */
public class DoctorTrackFrame extends JFrame {

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_APPOINTMENTS = "APPOINTMENTS";
    private static final String CARD_DOCTORS = "DOCTORS";
    private static final String CARD_PATIENTS = "PATIENTS";
    private static final String CARD_SKELETON = "SKELETON";

    private static final String[] SPECIALIZATIONS = {
            "General Medicine", "Cardiology", "Pediatrics", "Dermatology",
            "Orthopedics", "Neurology", "OB-GYN", "Dental"
    };

    private static final String[] DOCTOR_STATUSES = {
            "Available", "In Consultation", "On Leave"
    };

    private static final String[] SHIFTS = {
            "08:00 AM - 04:00 PM",
            "08:00 AM - 05:00 PM",
            "09:00 AM - 05:00 PM",
            "09:00 AM - 03:00 PM",
            "10:00 AM - 06:00 PM",
            "01:00 PM - 08:00 PM"
    };

    private static final String[] GENDERS = { "Male", "Female", "Other" };

    private static final String[] BLOOD_TYPES = {
            "O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Unknown"
    };

    private static final String[] TIME_SLOTS = {
            "08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM",
            "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM",
            "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM",
            "05:00 PM"
    };

    private static final String[] APPOINTMENT_STATUSES = {
            "Scheduled", "In Consultation", "Completed", "Cancelled"
    };

    // Navigation & Layout
    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCardPanel = new JPanel(contentCardLayout);
    private final SkeletonLoaderPanel skeletonPanel = new SkeletonLoaderPanel();
    private String currentActiveCard = CARD_DASHBOARD;
    private Timer skeletonTransitionTimer;
    private final List<JButton> navButtons = new ArrayList<>();
    private final JLabel lblTopViewTitle = new JLabel("Dashboard Overview");
    private final JLabel lblTopViewSubtitle = new JLabel(
            "Real-time summary of today's clinic schedule and doctor availability");

    // -------------------------------------------------------------------------
    // 1. Dashboard Controls
    // -------------------------------------------------------------------------
    private final JLabel lblKpiDoctorsValue = new JLabel("0");
    private final JLabel lblKpiDoctorsSub = new JLabel("0 Available now");
    private final JLabel lblKpiPatientsValue = new JLabel("0");
    private final JLabel lblKpiPatientsSub = new JLabel("Registered profiles");
    private final JLabel lblKpiTodayApptValue = new JLabel("0");
    private final JLabel lblKpiTodayApptSub = new JLabel("Scheduled for today");
    private final JLabel lblKpiCompletedValue = new JLabel("0");
    private final JLabel lblKpiCompletedSub = new JLabel("0 Pending / In Consult");

    private DefaultTableModel dashQueueModel;
    private JTable dashQueueTable;
    private DefaultTableModel dashDoctorStatusModel;
    private JTable dashDoctorStatusTable;

    // -------------------------------------------------------------------------
    // 2. Appointment Scheduler Controls
    // -------------------------------------------------------------------------
    private final JComboBox<Models.DoctorItem> cmbApptDoctor = new JComboBox<>();
    private final JComboBox<Models.PatientItem> cmbApptPatient = new JComboBox<>();
    private DatePicker apptDatePicker;
    private final JComboBox<String> cmbApptTime = new JComboBox<>(TIME_SLOTS);
    private final JTextField txtApptReason = new JTextField();
    private final JComboBox<String> cmbApptStatus = new JComboBox<>(APPOINTMENT_STATUSES);

    private final JComboBox<String> cmbFilterApptDateScope = new JComboBox<>(
            new String[] { "All Dates", "Today Only", "Selected Date" });
    private DatePicker filterDatePicker;
    private final JComboBox<String> cmbFilterApptStatus = new JComboBox<>(
            new String[] { "All Statuses", "Scheduled", "In Consultation", "Completed", "Cancelled" });
    private final JTextField txtSearchAppt = new JTextField();
    private DefaultTableModel apptTableModel;
    private JTable apptTable;
    private int selectedAppointmentId = -1;

    // -------------------------------------------------------------------------
    // 3. Doctor Tracker Controls
    // -------------------------------------------------------------------------
    private final JTextField txtDocName = new JTextField();
    private final JComboBox<String> cmbDocSpec = new JComboBox<>(SPECIALIZATIONS);
    private final JTextField txtDocRoom = new JTextField();
    private final JTextField txtDocPhone = new JTextField();
    private final JComboBox<String> cmbDocShift = new JComboBox<>(SHIFTS);
    private final JTextField txtDocFee = new JTextField("500.00");
    private final JComboBox<String> cmbDocStatus = new JComboBox<>(DOCTOR_STATUSES);

    private final JComboBox<String> cmbFilterDocSpec = new JComboBox<>();
    private final JComboBox<String> cmbFilterDocStatus = new JComboBox<>(
            new String[] { "All Statuses", "Available", "In Consultation", "On Leave" });
    private final JTextField txtSearchDoc = new JTextField();
    private DefaultTableModel docTableModel;
    private JTable docTable;
    private int selectedDoctorId = -1;

    // -------------------------------------------------------------------------
    // 4. Patient Registry Controls
    // -------------------------------------------------------------------------
    private final JTextField txtPatName = new JTextField();
    private final JTextField txtPatAge = new JTextField();
    private final JComboBox<String> cmbPatGender = new JComboBox<>(GENDERS);
    private final JComboBox<String> cmbPatBlood = new JComboBox<>(BLOOD_TYPES);
    private final JTextField txtPatPhone = new JTextField();
    private final JTextArea txtPatNotes = new JTextArea(3, 20);

    private final JComboBox<String> cmbFilterPatBlood = new JComboBox<>(
            new String[] { "All Blood Types", "O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Unknown" });
    private final JTextField txtSearchPat = new JTextField();
    private DefaultTableModel patTableModel;
    private JTable patTable;
    private int selectedPatientId = -1;

    public DoctorTrackFrame() {
        setTitle("DoctorTrack — Doctor & Patient Scheduling System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 760);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);

        // Input validation filters
        ((AbstractDocument) txtDocFee.getDocument()).setDocumentFilter(
                new UITheme.RegexDocumentFilter("^\\d*(\\.\\d{0,2})?$"));
        ((AbstractDocument) txtPatAge.getDocument()).setDocumentFilter(
                new UITheme.RegexDocumentFilter("^\\d{0,3}$"));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);
        setContentPane(root);

        // Left Sidebar Navigation
        root.add(createSidebar(), BorderLayout.WEST);

        // Main Workspace (Top Bar + Card Content)
        JPanel workspace = new JPanel(new BorderLayout(0, 14));
        workspace.setBackground(UITheme.BG_MAIN);
        workspace.setBorder(new EmptyBorder(18, 20, 18, 20));
        workspace.add(createTopHeaderBar(), BorderLayout.NORTH);

        contentCardPanel.setOpaque(false);
        contentCardPanel.add(createDashboardView(), CARD_DASHBOARD);
        contentCardPanel.add(createAppointmentsView(), CARD_APPOINTMENTS);
        contentCardPanel.add(createDoctorsView(), CARD_DOCTORS);
        contentCardPanel.add(createPatientsView(), CARD_PATIENTS);
        contentCardPanel.add(skeletonPanel, CARD_SKELETON);

        workspace.add(contentCardPanel, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);

        // Initial load of all data with skeleton loader transition
        showWithSkeleton(CARD_DASHBOARD);
    }

    // =========================================================================
    // SIDEBAR & TOP HEADER
    // =========================================================================

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));

        // Top Brand Panel
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(4, 8, 24, 8));

        JPanel badgeIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.fillRoundRect(cx - 3, cy - 10, 6, 20, 3, 3);
                g2.fillRoundRect(cx - 10, cy - 3, 20, 6, 3, 3);
                g2.dispose();
            }
        };
        badgeIcon.setOpaque(false);
        badgeIcon.setPreferredSize(new Dimension(40, 40));

        JPanel brandText = new JPanel(new GridLayout(2, 1));
        brandText.setOpaque(false);
        JLabel lblBrand = new JLabel("DoctorTrack");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);
        JLabel lblTag = new JLabel("Clinic Scheduling Suite");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTag.setForeground(new Color(45, 212, 191));
        brandText.add(lblBrand);
        brandText.add(lblTag);

        brandPanel.add(badgeIcon, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Items
        JPanel navList = new JPanel();
        navList.setOpaque(false);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));

        JLabel lblMenuSection = new JLabel("  NAVIGATION");
        lblMenuSection.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenuSection.setForeground(new Color(100, 116, 139));
        lblMenuSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        navList.add(lblMenuSection);
        navList.add(Box.createVerticalStrut(10));

        JButton btnDash = createNavButton("Dashboard Overview", CARD_DASHBOARD,
                "Dashboard Overview", "Real-time summary of today's clinic schedule and doctor availability");
        JButton btnPats = createNavButton("Patient Registry", CARD_PATIENTS,
                "Patient Registry & Records",
                "Manage patient profiles, blood types, contact details, and medical notes");
        JButton btnAppt = createNavButton("Appointment Schedule", CARD_APPOINTMENTS,
                "Appointment & Scheduling Engine",
                "Book, reschedule, and filter patient appointments with conflict protection");
        JButton btnDocs = createNavButton("Doctor Tracker", CARD_DOCTORS,
                "Doctor Tracker & Availability",
                "Manage doctors, specialties, shift hours, fees, and live clinic status");

        navList.add(btnDash);
        navList.add(Box.createVerticalStrut(6));
        navList.add(btnPats);
        navList.add(Box.createVerticalStrut(6));
        navList.add(btnAppt);
        navList.add(Box.createVerticalStrut(6));
        navList.add(btnDocs);

        sidebar.add(navList, BorderLayout.CENTER);

        // Bottom Live Clock Card
        JPanel footerCard = new JPanel(new GridLayout(2, 1, 0, 2));
        footerCard.setBackground(new Color(30, 41, 59));
        footerCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");
        JLabel lblClockTime = new JLabel(LocalDateTime.now().format(timeFmt));
        lblClockTime.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClockTime.setForeground(new Color(94, 234, 212));

        JLabel lblClockDate = new JLabel(LocalDate.now().format(dateFmt));
        lblClockDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClockDate.setForeground(new Color(148, 163, 184));

        footerCard.add(lblClockTime);
        footerCard.add(lblClockDate);

        Timer clockTimer = new Timer(1000, e -> {
            lblClockTime.setText(LocalDateTime.now().format(timeFmt));
            lblClockDate.setText(LocalDate.now().format(dateFmt));
        });
        clockTimer.start();

        sidebar.add(footerCard, BorderLayout.SOUTH);

        setActiveNavButton(btnDash);
        return sidebar;
    }

    private JButton createNavButton(String label, String cardName, String headerTitle, String headerSubtitle) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addActionListener(e -> {
            switchToCard(cardName, headerTitle, headerSubtitle, btn);
        });

        navButtons.add(btn);
        return btn;
    }

    private void switchToCard(String cardName, String title, String subtitle, JButton activeBtn) {
        setActiveNavButton(activeBtn);
        lblTopViewTitle.setText(title);
        lblTopViewSubtitle.setText(subtitle);
        showWithSkeleton(cardName);
    }

    /**
     * Displays the layout-matched 60 FPS shimmer skeleton loader while refreshing
     * data,
     * then transitions smoothly to the target rendered view.
     */
    private void showWithSkeleton(String targetCard) {
        currentActiveCard = targetCard;
        if (skeletonTransitionTimer != null && skeletonTransitionTimer.isRunning()) {
            skeletonTransitionTimer.stop();
        }

        SkeletonLoaderPanel.Mode mode = CARD_DASHBOARD.equals(targetCard)
                ? SkeletonLoaderPanel.Mode.DASHBOARD
                : SkeletonLoaderPanel.Mode.SPLIT_FORM_TABLE;
        skeletonPanel.setMode(mode);
        skeletonPanel.startAnimation();
        contentCardLayout.show(contentCardPanel, CARD_SKELETON);

        // Refresh database models and reveal live components after shimmer sweep
        SwingUtilities.invokeLater(this::refreshAllData);

        skeletonTransitionTimer = new Timer(460, e -> {
            skeletonPanel.stopAnimation();
            contentCardLayout.show(contentCardPanel, targetCard);
        });
        skeletonTransitionTimer.setRepeats(false);
        skeletonTransitionTimer.start();
    }

    private void setActiveNavButton(JButton activeBtn) {
        for (JButton btn : navButtons) {
            if (btn == activeBtn) {
                btn.setBackground(UITheme.PRIMARY);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.SIDEBAR_BG);
                btn.setForeground(UITheme.SIDEBAR_TEXT);
            }
        }
    }

    private JPanel createTopHeaderBar() {
        JPanel header = UITheme.createCardPanel(new BorderLayout(16, 0), 14);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        lblTopViewTitle.setFont(UITheme.FONT_TITLE);
        lblTopViewTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTopViewSubtitle.setFont(UITheme.FONT_SMALL);
        lblTopViewSubtitle.setForeground(UITheme.TEXT_SECONDARY);
        titleBox.add(lblTopViewTitle);
        titleBox.add(lblTopViewSubtitle);

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quickActions.setOpaque(false);

        JButton btnQuickBook = UITheme.createButton("+ New Appointment", UITheme.PRIMARY, Color.WHITE);
        btnQuickBook.setPreferredSize(new Dimension(165, 36));
        btnQuickBook.addActionListener(e -> {
            if (navButtons.size() > 2) {
                switchToCard(CARD_APPOINTMENTS,
                        "Appointment & Scheduling Engine",
                        "Book, reschedule, and filter patient appointments with conflict protection",
                        navButtons.get(2));
            }
        });

        JButton btnRefresh = UITheme.createButton("Refresh Data", new Color(241, 245, 249), UITheme.TEXT_PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(125, 36));
        btnRefresh.addActionListener(e -> showWithSkeleton(currentActiveCard));

        quickActions.add(btnRefresh);
        quickActions.add(btnQuickBook);

        header.add(titleBox, BorderLayout.WEST);
        header.add(quickActions, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // 1. DASHBOARD VIEW
    // =========================================================================

    private JPanel createDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        // Top KPI Row (4 Cards)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(createKpiCard("ACTIVE DOCTORS", lblKpiDoctorsValue, lblKpiDoctorsSub, UITheme.PRIMARY));
        kpiGrid.add(createKpiCard("REGISTERED PATIENTS", lblKpiPatientsValue, lblKpiPatientsSub, UITheme.INFO));
        kpiGrid.add(createKpiCard("TODAY'S SCHEDULE", lblKpiTodayApptValue, lblKpiTodayApptSub, UITheme.WARNING));
        kpiGrid.add(createKpiCard("COMPLETED TODAY", lblKpiCompletedValue, lblKpiCompletedSub, UITheme.SUCCESS));

        panel.add(kpiGrid, BorderLayout.NORTH);

        // Center Split: Left = Today's Appointment Queue, Right = Doctor Availability
        // Snapshot
        JPanel centerSplit = new JPanel(new BorderLayout(16, 0));
        centerSplit.setOpaque(false);

        // Left: Today's Queue Card
        JPanel queueCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);
        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);
        JLabel lblQueueTitle = new JLabel("Today's Appointment Queue (" + LocalDate.now() + ")");
        lblQueueTitle.setFont(UITheme.FONT_SECTION);
        lblQueueTitle.setForeground(UITheme.TEXT_PRIMARY);
        queueHeader.add(lblQueueTitle, BorderLayout.WEST);

        JPanel queueActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        queueActionBtns.setOpaque(false);

        JButton btnStartConsult = UITheme.createButton("Start Consult", UITheme.WARNING_BG, UITheme.WARNING);
        btnStartConsult.setPreferredSize(new Dimension(125, 32));
        btnStartConsult.addActionListener(e -> updateSelectedDashboardAppointmentStatus("In Consultation"));

        JButton btnComplete = UITheme.createButton("Mark Completed", UITheme.SUCCESS_BG, UITheme.SUCCESS);
        btnComplete.setPreferredSize(new Dimension(140, 32));
        btnComplete.addActionListener(e -> updateSelectedDashboardAppointmentStatus("Completed"));

        JButton btnCancel = UITheme.createButton("Cancel", UITheme.DANGER_BG, UITheme.DANGER);
        btnCancel.setPreferredSize(new Dimension(90, 32));
        btnCancel.addActionListener(e -> updateSelectedDashboardAppointmentStatus("Cancelled"));

        queueActionBtns.add(btnStartConsult);
        queueActionBtns.add(btnComplete);
        queueActionBtns.add(btnCancel);
        queueHeader.add(queueActionBtns, BorderLayout.EAST);

        String[] queueCols = { "ID", "Time Slot", "Patient", "Doctor", "Specialty", "Reason", "Status" };
        dashQueueModel = new DefaultTableModel(queueCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dashQueueTable = new JTable(dashQueueModel);
        UITheme.styleTable(dashQueueTable);
        dashQueueTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        dashQueueTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        dashQueueTable.getColumnModel().getColumn(2).setPreferredWidth(135);
        dashQueueTable.getColumnModel().getColumn(3).setPreferredWidth(135);
        dashQueueTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        dashQueueTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        dashQueueTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        dashQueueTable.getColumnModel().getColumn(6).setCellRenderer(UITheme.createStatusBadgeRenderer());

        JScrollPane queueScroll = new JScrollPane(dashQueueTable);
        queueScroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        queueCard.add(queueHeader, BorderLayout.NORTH);
        queueCard.add(queueScroll, BorderLayout.CENTER);

        // Right: Live Doctor Availability Card
        JPanel docSnapCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);
        docSnapCard.setPreferredSize(new Dimension(370, 0));

        JPanel docSnapHeader = new JPanel(new BorderLayout());
        docSnapHeader.setOpaque(false);
        JLabel lblDocSnapTitle = new JLabel("Doctor Live Availability");
        lblDocSnapTitle.setFont(UITheme.FONT_SECTION);
        lblDocSnapTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnToggleDocStatus = UITheme.createButton("Cycle Status", UITheme.PRIMARY_SOFT, UITheme.PRIMARY_DARK);
        btnToggleDocStatus.setPreferredSize(new Dimension(115, 30));
        btnToggleDocStatus.addActionListener(e -> cycleDashboardDoctorStatus());

        docSnapHeader.add(lblDocSnapTitle, BorderLayout.WEST);
        docSnapHeader.add(btnToggleDocStatus, BorderLayout.EAST);

        String[] docSnapCols = { "ID", "Doctor", "Specialty", "Status" };
        dashDoctorStatusModel = new DefaultTableModel(docSnapCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dashDoctorStatusTable = new JTable(dashDoctorStatusModel);
        UITheme.styleTable(dashDoctorStatusTable);
        dashDoctorStatusTable.getColumnModel().getColumn(0).setMinWidth(0);
        dashDoctorStatusTable.getColumnModel().getColumn(0).setMaxWidth(0);
        dashDoctorStatusTable.getColumnModel().getColumn(0).setPreferredWidth(0);
        dashDoctorStatusTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        dashDoctorStatusTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        dashDoctorStatusTable.getColumnModel().getColumn(3).setPreferredWidth(115);
        dashDoctorStatusTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.createStatusBadgeRenderer());

        JScrollPane docSnapScroll = new JScrollPane(dashDoctorStatusTable);
        docSnapScroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        docSnapCard.add(docSnapHeader, BorderLayout.NORTH);
        docSnapCard.add(docSnapScroll, BorderLayout.CENTER);

        centerSplit.add(queueCard, BorderLayout.CENTER);
        centerSplit.add(docSnapCard, BorderLayout.EAST);

        panel.add(centerSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKpiCard(String title, JLabel lblValue, JLabel lblSub, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 16)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_SECONDARY);

        lblValue.setFont(UITheme.FONT_KPI_VALUE);
        lblValue.setForeground(UITheme.TEXT_PRIMARY);

        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. APPOINTMENTS & SCHEDULING VIEW
    // =========================================================================

    private JPanel createAppointmentsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);

        // Left Booking Form Card
        JPanel formCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);
        formCard.setPreferredSize(new Dimension(355, 0));

        JLabel lblFormTitle = new JLabel("Schedule Appointment");
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        UITheme.styleComboBox(cmbApptDoctor);
        UITheme.styleComboBox(cmbApptPatient);
        UITheme.styleComboBox(cmbApptTime);
        UITheme.styleTextField(txtApptReason);
        UITheme.styleComboBox(cmbApptStatus);

        apptDatePicker = createConfiguredDatePicker();

        int row = 0;
        addFormRow(fields, gbc, row++, "Select Doctor", cmbApptDoctor);
        addFormRow(fields, gbc, row++, "Select Patient", cmbApptPatient);
        addFormRow(fields, gbc, row++, "Appointment Date", apptDatePicker);
        addFormRow(fields, gbc, row++, "Time Slot (30-Min Interval)", cmbApptTime);
        addFormRow(fields, gbc, row++, "Reason for Visit / Chief Complaint", txtApptReason);
        addFormRow(fields, gbc, row++, "Appointment Status", cmbApptStatus);

        gbc.gridy = row * 2;
        gbc.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), gbc);

        formCard.add(fields, BorderLayout.CENTER);

        // Form Action Buttons
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        JButton btnBook = UITheme.createButton("Book Slot", UITheme.PRIMARY, Color.WHITE);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.INFO, Color.WHITE);
        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER, Color.WHITE);
        JButton btnClear = UITheme.createButton("Clear Form", new Color(226, 232, 240), UITheme.TEXT_PRIMARY);

        btnBook.addActionListener(e -> addAppointment());
        btnUpdate.addActionListener(e -> updateAppointment());
        btnDelete.addActionListener(e -> deleteAppointment());
        btnClear.addActionListener(e -> clearAppointmentForm());

        btnGrid.add(btnBook);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);

        formCard.add(btnGrid, BorderLayout.SOUTH);

        // Right Table & Filter Card
        JPanel tableCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterDatePicker = createConfiguredDatePicker();
        filterDatePicker.setPreferredSize(new Dimension(150, 34));
        filterDatePicker.setEnabled(false);

        cmbFilterApptDateScope.setFont(UITheme.FONT_BODY);
        cmbFilterApptDateScope.setPreferredSize(new Dimension(130, 34));
        cmbFilterApptDateScope.addActionListener(e -> {
            boolean useDate = "Selected Date".equals(cmbFilterApptDateScope.getSelectedItem());
            filterDatePicker.setEnabled(useDate);
            loadAppointmentsTable();
        });

        filterDatePicker.addDateChangeListener(e -> loadAppointmentsTable());

        cmbFilterApptStatus.setFont(UITheme.FONT_BODY);
        cmbFilterApptStatus.setPreferredSize(new Dimension(145, 34));
        cmbFilterApptStatus.addActionListener(e -> loadAppointmentsTable());

        txtSearchAppt.setFont(UITheme.FONT_BODY);
        txtSearchAppt.setPreferredSize(new Dimension(175, 34));
        txtSearchAppt.putClientProperty("JTextField.placeholderText", "Search doctor or patient...");
        addInstantSearchListener(txtSearchAppt, this::loadAppointmentsTable);

        filterBar.add(UITheme.createFieldLabel("Date:"));
        filterBar.add(cmbFilterApptDateScope);
        filterBar.add(filterDatePicker);
        filterBar.add(UITheme.createFieldLabel("Status:"));
        filterBar.add(cmbFilterApptStatus);
        filterBar.add(UITheme.createFieldLabel("Search:"));
        filterBar.add(txtSearchAppt);

        String[] cols = { "ID", "Date", "Time", "Doctor", "Specialty", "Patient", "Reason for Visit", "Status", "DocID",
                "PatID" };
        apptTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        apptTable = new JTable(apptTableModel);
        UITheme.styleTable(apptTable);

        // Hide DocID and PatID helper columns
        hideColumn(apptTable, 8);
        hideColumn(apptTable, 9);

        apptTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        apptTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        apptTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        apptTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        apptTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        apptTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        apptTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        apptTable.getColumnModel().getColumn(7).setPreferredWidth(115);
        apptTable.getColumnModel().getColumn(7).setCellRenderer(UITheme.createStatusBadgeRenderer());

        apptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateAppointmentFormFromSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(apptTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        tableCard.add(filterBar, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(formCard, BorderLayout.WEST);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 3. DOCTOR TRACKER VIEW
    // =========================================================================

    private JPanel createDoctorsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);

        JPanel formCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);
        formCard.setPreferredSize(new Dimension(345, 0));

        JLabel lblFormTitle = new JLabel("Doctor Profile & Shift");
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        UITheme.styleTextField(txtDocName);
        UITheme.styleComboBox(cmbDocSpec);
        UITheme.styleTextField(txtDocRoom);
        UITheme.styleTextField(txtDocPhone);
        UITheme.styleComboBox(cmbDocShift);
        UITheme.styleTextField(txtDocFee);
        UITheme.styleComboBox(cmbDocStatus);

        int row = 0;
        addFormRow(fields, gbc, row++, "Doctor Full Name", txtDocName);
        addFormRow(fields, gbc, row++, "Specialization", cmbDocSpec);
        addFormRow(fields, gbc, row++, "Clinic / Room Number", txtDocRoom);
        addFormRow(fields, gbc, row++, "Contact Number", txtDocPhone);
        addFormRow(fields, gbc, row++, "Shift Hours", cmbDocShift);
        addFormRow(fields, gbc, row++, "Consultation Fee (₱)", txtDocFee);
        addFormRow(fields, gbc, row++, "Current Availability Status", cmbDocStatus);

        gbc.gridy = row * 2;
        gbc.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), gbc);
        formCard.add(fields, BorderLayout.CENTER);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        JButton btnAdd = UITheme.createButton("Add Doctor", UITheme.PRIMARY, Color.WHITE);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.INFO, Color.WHITE);
        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER, Color.WHITE);
        JButton btnClear = UITheme.createButton("Clear", new Color(226, 232, 240), UITheme.TEXT_PRIMARY);

        btnAdd.addActionListener(e -> addDoctor());
        btnUpdate.addActionListener(e -> updateDoctor());
        btnDelete.addActionListener(e -> deleteDoctor());
        btnClear.addActionListener(e -> clearDoctorForm());

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);
        formCard.add(btnGrid, BorderLayout.SOUTH);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        cmbFilterDocSpec.addItem("All Specialties");
        for (String s : SPECIALIZATIONS) {
            cmbFilterDocSpec.addItem(s);
        }
        cmbFilterDocSpec.setFont(UITheme.FONT_BODY);
        cmbFilterDocSpec.setPreferredSize(new Dimension(155, 34));
        cmbFilterDocSpec.addActionListener(e -> loadDoctorsTable());

        cmbFilterDocStatus.setFont(UITheme.FONT_BODY);
        cmbFilterDocStatus.setPreferredSize(new Dimension(145, 34));
        cmbFilterDocStatus.addActionListener(e -> loadDoctorsTable());

        txtSearchDoc.setFont(UITheme.FONT_BODY);
        txtSearchDoc.setPreferredSize(new Dimension(190, 34));
        txtSearchDoc.putClientProperty("JTextField.placeholderText", "Search doctor name or room...");
        addInstantSearchListener(txtSearchDoc, this::loadDoctorsTable);

        filterBar.add(UITheme.createFieldLabel("Specialty:"));
        filterBar.add(cmbFilterDocSpec);
        filterBar.add(UITheme.createFieldLabel("Status:"));
        filterBar.add(cmbFilterDocStatus);
        filterBar.add(UITheme.createFieldLabel("Search:"));
        filterBar.add(txtSearchDoc);

        String[] cols = { "ID", "Doctor Name", "Specialization", "Room", "Contact", "Shift Hours", "Fee", "Status" };
        docTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        docTable = new JTable(docTableModel);
        UITheme.styleTable(docTable);
        docTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        docTable.getColumnModel().getColumn(1).setPreferredWidth(145);
        docTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        docTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        docTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        docTable.getColumnModel().getColumn(5).setPreferredWidth(145);
        docTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        docTable.getColumnModel().getColumn(7).setPreferredWidth(120);
        docTable.getColumnModel().getColumn(7).setCellRenderer(UITheme.createStatusBadgeRenderer());

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        docTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        docTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateDoctorFormFromSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(docTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        tableCard.add(filterBar, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(formCard, BorderLayout.WEST);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 4. PATIENT REGISTRY VIEW
    // =========================================================================

    private JPanel createPatientsView() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setOpaque(false);

        JPanel formCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);
        formCard.setPreferredSize(new Dimension(345, 0));

        JLabel lblFormTitle = new JLabel("Patient Registration");
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.TEXT_PRIMARY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        UITheme.styleTextField(txtPatName);
        UITheme.styleTextField(txtPatAge);
        UITheme.styleComboBox(cmbPatGender);
        UITheme.styleComboBox(cmbPatBlood);
        UITheme.styleTextField(txtPatPhone);

        txtPatNotes.setFont(UITheme.FONT_BODY);
        txtPatNotes.setLineWrap(true);
        txtPatNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtPatNotes);
        notesScroll.setPreferredSize(new Dimension(0, 74));

        int row = 0;
        addFormRow(fields, gbc, row++, "Patient Full Name", txtPatName);
        addFormRow(fields, gbc, row++, "Age (Years)", txtPatAge);
        addFormRow(fields, gbc, row++, "Gender", cmbPatGender);
        addFormRow(fields, gbc, row++, "Blood Type", cmbPatBlood);
        addFormRow(fields, gbc, row++, "Contact Number", txtPatPhone);
        addFormRow(fields, gbc, row++, "Medical Notes / Allergies", notesScroll);

        gbc.gridy = row * 2;
        gbc.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), gbc);
        formCard.add(fields, BorderLayout.CENTER);

        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        btnGrid.setOpaque(false);

        JButton btnAdd = UITheme.createButton("Add Patient", UITheme.PRIMARY, Color.WHITE);
        JButton btnUpdate = UITheme.createButton("Update", UITheme.INFO, Color.WHITE);
        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER, Color.WHITE);
        JButton btnClear = UITheme.createButton("Clear", new Color(226, 232, 240), UITheme.TEXT_PRIMARY);

        btnAdd.addActionListener(e -> addPatient());
        btnUpdate.addActionListener(e -> updatePatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnClear.addActionListener(e -> clearPatientForm());

        btnGrid.add(btnAdd);
        btnGrid.add(btnUpdate);
        btnGrid.add(btnDelete);
        btnGrid.add(btnClear);
        formCard.add(btnGrid, BorderLayout.SOUTH);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel(new BorderLayout(0, 12), 16);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        cmbFilterPatBlood.setFont(UITheme.FONT_BODY);
        cmbFilterPatBlood.setPreferredSize(new Dimension(150, 34));
        cmbFilterPatBlood.addActionListener(e -> loadPatientsTable());

        txtSearchPat.setFont(UITheme.FONT_BODY);
        txtSearchPat.setPreferredSize(new Dimension(220, 34));
        txtSearchPat.putClientProperty("JTextField.placeholderText", "Search patient name or phone...");
        addInstantSearchListener(txtSearchPat, this::loadPatientsTable);

        filterBar.add(UITheme.createFieldLabel("Blood Type:"));
        filterBar.add(cmbFilterPatBlood);
        filterBar.add(UITheme.createFieldLabel("Search:"));
        filterBar.add(txtSearchPat);

        String[] cols = { "ID", "Patient Name", "Age", "Gender", "Blood Type", "Contact Number",
                "Medical Notes / Allergies" };
        patTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        patTable = new JTable(patTableModel);
        UITheme.styleTable(patTable);
        patTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        patTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        patTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        patTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        patTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        patTable.getColumnModel().getColumn(5).setPreferredWidth(115);
        patTable.getColumnModel().getColumn(6).setPreferredWidth(240);

        patTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populatePatientFormFromSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(patTable);
        scroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        tableCard.add(filterBar, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(formCard, BorderLayout.WEST);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // DATA LOADING & SYNCHRONIZATION
    // =========================================================================

    public final void refreshAllData() {
        loadDoctorsTable();
        loadPatientsTable();
        reloadDoctorAndPatientCombos();
        loadAppointmentsTable();
        loadDashboardMetricsAndTables();
    }

    private void loadDashboardMetricsAndTables() {
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Doctors KPI
            int totalDocs = 0;
            int availDocs = 0;
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT status, COUNT(*) AS cnt FROM doctors GROUP BY status")) {
                while (rs.next()) {
                    int c = rs.getInt("cnt");
                    totalDocs += c;
                    if ("Available".equalsIgnoreCase(rs.getString("status"))) {
                        availDocs += c;
                    }
                }
            }
            lblKpiDoctorsValue.setText(String.valueOf(totalDocs));
            lblKpiDoctorsSub.setText(availDocs + " Available right now");

            // 2. Patients KPI
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patients")) {
                if (rs.next()) {
                    lblKpiPatientsValue.setText(String.valueOf(rs.getInt(1)));
                }
            }

            // 3. Today's Appointments KPI
            int todayTotal = 0;
            int todayCompleted = 0;
            int todayActive = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status, COUNT(*) AS cnt FROM appointments WHERE appointment_date = CURRENT_DATE GROUP BY status")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String st = rs.getString("status");
                        int c = rs.getInt("cnt");
                        todayTotal += c;
                        if ("Completed".equalsIgnoreCase(st)) {
                            todayCompleted += c;
                        } else if ("Scheduled".equalsIgnoreCase(st) || "In Consultation".equalsIgnoreCase(st)) {
                            todayActive += c;
                        }
                    }
                }
            }
            lblKpiTodayApptValue.setText(String.valueOf(todayTotal));
            lblKpiTodayApptSub.setText("Scheduled for " + LocalDate.now());
            lblKpiCompletedValue.setText(String.valueOf(todayCompleted));
            lblKpiCompletedSub.setText(todayActive + " Active / Waiting today");

            // 4. Today's Queue Table
            dashQueueModel.setRowCount(0);
            String queueSql = """
                        SELECT a.id, a.appointment_time, p.full_name AS patient_name,
                               d.full_name AS doctor_name, d.specialization, a.reason, a.status
                        FROM appointments a
                        JOIN doctors d ON a.doctor_id = d.id
                        JOIN patients p ON a.patient_id = p.id
                        WHERE a.appointment_date = CURRENT_DATE
                        ORDER BY a.appointment_time ASC, a.id ASC
                    """;
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(queueSql)) {
                while (rs.next()) {
                    dashQueueModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("appointment_time"),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("specialization"),
                            rs.getString("reason"),
                            rs.getString("status")
                    });
                }
            }

            // 5. Doctor Live Availability Snapshot Table
            dashDoctorStatusModel.setRowCount(0);
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT id, full_name, specialization, status FROM doctors ORDER BY full_name ASC")) {
                while (rs.next()) {
                    dashDoctorStatusModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("specialization"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load dashboard metrics: " + ex.getMessage());
        }
    }

    private void updateSelectedDashboardAppointmentStatus(String newStatus) {
        int row = dashQueueTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment from Today's Appointment Queue first.",
                    "Select Appointment", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int apptId = (int) dashQueueModel.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE appointments SET status = ? WHERE id = ?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, apptId);
            ps.executeUpdate();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Could not update appointment status: " + ex.getMessage());
        }
    }

    private void cycleDashboardDoctorStatus() {
        int row = dashDoctorStatusTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a doctor in the 'Doctor Live Availability' table to toggle their status.",
                    "Select Doctor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int docId = (int) dashDoctorStatusModel.getValueAt(row, 0);
        String current = String.valueOf(dashDoctorStatusModel.getValueAt(row, 3));
        String next = switch (current) {
            case "Available" -> "In Consultation";
            case "In Consultation" -> "On Leave";
            default -> "Available";
        };

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE doctors SET status = ? WHERE id = ?")) {
            ps.setString(1, next);
            ps.setInt(2, docId);
            ps.executeUpdate();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to toggle doctor status: " + ex.getMessage());
        }
    }

    private void reloadDoctorAndPatientCombos() {
        Integer prevDocId = (cmbApptDoctor.getSelectedItem() instanceof Models.DoctorItem d) ? d.getId() : null;
        Integer prevPatId = (cmbApptPatient.getSelectedItem() instanceof Models.PatientItem p) ? p.getId() : null;

        cmbApptDoctor.removeAllItems();
        cmbApptPatient.removeAllItems();

        try (Connection conn = DBConnection.getConnection()) {
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM doctors ORDER BY full_name ASC")) {
                while (rs.next()) {
                    Models.DoctorItem item = new Models.DoctorItem(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("specialization"),
                            rs.getString("room_number"),
                            rs.getString("phone"),
                            rs.getString("shift_hours"),
                            rs.getBigDecimal("consultation_fee"),
                            rs.getString("status"));
                    cmbApptDoctor.addItem(item);
                    if (prevDocId != null && item.getId() == prevDocId) {
                        cmbApptDoctor.setSelectedItem(item);
                    }
                }
            }

            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM patients ORDER BY full_name ASC")) {
                while (rs.next()) {
                    Models.PatientItem item = new Models.PatientItem(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("blood_type"),
                            rs.getString("phone"),
                            rs.getString("medical_notes"));
                    cmbApptPatient.addItem(item);
                    if (prevPatId != null && item.getId() == prevPatId) {
                        cmbApptPatient.setSelectedItem(item);
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Error loading doctors/patients into selectors: " + ex.getMessage());
        }
    }

    // =========================================================================
    // APPOINTMENTS CRUD & DOUBLE-BOOKING GUARD
    // =========================================================================

    private void loadAppointmentsTable() {
        if (apptTableModel == null) {
            return;
        }
        apptTableModel.setRowCount(0);

        String dateScope = String.valueOf(cmbFilterApptDateScope.getSelectedItem());
        String statusFilter = String.valueOf(cmbFilterApptStatus.getSelectedItem());
        String search = txtSearchAppt.getText().trim();

        StringBuilder sql = new StringBuilder("""
                    SELECT a.id, a.appointment_date, a.appointment_time,
                           d.full_name AS doctor_name, d.specialization,
                           p.full_name AS patient_name, a.reason, a.status,
                           a.doctor_id, a.patient_id
                    FROM appointments a
                    JOIN doctors d ON a.doctor_id = d.id
                    JOIN patients p ON a.patient_id = p.id
                    WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if ("Today Only".equals(dateScope)) {
            sql.append(" AND a.appointment_date = CURRENT_DATE");
        } else if ("Selected Date".equals(dateScope) && filterDatePicker != null
                && filterDatePicker.getDate() != null) {
            sql.append(" AND a.appointment_date = ?");
            params.add(Date.valueOf(filterDatePicker.getDate()));
        }

        if (!"All Statuses".equals(statusFilter)) {
            sql.append(" AND a.status = ?");
            params.add(statusFilter);
        }

        if (!search.isEmpty()) {
            sql.append(" AND (d.full_name LIKE ? OR p.full_name LIKE ? OR a.reason LIKE ?)");
            String like = "%" + search + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time ASC, a.id DESC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    apptTableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getDate("appointment_date").toLocalDate().toString(),
                            rs.getString("appointment_time"),
                            rs.getString("doctor_name"),
                            rs.getString("specialization"),
                            rs.getString("patient_name"),
                            rs.getString("reason"),
                            rs.getString("status"),
                            rs.getInt("doctor_id"),
                            rs.getInt("patient_id")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load appointments: " + ex.getMessage());
        }
    }

    private boolean hasAppointmentConflict(int doctorId, LocalDate date, String timeSlot, int excludeApptId) {
        String sql = """
                    SELECT COUNT(*) FROM appointments
                    WHERE doctor_id = ?
                      AND appointment_date = ?
                      AND appointment_time = ?
                      AND status IN ('Scheduled', 'In Consultation')
                      AND id <> ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, timeSlot);
            ps.setInt(4, excludeApptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            showError("Error checking schedule conflict: " + ex.getMessage());
            return true;
        }
    }

    private void addAppointment() {
        Models.DoctorItem doctor = (Models.DoctorItem) cmbApptDoctor.getSelectedItem();
        Models.PatientItem patient = (Models.PatientItem) cmbApptPatient.getSelectedItem();
        LocalDate date = apptDatePicker.getDate();
        String timeSlot = (String) cmbApptTime.getSelectedItem();
        String reason = txtApptReason.getText().trim();
        String status = (String) cmbApptStatus.getSelectedItem();

        if (doctor == null || patient == null) {
            showWarning("Please ensure both a Doctor and a Patient are selected.");
            return;
        }
        if (date == null) {
            showWarning("Please choose an appointment date.");
            return;
        }
        if (reason.isEmpty()) {
            showWarning("Please enter the reason for visit or chief complaint.");
            txtApptReason.requestFocus();
            return;
        }
        if ("On Leave".equalsIgnoreCase(doctor.getStatus()) && !"Cancelled".equalsIgnoreCase(status)) {
            showWarning(
                    doctor.getFullName() + " is currently marked 'On Leave'. Please choose another available doctor.");
            return;
        }
        if (!"Cancelled".equalsIgnoreCase(status) && hasAppointmentConflict(doctor.getId(), date, timeSlot, -1)) {
            showWarning("Schedule Conflict Detected!\n\n" + doctor.getFullName()
                    + " already has an active appointment on " + date + " at " + timeSlot + ".");
            return;
        }

        String sql = """
                    INSERT INTO appointments (doctor_id, patient_id, appointment_date, appointment_time, reason, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctor.getId());
            ps.setInt(2, patient.getId());
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, timeSlot);
            ps.setString(5, reason);
            ps.setString(6, status);
            ps.executeUpdate();

            clearAppointmentForm();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!", "Booked",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Failed to book appointment: " + ex.getMessage());
        }
    }

    private void updateAppointment() {
        if (selectedAppointmentId == -1) {
            showWarning("Please select an appointment from the table to update.");
            return;
        }
        Models.DoctorItem doctor = (Models.DoctorItem) cmbApptDoctor.getSelectedItem();
        Models.PatientItem patient = (Models.PatientItem) cmbApptPatient.getSelectedItem();
        LocalDate date = apptDatePicker.getDate();
        String timeSlot = (String) cmbApptTime.getSelectedItem();
        String reason = txtApptReason.getText().trim();
        String status = (String) cmbApptStatus.getSelectedItem();

        if (doctor == null || patient == null || date == null || reason.isEmpty()) {
            showWarning("Please fill in all appointment fields before updating.");
            return;
        }
        if (!"Cancelled".equalsIgnoreCase(status)
                && hasAppointmentConflict(doctor.getId(), date, timeSlot, selectedAppointmentId)) {
            showWarning("Schedule Conflict Detected!\n\n" + doctor.getFullName()
                    + " already has another active appointment on " + date + " at " + timeSlot + ".");
            return;
        }

        String sql = """
                    UPDATE appointments
                    SET doctor_id = ?, patient_id = ?, appointment_date = ?, appointment_time = ?, reason = ?, status = ?
                    WHERE id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctor.getId());
            ps.setInt(2, patient.getId());
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, timeSlot);
            ps.setString(5, reason);
            ps.setString(6, status);
            ps.setInt(7, selectedAppointmentId);
            ps.executeUpdate();

            clearAppointmentForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to update appointment: " + ex.getMessage());
        }
    }

    private void deleteAppointment() {
        if (selectedAppointmentId == -1) {
            showWarning("Please select an appointment from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete appointment #" + selectedAppointmentId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
            ps.setInt(1, selectedAppointmentId);
            ps.executeUpdate();
            clearAppointmentForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to delete appointment: " + ex.getMessage());
        }
    }

    private void populateAppointmentFormFromSelection() {
        int row = apptTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedAppointmentId = (int) apptTableModel.getValueAt(row, 0);
        LocalDate date = LocalDate.parse(String.valueOf(apptTableModel.getValueAt(row, 1)));
        String time = String.valueOf(apptTableModel.getValueAt(row, 2));
        String reason = String.valueOf(apptTableModel.getValueAt(row, 6));
        String status = String.valueOf(apptTableModel.getValueAt(row, 7));
        int docId = (int) apptTableModel.getValueAt(row, 8);
        int patId = (int) apptTableModel.getValueAt(row, 9);

        apptDatePicker.setDate(date);
        cmbApptTime.setSelectedItem(time);
        txtApptReason.setText(reason);
        cmbApptStatus.setSelectedItem(status);

        for (int i = 0; i < cmbApptDoctor.getItemCount(); i++) {
            if (cmbApptDoctor.getItemAt(i).getId() == docId) {
                cmbApptDoctor.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cmbApptPatient.getItemCount(); i++) {
            if (cmbApptPatient.getItemAt(i).getId() == patId) {
                cmbApptPatient.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearAppointmentForm() {
        selectedAppointmentId = -1;
        apptDatePicker.setDateToToday();
        cmbApptTime.setSelectedIndex(2); // 09:00 AM
        txtApptReason.setText("");
        cmbApptStatus.setSelectedIndex(0);
        if (apptTable != null) {
            apptTable.clearSelection();
        }
    }

    // =========================================================================
    // DOCTORS CRUD ("DOCTOR TRACKER")
    // =========================================================================

    private void loadDoctorsTable() {
        if (docTableModel == null) {
            return;
        }
        docTableModel.setRowCount(0);

        String specFilter = String.valueOf(cmbFilterDocSpec.getSelectedItem());
        String statusFilter = String.valueOf(cmbFilterDocStatus.getSelectedItem());
        String search = txtSearchDoc.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM doctors WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (specFilter != null && !"All Specialties".equals(specFilter) && !"null".equals(specFilter)) {
            sql.append(" AND specialization = ?");
            params.add(specFilter);
        }
        if (statusFilter != null && !"All Statuses".equals(statusFilter)) {
            sql.append(" AND status = ?");
            params.add(statusFilter);
        }
        if (!search.isEmpty()) {
            sql.append(" AND (full_name LIKE ? OR room_number LIKE ? OR phone LIKE ?)");
            String like = "%" + search + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY id ASC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal fee = rs.getBigDecimal("consultation_fee");
                    docTableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("specialization"),
                            rs.getString("room_number"),
                            rs.getString("phone"),
                            rs.getString("shift_hours"),
                            UITheme.CURRENCY_FORMAT.format(fee),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load doctors list: " + ex.getMessage());
        }
    }

    private void addDoctor() {
        String name = txtDocName.getText().trim();
        String spec = String.valueOf(cmbDocSpec.getSelectedItem());
        String room = txtDocRoom.getText().trim();
        String phone = txtDocPhone.getText().trim();
        String shift = String.valueOf(cmbDocShift.getSelectedItem());
        String feeStr = txtDocFee.getText().trim();
        String status = String.valueOf(cmbDocStatus.getSelectedItem());

        if (name.isEmpty() || room.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
            showWarning("Please fill in Doctor Name, Room Number, Contact Number, and Consultation Fee.");
            return;
        }

        BigDecimal fee = new BigDecimal(feeStr);
        String sql = """
                    INSERT INTO doctors (full_name, specialization, room_number, phone, shift_hours, consultation_fee, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.startsWith("Dr.") ? name : "Dr. " + name);
            ps.setString(2, spec);
            ps.setString(3, room);
            ps.setString(4, phone);
            ps.setString(5, shift);
            ps.setBigDecimal(6, fee);
            ps.setString(7, status);
            ps.executeUpdate();

            clearDoctorForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to add doctor: " + ex.getMessage());
        }
    }

    private void updateDoctor() {
        if (selectedDoctorId == -1) {
            showWarning("Please select a doctor from the table to update.");
            return;
        }
        String name = txtDocName.getText().trim();
        String spec = String.valueOf(cmbDocSpec.getSelectedItem());
        String room = txtDocRoom.getText().trim();
        String phone = txtDocPhone.getText().trim();
        String shift = String.valueOf(cmbDocShift.getSelectedItem());
        String feeStr = txtDocFee.getText().trim();
        String status = String.valueOf(cmbDocStatus.getSelectedItem());

        if (name.isEmpty() || room.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
            showWarning("Please complete all doctor fields before updating.");
            return;
        }

        String sql = """
                    UPDATE doctors
                    SET full_name = ?, specialization = ?, room_number = ?, phone = ?,
                        shift_hours = ?, consultation_fee = ?, status = ?
                    WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, spec);
            ps.setString(3, room);
            ps.setString(4, phone);
            ps.setString(5, shift);
            ps.setBigDecimal(6, new BigDecimal(feeStr));
            ps.setString(7, status);
            ps.setInt(8, selectedDoctorId);
            ps.executeUpdate();

            clearDoctorForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to update doctor: " + ex.getMessage());
        }
    }

    private void deleteDoctor() {
        if (selectedDoctorId == -1) {
            showWarning("Please select a doctor from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove this doctor and their linked appointments?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM doctors WHERE id = ?")) {
            ps.setInt(1, selectedDoctorId);
            ps.executeUpdate();
            clearDoctorForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to delete doctor: " + ex.getMessage());
        }
    }

    private void populateDoctorFormFromSelection() {
        int row = docTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedDoctorId = (int) docTableModel.getValueAt(row, 0);
        txtDocName.setText(String.valueOf(docTableModel.getValueAt(row, 1)));
        cmbDocSpec.setSelectedItem(String.valueOf(docTableModel.getValueAt(row, 2)));
        txtDocRoom.setText(String.valueOf(docTableModel.getValueAt(row, 3)));
        txtDocPhone.setText(String.valueOf(docTableModel.getValueAt(row, 4)));
        cmbDocShift.setSelectedItem(String.valueOf(docTableModel.getValueAt(row, 5)));

        String rawFee = String.valueOf(docTableModel.getValueAt(row, 6))
                .replace("₱", "").replace(",", "").trim();
        txtDocFee.setText(rawFee);
        cmbDocStatus.setSelectedItem(String.valueOf(docTableModel.getValueAt(row, 7)));
    }

    private void clearDoctorForm() {
        selectedDoctorId = -1;
        txtDocName.setText("");
        cmbDocSpec.setSelectedIndex(0);
        txtDocRoom.setText("");
        txtDocPhone.setText("");
        cmbDocShift.setSelectedIndex(0);
        txtDocFee.setText("500.00");
        cmbDocStatus.setSelectedIndex(0);
        if (docTable != null) {
            docTable.clearSelection();
        }
    }

    // =========================================================================
    // PATIENTS CRUD ("PATIENT REGISTRY")
    // =========================================================================

    private void loadPatientsTable() {
        if (patTableModel == null) {
            return;
        }
        patTableModel.setRowCount(0);

        String bloodFilter = String.valueOf(cmbFilterPatBlood.getSelectedItem());
        String search = txtSearchPat.getText().trim();

        StringBuilder sql = new StringBuilder("SELECT * FROM patients WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (bloodFilter != null && !"All Blood Types".equals(bloodFilter)) {
            sql.append(" AND blood_type = ?");
            params.add(bloodFilter);
        }
        if (!search.isEmpty()) {
            sql.append(" AND (full_name LIKE ? OR phone LIKE ? OR medical_notes LIKE ?)");
            String like = "%" + search + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY id ASC");

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patTableModel.addRow(new Object[] {
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("blood_type"),
                            rs.getString("phone"),
                            rs.getString("medical_notes")
                    });
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load patients: " + ex.getMessage());
        }
    }

    private void addPatient() {
        String name = txtPatName.getText().trim();
        String ageStr = txtPatAge.getText().trim();
        String gender = String.valueOf(cmbPatGender.getSelectedItem());
        String blood = String.valueOf(cmbPatBlood.getSelectedItem());
        String phone = txtPatPhone.getText().trim();
        String notes = txtPatNotes.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty()) {
            showWarning("Please fill in Patient Full Name, Age, and Contact Number.");
            return;
        }

        int age = Integer.parseInt(ageStr);
        String sql = """
                    INSERT INTO patients (full_name, age, gender, blood_type, phone, medical_notes)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, blood);
            ps.setString(5, phone);
            ps.setString(6, notes);
            ps.executeUpdate();

            clearPatientForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to register patient: " + ex.getMessage());
        }
    }

    private void updatePatient() {
        if (selectedPatientId == -1) {
            showWarning("Please select a patient from the table to update.");
            return;
        }
        String name = txtPatName.getText().trim();
        String ageStr = txtPatAge.getText().trim();
        String gender = String.valueOf(cmbPatGender.getSelectedItem());
        String blood = String.valueOf(cmbPatBlood.getSelectedItem());
        String phone = txtPatPhone.getText().trim();
        String notes = txtPatNotes.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty()) {
            showWarning("Please complete Patient Name, Age, and Contact Number.");
            return;
        }

        String sql = """
                    UPDATE patients
                    SET full_name = ?, age = ?, gender = ?, blood_type = ?, phone = ?, medical_notes = ?
                    WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, Integer.parseInt(ageStr));
            ps.setString(3, gender);
            ps.setString(4, blood);
            ps.setString(5, phone);
            ps.setString(6, notes);
            ps.setInt(7, selectedPatientId);
            ps.executeUpdate();

            clearPatientForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to update patient: " + ex.getMessage());
        }
    }

    private void deletePatient() {
        if (selectedPatientId == -1) {
            showWarning("Please select a patient from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this patient record and their scheduled appointments?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE id = ?")) {
            ps.setInt(1, selectedPatientId);
            ps.executeUpdate();
            clearPatientForm();
            refreshAllData();
        } catch (SQLException ex) {
            showError("Failed to delete patient: " + ex.getMessage());
        }
    }

    private void populatePatientFormFromSelection() {
        int row = patTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedPatientId = (int) patTableModel.getValueAt(row, 0);
        txtPatName.setText(String.valueOf(patTableModel.getValueAt(row, 1)));
        txtPatAge.setText(String.valueOf(patTableModel.getValueAt(row, 2)));
        cmbPatGender.setSelectedItem(String.valueOf(patTableModel.getValueAt(row, 3)));
        cmbPatBlood.setSelectedItem(String.valueOf(patTableModel.getValueAt(row, 4)));
        txtPatPhone.setText(String.valueOf(patTableModel.getValueAt(row, 5)));
        txtPatNotes.setText(String.valueOf(patTableModel.getValueAt(row, 6)));
    }

    private void clearPatientForm() {
        selectedPatientId = -1;
        txtPatName.setText("");
        txtPatAge.setText("");
        cmbPatGender.setSelectedIndex(0);
        cmbPatBlood.setSelectedIndex(0);
        txtPatPhone.setText("");
        txtPatNotes.setText("");
        if (patTable != null) {
            patTable.clearSelection();
        }
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    private DatePicker createConfiguredDatePicker() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("yyyy-MM-dd");
        settings.setAllowKeyboardEditing(false);
        settings.setAllowEmptyDates(false);
        DatePicker picker = new DatePicker(settings);
        picker.setDateToToday();
        picker.getComponentDateTextField().setFont(UITheme.FONT_BODY);
        picker.getComponentDateTextField().setPreferredSize(new Dimension(0, 34));
        picker.getComponentToggleCalendarButton().setPreferredSize(new Dimension(36, 34));
        picker.getComponentToggleCalendarButton().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return picker;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int rowIdx, String labelText, JComponent comp) {
        gbc.gridy = rowIdx * 2;
        gbc.insets = new Insets(6, 0, 2, 0);
        panel.add(UITheme.createFieldLabel(labelText), gbc);

        gbc.gridy = rowIdx * 2 + 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(comp, gbc);
    }

    private void hideColumn(JTable table, int colIdx) {
        table.getColumnModel().getColumn(colIdx).setMinWidth(0);
        table.getColumnModel().getColumn(colIdx).setMaxWidth(0);
        table.getColumnModel().getColumn(colIdx).setPreferredWidth(0);
    }

    private void addInstantSearchListener(JTextField field, Runnable callback) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                callback.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                callback.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                callback.run();
            }
        });
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Notice", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
