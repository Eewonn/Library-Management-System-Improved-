package com.library.management.gui;

import com.library.management.database.databaseConnection;
import com.library.management.classes.Member;
import com.library.management.classes.User;

import javax.swing.*;
//import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembersPage extends LibraryDashboard {
    // Static Attributes
    private static final Color THEME_COLOR = new Color(60, 106, 117);
    @SuppressWarnings("unused")
    private static final Color DARKER_THEME_COLOR = Color.BLACK;
    private static final Color TABLE_TEXT_COLOR = Color.WHITE;
    private static final Color TABLE_HEADER_COLOR = new Color(60, 106, 117);
    private static final Color TABLE_BACKGROUND_COLOR = new Color(60, 106, 117);

    // Attributes
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private List<Member> memberList;
    private List<JTextField> inputFields;

    // Constructor
    public MembersPage(User user) {
        super(user, null);
        memberList = new ArrayList<>();
        setTitle("Members Management");
        setupUI();
        loadMembersFromDatabase(); // Load members from the database
        setVisible(true);
    }

    // GUI SET UP METHODS
    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Member ID", "Member Name", "Borrowed Books"};
        tableModel = new DefaultTableModel(columnNames, 0);
        membersTable = createMembersTable();

        // Create a JScrollPane for the table
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setPreferredSize(new Dimension(0, 0));

        // Create a panel to add padding around the JScrollPane
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 10, 50));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons to add, remove, and update members
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Create members table
    private JTable createMembersTable() {
        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(THEME_COLOR);
                } else {
                    c.setBackground(TABLE_BACKGROUND_COLOR);
                }
                c.setForeground(TABLE_TEXT_COLOR);
                return c;
            }
        };

        // Set table styles
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(TABLE_TEXT_COLOR);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setBackground(TABLE_BACKGROUND_COLOR);
        table.setForeground(TABLE_TEXT_COLOR);

        return table;
    }

    // Create Custom Button Panel
    private JPanel createButtonPanel() {
        JButton addButton = new JButton("Add Member");
        JButton removeButton = new JButton("Remove Member");
        JButton updateButton = new JButton("Update Member");

        // Set common properties for all buttons
        for (JButton button : new JButton[]{addButton, removeButton, updateButton}) {
            button.setBackground(THEME_COLOR);
            button.setForeground(Color.WHITE);
            button.setPreferredSize(new Dimension(120, 40));
        }

        // Add action listeners
        addButton.addActionListener(e -> showMemberInputDialog("Add", null));
        removeButton.addActionListener(e -> removeMember());
                updateButton.addActionListener(e -> {
                    int selectedRow = membersTable.getSelectedRow();
                    if (selectedRow != -1) {
                        Member selectedMember = memberList.get(selectedRow);
                        showMemberInputDialog("Update", selectedMember);
                    } else {
                        showError("Please select a member to update");
                    }
                });
        
                // Create button panel and add buttons
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(addButton);
                buttonPanel.add(removeButton);
                buttonPanel.add(updateButton);
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        
                return buttonPanel;
            }
        
            private void removeMember() {
                int selectedRow = membersTable.getSelectedRow(); // Get the selected row
                if (selectedRow != -1) {
                    Member memberToRemove = memberList.get(selectedRow); // Get the member object
            
                    // Confirm deletion from the user
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to remove the member: " + memberToRemove.getName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );
            
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Remove from database
                        if (removeMemberFromDatabase(memberToRemove)) {
                                                    // If successful, remove from list and table
                                                    memberList.remove(selectedRow);
                                                    tableModel.removeRow(selectedRow); // Remove from JTable
                                                    JOptionPane.showMessageDialog(this, "Member removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                                }
                                            }
                                        } else {
                                            showError("Please select a member to remove.");
                                        }
                                    }      
                                
                                    private boolean removeMemberFromDatabase(Member memberToRemove) {
                                        String sql = "DELETE FROM members WHERE member_id = ?";
                                        try (Connection conn = databaseConnection.getConnection();
                                             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                                            pstmt.setInt(1, memberToRemove.getMemberId()); // Set the member_id parameter
                                            int rowsDeleted = pstmt.executeUpdate();
                                    
                                            if (rowsDeleted > 0) {
                                                JOptionPane.showMessageDialog(this, "Member removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                                return true;
                                            } else {
                                                showError("No member found with the given ID.");
                                            }
                                        } catch (SQLException e) {
                                            showError("Error removing member from database: " + e.getMessage());
                                        }
                                        return false; // Return false if an error occurred or no rows were deleted
                                    }
                                    
                        
                            // Input dialog for inserting and updating member values
    private void showMemberInputDialog(String action, Member member) {
        JDialog dialog = new JDialog(this, action + " Member", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        inputFields = new ArrayList<>();
        String[] labels = {"Member Name:"};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            dialog.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            JTextField textField = new JTextField(20);
            if (member != null) {
                textField.setText(member.getName());
            }
            inputFields.add(textField);
            dialog.add(textField, gbc);
        }

        JButton confirmButton = new JButton(action);
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                if (action.equals("Add")) {
                    addMemberToDatabase(inputFields.get(0).getText());
                } else if (action.equals("Update")) {
                    updateMemberInDatabase(member);
                                    }
                                    dialog.dispose();
                                }
                            });
                            confirmButton.setBackground(THEME_COLOR);
                            confirmButton.setForeground(Color.WHITE);
                    
                            gbc.gridx = 0;
                            gbc.gridy = labels.length;
                            gbc.gridwidth = 2;
                            dialog.add(confirmButton, gbc);
                    
                            dialog.pack();
                            dialog.setLocationRelativeTo(this);
                            dialog.setVisible(true);
                        }
                    
                        private void updateMemberInDatabase(Member member) {
                            String sql = "UPDATE members SET member_name = ? WHERE member_id = ?";
                            try (Connection conn = databaseConnection.getConnection();
                                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                                pstmt.setString(1, member.getName()); // Set the new name
                                pstmt.setInt(2, member.getMemberId()); // Identify the record by member_id
                                int rowsUpdated = pstmt.executeUpdate();
                        
                                if (rowsUpdated > 0) {
                                    JOptionPane.showMessageDialog(this, "Member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    showError("No member found with the given ID.");
                                }
                            } catch (SQLException e) {
                                showError("Error updating member in database: " + e.getMessage());
                            }
                        }
                        
                        private boolean validateInputs() {
        for (JTextField field : inputFields) {
            if (field.getText().trim().isEmpty()) {
                showError("Please fill in all fields.");
                return false;
            }
        }
        return true;
    }

    // Add member to the database
    private void addMemberToDatabase(String name) {
        String sql = "INSERT INTO members (member_name) VALUES (?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Member added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            reloadTable();
        } catch (SQLException e) {
            showError("Error adding member: " + e.getMessage());
        }
    }

    // Load members from the database
