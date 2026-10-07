package FProject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentEnrollmentStatus extends JPanel {
    private String studentId;
    private JTable table;
    private DefaultTableModel model;

    // COR Info Labels
    private JLabel lblStudentName, lblStudentIdNum, lblProgramYear, lblTerm;
    private JLabel lblTotalUnits;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public StudentEnrollmentStatus(String studentId) {
        this.studentId = studentId;
        setLayout(new GridBagLayout());
        setOpaque(false);

        // Main Card Container
        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(new Color(255, 255, 255, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(22, 25, 20, 25)));
        cardPanel.setPreferredSize(new Dimension(840, 560));

        // Header Section (COR Branding)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel instLabel = new JLabel("INTERNATIONAL STATE COLLEGE OF THE PHILIPPINES");
        instLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        instLabel.setForeground(new Color(100, 116, 139));
        instLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("CERTIFICATE OF ENROLLMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(instLabel);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(12));

        // Student Info Panel (Typical COR metadata block)
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 15, 6));
        infoPanel.setOpaque(true);
        infoPanel.setBackground(new Color(248, 250, 252));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        lblStudentName = new JLabel("Student Name: Loading...");
        lblStudentIdNum = new JLabel("Student ID: Loading...");
        lblProgramYear = new JLabel("Program & Year: Loading...");
        lblTerm = new JLabel("Academic Term: Loading...");

        Font infoFont = new Font("Segoe UI", Font.BOLD, 12);
        Color infoColor = new Color(30, 41, 59);
        for (JLabel lbl : new JLabel[] { lblStudentName, lblStudentIdNum, lblProgramYear, lblTerm }) {
            lbl.setFont(infoFont);
            lbl.setForeground(infoColor);
            infoPanel.add(lbl);
        }

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(infoPanel, BorderLayout.CENTER);
        cardPanel.add(topContainer, BorderLayout.NORTH);

        // Table Setup
        String[] columns = { "Course Code", "Course Name", "Units", "Assigned Faculty", "School Year / Semester",
                "Enrollment Date" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(15, 23, 42));

        // Center align specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Units
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // SY/Sem
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Date

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer Section (Total Units & Refresh Button)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        lblTotalUnits = new JLabel("Total Enrolled Units: 0");
        lblTotalUnits.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalUnits.setForeground(new Color(30, 41, 59));

        JButton btnRefresh = new JButton("Refresh COR");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(37, 99, 235));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(130, 32));
        btnRefresh.addActionListener(e -> loadEnrollmentData());

        bottomPanel.add(lblTotalUnits, BorderLayout.WEST);
        bottomPanel.add(btnRefresh, BorderLayout.EAST);
        cardPanel.add(bottomPanel, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(cardPanel, gbc);

        loadEnrollmentData();
    }

    private void loadEnrollmentData() {
        model.setRowCount(0);
        if (studentId == null || studentId.trim().isEmpty())
            return;

        // Fetch Student Metadata first
        String studentQuery = "SELECT s.first_name, s.middle_name, s.last_name, s.year_level, p.program_name " +
                "FROM student s LEFT JOIN program p ON s.program_id = p.program_id WHERE s.student_id = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pstStu = conn.prepareStatement(studentQuery)) {
            pstStu.setString(1, studentId.trim());
            try (ResultSet rsStu = pstStu.executeQuery()) {
                if (rsStu.next()) {
                    String fName = rsStu.getString("first_name");
                    String mName = rsStu.getString("middle_name");
                    String lName = rsStu.getString("last_name");
                    String fullName = (fName != null ? fName : "") +
                            (mName != null && !mName.trim().isEmpty() ? " " + mName + " " : " ") +
                            (lName != null ? lName : "");
                    String progName = rsStu.getString("program_name");
                    int yrLvl = rsStu.getInt("year_level");

                    lblStudentName.setText("Student Name: " + fullName.trim());
                    lblStudentIdNum.setText("Student ID: " + studentId);
                    lblProgramYear
                            .setText("Program & Year: " + (progName != null ? progName : "N/A") + " - Year " + yrLvl);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Fetch Enrolled Courses
        String query = "SELECT c.course_code, c.course_name, c.units, " +
                "COALESCE(f.first_name + ' ' + f.last_name, 'TBA') AS faculty_name, " +
                "sy.academic_year, sy.semester, e.enrollment_date " +
                "FROM enrollment e " +
                "JOIN course c ON e.course_id = c.course_id " +
                "LEFT JOIN schoolyear sy ON e.schoolyear_id = sy.schoolyear_id " +
                "LEFT JOIN faculty_assignment fa ON e.assignment_id = fa.assignment_id " +
                "LEFT JOIN faculty f ON fa.faculty_id = f.faculty_id " +
                "WHERE e.student_id = ?";

        int totalUnits = 0;
        String latestTerm = "N/A";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, studentId.trim());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int units = rs.getInt("units");
                    totalUnits += units;
                    String sy = rs.getString("academic_year");
                    String sem = rs.getString("semester");
                    String sySem = (sy != null) ? sy + " - " + sem : "N/A";
                    if (!sySem.equals("N/A"))
                        latestTerm = sySem;

                    model.addRow(new Object[] {
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            units,
                            rs.getString("faculty_name"),
                            sySem,
                            rs.getDate("enrollment_date")
                    });
                }
            }
            lblTerm.setText("Academic Term: " + latestTerm);
            lblTotalUnits.setText("Total Enrolled Units: " + totalUnits);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading COR status: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}