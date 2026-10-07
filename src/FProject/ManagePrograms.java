package FProject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManagePrograms extends JPanel {
    private JTable programTable;
    private DefaultTableModel tableModel;
    private JTextField searchField, codeField, nameField;

    private final String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=Student Enrollment System;encrypt=false;trustServerCertificate=true;";
    private final String dbUser = "sa";
    private final String dbPass = "Jessrai08140410!";

    public ManagePrograms() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(255, 255, 255, 220));
        topPanel.setOpaque(true);
        topPanel.add(new JLabel("Search Program:"));
        searchField = new JTextField(15);
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = { "Program ID", "Program Code", "Program Name" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        programTable = new JTable(tableModel);
        programTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(programTable);
        scrollPane.getViewport().setBackground(new Color(255, 255, 255, 200));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Program Actions"));
        bottomPanel.setBackground(new Color(255, 255, 255, 220));
        bottomPanel.setOpaque(true);

        bottomPanel.add(new JLabel("Program Code:"));
        codeField = new JTextField(8);
        bottomPanel.add(codeField);

        bottomPanel.add(new JLabel("Program Name:"));
        nameField = new JTextField(15);
        bottomPanel.add(nameField);

        JButton addButton = new JButton("Add Program");
        addButton.setBackground(new Color(220, 240, 220));
        bottomPanel.add(addButton);

        JButton updateButton = new JButton("Update");
        bottomPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setForeground(Color.RED);
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);

        loadProgramData("");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterPrograms();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterPrograms();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterPrograms();
            }
        });

        programTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = programTable.getSelectedRow();
            if (selectedRow != -1) {
                codeField.setText(tableModel.getValueAt(selectedRow, 1).toString());
                nameField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            }
        });

        addButton.addActionListener(e -> addProgram());
        updateButton.addActionListener(e -> updateProgram());
        deleteButton.addActionListener(e -> deleteProgram());
    }

    private void filterPrograms() {
        loadProgramData(searchField.getText().trim());
    }

    public void loadProgramData(String searchKeyword) {
        tableModel.setRowCount(0);
        String query = "SELECT program_id, program_code, program_name FROM program";
        if (!searchKeyword.isEmpty()) {
            query += " WHERE program_code LIKE ? OR program_name LIKE ?";
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(query)) {

            if (!searchKeyword.isEmpty()) {
                pst.setString(1, "%" + searchKeyword + "%");
                pst.setString(2, "%" + searchKeyword + "%");
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[] {
                            rs.getInt("program_id"),
                            rs.getString("program_code"),
                            rs.getString("program_name")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading programs: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProgram() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn
                        .prepareStatement("INSERT INTO program (program_code, program_name) VALUES (?, ?)")) {

            pst.setString(1, code);
            pst.setString(2, name);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Program added successfully!");
            clearForm();
            loadProgramData("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProgram() {
        int selectedRow = programTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a program to update.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int programId = (int) tableModel.getValueAt(selectedRow, 0);
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                PreparedStatement pst = conn.prepareStatement(
                        "UPDATE program SET program_code = ?, program_name = ? WHERE program_id = ?")) {

            pst.setString(1, code);
            pst.setString(2, name);
            pst.setInt(3, programId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Program updated successfully!");
            clearForm();
            loadProgramData("");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Failed: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProgram() {
        int selectedRow = programTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a program to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int programId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure? Deleting a program may affect associated courses.", "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                    PreparedStatement pst = conn.prepareStatement("DELETE FROM program WHERE program_id = ?")) {

                pst.setInt(1, programId);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Program deleted successfully.");
                clearForm();
                loadProgramData("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        codeField.setText("");
        nameField.setText("");
        searchField.setText("");
        programTable.clearSelection();
    }
}