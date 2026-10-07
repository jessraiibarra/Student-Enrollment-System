package FProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;

public class loginwindow extends JFrame {
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;

    // Custom background panel with dark gradient overlay for visual depth
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            backgroundImage = loadImage("iscp_bg.jpg");
        }

        private Image loadImage(String fileName) {
            try {
                URL imageURL = getClass().getResource("/FProject/" + fileName);
                if (imageURL != null) {
                    return new ImageIcon(imageURL).getImage();
                } else {
                    return new ImageIcon(fileName).getImage();
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // Semi-transparent dark overlay for high contrast and modern look
                g2d.setColor(new Color(15, 23, 42, 160));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2d.setPaint(
                        new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public loginwindow() {
        setTitle("International State College of the Philippines - Login Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen view
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        // --- LEFT PANEL: Branding (Logo, Title, Subtitle) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel sealWrapper = new JPanel();
        sealWrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sealWrapper.setOpaque(false);
        sealWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        sealWrapper.setMaximumSize(new Dimension(720, 200));

        JLabel lblSeal = new JLabel();
        ImageIcon sealIcon = loadIcon("iscp_seal.png");
        if (sealIcon != null) {
            Image scaledImage = sealIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            lblSeal.setIcon(new ImageIcon(scaledImage));
        }
        sealWrapper.add(lblSeal);

        JLabel lblTitle = new JLabel("International State College of the Philippines");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Official Student Portal & Enrollment System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(720, 12));
        separator.setForeground(new Color(255, 255, 255, 50));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(sealWrapper);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(lblTitle);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(lblSubtitle);
        leftPanel.add(Box.createVerticalStrut(16));
        leftPanel.add(separator);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.60;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 60, 0, 30);
        backgroundPanel.add(leftPanel, gbc);

        // --- RIGHT PANEL: High-End Frosted Glass Form Card ---
        JPanel rightCardWrapper = new JPanel(new GridBagLayout());
        rightCardWrapper.setOpaque(false);
        rightCardWrapper.setPreferredSize(new Dimension(420, 480));
        rightCardWrapper.setMaximumSize(new Dimension(420, 480));

        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Frosted card background (Semi-transparent dark slate)
                g2.setColor(new Color(30, 41, 59, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)));

        JLabel formTitle = new JLabel("System Login");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        formTitle.setForeground(Color.WHITE);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formSub = new JLabel("Enter your credentials to access portal");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(new Color(148, 163, 184));
        formSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        formSub.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));

        JLabel roleLabel = new JLabel("Select Role:");
        styleFormLabel(roleLabel);

        String[] roles = { "Admin", "Student", "Faculty" };
        roleComboBox = new JComboBox<>(roles);
        styleFormInput(roleComboBox);

        JLabel userLabel = new JLabel("Username:");
        styleFormLabel(userLabel);

        userTextField = new JTextField();
        styleFormInput(userTextField);

        JLabel passLabel = new JLabel("Password:");
        styleFormLabel(passLabel);

        passwordField = new JPasswordField();
        styleFormInput(passwordField);

        JButton loginButton = new JButton("Login to Portal");
        styleActionButton(loginButton, new Color(37, 99, 235), Color.WHITE);

        rightPanel.add(formTitle);
        rightPanel.add(formSub);
        rightPanel.add(roleLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(roleComboBox);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(userLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(userTextField);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(passLabel);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(passwordField);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(loginButton);

        rightCardWrapper.add(rightPanel);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.40;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 20, 0, 60);
        backgroundPanel.add(rightCardWrapper, gbc);

        // Login Action Logic
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = (String) roleComboBox.getSelectedItem();
                String username = userTextField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                authenticateUser(role, username, password);
            }
        });
    }

    private ImageIcon loadIcon(String fileName) {
        try {
            URL imageURL = getClass().getResource("/FProject/" + fileName);
            if (imageURL != null) {
                return new ImageIcon(imageURL);
            } else {
                return new ImageIcon(fileName);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void styleFormLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(226, 232, 240));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleFormInput(JComponent comp) {
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comp.setMaximumSize(new Dimension(350, 38));
        comp.setPreferredSize(new Dimension(350, 38));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(350, 44));
        btn.setPreferredSize(new Dimension(350, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtle hover response for interactivity
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(29, 78, 216)); // Darker blue on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    private void authenticateUser(String role, String username, String password) {
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
        String dbUser = "sa";
        String dbPass = "Jessrai08140410!";

        String query = "";
        if (role.equals("Admin")) {
            query = "SELECT * FROM admin_logins WHERE username = ? AND password = ?";
        } else if (role.equals("Student")) {
            query = "SELECT * FROM student_logins WHERE username = ? AND password = ?";
        } else if (role.equals("Faculty")) {
            query = "SELECT * FROM faculty_logins WHERE username = ? AND password = ?";
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null, role + " Login Successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();

                if (role.equals("Admin")) {
                    new AdminDashboard().setVisible(true);
                } else if (role.equals("Student")) {
                    new StudentDashboard(username).setVisible(true);
                } else if (role.equals("Faculty")) {
                    new FacultyDashboard(username).setVisible(true);
                }

            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.", "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}