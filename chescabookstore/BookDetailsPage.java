package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.text.SimpleDateFormat;

public class BookDetailsPage extends JFrame {

    // Colors
    private static final Color BG_COLOR = new Color(250, 250, 252);
    private static final Color ACCENT_COLOR = new Color(39, 55, 77);
    private static final Color TEXT_COLOR = new Color(50, 50, 70);
    private static final Color LIGHT_TEXT_COLOR = new Color(100, 100, 120);

    // Database connection
    private Connection connection;
    private int userId;
    private int bookId;

    // Book data
    private String title;
    private String author;
    private double price;
    private String description;
    private String coverImage;
    private String isbn;
    private String publisher;
    private java.util.Date publicationDate; // Fully qualified name
    private int stockQuantity;

    public BookDetailsPage(Connection connection, int userId, int bookId) {
        this.connection = connection;
        this.userId = userId;
        this.bookId = bookId;

        setTitle("Book Details");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load book data
        loadBookData();

        // Set up UI
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // Create content
        createContent();
    }

    private void loadBookData() {
        try {
            String query = "SELECT * FROM books WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, bookId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                title = resultSet.getString("title");
                author = resultSet.getString("author");
                price = resultSet.getDouble("price");
                description = resultSet.getString("description");
                coverImage = resultSet.getString("cover_image");
                isbn = resultSet.getString("isbn");
                publisher = resultSet.getString("publisher");
                publicationDate = resultSet.getDate("publication_date");
                stockQuantity = resultSet.getInt("stock_quantity");
            }

            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading book details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void createContent() {
        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Left panel for image
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BG_COLOR);

        // Book cover image
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(250, 350));
        imageContainer.setMaximumSize(new Dimension(250, 350));
        imageContainer.setMinimumSize(new Dimension(250, 350));
        imageContainer.setBackground(new Color(230, 230, 240)); // Gray background for container
        leftPanel.add(imageContainer, BorderLayout.CENTER);

        // Book cover image
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                // Load and resize the image - LIMIT HEIGHT TO 350px MAX
                ImageIcon originalIcon = new ImageIcon(getClass().getResource("/assets/" + coverImage));
                Image originalImage = originalIcon.getImage();

                // Calculate scaling to maintain aspect ratio and not exceed 250x350
                int origWidth = originalImage.getWidth(null);
                int origHeight = originalImage.getHeight(null);
                double widthRatio = 250.0 / origWidth;
                double heightRatio = 350.0 / origHeight;
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

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton addToCartButton = new JButton("Add to Cart");
        JButton addToWishlistButton = new JButton("Add to Wishlist");
        JButton backButton = new JButton("Back to Books");

        styleButton(addToCartButton);
        styleButton(addToWishlistButton);
        styleButton(backButton);

        buttonPanel.add(addToCartButton);
        buttonPanel.add(addToWishlistButton);
        buttonPanel.add(backButton);

        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Right panel for details
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BG_COLOR);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Author
        JLabel authorLabel = new JLabel("by " + author);
        authorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        authorLabel.setForeground(LIGHT_TEXT_COLOR);
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Price
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        JLabel priceLabel = new JLabel(formatter.format(price));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(new Color(50, 120, 80));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Stock status
        JLabel stockLabel = new JLabel(stockQuantity > 0 ? "In Stock (" + stockQuantity + " available)" : "Out of Stock");
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        stockLabel.setForeground(stockQuantity > 0 ? new Color(50, 120, 80) : Color.RED);
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description header
        JLabel descriptionHeader = new JLabel("Description");
        descriptionHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        descriptionHeader.setForeground(TEXT_COLOR);
        descriptionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description text
        JTextArea descriptionArea = new JTextArea(description != null ? description : "No description available.");
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(BG_COLOR);
        descriptionArea.setForeground(TEXT_COLOR);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Details panel
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        detailsPanel.setBackground(BG_COLOR);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Format publication date
        String formattedDate = "";
        if (publicationDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
            formattedDate = dateFormat.format(publicationDate);
        }

        // Add detail fields
        addDetailField(detailsPanel, "ISBN:", isbn != null ? isbn : "N/A");
        addDetailField(detailsPanel, "Publisher:", publisher != null ? publisher : "N/A");
        addDetailField(detailsPanel, "Publication Date:", formattedDate.isEmpty() ? "N/A" : formattedDate);
        addDetailField(detailsPanel, "Format:", "Paperback"); // This could be fetched from database in a real app

        // Add components to right panel with spacing
        rightPanel.add(titleLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(authorLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(priceLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(stockLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        rightPanel.add(descriptionHeader);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(descriptionArea);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(detailsPanel);

        // Add panels to main panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);

        // Set up listeners for buttons
        addToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToCart();
            }
        });

        addToWishlistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToWishlist();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                // Assuming BooksListPage is another frame class in your application
//                new BooksListPage(connection, userId).setVisible(true);
            }
        });
    }

    private void addDetailField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(TEXT_COLOR);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComponent.setForeground(LIGHT_TEXT_COLOR);

        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Make sure the background is painted properly with rounded corners
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(45, 65, 92)); // Slightly lighter on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
    }

    private void addToCart() {
        try {
            // Check if book is already in cart
            String checkQuery = "SELECT * FROM cart WHERE user_id = ? AND book_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, userId);
            checkStatement.setInt(2, bookId);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                // Book already in cart, update quantity
                int currentQuantity = resultSet.getInt("quantity");

                String updateQuery = "UPDATE cart SET quantity = ? WHERE user_id = ? AND book_id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                updateStatement.setInt(1, currentQuantity + 1);
                updateStatement.setInt(2, userId);
                updateStatement.setInt(3, bookId);

                updateStatement.executeUpdate();
                updateStatement.close();

                JOptionPane.showMessageDialog(this, "Item quantity updated in cart!", "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Book not in cart, add it
                String insertQuery = "INSERT INTO cart (user_id, book_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, userId);
                insertStatement.setInt(2, bookId);
                insertStatement.setInt(3, 1);

                insertStatement.executeUpdate();
                insertStatement.close();

                JOptionPane.showMessageDialog(this, "Item added to cart!", "Cart Updated", JOptionPane.INFORMATION_MESSAGE);
            }

            resultSet.close();
            checkStatement.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding to cart: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addToWishlist() {
        try {
            // Check if book is already in wishlist
            String checkQuery = "SELECT * FROM wishlist WHERE user_id = ? AND book_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
            checkStatement.setInt(1, userId);
            checkStatement.setInt(2, bookId);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                JOptionPane.showMessageDialog(this, "This book is already in your wishlist!", "Wishlist", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Book not in wishlist, add it
                String insertQuery = "INSERT INTO wishlist (user_id, book_id, date_added) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setInt(1, userId);
                insertStatement.setInt(2, bookId);
                insertStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));

                insertStatement.executeUpdate();
                insertStatement.close();

                JOptionPane.showMessageDialog(this, "Item added to wishlist!", "Wishlist Updated", JOptionPane.INFORMATION_MESSAGE);
            }

            resultSet.close();
            checkStatement.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding to wishlist: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        // This would be for testing purposes only
        try {
            // Create a test connection
            Connection testConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore", "username", "password");

            // Create and show the frame
            SwingUtilities.invokeLater(() -> {
                new BookDetailsPage(testConnection, 1, 1).setVisible(true);
            });

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }
}
