package FProject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageFaculty extends JPanel {
    private JTable facultyTable;
    private DefaultTableModel tableModel;
    private JTextField searchField, idField, firstNameField, lastNameField, departmentField, emailField;
    private JComboBox<String> departmentDropdown;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public ManageFaculty() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(255, 255, 255, 220));
        topPanel.setOpaque(true);

        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(12);
        topPanel.add(searchField);

        topPanel.add(new JLabel("Filter Department:"));
        departmentDropdown = new JComboBox<>();
        departmentDropdown.addItem("All Departments");
        loadDepartmentDropdown();
        topPanel.add(departmentDropdown);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = { "Faculty ID", "First Name", "Last Name", "Department", "Email" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        facultyTable = new JTable(tableModel);
        facultyTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(facultyTable);
        scrollPane.getViewport().setBackground(new Color(255, 255, 255, 200));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Faculty Actions"));
        bottomPanel.setBackground(new Color(255, 255, 255, 220));
        bottomPanel.setOpaque(true);

        bottomPanel.add(new JLabel("ID:"));
        idField = new JTextField(5);
        bottomPanel.add(idField);

        bottomPanel.add(new JLabel("First Name:"));
        firstNameField = new JTextField(8);
        bottomPanel.add(firstNameField);

        bottomPanel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField(8);
        bottomPanel.add(lastNameField);

        bottomPanel.add(new JLabel("Dept:"));
        departmentField = new JTextField(8);
        bottomPanel.add(departmentField);

        bottomPanel.add(new JLabel("Email:"));
        emailField = new JTextField(10);
        bottomPanel.add(emailField);

        JButton addButton = new JButton("Add");
        addButton.setBackground(new Color(220, 240, 220));
        bottomPanel.add(addButton);

        JButton updateButton = new JButton("Update");
        bottomPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setForeground(Color.RED);
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadFacultyData("", "All Departments");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterFaculty();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterFaculty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterFaculty();
            }
        });

        departmentDropdown.addActionListener(e -> filterFaculty());

        facultyTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = facultyTable.getSelectedRow();
            if (selectedRow != -1) {
                idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
                idField.setEditable(false);
                firstNameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                lastNameField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                departmentField.setText(
                        tableModel.getValueAt(selectedRow, 3) != null ? tableModel.getValueAt(selectedRow, 3).toString()
                                : "");
                emailField.setText(
                        tableModel.getValueAt(selectedRow, 4) != null ? tableModel.getValueAt(selectedRow, 4).toString()
                                : "");
            }
        });

        addButton.addActionListener(e -> addFaculty());
        updateButton.addActionListener(e -> updateFaculty());
        deleteButton.addActionListener(e -> deleteFaculty());
    }

    public void loadDepartmentDropdown() {
        String query = "SELECT DISTINCT department FROM faculty WHERE department IS NOT NULL ORDER BY department";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                departmentDropdown.addItem(rs.getString("department"));
            }
        } catch (SQLException ignored) {
        }
    }

    private void filterFaculty() {
        loadFacultyData(searchField.getText().trim(), (String) departmentDropdown.getSelectedItem());
    }

    public void loadFacultyData(String searchKeyword, String departmentFilter) {
        tableModel.setRowCount(0);
        StringBuilder query = new StringBuilder(
                "SELECT faculty_id, first_name, last_name, department, email FROM faculty WHERE 1=1");

        boolean hasDeptFilter = departmentFilter != null && !departmentFilter.equals("All Departments");
        boolean hasKeyword = !searchKeyword.isEmpty();

        if (hasDeptFilter)
            query.append(" AND department = ?");
        if (hasKeyword)
            query.append(" AND (faculty_id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR email LIKE ?)");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (hasDeptFilter)
                pst.setString(paramIndex++, departmentFilter);
            if (hasKeyword) {
                String wildcard = "%" + searchKeyword + "%";
                pst.setString(paramIndex++, wildcard);
                pst.setString(paramIndex++, wildcard);
                pst.setString(paramIndex++, wildcard);
                pst.setString(paramIndex++, wildcard);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getString("faculty_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("department"),
                            rs.getString("email")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading faculty: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFaculty() {
        String id = idField.getText().trim();
        String fname = firstNameField.getText().trim();
        String lname = lastNameField.getText().trim();
        String dept = departmentField.getText().trim();
        String email = emailField.getText().trim();

        if (id.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in ID, First Name, and Last Name.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO faculty (faculty_id, first_name, last_name, department, email) VALUES (?, ?, ?, ?, ?)")) {

            pst.setString(1, id);
            pst.setString(2, fname);
            pst.setString(3, lname);
            pst.setString(4, dept.isEmpty() ? null : dept);
            pst.setString(5, email.isEmpty() ? null : email);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Faculty added successfully!");
            refreshDropdownAndTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFaculty() {
        String id = idField.getText().trim();
        String fname = firstNameField.getText().trim();
        String lname = lastNameField.getText().trim();
        String dept = departmentField.getText().trim();
        String email = emailField.getText().trim();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(
                        "UPDATE faculty SET first_name = ?, last_name = ?, department = ?, email = ? WHERE faculty_id = ?")) {

            pst.setString(1, fname);
            pst.setString(2, lname);
            pst.setString(3, dept.isEmpty() ? null : dept);
            pst.setString(4, email.isEmpty() ? null : email);
            pst.setString(5, id);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Faculty updated successfully!");
            refreshDropdownAndTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFaculty() {
        int selectedRow = facultyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String facultyId = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete faculty ID: " + facultyId + "?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM faculty WHERE faculty_id = ?")) {

                pst.setString(1, facultyId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Faculty deleted successfully.");
                refreshDropdownAndTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshDropdownAndTable() {
        String currentSelection = (String) departmentDropdown.getSelectedItem();
        departmentDropdown.removeAllItems();
        departmentDropdown.addItem("All Departments");
        loadDepartmentDropdown();
        departmentDropdown.setSelectedItem(currentSelection);
        clearForm();
        filterFaculty();
    }

    private void clearForm() {
        idField.setText("");
        idField.setEditable(true);
        firstNameField.setText("");
        lastNameField.setText("");
        departmentField.setText("");
        emailField.setText("");
        searchField.setText("");
        facultyTable.clearSelection();
    }
}