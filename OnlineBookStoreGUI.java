// OnlineBookStoreGUI.java
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OnlineBookStoreGUI extends JFrame {
    private BookStoreFacade facade;
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public OnlineBookStoreGUI() {
        facade = new BookStoreFacade();
        
        setTitle("Online Book Store");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Online Book Store");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);

        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(41, 128, 185));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        JButton registerButton = new JButton("Register as Customer");
        registerButton.setBackground(new Color(46, 204, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        gbc.gridy = 4;
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            User user = facade.login(username, password);
            if (user != null) {
                currentUser = user;
                if (user instanceof Customer) {
                    showCustomerDashboard((Customer) user);
                } else if (user instanceof Admin) {
                    showAdminDashboard((Admin) user);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", 
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> showRegistrationDialog());

        return panel;
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Customer Registration", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(46, 204, 113));
        registerBtn.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String address = addressField.getText();
            String phone = phoneField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password required!");
                return;
            }

            facade.registerCustomer(username, password, address, phone);
            JOptionPane.showMessageDialog(dialog, "Registration successful!");
            dialog.dispose();
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCustomerDashboard(Customer customer) {
        JPanel dashboard = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + customer.getUsername());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            currentUser = null;
            mainPanel.removeAll();
            mainPanel.add(createLoginPanel(), "LOGIN");
            cardLayout.show(mainPanel, "LOGIN");
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        dashboard.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Store references to panels that need refreshing
        JPanel[] cartPanelHolder = new JPanel[1];
        JPanel[] ordersPanelHolder = new JPanel[1];
        
        tabbedPane.addTab("Browse Books", createBrowseBooksPanel(customer));
        cartPanelHolder[0] = createCartPanel(customer);
        tabbedPane.addTab("Shopping Cart", cartPanelHolder[0]);
        ordersPanelHolder[0] = createOrdersPanel(customer);
        tabbedPane.addTab("My Orders", ordersPanelHolder[0]);
        tabbedPane.addTab("Account", createAccountPanel(customer));

        // Add listener to refresh cart when tab is selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 1) { // Shopping Cart tab
                System.out.println("DEBUG: Switching to cart tab, refreshing...");
                tabbedPane.setComponentAt(1, createCartPanel(customer));
            } else if (selectedIndex == 2) { // My Orders tab
                tabbedPane.setComponentAt(2, createOrdersPanel(customer));
            }
        });

        dashboard.add(tabbedPane, BorderLayout.CENTER);

        mainPanel.add(dashboard, "CUSTOMER_DASHBOARD");
        cardLayout.show(mainPanel, "CUSTOMER_DASHBOARD");
    }

    private JPanel createBrowseBooksPanel(Customer customer) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.addItem("All Categories");
        facade.getAllCategories().forEach(categoryCombo::addItem);
        
        JComboBox<String> sortCombo = new JComboBox<>(
                new String[]{"Default", "Price: Low to High", "Price: High to Low", "Popularity"});

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryCombo);
        searchPanel.add(new JLabel("Sort:"));
        searchPanel.add(sortCombo);

        panel.add(searchPanel, BorderLayout.NORTH);

        JPanel booksPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(booksPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        Runnable updateBooks = () -> {
            booksPanel.removeAll();
            java.util.List<Book> books = facade.browseAllBooks();

            String searchText = searchField.getText();
            if (!searchText.isEmpty()) {
                books = facade.searchBooks(searchText);
            }

            String selectedCategory = (String) categoryCombo.getSelectedItem();
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                books = facade.filterBooksByCategory(selectedCategory);
            }

            String sortOption = (String) sortCombo.getSelectedItem();
            if (sortOption != null) {
                switch (sortOption) {
                    case "Price: Low to High":
                        books = facade.sortBooksByPrice(true);
                        break;
                    case "Price: High to Low":
                        books = facade.sortBooksByPrice(false);
                        break;
                    case "Popularity":
                        books = facade.sortBooksByPopularity();
                        break;
                }
            }

            for (Book book : books) {
                booksPanel.add(createBookCard(book, customer));
            }

            booksPanel.revalidate();
            booksPanel.repaint();
        };

        searchButton.addActionListener(e -> updateBooks.run());
        categoryCombo.addActionListener(e -> updateBooks.run());
        sortCombo.addActionListener(e -> updateBooks.run());

        updateBooks.run();

        return panel;
    }

    private JPanel createBookCard(Book book, Customer customer) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.setBackground(Color.WHITE);
        
        String titleText = "<html><b>" + book.getTitle() + "</b>";
        if (book.isFeatured()) {
            titleText += " <span style='color: gold;'>⭐ FEATURED</span>";
        }
        titleText += "</html>";
        infoPanel.add(new JLabel(titleText));
        
        infoPanel.add(new JLabel("Author: " + book.getAuthor()));
        
        if (book.isDiscounted()) {
            String priceText = "<html>Price: <strike>$" + String.format("%.2f", book.getOriginalPrice()) + 
                             "</strike> <span style='color: red;'>$" + String.format("%.2f", book.getPrice()) + 
                             " (" + (int)book.getDiscountPercentage() + "% OFF)</span></html>";
            infoPanel.add(new JLabel(priceText));
        } else {
            infoPanel.add(new JLabel("Price: $" + String.format("%.2f", book.getPrice())));
        }
        
        infoPanel.add(new JLabel("Category: " + book.getCategory()));
        infoPanel.add(new JLabel("Stock: " + book.getStock()));
        infoPanel.add(new JLabel("Popularity: " + book.getPopularity()));

        card.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, book.getStock(), 1));
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setBackground(new Color(46, 204, 113));
        addToCartBtn.setForeground(Color.WHITE);
        
        JButton reviewsBtn = new JButton("Reviews");
        reviewsBtn.setBackground(new Color(52, 152, 219));
        reviewsBtn.setForeground(Color.WHITE);

        addToCartBtn.addActionListener(e -> {
            int quantity = (Integer) quantitySpinner.getValue();
            try {
                facade.addToCart(customer, book, quantity);
                JOptionPane.showMessageDialog(this, "Added to cart!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        reviewsBtn.addActionListener(e -> showReviewsDialog(book, customer));

        buttonPanel.add(quantitySpinner);
        buttonPanel.add(addToCartBtn);
        buttonPanel.add(reviewsBtn);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private void showReviewsDialog(Book book, Customer customer) {
        JDialog dialog = new JDialog(this, "Reviews for " + book.getTitle(), true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(reviewsPanel);

        java.util.List<Review> reviews = facade.getBookReviews(book.getId());
        if (reviews.isEmpty()) {
            reviewsPanel.add(new JLabel("No reviews yet. Be the first to review!"));
        } else {
            for (Review review : reviews) {
                JPanel reviewPanel = new JPanel(new BorderLayout());
                reviewPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                reviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
                
                JLabel reviewLabel = new JLabel("<html><b>" + review.getCustomerUsername() + 
                        "</b> - Rating: " + review.getRating() + "/5<br>" +
                        review.getComment() + "<br><i>" + review.getReviewDate() + "</i></html>");
                reviewPanel.add(reviewLabel);
                reviewsPanel.add(reviewPanel);
            }
        }

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel addReviewPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        addReviewPanel.add(new JLabel("Rating (1-5):"), gbc);
        
        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        gbc.gridx = 1;
        addReviewPanel.add(ratingSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addReviewPanel.add(new JLabel("Comment:"), gbc);
        
        JTextArea commentArea = new JTextArea(3, 30);
        gbc.gridx = 1;
        addReviewPanel.add(new JScrollPane(commentArea), gbc);

        JButton submitBtn = new JButton("Submit Review");
        submitBtn.setBackground(new Color(46, 204, 113));
        submitBtn.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        addReviewPanel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            int rating = (Integer) ratingSpinner.getValue();
            String comment = commentArea.getText();
            if (!comment.isEmpty()) {
                facade.addReview(customer, book.getId(), rating, comment);
                JOptionPane.showMessageDialog(dialog, "Review submitted!");
                dialog.dispose();
            }
        });

        mainPanel.add(addReviewPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createCartPanel(Customer customer) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(totalLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearCartBtn = new JButton("Clear Cart");
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.setBackground(new Color(46, 204, 113));
        checkoutBtn.setForeground(Color.WHITE);
        clearCartBtn.setBackground(new Color(231, 76, 60));
        clearCartBtn.setForeground(Color.WHITE);

        buttonPanel.add(clearCartBtn);
        buttonPanel.add(checkoutBtn);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        final Runnable[] updateCartWrapper = new Runnable[1];
        
        Runnable updateCart = () -> {
            cartItemsPanel.removeAll();
            
            List<OrderItem> cartItems = facade.getCartItems(customer);
            System.out.println("DEBUG: Cart has " + cartItems.size() + " items"); // DEBUG
            
            if (cartItems.isEmpty()) {
                JLabel emptyLabel = new JLabel("Your cart is empty");
                emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                cartItemsPanel.add(emptyLabel);
            } else {
                for (OrderItem item : cartItems) {
                    System.out.println("DEBUG: Item - " + item.getBook().getTitle()); // DEBUG
                    
                    JPanel itemPanel = new JPanel(new BorderLayout(10, 10));
                    itemPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

                    JLabel itemLabel = new JLabel("<html><b>" + item.getBook().getTitle() + 
                            "</b><br>Price: $" + String.format("%.2f", item.getPriceAtPurchase()) + 
                            " x " + item.getQuantity() + 
                            " = $" + String.format("%.2f", item.getSubtotal()) + "</html>");
                    itemPanel.add(itemLabel, BorderLayout.CENTER);

                    JPanel controlPanel = new JPanel(new FlowLayout());
                    JSpinner quantitySpinner = new JSpinner(
                            new SpinnerNumberModel(item.getQuantity(), 1, item.getBook().getStock(), 1));
                    JButton updateBtn = new JButton("Update");
                    JButton removeBtn = new JButton("Remove");
                    removeBtn.setBackground(new Color(231, 76, 60));
                    removeBtn.setForeground(Color.WHITE);

                    updateBtn.addActionListener(e -> {
                        int newQuantity = (Integer) quantitySpinner.getValue();
                        facade.updateCartQuantity(customer, item.getBook().getId(), newQuantity);
                        updateCartWrapper[0].run();
                    });

                    removeBtn.addActionListener(e -> {
                        facade.removeFromCart(customer, item.getBook().getId());
                        updateCartWrapper[0].run();
                    });

                    controlPanel.add(new JLabel("Qty:"));
                    controlPanel.add(quantitySpinner);
                    controlPanel.add(updateBtn);
                    controlPanel.add(removeBtn);
                    itemPanel.add(controlPanel, BorderLayout.EAST);

                    cartItemsPanel.add(itemPanel);
                }
            }

            totalLabel.setText("Total: $" + String.format("%.2f", facade.getCartTotal(customer)));
            cartItemsPanel.revalidate();
            cartItemsPanel.repaint();
        };
        
        updateCartWrapper[0] = updateCart;

        clearCartBtn.addActionListener(e -> {
            facade.clearCart(customer);
            updateCart.run();
        });

        checkoutBtn.addActionListener(e -> {
            if (customer.getCart().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Proceed to checkout?\nTotal: $" + facade.getCartTotal(customer),
                    "Checkout", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Order order = facade.placeOrder(customer);
                    JOptionPane.showMessageDialog(this, 
                            "Order placed successfully!\nOrder ID: " + order.getOrderId());
                    updateCart.run();
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateCart.run();

        return panel;
    }

    private JPanel createOrdersPanel(Customer customer) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> updateOrders(customer, ordersPanel));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshBtn);
        panel.add(topPanel, BorderLayout.NORTH);

        updateOrders(customer, ordersPanel);

        return panel;
    }

    private void updateOrders(Customer customer, JPanel ordersPanel) {
        ordersPanel.removeAll();

        for (Order order : facade.getCustomerOrderHistory(customer)) {
            JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
            orderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            orderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

            StringBuilder orderInfo = new StringBuilder("<html><b>Order ID: " + order.getOrderId() + "</b><br>");
            orderInfo.append("Status: ").append(order.getStatus()).append("<br>");
            orderInfo.append("Date: ").append(order.getOrderDate()).append("<br>");
            orderInfo.append("Total: $").append(String.format("%.2f", order.getTotalAmount())).append("<br>");
            orderInfo.append("Items:<br>");
            for (OrderItem item : order.getItems()) {
                orderInfo.append("&nbsp;&nbsp;- ").append(item.toString()).append("<br>");
            }
            orderInfo.append("</html>");

            JLabel orderLabel = new JLabel(orderInfo.toString());
            orderPanel.add(orderLabel, BorderLayout.CENTER);

            if (order.getStatus().equals("PENDING")) {
                JButton cancelBtn = new JButton("Cancel Order");
                cancelBtn.setBackground(new Color(231, 76, 60));
                cancelBtn.setForeground(Color.WHITE);
                cancelBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Cancel this order?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        facade.cancelOrder(customer, order.getOrderId());
                        updateOrders(customer, ordersPanel);
                    }
                });
                orderPanel.add(cancelBtn, BorderLayout.EAST);
            }

            ordersPanel.add(orderPanel);
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private JPanel createAccountPanel(Customer customer) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Account Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(customer.getUsername()), gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Address:"), gbc);
        JTextField addressField = new JTextField(customer.getAddress(), 20);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Phone:"), gbc);
        JTextField phoneField = new JTextField(customer.getPhone(), 20);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        JButton updateBtn = new JButton("Update Information");
        updateBtn.setBackground(new Color(41, 128, 185));
        updateBtn.setForeground(Color.WHITE);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(updateBtn, gbc);

        updateBtn.addActionListener(e -> {
            facade.updateCustomerInfo(customer, addressField.getText(), phoneField.getText());
            JOptionPane.showMessageDialog(this, "Information updated successfully!");
        });

        return panel;
    }

    private void showAdminDashboard(Admin admin) {
        JPanel dashboard = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(52, 73, 94));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Admin Dashboard - " + admin.getUsername());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> {
            currentUser = null;
            mainPanel.removeAll();
            mainPanel.add(createLoginPanel(), "LOGIN");
            cardLayout.show(mainPanel, "LOGIN");
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        dashboard.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Books", createManageBooksPanel());
        tabbedPane.addTab("Manage Orders", createManageOrdersPanel());
        tabbedPane.addTab("Statistics", createStatisticsPanel());
        tabbedPane.addTab("Categories", createManageCategoriesPanel());

        dashboard.add(tabbedPane, BorderLayout.CENTER);

        mainPanel.add(dashboard, "ADMIN_DASHBOARD");
        cardLayout.show(mainPanel, "ADMIN_DASHBOARD");
    }

    private JPanel createManageBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBookBtn = new JButton("Add New Book");
        addBookBtn.setBackground(new Color(46, 204, 113));
        addBookBtn.setForeground(Color.WHITE);
        buttonPanel.add(addBookBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Title", "Author", "Price", "Category", "Stock", "Edition", "Actions"};
        Object[][] data = {};
        
        JTable booksTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        Runnable updateTable = () -> {
            java.util.List<Book> books = facade.browseAllBooks();
            Object[][] newData = new Object[books.size()][8];
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                newData[i] = new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    "$" + String.format("%.2f", book.getPrice()),
                    book.getCategory(),
                    book.getStock(),
                    book.getEdition(),
                    "Edit/Delete"
                };
            }
            booksTable.setModel(new javax.swing.table.DefaultTableModel(newData, columnNames));
        };

        addBookBtn.addActionListener(e -> showAddBookDialog(updateTable));
        
        booksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = booksTable.rowAtPoint(evt.getPoint());
int col = booksTable.columnAtPoint(evt.getPoint());
if (col == 7) {
String bookId = (String) booksTable.getValueAt(row, 0);
showBookActionsDialog(bookId, updateTable);
}
}
});
    updateTable.run();

    return panel;
}

