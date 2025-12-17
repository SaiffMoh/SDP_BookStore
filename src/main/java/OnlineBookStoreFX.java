import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OnlineBookStoreFX extends Application {
    private BookStoreFacade facade;
    private String currentUsername;
    private String currentUserType;
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
            facade.saveAllData();
            System.out.println("Goodbye!");
        }));

        // Show login dialog first
        showLoginDialog();
    }

    private void showLoginDialog() {
        Stage loginStage = new Stage();
        loginStage.initStyle(StageStyle.UNDECORATED);
        
        VBox loginBox = new VBox(24);
        loginBox.getStyleClass().add("login-dialog");
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(60, 80, 60, 80));
        
        // Close button
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #86868b; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 8;");
        closeBtn.setOnAction(e -> {
            loginStage.close();
            Platform.exit();
        });
        StackPane closeContainer = new StackPane(closeBtn);
        closeContainer.setAlignment(Pos.TOP_RIGHT);
        closeContainer.setPadding(new Insets(-40, -60, 0, 0));

        // Icon/Logo
        Label iconLabel = new Label("📚");
        iconLabel.setStyle("-fx-font-size: 56px;");
        
        Label titleLabel = new Label("Bookstore");
        titleLabel.getStyleClass().add("login-title");
        
        Label subtitleLabel = new Label("Sign in to your account");
        subtitleLabel.getStyleClass().add("login-subtitle");

        VBox formBox = new VBox(16);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setMaxWidth(320);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("field-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("field-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");

        formBox.getChildren().addAll(
            usernameLabel, usernameField,
            passwordLabel, passwordField
        );

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setPrefWidth(154);
        loginButton.setPrefHeight(44);
        
        Button registerButton = new Button("Create Account");
        registerButton.getStyleClass().add("btn-success");
        registerButton.setPrefWidth(154);
        registerButton.setPrefHeight(44);

        buttonBox.getChildren().addAll(loginButton, registerButton);

        loginBox.getChildren().addAll(closeContainer, iconLabel, titleLabel, subtitleLabel, formBox, buttonBox);

        Scene loginScene = new Scene(loginBox, 520, 600);
        loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        loginStage.setScene(loginScene);
        loginStage.centerOnScreen();
        loginStage.show();

        // Login button action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            String userType = facade.login(username, password);
            if (userType != null) {
                currentUsername = username;
                currentUserType = userType;
                loginStage.close();
                expandToMainApplication();
            } else {
                showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
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

        mainScene = new Scene(mainContainer, 1400, 900);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        if ("CUSTOMER".equals(currentUserType)) {
            showCustomerDashboard();
        } else if ("ADMIN".equals(currentUserType)) {
            showAdminDashboard();
        }

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Bookstore");
        primaryStage.show();
        primaryStage.centerOnScreen();

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void showRegistrationDialog(Stage ownerStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.setHeaderText("Register a new customer account");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(24));
        grid.setMaxWidth(400);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        TextField addressField = new TextField();
        addressField.setPromptText("Your address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Your phone number");

        int row = 0;
        grid.add(new Label("Username"), 0, row);
        grid.add(usernameField, 1, row++);
        grid.add(new Label("Password"), 0, row);
        grid.add(passwordField, 1, row++);
        grid.add(new Label("Address"), 0, row);
        grid.add(addressField, 1, row++);
        grid.add(new Label("Phone"), 0, row);
        grid.add(phoneField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String username = usernameField.getText();
                String password = passwordField.getText();
                String address = addressField.getText();
                String phone = phoneField.getText();

                if (!username.isEmpty() && !password.isEmpty()) {
                    facade.registerCustomer(username, password, address, phone);
                    showAlert("Success", "Account created successfully! You can now sign in.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Username and password are required", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showCustomerDashboard() {
        // Modern top navigation bar
        HBox topBar = new HBox(20);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 32, 16, 32));

        Label logoLabel = new Label("📚 Bookstore");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #1d1d1f;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Navigation buttons
        Button browseBtn = new Button("Browse");
        browseBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0071e3; -fx-font-size: 15px; -fx-cursor: hand; -fx-font-weight: 500;");
        browseBtn.setOnAction(e -> showBrowseBooksScreen());

        Button ordersBtn = new Button("Orders");
        ordersBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d1d1f; -fx-font-size: 15px; -fx-cursor: hand;");
        ordersBtn.setOnAction(e -> showOrdersScreen());

        Button cartBtn = new Button("Cart");
        cartBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d1d1f; -fx-font-size: 15px; -fx-cursor: hand;");
        cartBtn.setOnAction(e -> showCartScreen());

        Button accountBtn = new Button("Account");
        accountBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d1d1f; -fx-font-size: 15px; -fx-cursor: hand;");
        accountBtn.setOnAction(e -> showAccountScreen());

        Separator sep = new Separator(Orientation.VERTICAL);
        sep.setPrefHeight(20);
        sep.setStyle("-fx-background-color: #e5e5e7;");

        Label usernameLabel = new Label(currentUsername);
        usernameLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setPrefHeight(36);
        logoutBtn.setOnAction(e -> logout());
        
        Button fullscreenBtn = new Button("⛶");
        fullscreenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d1d1f; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 8;");
        fullscreenBtn.setOnAction(e -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff3b30; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 8;");
        closeBtn.setOnAction(e -> {
            Platform.exit();
        });

        topBar.getChildren().addAll(logoLabel, spacer, browseBtn, ordersBtn, cartBtn, accountBtn, sep, usernameLabel, logoutBtn, fullscreenBtn, closeBtn);
        mainContainer.setTop(topBar);

        // Main content - Browse Books
        showBrowseBooksScreen();
    }

    private void showBrowseBooksScreen() {
        mainContainer.setCenter(createBrowseBooksPanel());
    }

    private void showCartScreen() {
        ScrollPane cartContent = createCartPanel();
        mainContainer.setCenter(cartContent);
    }

    private void showOrdersScreen() {
        ScrollPane ordersContent = createOrdersPanel();
        mainContainer.setCenter(ordersContent);
    }

    private void showAccountScreen() {
        ScrollPane accountContent = createAccountPanel();
        mainContainer.setCenter(accountContent);
    }

    private void showAdminDashboard() {
        // Admin top bar
        HBox topBar = new HBox(20);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 32, 16, 32));

        Label logoLabel = new Label("📚 Bookstore Admin");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #1d1d1f;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminLabel = new Label(currentUsername);
        adminLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setPrefHeight(36);
        logoutBtn.setOnAction(e -> logout());
        
        Button fullscreenBtn = new Button("⛶");
        fullscreenBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1d1d1f; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 8;");
        fullscreenBtn.setOnAction(e -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));
        
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff3b30; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 8;");
        closeBtn.setOnAction(e -> {
            Platform.exit();
        });

        topBar.getChildren().addAll(logoLabel, spacer, adminLabel, logoutBtn, fullscreenBtn, closeBtn);
        mainContainer.setTop(topBar);

        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab booksTab = new Tab("Books", createManageBooksPanel());
        Tab ordersTab = new Tab("Orders", createManageOrdersPanel());
        Tab statsTab = new Tab("Statistics", createStatisticsPanel());
        Tab categoriesTab = new Tab("Categories", createManageCategoriesPanel());

        tabPane.getTabs().addAll(booksTab, ordersTab, statsTab, categoriesTab);

        mainContainer.setCenter(tabPane);
    }

    private void logout() {
        facade.logout();
        currentUsername = null;
        currentUserType = null;
        primaryStage.close();
        showLoginDialog();
    }

    private ScrollPane createBrowseBooksPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        // Search and filter section
        VBox searchSection = new VBox(16);
        searchSection.getStyleClass().add("card");
        searchSection.setPadding(new Insets(20));
        
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search books by title or author...");
        searchField.setPrefWidth(400);
        searchField.setPrefHeight(40);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("All Categories");
        categoryCombo.getItems().add("All Categories");
        categoryCombo.getItems().addAll(facade.getAllCategories());
        categoryCombo.setValue("All Categories");
        categoryCombo.setPrefHeight(40);

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Default", "Price: Low to High", "Price: High to Low", "Popularity");
        sortCombo.setValue("Default");
        sortCombo.setPrefHeight(40);

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");
        searchBtn.setPrefHeight(40);

        searchRow.getChildren().addAll(searchField, categoryCombo, sortCombo, searchBtn);
        searchSection.getChildren().add(searchRow);

        // Books grid - Compact 4-column layout
        GridPane booksGrid = new GridPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(20);
        booksGrid.setPadding(new Insets(8, 0, 0, 0));

        Runnable updateBooks = () -> {
            booksGrid.getChildren().clear();
            List<Map<String, Object>> books = facade.browseAllBooks();

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

            int col = 0;
            int row = 0;
            for (Map<String, Object> book : books) {
                VBox bookCard = createCompactBookCard(book);
                booksGrid.add(bookCard, col, row);
                col++;
                if (col == 4) {
                    col = 0;
                    row++;
                }
            }
        };

        searchBtn.setOnAction(e -> updateBooks.run());
        categoryCombo.setOnAction(e -> updateBooks.run());
        sortCombo.setOnAction(e -> updateBooks.run());
        searchField.setOnAction(e -> updateBooks.run());

        updateBooks.run();

        container.getChildren().addAll(searchSection, booksGrid);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private VBox createCompactBookCard(Map<String, Object> bookDTO) {
        VBox card = new VBox(12);
        card.getStyleClass().add("book-card");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setPadding(new Insets(16));

        // Extract book properties from DTO
        String bookId = (String) bookDTO.get("id");
        String title = (String) bookDTO.get("title");
        String author = (String) bookDTO.get("author");
        double price = (double) bookDTO.get("price");
        double originalPrice = (double) bookDTO.get("originalPrice");
        int stock = (int) bookDTO.get("stock");
        String coverImage = (String) bookDTO.get("coverImage");
        int popularity = (int) bookDTO.get("popularity");
        boolean isFeatured = (boolean) bookDTO.get("isFeatured");
        boolean isDiscounted = (boolean) bookDTO.get("isDiscounted");
        double discountPercentage = (double) bookDTO.get("discountPercentage");

        // Book cover image - smaller and cleaner
        ImageView coverImageView = new ImageView();
        coverImageView.setFitWidth(268);
        coverImageView.setFitHeight(160);
        coverImageView.setPreserveRatio(true);
        coverImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        try {
            String imagePath = "file:bookstore_data/images/" + coverImage;
            Image image = new Image(imagePath, true);
            if (!image.isError()) {
                coverImageView.setImage(image);
            } else {
                coverImageView.setImage(createPlaceholderImage());
            }
        } catch (Exception e) {
            coverImageView.setImage(createPlaceholderImage());
        }
        
        StackPane imageContainer = new StackPane(coverImageView);
        imageContainer.setStyle("-fx-background-color: #f5f5f7; -fx-background-radius: 8;");
        imageContainer.setPadding(new Insets(8));

        // Title and author
        VBox infoBox = new VBox(4);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 15px; -fx-text-fill: #1d1d1f;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(268);
        
        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 13px;");
        authorLabel.setMaxWidth(268);

        infoBox.getChildren().addAll(titleLabel, authorLabel);

        // Badges
        HBox badgesBox = new HBox(6);
        badgesBox.setAlignment(Pos.CENTER_LEFT);
        
        if (isFeatured) {
            Label featuredBadge = new Label("FEATURED");
            featuredBadge.getStyleClass().add("featured-badge");
            badgesBox.getChildren().add(featuredBadge);
        }
        
        if (isDiscounted) {
            Label discountBadge = new Label((int)discountPercentage + "% OFF");
            discountBadge.getStyleClass().add("discount-badge");
            badgesBox.getChildren().add(discountBadge);
        }

        // Price
        HBox priceBox = new HBox(8);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        if (isDiscounted) {
            Label discountedPrice = new Label("$" + String.format("%.2f", price));
            discountedPrice.setStyle("-fx-text-fill: #ff3b30; -fx-font-weight: 600; -fx-font-size: 20px;");
            
            Label originalPriceLabel = new Label("$" + String.format("%.2f", originalPrice));
            originalPriceLabel.setStyle("-fx-text-fill: #86868b; -fx-strikethrough: true; -fx-font-size: 13px;");
            
            priceBox.getChildren().addAll(discountedPrice, originalPriceLabel);
        } else {
            Label priceLabel = new Label("$" + String.format("%.2f", price));
            priceLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 20px; -fx-text-fill: #1d1d1f;");
            priceBox.getChildren().add(priceLabel);
        }

        // Compact info
        HBox metaBox = new HBox(16);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        
        Label stockLabel = new Label("Stock: " + stock);
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");
        
        Label ratingLabel = new Label("⭐ " + popularity);
        ratingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");
        
        metaBox.getChildren().addAll(stockLabel, ratingLabel);

        // Add to cart section
        HBox cartBox = new HBox(8);
        cartBox.setAlignment(Pos.CENTER_LEFT);
        
        Label qtyLabel = new Label("Qty");
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, stock, 1);
        quantitySpinner.setPrefWidth(70);
        quantitySpinner.setPrefHeight(36);
        quantitySpinner.setEditable(true);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("btn-success");
        addToCartBtn.setPrefHeight(36);
        HBox.setHgrow(addToCartBtn, Priority.ALWAYS);
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setOnAction(e -> {
            try {
                facade.addToCart(bookId, quantitySpinner.getValue());
                showAlert("Added to Cart", title + " has been added to your cart", Alert.AlertType.INFORMATION);
            } catch (IllegalArgumentException ex) {
                showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        cartBox.getChildren().addAll(qtyLabel, quantitySpinner, addToCartBtn);

        Button reviewsBtn = new Button("Reviews");
        reviewsBtn.setPrefHeight(36);
        reviewsBtn.setMaxWidth(Double.MAX_VALUE);
        reviewsBtn.setOnAction(e -> showReviewsDialog(bookId, title));

        card.getChildren().addAll(
            imageContainer,
            infoBox,
            badgesBox,
            priceBox,
            metaBox,
            cartBox,
            reviewsBtn
        );

        return card;
    }

    private Image createPlaceholderImage() {
        try {
            return new Image("file:bookstore_data/images/placeholder.png", true);
        } catch (Exception e) {
            return null;
        }
    }

    private void showReviewsDialog(String bookId, String bookTitle) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reviews");
        dialog.setHeaderText(bookTitle);

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setPrefWidth(600);

        // Reviews list
        VBox reviewsList = new VBox(12);
        List<Map<String, Object>> reviews = facade.getBookReviews(bookId);
        
        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review this book!");
            noReviews.setStyle("-fx-font-style: italic; -fx-text-fill: #86868b;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Map<String, Object> review : reviews) {
                VBox reviewBox = new VBox(8);
                reviewBox.getStyleClass().add("card");
                reviewBox.setPadding(new Insets(16));
                
                HBox headerBox = new HBox(12);
                headerBox.setAlignment(Pos.CENTER_LEFT);
                
                Label usernameLabel = new Label((String) review.get("customerUsername"));
                usernameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 15px;");
                
                Label ratingLabel = new Label("⭐".repeat((int) review.get("rating")));
                ratingLabel.setStyle("-fx-font-size: 14px;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label dateLabel = new Label((String) review.get("reviewDate"));
                dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");
                
                headerBox.getChildren().addAll(usernameLabel, ratingLabel, spacer, dateLabel);
                
                Label commentLabel = new Label((String) review.get("comment"));
                commentLabel.setWrapText(true);
                commentLabel.setStyle("-fx-font-size: 15px;");
                
                reviewBox.getChildren().addAll(headerBox, commentLabel);
                reviewsList.getChildren().add(reviewBox);
            }
        }

        ScrollPane reviewsScroll = new ScrollPane(reviewsList);
        reviewsScroll.setFitToWidth(true);
        reviewsScroll.setPrefHeight(300);
        reviewsScroll.setStyle("-fx-background-color: transparent;");

        // Add review section
        VBox addReviewBox = new VBox(12);
        addReviewBox.getStyleClass().add("card");
        addReviewBox.setPadding(new Insets(16));

        Label addReviewLabel = new Label("Write a Review");
        addReviewLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 15px;");

        HBox ratingBox = new HBox(12);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Rating");
        Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
        ratingSpinner.setPrefWidth(80);
        ratingSpinner.setPrefHeight(36);
        ratingBox.getChildren().addAll(ratingLabel, ratingSpinner);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your thoughts about this book...");
        commentArea.setPrefRowCount(4);

        Button submitBtn = new Button("Submit Review");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setPrefHeight(40);
        submitBtn.setOnAction(e -> {
            String comment = commentArea.getText();
            if (!comment.isEmpty()) {
                facade.addReview(bookId, ratingSpinner.getValue(), comment);
                showAlert("Success", "Your review has been submitted", Alert.AlertType.INFORMATION);
                dialog.close();
            }
        });

        addReviewBox.getChildren().addAll(addReviewLabel, ratingBox, commentArea, submitBtn);

        content.getChildren().addAll(reviewsScroll, addReviewBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private ScrollPane createCartPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        Label titleLabel = new Label("Shopping Cart");
        titleLabel.getStyleClass().add("title-label");

        VBox cartItems = new VBox(12);

        List<Map<String, Object>> items = facade.getCartItems();

        if (items.isEmpty()) {
            VBox emptyBox = new VBox(16);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            emptyBox.getStyleClass().add("card");
            
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #86868b;");
            
            Label emptySubLabel = new Label("Add some books to get started");
            emptySubLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #86868b;");
            
            emptyBox.getChildren().addAll(emptyLabel, emptySubLabel);
            cartItems.getChildren().add(emptyBox);
        } else {
            for (Map<String, Object> item : items) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bookDTO = (Map<String, Object>) item.get("book");
                int quantity = (int) item.get("quantity");
                double subtotal = (double) item.get("subtotal");
                
                String bookId = (String) bookDTO.get("id");
                String bookTitle = (String) bookDTO.get("title");
                String bookAuthor = (String) bookDTO.get("author");
                double priceAtPurchase = (double) bookDTO.get("price");
                int stock = (int) bookDTO.get("stock");
                
                HBox itemBox = new HBox(20);
                itemBox.getStyleClass().add("card");
                itemBox.setAlignment(Pos.CENTER_LEFT);
                itemBox.setPadding(new Insets(16));

                VBox infoBox = new VBox(6);
                Label bookTitleLabel = new Label(bookTitle);
                bookTitleLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 17px;");
                
                Label authorLabel = new Label("by " + bookAuthor);
                authorLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");
                
                Label priceLabel = new Label("$" + String.format("%.2f", priceAtPurchase) + " each");
                priceLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");
                
                infoBox.getChildren().addAll(bookTitleLabel, authorLabel, priceLabel);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                // Controls
                HBox controls = new HBox(12);
                controls.setAlignment(Pos.CENTER_RIGHT);

                Label qtyLabel = new Label("Qty");
                qtyLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 13px;");

                // Ensure max is at least equal to current quantity to avoid spinner issues
                int maxQty = Math.max(stock, quantity);
                Spinner<Integer> qtySpinner = new Spinner<>(1, maxQty, quantity);
                qtySpinner.setPrefWidth(80);
                qtySpinner.setPrefHeight(36);

                Button updateBtn = new Button("Update");
                updateBtn.setPrefHeight(36);
                updateBtn.setOnAction(e -> {
                    facade.updateCartQuantity(bookId, qtySpinner.getValue());
                    mainContainer.setCenter(createCartPanel());
                });

                Label subtotalLabel = new Label("$" + String.format("%.2f", subtotal));
                subtotalLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 20px; -fx-text-fill: #1d1d1f; -fx-min-width: 80; -fx-alignment: center-right;");

                Button removeBtn = new Button("Remove");
                removeBtn.getStyleClass().add("btn-danger");
                removeBtn.setPrefHeight(36);
                removeBtn.setOnAction(e -> {
                    facade.removeFromCart(bookId);
                    mainContainer.setCenter(createCartPanel());
                });

                controls.getChildren().addAll(qtyLabel, qtySpinner, updateBtn, subtotalLabel, removeBtn);

                itemBox.getChildren().addAll(infoBox, controls);
                cartItems.getChildren().add(itemBox);
            }
        }

        // Total and checkout
        HBox bottomBox = new HBox(16);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(24, 0, 0, 0));

        if (!items.isEmpty()) {
            Button clearBtn = new Button("Clear Cart");
            clearBtn.getStyleClass().add("btn-danger");
            clearBtn.setPrefHeight(44);
            clearBtn.setOnAction(e -> {
                facade.clearCart();
                mainContainer.setCenter(createCartPanel());
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label totalLabel = new Label("Total: $" + String.format("%.2f", facade.getCartTotal()));
            totalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 600; -fx-text-fill: #1d1d1f;");

            Button checkoutBtn = new Button("Checkout");
            checkoutBtn.getStyleClass().add("btn-success");
            checkoutBtn.setPrefWidth(200);
            checkoutBtn.setPrefHeight(44);
            checkoutBtn.setOnAction(e -> {
                if (items.isEmpty()) {
                    showAlert("Empty Cart", "Your cart is empty", Alert.AlertType.ERROR);
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Checkout");
                confirm.setHeaderText("Complete your purchase?");
                confirm.setContentText("Total: $" + facade.getCartTotal());

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            String orderId = facade.placeOrder();
                            showAlert("Order Placed", "Order #" + orderId + " has been placed successfully", 
                                    Alert.AlertType.INFORMATION);
                            mainContainer.setCenter(createCartPanel());
                        } catch (IllegalStateException ex) {
                            showAlert("Error", ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                });
            });

            bottomBox.getChildren().addAll(clearBtn, spacer, totalLabel, checkoutBtn);
        }

        container.getChildren().addAll(titleLabel, cartItems, bottomBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private ScrollPane createOrdersPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        Label titleLabel = new Label("Order History");
        titleLabel.getStyleClass().add("title-label");

        VBox ordersList = new VBox(16);
        List<Map<String, Object>> orders = facade.getCustomerOrderHistory();

        if (orders.isEmpty()) {
            VBox emptyBox = new VBox(16);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60));
            emptyBox.getStyleClass().add("card");
            
            Label emptyLabel = new Label("No orders yet");
            emptyLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #86868b;");
            
            Label emptySubLabel = new Label("Your order history will appear here");
            emptySubLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #86868b;");
            
            emptyBox.getChildren().addAll(emptyLabel, emptySubLabel);
            ordersList.getChildren().add(emptyBox);
        } else {
            for (Map<String, Object> order : orders) {
                String orderId = (String) order.get("orderId");
                String status = (String) order.get("status");
                String orderDate = (String) order.get("orderDate");
                double totalAmount = (double) order.get("totalAmount");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemsList = (List<Map<String, Object>>) order.get("items");
                
                VBox orderCard = new VBox(16);
                orderCard.getStyleClass().add("card");
                orderCard.setPadding(new Insets(20));

                // Header row
                HBox headerRow = new HBox(20);
                headerRow.setAlignment(Pos.CENTER_LEFT);
                
                Label orderIdLabel = new Label("Order #" + orderId);
                orderIdLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 17px;");
                
                Label statusLabel = new Label(status);
                statusLabel.getStyleClass().add("status-" + status.toLowerCase());
                statusLabel.setStyle(statusLabel.getStyle() + "; -fx-padding: 6 12; -fx-background-color: " + 
                    (status.equals("PENDING") ? "#fff3cd" :
                     status.equals("CONFIRMED") ? "#cfe2ff" :
                     status.equals("SHIPPED") ? "#d1e7dd" :
                     status.equals("CANCELLED") ? "#f8d7da" : "#d1e7dd") + 
                    "; -fx-background-radius: 6;");
                
                Region spacer1 = new Region();
                HBox.setHgrow(spacer1, Priority.ALWAYS);
                
                Label dateLabel = new Label(orderDate);
                dateLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #86868b;");
                
                Label totalLabel = new Label("$" + String.format("%.2f", totalAmount));
                totalLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 20px;");
                
                headerRow.getChildren().addAll(orderIdLabel, statusLabel, spacer1, dateLabel, totalLabel);

                // Items
                VBox itemsBox = new VBox(8);
                for (Map<String, Object> item : itemsList) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bookDTO = (Map<String, Object>) item.get("book");
                    int quantity = (int) item.get("quantity");
                    double subtotal = (double) item.get("subtotal");
                    
                    String bookTitle = (String) bookDTO.get("title");
                    
                    HBox itemRow = new HBox(12);
                    itemRow.setAlignment(Pos.CENTER_LEFT);
                    itemRow.setStyle("-fx-background-color: #fafafa; -fx-padding: 12; -fx-background-radius: 8;");
                    
                    Label itemLabel = new Label(bookTitle);
                    itemLabel.setStyle("-fx-font-size: 15px;");
                    
                    Region spacer2 = new Region();
                    HBox.setHgrow(spacer2, Priority.ALWAYS);
                    
                    Label qtyLabel = new Label("×" + quantity);
                    qtyLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");
                    
                    Label itemPrice = new Label("$" + String.format("%.2f", subtotal));
                    itemPrice.setStyle("-fx-font-weight: 500; -fx-font-size: 15px;");
                    
                    itemRow.getChildren().addAll(itemLabel, spacer2, qtyLabel, itemPrice);
                    itemsBox.getChildren().add(itemRow);
                }

                orderCard.getChildren().addAll(headerRow, itemsBox);

                // Cancel button if pending
                if (status.equals("PENDING")) {
                    HBox actionRow = new HBox();
                    actionRow.setAlignment(Pos.CENTER_RIGHT);
                    actionRow.setPadding(new Insets(8, 0, 0, 0));
                    
                    Button cancelBtn = new Button("Cancel Order");
                    cancelBtn.getStyleClass().add("btn-danger");
                    cancelBtn.setPrefHeight(36);
                    cancelBtn.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Cancel Order");
                        confirm.setHeaderText("Cancel this order?");
                        confirm.setContentText("Order #" + orderId);
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                facade.cancelOrder(orderId);
                                mainContainer.setCenter(createOrdersPanel());
                            }
                        });
                    });
                    actionRow.getChildren().add(cancelBtn);
                    orderCard.getChildren().add(actionRow);
                }

                ordersList.getChildren().add(orderCard);
            }
        }

        container.getChildren().addAll(titleLabel, ordersList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private ScrollPane createAccountPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: #fafafa;");

        Label titleLabel = new Label("Account Settings");
        titleLabel.getStyleClass().add("title-label");

        VBox formBox = new VBox(20);
        formBox.getStyleClass().add("card");
        formBox.setPadding(new Insets(32));
        formBox.setMaxWidth(600);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(20);

        Map<String, String> customerInfo = facade.getCustomerInfo();
        String username = customerInfo.get("username");
        String address = customerInfo.get("address");
        String phone = customerInfo.get("phone");

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("field-label");
        Label usernameValue = new Label(username);
        usernameValue.setStyle("-fx-font-size: 17px;");

        Label addressLabel = new Label("Address");
        addressLabel.getStyleClass().add("field-label");
        TextField addressField = new TextField(address);
        addressField.setPrefHeight(40);

        Label phoneLabel = new Label("Phone");
        phoneLabel.getStyleClass().add("field-label");
        TextField phoneField = new TextField(phone);
        phoneField.setPrefHeight(40);

        int row = 0;
        grid.add(usernameLabel, 0, row++);
        grid.add(usernameValue, 0, row++);
        grid.add(addressLabel, 0, row++);
        grid.add(addressField, 0, row++);
        grid.add(phoneLabel, 0, row++);
        grid.add(phoneField, 0, row++);

        Button updateBtn = new Button("Save Changes");
        updateBtn.getStyleClass().add("btn-primary");
        updateBtn.setPrefHeight(44);
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setOnAction(e -> {
            facade.updateCustomerInfo(addressField.getText(), phoneField.getText());
            showAlert("Success", "Your information has been updated", Alert.AlertType.INFORMATION);
        });

        formBox.getChildren().addAll(grid, updateBtn);
        container.getChildren().addAll(titleLabel, formBox);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    // Admin panels
    private ScrollPane createManageBooksPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Manage Books");
        titleLabel.getStyleClass().add("title-label");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Button addBookBtn = new Button("Add New Book");
        addBookBtn.getStyleClass().add("btn-success");
        addBookBtn.setPrefHeight(40);
        addBookBtn.setOnAction(e -> showAddBookDialog(container));

        headerBox.getChildren().addAll(titleLabel, addBookBtn);

        VBox booksList = new VBox(12);
        updateBooksListAdmin(booksList, container);

        container.getChildren().addAll(headerBox, booksList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private void updateBooksListAdmin(VBox booksList, VBox container) {
        booksList.getChildren().clear();
        List<Map<String, Object>> books = facade.browseAllBooks();

        for (Map<String, Object> book : books) {
            String bookId = (String) book.get("id");
            String bookTitle = (String) book.get("title");
            String author = (String) book.get("author");
            double price = (double) book.get("price");
            double originalPrice = (double) book.get("originalPrice");
            String category = (String) book.get("category");
            int stock = (int) book.get("stock");
            boolean isFeatured = (boolean) book.get("isFeatured");
            boolean isDiscounted = (boolean) book.get("isDiscounted");
            double discountPercentage = (double) book.get("discountPercentage");
            
            HBox bookBox = new HBox(20);
            bookBox.getStyleClass().add("card");
            bookBox.setAlignment(Pos.CENTER_LEFT);
            bookBox.setPadding(new Insets(16));

            VBox infoBox = new VBox(6);
            
            HBox titleBox = new HBox(10);
            titleBox.setAlignment(Pos.CENTER_LEFT);
            
            Label titleLabel = new Label(bookTitle);
            titleLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 17px;");
            
            if (isFeatured) {
                Label featuredBadge = new Label("FEATURED");
                featuredBadge.getStyleClass().add("featured-badge");
                titleBox.getChildren().add(featuredBadge);
            }
            
            if (isDiscounted) {
                Label discountBadge = new Label((int)discountPercentage + "% OFF");
                discountBadge.getStyleClass().add("discount-badge");
                titleBox.getChildren().add(discountBadge);
            }
            
            titleBox.getChildren().add(0, titleLabel);
            
            String priceDisplay = "$" + String.format("%.2f", price);
            if (isDiscounted) {
                priceDisplay += " (was $" + String.format("%.2f", originalPrice) + ")";
            }
            
            Label detailsLabel = new Label("by " + author + " • " + priceDisplay + 
                    " • " + category + " • Stock: " + stock);
            detailsLabel.setStyle("-fx-text-fill: #86868b; -fx-font-size: 15px;");
            
            infoBox.getChildren().addAll(titleBox, detailsLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox controls = new HBox(10);
            controls.setAlignment(Pos.CENTER_RIGHT);

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("btn-primary");
            editBtn.setPrefHeight(36);
            editBtn.setOnAction(e -> showEditBookDialog(book, booksList, container));

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setPrefHeight(36);
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Book");
                confirm.setHeaderText("Delete this book?");
                confirm.setContentText(bookTitle);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        facade.deleteBook(bookId);
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
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(24));

        TextField idField = new TextField();
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField priceField = new TextField();
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(facade.getAllCategories());
        TextField stockField = new TextField();
        TextField editionField = new TextField();
        TextField coverField = new TextField("default.jpg");
        final File[] selectedImageFile = new File[1];
        
        Button uploadImageBtn = new Button("Choose File...");
        uploadImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Book Cover Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                selectedImageFile[0] = selectedFile;
                coverField.setText(selectedFile.getName());
            }
        });
        
        HBox coverBox = new HBox(10, coverField, uploadImageBtn);
        CheckBox featuredCheck = new CheckBox("Featured");
        CheckBox discountCheck = new CheckBox("Discounted");
        TextField discountField = new TextField("0");
        discountField.setDisable(true);

        discountCheck.setOnAction(e -> discountField.setDisable(!discountCheck.isSelected()));

        int row = 0;
        grid.add(new Label("Book ID"), 0, row);
        grid.add(idField, 1, row++);
        grid.add(new Label("Title"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Author"), 0, row);
        grid.add(authorField, 1, row++);
        grid.add(new Label("Price"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Category"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Stock"), 0, row);
        grid.add(stockField, 1, row++);
        grid.add(new Label("Edition"), 0, row);
        grid.add(editionField, 1, row++);
        grid.add(new Label("Cover Image"), 0, row);
        grid.add(coverBox, 1, row++);
        grid.add(featuredCheck, 0, row);
        grid.add(discountCheck, 1, row++);
        grid.add(new Label("Discount %"), 0, row);
        grid.add(discountField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String coverImageName = coverField.getText();
                    if (selectedImageFile[0] != null) {
                        copyImageToDataDirectory(selectedImageFile[0]);
                    }
                    
                    // Create base book
                    Book book = new BasicBook(
                        idField.getText(),
                        titleField.getText(),
                        authorField.getText(),
                        Double.parseDouble(priceField.getText()),
                        categoryCombo.getValue(),
                        Integer.parseInt(stockField.getText()),
                        editionField.getText(),
                        coverImageName
                    );

                    // Apply decorators based on selections
                    if (discountCheck.isSelected()) {
                        double discount = Double.parseDouble(discountField.getText()) / 100.0;
                        book = new DiscountedBook(book, discount);
                    }
                    
                    if (featuredCheck.isSelected()) {
                        book = new FeaturedBook(book);
                    }

                    facade.addBookWithDecorators(book);
                    showAlert("Success", "Book added successfully", Alert.AlertType.INFORMATION);
                    
                    VBox booksList = (VBox) container.getChildren().get(1);
                    updateBooksListAdmin(booksList, container);
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid number format", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showEditBookDialog(Map<String, Object> bookDTO, VBox booksList, VBox container) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit book details");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(16);
        grid.setPadding(new Insets(24));

        String bookId = (String) bookDTO.get("id");
        String title = (String) bookDTO.get("title");
        String author = (String) bookDTO.get("author");
        double originalPrice = (double) bookDTO.get("originalPrice");
        String category = (String) bookDTO.get("category");
        int stock = (int) bookDTO.get("stock");
        String edition = (String) bookDTO.get("edition");
        String coverImage = (String) bookDTO.get("coverImage");
        boolean isFeatured = (boolean) bookDTO.get("isFeatured");
        boolean isDiscounted = (boolean) bookDTO.get("isDiscounted");
        double discountPercentage = (double) bookDTO.get("discountPercentage");

        TextField titleField = new TextField(title);
        TextField authorField = new TextField(author);
        TextField priceField = new TextField(String.valueOf(originalPrice));
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(facade.getAllCategories());
        categoryCombo.setValue(category);
        TextField stockField = new TextField(String.valueOf(stock));
        TextField editionField = new TextField(edition);
        TextField coverField = new TextField(coverImage);
        final File[] selectedImageFile = new File[1];
        
        Button uploadImageBtn = new Button("Choose File...");
        uploadImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Book Cover Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                selectedImageFile[0] = selectedFile;
                coverField.setText(selectedFile.getName());
            }
        });
        
        HBox editCoverBox = new HBox(10, coverField, uploadImageBtn);
        CheckBox featuredCheck = new CheckBox("Featured");
        featuredCheck.setSelected(isFeatured);
        CheckBox discountCheck = new CheckBox("Discounted");
        discountCheck.setSelected(isDiscounted);
        TextField discountField = new TextField(String.valueOf((int)discountPercentage));
        discountField.setDisable(!isDiscounted);

        discountCheck.setOnAction(e -> discountField.setDisable(!discountCheck.isSelected()));

        int row = 0;
        grid.add(new Label("Title"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Author"), 0, row);
        grid.add(authorField, 1, row++);
        grid.add(new Label("Price"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Category"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Stock"), 0, row);
        grid.add(stockField, 1, row++);
        grid.add(new Label("Edition"), 0, row);
        grid.add(editionField, 1, row++);
        grid.add(new Label("Cover Image"), 0, row);
        grid.add(editCoverBox, 1, row++);
        grid.add(featuredCheck, 0, row);
        grid.add(discountCheck, 1, row++);
        grid.add(new Label("Discount %"), 0, row);
        grid.add(discountField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String coverImageName = coverField.getText();
                    if (selectedImageFile[0] != null) {
                        copyImageToDataDirectory(selectedImageFile[0]);
                    }
                    
                    Book updatedBook = new BasicBook(
                        bookId,
                        titleField.getText(),
                        authorField.getText(),
                        Double.parseDouble(priceField.getText()),
                        categoryCombo.getValue(),
                        Integer.parseInt(stockField.getText()),
                        editionField.getText(),
                        coverImageName
                    );
                    
                    // Apply decorators based on selections
                    if (discountCheck.isSelected()) {
                        double discount = Double.parseDouble(discountField.getText()) / 100.0;
                        updatedBook = new DiscountedBook(updatedBook, discount);
                    }
                    
                    if (featuredCheck.isSelected()) {
                        updatedBook = new FeaturedBook(updatedBook);
                    }
                    
                    facade.updateBook(updatedBook);

                    showAlert("Success", "Book updated successfully", Alert.AlertType.INFORMATION);
                    updateBooksListAdmin(booksList, container);
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid number format", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private ScrollPane createManageOrdersPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Manage Orders");
        titleLabel.getStyleClass().add("title-label");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Orders", "Pending", "Confirmed", "Shipped", "Cancelled");
        filterCombo.setValue("All Orders");
        filterCombo.setPrefHeight(40);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setPrefHeight(40);
        refreshBtn.setOnAction(e -> {
            VBox ordersList = (VBox) container.getChildren().get(1);
            updateOrdersListAdmin(ordersList, container, filterCombo.getValue());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(titleLabel, spacer, new Label("Filter"), filterCombo, refreshBtn);

        VBox ordersList = new VBox(12);
        filterCombo.setOnAction(e -> updateOrdersListAdmin(ordersList, container, filterCombo.getValue()));
        updateOrdersListAdmin(ordersList, container, "All Orders");

        container.getChildren().addAll(headerBox, ordersList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private void updateOrdersListAdmin(VBox ordersList, VBox container, String filter) {
        ordersList.getChildren().clear();
        List<Map<String, Object>> orders = facade.getAllOrders();

        if (!filter.equals("All Orders")) {
            final String status = filter.toUpperCase();
            orders = orders.stream()
                    .filter(o -> o.get("status").equals(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        for (Map<String, Object> order : orders) {
            String orderId = (String) order.get("orderId");
            String customerUsername = (String) order.get("customerUsername");
            String status = (String) order.get("status");
            String orderDate = (String) order.get("orderDate");
            double totalAmount = (double) order.get("totalAmount");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) order.get("items");
            
            VBox orderBox = new VBox(16);
            orderBox.getStyleClass().add("card");
            orderBox.setPadding(new Insets(20));

            HBox headerRow = new HBox(20);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            Label orderIdLabel = new Label("Order #" + orderId);
            orderIdLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 17px;");

            Label customerLabel = new Label("Customer: " + customerUsername);
            customerLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #86868b;");

            Label statusLabel = new Label(status);
            statusLabel.getStyleClass().add("status-" + status.toLowerCase());
            statusLabel.setStyle(statusLabel.getStyle() + "; -fx-padding: 6 12; -fx-background-color: " + 
                (status.equals("PENDING") ? "#fff3cd" :
                 status.equals("CONFIRMED") ? "#cfe2ff" :
                 status.equals("SHIPPED") ? "#d1e7dd" :
                 status.equals("CANCELLED") ? "#f8d7da" : "#d1e7dd") + 
                "; -fx-background-radius: 6;");

            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);

            Label dateLabel = new Label(orderDate);
            dateLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #86868b;");

            Label totalLabel = new Label("$" + String.format("%.2f", totalAmount));
            totalLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 20px;");

            headerRow.getChildren().addAll(orderIdLabel, customerLabel, statusLabel, spacer1, dateLabel, totalLabel);

            VBox itemsBox = new VBox(8);
            for (Map<String, Object> item : itemsList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bookDTO = (Map<String, Object>) item.get("book");
                int quantity = (int) item.get("quantity");
                
                String bookTitle = (String) bookDTO.get("title");
                String bookAuthor = (String) bookDTO.get("author");
                
                HBox itemRow = new HBox(12);
                itemRow.setPadding(new Insets(8));
                itemRow.setStyle("-fx-background-color: #f5f5f7; -fx-background-radius: 6;");
                
                Label itemLabel = new Label(bookTitle + " by " + bookAuthor + " (x" + quantity + ")");
                itemLabel.setStyle("-fx-font-size: 15px;");
                
                itemRow.getChildren().add(itemLabel);
                itemsBox.getChildren().add(itemRow);
            }

            orderBox.getChildren().addAll(headerRow, itemsBox);

            if (!status.equals("CANCELLED") && !status.equals("DELIVERED")) {
                HBox actionsBox = new HBox(10);
                actionsBox.setAlignment(Pos.CENTER_RIGHT);
                actionsBox.setPadding(new Insets(8, 0, 0, 0));

                if (status.equals("PENDING")) {
                    Button confirmBtn = new Button("Confirm");
                    confirmBtn.getStyleClass().add("btn-success");
                    confirmBtn.setPrefHeight(36);
                    confirmBtn.setOnAction(e -> {
                        facade.confirmOrder(orderId);
                        updateOrdersListAdmin(ordersList, container, filter);
                    });

                    Button cancelBtn = new Button("Cancel");
                    cancelBtn.getStyleClass().add("btn-danger");
                    cancelBtn.setPrefHeight(36);
                    cancelBtn.setOnAction(e -> {
                        facade.cancelOrderByAdmin(orderId);
                        updateOrdersListAdmin(ordersList, container, filter);
                    });

                    actionsBox.getChildren().addAll(confirmBtn, cancelBtn);
                }

                if (status.equals("CONFIRMED")) {
                    Button shipBtn = new Button("Mark as Shipped");
                    shipBtn.getStyleClass().add("btn-primary");
                    shipBtn.setPrefHeight(36);
                    shipBtn.setOnAction(e -> {
                        facade.shipOrder(orderId);
                        updateOrdersListAdmin(ordersList, container, filter);
                    });
                    actionsBox.getChildren().add(shipBtn);
                }

                if (!actionsBox.getChildren().isEmpty()) {
                    orderBox.getChildren().add(actionsBox);
                }
            }

            ordersList.getChildren().add(orderBox);
        }
    }

    private ScrollPane createStatisticsPanel() {
        VBox container = new VBox(32);
        container.setPadding(new Insets(40));
        container.setStyle("-fx-background-color: #fafafa;");

        // Header
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📊 Business Analytics");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #1d1d1f;");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-primary");
        refreshBtn.setPrefHeight(40);
        refreshBtn.setOnAction(e -> {
            TabPane tabPane = (TabPane) mainContainer.getCenter();
            Tab statsTab = tabPane.getTabs().get(2);
            statsTab.setContent(createStatisticsPanel());
        });
        
        headerBox.getChildren().addAll(titleLabel, headerSpacer, refreshBtn);

        // Key Metrics - 4 cards in a row
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(16);
        
        // Revenue Card
        VBox revenueCard = createMetricCard("💰", "Total Revenue", 
            "$" + String.format("%.2f", facade.getTotalRevenue()), 
            "From completed orders", "#0071e3");
        
        // Completed Orders Card
        VBox completedCard = createMetricCard("✅", "Completed Orders", 
            String.valueOf(facade.getCompletedOrdersCount()), 
            "Confirmed & shipped", "#34c759");
        
        // Pending Orders Card
        VBox pendingCard = createMetricCard("⏳", "Pending Orders", 
            String.valueOf(facade.getPendingOrdersCount()), 
            "Awaiting confirmation", "#ff9500");
        
        // Cancelled Orders Card
        VBox cancelledCard = createMetricCard("❌", "Cancelled Orders", 
            String.valueOf(facade.getCancelledOrdersCount()), 
            "Not counted in revenue", "#ff3b30");
        
        metricsGrid.add(revenueCard, 0, 0);
        metricsGrid.add(completedCard, 1, 0);
        metricsGrid.add(pendingCard, 2, 0);
        metricsGrid.add(cancelledCard, 3, 0);

        // Two columns layout for detailed stats
        HBox detailsRow = new HBox(20);
        
        // Top Selling Books
        VBox topBooksBox = new VBox(16);
        topBooksBox.getStyleClass().add("card");
        topBooksBox.setPadding(new Insets(24));
        topBooksBox.setPrefWidth(550);
        HBox.setHgrow(topBooksBox, Priority.ALWAYS);

        Label topBooksLabel = new Label("🏆 Top Selling Books");
        topBooksLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 20px; -fx-text-fill: #1d1d1f;");

        VBox topBooksList = new VBox(12);
        List<Map<String, Object>> topBooks = facade.getTopSellingBooks(5);
        
        if (topBooks.isEmpty()) {
            Label emptyLabel = new Label("No sales data available yet");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #86868b; -fx-padding: 20 0;");
            topBooksList.getChildren().add(emptyLabel);
        } else {
            int rank = 1;
            for (Map<String, Object> book : topBooks) {
                String bookTitle = (String) book.get("title");
                String author = (String) book.get("author");
                int popularity = (int) book.get("popularity");
                HBox bookRow = new HBox(16);
                bookRow.setAlignment(Pos.CENTER_LEFT);
                bookRow.setStyle("-fx-background-color: #f5f5f7; -fx-padding: 16; -fx-background-radius: 10;");
                
                // Rank badge
                Label rankLabel = new Label(String.valueOf(rank++));
                rankLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 18px; -fx-text-fill: white; " +
                    "-fx-background-color: #0071e3; -fx-background-radius: 20; " +
                    "-fx-min-width: 36; -fx-min-height: 36; -fx-alignment: center;");
                
                VBox bookInfo = new VBox(4);
                Label bookTitleLabel = new Label(bookTitle);
                bookTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
                Label authorLabel = new Label("by " + author);
                authorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #86868b;");
                bookInfo.getChildren().addAll(bookTitleLabel, authorLabel);
                HBox.setHgrow(bookInfo, Priority.ALWAYS);
                
                VBox salesInfo = new VBox(4);
                salesInfo.setAlignment(Pos.CENTER_RIGHT);
                Label soldLabel = new Label(popularity + " sold");
                soldLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #0071e3;");
                double price = (double) book.get("price");
                Label priceLabel = new Label("$" + String.format("%.2f", price));
                priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #86868b;");
                salesInfo.getChildren().addAll(soldLabel, priceLabel);
                
                bookRow.getChildren().addAll(rankLabel, bookInfo, salesInfo);
                topBooksList.getChildren().add(bookRow);
            }
        }

        topBooksBox.getChildren().addAll(topBooksLabel, topBooksList);

        // Category Sales
        VBox categorySalesBox = new VBox(16);
        categorySalesBox.getStyleClass().add("card");
        categorySalesBox.setPadding(new Insets(24));
        categorySalesBox.setPrefWidth(550);
        HBox.setHgrow(categorySalesBox, Priority.ALWAYS);

        Label categorySalesLabel = new Label("📚 Sales by Category");
        categorySalesLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 20px; -fx-text-fill: #1d1d1f;");

        VBox categorySalesList = new VBox(12);
        Map<String, Integer> categoryStats = facade.getCategorySalesStatistics();
        
        if (categoryStats.isEmpty()) {
            Label emptyLabel = new Label("No category sales data available yet");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #86868b; -fx-padding: 20 0;");
            categorySalesList.getChildren().add(emptyLabel);
        } else {
            // Find max value for bar chart effect
            int maxSales = categoryStats.values().stream().max(Integer::compareTo).orElse(1);
            
            for (java.util.Map.Entry<String, Integer> entry : categoryStats.entrySet()) {
                VBox categoryRow = new VBox(8);
                categoryRow.setStyle("-fx-background-color: #f5f5f7; -fx-padding: 16; -fx-background-radius: 10;");
                
                HBox headerRow = new HBox(12);
                headerRow.setAlignment(Pos.CENTER_LEFT);
                
                Label categoryLabel = new Label(entry.getKey());
                categoryLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label salesLabel = new Label(entry.getValue() + " books sold");
                salesLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #0071e3; -fx-font-weight: 600;");
                
                headerRow.getChildren().addAll(categoryLabel, spacer, salesLabel);
                
                // Progress bar
                ProgressBar progressBar = new ProgressBar((double) entry.getValue() / maxSales);
                progressBar.setMaxWidth(Double.MAX_VALUE);
                progressBar.setPrefHeight(8);
                progressBar.setStyle("-fx-accent: #0071e3;");
                
                categoryRow.getChildren().addAll(headerRow, progressBar);
                categorySalesList.getChildren().add(categoryRow);
            }
        }

        categorySalesBox.getChildren().addAll(categorySalesLabel, categorySalesList);
        
        detailsRow.getChildren().addAll(topBooksBox, categorySalesBox);

        container.getChildren().addAll(headerBox, metricsGrid, detailsRow);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }
    
    private VBox createMetricCard(String icon, String label, String value, String subtitle, String color) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));
        card.setPrefWidth(250);
        card.setStyle(card.getStyle() + "; -fx-background-color: white; -fx-border-color: " + color + 
            "; -fx-border-width: 0 0 0 4;");
        
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        VBox textBox = new VBox(4);
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #86868b; -fx-font-weight: 500;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        
        textBox.getChildren().addAll(titleLabel, valueLabel);
        headerBox.getChildren().addAll(iconLabel, textBox);
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #86868b;");
        
        card.getChildren().addAll(headerBox, subtitleLabel);
        return card;
    }

    private ScrollPane createManageCategoriesPanel() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(32));
        container.setStyle("-fx-background-color: #fafafa;");

        Label titleLabel = new Label("Manage Categories");
        titleLabel.getStyleClass().add("title-label");

        VBox addCategoryBox = new VBox(12);
        addCategoryBox.getStyleClass().add("card");
        addCategoryBox.setPadding(new Insets(20));

        HBox addRow = new HBox(12);
        addRow.setAlignment(Pos.CENTER_LEFT);

        TextField newCategoryField = new TextField();
        newCategoryField.setPromptText("Enter new category name");
        newCategoryField.setPrefWidth(300);
        newCategoryField.setPrefHeight(40);

        Button addBtn = new Button("Add Category");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setPrefHeight(40);
        addBtn.setOnAction(e -> {
            String newCategory = newCategoryField.getText().trim();
            if (!newCategory.isEmpty()) {
                facade.addCategory(newCategory);
                newCategoryField.clear();
                VBox categoriesList = (VBox) container.getChildren().get(2);
                updateCategoriesList(categoriesList);
                showAlert("Success", "Category added successfully", Alert.AlertType.INFORMATION);
            }
        });

        addRow.getChildren().addAll(newCategoryField, addBtn);
        addCategoryBox.getChildren().add(addRow);

        VBox categoriesList = new VBox(10);
        updateCategoriesList(categoriesList);

        container.getChildren().addAll(titleLabel, addCategoryBox, categoriesList);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #fafafa;");
        return scroll;
    }

    private void updateCategoriesList(VBox categoriesList) {
        categoriesList.getChildren().clear();
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        
        List<String> categories = new java.util.ArrayList<>(facade.getAllCategories());
        int col = 0;
        int row = 0;
        
        for (String category : categories) {
            VBox categoryCard = new VBox(8);
            categoryCard.getStyleClass().add("card");
            categoryCard.setPadding(new Insets(16));
            categoryCard.setPrefWidth(200);
            categoryCard.setAlignment(Pos.CENTER);
            
            Label categoryLabel = new Label(category);
            categoryLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: 500;");
            
            categoryCard.getChildren().add(categoryLabel);
            grid.add(categoryCard, col, row);
            
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
        
        categoriesList.getChildren().add(grid);
    }

    private void copyImageToDataDirectory(File sourceFile) {
        try {
            File imagesDir = new File("bookstore_data/images");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
            
            if (sourceFile != null && sourceFile.exists()) {
                File destFile = new File(imagesDir, sourceFile.getName());
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image copied: " + sourceFile.getName());
            }
        } catch (Exception e) {
            System.err.println("Error copying image: " + e.getMessage());
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