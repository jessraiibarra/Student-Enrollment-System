package FProject;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboard extends JFrame {
    private JLabel statusLabel;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String fileName, CardLayout layout) {
            super(layout);
            try {
                URL imageURL = getClass().getResource("/FProject/" + "iscp_bg.jpg");
                if (imageURL != null) {
                    backgroundImage = new ImageIcon(imageURL).getImage();
                } else {
                    backgroundImage = new ImageIcon("iscp_bg.jpg").getImage();
                }
            } catch (Exception e) {
                backgroundImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2d.setPaint(new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public AdminDashboard() {
        setTitle("International State College of the Philippines - Administrator Portal");

        // --- WINDOW CONFIGURATION (FULL SCREEN) ---
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PROFESSIONAL DARK NAVY SIDEBAR ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        sidebarPanel.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));

        JLabel brandTitle = new JLabel("ISCP PORTAL");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandTitle.setForeground(new Color(248, 250, 252));
        brandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Administrator Console");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(new Color(148, 163, 184));
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandSub.setBorder(BorderFactory.createEmptyBorder(4, 0, 30, 0));

        sidebarPanel.add(brandTitle);
        sidebarPanel.add(brandSub);

        JButton btnHome = createSidebarButton("📊  Dashboard Home", true);
        JButton btnManageStudents = createSidebarButton("🎓  Manage Students", false);
        JButton btnManageCourses = createSidebarButton("📚  Manage Courses", false);
        JButton btnManagePrograms = createSidebarButton("🏛️  Manage Programs", false);
        JButton btnManageFaculty = createSidebarButton("👥  Manage Faculty", false);
        JButton btnViewEnrollments = createSidebarButton("📋  View Enrollments", false);
        JButton btnLogout = createSidebarButton("🚪  Logout System", false);
        btnLogout.setForeground(new Color(248, 113, 113)); // Soft red accent for logout

        sidebarPanel.add(btnHome);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnManageStudents);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnManageCourses);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnManagePrograms);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnManageFaculty);
        sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnViewEnrollments);

        // Push logout button to the bottom of the sidebar
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        add(sidebarPanel, BorderLayout.WEST);

        // --- CENTER CARD LAYOUT CONTAINER ---
        cardLayout = new CardLayout();
        cardPanel = new BackgroundPanel("background.jpg", cardLayout);

        // 1. Welcome / Home Panel
        JPanel homePanel = createHomePanel();
        cardPanel.add(homePanel, "HOME");

        // 2. Management Panels
        ManagePrograms programsPanel = new ManagePrograms();
        ManageFaculty facultyPanel = new ManageFaculty();
        ManageCourses coursesPanel = new ManageCourses();
        StudentRegistration studentRegistrationPanel = new StudentRegistration(this); // Pass 'this' as parent frame

        cardPanel.add(programsPanel, "PROGRAMS");
        cardPanel.add(facultyPanel, "FACULTY");
        cardPanel.add(coursesPanel, "COURSES");
        cardPanel.add(studentRegistrationPanel, "STUDENTS");

        add(cardPanel, BorderLayout.CENTER);

        // --- NAVIGATION LISTENERS ---
        btnHome.addActionListener(e -> cardLayout.show(cardPanel, "HOME"));

        btnManageCourses.addActionListener(e -> {
            coursesPanel.loadPrograms();
            cardLayout.show(cardPanel, "COURSES");
        });

        btnManagePrograms.addActionListener(e -> {
            programsPanel.loadProgramData("");
            cardLayout.show(cardPanel, "PROGRAMS");
        });

        btnManageFaculty.addActionListener(e -> {
            facultyPanel.loadFacultyData("", "All Departments");
            cardLayout.show(cardPanel, "FACULTY");
        });

        btnManageStudents.addActionListener(e -> cardLayout.show(cardPanel, "STUDENTS"));

        btnViewEnrollments.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "View Enrollments module is coming soon!", "Information", JOptionPane.INFORMATION_MESSAGE));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out of the admin console?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
    }

    private JButton createSidebarButton(String text, boolean isActive) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover() || isActive) {
                    g2.setColor(new Color(30, 41, 59));
                } else {
                    g2.setColor(new Color(15, 23, 42));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(isActive ? new Color(96, 165, 250) : new Color(226, 232, 240));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 44));
        button.setMinimumSize(new Dimension(240, 44));
        button.setPreferredSize(new Dimension(240, 44));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isActive)
                    button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isActive)
                    button.setForeground(new Color(226, 232, 240));
            }
        });
        return button;
    }

    private JPanel createHomePanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        contentPanel.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(40, 45, 40, 45)));
        card.setPreferredSize(new Dimension(580, 340));

        JLabel titleLabel = new JLabel("Good Day, Administrator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel roleBadge = new JLabel("System Management Console");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleBadge.setForeground(new Color(37, 99, 235));
        roleBadge.setBorder(BorderFactory.createEmptyBorder(6, 0, 16, 0));

        JLabel subtitleLabel = new JLabel(
                "<html><body style='width: 470px;'>Welcome back to your central administration workspace. Oversee student enrollments, academic programs, faculty assignments, and database connection security with ease.</body></html>");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy • hh:mm a");
        JLabel dateLabel = new JLabel("📅  " + now.format(formatter));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(71, 85, 105));

        statusLabel = new JLabel("⏳ Checking Database Connection...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        checkDatabaseConnection();

        card.add(titleLabel);
        card.add(roleBadge);
        card.add(subtitleLabel);
        card.add(dateLabel);
        card.add(statusLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(card, gbc);

        return contentPanel;
    }

    private void checkDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            if (conn != null) {
                statusLabel.setText("● System Status: Database Connected (SQL Server)");
                statusLabel.setForeground(new Color(22, 163, 74));
            }
        } catch (SQLException ex) {
            statusLabel.setText("✘ System Status: Database Connection Failed");
            statusLabel.setForeground(new Color(220, 38, 38));
        }
    }
}