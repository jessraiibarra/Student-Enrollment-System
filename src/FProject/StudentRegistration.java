package FProject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentRegistration extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<ProgramItem> programFilterComboBox;
    private JFrame parentFrame;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public static class ProgramItem {
        int id;
        String name;

        public ProgramItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public StudentRegistration(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setOpaque(false); // Transparent para lumitaw ang background ng dashboard

        // --- TOP PANEL: Search Bar & Program Filter ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(searchLabel);

        searchField = new JTextField(15);
        topPanel.add(searchField);

        JLabel filterLabel = new JLabel("Filter Program:");
        filterLabel.setForeground(Color.WHITE);
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topPanel.add(filterLabel);

        programFilterComboBox = new JComboBox<>();
        programFilterComboBox.addItem(new ProgramItem(0, "All Programs"));
        loadProgramsToDropdown();
        topPanel.add(programFilterComboBox);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Student JTable ---
        String[] columns = { "Student ID", "First Name", "Middle Name", "Last Name", "Gender", "Birth Date", "Province",
                "Municipality", "Barangay", "Street", "Contact", "Program" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // --- SOUTH PANEL: Action Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100)),
                "Student Actions",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 12),
                Color.WHITE));

        JButton openAddModalButton = new JButton("Add New Student");
        openAddModalButton.setBackground(new Color(220, 240, 220));

        JButton openEditModalButton = new JButton("Edit Selected Student");
        openEditModalButton.setBackground(new Color(220, 230, 240));

        JButton deleteButton = new JButton("Delete Selected Student");
        deleteButton.setForeground(Color.RED);

        JButton refreshButton = new JButton("Refresh Table");

        bottomPanel.add(openAddModalButton);
        bottomPanel.add(openEditModalButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(refreshButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadStudentData("", 0);

        // --- LISTENERS ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterStudents();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterStudents();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterStudents();
            }
        });

        programFilterComboBox.addActionListener(e -> filterStudents());

        openAddModalButton.addActionListener(e -> {
            AddStudentDialog addDialog = new AddStudentDialog(parentFrame);
            addDialog.setVisible(true);
            filterStudents();
        });

        openEditModalButton.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a student from the table to edit.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String studentId = studentTable.getValueAt(selectedRow, 0).toString();
            EditStudentDialog editDialog = new EditStudentDialog(parentFrame, studentId);
            editDialog.setVisible(true);
            filterStudents();
        });

        deleteButton.addActionListener(e -> deleteStudent());
        refreshButton.addActionListener(e -> filterStudents());
    }

    private void loadProgramsToDropdown() {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT program_id, program_code FROM program")) {

            while (rs.next()) {
                int id = rs.getInt("program_id");
                String code = rs.getString("program_code");
                programFilterComboBox.addItem(new ProgramItem(id, code));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterStudents() {
        String keyword = searchField.getText().trim();
        ProgramItem selectedProgram = (ProgramItem) programFilterComboBox.getSelectedItem();
        int programId = (selectedProgram != null) ? selectedProgram.id : 0;
        loadStudentData(keyword, programId);
    }

    public void loadStudentData(String searchKeyword, int programIdFilter) {
        tableModel.setRowCount(0);
        StringBuilder query = new StringBuilder(
                "SELECT s.student_id, s.first_name, s.middle_name, s.last_name, s.gender, s.birth_date, " +
                        "s.province, s.municipality, s.barangay, s.street, s.contact_number, p.program_code " +
                        "FROM student s LEFT JOIN program p ON s.program_id = p.program_id WHERE 1=1");

        boolean hasProgFilter = programIdFilter > 0;
        boolean hasKeyword = !searchKeyword.isEmpty();

        if (hasProgFilter) {
            query.append(" AND s.program_id = ?");
        }
        if (hasKeyword) {
            query.append(" AND (s.student_id LIKE ? OR s.first_name LIKE ? OR s.last_name LIKE ?)");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (hasProgFilter) {
                pst.setInt(paramIndex++, programIdFilter);
            }
            if (hasKeyword) {
                String wildcard = "%" + searchKeyword + "%";
                pst.setString(paramIndex++, wildcard);
                pst.setString(paramIndex++, wildcard);
                pst.setString(paramIndex++, wildcard);
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                            rs.getString("student_id"),
                            rs.getString("first_name"),
                            rs.getString("middle_name"),
                            rs.getString("last_name"),
                            rs.getString("gender"),
                            rs.getDate("birth_date"),
                            rs.getString("province"),
                            rs.getString("municipality"),
                            rs.getString("barangay"),
                            rs.getString("street"),
                            rs.getString("contact_number"),
                            rs.getString("program_code")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading student data: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentId = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student ID: " + studentId + "?", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String loginDelQuery = "DELETE FROM student_logins WHERE student_id = ?";
            String studentDelQuery = "DELETE FROM student WHERE student_id = ?";

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstLog = conn.prepareStatement(loginDelQuery);
                        PreparedStatement pstStud = conn.prepareStatement(studentDelQuery)) {

                    pstLog.setString(1, studentId);
                    pstLog.executeUpdate();

                    pstStud.setString(1, studentId);
                    pstStud.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Student deleted successfully.");
                    filterStudents();

                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}