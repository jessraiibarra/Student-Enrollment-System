package FProject;

import com.toedter.calendar.JDateChooser;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class EditStudentDialog extends JDialog {
    private JTextField idTextField, firstNameField, middleNameField, lastNameField, streetField, contactField;
    private JComboBox<StudentRegistration.ProgramItem> programComboBox;

    private JComboBox<String> sexComboBox;
    private JDateChooser birthDateChooser;
    private JComboBox<String> provinceComboBox;
    private JComboBox<String> municipalityComboBox;
    private JComboBox<String> barangayComboBox;

    private JSONObject addressDataRoot;
    private String studentIdToEdit;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    // Custom background panel para sa parehong tema ng UI
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String fileName) {
            try {
                URL imageURL = getClass().getResource("/FProject/" + fileName);
                if (imageURL != null) {
                    backgroundImage = new ImageIcon(imageURL).getImage();
                } else {
                    backgroundImage = new ImageIcon(fileName).getImage();
                }
            } catch (Exception e) {
                backgroundImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2d.setPaint(
                        new GradientPaint(0, 0, new Color(135, 206, 235), 0, getHeight(), new Color(70, 130, 180)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    public EditStudentDialog(JFrame parent, String studentId) {
        super(parent, "ISCP - Edit Student Details", true);
        this.studentIdToEdit = studentId;
        setSize(700, 780);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Itakda ang custom background panel bilang content pane
        BackgroundPanel mainPanel = new BackgroundPanel("iscp_bg.jpg");
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        // --- FROSTED CARD CONTAINER ---
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(new Color(255, 255, 255, 240)); // Frosted semi-transparent card style
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));
        cardPanel.setPreferredSize(new Dimension(580, 690));

        // Title Header
        JLabel titleLabel = new JLabel("Edit Student Details", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Fields Container (GridBagLayout para sa tumpak na alignment)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize components
        idTextField = new JTextField(studentId);
        idTextField.setEditable(false);
        idTextField.setBackground(new Color(226, 232, 240));
        idTextField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        firstNameField = new JTextField();
        middleNameField = new JTextField();
        lastNameField = new JTextField();

        String[] sexOptions = { "Male", "Female", "Prefer not to say" };
        sexComboBox = new JComboBox<>(sexOptions);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");

        provinceComboBox = new JComboBox<>();
        municipalityComboBox = new JComboBox<>();
        barangayComboBox = new JComboBox<>();

        loadAddressJsonData();
        setupDynamicAddressListeners();

        streetField = new JTextField();
        contactField = new JTextField();
        programComboBox = new JComboBox<>();
        loadPrograms();

        // Style standard text fields/comboboxes
        styleComponent(firstNameField);
        styleComponent(middleNameField);
        styleComponent(lastNameField);
        styleComponent(sexComboBox);
        styleComponent(provinceComboBox);
        styleComponent(municipalityComboBox);
        styleComponent(barangayComboBox);
        styleComponent(streetField);
        styleComponent(contactField);
        styleComponent(programComboBox);

        // Add fields to form panel grid
        int row = 0;
        addFormRow(formPanel, gbc, row++, "Student ID (Locked):", idTextField);
        addFormRow(formPanel, gbc, row++, "First Name:", firstNameField);
        addFormRow(formPanel, gbc, row++, "Middle Name:", middleNameField);
        addFormRow(formPanel, gbc, row++, "Last Name:", lastNameField);
        addFormRow(formPanel, gbc, row++, "Sex:", sexComboBox);
        addFormRow(formPanel, gbc, row++, "Birth Date:", birthDateChooser);
        addFormRow(formPanel, gbc, row++, "Province:", provinceComboBox);
        addFormRow(formPanel, gbc, row++, "Municipality/City:", municipalityComboBox);
        addFormRow(formPanel, gbc, row++, "Barangay:", barangayComboBox);
        addFormRow(formPanel, gbc, row++, "House No. & Street:", streetField);
        addFormRow(formPanel, gbc, row++, "Contact Number:", contactField);
        addFormRow(formPanel, gbc, row++, "Program:", programComboBox);

        cardPanel.add(formPanel, BorderLayout.CENTER);

        // South Panel para sa Save Button
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        southPanel.setOpaque(false);

        JButton saveButton = new JButton("Save Changes");
        styleButton(saveButton, new Color(37, 99, 235), Color.WHITE);
        saveButton.setPreferredSize(new Dimension(150, 38));
        southPanel.add(saveButton);

        cardPanel.add(southPanel, BorderLayout.SOUTH);

        // Idagdag ang cardPanel sa gitna ng main window
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(cardPanel, mainGbc);

        // Load existing student data into fields
        loadStudentDetailsToForm();

        saveButton.addActionListener(e -> updateStudent());
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void styleComponent(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(300, 28));
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    private void loadAddressJsonData() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("assets/ph_addresses.json")));
            addressDataRoot = new JSONObject(content);

            provinceComboBox.removeAllItems();
            provinceComboBox.addItem("-- Select Province --");

            List<String> provinceListNames = new ArrayList<>();
            Iterator<String> regionKeys = addressDataRoot.keys();
            while (regionKeys.hasNext()) {
                JSONObject regionObj = addressDataRoot.getJSONObject(regionKeys.next());
                if (regionObj.has("province_list")) {
                    JSONObject provinceList = regionObj.getJSONObject("province_list");
                    Iterator<String> provKeys = provinceList.keys();
                    while (provKeys.hasNext()) {
                        provinceListNames.add(provKeys.next());
                    }
                }
            }
            Collections.sort(provinceListNames);
            for (String prov : provinceListNames) {
                provinceComboBox.addItem(prov);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading local address file: " + e.getMessage(), "Asset Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDynamicAddressListeners() {
        provinceComboBox.addActionListener(e -> {
            String selectedProv = (String) provinceComboBox.getSelectedItem();
            municipalityComboBox.removeAllItems();
            barangayComboBox.removeAllItems();
            municipalityComboBox.addItem("-- Select Municipality --");
            barangayComboBox.addItem("-- Select Barangay --");

            if (selectedProv == null || selectedProv.startsWith("--"))
                return;

            try {
                List<String> munListNames = new ArrayList<>();
                Iterator<String> regionKeys = addressDataRoot.keys();
                while (regionKeys.hasNext()) {
                    JSONObject regionObj = addressDataRoot.getJSONObject(regionKeys.next());
                    if (regionObj.has("province_list")) {
                        JSONObject provinceList = regionObj.getJSONObject("province_list");
                        if (provinceList.has(selectedProv)) {
                            JSONObject provObj = provinceList.getJSONObject(selectedProv);
                            if (provObj.has("municipality_list")) {
                                Object munObj = provObj.get("municipality_list");
                                if (munObj instanceof JSONArray) {
                                    JSONArray munArray = (JSONArray) munObj;
                                    for (int i = 0; i < munArray.length(); i++) {
                                        JSONObject munItem = munArray.getJSONObject(i);
                                        if (munItem.has("municipality_name")) {
                                            munListNames.add(munItem.getString("municipality_name"));
                                        } else {
                                            Iterator<String> keys = munItem.keys();
                                            while (keys.hasNext())
                                                munListNames.add(keys.next());
                                        }
                                    }
                                } else if (munObj instanceof JSONObject) {
                                    JSONObject munList = (JSONObject) munObj;
                                    Iterator<String> munKeys = munList.keys();
                                    while (munKeys.hasNext())
                                        munListNames.add(munKeys.next());
                                }
                            }
                            break;
                        }
                    }
                }
                Collections.sort(munListNames);
                for (String mun : munListNames)
                    municipalityComboBox.addItem(mun);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        municipalityComboBox.addActionListener(e -> {
            String selectedProv = (String) provinceComboBox.getSelectedItem();
            String selectedMun = (String) municipalityComboBox.getSelectedItem();
            barangayComboBox.removeAllItems();
            barangayComboBox.addItem("-- Select Barangay --");

            if (selectedMun == null || selectedMun.startsWith("--") || selectedProv == null
                    || selectedProv.startsWith("--"))
                return;

            try {
                Object brgyObj = null;
                Iterator<String> regionKeys = addressDataRoot.keys();
                while (regionKeys.hasNext()) {
                    JSONObject regionObj = addressDataRoot.getJSONObject(regionKeys.next());
                    if (regionObj.has("province_list")) {
                        JSONObject provinceList = regionObj.getJSONObject("province_list");
                        if (provinceList.has(selectedProv)) {
                            JSONObject provObj = provinceList.getJSONObject(selectedProv);
                            if (provObj.has("municipality_list")) {
                                Object munObj = provObj.get("municipality_list");
                                if (munObj instanceof JSONArray) {
                                    JSONArray munArray = (JSONArray) munObj;
                                    for (int i = 0; i < munArray.length(); i++) {
                                        JSONObject munItem = munArray.getJSONObject(i);
                                        if (munItem.has(selectedMun)) {
                                            brgyObj = munItem.getJSONObject(selectedMun).get("barangay_list");
                                            break;
                                        } else if (munItem.has("municipality_name")
                                                && munItem.getString("municipality_name").equals(selectedMun)) {
                                            brgyObj = munItem.get("barangay_list");
                                            break;
                                        }
                                    }
                                } else if (munObj instanceof JSONObject) {
                                    JSONObject munList = (JSONObject) munObj;
                                    if (munList.has(selectedMun)) {
                                        brgyObj = munList.getJSONObject(selectedMun).get("barangay_list");
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                List<String> brgyListNames = new ArrayList<>();
                if (brgyObj instanceof JSONArray) {
                    JSONArray brgyArray = (JSONArray) brgyObj;
                    for (int i = 0; i < brgyArray.length(); i++) {
                        Object brgyItem = brgyArray.get(i);
                        if (brgyItem instanceof String)
                            brgyListNames.add((String) brgyItem);
                        else if (brgyItem instanceof JSONObject) {
                            JSONObject bObj = (JSONObject) brgyItem;
                            if (bObj.has("barangay_name"))
                                brgyListNames.add(bObj.getString("barangay_name"));
                            else {
                                Iterator<String> keys = bObj.keys();
                                if (keys.hasNext())
                                    brgyListNames.add(keys.next());
                            }
                        }
                    }
                }
                Collections.sort(brgyListNames);
                for (String brgy : brgyListNames)
                    barangayComboBox.addItem(brgy);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadPrograms() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT program_id, program_code FROM program")) {
            while (rs.next()) {
                programComboBox.addItem(
                        new StudentRegistration.ProgramItem(rs.getInt("program_id"), rs.getString("program_code")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStudentDetailsToForm() {
        String query = "SELECT * FROM student WHERE student_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, studentIdToEdit);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    firstNameField.setText(rs.getString("first_name"));
                    middleNameField.setText(rs.getString("middle_name"));
                    lastNameField.setText(rs.getString("last_name"));
                    sexComboBox.setSelectedItem(rs.getString("gender"));
                    birthDateChooser.setDate(rs.getDate("birth_date"));
                    provinceComboBox.setSelectedItem(rs.getString("province"));
                    municipalityComboBox.setSelectedItem(rs.getString("municipality"));
                    barangayComboBox.setSelectedItem(rs.getString("barangay"));
                    streetField.setText(rs.getString("street"));
                    contactField.setText(rs.getString("contact_number"));

                    int progId = rs.getInt("program_id");
                    for (int i = 0; i < programComboBox.getItemCount(); i++) {
                        if (programComboBox.getItemAt(i).id == progId) {
                            programComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading student details: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        String fname = firstNameField.getText().trim();
        String mname = middleNameField.getText().trim();
        String lname = lastNameField.getText().trim();
        String sex = (String) sexComboBox.getSelectedItem();
        java.util.Date birthDate = birthDateChooser.getDate();
        String province = (String) provinceComboBox.getSelectedItem();
        String municipality = (String) municipalityComboBox.getSelectedItem();
        String barangay = (String) barangayComboBox.getSelectedItem();
        String street = streetField.getText().trim();
        String contact = contactField.getText().trim();
        StudentRegistration.ProgramItem program = (StudentRegistration.ProgramItem) programComboBox.getSelectedItem();

        if (fname.isEmpty() || lname.isEmpty() || birthDate == null || program == null) {
            JOptionPane.showMessageDialog(this, "Please fill in required fields.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "UPDATE student SET first_name = ?, middle_name = ?, last_name = ?, gender = ?, birth_date = ?, street = ?, province = ?, municipality = ?, barangay = ?, contact_number = ?, program_id = ? WHERE student_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, fname);
            pst.setString(2, mname.isEmpty() ? null : mname);
            pst.setString(3, lname);
            pst.setString(4, sex);
            pst.setDate(5, new java.sql.Date(birthDate.getTime()));
            pst.setString(6, street.isEmpty() ? null : street);
            pst.setString(7, province);
            pst.setString(8, municipality);
            pst.setString(9, barangay);
            pst.setString(10, contact.isEmpty() ? null : contact);
            pst.setInt(11, program.id);
            pst.setString(12, studentIdToEdit);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student updated successfully!");
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}