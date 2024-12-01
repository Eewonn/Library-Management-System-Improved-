package com.library.management.gui;

import com.library.management.database.*;
import com.library.management.classes.Book;
import com.library.management.classes.Member;
import com.library.management.classes.User;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final Color DARKER_THEME_COLOR = Color.BLACK;
    private static final Color TABLE_TEXT_COLOR = Color.WHITE;
    private static final Color TABLE_HEADER_COLOR = new Color(60, 106, 117);
    private static final Color TABLE_BACKGROUND_COLOR = new Color(60, 106, 117);
    private static final Color PLACEHOLDER_COLOR = Color.GRAY;

    // Attributes
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private List<Member> memberList;
    private List<JTextField> inputFields;

    // Constructor
    public MembersPage(User user) {
        super(user);
        memberList = new ArrayList<>();
        setTitle("Members Management");
        setupUI();
        loadMembersFromDatabase();
        setVisible(true);
    }

    // GUI SET UP METHODS
    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Member ID", "Member Name", "Borrowed Books"};
        tableModel = new DefaultTableModel(columnNames, 0);
        membersTable = createMembersTable();

        // Create a search bar
        JTextField searchField = new JTextField("Search by Member Name");
        searchField.setToolTipText("Search by Member Name");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setForeground(PLACEHOLDER_COLOR);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search by Member Name")) {
                    searchField.setForeground(Color.BLACK);
                    searchField.setText("");
                }
            }
        
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(PLACEHOLDER_COLOR); 
                    searchField.setText("Search by Member Name");
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMembers(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMembers(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMembers(searchField.getText());
            }
        });

        // Create a panel to hold the search field
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 150, 0, 150));

        // Create a JScrollPane for the table
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setPreferredSize(new Dimension(0, 0));
        scrollPane.setMaximumSize(new Dimension(0, 0));

        // Customize the scrollbar
        customizeScrollBar(scrollPane);

        // Create a panel to add padding around the JScrollPane
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 10, 50));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Create buttons to add, remove, and update members
        JPanel buttonPanel = createButtonPanel();

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void filterMembers(String query) {
        // Clear the current table model
        tableModel.setRowCount(0);
        
        // Convert query to lowercase for case-insensitive matching
        String lowerCaseQuery = query.toLowerCase();
        
        // Filter the member list based on the search query
        for (Member member : memberList) {
            String memberName = member.getName().toLowerCase(); 
            
            // Check if the member's name contains the search query
            if (memberName.contains(lowerCaseQuery)) {
                // Convert the borrowedBooks list to a list of titles
                List<String> borrowedBookTitles = member.getBorrowedBooks()
                                                        .stream()
                                                        .map(Book::getTitle) 
                                                        .toList(); 
                
                // Create a string representation of borrowed books
                String borrowedBooks = borrowedBookTitles.isEmpty() 
                    ? "No borrowed books" 
                    : String.join(", ", borrowedBookTitles); 
    
                // Add the member's details to the table model
                tableModel.addRow(new Object[] {
                    member.getMemberId(),
                    member.getName(),
                    borrowedBooks 
                });
            }
        }
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

        // Center align the column headers
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // Center align the cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set table color
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(TABLE_TEXT_COLOR);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setBorder(new LineBorder(Color.BLACK, 1));
        table.setBackground(TABLE_BACKGROUND_COLOR);
        table.setForeground(TABLE_TEXT_COLOR);

        return table;
    }

    // Method to customize the scrollbar
    private void customizeScrollBar(JScrollPane scrollPane) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();

        verticalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = THEME_COLOR; 
                this.trackColor = Color.WHITE; 
            }
        });

        horizontalScrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = THEME_COLOR; 
                this.trackColor = Color.WHITE; 
            }
        });
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
        addButton.addActionListener(e -> showMemberInputDialog("Add", null, THEME_COLOR, DARKER_THEME_COLOR));
        removeButton.addActionListener(e -> removeMember());
        updateButton.addActionListener(e -> {
            int selectedRow = membersTable.getSelectedRow();
            if (selectedRow != -1) {
                Member selectedMember = memberList.get(selectedRow);
                showMemberInputDialog("Update", selectedMember, THEME_COLOR, DARKER_THEME_COLOR);
            } else {
                showError("Please select a member to update");
            }
        });

        // Create button panel and add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);

        // Add padding to the bottom of the button panel
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0)); 

        return buttonPanel;
    }

    // Input dialog for inserting and updating member values
    private void showMemberInputDialog(String action, Member member, Color themeColor, Color darkerThemeColor) {
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
            textField.setBackground(Color.WHITE);
            textField.setForeground(darkerThemeColor);
            dialog.add(textField, gbc);
        }

        JButton confirmButton = new JButton(action);
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                if (action.equals("Add")) {
                    addMember();
                } else if (action.equals("Update")) {
                    updateMember(member);
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

        dialog.setBackground(themeColor);
        dialog.setForeground(darkerThemeColor);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Input Validation Methods
    private boolean validateInputs() {
        String name = inputFields.get(0).getText();
        if (name.isEmpty()) {
            showError("Please fill in all fields correctly");
            return false;
        }
        return true;
    }

    // Database Methods
    private void loadMembersFromDatabase() {
        String sql = "SELECT m.member_id, m.member_name, GROUP_CONCAT(b.title, ', ') AS borrowed_books " +
                     "FROM members m " +
                     "LEFT JOIN BorrowedBooks bb ON m.member_id = bb.member_id " +
                     "LEFT JOIN Books b ON bb.book_id = b.book_id " +
                     "GROUP BY m.member_id, m.member_name"; 

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
                tableModel.addRow(new Object[]{memberId, memberName, borrowedBooks}); 
            }
        } catch (SQLException e) {
            showError("Error loading members from database: " + e.getMessage());
        }
    }

    // Add member to the table
    private void addMember() {
        if (validateInputs()) {
            String name = inputFields.get(0).getText();
            Member newMember = new Member(name);
            int generatedId = insertMemberIntoDatabase(newMember); 
            
            if (generatedId != -1) { 
                newMember.setMemberId(generatedId); 
                memberList.add(newMember);
                tableModel.addRow(new Object[]{generatedId, name, "No borrowed books"}); 
                clearFields();
            } else {
                showError("Failed to add member to the database.");
            }
        }
    }

    // Update member in the table
    private void updateMember(Member member) {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow != -1) {
            String name = inputFields.get(0).getText();
            memberList.get(selectedRow).setName(name); 
            tableModel.setValueAt(name, selectedRow, 1);
            updateMemberInDatabase(member);
            clearFields();
        } else {
            showError("Please select a member to update");
        }
    }

    // Remove member from the table
    private void removeMember() {
        int selectedRow = membersTable.getSelectedRow(); 
        if (selectedRow != -1) {
            Member memberToRemove = memberList.get(selectedRow); 

            // Confirm deletion from the user
            int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to remove the member: " + memberToRemove.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Remove from database
                if (removeMemberFromDatabase(memberToRemove)) {
                    // If successful, remove from list and table
                    memberList.remove(selectedRow);
                    tableModel.removeRow(selectedRow); 
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
            pstmt.setInt(1, memberToRemove.getMemberId()); 
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                return true;
            } else {
                showError("No member found with the given ID.");
            }
        } catch (SQLException e) {
            showError("Error removing member from database: " + e.getMessage());
        }
        return false; 
    }

    // Database Methods
    private int insertMemberIntoDatabase(Member member) {
        String sql = "INSERT INTO members (member_name) VALUES (?)";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, member.getName());
            pstmt.executeUpdate();
            
            // Retrieve the generated member ID
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1); 
            }
        } catch (SQLException e) {
            showError("Error adding member to database: " + e.getMessage());
        }
        return -1; 
    }

    private void updateMemberInDatabase(Member member) {
        String sql = "UPDATE members SET member_name = ? WHERE member_id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getName()); 
            pstmt.setInt(2, member.getMemberId()); 
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Member updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("No member found with the given ID.");
            }
        } catch (SQLException e){
            showError("Error updating member in database: " + e.getMessage());
        }
    }

    // Utility Methods
    private void clearFields() {
        for (JTextField field : inputFields) {
            field.setText("");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}