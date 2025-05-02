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

public class WishlistPage extends JFrame {

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
    private JPanel wishlistItemsPanel;
    private JScrollPane scrollPane;
    private JLabel headerLabel;
    private JPanel emptyWishlistPanel;
    
    // Table for wishlist items
    private JTable wishlistTable;
    private DefaultTableModel tableModel;
    
    // Track wishlist items
    private int totalItems = 0;
    private boolean isWishlistEmpty = true;

    public WishlistPage(int userId, int roleId) {
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
        setTitle("Libroloco - Your Wishlist");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        setLayout(new BorderLayout());

        // Create and add sidebar
        createSidebar();

        // Create and add content area
        createContentArea();

        // Load wishlist items
        loadWishlistItems();

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
        panel.setBackground(text.equals("Wishlist") ? HIGHLIGHT_COLOR : SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 40));

        JLabel label = new JLabel(text);
        label.setFont(MENU_FONT);
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!text.equals("Wishlist")) { // Don't change highlight for active menu
                    panel.setBackground(HIGHLIGHT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!text.equals("Wishlist")) { // Don't reset highlight for active menu
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
        headerLabel = new JLabel("My Wishlist");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(new Color(50, 50, 70));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Continue Shopping button on right side
        JButton continueShoppingButton = new JButton("Continue Shopping");
        styleButton(continueShoppingButton);
        continueShoppingButton.addActionListener(e -> goToMainPage());
        headerPanel.add(continueShoppingButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Create main panel for wishlist
        JPanel mainWishlistPanel = new JPanel(new BorderLayout(0, 20));
        mainWishlistPanel.setBackground(MAIN_BG_COLOR);

        // Create wishlist items panel
        wishlistItemsPanel = new JPanel(new BorderLayout());
        wishlistItemsPanel.setBackground(MAIN_BG_COLOR);
        
        // Create empty wishlist panel (will be shown if wishlist is empty)
        createEmptyWishlistPanel();
        
        // Create table for wishlist items
        createWishlistTable();
        
        // Add wishlist items panel to main wishlist panel
        mainWishlistPanel.add(wishlistItemsPanel, BorderLayout.CENTER);
        
        // Add main wishlist panel to content panel
        contentPanel.add(mainWishlistPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createEmptyWishlistPanel() {
        emptyWishlistPanel = new JPanel();
        emptyWishlistPanel.setLayout(new BoxLayout(emptyWishlistPanel, BoxLayout.Y_AXIS));
        emptyWishlistPanel.setBackground(MAIN_BG_COLOR);
        emptyWishlistPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyWishlistPanel.setBorder(new EmptyBorder(50, 0, 0, 0));
        
        JLabel emptyWishlistIcon = new JLabel("♡");
        emptyWishlistIcon.setFont(new Font("Segoe UI", Font.PLAIN, 70));
        emptyWishlistIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyWishlistLabel = new JLabel("Your wishlist is empty");
        emptyWishlistLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        emptyWishlistLabel.setForeground(new Color(100, 100, 100));
        emptyWishlistLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel emptyWishlistSubLabel = new JLabel("Save books you're interested in for later");
        emptyWishlistSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emptyWishlistSubLabel.setForeground(new Color(120, 120, 120));
        emptyWishlistSubLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton browseButton = new JButton("Browse Books");
        styleButton(browseButton);
        browseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseButton.setMaximumSize(new Dimension(150, 35));
        browseButton.addActionListener(e -> goToMainPage());
        
        emptyWishlistPanel.add(Box.createVerticalGlue());
        emptyWishlistPanel.add(emptyWishlistIcon);
        emptyWishlistPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        emptyWishlistPanel.add(emptyWishlistLabel);
        emptyWishlistPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        emptyWishlistPanel.add(emptyWishlistSubLabel);
        emptyWishlistPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        emptyWishlistPanel.add(browseButton);
        emptyWishlistPanel.add(Box.createVerticalGlue());
    }
    
    private void createWishlistTable() {
        // Create table model with columns
        String[] columnNames = {"", "Book", "Price", "Actions", ""};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make no columns editable
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) { // Cover image column
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };
        
        wishlistTable = new JTable(tableModel);
        wishlistTable.setRowHeight(120);
        wishlistTable.setShowGrid(false);
        wishlistTable.setIntercellSpacing(new Dimension(0, 10));
        wishlistTable.setFocusable(false);
        wishlistTable.setBackground(CARD_BG_COLOR);
        wishlistTable.setSelectionBackground(new Color(240, 240, 245));
        
        // Set custom renderers for each column
        wishlistTable.setDefaultRenderer(Object.class, new WishlistTableCellRenderer());
        
        // Set column widths
        TableColumnModel columnModel = wishlistTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // Cover image
        columnModel.getColumn(1).setPreferredWidth(350); // Book title
        columnModel.getColumn(2).setPreferredWidth(100); // Price
        columnModel.getColumn(3).setPreferredWidth(150); // Actions (Add to Cart)
        columnModel.getColumn(4).setPreferredWidth(50);  // Remove button
        
        // Create scroll pane for the wishlist table
        scrollPane = new JScrollPane(wishlistTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(MAIN_BG_COLOR);
        
        // Add custom renderers for the action buttons
        columnModel.getColumn(3).setCellRenderer(new ActionButtonRenderer());
        columnModel.getColumn(4).setCellRenderer(new RemoveButtonRenderer());
        
        // Add mouse listener for button clicks
        wishlistTable.addMouseListener(new TableButtonClickListener());
        
        wishlistItemsPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadWishlistItems() {
        // Clear existing table data
        tableModel.setRowCount(0);
        
        // Reset tracking variables
        totalItems = 0;
        
        try {
            // Query to get wishlist items with book details
            String query = "SELECT w.id, w.book_id, b.title, b.author, b.price, b.cover_image " +
                           "FROM wishlist w " +
                           "JOIN books b ON w.book_id = b.id " +
                           "WHERE w.user_id = ?";
                           
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            isWishlistEmpty = true;
            
            while (rs.next()) {
                isWishlistEmpty = false;
                
                int wishlistId = rs.getInt("id");
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                double price = rs.getDouble("price");
                String coverImage = rs.getString("cover_image");
                
                // Format price for display
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                String formattedPrice = currencyFormat.format(price);
                
                // Create an ImageIcon for the cover image
                ImageIcon coverIcon = createCoverImageIcon(coverImage);
                
                // Add row to table model
                tableModel.addRow(new Object[]{
                    coverIcon,
                    "<html><b>" + title + "</b><br>by " + author + "</html>",
                    formattedPrice,
                    bookId,  // Store book ID in the action button column
                    wishlistId  // Store wishlist ID in the remove button column
                });
                
                // Update tracking variables
                totalItems++;
            }
            
            rs.close();
            stmt.close();
            
            // Update header with count
            updateHeaderWithCount();
            
            // Show either the wishlist table or empty wishlist message
            showAppropriateView();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading wishlist items: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void updateHeaderWithCount() {
        // Update header label with count of items
        if (totalItems > 0) {
            headerLabel.setText("My Wishlist (" + totalItems + ")");
        } else {
            headerLabel.setText("My Wishlist");
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
    
    private void showAppropriateView() {
        wishlistItemsPanel.removeAll();
        
        if (isWishlistEmpty) {
            wishlistItemsPanel.add(emptyWishlistPanel, BorderLayout.CENTER);
        } else {
            wishlistItemsPanel.add(scrollPane, BorderLayout.CENTER);
        }
        
        wishlistItemsPanel.revalidate();
        wishlistItemsPanel.repaint();
    }
    
    private void removeFromWishlist(int row) {
        try {
            // Get wishlist id from table model
            int wishlistId = (int) tableModel.getValueAt(row, 4);
            
            // Delete item from database
            String deleteQuery = "DELETE FROM wishlist WHERE id = ?";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, wishlistId);
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Remove row from table model
            tableModel.removeRow(row);
            
            // Update total items count
            totalItems--;
            updateHeaderWithCount();
            
            // Check if wishlist is now empty
            if (tableModel.getRowCount() == 0) {
                isWishlistEmpty = true;
                showAppropriateView();
            }
            
            // Show success message
            showToastMessage("Item removed from wishlist");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error removing item: " + e.getMessage(),
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addToCart(int row) {
        try {
            // Get book id from table model
            int bookId = (int) tableModel.getValueAt(row, 3);
            
            // Check if item already exists in cart
            String checkQuery = "SELECT id, quantity FROM cart WHERE user_id = ? AND book_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, currentUserId);
            checkStmt.setInt(2, bookId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Item already exists in cart, update quantity
                int cartId = rs.getInt("id");
                int currentQty = rs.getInt("quantity");
                
                String updateQuery = "UPDATE cart SET quantity = ? WHERE id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setInt(1, currentQty + 1);
                updateStmt.setInt(2, cartId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                showToastMessage("Item quantity updated in cart");
            } else {
                // Item doesn't exist in cart, add it
                String insertQuery = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, 1)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setInt(1, currentUserId);
                insertStmt.setInt(2, bookId);
                insertStmt.executeUpdate();
                insertStmt.close();
                
                showToastMessage("Item added to cart");
            }
            
            rs.close();
            checkStmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding item to cart: " + e.getMessage(),
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
    
    private void goToMainPage() {
        // Close this page and open MainPage
        dispose();
        SwingUtilities.invokeLater(() -> new MainPage(currentUserId, roleId));
    }
    
    private void goToCartPage() {
        // Close this page and open CartPage
        dispose();
        SwingUtilities.invokeLater(() -> new CartPage(currentUserId, roleId));
    }
    
    private void handleMenuItemClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                goToMainPage();
                break;
            case "My Cart":
                goToCartPage();
                break;
            case "Wishlist":
                // Already on wishlist page
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
    private class WishlistTableCellRenderer extends DefaultTableCellRenderer {
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
                    // Price is right-aligned
                    label.setHorizontalAlignment(JLabel.RIGHT);
                    break;
                    
                case 3: // Add to cart button column
                case 4: // Remove button column
                    // These columns use custom renderers
                    break;
            }
            
            // Add a separator line at the bottom of each cell except the last row
            if (row < table.getRowCount() - 1) {
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
            }
            
            return label;
        }
    }
    
    // Custom renderer for the "Add to Cart" button column
    private class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            
            setText("Add to Cart");
            setForeground(new Color(39, 55, 77));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(39, 55, 77), 1, 8),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)
            ));
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
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
    
    // Custom renderer for the remove button column
    private class RemoveButtonRenderer extends JButton implements TableCellRenderer {
        private final Icon removeIcon;
        
        public RemoveButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            
            // Create a custom remove icon (X) instead of loading from file
            removeIcon = createRemoveIcon();
            setIcon(removeIcon);
            
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Remove from wishlist");
        }
        
        private Icon createRemoveIcon() {
            // Create a custom "X" icon
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Use a soft red color for the X
                    // Use a soft red color for the X
                    g2.setColor(new Color(210, 80, 80));
                    
                    // Draw the X lines
                    g2.setStroke(new BasicStroke(2));
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
    
    // Mouse listener to handle button clicks in the table
    private class TableButtonClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int column = wishlistTable.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / wishlistTable.getRowHeight();
            
            // Check if row and column are valid
            if (row < wishlistTable.getRowCount() && row >= 0 && 
                column < wishlistTable.getColumnCount() && column >= 0) {
                
                // Handle action button column (Add to Cart)
                if (column == 3) {
                    addToCart(row);
                }
                
                // Handle remove button column
                else if (column == 4) {
                    removeFromWishlist(row);
                }
            }
        }
    }
    
    // Custom border for rounded buttons
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
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Create and show the application
            SwingUtilities.invokeLater(() -> {
                // For testing: Use user ID 1 and role ID 2 (regular user)
                new WishlistPage(1, 2);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}