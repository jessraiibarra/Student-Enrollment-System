package FProject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FacultyDashboard extends JFrame {
    private String facultyId;
    private JPanel contentAreaPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;

    // Sidebar components
    private CustomAvatarLabel lblSidebarAvatar;
    private JLabel lblSidebarName;
    private JLabel lblSidebarEmail;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public FacultyDashboard(String facultyId) {
        this.facultyId = (facultyId != null) ? facultyId.trim() : "";

        setTitle("International State College of the Philippines - Faculty Portal");

        // --- FULL SCREEN CONFIGURATION ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- BACKGROUND PANEL WITH iscp_faculty.png SUPPORT ---
        JPanel rootPanel = new JPanel(new BorderLayout()) {
            private Image bgImage = loadBackgroundImage();

            private Image loadBackgroundImage() {
                try {
                    URL imgUrl = getClass().getResource("iscp_faculty.png");
                    if (imgUrl != null) {
                        return new ImageIcon(imgUrl).getImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File imgFile = new File("src/FProject/iscp_faculty.png");
                if (imgFile.exists()) {
                    return new ImageIcon(imgFile.getAbsolutePath()).getImage();
                }

                return null;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(15, 23, 42));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        setContentPane(rootPanel);

        // --- SIDEBAR PANEL (LEFT) ---
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // --- CONTENT AREA (RIGHT) ---
        cardLayout = new CardLayout();
        contentAreaPanel = new JPanel(cardLayout);
        contentAreaPanel.setOpaque(false);

        // Initialize Sub-Panels
        JPanel dashboardHomePanel = createHomePanel();

        // Faculty Profile serves as Personal Information & Editing View
        FacultyProfile profilePanel = new FacultyProfile(this.facultyId);
        profilePanel.setOnProfileUpdatedListener(this::refreshSidebarInfo);

        // Assigned Classes Panel
        FacultyClasses classesPanel = new FacultyClasses(this.facultyId);

        // Add panels to CardLayout container
        contentAreaPanel.add(dashboardHomePanel, "HOME");
        contentAreaPanel.add(profilePanel, "INFO"); // Personal Information / Profile Editing
        contentAreaPanel.add(classesPanel, "CLASSES"); // Assigned Classes View

        rootPanel.add(contentAreaPanel, BorderLayout.CENTER);

        // Load initial sidebar user details from database
        refreshSidebarInfo();

        // Default view opens to Dashboard Home
        cardLayout.show(contentAreaPanel, "HOME");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBackground(new Color(9, 15, 30));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));

        // Portal Branding Header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);
        brandPanel.setMaximumSize(new Dimension(248, 40));

        JLabel logoIcon = new JLabel("IS");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 165, 250), 1),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));

        JPanel brandTextGroup = new JPanel();
        brandTextGroup.setLayout(new BoxLayout(brandTextGroup, BoxLayout.Y_AXIS));
        brandTextGroup.setOpaque(false);

        JLabel brandTitle = new JLabel("ISCP PORTAL");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Faculty Management");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandSub.setForeground(new Color(148, 163, 184));

        brandTextGroup.add(brandTitle);
        brandTextGroup.add(brandSub);
        brandPanel.add(logoIcon);
        brandPanel.add(brandTextGroup);

        sidebar.add(brandPanel);
        sidebar.add(Box.createVerticalStrut(25));

        // Sidebar Profile Summary
        JPanel userSummaryPanel = new JPanel();
        userSummaryPanel.setLayout(new BoxLayout(userSummaryPanel, BoxLayout.Y_AXIS));
        userSummaryPanel.setOpaque(false);
        userSummaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarAvatar = new CustomAvatarLabel();
        lblSidebarAvatar.setPreferredSize(new Dimension(70, 70));
        lblSidebarAvatar.setMaximumSize(new Dimension(70, 70));
        lblSidebarAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarName = new JLabel("Loading...");
        lblSidebarName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSidebarName.setForeground(Color.WHITE);
        lblSidebarName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSidebarEmail = new JLabel("");
        lblSidebarEmail.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSidebarEmail.setForeground(new Color(191, 219, 254));
        lblSidebarEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        userSummaryPanel.add(lblSidebarAvatar);
        userSummaryPanel.add(Box.createVerticalStrut(10));
        userSummaryPanel.add(lblSidebarName);
        userSummaryPanel.add(Box.createVerticalStrut(3));
        userSummaryPanel.add(lblSidebarEmail);

        sidebar.add(userSummaryPanel);
        sidebar.add(Box.createVerticalStrut(25));
        sidebar.add(createSidebarSeparator());
        sidebar.add(Box.createVerticalStrut(15));

        // Navigation Links
        sidebar.add(createNavButton("📊  Dashboard Home", e -> cardLayout.show(contentAreaPanel, "HOME")));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("👤  Personal Information", e -> cardLayout.show(contentAreaPanel, "INFO")));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("📚  Assigned Classes", e -> cardLayout.show(contentAreaPanel, "CLASSES")));

        // Bottom Logout Section
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarSeparator());
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createNavButton("🚪  Logout System", e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        }));

        return sidebar;
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(new Color(9, 15, 30));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setMargin(new Insets(0, 10, 0, 0));
        btn.setMaximumSize(new Dimension(248, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(30, 41, 59));
                btn.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(9, 15, 30));
                btn.setForeground(new Color(226, 232, 240));
            }
        });

        return btn;
    }

    private JSeparator createSidebarSeparator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(248, 1));
        sep.setForeground(new Color(30, 41, 59));
        return sep;
    }

    private JPanel createHomePanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(40, 45, 40, 45)));
        card.setPreferredSize(new Dimension(640, 360));

        JLabel titleLabel = new JLabel("Good Day, Faculty");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel("Faculty Management Console");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitleLabel.setForeground(new Color(37, 99, 235));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));

        JLabel descLabel = new JLabel(
                "<html><body style='width: 520px;'>Welcome back to your faculty workspace. Oversee your assigned classes, inspect personal profile records, and manage academic responsibilities with ease.</body></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 116, 139));
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy • hh:mm a");
        JLabel dateLabel = new JLabel("📅  " + now.format(formatter));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(71, 85, 105));

        statusLabel = new JLabel("● System Status: Database Connected (SQL Server)");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(22, 163, 74));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        card.add(titleLabel);
        card.add(subtitleLabel);
        card.add(descLabel);
        card.add(dateLabel);
        card.add(statusLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(card, gbc);

        return contentPanel;
    }

    public void refreshSidebarInfo() {
        if (facultyId == null || facultyId.isEmpty())
            return;

        String query = "SELECT first_name, last_name, email, profile_picture FROM faculty " +
                "WHERE faculty_id = ? OR email = ? OR email LIKE ? + '@%'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, facultyId);
            pst.setString(2, facultyId);
            pst.setString(3, facultyId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String email = rs.getString("email");
                    String picPath = rs.getString("profile_picture");

                    lblSidebarName
                            .setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    lblSidebarEmail.setText(email != null ? email : "");

                    if (picPath != null && !picPath.trim().isEmpty()) {
                        File file = new File(picPath.trim());
                        if (file.exists()) {
                            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                            lblSidebarAvatar.setSidebarImage(icon.getImage());
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
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2.drawString("?", size / 2 - 6, size / 2 + 8);
            }
            g2.dispose();

            Graphics2D g2Border = (Graphics2D) g.create();
            g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2Border.setColor(new Color(96, 165, 250));
            g2Border.setStroke(new BasicStroke(2));
            g2Border.drawOval(1, 1, size - 2, size - 2);
            g2Border.dispose();
        }
    }
}