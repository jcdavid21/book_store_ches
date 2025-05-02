package chescabookstore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SignupForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextArea txtAddress;
    private JTextField txtPhone;
    private JButton btnSignup;
    private JButton btnBack;
    private JLabel lblMessage;

    public SignupForm() {
        setTitle("Book Store Sign Up");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel lblTitle = new JLabel("Create New Account", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(lblTitle, gbc);
        
        // Username
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(lblUsername, gbc);
        
        txtUsername = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(txtUsername, gbc);
        
        // Password
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(txtPassword, gbc);
        
        // Confirm Password
        JLabel lblConfirmPassword = new JLabel("Confirm Password:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblConfirmPassword, gbc);
        
        txtConfirmPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(txtConfirmPassword, gbc);
        
        // Email
        JLabel lblEmail = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(lblEmail, gbc);
        
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(txtEmail, gbc);
        
        // First Name
        JLabel lblFirstName = new JLabel("First Name:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(lblFirstName, gbc);
        
        txtFirstName = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(txtFirstName, gbc);
        
        // Last Name
        JLabel lblLastName = new JLabel("Last Name:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(lblLastName, gbc);
        
        txtLastName = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 6;
        formPanel.add(txtLastName, gbc);
        
        // Address
        JLabel lblAddress = new JLabel("Address:");
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(lblAddress, gbc);
        
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        gbc.gridx = 1;
        gbc.gridy = 7;
        formPanel.add(scrollAddress, gbc);
        
        // Phone
        JLabel lblPhone = new JLabel("Phone:");
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(lblPhone, gbc);
        
        txtPhone = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 8;
        formPanel.add(txtPhone, gbc);
        
        // Message for feedback
        lblMessage = new JLabel("");
        lblMessage.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        formPanel.add(lblMessage, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSignup = new JButton("Sign Up");
        btnBack = new JButton("Back to Login");
        
        buttonPanel.add(btnSignup);
        buttonPanel.add(btnBack);
        
        // Add action listeners
        btnSignup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                signup();
            }
        });
        
        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backToLogin();
            }
        });
        
        // Add panels to frame
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        
        setVisible(true);
    }
    
    private void signup() {
        // Get values from the form
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String email = txtEmail.getText();
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String address = txtAddress.getText();
        String phone = txtPhone.getText();
        
        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            lblMessage.setText("Username, password, and email are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            lblMessage.setText("Passwords do not match");
            return;
        }
        
        // Email validation (basic check)
        if (!email.contains("@") || !email.contains(".")) {
            lblMessage.setText("Invalid email format");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if username or email already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            
            ResultSet checkResult = checkStmt.executeQuery();
            checkResult.next();
            int count = checkResult.getInt(1);
            
            if (count > 0) {
                lblMessage.setText("Username or email already exists");
                return;
            }
            
            // Insert user data
            String insertQuery = "INSERT INTO users (username, password, email, first_name, last_name, address, phone, role_id) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
            
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // In a real app, use password hashing!
            insertStmt.setString(3, email);
            insertStmt.setString(4, firstName);
            insertStmt.setString(5, lastName);
            insertStmt.setString(6, address);
            insertStmt.setString(7, phone);
            
            int rowsAffected = insertStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                backToLogin();
            } else {
                lblMessage.setText("Registration failed. Please try again.");
            }
            
        } catch (SQLException ex) {
            lblMessage.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void backToLogin() {
        new LoginForm();
        dispose();
    }
}