import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.List;
import java.util.Optional;

public class OnlineBookStoreFX extends Application {
    private BookStoreFacade facade;
    private User currentUser;
    private Stage primaryStage;
    private Scene mainScene;
    private BorderPane mainContainer;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.facade = new BookStoreFacade();

        // Add shutdown hook to save data
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n========== Application Closing ==========");
            BookStoreSystem.getInstance().saveAllData();
            System.out.println("Goodbye!");
        }));

        // Show login dialog first
        showLoginDialog();
    }

    private void showLoginDialog() {
        Stage loginStage = new Stage();
        loginStage.initStyle(StageStyle.UNDECORATED);
        
        VBox loginBox = new VBox(20);
        loginBox.getStyleClass().add("login-dialog");
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(50, 60, 50, 60));

        // Icon/Logo placeholder
        Label iconLabel = new Label("📚");
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label titleLabel = new Label("Book Store");
        titleLabel.getStyleClass().add("login-title");
        
        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.getStyleClass().add("login-subtitle");

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("field-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().add("text-field");

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("password-field");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setPrefWidth(140);
        
        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("btn-success");
        registerButton.setPrefWidth(140);

        buttonBox.getChildren().addAll(loginButton, registerButton);

        formBox.getChildren().addAll(
            usernameLabel, usernameField,
            passwordLabel, passwordField
        );

        loginBox.getChildren().addAll(iconLabel, titleLabel, subtitleLabel, formBox, buttonBox);

        Scene loginScene = new Scene(loginBox, 500, 550);
        loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        loginStage.setScene(loginScene);
        loginStage.centerOnScreen();
        loginStage.show();

        // Login button action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            User user = facade.login(username, password);
            if (user != null) {
                currentUser = user;
                loginStage.close();
                expandToMainApplication();
            } else {
                showAlert("Login Failed", "Invalid credentials!", Alert.AlertType.ERROR);
            }
        });

        // Register button action
        registerButton.setOnAction(e -> showRegistrationDialog(loginStage));

        // Enter key support
        passwordField.setOnAction(e -> loginButton.fire());
    }

    private void expandToMainApplication() {
        mainContainer = new BorderPane();
        mainContainer.getStyleClass().add("main-container");

        if (currentUser instanceof Customer) {
            showCustomerDashboard((Customer) currentUser);
        } else if (currentUser instanceof Admin) {
            showAdminDashboard((Admin) currentUser);
        }

        mainScene = new Scene(mainContainer, 1200, 800);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Online Book Store");
        primaryStage.show();
        primaryStage.centerOnScreen();

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void showRegistrationDialog(Stage ownerStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Customer Registration");
        dialog.setHeaderText("Create a new account");
        dialog.initOwner(ownerStage);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        content.getChildren().addAll(
            new Label("Username:"), usernameField,
            new Label("Password:"), passwordField,
            new Label("Address:"), addressField,
            new Label("Phone:"), phoneField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String address = addressField.getText();
                String phone = phoneField.getText();

                if (!username.isEmpty() && !password.isEmpty()) {
                    facade.registerCustomer(username, password, address, phone);
                    showAlert("Success", "Registration successful! You can now login.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Username and password are required!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showCustomerDashboard(Customer customer) {
        // Top bar
        HBox topBar = createTopBar("Welcome, " + customer.getUsername(), true);
        mainContainer.setTop(topBar);

        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab browseTab = new Tab("Browse Books", createBrowseBooksPanel(customer));
        Tab cartTab = new Tab("Shopping Cart", createCartPanel(customer));
        Tab ordersTab = new Tab("My Orders", createOrdersPanel(customer));
        Tab accountTab = new Tab("Account", createAccountPanel(customer));

        tabPane.getTabs().addAll(browseTab, cartTab, ordersTab, accountTab);

        // Refresh cart and orders when tabs are selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == cartTab) {
                cartTab.setContent(createCartPanel(customer));
            } else if (newTab == ordersTab) {
                ordersTab.setContent(createOrdersPanel(customer));
            }
        });

        mainContainer.setCenter(tabPane);
    }

    private void showAdminDashboard(Admin admin) {
        // Top bar
        HBox topBar = createTopBar("Admin Dashboard - " + admin.getUsername(), true);
        topBar.setStyle("-fx-background-color: #34495e;");
        mainContainer.setTop(topBar);

        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab booksTab = new Tab("Manage Books", createManageBooksPanel());
        Tab ordersTab = new Tab("Manage Orders", createManageOrdersPanel());
        Tab statsTab = new Tab("Statistics", createStatisticsPanel());
        Tab categoriesTab = new Tab("Categories", createManageCategoriesPanel());

        tabPane.getTabs().addAll(booksTab, ordersTab, statsTab, categoriesTab);

        mainContainer.setCenter(tabPane);
    }

    private HBox createTopBar(String text, boolean showLogout) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);

        Label label = new Label(text);
        label.getStyleClass().add("top-bar-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(label, spacer);

        if (showLogout) {
            Button logoutBtn = new Button("Logout");
            logoutBtn.getStyleClass().add("btn-danger");
            logoutBtn.setOnAction(e -> logout());
            topBar.getChildren().add(logoutBtn);
        }

        return topBar;
    }

    private void logout() {
        currentUser = null;
        primaryStage.close();
        showLoginDialog();
    }

    private ScrollPane createBrowseBooksPanel(Customer customer) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #f5f7fa;");

        // Search and filter bar - Card style
        VBox searchCard = new VBox(15);
        searchCard.getStyleClass().add("card");
        searchCard.setPadding(new Insets(20));
        
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search books...");
        searchField.setPrefWidth(300);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("All Categories");
        categoryCombo.getItems().add("All Categories");
        categoryCombo.getItems().addAll(facade.getAllCategories());
        categoryCombo.setValue("All Categories");

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Default", "Price: Low to High", "Price: High to Low", "Popularity");
        sortCombo.setValue("Default");

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");

        searchBar.getChildren().addAll(
            searchField,
            new Label("Category:"), categoryCombo,
            new Label("Sort:"), sortCombo,
            searchBtn
        );
        
        searchCard.getChildren().add(searchBar);

        // Books grid
        FlowPane booksGrid = new FlowPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(20);
        booksGrid.setPadding(new Insets(15));
        booksGrid.setStyle("-fx-background-color: transparent;");

        Runnable updateBooks = () -> {
            booksGrid.getChildren().clear();
            List<Book> books = facade.browseAllBooks();

            String searchText = searchField.getText();
            if (!searchText.isEmpty()) {
                books = facade.searchBooks(searchText);
            }

            String selectedCategory = categoryCombo.getValue();
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                books = facade.filterBooksByCategory(selectedCategory);
            }

            String sortOption = sortCombo.getValue();
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
                booksGrid.getChildren().add(createBookCard(book, customer));
            }
        };

        searchBtn.setOnAction(e -> updateBooks.run());
        categoryCombo.setOnAction(e -> updateBooks.run());
        sortCombo.setOnAction(e -> updateBooks.run());
        searchField.setOnAction(e -> updateBooks.run());

        updateBooks.run();

        container.getChildren().addAll(searchCard, booksGrid);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private VBox createBookCard(Book book, Customer customer) {
        VBox card = new VBox(12);
        card.getStyleClass().add("book-card");
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setMinHeight(450);
        card.setPadding(new Insets(20));

        // Title with badges
        VBox titleSection = new VBox(5);
        
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1a202c;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        
        HBox badgesBox = new HBox(8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);
        
        if (book.isFeatured()) {
            Label featuredBadge = new Label("⭐ FEATURED");
            featuredBadge.getStyleClass().add("featured-badge");
            badgesBox.getChildren().add(featuredBadge);
        }
        
        if (book.isDiscounted()) {
            Label discountBadge = new Label((int)book.getDiscountPercentage() + "% OFF");
            discountBadge.getStyleClass().add("discount-badge");
            badgesBox.getChildren().add(discountBadge);
        }
        
        titleSection.getChildren().addAll(titleLabel, badgesBox);

        Label authorLabel = new Label("by " + book.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-font-style: italic;");

        // Price display
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        priceBox.setPadding(new Insets(8, 0, 8, 0));

        if (book.isDiscounted()) {
            Label originalPrice = new Label("$" + String.format("%.2f", book.getOriginalPrice()));
            originalPrice.setStyle("-fx-text-fill: #a0aec0; -fx-strikethrough: true; -fx-font-size: 14px;");
            
            Label discountedPrice = new Label("$" + String.format("%.2f", book.getPrice()));
            discountedPrice.setStyle("-fx-text-fill: #eb3349; -fx-font-weight: bold; -fx-font-size: 24px;");
            
            priceBox.getChildren().addAll(discountedPrice, originalPrice);
        } else {
            Label price = new Label("$" + String.format("%.2f", book.getPrice()));
            price.setStyle("-fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #667eea;");
            priceBox.getChildren().add(price);
        }

        // Info section with icons
        VBox infoBox = new VBox(6);
        infoBox.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10; -fx-padding: 12;");
        
        Label categoryLabel = new Label("📁 " + book.getCategory());
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        
        Label stockLabel = new Label("📦 Stock: " + book.getStock());
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        
        Label popularityLabel = new Label("⭐ Rating: " + book.getPopularity());
        popularityLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568;");
        
        infoBox.getChildren().addAll(categoryLabel, stockLabel, popularityLabel);

        // Add to cart section
        VBox actionsBox = new VBox(10);
        actionsBox.setPadding(new Insets(5, 0, 0, 0));
        
        HBox cartBox = new HBox(10);
        cartBox.setAlignment(Pos.CENTER);
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, book.getStock(), 1);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.setEditable(true);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("btn-success");
        addToCartBtn.setPrefWidth(120);
        addToCartBtn.setOnAction(e -> {
            try {
                facade.addToCart(customer, book, quantitySpinner.getValue());
                showAlert("Success", "Added to cart!", Alert.AlertType.INFORMATION);
            } catch (IllegalArgumentException ex) {
                showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        cartBox.getChildren().addAll(new Label("Qty:"), quantitySpinner);
        
        VBox buttonBox = new VBox(8);
        buttonBox.getChildren().addAll(addToCartBtn);

        Button reviewsBtn = new Button("📝 Reviews");
        reviewsBtn.setPrefWidth(180);
        buttonBox.getChildren().add(reviewsBtn);
        reviewsBtn.setOnAction(e -> showReviewsDialog(book, customer));

        actionsBox.getChildren().addAll(cartBox, buttonBox);

        card.getChildren().addAll(
            titleSection, authorLabel, priceBox, infoBox,
            new Separator(),
            actionsBox
        );

        return card;
    }

    private void showReviewsDialog(Book book, Customer customer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reviews for " + book.getTitle());
        dialog.setHeaderText(null);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(550);

        // Reviews list
        VBox reviewsList = new VBox(10);
        List<Review> reviews = facade.getBookReviews(book.getId());
        
        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review!");
            noReviews.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                VBox reviewBox = new VBox(5);
                reviewBox.getStyleClass().add("card");
                reviewBox.setPadding(new Insets(10));
                
                Label reviewHeader = new Label(review.getCustomerUsername() + " - Rating: " + review.getRating() + "/5");
                reviewHeader.setStyle("-fx-font-weight: bold;");
                
                Label reviewComment = new Label(review.getComment());
                reviewComment.setWrapText(true);
                
                Label reviewDate = new Label(review.getReviewDate().toString());
                reviewDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
                
                reviewBox.getChildren().addAll(reviewHeader, reviewComment, reviewDate);
                reviewsList.getChildren().add(reviewBox);
            }
        }

        ScrollPane reviewsScroll = new ScrollPane(reviewsList);
        reviewsScroll.setFitToWidth(true);
        reviewsScroll.setPrefHeight(250);

        // Add review section
        Label addReviewLabel = new Label("Add Your Review");
        addReviewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Rating (1-5):");
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
        ratingSpinner.setPrefWidth(80);
        ratingBox.getChildren().addAll(ratingLabel, ratingSpinner);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your comment here...");
        commentArea.setPrefRowCount(3);

        Button submitBtn = new Button("Submit Review");
        submitBtn.getStyleClass().add("btn-success");
        submitBtn.setOnAction(e -> {
            String comment = commentArea.getText();
            if (!comment.isEmpty()) {
                facade.addReview(customer, book.getId(), ratingSpinner.getValue(), comment);
                showAlert("Success", "Review submitted!", Alert.AlertType.INFORMATION);
                dialog.close();
            }
        });

        content.getChildren().addAll(
            reviewsScroll,
            new Separator(),
            addReviewLabel,
            ratingBox,
            commentArea,
            submitBtn
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private ScrollPane createCartPanel(Customer customer) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("Shopping Cart");
        titleLabel.getStyleClass().add("title-label");

        VBox cartItems = new VBox(10);
        List<OrderItem> items = facade.getCartItems(customer);

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
            cartItems.getChildren().add(emptyLabel);
        } else {
            for (OrderItem item : items) {
                HBox itemBox = new HBox(20);
                itemBox.getStyleClass().add("card");
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setPadding(new Insets(15));

                VBox infoBox = new VBox(5);
                Label bookTitle = new Label(item.getBook().getTitle());
                bookTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                
                Label priceLabel = new Label("Price: $" + String.format("%.2f", item.getPriceAtPurchase()) +
                        " x " + item.getQuantity() + " = $" + String.format("%.2f", item.getSubtotal()));
                
                infoBox.getChildren().addAll(bookTitle, priceLabel);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Controls
                HBox controls = new HBox(10);
                controls.setAlignment(Pos.CENTER_RIGHT);

                Spinner<Integer> qtySpinner = new Spinner<>(1, item.getBook().getStock(), item.getQuantity());
                qtySpinner.setPrefWidth(80);

                Button updateBtn = new Button("Update");
                updateBtn.setOnAction(e -> {
                    facade.updateCartQuantity(customer, item.getBook().getId(), qtySpinner.getValue());
                    container.getChildren().clear();
                    container.getChildren().addAll(createCartPanel(customer).getContent());
                });

                Button removeBtn = new Button("Remove");
                removeBtn.getStyleClass().add("btn-danger");
                removeBtn.setOnAction(e -> {
                    facade.removeFromCart(customer, item.getBook().getId());
                    container.getChildren().clear();
                    container.getChildren().addAll(createCartPanel(customer).getContent());
                });

                controls.getChildren().addAll(new Label("Qty:"), qtySpinner, updateBtn, removeBtn);

                itemBox.getChildren().addAll(infoBox, controls);
                cartItems.getChildren().add(itemBox);
            }
        }

        // Total and checkout
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        Label totalLabel = new Label("Total: $" + String.format("%.2f", facade.getCartTotal(customer)));
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button clearBtn = new Button("Clear Cart");
        clearBtn.getStyleClass().add("btn-danger");
        clearBtn.setOnAction(e -> {
            facade.clearCart(customer);
            container.getChildren().clear();
            container.getChildren().addAll(createCartPanel(customer).getContent());
        });

        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("btn-success");
        checkoutBtn.setOnAction(e -> {
            if (customer.getCart().isEmpty()) {
                showAlert("Error", "Cart is empty!", Alert.AlertType.ERROR);
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Checkout");
            confirm.setHeaderText("Proceed to checkout?");
            confirm.setContentText("Total: $" + facade.getCartTotal(customer));

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        Order order = facade.placeOrder(customer);
                        showAlert("Success", "Order placed successfully!\nOrder ID: " + order.getOrderId(), 
                                Alert.AlertType.INFORMATION);
                        container.getChildren().clear();
                        container.getChildren().addAll(createCartPanel(customer).getContent());
                    } catch (IllegalStateException ex) {
                        showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        });

        bottomBox.getChildren().addAll(totalLabel, clearBtn, checkoutBtn);

        container.getChildren().addAll(titleLabel, cartItems, bottomBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createOrdersPanel(Customer customer) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("My Orders");
        titleLabel.getStyleClass().add("title-label");

        VBox ordersList = new VBox(15);
        List<Order> orders = facade.getCustomerOrderHistory(customer);

        if (orders.isEmpty()) {
            Label emptyLabel = new Label("No orders yet");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
            ordersList.getChildren().add(emptyLabel);
        } else {
            for (Order order : orders) {
                VBox orderBox = new VBox(10);
                orderBox.getStyleClass().add("card");
                orderBox.setPadding(new Insets(15));

                Label orderHeader = new Label("Order ID: " + order.getOrderId());
                orderHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                Label statusLabel = new Label("Status: " + order.getStatus());
                statusLabel.getStyleClass().add("status-" + order.getStatus().toLowerCase());

                Label dateLabel = new Label("Date: " + order.getOrderDate());
                Label totalLabel = new Label("Total: $" + String.format("%.2f", order.getTotalAmount()));
                totalLabel.setStyle("-fx-font-weight: bold;");

                VBox itemsBox = new VBox(5);
                Label itemsHeader = new Label("Items:");
                itemsHeader.setStyle("-fx-font-weight: bold;");
                itemsBox.getChildren().add(itemsHeader);

                for (OrderItem item : order.getItems()) {
                    Label itemLabel = new Label("  • " + item.toString());
                    itemsBox.getChildren().add(itemLabel);
                }

                orderBox.getChildren().addAll(orderHeader, statusLabel, dateLabel, totalLabel, itemsBox);

                if (order.getStatus().equals("PENDING")) {
                    Button cancelBtn = new Button("Cancel Order");
                    cancelBtn.getStyleClass().add("btn-danger");
                    cancelBtn.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Cancel Order");
                        confirm.setHeaderText("Cancel this order?");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                facade.cancelOrder(customer, order.getOrderId());
                                container.getChildren().clear();
                                container.getChildren().addAll(createOrdersPanel(customer).getContent());
                            }
                        });
                    });
                    orderBox.getChildren().add(cancelBtn);
                }

                ordersList.getChildren().add(orderBox);
            }
        }

        container.getChildren().addAll(titleLabel, ordersList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createAccountPanel(Customer customer) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(600);

        Label titleLabel = new Label("Account Information");
        titleLabel.getStyleClass().add("title-label");

        VBox formBox = new VBox(15);
        formBox.getStyleClass().add("card");
        formBox.setPadding(new Insets(30));

        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("field-label");
        Label usernameValue = new Label(customer.getUsername());
        usernameValue.setStyle("-fx-font-size: 16px;");

        Label addressLabel = new Label("Address:");
        addressLabel.getStyleClass().add("field-label");
        TextField addressField = new TextField(customer.getAddress());

        Label phoneLabel = new Label("Phone:");
        phoneLabel.getStyleClass().add("field-label");
        TextField phoneField = new TextField(customer.getPhone());

        Button updateBtn = new Button("Update Information");
        updateBtn.getStyleClass().add("btn-primary");
        updateBtn.setOnAction(e -> {
            facade.updateCustomerInfo(customer, addressField.getText(), phoneField.getText());
            showAlert("Success", "Information updated successfully!", Alert.AlertType.INFORMATION);
        });

        formBox.getChildren().addAll(
            usernameLabel, usernameValue,
            new Separator(),
            addressLabel, addressField,
            phoneLabel, phoneField,
            updateBtn
        );

        container.getChildren().addAll(titleLabel, formBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // Admin panels
    private ScrollPane createManageBooksPanel() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Manage Books");
        titleLabel.getStyleClass().add("title-label");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button addBookBtn = new Button("Add New Book");
        addBookBtn.getStyleClass().add("btn-success");
        addBookBtn.setOnAction(e -> showAddBookDialog(container));

        headerBox.getChildren().addAll(titleLabel, addBookBtn);

        VBox booksList = new VBox(10);
        updateBooksListAdmin(booksList, container);

        container.getChildren().addAll(headerBox, booksList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void updateBooksListAdmin(VBox booksList, VBox container) {
        booksList.getChildren().clear();
        List<Book> books = facade.browseAllBooks();

        for (Book book : books) {
            HBox bookBox = new HBox(20);
            bookBox.getStyleClass().add("card");
            bookBox.setAlignment(Pos.CENTER_LEFT);
            bookBox.setPadding(new Insets(15));

            VBox infoBox = new VBox(5);
            
            HBox titleBox = new HBox(10);
            titleBox.setAlignment(Pos.CENTER_LEFT);
            
            Label titleLabel = new Label(book.getTitle() + " by " + book.getAuthor());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            titleBox.getChildren().add(titleLabel);
            
            // Show decorator badges
            if (book.isFeatured()) {
                Label featuredBadge = new Label("⭐ FEATURED");
                featuredBadge.getStyleClass().add("featured-badge");
                titleBox.getChildren().add(featuredBadge);
            }
            
            if (book.isDiscounted()) {
                Label discountBadge = new Label((int)book.getDiscountPercentage() + "% OFF");
                discountBadge.getStyleClass().add("discount-badge");
                titleBox.getChildren().add(discountBadge);
            }
            
            String priceDisplay = "Price: $" + String.format("%.2f", book.getPrice());
            if (book.isDiscounted()) {
                priceDisplay += " (Original: $" + String.format("%.2f", book.getOriginalPrice()) + ")";
            }
            
            Label detailsLabel = new Label(priceDisplay +
                    " | Category: " + book.getCategory() + " | Stock: " + book.getStock());
            
            infoBox.getChildren().addAll(titleBox, detailsLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox controls = new HBox(10);
            controls.setAlignment(Pos.CENTER_RIGHT);

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("btn-primary");
            editBtn.setOnAction(e -> showEditBookDialog(book, booksList, container));

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Book");
                confirm.setHeaderText("Delete: " + book.getTitle() + "?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        facade.deleteBook(book.getId());
                        updateBooksListAdmin(booksList, container);
                    }
                });
            });

            controls.getChildren().addAll(editBtn, deleteBtn);
            bookBox.getChildren().addAll(infoBox, controls);
            booksList.getChildren().add(bookBox);
        }
    }

    private void showAddBookDialog(VBox container) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter book details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField idField = new TextField();
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField priceField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(facade.getAllCategories());
        TextField stockField = new TextField();
        TextField editionField = new TextField();
        TextField coverField = new TextField("default.jpg");
        CheckBox featuredCheck = new CheckBox("Featured");
        CheckBox discountCheck = new CheckBox("Discounted");
        TextField discountField = new TextField("0");
        discountField.setDisable(true);

        discountCheck.setOnAction(e -> discountField.setDisable(!discountCheck.isSelected()));

        int row = 0;
        grid.add(new Label("Book ID:"), 0, row);
        grid.add(idField, 1, row++);
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Author:"), 0, row);
        grid.add(authorField, 1, row++);
        grid.add(new Label("Price:"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Stock:"), 0, row);
        grid.add(stockField, 1, row++);
        grid.add(new Label("Edition:"), 0, row);
        grid.add(editionField, 1, row++);
        grid.add(new Label("Cover Image:"), 0, row);
        grid.add(coverField, 1, row++);
        grid.add(featuredCheck, 0, row);
        grid.add(discountCheck, 1, row++);
        grid.add(new Label("Discount %:"), 0, row);
        grid.add(discountField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    BasicBook book = new BasicBook(
                        idField.getText(),
                        titleField.getText(),
                        authorField.getText(),
                        Double.parseDouble(priceField.getText()),
                        categoryCombo.getValue(),
                        Integer.parseInt(stockField.getText()),
                        editionField.getText(),
                        coverField.getText()
                    );

                    // Set decorator properties directly on BasicBook
                    book.setFeatured(featuredCheck.isSelected());
                    
                    if (discountCheck.isSelected()) {
                        double discount = Double.parseDouble(discountField.getText()) / 100.0;
                        book.setDiscountPercentage(discount);
                    }

                    facade.addBookWithDecorators(book);
                    showAlert("Success", "Book added successfully!", Alert.AlertType.INFORMATION);
                    
                    // Refresh the books list
                    VBox booksList = (VBox) container.getChildren().get(1);
                    updateBooksListAdmin(booksList, container);
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid price, stock, or discount value!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showEditBookDialog(Book book, VBox booksList, VBox container) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit book details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Get base book to edit
        BasicBook baseBook = book.getBaseBook();

        TextField titleField = new TextField(baseBook.getTitle());
        TextField authorField = new TextField(baseBook.getAuthor());
        TextField priceField = new TextField(String.valueOf(baseBook.getOriginalPrice()));
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(facade.getAllCategories());
        categoryCombo.setValue(baseBook.getCategory());
        TextField stockField = new TextField(String.valueOf(baseBook.getStock()));
        TextField editionField = new TextField(baseBook.getEdition());
        TextField coverField = new TextField(baseBook.getCoverImage());
        CheckBox featuredCheck = new CheckBox("Featured");
        featuredCheck.setSelected(book.isFeatured());
        CheckBox discountCheck = new CheckBox("Discounted");
        discountCheck.setSelected(book.isDiscounted());
        TextField discountField = new TextField(String.valueOf((int)book.getDiscountPercentage()));
        discountField.setDisable(!book.isDiscounted());

        discountCheck.setOnAction(e -> discountField.setDisable(!discountCheck.isSelected()));

        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Author:"), 0, row);
        grid.add(authorField, 1, row++);
        grid.add(new Label("Price:"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Stock:"), 0, row);
        grid.add(stockField, 1, row++);
        grid.add(new Label("Edition:"), 0, row);
        grid.add(editionField, 1, row++);
        grid.add(new Label("Cover Image:"), 0, row);
        grid.add(coverField, 1, row++);
        grid.add(featuredCheck, 0, row);
        grid.add(discountCheck, 1, row++);
        grid.add(new Label("Discount %:"), 0, row);
        grid.add(discountField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Update the base book fields
                    baseBook.setTitle(titleField.getText());
                    baseBook.setAuthor(authorField.getText());
                    baseBook.setPrice(Double.parseDouble(priceField.getText()));
                    baseBook.setCategory(categoryCombo.getValue());
                    baseBook.setStock(Integer.parseInt(stockField.getText()));
                    baseBook.setEdition(editionField.getText());
                    baseBook.setCoverImage(coverField.getText());
                    baseBook.setFeatured(featuredCheck.isSelected());
                    
                    if (discountCheck.isSelected()) {
                        double discount = Double.parseDouble(discountField.getText()) / 100.0;
                        baseBook.setDiscountPercentage(discount);
                    } else {
                        baseBook.setDiscountPercentage(0.0);
                    }

                    showAlert("Success", "Book updated successfully!", Alert.AlertType.INFORMATION);
                    updateBooksListAdmin(booksList, container);
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid price, stock, or discount value!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private ScrollPane createManageOrdersPanel() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Manage Orders");
        titleLabel.getStyleClass().add("title-label");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Orders", "Pending", "Confirmed", "Shipped", "Cancelled");
        filterCombo.setValue("All Orders");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            VBox ordersList = (VBox) container.getChildren().get(1);
            updateOrdersListAdmin(ordersList, container, filterCombo.getValue());
        });

        headerBox.getChildren().addAll(titleLabel, new Label("Filter:"), filterCombo, refreshBtn);

        VBox ordersList = new VBox(15);
        filterCombo.setOnAction(e -> updateOrdersListAdmin(ordersList, container, filterCombo.getValue()));
        updateOrdersListAdmin(ordersList, container, "All Orders");

        container.getChildren().addAll(headerBox, ordersList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void updateOrdersListAdmin(VBox ordersList, VBox container, String filter) {
        ordersList.getChildren().clear();
        List<Order> orders = facade.getAllOrders();

        if (!filter.equals("All Orders")) {
            final String status = filter.toUpperCase();
            orders = orders.stream()
                    .filter(o -> o.getStatus().equals(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        for (Order order : orders) {
            VBox orderBox = new VBox(10);
            orderBox.getStyleClass().add("card");
            orderBox.setPadding(new Insets(15));

            Label orderHeader = new Label("Order ID: " + order.getOrderId());
            orderHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Label customerLabel = new Label("Customer: " + order.getCustomer().getUsername());
            Label statusLabel = new Label("Status: " + order.getStatus());
            statusLabel.getStyleClass().add("status-" + order.getStatus().toLowerCase());

            Label dateLabel = new Label("Date: " + order.getOrderDate());
            Label totalLabel = new Label("Total: $" + String.format("%.2f", order.getTotalAmount()));
            totalLabel.setStyle("-fx-font-weight: bold;");

            VBox itemsBox = new VBox(5);
            Label itemsHeader = new Label("Items:");
            itemsHeader.setStyle("-fx-font-weight: bold;");
            itemsBox.getChildren().add(itemsHeader);

            for (OrderItem item : order.getItems()) {
                Label itemLabel = new Label("  • " + item.toString());
                itemsBox.getChildren().add(itemLabel);
            }

            HBox actionsBox = new HBox(10);
            actionsBox.setAlignment(Pos.CENTER_LEFT);

            if (order.getStatus().equals("PENDING")) {
                Button confirmBtn = new Button("Confirm");
                confirmBtn.getStyleClass().add("btn-success");
                confirmBtn.setOnAction(e -> {
                    facade.confirmOrder(order.getOrderId());
                    updateOrdersListAdmin(ordersList, container, filter);
                });

                Button cancelBtn = new Button("Cancel");
                cancelBtn.getStyleClass().add("btn-danger");
                cancelBtn.setOnAction(e -> {
                    facade.cancelOrderByAdmin(order.getOrderId());
                    updateOrdersListAdmin(ordersList, container, filter);
                });

                actionsBox.getChildren().addAll(confirmBtn, cancelBtn);
            }

            if (order.getStatus().equals("CONFIRMED")) {
                Button shipBtn = new Button("Mark as Shipped");
                shipBtn.getStyleClass().add("btn-primary");
                shipBtn.setOnAction(e -> {
                    facade.shipOrder(order.getOrderId());
                    updateOrdersListAdmin(ordersList, container, filter);
                });
                actionsBox.getChildren().add(shipBtn);
            }

            orderBox.getChildren().addAll(orderHeader, customerLabel, statusLabel, dateLabel, totalLabel, itemsBox);
            
            if (!actionsBox.getChildren().isEmpty()) {
                orderBox.getChildren().add(actionsBox);
            }

            ordersList.getChildren().add(orderBox);
        }
    }

    private ScrollPane createStatisticsPanel() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label titleLabel = new Label("Statistics");
        titleLabel.getStyleClass().add("title-label");

        VBox statsBox = new VBox(15);
        statsBox.getStyleClass().add("card");
        statsBox.setPadding(new Insets(25));

        Label revenueLabel = new Label("Total Revenue: $" + 
                String.format("%.2f", facade.getTotalRevenue()));
        revenueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label ordersLabel = new Label("Total Orders: " + facade.getTotalOrdersCount());
        ordersLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

        Label topBooksLabel = new Label("Top 5 Selling Books:");
        topBooksLabel.getStyleClass().add("subtitle-label");

        VBox topBooksList = new VBox(5);
        for (Book book : facade.getTopSellingBooks(5)) {
            Label bookLabel = new Label("• " + book.getTitle() + " (Popularity: " + book.getPopularity() + ")");
            topBooksList.getChildren().add(bookLabel);
        }

        Label categorySalesLabel = new Label("Sales by Category:");
        categorySalesLabel.getStyleClass().add("subtitle-label");

        VBox categorySalesList = new VBox(5);
        for (java.util.Map.Entry<String, Integer> entry : facade.getCategorySalesStatistics().entrySet()) {
            Label categoryLabel = new Label("• " + entry.getKey() + ": " + entry.getValue() + " books sold");
            categorySalesList.getChildren().add(categoryLabel);
        }

        Button refreshBtn = new Button("Refresh Statistics");
        refreshBtn.getStyleClass().add("btn-primary");
        refreshBtn.setOnAction(e -> {
            mainContainer.setCenter(createStatisticsPanel());
        });

        statsBox.getChildren().addAll(
            revenueLabel, ordersLabel,
            new Separator(),
            topBooksLabel, topBooksList,
            new Separator(),
            categorySalesLabel, categorySalesList,
            new Separator(),
            refreshBtn
        );

        container.getChildren().addAll(titleLabel, statsBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private ScrollPane createManageCategoriesPanel() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("Manage Categories");
        titleLabel.getStyleClass().add("title-label");

        HBox addCategoryBox = new HBox(15);
        addCategoryBox.setAlignment(Pos.CENTER_LEFT);

        TextField newCategoryField = new TextField();
        newCategoryField.setPromptText("New category name");
        newCategoryField.setPrefWidth(250);

        Button addBtn = new Button("Add Category");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setOnAction(e -> {
            String newCategory = newCategoryField.getText().trim();
            if (!newCategory.isEmpty()) {
                facade.addCategory(newCategory);
                newCategoryField.clear();
                VBox categoriesList = (VBox) container.getChildren().get(2);
                updateCategoriesList(categoriesList);
                showAlert("Success", "Category added!", Alert.AlertType.INFORMATION);
            }
        });

        addCategoryBox.getChildren().addAll(new Label("New Category:"), newCategoryField, addBtn);

        VBox categoriesList = new VBox(10);
        updateCategoriesList(categoriesList);

        container.getChildren().addAll(titleLabel, addCategoryBox, categoriesList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void updateCategoriesList(VBox categoriesList) {
        categoriesList.getChildren().clear();
        for (String category : facade.getAllCategories()) {
            Label categoryLabel = new Label("• " + category);
            categoryLabel.setStyle("-fx-font-size: 16px; -fx-padding: 5 10;");
            categoriesList.getChildren().add(categoryLabel);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
