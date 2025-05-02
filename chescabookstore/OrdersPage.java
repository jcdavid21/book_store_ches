package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class OrdersPage extends JFrame {

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
    private JPanel ordersPanel;
    private JScrollPane scrollPane;
    private JLabel headerLabel;
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JPanel emptyOrdersPanel;
    
    // Order details panel
    private JDialog orderDetailsDialog;
    private JTable orderItemsTable;
    private DefaultTableModel orderItemsTableModel;

    public OrdersPage(int userId, int roleId) {
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
        setTitle("Libroloco - My Orders");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        setLayout(new BorderLayout());

        // Create and add sidebar
        createSidebar();

        // Create and add content area
        createContentArea();

        // Load orders 
        loadOrders();

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
        panel.setBackground(text.equals("My Orders") ? HIGHLIGHT_COLOR : SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 40));

        JLabel label = new JLabel(text);
        label.setFont(MENU_FONT);
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!text.equals("My Orders")) { // Don't change highlight for active menu
                    panel.setBackground(HIGHLIGHT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("My Orders")) { // Don't reset highlight for active menu
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
        headerLabel = new JLabel("My Orders");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(new Color(50, 50, 70));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Continue Shopping button on right side
        JButton continueShoppingButton = new JButton("Continue Shopping");
        styleButton(continueShoppingButton);
        continueShoppingButton.addActionListener(e -> goToMainPage());
        headerPanel.add(continueShoppingButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Create orders panel
        ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBackground(MAIN_BG_COLOR);
        
        // Create empty orders panel
        createEmptyOrdersPanel();
        
        // Create orders table
        createOrdersTable();
        
        // Add to content panel
        contentPanel.add(ordersPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createEmptyOrdersPanel() {
        emptyOrdersPanel = new JPanel();
        emptyOrdersPanel.setLayout(new BoxLayout(emptyOrdersPanel, BoxLayout.Y_AXIS));
        emptyOrdersPanel.setBackground(MAIN_BG_COLOR);
        emptyOrdersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyOrdersPanel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        JLabel emptyIcon = new JLabel("📦");
        emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 70));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyLabel = new JLabel("No orders yet");
        emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        emptyLabel.setForeground(new Color(100, 100, 100));
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptySubLabel = new JLabel("You haven't placed any orders yet");
        emptySubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptySubLabel.setForeground(new Color(120, 120, 120));
        emptySubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton browseButton = new JButton("Browse Books");
        styleButton(browseButton);
        browseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseButton.setMaximumSize(new Dimension(150, 35));
        browseButton.addActionListener(e -> goToMainPage());
        
        emptyOrdersPanel.add(Box.createVerticalGlue());
        emptyOrdersPanel.add(emptyIcon);
        emptyOrdersPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        emptyOrdersPanel.add(emptyLabel);
        emptyOrdersPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        emptyOrdersPanel.add(emptySubLabel);
        emptyOrdersPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        emptyOrdersPanel.add(browseButton);
        emptyOrdersPanel.add(Box.createVerticalGlue());
    }
    
    private void createOrdersTable() {
        // Create table model with columns
        String[] columnNames = {"Order ID", "Order Date", "Total Amount", "Status", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setRowHeight(50);
        ordersTable.setShowGrid(false);
        ordersTable.setIntercellSpacing(new Dimension(0, 10));
        ordersTable.setFocusable(false);
        ordersTable.setBackground(CARD_BG_COLOR);
        ordersTable.setSelectionBackground(new Color(240, 240, 245));
        
        // Set custom renderers
        ordersTable.setDefaultRenderer(Object.class, new OrdersTableCellRenderer());
        
        // Set column widths
        TableColumnModel columnModel = ordersTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Order ID
        columnModel.getColumn(1).setPreferredWidth(150); // Order Date
        columnModel.getColumn(2).setPreferredWidth(120); // Total Amount
        columnModel.getColumn(3).setPreferredWidth(100); // Status
        columnModel.getColumn(4).setPreferredWidth(100); // Actions
        
        // Add the view details button renderer
        columnModel.getColumn(4).setCellRenderer(new ButtonRenderer("View Details"));
        ordersTable.addMouseListener(new OrdersTableClickListener());
        
        // Create scroll pane
        scrollPane = new JScrollPane(ordersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(MAIN_BG_COLOR);
        
        ordersPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadOrders() {
        // Clear existing table data
        tableModel.setRowCount(0);
        
        try {
            // Query to get orders
            String query = "SELECT id, order_date, total_amount, status FROM orders WHERE user_id = ? ORDER BY order_date DESC";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            boolean hasOrders = false;
            
            while (rs.next()) {
                hasOrders = true;
                
                int orderId = rs.getInt("id");
                Timestamp orderDate = rs.getTimestamp("order_date");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("status");
                
                // Format date and amount for display
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                String formattedDate = dateFormat.format(orderDate);
                
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                String formattedTotal = currencyFormat.format(totalAmount);
                
                // Add row to table model
                tableModel.addRow(new Object[]{
                    "#" + orderId,
                    formattedDate,
                    formattedTotal,
                    status,
                    "View Details" // Action button
                });
            }
            
            rs.close();
            stmt.close();
            
            // Show appropriate view based on whether there are orders
            if (hasOrders) {
                ordersPanel.removeAll();
                ordersPanel.add(scrollPane, BorderLayout.CENTER);
            } else {
                ordersPanel.removeAll();
                ordersPanel.add(emptyOrdersPanel, BorderLayout.CENTER);
            }
            
            ordersPanel.revalidate();
            ordersPanel.repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void showOrderDetails(int orderId) {
        try {
            // Get order information
            String orderQuery = "SELECT o.id, o.order_date, o.total_amount, o.status, o.shipping_address, o.payment_method " +
                              "FROM orders o WHERE o.id = ? AND o.user_id = ?";
            PreparedStatement orderStmt = connection.prepareStatement(orderQuery);
            orderStmt.setInt(1, orderId);
            orderStmt.setInt(2, currentUserId);
            ResultSet orderRs = orderStmt.executeQuery();
            
            if (orderRs.next()) {
                // Get order details
                Timestamp orderDate = orderRs.getTimestamp("order_date");
                double totalAmount = orderRs.getDouble("total_amount");
                String status = orderRs.getString("status");
                String shippingAddress = orderRs.getString("shipping_address");
                String paymentMethod = orderRs.getString("payment_method");
                
                // Format date and currency
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss");
                String formattedDate = dateFormat.format(orderDate);
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                
                // Create dialog for order details
                orderDetailsDialog = new JDialog(this, "Order #" + orderId + " Details", true);
                orderDetailsDialog.setSize(800, 600);
                orderDetailsDialog.setLocationRelativeTo(this);
                orderDetailsDialog.setLayout(new BorderLayout());
                
                // Create main panel
                JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
                mainPanel.setBackground(MAIN_BG_COLOR);
                mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
                
                // Create header panel
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(MAIN_BG_COLOR);
                
                // Order ID and date
                JLabel orderIdLabel = new JLabel("Order #" + orderId);
                orderIdLabel.setFont(HEADER_FONT);
                
                JLabel orderDateLabel = new JLabel("Placed on: " + formattedDate);
                orderDateLabel.setFont(BODY_FONT);
                
                JPanel orderIdPanel = new JPanel(new GridLayout(2, 1));
                orderIdPanel.setBackground(MAIN_BG_COLOR);
                orderIdPanel.add(orderIdLabel);
                orderIdPanel.add(orderDateLabel);
                
                // Status panel on the right
                JPanel statusPanel = new JPanel();
                statusPanel.setBackground(MAIN_BG_COLOR);
                
                JLabel statusLabel = new JLabel("Status: ");
                statusLabel.setFont(TITLE_FONT);
                
                JLabel statusValueLabel = new JLabel(status);
                statusValueLabel.setFont(TITLE_FONT);
                
                // Set status color based on value
                switch (status) {
                    case "Delivered":
                        statusValueLabel.setForeground(new Color(46, 125, 50));
                        break;
                    case "Shipped":
                        statusValueLabel.setForeground(new Color(21, 101, 192));
                        break;
                    case "Processing":
                        statusValueLabel.setForeground(new Color(237, 108, 2));
                        break;
                    case "Pending":
                        statusValueLabel.setForeground(new Color(191, 54, 12));
                        break;
                    case "Cancelled":
                        statusValueLabel.setForeground(new Color(183, 28, 28));
                        break;
                    default:
                        statusValueLabel.setForeground(new Color(66, 66, 66));
                }
                
                statusPanel.add(statusLabel);
                statusPanel.add(statusValueLabel);
                
                headerPanel.add(orderIdPanel, BorderLayout.WEST);
                headerPanel.add(statusPanel, BorderLayout.EAST);
                
                // Add separator
                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(200, 200, 200));
                
                // Create order items table
                String[] columnNames = {"Book", "Price", "Quantity", "Total"};
                orderItemsTableModel = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Make table non-editable
                    }
                };
                
                orderItemsTable = new JTable(orderItemsTableModel);
                orderItemsTable.setRowHeight(30);
                orderItemsTable.setBackground(CARD_BG_COLOR);
                orderItemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                orderItemsTable.getTableHeader().setBackground(new Color(240, 240, 240));
                
                // Set column widths
                TableColumnModel itemsColumnModel = orderItemsTable.getColumnModel();
                itemsColumnModel.getColumn(0).setPreferredWidth(300);  // Book
                itemsColumnModel.getColumn(1).setPreferredWidth(100);  // Price
                itemsColumnModel.getColumn(2).setPreferredWidth(80);   // Quantity
                itemsColumnModel.getColumn(3).setPreferredWidth(100);  // Total
                
                // Create scroll pane for items table
                JScrollPane itemsScrollPane = new JScrollPane(orderItemsTable);
                itemsScrollPane.setBorder(BorderFactory.createEmptyBorder());
                
                // Load order items
                String itemsQuery = "SELECT oi.quantity, oi.price, b.title, b.author " +
                                  "FROM order_items oi " +
                                  "JOIN books b ON oi.book_id = b.id " +
                                  "WHERE oi.order_id = ?";
                PreparedStatement itemsStmt = connection.prepareStatement(itemsQuery);
                itemsStmt.setInt(1, orderId);
                ResultSet itemsRs = itemsStmt.executeQuery();
                
                while (itemsRs.next()) {
                    String title = itemsRs.getString("title");
                    String author = itemsRs.getString("author");
                    double price = itemsRs.getDouble("price");
                    int quantity = itemsRs.getInt("quantity");
                    double total = price * quantity;
                    
                    // Format for display
                    String formattedPrice = currencyFormat.format(price);
                    String formattedItemTotal = currencyFormat.format(total);
                    
                    // Add to table
                    orderItemsTableModel.addRow(new Object[]{
                        title + " by " + author,
                        formattedPrice,
                        quantity,
                        formattedItemTotal
                    });
                }
                
                // Create order summary panel (bottom right)
                JPanel summaryPanel = new JPanel();
                summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
                summaryPanel.setBackground(CARD_BG_COLOR);
                summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                    new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
                    new EmptyBorder(15, 15, 15, 15)
                ));
                
                JLabel orderSummaryLabel = new JLabel("Order Summary");
                orderSummaryLabel.setFont(TITLE_FONT);
                orderSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JSeparator summaryDivider = new JSeparator();
                summaryDivider.setMaximumSize(new Dimension(260, 1));
                summaryDivider.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel totalLabel = new JLabel("Total Amount: " + currencyFormat.format(totalAmount));
                totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel methodLabel = new JLabel("Payment Method: " + (paymentMethod != null ? paymentMethod : "N/A"));
                methodLabel.setFont(BODY_FONT);
                methodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Address panel
                JPanel addressPanel = new JPanel();
                addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.Y_AXIS));
                addressPanel.setBackground(CARD_BG_COLOR);
                addressPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                addressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel addressLabel = new JLabel("Shipping Address:");
                addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JTextArea addressArea = new JTextArea(shippingAddress != null ? shippingAddress : "No address provided");
                addressArea.setFont(BODY_FONT);
                addressArea.setEditable(false);
                addressArea.setLineWrap(true);
                addressArea.setWrapStyleWord(true);
                addressArea.setBackground(CARD_BG_COLOR);
                addressArea.setBorder(BorderFactory.createEmptyBorder());
                addressArea.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                addressPanel.add(addressLabel);
                addressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                addressPanel.add(addressArea);
                
                // Add components to summary panel
                summaryPanel.add(orderSummaryLabel);
                summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                summaryPanel.add(summaryDivider);
                summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                summaryPanel.add(totalLabel);
                summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                summaryPanel.add(methodLabel);
                summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                summaryPanel.add(addressPanel);
                
                // Bottom panel for summary and close button
                JPanel bottomPanel = new JPanel(new BorderLayout(20, 0));
                bottomPanel.setBackground(MAIN_BG_COLOR);
                
                JButton closeButton = new JButton("Close");
                styleButton(closeButton);
                closeButton.addActionListener(e -> orderDetailsDialog.dispose());
                
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(MAIN_BG_COLOR);
                buttonPanel.add(closeButton);
                
                bottomPanel.add(summaryPanel, BorderLayout.CENTER);
                bottomPanel.add(buttonPanel, BorderLayout.EAST);
                
                // Add all panels to main panel
                mainPanel.add(headerPanel, BorderLayout.NORTH);
                mainPanel.add(separator, BorderLayout.NORTH);
                mainPanel.add(itemsScrollPane, BorderLayout.CENTER);
                mainPanel.add(bottomPanel, BorderLayout.SOUTH);
                
                // Add main panel to dialog
                orderDetailsDialog.add(mainPanel);
                
                // Clean up
                itemsRs.close();
                itemsStmt.close();
                
                // Show dialog
                orderDetailsDialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Order not found or not authorized to view.", 
                                             "Order Error", JOptionPane.ERROR_MESSAGE);
            }
            
            orderRs.close();
            orderStmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading order details: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void goToMainPage() {
        dispose();
        SwingUtilities.invokeLater(() -> new MainPage(currentUserId, roleId));
    }
    
    private void handleMenuItemClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                goToMainPage();
                break;
            case "My Cart":
                dispose();
                SwingUtilities.invokeLater(() -> new CartPage(currentUserId, roleId));
                break;
            case "Wishlist":
                dispose();
                SwingUtilities.invokeLater(() -> new WishlistPage(currentUserId, roleId));
                break;
            case "My Orders":
                // Already on orders page
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
    
    // Custom table cell renderer for orders table
    private class OrdersTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                                                      boolean isSelected, boolean hasFocus, 
                                                      int row, int column) {
            // Get the default component
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            // Configure the cell rendering based on column
            switch (column) {
                case 0: // Order ID
                    label.setHorizontalAlignment(JLabel.LEFT);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    break;
                    
                case 1: // Order Date
                    label.setHorizontalAlignment(JLabel.LEFT);
                    break;
                    
                case 2: // Total Amount
                    label.setHorizontalAlignment(JLabel.RIGHT);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    break;
                    
                case 3: // Status
                    label.setHorizontalAlignment(JLabel.CENTER);
                    
                    // Set status color and style based on value
                    String status = value.toString();
                    switch (status) {
                        case "Delivered":
                            label.setForeground(new Color(46, 125, 50));
                            break;
                        case "Shipped":
                            label.setForeground(new Color(21, 101, 192));
                            break;
                        case "Processing":
                            label.setForeground(new Color(237, 108, 2));
                            break;
                        case "Pending":
                            label.setForeground(new Color(191, 54, 12));
                            break;
                        case "Cancelled":
                            label.setForeground(new Color(183, 28, 28));
                            break;
                        default:
                            label.setForeground(new Color(66, 66, 66));
                    }
                    
                    // Create a rounded panel for status
                    JPanel statusPanel = new JPanel();
                    statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                    statusPanel.setBackground(new Color(245, 245, 245));
                    
                    // Add status label to panel
                    JLabel statusLabel = new JLabel(status);
                    statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    statusLabel.setForeground(label.getForeground());
                    statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
                    
                    statusPanel.add(statusLabel);
                    
                    if (isSelected) {
                        statusPanel.setBackground(table.getSelectionBackground());
                    }
                    
                    return statusPanel;
                    
                case 4: // Actions 
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
    
            private class ButtonRenderer extends JButton implements TableCellRenderer {
                public ButtonRenderer(String text) {
                    setText(text);
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    setOpaque(true);
                    setBorderPainted(false);
                    setBackground(new Color(39, 55, 77));
                    setForeground(Color.WHITE);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                              boolean isSelected, boolean hasFocus,
                                                              int row, int column) {
                    if (isSelected) {
                        setBackground(new Color(59, 75, 97));
                    } else {
                        setBackground(new Color(39, 55, 77));
                    }
                    return this;
                }
            }

            // Mouse listener for table click events
            private class OrdersTableClickListener extends MouseAdapter {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = ordersTable.getColumnModel().getColumnIndexAtX(e.getX());
                    int row = e.getY() / ordersTable.getRowHeight();

                    // Check if we clicked on the action column
                    if (column == 4 && row < ordersTable.getRowCount() && row >= 0) {
                        // Extract order ID (without # symbol)
                        String orderIdString = ((String) tableModel.getValueAt(row, 0)).substring(1);
                        int orderId = Integer.parseInt(orderIdString);

                        // Show order details
                        showOrderDetails(orderId);
                    }
                }
            }

            // Helper method to style buttons
            private void styleButton(JButton button) {
                button.setBackground(new Color(39, 55, 77));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setFont(new Font("Segoe UI", Font.BOLD, 12));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setPreferredSize(new Dimension(150, 35));

                // Add hover effect
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setBackground(new Color(59, 75, 97));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setBackground(new Color(39, 55, 77));
                    }
                });
            }

        public static void main(String[] args) {
                try {
                    // Set system look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Create and show the orders page
                SwingUtilities.invokeLater(() -> {
                    // For testing, use user ID 1 and role ID 1 (member)
                    new OrdersPage(1, 1);
                });
        }
    }

