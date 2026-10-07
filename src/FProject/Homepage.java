package FProject;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Homepage extends JFrame {

    // Custom background panel that scales the ISCP campus background and handles
    // proportional sizing
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

            // Draw full-screen background with a rich dark gradient overlay for depth
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // Semi-transparent dark overlay to make text high contrast and readable
                g2d.setColor(new Color(15, 23, 42, 175));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2d.setPaint(
                        new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public Homepage() {
        setTitle("International State College of the Philippines - ISCP");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen view
        setMinimumSize(new Dimension(1100, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set custom background panel as content pane
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // Main layout container using GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();

        // --- LEFT PANEL: Fully Left-Aligned (Logo, Title, Subtitle, Mission, Vision)
        // ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false); // Fully transparent background
        leftPanel.setMaximumSize(new Dimension(640, 640));

        // 1. Prominent Logo / Seal Image (Left-Aligned Wrapper) - Increased size to
        // 180x180
        JPanel sealWrapper = new JPanel();
        sealWrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sealWrapper.setOpaque(false);
        sealWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        sealWrapper.setMaximumSize(new Dimension(640, 195));

        JLabel lblSeal = new JLabel();
        ImageIcon sealIcon = loadIcon("iscp_seal.png");
        if (sealIcon != null) {
            Image scaledImage = sealIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            lblSeal.setIcon(new ImageIcon(scaledImage));
        }
        sealWrapper.add(lblSeal);

        // 2. ISCP Name & Subtitle (Left-Aligned)
        JLabel lblTitle = new JLabel("International State College of the Philippines");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Official Student Portal & Enrollment System");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(640, 10));
        separator.setForeground(new Color(255, 255, 255, 50));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Mission & Vision Container (Left-Aligned)
        JPanel missionVisionContainer = new JPanel();
        missionVisionContainer.setLayout(new BoxLayout(missionVisionContainer, BoxLayout.Y_AXIS));
        missionVisionContainer.setOpaque(false);
        missionVisionContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        missionVisionContainer.setMaximumSize(new Dimension(640, 300));

        JLabel lblMissionHeader = new JLabel("Our Mission");
        lblMissionHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMissionHeader.setForeground(new Color(96, 165, 250)); // Modern Accent Blue
        lblMissionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMissionText = new JLabel(
                "<html><div style='text-align: left; width: 600px;'>To provide accessible, high-tech, and globally competitive education that empowers students with cutting-edge knowledge, professional skills, and unwavering moral character to lead in an evolving digital world.</div></html>");
        styleResponsiveLabel(lblMissionText);

        JLabel lblVisionHeader = new JLabel("Our Vision");
        lblVisionHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblVisionHeader.setForeground(new Color(96, 165, 250));
        lblVisionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVisionText = new JLabel(
                "<html><div style='text-align: left; width: 600px;'>To be a premier international institution of excellence, recognized for innovation in academic programs, research leadership, and commitment to community transformation.</div></html>");
        styleResponsiveLabel(lblVisionText);

        missionVisionContainer.add(lblMissionHeader);
        missionVisionContainer.add(Box.createVerticalStrut(3));
        missionVisionContainer.add(lblMissionText);
        missionVisionContainer.add(Box.createVerticalStrut(14));
        missionVisionContainer.add(lblVisionHeader);
        missionVisionContainer.add(Box.createVerticalStrut(3));
        missionVisionContainer.add(lblVisionText);

        // Assemble Left Panel
        leftPanel.add(sealWrapper);
        leftPanel.add(Box.createVerticalStrut(6));
        leftPanel.add(lblTitle);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(lblSubtitle);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(separator);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(missionVisionContainer);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.58;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 80, 0, 40);
        backgroundPanel.add(leftPanel, gbc);

        // --- RIGHT PANEL: High-End Frosted Glass Action Card ---
        JPanel rightCardWrapper = new JPanel(new GridBagLayout());
        rightCardWrapper.setOpaque(false);
        rightCardWrapper.setPreferredSize(new Dimension(420, 320));
        rightCardWrapper.setMaximumSize(new Dimension(420, 320));

        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Frosted card background (Semi-transparent dark slate)
                g2.setColor(new Color(30, 41, 59, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)));

        JLabel lblWelcomeBack = new JLabel("Welcome Portal");
        lblWelcomeBack.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcomeBack.setForeground(Color.WHITE);
        lblWelcomeBack.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHasAccount = new JLabel("Access your account to continue");
        lblHasAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHasAccount.setForeground(new Color(148, 163, 184));
        lblHasAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHasAccount.setBorder(BorderFactory.createEmptyBorder(6, 0, 28, 0));

        JButton btnLogin = new JButton("Login to Portal");
        styleActionButton(btnLogin, new Color(37, 99, 235), Color.WHITE);

        rightPanel.add(lblWelcomeBack);
        rightPanel.add(lblHasAccount);
        rightPanel.add(btnLogin);

        rightCardWrapper.add(rightPanel);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.42;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 20, 0, 80);
        backgroundPanel.add(rightCardWrapper, gbc);

        // --- ACTION LISTENER ---
        btnLogin.addActionListener(e -> {
            new loginwindow().setVisible(true);
            dispose();
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

    private void styleResponsiveLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(226, 232, 240));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(640, 100));
    }

    private void styleActionButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(320, 48));
        btn.setPreferredSize(new Dimension(320, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add subtle hover response for interactivity
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(29, 78, 216)); // Darker shade of blue on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Homepage().setVisible(true);
        });
    }
}