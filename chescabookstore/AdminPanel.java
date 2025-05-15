package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;

public class AdminPanel extends JFrame {
    // Colors
    private static final Color SIDEBAR_COLOR = new Color(39, 55, 77);
    private static final Color HIGHLIGHT_COLOR = new Color(157, 178, 191);
    private static final Color TEXT_COLOR = new Color(221, 230, 237);
    private static final Color CONTENT_BG = new Color(240, 244, 248);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color HEADING_COLOR = new Color(52, 73, 94);
    
    // Font
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font CARD_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private int userId;
    
    // Components
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel dashboardPanel;
    private JPanel booksPanel;
    private JPanel usersPanel;
    private JPanel ordersPanel;
    private JPanel reportsPanel;
    
    // Current active panel for tracking
    private JPanel currentPanel;
    
    // Menu buttons
    private JButton dashboardBtn;
    private JButton booksBtn;
    private JButton usersBtn;
    private JButton ordersBtn;
    private JButton reportsBtn;
    private JButton logoutBtn;
    
    public AdminPanel(int userId, int roleId) {
        this.userId = userId;
        
        
        setTitle("Book Store Admin Panel");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        if (userId == 0 || roleId != 2) {
            JOptionPane.showMessageDialog(this, "You must login first", 
                                         "Authentication Required", JOptionPane.WARNING_MESSAGE);
            dispose();
            new LoginForm();
            return; // Important: stop initialization if authentication fails
        }
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Create sidebar and content panels
        createSidebar();
        createContentPanel();
        
        // Initialize all content panels
        initializeDashboard();
        initializeBooksPanel();
        initializeUsersPanel();
        initializeOrdersPanel();
        initializeReportsPanel();
        
        // Show dashboard by default
        showPanel(dashboardPanel);
        
        setVisible(true);
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(220, getHeight()));

        // Logo/Title Panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(SIDEBAR_COLOR);
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel titleLabel = new JLabel("BookStore Admin");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        logoPanel.add(titleLabel);

        // Add menu buttons
        dashboardBtn = createMenuButton("Dashboard", "/assets/dashboard.png");
        booksBtn = createMenuButton("Books", "/assets/book.png");
        usersBtn = createMenuButton("Users", "/assets/user.png");
        ordersBtn = createMenuButton("Orders", "/assets/clipboard.png");
//        reportsBtn = createMenuButton("Reports", "/assets/report.png");
        logoutBtn = createMenuButton("Logout", "/assets/logout.png");

