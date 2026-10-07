package FProject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FacultyClasses extends JPanel {
    private String facultyId;
    private JTable table;
    private DefaultTableModel model;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public FacultyClasses(String facultyId) {
        this.facultyId = (facultyId != null) ? facultyId.trim() : "";

        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)));
        card.setPreferredSize(new Dimension(840, 520));

        JLabel titleLabel = new JLabel("Assigned Teaching Load", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(15, 23, 42));
        card.add(titleLabel, BorderLayout.NORTH);

        String[] columns = { "Course Code", "Course Name", "Units", "School Year / Semester" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(51, 65, 85));
        table.setGridColor(new Color(226, 232, 240));

        // Center align units column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        card.add(scrollPane, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Schedule");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(new Color(37, 99, 235));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnRefresh);
        card.add(bottomPanel, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(card, gbc);

        loadAssignedClasses();
        btnRefresh.addActionListener(e -> loadAssignedClasses());
    }

    private void loadAssignedClasses() {
        model.setRowCount(0);
        String query = "SELECT c.course_code, c.course_name, c.units, sy.academic_year, sy.semester " +
                "FROM faculty_assignment fa " +
                "JOIN course c ON fa.course_id = c.course_id " +
                "JOIN schoolyear sy ON fa.schoolyear_id = sy.schoolyear_id " +
                "JOIN faculty f ON fa.faculty_id = f.faculty_id " +
                "WHERE f.faculty_id = ? OR f.email = ? OR f.email LIKE ? + '@%'";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, facultyId);
            pst.setString(2, facultyId);
            pst.setString(3, facultyId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String sySem = rs.getString("academic_year") + " - " + rs.getString("semester");
                    model.addRow(new Object[] {
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("units"),
                            sySem
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading classes: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}