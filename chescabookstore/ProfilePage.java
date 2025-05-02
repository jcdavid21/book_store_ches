package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;

public class ProfilePage extends JFrame {

    // Colors
    private static final Color SIDEBAR_COLOR = new Color(39, 55, 77);
    private static final Color HIGHLIGHT_COLOR = new Color(157, 178, 191);
    private static final Color TEXT_COLOR = new Color(221, 230, 237);
    private static final Color MAIN_BG_COLOR = new Color(250, 250, 252);
    private static final Color CARD_BG_COLOR = new Color(255, 255, 255);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color ERROR_COLOR = new Color(198, 40, 40);

    // Font
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);

    // Database connection
    private Connection connection;

    // User information
    private int currentUserId;
    private String currentUsername;
    private int roleId;
    private Map<String, String> userInfo = new HashMap<>();

    // UI Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    
    // Profile form fields
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    
    // Password change fields
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordMatchLabel;
    
    // Form state tracking
    private boolean isFormDirty = false;

    public ProfilePage(int userId, int roleId) {
        try {
            // Get connection from DatabaseConnection class
            this.connection = DatabaseConnection.getConnection();
            this.currentUserId = userId;
            this.roleId = roleId;
            
            // Get username from database
            this.currentUsername = getUsernameById(userId);
            
            // Load user information
            loadUserInfo();
            
            initializeUI();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private String getUsernameById(int userId) throws SQLException {
        String username = "";
        String query = "SELECT username FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }
        }
        return username;
    }
    
    private void loadUserInfo() throws SQLException {
        String query = "SELECT username, email, first_name, last_name, address, phone, registration_date " +
                      "FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("firstName", rs.getString("first_name") != null ? rs.getString("first_name") : "");
                userInfo.put("lastName", rs.getString("last_name") != null ? rs.getString("last_name") : "");
                userInfo.put("address", rs.getString("address") != null ? rs.getString("address") : "");
                userInfo.put("phone", rs.getString("phone") != null ? rs.getString("phone") : "");
                
                // Format registration date
                Timestamp regDate = rs.getTimestamp("registration_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
                userInfo.put("registrationDate", dateFormat.format(regDate));
            }
        }
    }
    
    private void initializeUI() {
        setTitle("Libroloco - My Profile");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        setLayout(new BorderLayout());

        // Create and add sidebar
        createSidebar();

        // Create and add content area
        createContentArea();

        setVisible(true);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(SIDEBAR_COLOR);
        logoPanel.setMaximumSize(new Dimension(200, 100));
        JLabel logoLabel = new JLabel("Libroloco");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(TEXT_COLOR);
        logoPanel.add(logoLabel);
        sidebarPanel.add(logoPanel);

        // Add some space
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Menu items
        String[] menuItems = {"Home", "My Cart", "Wishlist", "My Orders", "Profile", "Logout"};

        for (String item : menuItems) {
            JPanel menuItemPanel = createMenuItemPanel(item);
            sidebarPanel.add(menuItemPanel);
        }

        // Add to main frame
        add(sidebarPanel, BorderLayout.WEST);
    }

    private JPanel createMenuItemPanel(String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBackground(text.equals("Profile") ? HIGHLIGHT_COLOR : SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 40));

        JLabel label = new JLabel(text);
        label.setFont(MENU_FONT);
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!text.equals("Profile")) { // Don't change highlight for active menu
                    panel.setBackground(HIGHLIGHT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("Profile")) { // Don't reset highlight for active menu
                    panel.setBackground(SIDEBAR_COLOR);
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMenuItemClick(text);
            }
        });

        return panel;
    }

    private void createContentArea() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(MAIN_BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(MAIN_BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        headerPanel.setLayout(new BorderLayout());

        // Left side with header label
        JLabel headerLabel = new JLabel("My Profile");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(new Color(50, 50, 70));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Main panel with tabbed panes
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(MAIN_BG_COLOR);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(TITLE_FONT);
        tabbedPane.setBackground(MAIN_BG_COLOR);
        
        // Create tabs
        JPanel profileInfoPanel = createProfileInfoPanel();
        JPanel securityPanel = createSecurityPanel();
        
        // Add tabs to tabbed pane
        tabbedPane.addTab("Profile Information", profileInfoPanel);
        tabbedPane.addTab("Security", securityPanel);
        
        // Add tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Status label for feedback messages
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add to content panel
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(MAIN_BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Profile form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_BG_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Profile form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(CARD_BG_COLOR);
        fieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        
        // Labels
        JLabel usernameLabel = new JLabel("Username:");
        JLabel firstNameLabel = new JLabel("First Name:");
        JLabel lastNameLabel = new JLabel("Last Name:");
        JLabel emailLabel = new JLabel("Email:");
        JLabel phoneLabel = new JLabel("Phone:");
        JLabel addressLabel = new JLabel("Address:");
        JLabel regDateLabel = new JLabel("Member Since:");
        
        // Style all labels
        JLabel[] labels = {usernameLabel, firstNameLabel, lastNameLabel, emailLabel, phoneLabel, addressLabel, regDateLabel};
        for (JLabel label : labels) {
            label.setFont(LABEL_FONT);
        }
        
        // Username field (disabled)
        JTextField usernameField = new JTextField(userInfo.get("username"));
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(240, 240, 240));
        usernameField.setFont(BODY_FONT);
        
        // First name field
        firstNameField = new JTextField(userInfo.get("firstName"));
        firstNameField.setFont(BODY_FONT);
        
        // Last name field
        lastNameField = new JTextField(userInfo.get("lastName"));
        lastNameField.setFont(BODY_FONT);
        
        // Email field
        emailField = new JTextField(userInfo.get("email"));
        emailField.setFont(BODY_FONT);
        
        // Phone field
        phoneField = new JTextField(userInfo.get("phone"));
        phoneField.setFont(BODY_FONT);
        
        // Address area
        addressArea = new JTextArea(userInfo.get("address"), 3, 20);
        addressArea.setFont(BODY_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);
        
        // Registration date (read-only)
        JTextField regDateField = new JTextField(userInfo.get("registrationDate"));
        regDateField.setEditable(false);
        regDateField.setBackground(new Color(240, 240, 240));
        regDateField.setFont(BODY_FONT);
        
        // Add components to form panel using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        fieldsPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        fieldsPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        fieldsPanel.add(firstNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        fieldsPanel.add(firstNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        fieldsPanel.add(lastNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        fieldsPanel.add(lastNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        fieldsPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        fieldsPanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        fieldsPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        fieldsPanel.add(phoneField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        fieldsPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        fieldsPanel.add(addressScrollPane, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        fieldsPanel.add(regDateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        fieldsPanel.add(regDateField, gbc);
        
        // Add change listeners to track form changes
        DocumentListener changeListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isFormDirty = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isFormDirty = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isFormDirty = true;
            }
        };
        
        firstNameField.getDocument().addDocumentListener(changeListener);
        lastNameField.getDocument().addDocumentListener(changeListener);
        emailField.getDocument().addDocumentListener(changeListener);
        phoneField.getDocument().addDocumentListener(changeListener);
        addressArea.getDocument().addDocumentListener(changeListener);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton saveButton = new JButton("Save Changes");
        styleButton(saveButton);
        saveButton.addActionListener(e -> saveProfileChanges());
        
        JButton resetButton = new JButton("Reset");
        styleSecondaryButton(resetButton);
        resetButton.addActionListener(e -> resetProfileForm());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        // Add components to form panel
        formPanel.add(fieldsPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(buttonPanel);
        
        // Side panel with avatar and account info
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(MAIN_BG_COLOR);
        sidePanel.setPreferredSize(new Dimension(250, 500));
        
        // Avatar panel
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(CARD_BG_COLOR);
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Avatar "image" (placeholder circle with initials)
        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle
                g2d.setColor(SIDEBAR_COLOR);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                // Draw initials
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 40));
                
                String initials = "";
                if (!userInfo.get("firstName").isEmpty()) {
                    initials += userInfo.get("firstName").substring(0, 1).toUpperCase();
                }
                if (!userInfo.get("lastName").isEmpty()) {
                    initials += userInfo.get("lastName").substring(0, 1).toUpperCase();
                }
                if (initials.isEmpty()) {
                    initials = userInfo.get("username").substring(0, 1).toUpperCase();
                }
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initials);
                int textHeight = fm.getHeight();
                
                g2d.drawString(initials, (getWidth() - textWidth) / 2, 
                              (getHeight() - textHeight) / 2 + fm.getAscent());
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 120);
            }
        };
        
        JPanel avatarWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarWrapperPanel.setBackground(CARD_BG_COLOR);
        avatarWrapperPanel.add(avatarCircle);
        
        JLabel usernameDisplayLabel = new JLabel(userInfo.get("username"));
        usernameDisplayLabel.setFont(TITLE_FONT);
        usernameDisplayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String roleText = roleId == 1 ? "Member" : "Administrator";
        JLabel roleLabel = new JLabel(roleText);
        roleLabel.setFont(BODY_FONT);
        roleLabel.setForeground(new Color(100, 100, 100));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton changeAvatarButton = new JButton("Change Avatar");
        styleSecondaryButton(changeAvatarButton);
        changeAvatarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeAvatarButton.setMaximumSize(new Dimension(150, 30));
        
        // Avatar function is just a placeholder for now
        changeAvatarButton.addActionListener(e -> 
            JOptionPane.showMessageDialog(this, "Avatar upload functionality coming soon!")
        );
        
        avatarPanel.add(avatarWrapperPanel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        avatarPanel.add(usernameDisplayLabel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        avatarPanel.add(roleLabel);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        avatarPanel.add(changeAvatarButton);
        
        // Account tips panel
        JPanel tipsPanel = new JPanel();
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setBackground(CARD_BG_COLOR);
        tipsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tipsPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel tipsLabel = new JLabel("Tips & Information");
        tipsLabel.setFont(SECTION_FONT);
        tipsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea tipsArea = new JTextArea(
            "• Keep your profile information up to date\n" +
            "• Use a phone number where you can be reached\n" +
            "• Your address is used for shipping orders\n" +
            "• Change your password regularly\n" +
            "• Check your order history for past purchases"
        );
        tipsArea.setFont(BODY_FONT);
        tipsArea.setEditable(false);
        tipsArea.setLineWrap(true);
        tipsArea.setWrapStyleWord(true);
        tipsArea.setBackground(CARD_BG_COLOR);
        tipsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        tipsPanel.add(tipsLabel);
        tipsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        tipsPanel.add(tipsArea);
        
        // Add components to side panel
        sidePanel.add(avatarPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidePanel.add(tipsPanel);
        
        // Add panels to main panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(sidePanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(MAIN_BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Password change panel
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.Y_AXIS));
        passwordPanel.setBackground(CARD_BG_COLOR);
        passwordPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel securityHeader = new JLabel("Change Password");
        securityHeader.setFont(SECTION_FONT);
        securityHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG_COLOR);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        
        // Labels
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        JLabel newPasswordLabel = new JLabel("New Password:");
        JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
        
        // Style all labels
        JLabel[] labels = {currentPasswordLabel, newPasswordLabel, confirmPasswordLabel};
        for (JLabel label : labels) {
            label.setFont(LABEL_FONT);
        }
        
        // Password fields
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        // Password match indicator
        passwordMatchLabel = new JLabel("");
        passwordMatchLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        // Add document listeners to check for password match
        DocumentListener passwordListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }
        };
        
        newPasswordField.getDocument().addDocumentListener(passwordListener);
        confirmPasswordField.getDocument().addDocumentListener(passwordListener);
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        formPanel.add(currentPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(currentPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(newPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(newPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(passwordMatchLabel, gbc);
        
        // Password requirements panel
        JPanel requirementsPanel = new JPanel();
        requirementsPanel.setLayout(new BoxLayout(requirementsPanel, BoxLayout.Y_AXIS));
        requirementsPanel.setBackground(new Color(245, 245, 245));
        requirementsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        requirementsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel requirementsHeader = new JLabel("Password Requirements:");
        requirementsHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        requirementsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea requirementsText = new JTextArea(
            "• At least 8 characters long\n" +
            "• Contains at least one uppercase letter\n" +
            "• Contains at least one lowercase letter\n" +
            "• Contains at least one number\n" +
            "• Contains at least one special character"
        );
        requirementsText.setFont(BODY_FONT);
        requirementsText.setEditable(false);
        requirementsText.setLineWrap(true);
        requirementsText.setWrapStyleWord(true);
        requirementsText.setBackground(new Color(245, 245, 245));
        requirementsText.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        requirementsPanel.add(requirementsHeader);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        requirementsPanel.add(requirementsText);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton changePasswordButton = new JButton("Change Password");
        styleButton(changePasswordButton);
        changePasswordButton.addActionListener(e -> changePassword());
        
        JButton clearButton = new JButton("Clear");
        styleSecondaryButton(clearButton);
        clearButton.addActionListener(e -> {
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            passwordMatchLabel.setText("");
        });
        
        buttonPanel.add(clearButton);
        buttonPanel.add(changePasswordButton);
        
        // Add all components to password panel
        passwordPanel.add(securityHeader);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        passwordPanel.add(formPanel);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        passwordPanel.add(requirementsPanel);
        passwordPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        passwordPanel.add(buttonPanel);
        
        // Account activity panel
        JPanel activityPanel = new JPanel();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));
        activityPanel.setBackground(CARD_BG_COLOR);
        activityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel activityHeader = new JLabel("Account Activity");
        activityHeader.setFont(SECTION_FONT);
        activityHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea activityInfo = new JTextArea(
            "Last login: April 30, 2025 at 10:45 AM\n" +
            "Last password change: March 15, 2025\n" +
            "Last order placed: April 22, 2025\n" +
            "Recent device: Windows PC (Chrome)"
        );
        activityInfo.setFont(BODY_FONT);
        activityInfo.setEditable(false);
        activityInfo.setLineWrap(true);
        activityInfo.setWrapStyleWord(true);
        activityInfo.setBackground(CARD_BG_COLOR);
        activityInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        activityPanel.add(activityHeader);
        activityPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        activityPanel.add(activityInfo);
        
        // Add a spacer between panels
        JPanel spacerPanel = new JPanel();
        spacerPanel.setPreferredSize(new Dimension(0, 20));
        spacerPanel.setBackground(MAIN_BG_COLOR);
        
        // Main security panel to hold both sub-panels
        JPanel securityMainPanel = new JPanel();
        securityMainPanel.setLayout(new BoxLayout(securityMainPanel, BoxLayout.Y_AXIS));
        securityMainPanel.setBackground(MAIN_BG_COLOR);
        
        securityMainPanel.add(passwordPanel);
        securityMainPanel.add(spacerPanel);
        securityMainPanel.add(activityPanel);
        
        panel.add(securityMainPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void checkPasswordMatch() {
        char[] newPass = newPasswordField.getPassword();
        char[] confirmPass = confirmPasswordField.getPassword();
        
        if (newPass.length > 0 && confirmPass.length > 0) {
            if (Arrays.equals(newPass, confirmPass)) {
                passwordMatchLabel.setText("Passwords match");
                passwordMatchLabel.setForeground(SUCCESS_COLOR);
            } else {
                passwordMatchLabel.setText("Passwords do not match");
                passwordMatchLabel.setForeground(ERROR_COLOR);
            }
        } else {
            passwordMatchLabel.setText("");
        }
    }
    
    private void saveProfileChanges() {
        // Validate email format
        if (!isValidEmail(emailField.getText())) {
            showStatusMessage("Please enter a valid email address", ERROR_COLOR);
            return;
        }
        
        // Prepare SQL query
        String query = "UPDATE users SET email = ?, first_name = ?, last_name = ?, " +
                      "address = ?, phone = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, emailField.getText().trim());
            stmt.setString(2, firstNameField.getText().trim());
            stmt.setString(3, lastNameField.getText().trim());
            stmt.setString(4, addressArea.getText().trim());
            stmt.setString(5, phoneField.getText().trim());
            stmt.setInt(6, currentUserId);
            
            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated > 0) {
                // Update was successful
                showStatusMessage("Profile updated successfully", SUCCESS_COLOR);
                
                // Update the user info map with new values
                userInfo.put("email", emailField.getText().trim());
                userInfo.put("firstName", firstNameField.getText().trim());
                userInfo.put("lastName", lastNameField.getText().trim());
                userInfo.put("address", addressArea.getText().trim());
                userInfo.put("phone", phoneField.getText().trim());
                
                isFormDirty = false;
            } else {
                showStatusMessage("Failed to update profile", ERROR_COLOR);
            }
        } catch (SQLException e) {
            showStatusMessage("Database error: " + e.getMessage(), ERROR_COLOR);
            e.printStackTrace();
        }
    }
    
    private void resetProfileForm() {
        // Reset form fields to values in userInfo map
        firstNameField.setText(userInfo.get("firstName"));
        lastNameField.setText(userInfo.get("lastName"));
        emailField.setText(userInfo.get("email"));
        phoneField.setText(userInfo.get("phone"));
        addressArea.setText(userInfo.get("address"));
        
        isFormDirty = false;
        showStatusMessage("Form reset to saved values", null);
    }
    
    private void changePassword() {
        // Get password values
        char[] currentPass = currentPasswordField.getPassword();
        char[] newPass = newPasswordField.getPassword();
        char[] confirmPass = confirmPasswordField.getPassword();
        
        // Check if all fields are filled
        if (currentPass.length == 0 || newPass.length == 0 || confirmPass.length == 0) {
            showStatusMessage("Please fill all password fields", ERROR_COLOR);
            return;
        }
        
        // Check if new passwords match
        if (!Arrays.equals(newPass, confirmPass)) {
            showStatusMessage("New passwords do not match", ERROR_COLOR);
            return;
        }
        
        // Check password requirements
        if (!isValidPassword(new String(newPass))) {
            showStatusMessage("New password does not meet requirements", ERROR_COLOR);
            return;
        }
        
        // Verify current password
        if (!verifyCurrentPassword(new String(currentPass))) {
            showStatusMessage("Current password is incorrect", ERROR_COLOR);
            return;
        }
        
        // Update password in database
        if (updatePassword(new String(newPass))) {
            // Clear password fields
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            passwordMatchLabel.setText("");
            
            showStatusMessage("Password updated successfully", SUCCESS_COLOR);
        } else {
            showStatusMessage("Failed to update password", ERROR_COLOR);
        }
    }
    
    private boolean verifyCurrentPassword(String passwordToCheck) {
        boolean isValid = false;
        
        String query = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                // For simplicity, this example uses direct comparison
                // In a real application, you would use password hashing
                isValid = storedPassword.equals(passwordToCheck);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return isValid;
    }
    
    private boolean updatePassword(String newPassword) {
        boolean success = false;
        
        // In a real application, you would hash the password before storing
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, currentUserId);
            
            int rowsUpdated = stmt.executeUpdate();
            success = (rowsUpdated > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return success;
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private boolean isValidPassword(String password) {
        // Password requirements validation
        // At least 8 characters
        if (password.length() < 8) return false;
        
        // At least one uppercase letter
        if (!password.matches(".*[A-Z].*")) return false;
        
        // At least one lowercase letter
        if (!password.matches(".*[a-z].*")) return false;
        
        // At least one digit
        if (!password.matches(".*\\d.*")) return false;
        
        // At least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return false;
        
        return true;
    }
    
    private void showStatusMessage(String message, Color color) {
        statusLabel.setText(message);
        if (color != null) {
            statusLabel.setForeground(color);
        } else {
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    private void styleButton(JButton button) {
        button.setBackground(SIDEBAR_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(LABEL_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 40));

        // Apply Java2D graphics customization
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create rounded rectangle shape
                RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 12, 12);

                // Fill the button
                g2d.setColor(c.getBackground());
                g2d.fill(shape);

                // Add a subtle gradient effect
                GradientPaint gradient = new GradientPaint(
                    0, 0, c.getBackground().brighter(),
                    0, c.getHeight(), c.getBackground());
                g2d.setPaint(gradient);
                g2d.fill(shape);

                // Clean up
                g2d.dispose();

                // Paint the text and icon
                super.paint(g, c);
            }
        });
    }
    
    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(220, 220, 220));
        button.setForeground(new Color(50, 50, 50));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(LABEL_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
    }
    
     private void handleMenuItemClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                goToMainPage();
                break;
            case "My Cart":
                // TODO: Implement cart page
                setVisible(false);
                new CartPage(currentUserId, roleId);
                break;
            case "Wishlist":
                // TODO: Implement cart page
                setVisible(false);
                new WishlistPage(currentUserId, roleId);
                break;
            case "My Orders":
              
                dispose();
                SwingUtilities.invokeLater(() -> new OrdersPage(currentUserId, roleId));
                break;
            case "Profile":
                dispose();
                SwingUtilities.invokeLater(() -> new ProfilePage(currentUserId, roleId));
                break;
            case "Logout":
                logout();
                break;
            default:
                // Do nothing
        }
    }

    private void goToMainPage() {
        // Close this page and open MainPage
        dispose();
        SwingUtilities.invokeLater(() -> new MainPage(currentUserId, roleId));
    }

    
    private void logout() {
        int response = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
                
        if (response == JOptionPane.YES_OPTION) {
            // Close the current window
            dispose();
            
            // Open the login form
            SwingUtilities.invokeLater(() -> {
                new LoginForm();
            });
        }
    }
    
    // Main method for testing
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create database connection first
        try {
            DatabaseConnection.getConnection();
            
            // For testing, we'll use a hardcoded user ID (1) and role ID (1 for Member)
            int testUserId = 1;
            int testRoleId = 1;
            
            // Create and show profile page
            SwingUtilities.invokeLater(() -> {
                ProfilePage profilePage = new ProfilePage(testUserId, testRoleId);
                profilePage.setVisible(true);
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}