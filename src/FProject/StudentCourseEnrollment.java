package FProject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentCourseEnrollment extends JPanel {
    private String studentId;
    private int programId, yearLevel;
    private JComboBox<SchoolYearItem> cmbSchoolYear;
    private JTable courseTable;
    private DefaultTableModel courseTableModel;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public StudentCourseEnrollment(String studentId) {
        this.studentId = studentId;
        fetchStudentDetails();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // Main Card Container
        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(new Color(255, 255, 255, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(22, 25, 20, 25)));
        cardPanel.setPreferredSize(new Dimension(840, 560));

        // Header Section
        JLabel titleLabel = new JLabel("Curriculum Course Enlistment", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(15, 23, 42));

        JLabel subtitleLabel = new JLabel(
                "Select your target academic term and load available curriculum courses for registration.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(titleLabel);
        topContainer.add(Box.createVerticalStrut(2));
        topContainer.add(subtitleLabel);
        topContainer.add(Box.createVerticalStrut(12));

        // Controls Bar
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.setOpaque(false);

        JLabel lblTerm = new JLabel("Select Term:");
        lblTerm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTerm.setForeground(new Color(30, 41, 59));

        cmbSchoolYear = new JComboBox<>();
        cmbSchoolYear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbSchoolYear.setPreferredSize(new Dimension(200, 28));
        loadSchoolYears();

        JButton btnLoadCourses = new JButton("Load & Enroll Curriculum Courses");
        btnLoadCourses.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLoadCourses.setBackground(new Color(37, 99, 235));
        btnLoadCourses.setForeground(Color.WHITE);
        btnLoadCourses.setFocusPainted(false);
        btnLoadCourses.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoadCourses.setPreferredSize(new Dimension(240, 32));

        controlsPanel.add(lblTerm);
        controlsPanel.add(cmbSchoolYear);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(btnLoadCourses);

        topContainer.add(controlsPanel);
        cardPanel.add(topContainer, BorderLayout.NORTH);

        // Table Setup
        String[] columns = { "Course ID", "Course Code", "Course Name", "Units", "Assigned Faculty" };
        courseTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        courseTable = new JTable(courseTableModel);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        courseTable.setRowHeight(24);
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseTable.getTableHeader().setBackground(new Color(241, 245, 249));
        courseTable.getTableHeader().setForeground(new Color(15, 23, 42));

        // Center align specific columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        courseTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Course ID
        courseTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Course Code
        courseTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Units

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        cardPanel.add(scrollPane, BorderLayout.CENTER);

        btnLoadCourses.addActionListener(e -> loadAndEnrollAllCourses());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(cardPanel, gbc);
    }

    private void fetchStudentDetails() {
        String query = "SELECT program_id, year_level FROM student WHERE student_id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, studentId.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.programId = rs.getInt("program_id");
                this.yearLevel = rs.getInt("year_level");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSchoolYears() {
        String query = "SELECT schoolyear_id, academic_year, semester FROM schoolyear";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("schoolyear_id");
                String sy = rs.getString("academic_year");
                String sem = rs.getString("semester");
                cmbSchoolYear.addItem(new SchoolYearItem(id, sy + " - " + sem));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAndEnrollAllCourses() {
        SchoolYearItem selectedSy = (SchoolYearItem) cmbSchoolYear.getSelectedItem();
        if (selectedSy == null)
            return;

        courseTableModel.setRowCount(0);

        String fetchCoursesQuery = "SELECT c.course_id, c.course_code, c.course_name, c.units, fa.assignment_id, f.first_name, f.last_name "
                + "FROM course c "
                + "LEFT JOIN faculty_assignment fa ON c.course_id = fa.course_id AND fa.schoolyear_id = ? "
                + "LEFT JOIN faculty f ON fa.faculty_id = f.faculty_id "
                + "WHERE c.program_id = ? AND c.year_level = ?";

        String insertEnrollmentQuery = "IF NOT EXISTS (SELECT 1 FROM enrollment WHERE student_id = ? AND course_id = ? AND schoolyear_id = ?) "
                + "INSERT INTO enrollment (student_id, course_id, schoolyear_id, assignment_id, enrollment_date) VALUES (?, ?, ?, ?, GETDATE())";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchCoursesQuery);
                    PreparedStatement pstmtInsert = conn.prepareStatement(insertEnrollmentQuery)) {

                pstmtFetch.setInt(1, selectedSy.id);
                pstmtFetch.setInt(2, programId);
                pstmtFetch.setInt(3, yearLevel);
                ResultSet rs = pstmtFetch.executeQuery();

                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    int units = rs.getInt("units");
                    int assignmentId = rs.getInt("assignment_id");

                    String facultyName = (rs.getString("last_name") != null)
                            ? rs.getString("first_name") + " " + rs.getString("last_name")
                            : "TBA";

                    pstmtInsert.setString(1, studentId.trim());
                    pstmtInsert.setInt(2, courseId);
                    pstmtInsert.setInt(3, selectedSy.id);
                    pstmtInsert.setString(4, studentId.trim());
                    pstmtInsert.setInt(5, courseId);
                    pstmtInsert.setInt(6, selectedSy.id);
                    if (assignmentId > 0)
                        pstmtInsert.setInt(7, assignmentId);
                    else
                        pstmtInsert.setNull(7, java.sql.Types.INTEGER);

                    pstmtInsert.executeUpdate();
                    courseTableModel.addRow(new Object[] { courseId, code, name, units, facultyName });
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Courses successfully loaded and enrolled for the term!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing enrollments: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class SchoolYearItem {
        int id;
        String label;

        public SchoolYearItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}