// Updated loadMembersFromDatabase method to fetch borrowed books
private void loadMembersFromDatabase() {
    String sql = "SELECT m.member_id, m.member_name, GROUP_CONCAT(b.title, ', ') AS borrowed_books " +
                 "FROM members m " +
                 "LEFT JOIN BorrowedBooks bb ON m.member_id = bb.member_id " +
                 "LEFT JOIN Books b ON bb.book_id = b.book_id " +
                 "GROUP BY m.member_id, m.member_name"; // Updated query to include borrowed books

    try (Connection conn = databaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            int memberId = rs.getInt("member_id");
            String memberName = rs.getString("member_name");
            String borrowedBooks = rs.getString("borrowed_books");

            // Handle case where no books are borrowed (borrowedBooks could be null)
            if (borrowedBooks == null) {
                borrowedBooks = "No borrowed books";
            }

            // Create Member and display in the table
            memberList.add(new Member(memberId, memberName, borrowedBooks));
            tableModel.addRow(new Object[]{memberId, memberName, borrowedBooks}); // Add member data to the table
        }
    } catch (SQLException e) {
        showError("Error loading members from database: " + e.getMessage());
    }
}


    private void reloadTable() {
        tableModel.setRowCount(0);
        memberList.clear();
        loadMembersFromDatabase();
    }

    // Show error message
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
