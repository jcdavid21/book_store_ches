package chescabookstore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class SidebarPanel extends JPanel {

    // Colors
    private static final Color SIDEBAR_COLOR = new Color(39, 55, 77);
    private static final Color HIGHLIGHT_COLOR = new Color(157, 178, 191);
    private static final Color TEXT_COLOR = new Color(221, 230, 237);

    // Font
    private static final Font MENU_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    // Callback interface for menu item clicks
    public interface MenuClickListener {
        void onMenuItemClicked(String menuItem);
    }
    
    private MenuClickListener clickListener;
    
    public SidebarPanel(MenuClickListener listener) {
        this.clickListener = listener;
        initializeSidebar();
    }
    
    private void initializeSidebar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(SIDEBAR_COLOR);
        setPreferredSize(new Dimension(200, 0));
        setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(SIDEBAR_COLOR);
        logoPanel.setMaximumSize(new Dimension(200, 100));
        JLabel logoLabel = new JLabel("Libroloco");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(TEXT_COLOR);
        logoPanel.add(logoLabel);
        add(logoPanel);

        // Add some space
        add(Box.createRigidArea(new Dimension(0, 30)));

        // Menu items
        String[] menuItems = {"Home", "My Cart", "Wishlist", "My Orders", "Profile", "Logout"};

        for (String item : menuItems) {
            JPanel menuItemPanel = createMenuItemPanel(item);
            add(menuItemPanel);
        }
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
                if (clickListener != null) {
                    clickListener.onMenuItemClicked(text);
                }
            }
        });

        return panel;
    }

    public JPanel createSubMenuItemPanel(String text) {
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
}