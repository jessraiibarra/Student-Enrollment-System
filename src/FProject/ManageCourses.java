package FProject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManageCourses extends JPanel {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField, courseCodeField, courseTitleField, unitsField;
    private JComboBox<ProgramItem> programComboBox;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public static class ProgramItem {
        public int id;
        public String name;

        public ProgramItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public ManageCourses() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(255, 255, 255, 220));
        topPanel.setOpaque(true);

        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(10);
        topPanel.add(searchField);

        topPanel.add(new JLabel("Program:"));
        programComboBox = new JComboBox<>();
        loadPrograms();
        topPanel.add(programComboBox);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = { "Course ID", "Course Code", "Course Title", "Units", "Program ID" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.getViewport().setBackground(new Color(255, 255, 255, 200));
        add(scrollPane, BorderLayout.CENTER);

        JPanel crudPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        crudPanel.setBorder(BorderFactory.createTitledBorder("Course Actions"));
        crudPanel.setBackground(new Color(255, 255, 255, 220));
        crudPanel.setOpaque(true);

        crudPanel.add(new JLabel("Code:"));
        courseCodeField = new JTextField(6);
        crudPanel.add(courseCodeField);

        crudPanel.add(new JLabel("Title:"));
        courseTitleField = new JTextField(12);
        crudPanel.add(courseTitleField);

        crudPanel.add(new JLabel("Units:"));
        unitsField = new JTextField(3);
        crudPanel.add(unitsField);

        JButton addButton = new JButton("Add Course");
        addButton.setBackground(new Color(220, 240, 220));
        crudPanel.add(addButton);

        JButton updateButton = new JButton("Update Course");
        crudPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete Course");
        deleteButton.setForeground(Color.RED);
        crudPanel.add(deleteButton);

        add(crudPanel, BorderLayout.SOUTH);

        loadCourseData(0, "");

        programComboBox.addActionListener(e -> filterCourses());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterCourses();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterCourses();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterCourses();
            }
        });

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow != -1) {
                courseCodeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                courseTitleField.setText(tableModel.getValueAt(selectedRow, 2).toString());
                unitsField.setText(tableModel.getValueAt(selectedRow, 3).toString());
            }
        });

        addButton.addActionListener(e -> addCourse());
        updateButton.addActionListener(e -> updateCourse());
        deleteButton.addActionListener(e -> deleteCourse());
    }

    public void loadPrograms() {
        programComboBox.removeAllItems();
        programComboBox.addItem(new ProgramItem(0, "-- All Programs --"));

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT program_id, program_code FROM program")) {

            while (rs.next()) {
                programComboBox.addItem(new ProgramItem(rs.getInt("program_id"), rs.getString("program_code")));
            }
        } catch (SQLException ignored) {
        }
    }

    private void filterCourses() {
        ProgramItem selected = (ProgramItem) programComboBox.getSelectedItem();
        loadCourseData((selected != null) ? selected.id : 0, searchField.getText().trim());
    }

    public void loadCourseData(int programId, String searchKeyword) {
        tableModel.setRowCount(0);
        StringBuilder query = new StringBuilder(
                "SELECT course_id, course_code, course_name, units, program_id FROM course WHERE 1=1");

        if (programId > 0)
            query.append(" AND program_id = ").append(programId);
        if (!searchKeyword.isEmpty())
            query.append(" AND (course_code LIKE ? OR course_name LIKE ?)");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query.toString())) {

            if (!searchKeyword.isEmpty()) {
                pst.setString(1, "%" + searchKeyword + "%");
                pst.setString(2, "%" + searchKeyword + "%");
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getInt("course_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("units"),
                            rs.getInt("program_id")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCourse() {
        String code = courseCodeField.getText().trim();
        String title = courseTitleField.getText().trim();
        String unitsStr = unitsField.getText().trim();
        ProgramItem selectedProgram = (ProgramItem) programComboBox.getSelectedItem();

        if (code.isEmpty() || title.isEmpty() || unitsStr.isEmpty() || selectedProgram == null
                || selectedProgram.id == 0) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields and select a specific program.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int units = Integer.parseInt(unitsStr);
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    PreparedStatement pst = conn.prepareStatement(
                            "INSERT INTO course (course_code, course_name, units, program_id) VALUES (?, ?, ?, ?)")) {

                pst.setString(1, code);
                pst.setString(2, title);
                pst.setInt(3, units);
                pst.setInt(4, selectedProgram.id);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Course added successfully!");
                clearForm();
                filterCourses();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Units must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to update.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        String code = courseCodeField.getText().trim();
        String title = courseTitleField.getText().trim();
        String unitsStr = unitsField.getText().trim();

        try {
            int units = Integer.parseInt(unitsStr);
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    PreparedStatement pst = conn.prepareStatement(
                            "UPDATE course SET course_code = ?, course_name = ?, units = ? WHERE course_id = ?")) {

                pst.setString(1, code);
                pst.setString(2, title);
                pst.setInt(3, units);
                pst.setInt(4, courseId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Course updated successfully!");
                clearForm();
                filterCourses();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Units must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?", "Confirm",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM course WHERE course_id = ?")) {

                pst.setInt(1, courseId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Course deleted successfully.");
                clearForm();
                filterCourses();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        courseCodeField.setText("");
        courseTitleField.setText("");
        unitsField.setText("");
        courseTable.clearSelection();
    }
}