private void showAddBookDialog(Runnable updateCallback) {
    JDialog dialog = new JDialog(this, "Add New Book", true);
    dialog.setSize(500, 600);
    dialog.setLocationRelativeTo(this);

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField idField = new JTextField(20);
    JTextField titleField = new JTextField(20);
    JTextField authorField = new JTextField(20);
    JTextField priceField = new JTextField(20);
    JComboBox<String> categoryCombo = new JComboBox<>();
    facade.getAllCategories().forEach(categoryCombo::addItem);
    JTextField stockField = new JTextField(20);
    JTextField editionField = new JTextField(20);
    JTextField coverField = new JTextField(20);
    
    JCheckBox featuredCheck = new JCheckBox("Featured");
    JCheckBox discountCheck = new JCheckBox("Discounted");
    JTextField discountField = new JTextField("0", 20);
    discountField.setEnabled(false);

    discountCheck.addActionListener(e -> discountField.setEnabled(discountCheck.isSelected()));

    int row = 0;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Book ID:"), gbc);
    gbc.gridx = 1;
    panel.add(idField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Title:"), gbc);
    gbc.gridx = 1;
    panel.add(titleField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Author:"), gbc);
    gbc.gridx = 1;
    panel.add(authorField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Price:"), gbc);
    gbc.gridx = 1;
    panel.add(priceField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Category:"), gbc);
    gbc.gridx = 1;
    panel.add(categoryCombo, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Stock:"), gbc);
    gbc.gridx = 1;
    panel.add(stockField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Edition:"), gbc);
    gbc.gridx = 1;
    panel.add(editionField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Cover Image:"), gbc);
    gbc.gridx = 1;
    panel.add(coverField, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(featuredCheck, gbc);
    gbc.gridx = 1;
    panel.add(discountCheck, gbc);

    row++;
    gbc.gridx = 0; gbc.gridy = row;
    panel.add(new JLabel("Discount % (0-100):"), gbc);
    gbc.gridx = 1;
    panel.add(discountField, gbc);

    JButton addBtn = new JButton("Add Book");
    addBtn.setBackground(new Color(46, 204, 113));
    addBtn.setForeground(Color.WHITE);
    row++;
    gbc.gridx = 0; gbc.gridy = row;
    gbc.gridwidth = 2;
    panel.add(addBtn, gbc);

    addBtn.addActionListener(e -> {
        try {
            Book book = new BasicBook(
                idField.getText(),
                titleField.getText(),
                authorField.getText(),
                Double.parseDouble(priceField.getText()),
                (String) categoryCombo.getSelectedItem(),
                Integer.parseInt(stockField.getText()),
                editionField.getText(),
                coverField.getText()
            );
            
            // Apply decorators
            if (discountCheck.isSelected()) {
                double discount = Double.parseDouble(discountField.getText()) / 100.0;
                book = new DiscountedBook(book, discount);
            }
            
            if (featuredCheck.isSelected()) {
                book = new FeaturedBook(book);
            }
            
            facade.addBookWithDecorators(book);
            JOptionPane.showMessageDialog(dialog, "Book added successfully!");
            updateCallback.run();
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Invalid price, stock, or discount value!");
        }
    });

    dialog.add(new JScrollPane(panel));
    dialog.setVisible(true);
}

private void showBookActionsDialog(String bookId, Runnable updateCallback) {
    Book book = facade.getBookDetails(bookId);
    if (book == null) return;

    String[] options = {"Edit Stock", "Delete", "Cancel"};
    int choice = JOptionPane.showOptionDialog(this,
            "Choose action for: " + book.getTitle(),
            "Book Actions",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);

    if (choice == 0) { // Edit Stock
        String stockStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", book.getStock());
        if (stockStr != null) {
            try {
                int newStock = Integer.parseInt(stockStr);
                facade.updateBookStock(bookId, newStock);
                JOptionPane.showMessageDialog(this, "Stock updated!");
                updateCallback.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid stock value!");
            }
        }
    } else if (choice == 1) { // Delete
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete book: " + book.getTitle() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            facade.deleteBook(bookId);
            JOptionPane.showMessageDialog(this, "Book deleted!");
            updateCallback.run();
        }
    }
}

private JPanel createManageOrdersPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel ordersPanel = new JPanel();
    ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(ordersPanel);
    panel.add(scrollPane, BorderLayout.CENTER);

    JButton refreshBtn = new JButton("Refresh");
    JComboBox<String> filterCombo = new JComboBox<>(
            new String[]{"All Orders", "Pending", "Confirmed", "Shipped", "Cancelled"});
    
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(new JLabel("Filter:"));
    topPanel.add(filterCombo);
    topPanel.add(refreshBtn);
    panel.add(topPanel, BorderLayout.NORTH);

    final Runnable[] updateOrdersWrapper = new Runnable[1];
    
    Runnable updateOrders = () -> {
        ordersPanel.removeAll();
        java.util.List<Order> orders = facade.getAllOrders();
        
        String filter = (String) filterCombo.getSelectedItem();
        if (!filter.equals("All Orders")) {
            final String status = filter.toUpperCase();
            orders = orders.stream()
                    .filter(o -> o.getStatus().equals(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        for (Order order : orders) {
            JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
            orderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            orderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            StringBuilder orderInfo = new StringBuilder("<html><b>Order ID: " + order.getOrderId() + "</b><br>");
            orderInfo.append("Customer: ").append(order.getCustomer().getUsername()).append("<br>");
            orderInfo.append("Status: ").append(order.getStatus()).append("<br>");
            orderInfo.append("Date: ").append(order.getOrderDate()).append("<br>");
            orderInfo.append("Total: $").append(String.format("%.2f", order.getTotalAmount())).append("<br>");
            orderInfo.append("Items:<br>");
            for (OrderItem item : order.getItems()) {
                orderInfo.append("&nbsp;&nbsp;- ").append(item.toString()).append("<br>");
            }
            orderInfo.append("</html>");

            JLabel orderLabel = new JLabel(orderInfo.toString());
            orderPanel.add(orderLabel, BorderLayout.CENTER);

            JPanel actionPanel = new JPanel(new FlowLayout());
            
            if (order.getStatus().equals("PENDING")) {
                JButton confirmBtn = new JButton("Confirm");
                confirmBtn.setBackground(new Color(46, 204, 113));
                confirmBtn.setForeground(Color.WHITE);
                confirmBtn.addActionListener(e -> {
                    facade.confirmOrder(order.getOrderId());
                    updateOrdersWrapper[0].run();
                });
                actionPanel.add(confirmBtn);
                
                JButton cancelBtn = new JButton("Cancel");
                cancelBtn.setBackground(new Color(231, 76, 60));
                cancelBtn.setForeground(Color.WHITE);
                cancelBtn.addActionListener(e -> {
                    facade.cancelOrderByAdmin(order.getOrderId());
                    updateOrdersWrapper[0].run();
                });
                actionPanel.add(cancelBtn);
            }
            
            if (order.getStatus().equals("CONFIRMED")) {
                JButton shipBtn = new JButton("Mark as Shipped");
                shipBtn.setBackground(new Color(52, 152, 219));
                shipBtn.setForeground(Color.WHITE);
                shipBtn.addActionListener(e -> {
                    facade.shipOrder(order.getOrderId());
                    updateOrdersWrapper[0].run();
                });
                actionPanel.add(shipBtn);
            }

            orderPanel.add(actionPanel, BorderLayout.EAST);
            ordersPanel.add(orderPanel);
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    };
    
    updateOrdersWrapper[0] = updateOrders;

    refreshBtn.addActionListener(e -> updateOrders.run());
    filterCombo.addActionListener(e -> updateOrders.run());

    updateOrders.run();

    return panel;
}

private JPanel createStatisticsPanel() {
    JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel revenueLabel = new JLabel("Total Revenue: $" + 
            String.format("%.2f", facade.getTotalRevenue()));
    revenueLabel.setFont(new Font("Arial", Font.BOLD, 18));
    panel.add(revenueLabel);

    JLabel ordersLabel = new JLabel("Total Orders: " + facade.getTotalOrdersCount());
    ordersLabel.setFont(new Font("Arial", Font.BOLD, 18));
    panel.add(ordersLabel);

    JLabel topBooksLabel = new JLabel("Top 5 Selling Books:");
    topBooksLabel.setFont(new Font("Arial", Font.BOLD, 16));
    panel.add(topBooksLabel);

    for (Book book : facade.getTopSellingBooks(5)) {
        panel.add(new JLabel("  - " + book.getTitle() + " (Popularity: " + 
                book.getPopularity() + ")"));
    }

    JLabel categorySalesLabel = new JLabel("Sales by Category:");
    categorySalesLabel.setFont(new Font("Arial", Font.BOLD, 16));
    panel.add(categorySalesLabel);

    for (java.util.Map.Entry<String, Integer> entry : 
            facade.getCategorySalesStatistics().entrySet()) {
        panel.add(new JLabel("  - " + entry.getKey() + ": " + 
                entry.getValue() + " books sold"));
    }

    JButton refreshBtn = new JButton("Refresh Statistics");
    refreshBtn.addActionListener(e -> {
        Container parent = (Container) panel.getParent();
        if (parent != null) {
            parent.remove(panel);
            parent.add(createStatisticsPanel());
            parent.revalidate();
            parent.repaint();
        }
    });
    panel.add(refreshBtn);

    return panel;
}

private JPanel createManageCategoriesPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel categoriesPanel = new JPanel();
    categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
    JScrollPane scrollPane = new JScrollPane(categoriesPanel);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JTextField newCategoryField = new JTextField(20);
    JButton addCategoryBtn = new JButton("Add Category");
    addCategoryBtn.setBackground(new Color(46, 204, 113));
    addCategoryBtn.setForeground(Color.WHITE);
    
    topPanel.add(new JLabel("New Category:"));
    topPanel.add(newCategoryField);
    topPanel.add(addCategoryBtn);
    panel.add(topPanel, BorderLayout.NORTH);

    Runnable updateCategories = () -> {
        categoriesPanel.removeAll();
        for (String category : facade.getAllCategories()) {
            JLabel categoryLabel = new JLabel("• " + category);
            categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            categoriesPanel.add(categoryLabel);
        }
        categoriesPanel.revalidate();
        categoriesPanel.repaint();
    };

    addCategoryBtn.addActionListener(e -> {
        String newCategory = newCategoryField.getText().trim();
        if (!newCategory.isEmpty()) {
            facade.addCategory(newCategory);
            newCategoryField.setText("");
            updateCategories.run();
            JOptionPane.showMessageDialog(this, "Category added!");
        }
    });

    updateCategories.run();

    return panel;
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        OnlineBookStoreGUI gui = new OnlineBookStoreGUI();
        gui.setVisible(true);
    });
}
}