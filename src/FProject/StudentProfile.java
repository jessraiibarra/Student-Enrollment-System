package FProject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.sql.*;

public class StudentProfile extends JPanel {
    private String studentId;
    private Runnable onProfileUpdatedListener;

    private JTextField txtFirstName, txtMiddleName, txtLastName;
    private JTextField txtGender, txtBirthDate, txtContactNumber;
    private JTextField txtProgramId, txtYearLevel;
    private JTextField txtProvince, txtMunicipality, txtBarangay, txtStreet;
    private JTextField txtPicturePath;

    private AvatarPreviewLabel avatarPreview;
    private File selectedImageFile = null;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public StudentProfile(String studentId) {
        this.studentId = studentId;
        setOpaque(false);
        // Use GridBagLayout for center alignment so the card fits snugly and compactly
        // without dead white space
        setLayout(new GridBagLayout());

        // Main Compact White Card Container
        JPanel cardPanel = new JPanel(new BorderLayout(15, 12));
        cardPanel.setBackground(new Color(255, 255, 255, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(20, 25, 18, 25)));

        // Header Section
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Profile & Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel("View and update your personal details, address, and profile picture.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subtitleLabel);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Content: Left Avatar & Right Form Grid
        JPanel centerContent = new JPanel(new BorderLayout(20, 0));
        centerContent.setOpaque(false);

        // Left Avatar Panel
        JPanel leftAvatarPanel = new JPanel();
        leftAvatarPanel.setLayout(new BoxLayout(leftAvatarPanel, BoxLayout.Y_AXIS));
        leftAvatarPanel.setOpaque(false);
        leftAvatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPreview = new AvatarPreviewLabel();
        avatarPreview.setPreferredSize(new Dimension(95, 95));
        avatarPreview.setMaximumSize(new Dimension(95, 95));
        avatarPreview.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnChoosePhoto = new JButton("Choose Photo");
        btnChoosePhoto.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnChoosePhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnChoosePhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChoosePhoto.setMaximumSize(new Dimension(110, 26));
        btnChoosePhoto.addActionListener(e -> chooseProfilePicture());

        leftAvatarPanel.add(avatarPreview);
        leftAvatarPanel.add(Box.createVerticalStrut(10));
        leftAvatarPanel.add(btnChoosePhoto);
        centerContent.add(leftAvatarPanel, BorderLayout.WEST);

        // Right Compact Form Grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtFirstName = createTextField();
        txtMiddleName = createTextField();
        txtLastName = createTextField();
        txtGender = createTextField();
        txtBirthDate = createTextField();
        txtContactNumber = createTextField();
        txtProgramId = createTextField();
        txtYearLevel = createTextField();
        txtProvince = createTextField();
        txtMunicipality = createTextField();
        txtBarangay = createTextField();
        txtStreet = createTextField();
        txtPicturePath = createTextField();
        txtPicturePath.setEditable(false);

        int row = 0;
        addFormRow(formGrid, gbc, row++, "First Name:", txtFirstName, "Middle Name:", txtMiddleName);
        addFormRow(formGrid, gbc, row++, "Last Name:", txtLastName, "Gender:", txtGender);
        addFormRow(formGrid, gbc, row++, "Birth Date (YYYY-MM-DD):", txtBirthDate, "Contact Number:", txtContactNumber);
        addFormRow(formGrid, gbc, row++, "Program ID:", txtProgramId, "Year Level:", txtYearLevel);
        addFormRow(formGrid, gbc, row++, "Province:", txtProvince, "Municipality:", txtMunicipality);
        addFormRow(formGrid, gbc, row++, "Barangay:", txtBarangay, "Street:", txtStreet);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lblPic = new JLabel("Picture Path:");
        lblPic.setFont(new Font("Segoe UI", Font.BOLD, 11));
        formGrid.add(lblPic, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        formGrid.add(txtPicturePath, gbc);

        centerContent.add(formGrid, BorderLayout.CENTER);
        cardPanel.add(centerContent, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomActionPanel.setOpaque(false);

        JButton btnReload = new JButton("Reload Data");
        btnReload.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReload.addActionListener(e -> loadStudentData());

        JButton btnSave = new JButton("Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveStudentData());

        bottomActionPanel.add(btnReload);
        bottomActionPanel.add(btnSave);
        cardPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        // Wrap card in container to constrain its size nicely
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.anchor = GridBagConstraints.CENTER;
        add(cardPanel, outerGbc);

        loadStudentData();
    }

    public void setOnProfileUpdatedListener(Runnable listener) {
        this.onProfileUpdatedListener = listener;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setPreferredSize(new Dimension(175, 24));
        return tf;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String lbl1, JTextField tf1, String lbl2,
            JTextField tf2) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel l1 = new JLabel(lbl1);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        panel.add(l1, gbc);

        gbc.gridx = 1;
        panel.add(tf1, gbc);

        gbc.gridx = 2;
        JLabel l2 = new JLabel(lbl2);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        panel.add(l2, gbc);

        gbc.gridx = 3;
        panel.add(tf2, gbc);
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            txtPicturePath.setText(selectedImageFile.getAbsolutePath());
            avatarPreview.setImage(new ImageIcon(selectedImageFile.getAbsolutePath()).getImage());
        }
    }

    public void loadStudentData() {
        if (studentId == null || studentId.trim().isEmpty())
            return;

        // Correct column name: profile_picture
        String query = "SELECT first_name, middle_name, last_name, gender, birth_date, contact_number, " +
                "program_id, year_level, province, municipality, barangay, street, profile_picture " +
                "FROM student WHERE student_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, studentId.trim());

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    txtFirstName.setText(rs.getString("first_name"));
                    txtMiddleName.setText(rs.getString("middle_name"));
                    txtLastName.setText(rs.getString("last_name"));
                    txtGender.setText(rs.getString("gender"));
                    txtBirthDate.setText(rs.getString("birth_date"));
                    txtContactNumber.setText(rs.getString("contact_number"));

                    int progId = rs.getInt("program_id");
                    txtProgramId.setText(rs.wasNull() ? "" : String.valueOf(progId));

                    int yrLvl = rs.getInt("year_level");
                    txtYearLevel.setText(rs.wasNull() ? "" : String.valueOf(yrLvl));

                    txtProvince.setText(rs.getString("province"));
                    txtMunicipality.setText(rs.getString("municipality"));
                    txtBarangay.setText(rs.getString("barangay"));
                    txtStreet.setText(rs.getString("street"));

                    String picPath = rs.getString("profile_picture");
                    txtPicturePath.setText(picPath != null ? picPath : "");

                    if (picPath != null && !picPath.trim().isEmpty()) {
                        File f = new File(picPath.trim());
                        if (f.exists()) {
                            selectedImageFile = f;
                            avatarPreview.setImage(new ImageIcon(f.getAbsolutePath()).getImage());
                        } else {
                            avatarPreview.setImage(null);
                        }
                    } else {
                        avatarPreview.setImage(null);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile data: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveStudentData() {
        // Correct column name: profile_picture
        String query = "UPDATE student SET first_name = ?, middle_name = ?, last_name = ?, gender = ?, " +
                "birth_date = ?, contact_number = ?, program_id = ?, year_level = ?, " +
                "province = ?, municipality = ?, barangay = ?, street = ?, profile_picture = ? " +
                "WHERE student_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, txtFirstName.getText().trim());
            pst.setString(2, txtMiddleName.getText().trim());
            pst.setString(3, txtLastName.getText().trim());
            pst.setString(4, txtGender.getText().trim());

            String bDate = txtBirthDate.getText().trim();
            if (bDate.isEmpty()) {
                pst.setNull(5, java.sql.Types.DATE);
            } else {
                pst.setDate(5, java.sql.Date.valueOf(bDate));
            }

            pst.setString(6, txtContactNumber.getText().trim());

            String progStr = txtProgramId.getText().trim();
            if (progStr.isEmpty()) {
                pst.setNull(7, java.sql.Types.INTEGER);
            } else {
                pst.setInt(7, Integer.parseInt(progStr));
            }

            String yrStr = txtYearLevel.getText().trim();
            if (yrStr.isEmpty()) {
                pst.setNull(8, java.sql.Types.INTEGER);
            } else {
                pst.setInt(8, Integer.parseInt(yrStr));
            }

            pst.setString(9, txtProvince.getText().trim());
            pst.setString(10, txtMunicipality.getText().trim());
            pst.setString(11, txtBarangay.getText().trim());
            pst.setString(12, txtStreet.getText().trim());
            pst.setString(13, txtPicturePath.getText().trim());
            pst.setString(14, studentId.trim());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                if (onProfileUpdatedListener != null) {
                    onProfileUpdatedListener.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile record.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving profile: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class AvatarPreviewLabel extends JLabel {
        private Image img = null;

        public void setImage(Image img) {
            this.img = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));

            if (img != null) {
                g2.drawImage(img, 0, 0, size, size, this);
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.fillRect(0, 0, size, size);
                g2.setColor(new Color(71, 85, 105));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.drawString("?", size / 2 - 6, size / 2 + 8);
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