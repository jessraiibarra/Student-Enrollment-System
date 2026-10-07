package FProject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;

public class StudentDashboard extends JFrame {
    private String loggedInStudentId;
    private JLabel statusLabel;
    private JPanel contentAreaPanel;
    private CardLayout cardLayout;

    // Sidebar components
    private CustomAvatarLabel lblSidebarAvatar;
    private JLabel lblSidebarName;
    private JLabel lblSidebarId;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public StudentDashboard(String studentId) {
        this.loggedInStudentId = (studentId != null) ? studentId.trim() : "";
        setTitle("International State College of the Philippines - Student Portal");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Root Background Panel
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            private Image bgImage = loadBackgroundImage();

            private Image loadBackgroundImage() {
                try {
                    URL imgUrl = getClass().getResource("iscp_student.jpg");
                    if (imgUrl != null)
                        return new ImageIcon(imgUrl).getImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                File imgFile = new File("src/FProject/iscp_student.jpg");
                if (imgFile.exists())
                    return new ImageIcon(imgFile.getAbsolutePath()).getImage();
                return null;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(11, 17, 32));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(rootPanel);

        // Sidebar
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // Right Content Area
        cardLayout = new CardLayout();
        contentAreaPanel = new JPanel(cardLayout);
        contentAreaPanel.setOpaque(false);

        JPanel dashboardHomePanel = createHomePanel();

        StudentProfile profilePanel = new StudentProfile(loggedInStudentId);
        profilePanel.setOpaque(false);
        profilePanel.setOnProfileUpdatedListener(this::refreshSidebarInfo);

        StudentEnrollmentStatus enrollmentStatusPanel = new StudentEnrollmentStatus(loggedInStudentId);
        enrollmentStatusPanel.setOpaque(false);

        StudentCourseEnrollment courseEnrollmentPanel = new StudentCourseEnrollment(loggedInStudentId);
        courseEnrollmentPanel.setOpaque(false);

        contentAreaPanel.add(dashboardHomePanel, "HOME");
        contentAreaPanel.add(profilePanel, "PROFILE");
        contentAreaPanel.add(enrollmentStatusPanel, "ENROLLMENT_STATUS");
        contentAreaPanel.add(courseEnrollmentPanel, "ENROLL_COURSES");

        rootPanel.add(contentAreaPanel, BorderLayout.CENTER);

        refreshSidebarInfo();
        cardLayout.show(contentAreaPanel, "HOME");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBackground(new Color(8, 13, 26));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));

        // Brand Header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setMaximumSize(new Dimension(244, 42));

        JLabel logoIcon = new JLabel("IS");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JPanel brandTextGroup = new JPanel();
        brandTextGroup.setLayout(new BoxLayout(brandTextGroup, BoxLayout.Y_AXIS));
        brandTextGroup.setOpaque(false);

        JLabel brandTitle = new JLabel("ISCP PORTAL");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Student Management");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandSub.setForeground(new Color(148, 163, 184));

