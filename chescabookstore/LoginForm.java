package chescabookstore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnSignup;
    private JLabel lblMessage;

    public LoginForm() {
        setTitle("Book Store Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create panel with GridBagLayout for better control
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Create title label
        JLabel lblTitle = new JLabel("Book Store Login", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        // Username label and field
        JLabel lblUsername = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblUsername, gbc);
        
        txtUsername = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtUsername, gbc);
        
        // Password label and field
        JLabel lblPassword = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtPassword, gbc);
        
        // Message label for feedback
        lblMessage = new JLabel("");
        lblMessage.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(lblMessage, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnLogin = new JButton("Login");
        btnSignup = new JButton("Sign Up");
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnSignup);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        // Add action listeners
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        
        btnSignup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSignupForm();
            }
        });
        
        // Add panel to frame
        add(panel);
        setVisible(true);
    }
    
    private void login() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Username and password cannot be empty");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, role_id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password); // In a real app, use password hashing!
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                int roleId = rs.getInt("role_id");
                
                // Login successful
                lblMessage.setText("");
                JOptionPane.showMessageDialog(this, "Login successful!");
                
                // Open main page and pass user details
                openMainPage(userId, roleId);
                
                // Close login form
                dispose();
            } else {
                lblMessage.setText("Invalid username or password");
            }
            
        } catch (SQLException ex) {
            lblMessage.setText("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void openSignupForm() {
        new SignupForm();
        dispose();
    }
    
    private void openMainPage(int userId, int roleId) {
        SwingUtilities.invokeLater(() -> {
            new MainPage(userId, roleId);
        });
    }
    
    public static void main(String[] args) {
        try {
            // Set look and feel to system
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginForm();
        });
    }
}