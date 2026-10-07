package FProject;

import com.toedter.calendar.JDateChooser;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Year;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class AddStudentDialog extends JDialog {
    private JTextField idTextField, firstNameField, middleNameField, lastNameField, streetField, contactField;
    private JPasswordField passwordField;
    private JComboBox<StudentRegistration.ProgramItem> programComboBox;

    private JComboBox<String> sexComboBox;
    private JDateChooser birthDateChooser;
    private JComboBox<String> provinceComboBox;
    private JComboBox<String> municipalityComboBox;
    private JComboBox<String> barangayComboBox;

    private JSONObject addressDataRoot;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public AddStudentDialog(JFrame parent) {
        super(parent, "ISCP - Add New Student", true);
        setSize(580, 720);
        setLocationRelativeTo(parent);
        setLayout(null);

        // Title
        JLabel titleLabel = new JLabel("Student Registration Form");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBounds(170, 20, 250, 30);
        add(titleLabel);

        // Student ID
        JLabel idLabel = new JLabel("Student ID:");
        idLabel.setBounds(50, 70, 140, 25);
        add(idLabel);

        idTextField = new JTextField();
        idTextField.setBounds(200, 70, 300, 25);
        idTextField.setEditable(false);
        idTextField.setBackground(Color.LIGHT_GRAY);
        generateStudentId();
        add(idTextField);

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds(50, 110, 140, 25);
        add(firstNameLabel);

        firstNameField = new JTextField();
        firstNameField.setBounds(200, 110, 300, 25);
        add(firstNameField);

        // Middle Name
        JLabel middleNameLabel = new JLabel("Middle Name:");
        middleNameLabel.setBounds(50, 150, 140, 25);
        add(middleNameLabel);

        middleNameField = new JTextField();
        middleNameField.setBounds(200, 150, 300, 25);
        add(middleNameField);

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(50, 190, 140, 25);
        add(lastNameLabel);

        lastNameField = new JTextField();
        lastNameField.setBounds(200, 190, 300, 25);
        add(lastNameField);

        // Sex Dropdown
        JLabel sexLabel = new JLabel("Sex:");
        sexLabel.setBounds(50, 230, 140, 25);
        add(sexLabel);

        String[] sexOptions = { "Male", "Female", "Prefer not to say" };
        sexComboBox = new JComboBox<>(sexOptions);
        sexComboBox.setBounds(200, 230, 300, 25);
        add(sexComboBox);

        // Birth Date Calendar Picker
        JLabel birthLabel = new JLabel("Birth Date:");
        birthLabel.setBounds(50, 270, 140, 25);
        add(birthLabel);

        birthDateChooser = new JDateChooser();
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        birthDateChooser.setBounds(200, 270, 300, 25);
        add(birthDateChooser);

        // Address: Province
        JLabel provLabel = new JLabel("Province:");
        provLabel.setBounds(50, 310, 140, 25);
        add(provLabel);

        provinceComboBox = new JComboBox<>();
        provinceComboBox.setBounds(200, 310, 300, 25);
        add(provinceComboBox);

        // Address: Municipality / City
        JLabel munLabel = new JLabel("Municipality/City:");
        munLabel.setBounds(50, 350, 140, 25);
        add(munLabel);

        municipalityComboBox = new JComboBox<>();
        municipalityComboBox.setBounds(200, 350, 300, 25);
        add(municipalityComboBox);

        // Address: Barangay
        JLabel brgyLabel = new JLabel("Barangay:");
        brgyLabel.setBounds(50, 390, 140, 25);
        add(brgyLabel);

        barangayComboBox = new JComboBox<>();
        barangayComboBox.setBounds(200, 390, 300, 25);
        add(barangayComboBox);

        loadAddressJsonData();
        setupDynamicAddressListeners();

        // House Number & Street Field
        JLabel streetLabel = new JLabel("House No. & Street:");
        streetLabel.setBounds(50, 430, 140, 25);
        add(streetLabel);

        streetField = new JTextField();
        streetField.setBounds(200, 430, 300, 25);
        add(streetField);

        // Contact Number
        JLabel contactLabel = new JLabel("Contact Number:");
        contactLabel.setBounds(50, 470, 140, 25);
        add(contactLabel);

        contactField = new JTextField();
        contactField.setBounds(200, 470, 300, 25);
        add(contactField);

        // Program Selection
        JLabel programLabel = new JLabel("Program:");
        programLabel.setBounds(50, 510, 140, 25);
        add(programLabel);

        programComboBox = new JComboBox<>();
        programComboBox.setBounds(200, 510, 300, 25);
        loadPrograms();
        add(programComboBox);

        // Password for Login
        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(50, 550, 140, 25);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 550, 300, 25);
        add(passwordField);

        // Register Button
        JButton registerButton = new JButton("Register Student");
        registerButton.setBounds(200, 600, 150, 35);
        registerButton.setBackground(new Color(220, 240, 220));
        add(registerButton);

        registerButton.addActionListener(e -> registerStudent());
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
                    String regKey = regionKeys.next();
                    JSONObject regionObj = addressDataRoot.getJSONObject(regKey);

                    if (regionObj.has("province_list")) {
                        JSONObject provinceList = regionObj.getJSONObject("province_list");
                        if (provinceList.has(selectedProv)) {
                            JSONObject provObj = provinceList.getJSONObject(selectedProv);

                            if (provObj.has("municipality_list")) {
                                Object munObj = provObj.get("municipality_list");

                                if (munObj instanceof JSONArray) {
                                    JSONArray munArray = (JSONArray) munObj;
                                    for (int i = 0; i < munArray.length(); i++) {
                                        Object munItemObj = munArray.get(i);
                                        if (munItemObj instanceof JSONObject) {
                                            JSONObject munItem = (JSONObject) munItemObj;
                                            if (munItem.has("municipality_name")) {
                                                munListNames.add(munItem.getString("municipality_name"));
                                            } else {
                                                Iterator<String> keys = munItem.keys();
                                                while (keys.hasNext()) {
                                                    munListNames.add(keys.next());
                                                }
                                            }
                                        }
                                    }
                                } else if (munObj instanceof JSONObject) {
                                    JSONObject munList = (JSONObject) munObj;
                                    Iterator<String> munKeys = munList.keys();
                                    while (munKeys.hasNext()) {
                                        munListNames.add(munKeys.next());
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                Collections.sort(munListNames);
                for (String mun : munListNames) {
                    municipalityComboBox.addItem(mun);
                }
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
                    String regKey = regionKeys.next();
                    JSONObject regionObj = addressDataRoot.getJSONObject(regKey);

                    if (regionObj.has("province_list")) {
                        JSONObject provinceList = regionObj.getJSONObject("province_list");
                        if (provinceList.has(selectedProv)) {
                            JSONObject provObj = provinceList.getJSONObject(selectedProv);

                            if (provObj.has("municipality_list")) {
                                Object munObj = provObj.get("municipality_list");

                                if (munObj instanceof JSONArray) {
                                    JSONArray munArray = (JSONArray) munObj;
                                    for (int i = 0; i < munArray.length(); i++) {
                                        Object munItemObj = munArray.get(i);
                                        if (munItemObj instanceof JSONObject) {
                                            JSONObject munItem = (JSONObject) munItemObj;

                                            if (munItem.has(selectedMun)) {
                                                JSONObject munDetails = munItem.getJSONObject(selectedMun);
                                                if (munDetails.has("barangay_list")) {
                                                    brgyObj = munDetails.get("barangay_list");
                                                    break;
                                                }
                                            } else if (munItem.has("municipality_name")
                                                    && munItem.getString("municipality_name").equals(selectedMun)) {
                                                if (munItem.has("barangay_list")) {
                                                    brgyObj = munItem.get("barangay_list");
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else if (munObj instanceof JSONObject) {
                                    JSONObject munList = (JSONObject) munObj;
                                    if (munList.has(selectedMun)) {
                                        Object innerMun = munList.get(selectedMun);
                                        if (innerMun instanceof JSONObject) {
                                            JSONObject munObjDetails = (JSONObject) innerMun;
                                            if (munObjDetails.has("barangay_list")) {
                                                brgyObj = munObjDetails.get("barangay_list");
                                            }
                                        } else if (innerMun instanceof JSONArray) {
                                            brgyObj = innerMun;
                                        }
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
                        if (brgyItem instanceof String) {
                            brgyListNames.add((String) brgyItem);
                        } else if (brgyItem instanceof JSONObject) {
                            JSONObject bObj = (JSONObject) brgyItem;
                            if (bObj.has("barangay_name")) {
                                brgyListNames.add(bObj.getString("barangay_name"));
                            } else {
                                Iterator<String> keys = bObj.keys();
                                if (keys.hasNext()) {
                                    brgyListNames.add(keys.next());
                                }
                            }
                        }
                    }
                } else if (brgyObj instanceof JSONObject) {
                    JSONObject brgyDict = (JSONObject) brgyObj;
                    Iterator<String> brgyKeys = brgyDict.keys();
                    while (brgyKeys.hasNext()) {
                        brgyListNames.add(brgyKeys.next());
                    }
                }

                Collections.sort(brgyListNames);
                for (String brgy : brgyListNames) {
                    barangayComboBox.addItem(brgy);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void generateStudentId() {
        String currentYear = String.valueOf(Year.now().getValue());
        String query = "SELECT COUNT(*) AS total FROM student";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            int nextCount = 1;
            if (rs.next()) {
                nextCount = rs.getInt("total") + 1;
            }
            idTextField.setText(currentYear + String.format("%06d", nextCount));

        } catch (SQLException ex) {
            idTextField.setText(currentYear + "000001");
        }
    }

    private void loadPrograms() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT program_id, program_code FROM program")) {

            while (rs.next()) {
                int id = rs.getInt("program_id");
                String code = rs.getString("program_code");
                programComboBox.addItem(new StudentRegistration.ProgramItem(id, code));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerStudent() {
        String studentId = idTextField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String sex = (String) sexComboBox.getSelectedItem();
        java.util.Date selectedDate = birthDateChooser.getDate();

        String province = (String) provinceComboBox.getSelectedItem();
        String municipality = (String) municipalityComboBox.getSelectedItem();
        String barangay = (String) barangayComboBox.getSelectedItem();
        String street = streetField.getText().trim();

        String contactNumber = contactField.getText().trim();
        StudentRegistration.ProgramItem selectedProgram = (StudentRegistration.ProgramItem) programComboBox
                .getSelectedItem();
        String password = new String(passwordField.getPassword()).trim();

        if (firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || selectedDate == null
                || selectedProgram == null || province == null || province.startsWith("--")
                || municipality == null || municipality.startsWith("--")
                || barangay == null || barangay.startsWith("--")) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields, including complete address details.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.sql.Date sqlBirthDate = new java.sql.Date(selectedDate.getTime());
        String studentQuery = "INSERT INTO student (student_id, first_name, middle_name, last_name, gender, birth_date, street, province, municipality, barangay, contact_number, program_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String loginQuery = "INSERT INTO student_logins (student_id, username, password) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstStudent = conn.prepareStatement(studentQuery);
                    PreparedStatement pstLogin = conn.prepareStatement(loginQuery)) {

                pstStudent.setString(1, studentId);
                pstStudent.setString(2, firstName);
                pstStudent.setString(3, middleName.isEmpty() ? null : middleName);
                pstStudent.setString(4, lastName);
                pstStudent.setString(5, sex);
                pstStudent.setDate(6, sqlBirthDate);
                pstStudent.setString(7, street.isEmpty() ? null : street);
                pstStudent.setString(8, province);
                pstStudent.setString(9, municipality);
                pstStudent.setString(10, barangay);
                pstStudent.setString(11, contactNumber.isEmpty() ? null : contactNumber);
                pstStudent.setInt(12, selectedProgram.id);
                pstStudent.executeUpdate();

                pstLogin.setString(1, studentId);
                pstLogin.setString(2, studentId);
                pstLogin.setString(3, password);
                pstLogin.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Student Registered Successfully!\nID: " + studentId, "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close modal window on success

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration Failed: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}