        brandTextGroup.add(brandTitle);
        brandTextGroup.add(brandSub);
        brandPanel.add(logoIcon);
        brandPanel.add(brandTextGroup);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(25));

        // User Summary Section
        JPanel userSummaryPanel = new JPanel();
        userSummaryPanel.setLayout(new BoxLayout(userSummaryPanel, BoxLayout.Y_AXIS));
        userSummaryPanel.setOpaque(false);
        userSummaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userSummaryPanel.setMaximumSize(new Dimension(244, 150));

        lblSidebarAvatar = new CustomAvatarLabel();
        lblSidebarAvatar.setPreferredSize(new Dimension(72, 72));
        lblSidebarAvatar.setMaximumSize(new Dimension(72, 72));
        lblSidebarAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarName = new JLabel("Loading...");
        lblSidebarName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSidebarName.setForeground(Color.WHITE);
        lblSidebarName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarId = new JLabel("");
        lblSidebarId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSidebarId.setForeground(new Color(148, 163, 184));
        lblSidebarId.setAlignmentX(Component.CENTER_ALIGNMENT);

        userSummaryPanel.add(lblSidebarAvatar);
        userSummaryPanel.add(Box.createVerticalStrut(12));
        userSummaryPanel.add(lblSidebarName);
        userSummaryPanel.add(Box.createVerticalStrut(4));
        userSummaryPanel.add(lblSidebarId);

        sidebar.add(userSummaryPanel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createSidebarSeparator());
        sidebar.add(Box.createVerticalStrut(15));

        // Navigation Links
        sidebar.add(createNavButton("📊  Dashboard Home", e -> cardLayout.show(contentAreaPanel, "HOME"), false));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(
                createNavButton("👤  View / Update Profile", e -> cardLayout.show(contentAreaPanel, "PROFILE"), false));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createNavButton("📋  Enrollment Status",
                e -> cardLayout.show(contentAreaPanel, "ENROLLMENT_STATUS"), false));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(
                createNavButton("📚  Enroll Courses", e -> cardLayout.show(contentAreaPanel, "ENROLL_COURSES"), false));

        // Bottom Logout Section (Red Text as requested)
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarSeparator());
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createNavButton("🚪  Logout System", e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        }, true));

        return sidebar;
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener action, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(isLogout ? new Color(248, 113, 113) : new Color(226, 232, 240));
        btn.setBackground(new Color(8, 13, 26));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 12, 0, 0));
        btn.setMaximumSize(new Dimension(244, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(
                        isLogout ? new int[] { 69, 10, 10 }[0] > 0 ? new Color(69, 10, 10) : new Color(30, 41, 59)
                                : new Color(30, 41, 59));
                btn.setForeground(isLogout ? new Color(252, 165, 165) : Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(8, 13, 26));
                btn.setForeground(isLogout ? new Color(248, 113, 113) : new Color(226, 232, 240));
            }
        });

        return btn;
    }

    private JSeparator createSidebarSeparator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(244, 1));
        sep.setForeground(new Color(30, 41, 59));
        return sep;
    }

    private JPanel createHomePanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(255, 255, 255, 245));
        textPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)));
        textPanel.setOpaque(true);

        JLabel titleLabel = new JLabel("Welcome, Student (ID: " + loggedInStudentId + ")");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel(
                "<html><body style='width: 450px;'>Welcome back to your student portal. Select an option from the sidebar menu to view your institutional profile, check enrollment statuses, or register for courses.</body></html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 15, 0));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - hh:mm a");
        JLabel dateLabel = new JLabel("📅 " + now.format(formatter));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(71, 85, 105));

        statusLabel = new JLabel("✔ Student Portal Active & Secure");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(22, 163, 74));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        textPanel.add(dateLabel);
        textPanel.add(statusLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(textPanel, gbc);

        return contentPanel;
    }

    public void refreshSidebarInfo() {
        if (loggedInStudentId == null || loggedInStudentId.trim().isEmpty())
            return;

        // Correct column name: profile_picture
        String query = "SELECT first_name, middle_name, last_name, contact_number, profile_picture FROM student WHERE student_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, loggedInStudentId.trim());

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String middleName = rs.getString("middle_name");
                    String lastName = rs.getString("last_name");
                    String contactNumber = rs.getString("contact_number");
                    String picPath = rs.getString("profile_picture");

                    String fullName = (firstName != null ? firstName : "") +
                            (middleName != null && !middleName.trim().isEmpty() ? " " + middleName + " " : " ") +
                            (lastName != null ? lastName : "");

                    if (lblSidebarName != null) {
                        lblSidebarName
                                .setText(fullName.trim().isEmpty() ? "ID: " + loggedInStudentId : fullName.trim());
                    }
                    if (lblSidebarId != null) {
                        lblSidebarId.setText("ID: " + loggedInStudentId
                                + (contactNumber != null && !contactNumber.trim().isEmpty() ? " • " + contactNumber
                                        : ""));
                    }

                    if (picPath != null && !picPath.trim().isEmpty()) {
                        File file = new File(picPath.trim());
                        if (file.exists()) {
                            lblSidebarAvatar.setSidebarImage(new ImageIcon(file.getAbsolutePath()).getImage());
                        } else {
                            lblSidebarAvatar.setSidebarImage(null);
                        }
                    } else {
                        lblSidebarAvatar.setSidebarImage(null);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static class CustomAvatarLabel extends JLabel {
        private Image sidebarImg = null;

        public void setSidebarImage(Image img) {
            this.sidebarImg = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));

            if (sidebarImg != null) {
                g2.drawImage(sidebarImg, 0, 0, size, size, this);
            } else {
                g2.setColor(new Color(30, 41, 59));
                g2.fillRect(0, 0, size, size);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("?", size / 2 - 5, size / 2 + 7);
            }
            g2.dispose();

            Graphics2D g2Border = (Graphics2D) g.create();
            g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2Border.setColor(new Color(59, 130, 246));
            g2Border.setStroke(new BasicStroke(2));
            g2Border.drawOval(1, 1, size - 2, size - 2);
            g2Border.dispose();
        }
    }
}