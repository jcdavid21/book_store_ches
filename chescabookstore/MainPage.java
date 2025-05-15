package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.NumberFormat;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainPage extends JFrame {

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
    private JPanel booksPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> categoryComboBox;
    private JTextField searchField;
    private JButton searchButton;
    private JLabel headerLabel;
    
    // Search and category tracking
    private String currentCategory = "All";
    private String currentSearchQuery = "";

    // Add a window resize listener to adjust the book card layout
    public MainPage(int userId, int roleId) {
        if (userId == 0) {
            JOptionPane.showMessageDialog(this, "You need to log in first", 
                                         "Validation Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm();
            return;
        }
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
        setTitle("Libroloco - Online Bookstore");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set up the main layout
        setLayout(new BorderLayout());

        // Create and add sidebar
        createSidebar();

        // Create and add content area
        createContentArea();

        // Load books initially
        loadBooks(currentCategory, currentSearchQuery);

        // Add window resize listener to adjust layout
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Reload books to adjust card sizes
                loadBooks(currentCategory, currentSearchQuery);
            }
        });

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

        // Menu items - Removed "Categories" from the sidebar since they'll be in the header
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
        panel.setBackground(SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 40));

        JLabel label = new JLabel(text);
        label.setFont(MENU_FONT);
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(HIGHLIGHT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(SIDEBAR_COLOR);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMenuItemClick(text);
            }
        });

        return panel;
    }

    private JPanel createSubMenuItemPanel(String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 40, 8));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(200, 35));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_COLOR);

        panel.add(label);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(HIGHLIGHT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(SIDEBAR_COLOR);
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
        headerLabel = new JLabel("Discover Books");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(new Color(50, 50, 70));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Right side with categories and search
        JPanel searchAndCategoriesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchAndCategoriesPanel.setBackground(MAIN_BG_COLOR);

        // Categories dropdown
        JLabel categoryLabel = new JLabel("Categories:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] categories = {"All", "Fiction", "Non-Fiction", "Science", "History", "Biography", "Mystery", "Fantasy", "Romance"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setPreferredSize(new Dimension(120, 30));
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Style the combobox
        categoryComboBox.setBorder(BorderFactory.createLineBorder(new Color(39, 55, 77), 1));
        categoryComboBox.setBackground(Color.WHITE);
        
        // Add action listener to the combobox
        categoryComboBox.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            currentCategory = selectedCategory;
            loadBooks(currentCategory, currentSearchQuery);
        });

        // Search components
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 30));
        
        // Add action listener to search field for Enter key
        searchField.addActionListener(e -> performSearch());
        
        searchButton = new JButton("Search");
        styleButton(searchButton);
        searchButton.setPreferredSize(new Dimension(80, 30));
        
        // Add action listener to search button
        searchButton.addActionListener(e -> performSearch());

        // Add components to search panel
        searchAndCategoriesPanel.add(categoryLabel);
        searchAndCategoriesPanel.add(categoryComboBox);
        searchAndCategoriesPanel.add(Box.createRigidArea(new Dimension(15, 0))); // spacing
        searchAndCategoriesPanel.add(searchField);
        searchAndCategoriesPanel.add(searchButton);

        headerPanel.add(searchAndCategoriesPanel, BorderLayout.EAST);

        // Use GridLayout instead of WrapLayout to ensure 3 columns per row
        booksPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        booksPanel.setBackground(MAIN_BG_COLOR);

        // Scroll pane for books
        scrollPane = new JScrollPane(booksPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Customize scroll bar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 195);
                this.trackColor = MAIN_BG_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }
    
    // Method to handle the search button click and Enter key in search field
    private void performSearch() {
        String query = searchField.getText().trim();
        currentSearchQuery = query;
        loadBooks(currentCategory, currentSearchQuery);
        
        // Update header label to indicate search results if there's a search query
        if (!currentSearchQuery.isEmpty()) {
            headerLabel.setText("Search Results for \"" + currentSearchQuery + "\"");
        } else {
            headerLabel.setText("Discover Books");
        }
    }

    private void loadBooks(String category, String searchQuery) {
        booksPanel.removeAll();

        try {
            String query;
            PreparedStatement statement;

            // Build the query based on category and search parameters
            if (searchQuery.isEmpty()) {
                // No search query, filter by category only
                if (category.equals("All")) {
                    query = "SELECT * FROM books ORDER BY id DESC";
                    statement = connection.prepareStatement(query);
                } else {
                    query = "SELECT * FROM books WHERE category = ? ORDER BY id DESC";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, category);
                }
            } else {
                // Search query exists, search in title, author, and description
                if (category.equals("All")) {
                    query = "SELECT * FROM books WHERE (title LIKE ? OR author LIKE ? OR description LIKE ?) ORDER BY id DESC";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, "%" + searchQuery + "%");
                    statement.setString(2, "%" + searchQuery + "%");
                    statement.setString(3, "%" + searchQuery + "%");
                } else {
                    query = "SELECT * FROM books WHERE category = ? AND (title LIKE ? OR author LIKE ? OR description LIKE ?) ORDER BY id DESC";
                    statement = connection.prepareStatement(query);
                    statement.setString(1, category);
                    statement.setString(2, "%" + searchQuery + "%");
                    statement.setString(3, "%" + searchQuery + "%");
                    statement.setString(4, "%" + searchQuery + "%");
                }
            }

            ResultSet resultSet = statement.executeQuery();
            int bookCount = 0;

            while (resultSet.next()) {
                JPanel bookCard = createBookCard(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getDouble("price"),
                        resultSet.getString("cover_image"),
                        resultSet.getString("category"),
                        resultSet.getString("description")
                );
                booksPanel.add(bookCard);
                bookCount++;
            }
            
            // Show a message if no books were found
            if (bookCount == 0) {
                JPanel noResultsPanel = new JPanel();
                noResultsPanel.setLayout(new BorderLayout());
                noResultsPanel.setBackground(MAIN_BG_COLOR);
                
                JLabel noResultsLabel = new JLabel("No books found matching your search criteria.");
                noResultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
                noResultsLabel.setForeground(new Color(100, 100, 100));
                
                noResultsPanel.add(noResultsLabel, BorderLayout.CENTER);
                booksPanel.add(noResultsPanel);
            }

            resultSet.close();
            statement.close();

            booksPanel.revalidate();
            booksPanel.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to clear search and reset to default view
    private void clearSearch() {
        searchField.setText("");
        currentSearchQuery = "";
        headerLabel.setText("Discover Books");
        loadBooks(currentCategory, "");
    }

    private JPanel createBookCard(int id, String title, String author, double price, String coverImage, String category, String description) {
        // Card panel with rounded corners and shadow
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(CARD_BG_COLOR);

        // Ensure all cards are the same size to maintain the grid
        int cardWidth = (getWidth() - 200 - 60 - 40) / 3; // Main width minus sidebar, padding, and gaps
        cardPanel.setPreferredSize(new Dimension(cardWidth, 420));
        cardPanel.setMaximumSize(new Dimension(cardWidth, 420));
        cardPanel.setBorder(new CompoundBorder(
                new EmptyBorder(5, 5, 5, 5),
                new CompoundBorder(
                        new SoftBevelBorder(SoftBevelBorder.RAISED, new Color(230, 230, 230), new Color(240, 240, 240)),
                        new EmptyBorder(10, 10, 10, 10)
                )
        ));

        // Book cover image container - FIXED HEIGHT TO CONSISTENT 200px
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(150, 200));
        imageContainer.setMaximumSize(new Dimension(150, 200));
        imageContainer.setMinimumSize(new Dimension(150, 200));
        imageContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageContainer.setBackground(new Color(230, 230, 240)); // Gray background for container

        // Book cover image
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                // Load and resize the image - LIMIT HEIGHT TO 200px MAX
                ImageIcon originalIcon = new ImageIcon(getClass().getResource("/assets/" + coverImage));
                Image originalImage = originalIcon.getImage();

                // Calculate scaling to maintain aspect ratio and not exceed 150x200
                int origWidth = originalImage.getWidth(null);
                int origHeight = originalImage.getHeight(null);
                double widthRatio = 150.0 / origWidth;
                double heightRatio = 200.0 / origHeight;
                double ratio = Math.min(widthRatio, heightRatio);

                int scaledWidth = (int) (origWidth * ratio);
                int scaledHeight = (int) (origHeight * ratio);

                // Scale image maintaining aspect ratio
                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon finalIcon = new ImageIcon(scaledImage);

                JLabel coverLabel = new JLabel(finalIcon);
                coverLabel.setHorizontalAlignment(JLabel.CENTER);
                imageContainer.add(coverLabel, BorderLayout.CENTER);
            } catch (Exception e) {
                // If image loading fails, show placeholder
                JLabel noImageLabel = new JLabel("No Image");
                noImageLabel.setForeground(new Color(150, 150, 150));
                noImageLabel.setHorizontalAlignment(JLabel.CENTER);
                imageContainer.add(noImageLabel, BorderLayout.CENTER);
            }
        } else {
            // Default placeholder when no image is available
            JLabel noImageLabel = new JLabel("No Image");
            noImageLabel.setForeground(new Color(150, 150, 150));
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageContainer.add(noImageLabel, BorderLayout.CENTER);
        }

        cardPanel.add(imageContainer);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Book title - centered and wrapped
        JLabel titleLabel = new JLabel("<html><div style='text-align:center;width:100%'>" + title + "</div></html>");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);

        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Author - centered
        JLabel authorLabel = new JLabel("<html><div style='text-align:center'>by " + author + "</div></html>");
        authorLabel.setFont(BODY_FONT);
        authorLabel.setForeground(new Color(100, 100, 100));
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(authorLabel);

        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Category - centered
        JLabel categoryLabel = new JLabel("<html><div style='text-align:center'>" + category + "</div></html>");
        categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        categoryLabel.setForeground(new Color(130, 130, 130));
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(categoryLabel);

        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Price - centered
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        JLabel priceLabel = new JLabel("<html><div style='text-align:center'>" + formatter.format(price) + "</div></html>");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLabel.setForeground(new Color(50, 120, 80));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(priceLabel);

        cardPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Create buttons panel to ensure consistent sizing
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBackground(CARD_BG_COLOR);

        // Create "Add to Cart" button with fixed height
        JButton cartButton = new JButton("Add to Cart");
        styleButton(cartButton);
        cartButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED BUTTON DIMENSIONS
        Dimension buttonDimension = new Dimension(150, 30);
        cartButton.setPreferredSize(buttonDimension);
        cartButton.setMaximumSize(buttonDimension); // Fixed size

        // Add the action listener to handle adding to cart
        final int bookID = id; // Create final variable for use in lambda
        cartButton.addActionListener(e -> addToCart(bookID));

        buttonPanel.add(cartButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Add view button with fixed height
        JButton viewButton = new JButton("View Details");
        styleButton(viewButton);
        viewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewButton.setPreferredSize(buttonDimension);
        viewButton.setMaximumSize(buttonDimension); // Fixed size
        viewButton.addActionListener(e -> showBookDetails(id, title, author, price, coverImage, category, description));

        buttonPanel.add(viewButton);
        cardPanel.add(buttonPanel);

        // Add click listener to view book details for the whole card
        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBookDetails(id, title, author, price, coverImage, category, description);
            }
        });

        return cardPanel;
    }

    private void styleButton(JButton button) {
        // Set basic button properties
        button.setBackground(new Color(39, 55, 77));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Create rounded border with padding - fixed padding to maintain consistent height
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

    // Custom RoundedBorder class
    private static class RoundedBorder extends AbstractBorder {

        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = radius;
            insets.top = insets.bottom = radius;
            return insets;
        }
    }

    private void showBookDetails(int id, String title, String author, double price, String coverImage, String category, String description) {
        // Open book details page
        BookDetailsPage detailsPage = new BookDetailsPage(connection, currentUserId, id);
        detailsPage.setVisible(true);
    }

    // Update the addToCart method to show a visual confirmation
    private void addToCart(int bookId) {
        try {
            // Check if book is already in cart
            String checkQuery = "SELECT * FROM cart WHERE user_id = ? AND book_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, currentUserId);
            checkStatement.setInt(2, bookId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // Update quantity
                String updateQuery = "UPDATE cart SET quantity = quantity + 1 WHERE user_id = ? AND book_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, currentUserId);
                updateStatement.setInt(2, bookId);
                updateStatement.executeUpdate();
                updateStatement.close();
            } else {
                // Insert new cart item
                String insertQuery = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, 1)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, currentUserId);
                insertStatement.setInt(2, bookId);
                insertStatement.executeUpdate();
                insertStatement.close();
            }

            resultSet.close();
            checkStatement.close();

            // Create a custom success message
            JOptionPane pane = new JOptionPane(
                    "Book added to cart successfully!",
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    null,
                    new Object[]{},
                    null);

            // Create dialog
            JDialog dialog = pane.createDialog(this, "Added to Cart");

            // Auto-close after 1.5 seconds
            Timer timer = new Timer(1500, e -> dialog.dispose());
            timer.setRepeats(false);
            timer.start();

            dialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding book to cart: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleMenuItemClick(String menuItem) {
        switch (menuItem) {
            case "Home":
                // Clear search and reset to home view
                searchField.setText("");
                currentSearchQuery = "";
                headerLabel.setText("Discover Books");
                loadBooks("All", "");
                categoryComboBox.setSelectedItem("All");
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

    // WrapLayout class for flowing grid layout
    public class WrapLayout extends FlowLayout {

        public WrapLayout() {
            super();
        }

        public WrapLayout(int align) {
            super(align);
        }

        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;

                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);

                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }

                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }

                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }

                addRow(dim, rowWidth, rowHeight);

                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);

            if (dim.height > 0) {
                dim.height += getVgap();
            }

            dim.height += rowHeight;
        }
    }
    
    

    public static void main(String[] args) {
        try {
            // Set System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // You need to provide userId and roleId when creating MainPage
            // For example, default values or implement a login system
            final int defaultUserId = 0; // Default user ID
            final int defaultRoleId = 0; // Default role ID (perhaps 1 for regular user)

            SwingUtilities.invokeLater(() -> new MainPage(defaultUserId, defaultRoleId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}