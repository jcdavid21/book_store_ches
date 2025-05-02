package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.*;
import java.text.NumberFormat;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class CartPage extends JFrame {

    // Colors
    private static final Color SIDEBAR_COLOR = new Color(39, 55, 77);
    private static final Color HIGHLIGHT_COLOR = new Color(157, 178, 191);
    private static final Color TEXT_COLOR = new Color(221, 230, 237);
    private static final Color MAIN_BG_COLOR = new Color(250, 250, 252);
    private static final Color CARD_BG_COLOR = new Color(255, 255, 255);

    // Font
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    // Database connection
    private Connection connection;

    // User information
    private int currentUserId;
    private String currentUsername;
    private int roleId;

    // UI Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel cartItemsPanel;
    private JScrollPane scrollPane;
    private JLabel headerLabel;
    private JPanel summaryPanel;
    private JLabel totalItemsLabel;
    private JLabel subtotalLabel;
    private JButton checkoutButton;
    private JPanel emptyCartPanel;
    
    // Table for cart items
    private JTable cartTable;
    private DefaultTableModel tableModel;
    
    // Track cart items
    private double cartSubtotal = 0.0;
    private int totalItems = 0;
    private boolean isCartEmpty = true;

    public CartPage(int userId, int roleId) {
        try {
            // Get connection from DatabaseConnection class
            this.connection = DatabaseConnection.getConnection();
            this.currentUserId = userId;
            this.roleId = roleId;
            
            // Get username from database
            this.currentUsername = getUsernameById(userId);
            
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
    
    private void initializeUI() {
        setTitle("Libroloco - Your Cart");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        setLayout(new BorderLayout());

        // Create and add sidebar
        createSidebar();

        // Create and add content area
        createContentArea();

        // Load cart items
        loadCartItems();

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
        panel.setBackground(text.equals("My Cart") ? HIGHLIGHT_COLOR : SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 40));

        JLabel label = new JLabel(text);
        label.setFont(MENU_FONT);
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!text.equals("My Cart")) { // Don't change highlight for active menu
                    panel.setBackground(HIGHLIGHT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("My Cart")) { // Don't reset highlight for active menu
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
        headerLabel = new JLabel("Shopping Cart");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(new Color(50, 50, 70));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Continue Shopping button on right side
        JButton continueShoppingButton = new JButton("Continue Shopping");
        styleButton(continueShoppingButton);
        continueShoppingButton.addActionListener(e -> goToMainPage());
        headerPanel.add(continueShoppingButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Create main panel for cart
        JPanel mainCartPanel = new JPanel(new BorderLayout(0, 20));
        mainCartPanel.setBackground(MAIN_BG_COLOR);

        // Create cart items panel
        cartItemsPanel = new JPanel(new BorderLayout());
        cartItemsPanel.setBackground(MAIN_BG_COLOR);
        
        // Create empty cart panel (will be shown if cart is empty)
        createEmptyCartPanel();
        
        // Create table for cart items
        createCartTable();
        
        // Create summary panel (right side)
        createSummaryPanel();
        
        // Add cart items panel to main cart panel
        mainCartPanel.add(cartItemsPanel, BorderLayout.CENTER);
        
        // Add summary panel to main cart panel
        mainCartPanel.add(summaryPanel, BorderLayout.EAST);
        
        // Add main cart panel to content panel
        contentPanel.add(mainCartPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createEmptyCartPanel() {
        emptyCartPanel = new JPanel();
        emptyCartPanel.setLayout(new BoxLayout(emptyCartPanel, BoxLayout.Y_AXIS));
        emptyCartPanel.setBackground(MAIN_BG_COLOR);
        emptyCartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyCartPanel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        JLabel emptyCartIcon = new JLabel("🛒");
        emptyCartIcon.setFont(new Font("Segoe UI", Font.PLAIN, 70));
        emptyCartIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyCartLabel = new JLabel("Your cart is empty");
        emptyCartLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        emptyCartLabel.setForeground(new Color(100, 100, 100));
        emptyCartLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyCartSubLabel = new JLabel("Looks like you haven't added any books to your cart yet");
        emptyCartSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyCartSubLabel.setForeground(new Color(120, 120, 120));
        emptyCartSubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton browseButton = new JButton("Browse Books");
        styleButton(browseButton);
        browseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseButton.setMaximumSize(new Dimension(150, 35));
        browseButton.addActionListener(e -> goToMainPage());
        
        emptyCartPanel.add(Box.createVerticalGlue());
        emptyCartPanel.add(emptyCartIcon);
        emptyCartPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        emptyCartPanel.add(emptyCartLabel);
        emptyCartPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        emptyCartPanel.add(emptyCartSubLabel);
        emptyCartPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        emptyCartPanel.add(browseButton);
        emptyCartPanel.add(Box.createVerticalGlue());
    }
    
    private void createCartTable() {
        // Create table model with columns
        String[] columnNames = {"", "Book", "Price", "Quantity", "Total", ""};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the quantity column editable
                return column == 3;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) { // Cover image column
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };
        
        cartTable = new JTable(tableModel);
        cartTable.setRowHeight(120);
        cartTable.setShowGrid(false);
        cartTable.setIntercellSpacing(new Dimension(0, 10));
        cartTable.setFocusable(false);
        cartTable.setBackground(CARD_BG_COLOR);
        cartTable.setSelectionBackground(new Color(240, 240, 245));
        
        // Set custom renderers for each column
        cartTable.setDefaultRenderer(Object.class, new CartTableCellRenderer());
        
        // Set column widths
        TableColumnModel columnModel = cartTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Cover image
        columnModel.getColumn(1).setPreferredWidth(250); // Book title
        columnModel.getColumn(2).setPreferredWidth(80);  // Price
        columnModel.getColumn(3).setPreferredWidth(100); // Quantity
        columnModel.getColumn(4).setPreferredWidth(80);  // Total
        columnModel.getColumn(5).setPreferredWidth(50);  // Remove button
        
        // Create scroll pane for the cart table
        scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(MAIN_BG_COLOR);
        
        // Add editors for the quantity column
        columnModel.getColumn(3).setCellEditor(new DefaultCellEditor(createQuantityComboBox()));
        
        // Add a custom editor for the remove button column
        columnModel.getColumn(5).setCellRenderer(new ButtonRenderer());
        cartTable.addMouseListener(new TableButtonClickListener());
        
        cartItemsPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private JComboBox<Integer> createQuantityComboBox() {
        Integer[] quantities = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        JComboBox<Integer> quantityComboBox = new JComboBox<>(quantities);
        quantityComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int row = cartTable.getEditingRow();
                if (row >= 0) {
                    updateCartItemQuantity(row, (Integer) e.getItem());
                }
            }
        });
        return quantityComboBox;
    }
    
    private void createSummaryPanel() {
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(CARD_BG_COLOR);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        summaryPanel.setPreferredSize(new Dimension(300, 0));
        
        // Order Summary label
        JLabel summaryLabel = new JLabel("Order Summary");
        summaryLabel.setFont(TITLE_FONT);
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Divider
        JSeparator divider = new JSeparator();
        divider.setMaximumSize(new Dimension(260, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Total items label
        totalItemsLabel = new JLabel("Total Items: 0");
        totalItemsLabel.setFont(BODY_FONT);
        totalItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Subtotal label
        subtotalLabel = new JLabel("Subtotal: $0.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        subtotalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Checkout button
        checkoutButton = new JButton("Proceed to Checkout");
        styleButton(checkoutButton);
        checkoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutButton.setMaximumSize(new Dimension(260, 40));
        checkoutButton.addActionListener(e -> proceedToCheckout());
        
        // Add components to summary panel
        summaryPanel.add(summaryLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        summaryPanel.add(divider);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        summaryPanel.add(totalItemsLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        summaryPanel.add(checkoutButton);
    }
    
    private void loadCartItems() {
        // Clear existing table data
        tableModel.setRowCount(0);
        
        // Reset tracking variables
        cartSubtotal = 0.0;
        totalItems = 0;
        
        try {
            // Query to get cart items with book details
            String query = "SELECT c.id, c.book_id, c.quantity, b.title, b.author, b.price, b.cover_image " +
                           "FROM cart c " +
                           "JOIN books b ON c.book_id = b.id " +
                           "WHERE c.user_id = ?";
                           
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            isCartEmpty = true;
            
            while (rs.next()) {
                isCartEmpty = false;
                
                int cartId = rs.getInt("id");
                int bookId = rs.getInt("book_id");
                int quantity = rs.getInt("quantity");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("price");
                String coverImage = rs.getString("cover_image");
                double total = price * quantity;
                
                // Format price and total for display
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                String formattedPrice = currencyFormat.format(price);
                String formattedTotal = currencyFormat.format(total);
                
                // Create an ImageIcon for the cover image
                ImageIcon coverIcon = createCoverImageIcon(coverImage);
                
                // Add row to table model
                tableModel.addRow(new Object[]{
                    coverIcon,
                    "<html><b>" + title + "</b><br>by " + author + "</html>",
                    formattedPrice,
                    quantity,
                    formattedTotal,
                    cartId  // Store cart ID in the last column (will be used by the remove button)
                });
                
                // Update tracking variables
                cartSubtotal += total;
                totalItems += quantity;
            }
            
            rs.close();
            stmt.close();
            
            // Update summary panel
            updateSummaryPanel();
            
            // Show either the cart table or empty cart message
            showAppropriateView();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading cart items: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private ImageIcon createCoverImageIcon(String coverImagePath) {
        try {
            // Default image size for table
            int maxWidth = 60;
            int maxHeight = 90;
            
            if (coverImagePath != null && !coverImagePath.isEmpty()) {
                // Load and resize the image
                ImageIcon originalIcon = new ImageIcon(getClass().getResource("/assets/" + coverImagePath));
                Image originalImage = originalIcon.getImage();
                
                // Calculate scaling to maintain aspect ratio
                int origWidth = originalImage.getWidth(null);
                int origHeight = originalImage.getHeight(null);
                
                if (origWidth <= 0 || origHeight <= 0) {
                    // If image failed to load properly, return default icon
                    return createDefaultCoverIcon(maxWidth, maxHeight);
                }
                
                double widthRatio = (double)maxWidth / origWidth;
                double heightRatio = (double)maxHeight / origHeight;
                double ratio = Math.min(widthRatio, heightRatio);
                
                int scaledWidth = (int)(origWidth * ratio);
                int scaledHeight = (int)(origHeight * ratio);
                
                // Scale image maintaining aspect ratio
                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            // If any error occurs, return default icon
            e.printStackTrace();
        }
        
        // Return default icon if image loading fails or path is empty
        return createDefaultCoverIcon(60, 90);
    }
    
    private ImageIcon createDefaultCoverIcon(int width, int height) {
        // Create a default image with a light gray background and "No Image" text
        BufferedImage defaultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = defaultImage.createGraphics();
        
        // Set background color
        g2d.setColor(new Color(230, 230, 240));
        g2d.fillRect(0, 0, width, height);
        
        // Set text color and font
        g2d.setColor(new Color(150, 150, 150));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        // Center the text
        FontMetrics metrics = g2d.getFontMetrics();
        String text = "No Image";
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        g2d.dispose();
        
        return new ImageIcon(defaultImage);
    }
    
    private void updateSummaryPanel() {
        // Format the currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        
        // Update labels
        totalItemsLabel.setText("Total Items: " + totalItems);
        subtotalLabel.setText("Subtotal: " + currencyFormat.format(cartSubtotal));
        
        // Enable/disable checkout button based on cart status
        checkoutButton.setEnabled(!isCartEmpty);
    }
    
    private void showAppropriateView() {
        cartItemsPanel.removeAll();
        
        if (isCartEmpty) {
            cartItemsPanel.add(emptyCartPanel, BorderLayout.CENTER);
            summaryPanel.setVisible(false);
        } else {
            cartItemsPanel.add(scrollPane, BorderLayout.CENTER);
            summaryPanel.setVisible(true);
        }
        
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }
    
    private void updateCartItemQuantity(int row, int newQuantity) {
        try {
            // Get cart id from table model
            int cartId = (int) tableModel.getValueAt(row, 5);
            
            // Update quantity in database
            String updateQuery = "UPDATE cart SET quantity = ? WHERE id = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setInt(1, newQuantity);
            updateStmt.setInt(2, cartId);
            updateStmt.executeUpdate();
            updateStmt.close();
            
            // Get book price
            String priceStr = (String) tableModel.getValueAt(row, 2);
            double price = parseCurrency(priceStr);
            
            // Calculate new total
            double newTotal = price * newQuantity;
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
            // Update table model
            tableModel.setValueAt(currencyFormat.format(newTotal), row, 4);
            
            // Recalculate cart totals
            recalculateCartTotals();
            
            // Show success message
            showToastMessage("Quantity updated");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating quantity: " + e.getMessage(),
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void removeCartItem(int row) {
        try {
            // Get cart id from table model
            int cartId = (int) tableModel.getValueAt(row, 5);
            
            // Delete item from database
            String deleteQuery = "DELETE FROM cart WHERE id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, cartId);
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Remove row from table model
            tableModel.removeRow(row);
            
            // Recalculate cart totals
            recalculateCartTotals();
            
            // Check if cart is now empty
            if (tableModel.getRowCount() == 0) {
                isCartEmpty = true;
                showAppropriateView();
            }
            
            // Show success message
            showToastMessage("Item removed from cart");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error removing item: " + e.getMessage(),
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void recalculateCartTotals() {
        cartSubtotal = 0.0;
        totalItems = 0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int quantity = (int) tableModel.getValueAt(i, 3);
            String totalStr = (String) tableModel.getValueAt(i, 4);
            double rowTotal = parseCurrency(totalStr);
            
            cartSubtotal += rowTotal;
            totalItems += quantity;
        }
        
        updateSummaryPanel();
    }
    
    private double parseCurrency(String currencyStr) {
        // Remove currency symbol and commas, then parse to double
        return Double.parseDouble(currencyStr.replaceAll("[$,]", ""));
    }
    
    private void showToastMessage(String message) {
        // Create a custom JOptionPane
        JOptionPane pane = new JOptionPane(
            message,
            JOptionPane.INFORMATION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            new Object[]{}, // No buttons
            null);
        
        // Create dialog
        JDialog dialog = pane.createDialog(this, "");
        dialog.setUndecorated(true); // Remove title and border
        
        // Make it semi-transparent
        dialog.setOpacity(0.85f);
        
        // Center it near the bottom of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(
            screenSize.width/2 - dialog.getWidth()/2,
            screenSize.height - 150);
        
        // Auto-close after 1.5 seconds
        Timer timer = new Timer(1500, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
        
        dialog.setVisible(true);
    }
    
    private void proceedToCheckout() {
    try {
        // First, gather shipping address
        JPanel addressPanel = new JPanel(new GridLayout(0, 1));
        JTextField addressField = new JTextField(20);
        addressPanel.add(new JLabel("Shipping Address:"));
        addressPanel.add(addressField);
        
        // Then, gather payment method
        String[] paymentOptions = {"GCash", "Cash On Delivery"};
        JComboBox<String> paymentMethodComboBox = new JComboBox<>(paymentOptions);
        addressPanel.add(new JLabel("Payment Method:"));
        addressPanel.add(paymentMethodComboBox);
        
        // If GCash is selected, show additional fields
        JPanel gcashPanel = new JPanel(new GridLayout(0, 1));
        JTextField gcashNumberField = new JTextField(11);
        JTextField gcashNameField = new JTextField(20);
        
        gcashPanel.add(new JLabel("GCash Number:"));
        gcashPanel.add(gcashNumberField);
        gcashPanel.add(new JLabel("GCash Account Name:"));
        gcashPanel.add(gcashNameField);
        
        // Initially add GCash fields since it's default
        addressPanel.add(gcashPanel);
        
        // Add listener to show/hide GCash fields based on selection
        paymentMethodComboBox.addActionListener(e -> {
            String selectedMethod = (String) paymentMethodComboBox.getSelectedItem();
            gcashPanel.setVisible(selectedMethod.equals("GCash"));
            // Refresh the dialog
            SwingUtilities.getWindowAncestor(addressPanel).pack();
        });
        
        // Show the dialog
        int result = JOptionPane.showConfirmDialog(this, addressPanel, 
                "Checkout Information", JOptionPane.OK_CANCEL_OPTION);
        
        // Process if user clicked OK
        if (result == JOptionPane.OK_OPTION) {
            String shippingAddress = addressField.getText().trim();
            String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
            
            // Validate input
            if (shippingAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a shipping address", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Additional validation for GCash if selected
            if (paymentMethod.equals("GCash")) {
                String gcashNumber = gcashNumberField.getText().trim();
                String gcashName = gcashNameField.getText().trim();
                
                if (gcashNumber.isEmpty() || gcashName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all GCash details", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate GCash number format (should be 11 digits)
                if (!gcashNumber.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(this, "GCash number should be 11 digits", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Store GCash info in the payment method string
                paymentMethod = "GCash: " + gcashNumber + " (" + gcashName + ")";
            }
            
            // Create a new order with shipping and payment info
            String createOrderQuery = "INSERT INTO orders (user_id, order_date, total_amount, status, shipping_address, payment_method) " +
                                    "VALUES (?, NOW(), ?, 'Pending', ?, ?)";
            PreparedStatement orderStmt = connection.prepareStatement(createOrderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, currentUserId);
            orderStmt.setDouble(2, cartSubtotal);
            orderStmt.setString(3, shippingAddress);
            orderStmt.setString(4, paymentMethod);
            
            int rowsAffected = orderStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated order ID
                ResultSet generatedKeys = orderStmt.getGeneratedKeys();
                int orderId = -1;
                
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to create order, no ID obtained.");
                }
                
                // Create order items from cart items
                String createOrderItemsQuery = "INSERT INTO order_items (order_id, book_id, quantity, price) " +
                                              "SELECT ?, c.book_id, c.quantity, b.price " +
                                              "FROM cart c JOIN books b ON c.book_id = b.id " +
                                              "WHERE c.user_id = ?";
                
                PreparedStatement orderItemsStmt = connection.prepareStatement(createOrderItemsQuery);
                orderItemsStmt.setInt(1, orderId);
                orderItemsStmt.setInt(2, currentUserId);
                orderItemsStmt.executeUpdate();
                
                // Update book stock quantities
                String updateStockQuery = "UPDATE books b " +
                                         "JOIN cart c ON b.id = c.book_id " +
                                         "SET b.stock_quantity = b.stock_quantity - c.quantity " +
                                         "WHERE c.user_id = ?";
                PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery);
                updateStockStmt.setInt(1, currentUserId);
                updateStockStmt.executeUpdate();
                
                // Clear the user's cart
                String clearCartQuery = "DELETE FROM cart WHERE user_id = ?";
                PreparedStatement clearCartStmt = connection.prepareStatement(clearCartQuery);
                clearCartStmt.setInt(1, currentUserId);
                clearCartStmt.executeUpdate();
                
                // Show success message with options
                Object[] options = {"View My Orders", "Continue Shopping"};
                int choice = JOptionPane.showOptionDialog(this,
                    "Your order has been placed successfully!\nOrder #" + orderId + "\n\nThank you for your purchase!",
                    "Order Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
                    
                // Navigate based on user's choice
                dispose();
                if (choice == 0 || choice == JOptionPane.CLOSED_OPTION) {
                    // User chose "View My Orders" or closed the dialog
                    SwingUtilities.invokeLater(() -> new OrdersPage(currentUserId, roleId));
                } else {
                    // User chose "Continue Shopping"
                    SwingUtilities.invokeLater(() -> new MainPage(currentUserId, roleId));
                }
                
            } else {
                throw new SQLException("Failed to create order.");
            }
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error processing checkout: " + e.getMessage(), 
            "Checkout Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void goToMainPage() {
        // Close this page and open MainPage
        dispose();
        SwingUtilities.invokeLater(() -> new MainPage(currentUserId, roleId));
    }
    
    private void handleMenuItemClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                goToMainPage();
                break;
            case "My Cart":
                // Already on cart page
                break;
            case "Wishlist":
                // TODO: Implement cart page
                dispose();
                SwingUtilities.invokeLater(() -> new WishlistPage(currentUserId, roleId));
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
    
    private void styleButton(JButton button) {
        // Set basic button properties
        button.setBackground(new Color(39, 55, 77));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Create rounded border with padding
        int arc = 10;
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(29, 45, 67), 1, arc), // Outer rounded border
                BorderFactory.createEmptyBorder(4, 15, 4, 15) // Fixed vertical padding
        ));

        // Make sure the background is painted properly with rounded corners
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // Using consistent border in hover states
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(59, 82, 115));
                button.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(new Color(49, 72, 105), 1, arc),
                        BorderFactory.createEmptyBorder(4, 15, 4, 15) // Same padding as normal state
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(39, 55, 77));
                button.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(new Color(29, 45, 67), 1, arc),
                        BorderFactory.createEmptyBorder(4, 15, 4, 15) // Same padding as normal state
                ));
            }
        });
    }
    
    // Custom table cell renderer
    private class CartTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                      boolean isSelected, boolean hasFocus, 
                                                      int row, int column) {
            // Get the default component
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            // Configure the cell rendering based on column
            switch (column) {
                case 0: // Cover image column
                    // Just return the default rendering for ImageIcon objects
                    return super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                    
                case 1: // Book title & author column
                    // This column contains HTML-formatted text
                    label.setHorizontalAlignment(JLabel.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                    break;
                    
                case 2: // Price column
                case 4: // Total column
                    // Price and total are right-aligned
                    label.setHorizontalAlignment(JLabel.RIGHT);
                    break;
                    
                case 3: // Quantity column
                    // Center-align the quantity
                    label.setHorizontalAlignment(JLabel.CENTER);
                    break;
                    
                case 5: // Remove button column
                    // This column uses a custom renderer (ButtonRenderer)
                    break;
            }
            
            // Add a separator line at the bottom of each cell except the last row
            if (row < table.getRowCount() - 1) {
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
            }
            
            return label;
        }
    }
    
    // Custom renderer for the remove button column
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        private final Icon removeIcon;
        
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            
            // Create a custom remove icon (X) instead of loading from file
            removeIcon = createRemoveIcon();
            setIcon(removeIcon);
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Remove item");
        }
        
        private Icon createRemoveIcon() {
            // Create a custom "X" icon
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Use a soft red color for the X
                    g2.setColor(new Color(210, 80, 80));
                    g2.setStroke(new BasicStroke(2));
                    
                    // Draw the X
                    g2.drawLine(x + 3, y + 3, x + getIconWidth() - 3, y + getIconHeight() - 3);
                    g2.drawLine(x + getIconWidth() - 3, y + 3, x + 3, y + getIconHeight() - 3);
                    
                    g2.dispose();
                }
                
                @Override
                public int getIconWidth() {
                    return 16;
                }
                
                @Override
                public int getIconHeight() {
                    return 16;
                }
            };
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                    boolean isSelected, boolean hasFocus, 
                                                    int row, int column) {
            // Customize appearance based on selection state
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            
            return this;
        }
    }
    
    // Mouse listener for handling button clicks in the table
    private class TableButtonClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int column = cartTable.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / cartTable.getRowHeight();
            
            // Check if we clicked within bounds and on the button column
            if (row < cartTable.getRowCount() && row >= 0 && 
                column < cartTable.getColumnCount() && column >= 0) {
                
                // Check if it's the remove button column
                if (column == 5) {
                    // Confirm removal
                    int response = JOptionPane.showConfirmDialog(
                        CartPage.this,
                        "Remove this item from your cart?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (response == JOptionPane.YES_OPTION) {
                        removeCartItem(row);
                    }
                }
            }
        }
    }
    
    // Custom rounded border for buttons
    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;
        
        public RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    
    // Main method for testing
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Customize JScrollPane UI
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbDarkShadow", new Color(210, 210, 210));
            UIManager.put("ScrollBar.thumb", new Color(220, 220, 220));
            UIManager.put("ScrollBar.thumbHighlight", new Color(230, 230, 230));
            UIManager.put("ScrollBar.thumbShadow", new Color(220, 220, 220));
            UIManager.put("ScrollBar.track", new Color(240, 240, 240));
            
            // Apply custom ScrollBarUI
            UIManager.put("ScrollBarUI", BasicScrollBarUI.class.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            // For testing only - use user ID 1
            int default_user_id = 1;
            int default_role_id = 1;
            new CartPage(default_user_id, default_user_id);
        });
    }
}