        // Add components to sidebar
        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Create centered button containers
        sidebarPanel.add(createCenteredButtonPanel(dashboardBtn));
        sidebarPanel.add(createCenteredButtonPanel(booksBtn));
        sidebarPanel.add(createCenteredButtonPanel(usersBtn));
        sidebarPanel.add(createCenteredButtonPanel(ordersBtn));
//        sidebarPanel.add(createCenteredButtonPanel(reportsBtn));

        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(createCenteredButtonPanel(logoutBtn));
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        add(sidebarPanel, BorderLayout.WEST);
    }

    // Helper method to center buttons in the sidebar
    private JPanel createCenteredButtonPanel(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setMaximumSize(new Dimension(220, 50));
        panel.add(button);
        return panel;
    }

    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(MENU_FONT);
        button.setForeground(TEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT); // Center text
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Adjusted size
        button.setPreferredSize(new Dimension(180, 40));
        button.setMaximumSize(new Dimension(180, 40));

        // Try to load icon if available
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
            button.setIconTextGap(10);
        } catch (Exception e) {
            // If icon not found, just use text
            System.out.println("Icon not found: " + iconPath);
        }

        // Add padding - even on both sides for center alignment
        button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HIGHLIGHT_COLOR);
                button.setForeground(SIDEBAR_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActiveButton(button)) {
                    button.setBackground(SIDEBAR_COLOR);
                    button.setForeground(TEXT_COLOR);
                }
            }
        });

        // Add action listener
        button.addActionListener(e -> {
            resetButtonStyles();

            // Set active style
            button.setBackground(HIGHLIGHT_COLOR);
            button.setForeground(SIDEBAR_COLOR);

            // Show appropriate panel
            if (button == dashboardBtn) {
                showPanel(dashboardPanel);
            } else if (button == booksBtn) {
                showPanel(booksPanel);
            } else if (button == usersBtn) {
                showPanel(usersPanel);
            } else if (button == ordersBtn) {
                showPanel(ordersPanel);
            } else if (button == reportsBtn) {
                showPanel(reportsPanel);
            } else if (button == logoutBtn) {
                handleLogout();
            }
        });

        return button;
    }

    private boolean isActiveButton(JButton button) {
        return button.getBackground().equals(HIGHLIGHT_COLOR);
    }

    private void resetButtonStyles() {
        for (JButton button : new JButton[]{dashboardBtn, booksBtn, usersBtn, ordersBtn, logoutBtn}) {
            button.setBackground(SIDEBAR_COLOR);
            button.setForeground(TEXT_COLOR);
        }
    }

    private void createContentPanel() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(CONTENT_BG);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showPanel(JPanel panel) {
        // Hide all panels
        if (currentPanel != null) {
            currentPanel.setVisible(false);
        }

        // Show selected panel
        panel.setVisible(true);
        currentPanel = panel;

        // Replace current content with the new panel
        contentPanel.removeAll();
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm();
        }
    }
    
    // DASHBOARD PANEL
    private void initializeDashboard() {
        dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        dashboardPanel.setBackground(CONTENT_BG);
        
        // Header panel
        JPanel headerPanel = createHeaderPanel("Dashboard");
        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content panel (uses GridBagLayout for flexibility)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(CONTENT_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Add stat cards
        JPanel statsPanel = createStatsPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 20, 20, 20);
        mainPanel.add(statsPanel, gbc);
        
        // Add recent orders panel
        JPanel recentOrdersPanel = createRecentOrdersPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(recentOrdersPanel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(dashboardPanel);
    }
    
    private JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CONTENT_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(HEADING_COLOR);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(CONTENT_BG);
        
        // Current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String currentDate = dateFormat.format(new java.util.Date());
        JLabel dateLabel = new JLabel(currentDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        rightPanel.add(dateLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(CONTENT_BG);
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Total Books
            String bookQuery = "SELECT COUNT(*) FROM books";
            ResultSet bookRs = stmt.executeQuery(bookQuery);
            int totalBooks = 0;
            if (bookRs.next()) {
                totalBooks = bookRs.getInt(1);
            }
            
            // Total Users
            String userQuery = "SELECT COUNT(*) FROM users WHERE role_id = 1";
            ResultSet userRs = stmt.executeQuery(userQuery);
            int totalUsers = 0;
            if (userRs.next()) {
                totalUsers = userRs.getInt(1);
            }
            
            // Total Orders
            String orderQuery = "SELECT COUNT(*) FROM orders";
            ResultSet orderRs = stmt.executeQuery(orderQuery);
            int totalOrders = 0;
            if (orderRs.next()) {
                totalOrders = orderRs.getInt(1);
            }
            
            // Total Revenue
            String revenueQuery = "SELECT SUM(total_amount) FROM orders";
            ResultSet revenueRs = stmt.executeQuery(revenueQuery);
            double totalRevenue = 0;
            if (revenueRs.next() && revenueRs.getObject(1) != null) {
                totalRevenue = revenueRs.getDouble(1);
            }
            
            // Create cards
            statsPanel.add(createStatCard("Total Books", String.valueOf(totalBooks), new Color(41, 128, 185)));
            statsPanel.add(createStatCard("Total Users", String.valueOf(totalUsers), new Color(46, 204, 113)));
            statsPanel.add(createStatCard("Total Orders", String.valueOf(totalOrders), new Color(155, 89, 182)));
            statsPanel.add(createStatCard("Total Revenue", String.format("$%.2f", totalRevenue), new Color(230, 126, 34)));
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + e.getMessage());
            
            // Create default cards in case of error
            statsPanel.add(createStatCard("Total Books", "0", new Color(41, 128, 185)));
            statsPanel.add(createStatCard("Total Users", "0", new Color(46, 204, 113)));
            statsPanel.add(createStatCard("Total Orders", "0", new Color(155, 89, 182)));
            statsPanel.add(createStatCard("Total Revenue", "$0.00", new Color(230, 126, 34)));
        }
        
        return statsPanel;
    }
    
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Add colored strip at the top
        JPanel colorStrip = new JPanel();
        colorStrip.setBackground(accentColor);
        colorStrip.setPreferredSize(new Dimension(0, 5));
        colorStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE_FONT);
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(CARD_VALUE_FONT);
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(colorStrip);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createRecentOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("Recent Orders");
        titleLabel.setFont(CARD_TITLE_FONT);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Table model
        String[] columns = {"Order ID", "User", "Date", "Amount", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate with data
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT o.id, u.username, o.order_date, o.total_amount, o.status " +
                     "FROM orders o JOIN users u ON o.user_id = u.id " +
                     "ORDER BY o.order_date DESC LIMIT 10")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date")),
                    String.format("$%.2f", rs.getDouble("total_amount")),
                    rs.getString("status")
                };
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading recent orders: " + e.getMessage());
        }
        
        // Create table
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // View all button
        JButton viewAllBtn = new JButton("View All Orders");
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> showPanel(ordersPanel));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(viewAllBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // OTHER PANELS INITIALIZATION (placeholders for now)
    private void initializeBooksPanel() {
        booksPanel = new JPanel(new BorderLayout());
        booksPanel.setBackground(CONTENT_BG);
        
        JPanel headerPanel = createHeaderPanel("Books Management");
        booksPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CONTENT_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create table
        String[] columns = {"ID", "Title", "Author", "Category", "Price", "Stock"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate with data
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, author, category, price, stock_quantity FROM books")) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    String.format("$%.2f", rs.getDouble("price")),
                    rs.getInt("stock_quantity")
                };
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        booksPanel.add(mainPanel, BorderLayout.CENTER);
        
        // Control panel with buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(CONTENT_BG);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JButton addBtn = new JButton("Add Book");
        JButton editBtn = new JButton("Edit Book");
        JButton deleteBtn = new JButton("Delete Book");
        
        controlPanel.add(addBtn);
        controlPanel.add(editBtn);
        controlPanel.add(deleteBtn);
        
        booksPanel.add(controlPanel, BorderLayout.SOUTH);
        
        contentPanel.add(booksPanel);
        
        addBtn.addActionListener(e -> showAddBookDialog());
        
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to edit.", 
                                             "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int bookId = (int) table.getValueAt(selectedRow, 0);
            showEditBookDialog(bookId);
        });
        
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to delete.", 
                                             "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int bookId = (int) table.getValueAt(selectedRow, 0);
            deleteBook(bookId);
        });
    }
    
    private void initializeUsersPanel() {
        usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBackground(CONTENT_BG);
        
        JPanel headerPanel = createHeaderPanel("Users Management");
        usersPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(CONTENT_BG);
        placeholderPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create user table
        String[] columns = {"ID", "Username", "Email", "Name", "Role", "Registration Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate with data
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.id, u.username, u.email, u.first_name, u.last_name, r.role_name, u.registration_date " +
                     "FROM users u JOIN roles r ON u.role_id = r.role_id")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("role_name"),
                    new SimpleDateFormat("yyyy-MM-dd").format(rs.getTimestamp("registration_date"))
                };
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        placeholderPanel.add(scrollPane, BorderLayout.CENTER);
        usersPanel.add(placeholderPanel, BorderLayout.CENTER);
        
        contentPanel.add(usersPanel);
    }
    
    private void initializeOrdersPanel() {
        ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBackground(CONTENT_BG);
        
        JPanel headerPanel = createHeaderPanel("Orders Management");
        ordersPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(CONTENT_BG);
        placeholderPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create order table
        String[] columns = {"Order ID", "User", "Date", "Amount", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate with data
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT o.id, u.username, o.order_date, o.total_amount, o.status " +
                     "FROM orders o JOIN users u ON o.user_id = u.id " +
                     "ORDER BY o.order_date DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date")),
                    String.format("$%.2f", rs.getDouble("total_amount")),
                    rs.getString("status"),
                    "View Details"
                };
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Add button renderer for the action column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.addMouseListener(new JTableButtonMouseListener(table));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        placeholderPanel.add(scrollPane, BorderLayout.CENTER);
        ordersPanel.add(placeholderPanel, BorderLayout.CENTER);
        
        contentPanel.add(ordersPanel);
    }
    
    private void initializeReportsPanel() {
        reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBackground(CONTENT_BG);
        
        JPanel headerPanel = createHeaderPanel("Reports & Analytics");
        reportsPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(CONTENT_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add report options
        String[] reportTypes = {
            "Sales Report",
            "Inventory Report",
            "User Activity Report",
            "Popular Books Report"
        };
        
        JPanel reportButtonsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        reportButtonsPanel.setBackground(CONTENT_BG);
        reportButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportButtonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        for (String reportType : reportTypes) {
            JPanel reportCard = new JPanel();
            reportCard.setLayout(new BorderLayout());
            reportCard.setBackground(CARD_BG);
            reportCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            
            JLabel reportLabel = new JLabel(reportType);
            reportLabel.setFont(CARD_TITLE_FONT);
            reportLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JButton generateBtn = new JButton("Generate");
            generateBtn.setFocusPainted(false);
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(CARD_BG);
            btnPanel.add(generateBtn);
            
            reportCard.add(reportLabel, BorderLayout.CENTER);
            reportCard.add(btnPanel, BorderLayout.SOUTH);
            
            reportButtonsPanel.add(reportCard);
        }
        
        mainPanel.add(reportButtonsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Sample chart panel (placeholder)
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        chartPanel.setBackground(CARD_BG);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        chartPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        JLabel chartTitle = new JLabel("Monthly Sales Report");
        chartTitle.setFont(CARD_TITLE_FONT);
        
        // Sample chart placeholder (would be an actual chart in production)
        JPanel chartPlaceholder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Set rendering hints for better quality
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int padding = 40;
                
                // Draw axis
                g2d.setColor(Color.BLACK);
                g2d.drawLine(padding, height - padding, width - padding, height - padding); // X-axis
                g2d.drawLine(padding, padding, padding, height - padding); // Y-axis
                
                // Get monthly sales data from database
                Map<String, Double> monthlySales = getMonthlyOrderTotals();
                
                if (!monthlySales.isEmpty()) {
                    // Draw bar chart
                    int barWidth = (width - 2 * padding) / monthlySales.size() - 10;
                    int x = padding + 10;
                    double maxSale = monthlySales.values().stream().mapToDouble(v -> v).max().orElse(1000.0);
                    
                    int i = 0;
                    for (Map.Entry<String, Double> entry : monthlySales.entrySet()) {
                        int barHeight = (int) ((entry.getValue() / maxSale) * (height - 2 * padding));
                        
                        // Draw bar
                        g2d.setColor(new Color(41, 128, 185, 180));
                        g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
                        g2d.setColor(new Color(41, 128, 185));
                        g2d.drawRect(x, height - padding - barHeight, barWidth, barHeight);
                        
                        // Draw month label
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(entry.getKey(), x, height - padding + 15);
                        
                        // Draw value
                        g2d.drawString(String.format("$%.0f", entry.getValue()), x, height - padding - barHeight - 5);
                        
                        x += barWidth + 10;
                        i++;
                    }
                } else {
                    // No data message
                    g2d.setColor(Color.GRAY);
                    g2d.drawString("No sales data available", width / 2 - 60, height / 2);
                }
            }
        };
        
        chartPlaceholder.setPreferredSize(new Dimension(0, 200));
        chartPlaceholder.setBackground(Color.WHITE);
        
        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(chartPlaceholder, BorderLayout.CENTER);
        
        mainPanel.add(chartPanel);
        
        // Recent activity panel
        JPanel activityPanel = new JPanel();
        activityPanel.setLayout(new BorderLayout());
        activityPanel.setBackground(CARD_BG);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        activityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(CARD_TITLE_FONT);
        
        DefaultListModel<String> activityModel = new DefaultListModel<>();
        
        // Get audit trail data from database
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.username, at.trail_activity, at.trail_date " +
                     "FROM audit_trail at JOIN users u ON at.user_id = u.id " +
                     "ORDER BY at.trail_date DESC LIMIT 10")) {
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                do {
                    String username = rs.getString("username");
                    String activity = rs.getString("trail_activity");
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("trail_date"));
                    
                    activityModel.addElement(date + " - " + username + " " + activity);
                } while (rs.next());
            } else {
                activityModel.addElement("No recent activity");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            activityModel.addElement("Error loading activity data");
        }
        
        JList<String> activityList = new JList<>(activityModel);
        activityList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activityList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JScrollPane activityScrollPane = new JScrollPane(activityList);
        activityScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        activityPanel.add(activityTitle, BorderLayout.NORTH);
        activityPanel.add(activityScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(activityPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        reportsPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(reportsPanel);
    }
    
    private Map<String, Double> getMonthlyOrderTotals() {
        Map<String, Double> monthlySales = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May"};
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT MONTH(order_date) as month, SUM(total_amount) as total " +
                     "FROM orders " +
                     "WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 5 MONTH) " +
                     "GROUP BY MONTH(order_date) " +
                     "ORDER BY month")) {
            
            ResultSet rs = pstmt.executeQuery();
            
            // Initialize with zero values
            for (String month : months) {
                monthlySales.put(month, 0.0);
            }
            
            // Fill with actual data
            while (rs.next()) {
                int monthNum = rs.getInt("month") - 1; // 0-based index
                if (monthNum < months.length) {
                    monthlySales.put(months[monthNum], rs.getDouble("total"));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return monthlySales;
    }
    
    // Button renderer for JTable
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setBackground(new Color(41, 128, 185));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            return this;
        }
    }
    
    // Mouse listener for JTable buttons
    class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;
        
        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();
            
            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof String && "View Details".equals(value)) {
                    int orderId = (int) table.getValueAt(row, 0);
                    showOrderDetails(orderId);
                }
            }
        }
    }
    
    private void showOrderDetails(int orderId) {
        JDialog dialog = new JDialog(this, "Order Details #" + orderId, true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(CONTENT_BG);

        // Order information section
        JPanel orderInfoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        orderInfoPanel.setBackground(CARD_BG);
        orderInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT o.id, u.username, o.order_date, o.total_amount, o.status, " +
                     "o.shipping_address, o.payment_method, u.first_name, u.last_name, u.email, u.phone " +
                     "FROM orders o JOIN users u ON o.user_id = u.id " +
                     "WHERE o.id = ?")) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Order details
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date"));
                String customerName = rs.getString("first_name") + " " + rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("status");
                String shippingAddress = rs.getString("shipping_address");
                String paymentMethod = rs.getString("payment_method");

                // Add order information
                orderInfoPanel.add(createInfoLabel("Order Date:"));
                orderInfoPanel.add(createInfoValue(date));

                orderInfoPanel.add(createInfoLabel("Customer:"));
                orderInfoPanel.add(createInfoValue(customerName));

                orderInfoPanel.add(createInfoLabel("Email:"));
                orderInfoPanel.add(createInfoValue(email));

                orderInfoPanel.add(createInfoLabel("Phone:"));
                orderInfoPanel.add(createInfoValue(phone));

                orderInfoPanel.add(createInfoLabel("Status:"));

                // Status combo box for updating
                JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                statusPanel.setBackground(CARD_BG);
                String[] statuses = {"Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
                JComboBox<String> statusCombo = new JComboBox<>(statuses);
                statusCombo.setSelectedItem(status);

                JButton updateStatusBtn = new JButton("Update");
                updateStatusBtn.addActionListener(e -> {
                    String newStatus = (String) statusCombo.getSelectedItem();
                    updateOrderStatus(orderId, newStatus);
                    JOptionPane.showMessageDialog(dialog, "Order status updated to: " + newStatus);

                    // Refresh both orders panel and dashboard after status update
                    refreshOrdersPanel();
                    refreshDashboard();

                    // Close the dialog after updating
                    dialog.dispose();
                });

                statusPanel.add(statusCombo);
                statusPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                statusPanel.add(updateStatusBtn);
                orderInfoPanel.add(statusPanel);

                orderInfoPanel.add(createInfoLabel("Total Amount:"));
                orderInfoPanel.add(createInfoValue(String.format("$%.2f", totalAmount)));

                orderInfoPanel.add(createInfoLabel("Shipping Address:"));
                orderInfoPanel.add(createInfoValue(shippingAddress));

                orderInfoPanel.add(createInfoLabel("Payment Method:"));
                orderInfoPanel.add(createInfoValue(paymentMethod));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error loading order details: " + e.getMessage());
        }

        // Order items table
        String[] columns = {"Book ID", "Title", "Price", "Quantity", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT oi.book_id, b.title, oi.price, oi.quantity " +
                     "FROM order_items oi JOIN books b ON oi.book_id = b.id " +
                     "WHERE oi.order_id = ?")) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                double subtotal = price * quantity;

                Object[] row = {
                    bookId,
                    title,
                    String.format("$%.2f", price),
                    quantity,
                    String.format("$%.2f", subtotal)
                };
                model.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error loading order items: " + e.getMessage());
        }

        JTable itemsTable = new JTable(model);
        itemsTable.setRowHeight(30);
        itemsTable.setShowGrid(false);
        itemsTable.setIntercellSpacing(new Dimension(0, 0));
        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Add components to content panel
        JPanel orderInfoContainer = new JPanel(new BorderLayout());
        orderInfoContainer.setBackground(CONTENT_BG);
        orderInfoContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel orderInfoTitle = new JLabel("Order Information");
        orderInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        orderInfoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        orderInfoContainer.add(orderInfoTitle, BorderLayout.NORTH);
        orderInfoContainer.add(orderInfoPanel, BorderLayout.CENTER);

        JPanel itemsContainer = new JPanel(new BorderLayout());
        itemsContainer.setBackground(CONTENT_BG);

        JLabel itemsTitle = new JLabel("Order Items");
        itemsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        itemsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        itemsContainer.add(itemsTitle, BorderLayout.NORTH);
        itemsContainer.add(tableScrollPane, BorderLayout.CENTER);

        contentPanel.add(orderInfoContainer, BorderLayout.NORTH);
        contentPanel.add(itemsContainer, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CONTENT_BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }
    
    private JLabel createInfoValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
    
    private void updateOrderStatus(int orderId, String newStatus) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE orders SET status = ? WHERE id = ?")) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, orderId);

            int rowsAffected = pstmt.executeUpdate();

            // Log the status update in audit trail
            if (rowsAffected > 0) {
                try (PreparedStatement auditStmt = conn.prepareStatement(
                        "INSERT INTO audit_trail (user_id, trail_activity, trail_date) VALUES (?, ?, NOW())")) {
                    auditStmt.setInt(1, userId);
                    auditStmt.setString(2, "updated order #" + orderId + " status to " + newStatus);
                    auditStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating order status: " + e.getMessage());
        }
    }

    // Method to refresh orders panel data
    private void refreshOrdersPanel() {
        // Remove current panel
        contentPanel.remove(ordersPanel);

        // Reinitialize orders panel
        initializeOrdersPanel();

        // If orders panel is currently displayed, show it again
        if (currentPanel == ordersPanel) {
            showPanel(ordersPanel);
        }
    }

    // Method to refresh dashboard data
    private void refreshDashboard() {
        // Remove current dashboard panel
        contentPanel.remove(dashboardPanel);

        // Reinitialize dashboard
        initializeDashboard();

        // If dashboard is currently displayed, show it again
        if (currentPanel == dashboardPanel) {
            showPanel(dashboardPanel);
        }
    }
    
    private void addAuditTrail(String activity) {
        // In a real app, you would get the current user ID
        
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO audit_trail (user_id, trail_activity, trail_date) VALUES (?, ?, NOW())")) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, activity);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(CONTENT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("Book Title:");
        JTextField titleField = new JTextField(20);
        
        // Author
        JLabel authorLabel = new JLabel("Author:");
        JTextField authorField = new JTextField(20);
        
        // Category
        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Fiction", "Non-Fiction", "Science Fiction", "Fantasy", "Mystery", "Romance", 
                               "Thriller", "Horror", "Biography", "History", "Philosophy", "Psychology", 
                               "Science", "Technology", "Self-Help", "Travel", "Cooking", "Art", "Poetry"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        
        // Description
        JLabel descLabel = new JLabel("Description:");
        JTextArea descArea = new JTextArea(5, 20);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        
        // Price
        JLabel priceLabel = new JLabel("Price ($):");
        JTextField priceField = new JTextField(10);
        
        // Stock Quantity
        JLabel stockLabel = new JLabel("Stock Quantity:");
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
        
        // Publication Date
        JLabel pubDateLabel = new JLabel("Publication Date (YYYY-MM-DD):");
        JTextField pubDateField = new JTextField(10);
        
        // Publisher
        JLabel publisherLabel = new JLabel("Publisher:");
        JTextField publisherField = new JTextField(20);
        
        // ISBN
        JLabel isbnLabel = new JLabel("ISBN:");
        JTextField isbnField = new JTextField(20);
        
        // Cover Image Upload
        JLabel coverLabel = new JLabel("Cover Image:");
        JPanel coverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField coverField = new JTextField(15);
        coverField.setEditable(false);
        JButton browseButton = new JButton("Browse...");
        
        // To store the selected file
        final File[] selectedFile = {null};
        
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Cover Image");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                coverField.setText(selectedFile[0].getName());
            }
        });
        
        coverPanel.add(coverField);
        coverPanel.add(browseButton);
        
        // Add all components
        formPanel.add(createFormRow(titleLabel, titleField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(authorLabel, authorField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(categoryLabel, categoryCombo));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(descLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(descScrollPane);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(priceLabel, priceField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(stockLabel, stockSpinner));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(pubDateLabel, pubDateField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(publisherLabel, publisherField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(isbnLabel, isbnField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(createFormRow(coverLabel, coverPanel));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CONTENT_BG);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Validate inputs
            if (titleField.getText().trim().isEmpty() || 
                authorField.getText().trim().isEmpty() || 
                priceField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title, Author and Price are required fields.");
                return;
            }
            
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Price must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a valid number.");
                return;
            }
            
            // Handle file upload if a file was selected
            String coverImageFileName = "";
            if (selectedFile[0] != null) {
                try {
                    // Get the path to the assets package
                    URL resourceUrl = getClass().getResource("/assets");
                    String assetsPath;
                    
                    if (resourceUrl != null) {
                        // Convert URL to File path
                        assetsPath = new File(resourceUrl.toURI()).getPath();
                    } else {
                        // Assets directory doesn't exist in classpath, try to create it
                        String basePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                        assetsPath = basePath + File.separator + "assets";
                        File assetsDir = new File(assetsPath);
                        if (!assetsDir.exists()) {
                            assetsDir.mkdirs();
                        }
                    }
                    
                    // Generate a unique filename to avoid overwriting
                    String originalFileName = selectedFile[0].getName();
                    String fileExt = originalFileName.substring(originalFileName.lastIndexOf('.'));
                    String uniqueFileName = UUID.randomUUID().toString() + fileExt;
                    
                    File destFile = new File(assetsPath, uniqueFileName);
                    
                    // Copy the file
                    Files.copy(selectedFile[0].toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    // Store only the filename
                    coverImageFileName = uniqueFileName;
                    
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Error uploading image: " + ex.getMessage());
                    return;
                }
            }
            
            // Save book to database
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO books (title, author, category, description, price, cover_image, stock_quantity, publication_date, publisher, isbn) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                pstmt.setString(1, titleField.getText().trim());
                pstmt.setString(2, authorField.getText().trim());
                pstmt.setString(3, (String) categoryCombo.getSelectedItem());
                pstmt.setString(4, descArea.getText().trim());
                pstmt.setDouble(5, Double.parseDouble(priceField.getText().trim()));
                pstmt.setString(6, coverImageFileName); // Store only the filename
                pstmt.setInt(7, (Integer) stockSpinner.getValue());
                
                String pubDate = pubDateField.getText().trim();
                if (pubDate.isEmpty()) {
                    pstmt.setNull(8, java.sql.Types.DATE);
                } else {
                    try {
                        java.sql.Date sqlDate = java.sql.Date.valueOf(pubDate);
                        pstmt.setDate(8, sqlDate);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD format.");
                        return;
                    }
                }
                
                pstmt.setString(9, publisherField.getText().trim());
                pstmt.setString(10, isbnField.getText().trim());
                
                pstmt.executeUpdate();
                
                // Add audit trail entry
                addAuditTrail("added a new book: " + titleField.getText().trim());
                
                JOptionPane.showMessageDialog(dialog, "Book added successfully!");
                dialog.dispose();
                
                // Refresh books panel
                refreshBooksPanel();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error adding book: " + ex.getMessage());
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void showEditBookDialog(int bookId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM books WHERE id = ?")) {
            
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(this, "Edit Book", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());
                
                JPanel formPanel = new JPanel();
                formPanel.setLayout(new GridLayout(11, 2, 10, 10));
                formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                // Title
                JLabel titleLabel = new JLabel("Title:");
                JTextField titleField = new JTextField(rs.getString("title"));
                
                // Author
                JLabel authorLabel = new JLabel("Author:");
                JTextField authorField = new JTextField(rs.getString("author"));
                
                // Category
                JLabel categoryLabel = new JLabel("Category:");
                JTextField categoryField = new JTextField(rs.getString("category"));
                
                // Description
                JLabel descLabel = new JLabel("Description:");
                JTextArea descArea = new JTextArea(rs.getString("description"));
                JScrollPane descScrollPane = new JScrollPane(descArea);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                
                // Price
                JLabel priceLabel = new JLabel("Price:");
                JTextField priceField = new JTextField(String.valueOf(rs.getDouble("price")));
                
                // Stock
                JLabel stockLabel = new JLabel("Stock Quantity:");
                JTextField stockField = new JTextField(String.valueOf(rs.getInt("stock_quantity")));
                
                // Publication Date
                JLabel pubDateLabel = new JLabel("Publication Date (YYYY-MM-DD):");
                JTextField pubDateField = new JTextField();
                if (rs.getDate("publication_date") != null) {
                    pubDateField.setText(rs.getDate("publication_date").toString());
                }
                
                // Publisher
                JLabel publisherLabel = new JLabel("Publisher:");
                JTextField publisherField = new JTextField(rs.getString("publisher"));
                
                // ISBN
                JLabel isbnLabel = new JLabel("ISBN:");
                JTextField isbnField = new JTextField(rs.getString("isbn"));
                
                // Cover Image
                JLabel imageLabel = new JLabel("Cover Image:");
                JTextField imageField = new JTextField(rs.getString("cover_image"));
                JButton browseButton = new JButton("Browse...");
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.add(imageField, BorderLayout.CENTER);
                imagePanel.add(browseButton, BorderLayout.EAST);
                
                // Add components to form
                formPanel.add(titleLabel);
                formPanel.add(titleField);
                formPanel.add(authorLabel);
                formPanel.add(authorField);
                formPanel.add(categoryLabel);
                formPanel.add(categoryField);
                formPanel.add(descLabel);
                formPanel.add(descScrollPane);
                formPanel.add(priceLabel);
                formPanel.add(priceField);
                formPanel.add(stockLabel);
                formPanel.add(stockField);
                formPanel.add(pubDateLabel);
                formPanel.add(pubDateField);
                formPanel.add(publisherLabel);
                formPanel.add(publisherField);
                formPanel.add(isbnLabel);
                formPanel.add(isbnField);
                formPanel.add(imageLabel);
                formPanel.add(imagePanel);
                
                // Button panel
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton cancelButton = new JButton("Cancel");
                JButton saveButton = new JButton("Save");
                
                buttonPanel.add(cancelButton);
                buttonPanel.add(saveButton);
                
                // Add panels to dialog
                dialog.add(formPanel, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                
                // Store the selected file for later use
                final File[] selectedFile = new File[1];
                String originalCoverImage = rs.getString("cover_image");
                
                // Browse button action
                browseButton.addActionListener(e -> {
                    JFileChooser fileChooser = new JFileChooser();
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "Image Files", "jpg", "png", "jpeg", "gif");
                    fileChooser.setFileFilter(filter);
                    
                    int result = fileChooser.showOpenDialog(dialog);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFile[0] = fileChooser.getSelectedFile();
                        imageField.setText(selectedFile[0].getName());
                    }
                });
                
                // Cancel button action
                cancelButton.addActionListener(e -> dialog.dispose());
                
                // Save button action
                saveButton.addActionListener(e -> {
                    try {
                        // Validate input
                        if (titleField.getText().trim().isEmpty() || 
                            authorField.getText().trim().isEmpty() ||
                            priceField.getText().trim().isEmpty() ||
                            stockField.getText().trim().isEmpty()) {
                            
                            JOptionPane.showMessageDialog(dialog, 
                                "Title, Author, Price, and Stock Quantity are required fields.", 
                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        double price;
                        int stock;
                        
                        try {
                            price = Double.parseDouble(priceField.getText().trim());
                            if (price < 0) {
                                JOptionPane.showMessageDialog(dialog, "Price cannot be negative.",
                                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(dialog, "Price must be a valid number.",
                                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        try {
                            stock = Integer.parseInt(stockField.getText().trim());
                            if (stock < 0) {
                                JOptionPane.showMessageDialog(dialog, "Stock quantity cannot be negative.",
                                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(dialog, "Stock quantity must be a whole number.",
                                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        // Handle image upload if a new file is selected
                        String coverImageFileName = originalCoverImage;
                        
                        if (selectedFile[0] != null) {
                            try {
                                // Get the path to the assets package
                                URL resourceUrl = getClass().getResource("/assets");
                                String assetsPath;
                                
                                if (resourceUrl != null) {
                                    // Convert URL to File path
                                    assetsPath = new File(resourceUrl.toURI()).getPath();
                                } else {
                                    // Assets directory doesn't exist in classpath, try to create it
                                    String basePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                                    assetsPath = basePath + File.separator + "assets";
                                    File assetsDir = new File(assetsPath);
                                    if (!assetsDir.exists()) {
                                        assetsDir.mkdirs();
                                    }
                                }
                                
                                // Generate a unique filename to avoid overwriting
                                String originalFileName = selectedFile[0].getName();
                                String fileExt = originalFileName.substring(originalFileName.lastIndexOf('.'));
                                String uniqueFileName = UUID.randomUUID().toString() + fileExt;
                                
                                File destFile = new File(assetsPath, uniqueFileName);
                                
                                // Copy the file
                                Files.copy(selectedFile[0].toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                
                                // Store only the filename
                                coverImageFileName = uniqueFileName;
                                
                            } catch (IOException | URISyntaxException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(dialog, "Error uploading image: " + ex.getMessage());
                                return;
                            }
                        }
                        
                        // Prepare update query
                        String updateQuery = "UPDATE books SET title = ?, author = ?, category = ?, " +
                                            "description = ?, price = ?, stock_quantity = ?, " +
                                            "publication_date = ?, publisher = ?, isbn = ?, " +
                                            "cover_image = ? WHERE id = ?";
                        
                        try (Connection updateConn = DatabaseConnection.getConnection();
                             PreparedStatement updateStmt = updateConn.prepareStatement(updateQuery)) {
                            
                            updateStmt.setString(1, titleField.getText().trim());
                            updateStmt.setString(2, authorField.getText().trim());
                            updateStmt.setString(3, categoryField.getText().trim());
                            updateStmt.setString(4, descArea.getText().trim());
                            updateStmt.setDouble(5, price);
                            updateStmt.setInt(6, stock);
                            
                            // Handle date
                            String pubDateStr = pubDateField.getText().trim();
                            if (pubDateStr.isEmpty()) {
                                updateStmt.setNull(7, java.sql.Types.DATE);
                            } else {
                                try {
                                    LocalDate date = LocalDate.parse(pubDateStr, DateTimeFormatter.ISO_DATE);
                                    updateStmt.setDate(7, java.sql.Date.valueOf(date));
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(dialog, 
                                        "Invalid date format. Please use YYYY-MM-DD.", 
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            
                            updateStmt.setString(8, publisherField.getText().trim());
                            
                            // Handle ISBN
                            String isbn = isbnField.getText().trim();
                            if (isbn.isEmpty()) {
                                updateStmt.setNull(9, java.sql.Types.VARCHAR);
                            } else {
                                updateStmt.setString(9, isbn);
                            }
                            
                            // Set cover image filename in database
                            if (coverImageFileName == null || coverImageFileName.isEmpty()) {
                                updateStmt.setNull(10, java.sql.Types.VARCHAR);
                            } else {
                                updateStmt.setString(10, coverImageFileName);
                            }
                            
                            updateStmt.setInt(11, bookId);
                            
                            int rowsAffected = updateStmt.executeUpdate();
                            
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(dialog, "Book updated successfully!",
                                                           "Success", JOptionPane.INFORMATION_MESSAGE);
                                refreshBooksTable();
                                dialog.dispose();
                            } else {
                                JOptionPane.showMessageDialog(dialog, "Failed to update book.",
                                                           "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(),
                                                   "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Book not found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving book details: " + e.getMessage());
        }
    }
    
    private void deleteBook(int bookId) {
        // First check if the book exists and get its title for confirmation
        String bookTitle = "";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT title FROM books WHERE id = ?")) {
            
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                bookTitle = rs.getString("title");
            } else {
                JOptionPane.showMessageDialog(this, "Book not found!", 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving book: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if book is associated with any orders or in cart
        boolean isInUse = false;
        String errorMessage = "";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check order_items
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM order_items WHERE book_id = ?")) {
                pstmt.setInt(1, bookId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    isInUse = true;
                    errorMessage = "This book is associated with existing orders and cannot be deleted.";
                }
            }
            
            // Check cart
            if (!isInUse) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM cart WHERE book_id = ?")) {
                    pstmt.setInt(1, bookId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        isInUse = true;
                        errorMessage = "This book is in customers' shopping carts and cannot be deleted.";
                    }
                }
            }
            
            // Check wishlist
            if (!isInUse) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM wishlist WHERE book_id = ?")) {
                    pstmt.setInt(1, bookId);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        isInUse = true;
                        errorMessage = "This book is in customers' wishlists and cannot be deleted.";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking book references: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (isInUse) {
            JOptionPane.showMessageDialog(this, errorMessage, 
                                        "Cannot Delete Book", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the book \"" + bookTitle + "\"?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
                
                pstmt.setInt(1, bookId);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, 
                                                "Book \"" + bookTitle + "\" has been deleted.",
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBooksTable();
                } else {
                    JOptionPane.showMessageDialog(this, 
                                                "Failed to delete book.",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                                            "Error deleting book: " + e.getMessage(),
                                            "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Helper method to refresh the books table after changes
    private void refreshBooksTable() {
        // Get the table model from the books panel
        JTable table = null;
        DefaultTableModel model = null;
        
        // Find the table component in the books panel
        for (Component c : booksPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                for (Component inner : panel.getComponents()) {
                    if (inner instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) inner;
                        if (scrollPane.getViewport().getView() instanceof JTable) {
                            table = (JTable) scrollPane.getViewport().getView();
                            model = (DefaultTableModel) table.getModel();
                            break;
                        }
                    }
                }
            }
            if (model != null) break;
        }
        
        if (model == null) {
            JOptionPane.showMessageDialog(this, "Could not find books table to refresh.");
            return;
        }
        
        // Clear existing data
        model.setRowCount(0);
        
        // Reload data from database
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, author, category, price, stock_quantity FROM books")) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    String.format("$%.2f", rs.getDouble("price")),
                    rs.getInt("stock_quantity")
                };
                model.addRow(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reloading books: " + e.getMessage());
        }
    }
    
    private JPanel createFormRow(JLabel label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CONTENT_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(field);
        
        return panel;
    }
    
    private void refreshBooksPanel() {
        // Re-initialize the books panel to refresh the data
        contentPanel.remove(booksPanel);
        initializeBooksPanel();
        
        // If currently viewing books panel, show the updated one
        if (currentPanel == booksPanel) {
            showPanel(booksPanel);
        }
    }
    
    public static void main(String[] args) {
        try {
            // Set Nimbus look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        int testUserId = 0;
        int testRoleId = 0;
        
        SwingUtilities.invokeLater(() -> new AdminPanel(testUserId, testRoleId));
    }
}