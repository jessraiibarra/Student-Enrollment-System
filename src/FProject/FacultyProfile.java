package FProject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FacultyProfile extends JPanel {
    private String facultyId;
    private JLabel lblFacultyId, lblFullName, lblAvatar;
    private JTextField txtDepartment, txtEmail, txtAvatarFile;
    private Image avatarImage = null;

    // Callback listener to notify the parent frame (sidebar) in real-time
    private Runnable onProfileUpdatedListener;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public FacultyProfile(String facultyId) {
        this.facultyId = (facultyId != null) ? facultyId.trim() : "";

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(25, 35, 25, 35)));
        card.setPreferredSize(new Dimension(840, 560));

        // --- HEADER TITLE ---
        JPanel headerGroup = new JPanel();
        headerGroup.setLayout(new BoxLayout(headerGroup, BoxLayout.Y_AXIS));
        headerGroup.setOpaque(false);

        JLabel titleLabel = new JLabel("Faculty Profile & Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel(
                "View your institutional credentials, update your department, email, and profile picture.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        headerGroup.add(titleLabel);
        headerGroup.add(Box.createVerticalStrut(4));
        headerGroup.add(subtitleLabel);
        card.add(headerGroup, BorderLayout.NORTH);

        // --- MAIN CONTENT PANEL ---
        JPanel contentPanel = new JPanel(new BorderLayout(25, 0));
        contentPanel.setOpaque(false);

        // Left Side: Avatar & Upload Button
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setClip(new Ellipse2D.Float(0, 0, size, size));

                if (avatarImage != null) {
                    g2.drawImage(avatarImage, 0, 0, size, size, this);
                } else {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillRect(0, 0, size, size);
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                    g2.drawString("?", size / 2 - 10, size / 2 + 12);
                }
                g2.dispose();

                // Draw circular border
                Graphics2D g2Border = (Graphics2D) g.create();
                g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2Border.setColor(new Color(96, 165, 250));
                g2Border.setStroke(new BasicStroke(2));
                g2Border.drawOval(1, 1, size - 2, size - 2);
                g2Border.dispose();
            }
        };
        lblAvatar.setPreferredSize(new Dimension(110, 110));
        lblAvatar.setMaximumSize(new Dimension(110, 110));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnUploadPhoto = new JButton("Choose Photo");
        btnUploadPhoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUploadPhoto.setBackground(new Color(241, 245, 249));
        btnUploadPhoto.setForeground(new Color(51, 65, 85));
        btnUploadPhoto.setFocusPainted(false);
        btnUploadPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUploadPhoto.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnUploadPhoto.addActionListener(e -> chooseProfilePicture());

        avatarPanel.add(Box.createVerticalStrut(10));
        avatarPanel.add(lblAvatar);
        avatarPanel.add(Box.createVerticalStrut(12));
        avatarPanel.add(btnUploadPhoto);

        // Right Side: Form Inputs
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Faculty ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblIdTitle = new JLabel("Faculty ID:");
        lblIdTitle.setFont(labelFont);
        lblIdTitle.setForeground(new Color(51, 65, 85));
        formPanel.add(lblIdTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        lblFacultyId = new JLabel(this.facultyId);
        lblFacultyId.setFont(fieldFont);
        lblFacultyId.setForeground(new Color(37, 99, 235));
        formPanel.add(lblFacultyId, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblNameTitle = new JLabel("Full Name:");
        lblNameTitle.setFont(labelFont);
        lblNameTitle.setForeground(new Color(51, 65, 85));
        formPanel.add(lblNameTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        lblFullName = new JLabel("Loading...");
        lblFullName.setFont(fieldFont);
        formPanel.add(lblFullName, gbc);

        // Separator
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(226, 232, 240));
        formPanel.add(separator, gbc);
        gbc.gridwidth = 1;

        // Department
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        JLabel lblDeptTitle = new JLabel("Department:");
        lblDeptTitle.setFont(labelFont);
        lblDeptTitle.setForeground(new Color(51, 65, 85));
        formPanel.add(lblDeptTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.7;
        txtDepartment = new JTextField();
        txtDepartment.setFont(fieldFont);
        formPanel.add(txtDepartment, gbc);

        // Email Address
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        JLabel lblEmailTitle = new JLabel("Email Address:");
        lblEmailTitle.setFont(labelFont);
        lblEmailTitle.setForeground(new Color(51, 65, 85));
        formPanel.add(lblEmailTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 0.7;
        txtEmail = new JTextField();
        txtEmail.setFont(fieldFont);
        formPanel.add(txtEmail, gbc);

        // Profile Picture File Path
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        JLabel lblAvatarTitle = new JLabel("Picture Path:");
        lblAvatarTitle.setFont(labelFont);
        lblAvatarTitle.setForeground(new Color(51, 65, 85));
        formPanel.add(lblAvatarTitle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 0.7;
        txtAvatarFile = new JTextField();
        txtAvatarFile.setFont(fieldFont);
        formPanel.add(txtAvatarFile, gbc);

        contentPanel.add(avatarPanel, BorderLayout.WEST);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        card.add(contentPanel, BorderLayout.CENTER);

        // --- BUTTONS PANEL ---
        JButton btnSave = new JButton("Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        JButton btnReload = new JButton("Reload Data");
        btnReload.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReload.setBackground(new Color(241, 245, 249));
        btnReload.setForeground(new Color(71, 85, 105));
        btnReload.setFocusPainted(false);
        btnReload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReload.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnReload);
        bottomPanel.add(btnSave);
        card.add(bottomPanel, BorderLayout.SOUTH);

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        add(card, gbcMain);

        loadProfileData();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadProfileData();
            }
        });

        btnReload.addActionListener(e -> loadProfileData());
        btnSave.addActionListener(e -> saveProfileChanges());
    }

    // Method to set the listener from your main dashboard window
    public void setOnProfileUpdatedListener(Runnable listener) {
        this.onProfileUpdatedListener = listener;
    }

    private void loadAvatarImage(String filePath) {
        if (filePath != null && !filePath.trim().isEmpty()) {
            try {
                File file = new File(filePath.trim());
                if (file.exists()) {
                    avatarImage = new ImageIcon(file.getAbsolutePath()).getImage();
                } else {
                    URL url = getClass().getResource("/FProject/" + file.getName());
                    if (url != null) {
                        avatarImage = new ImageIcon(url).getImage();
                    } else {
                        avatarImage = null;
                    }
                }
            } catch (Exception e) {
                avatarImage = null;
            }
        } else {
            avatarImage = null;
        }
        lblAvatar.repaint();
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            txtAvatarFile.setText(selectedFile.getAbsolutePath());
            loadAvatarImage(selectedFile.getAbsolutePath());
        }
    }

    private void loadProfileData() {
        if (facultyId == null || facultyId.isEmpty()) {
            lblFullName.setText("No Faculty ID Provided");
            return;
        }

        String query = "SELECT first_name, last_name, department, email, profile_picture FROM faculty " +
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
                    lblFullName
                            .setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    txtDepartment.setText(rs.getString("department"));
                    txtEmail.setText(rs.getString("email"));

                    String picPath = rs.getString("profile_picture");
                    txtAvatarFile.setText(picPath != null ? picPath : "");
                    loadAvatarImage(picPath);
                } else {
                    lblFullName.setText("Record Not Found");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveProfileChanges() {
        String department = txtDepartment.getText().trim();
        String email = txtEmail.getText().trim();
        String avatarFile = txtAvatarFile.getText().trim();

        String query = "UPDATE faculty SET department = ?, email = ?, profile_picture = ? " +
                "WHERE faculty_id = ? OR email = ? OR email LIKE ? + '@%'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, department);
            pst.setString(2, email);
            pst.setString(3, avatarFile);
            pst.setString(4, facultyId);
            pst.setString(5, facultyId);
            pst.setString(6, facultyId);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadProfileData();

                // Trigger the real-time update in the sidebar/parent frame
                if (onProfileUpdatedListener != null) {
                    onProfileUpdatedListener.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile. Record not found.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}