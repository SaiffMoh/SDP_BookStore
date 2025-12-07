// BookStoreSystem.java - SINGLETON PATTERN with JSON Persistence
import java.util.*;
import java.util.stream.Collectors;

public class BookStoreSystem {
    private static BookStoreSystem instance;
    private List<Book> books;
    private List<User> users;
    private List<Order> allOrders;
    private List<Review> allReviews;
    private Set<String> categories;
    private int orderIdCounter;
    private DataManager dataManager;

    private BookStoreSystem() {
        dataManager = new DataManager();
        loadAllData();
    }

    public static synchronized BookStoreSystem getInstance() {
        if (instance == null) {
            instance = new BookStoreSystem();
        }
        return instance;
    }

    private void loadAllData() {
        System.out.println("\n========== Loading Data ==========");
        
        users = dataManager.loadUsers();
        books = dataManager.loadBooks();
        allOrders = dataManager.loadOrders();
        allReviews = dataManager.loadReviews();
        categories = dataManager.loadCategories();
        
        Map<String, Object> config = dataManager.loadConfig();
        if (config.containsKey("orderIdCounter")) {
            orderIdCounter = ((Double) config.get("orderIdCounter")).intValue();
        } else {
            orderIdCounter = 1000;
        }
        
        // If first run, initialize with default data
        if (users.isEmpty()) {
            System.out.println("\nℹ First run detected - initializing default data...");
            initializeDefaultData();
        }
        
        System.out.println("==================================\n");
    }

    private void initializeDefaultData() {
        users.add(UserFactory.createAdmin("admin", "admin123"));
        
        categories.add("IT");
        categories.add("History");
        categories.add("Classics");
        categories.add("Science");
        categories.add("Fiction");
        
        Book book1 = new BasicBook("B001", "Clean Code", "Robert Martin", 45.99, 
                "IT", 20, "1st Edition", "clean_code.jpg");
        Book book2 = new BasicBook("B002", "Design Patterns", "Gang of Four", 55.50, 
                "IT", 15, "1st Edition", "design_patterns.jpg");
        Book book3 = new BasicBook("B003", "Sapiens", "Yuval Harari", 30.00, 
                "History", 25, "2nd Edition", "sapiens.jpg");
        Book book4 = new BasicBook("B004", "1984", "George Orwell", 20.00, 
                "Classics", 30, "3rd Edition", "1984.jpg");
Book book5 = new BasicBook("B005", "The Selfish Gene", "Richard Dawkins", 35.00,
"Science", 18, "1st Edition", "selfish_gene.jpg");
    books.add(new FeaturedBook(new DiscountedBook(book1, 0.15)));
    books.add(new DiscountedBook(book2, 0.10));
    books.add(new FeaturedBook(book3));
    books.add(book4);
    books.add(new DiscountedBook(book5, 0.20));
    
    saveAllData();
}

public void saveAllData() {
    System.out.println("\n========== Saving Data ==========");
    dataManager.saveUsers(users);
    dataManager.saveBooks(books);
    dataManager.saveOrders(allOrders);
    dataManager.saveReviews(allReviews);
    dataManager.saveCategories(categories);
    
    Map<String, Object> config = new HashMap<>();
    config.put("orderIdCounter", orderIdCounter);
    dataManager.saveConfig(config);
    System.out.println("==================================\n");
}

// Book Management
public void addBook(Book book) {
    books.add(book);
    categories.add(book.getCategory());
    saveAllData();
}

public void removeBook(String bookId) {
    books.removeIf(book -> book.getId().equals(bookId));
    saveAllData();
}

public void updateBook(Book updatedBook) {
    for (int i = 0; i < books.size(); i++) {
        if (books.get(i).getId().equals(updatedBook.getId())) {
            books.set(i, updatedBook);
            categories.add(updatedBook.getCategory());
            saveAllData();
            break;
        }
    }
}

public Book getBookById(String id) {
    return books.stream()
            .filter(book -> book.getId().equals(id))
            .findFirst()
            .orElse(null);
}

public List<Book> getAllBooks() {
    return new ArrayList<>(books);
}

public List<Book> searchBooks(String query) {
    String lowerQuery = query.toLowerCase();
    return books.stream()
            .filter(book -> book.getTitle().toLowerCase().contains(lowerQuery) ||
                           book.getAuthor().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
}

public List<Book> filterByCategory(String category) {
    return books.stream()
            .filter(book -> book.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
}

public List<Book> sortByPrice(boolean ascending) {
    List<Book> sortedBooks = new ArrayList<>(books);
    if (ascending) {
        sortedBooks.sort(Comparator.comparingDouble(Book::getPrice));
    } else {
        sortedBooks.sort(Comparator.comparingDouble(Book::getPrice).reversed());
    }
    return sortedBooks;
}

public List<Book> sortByPopularity() {
    List<Book> sortedBooks = new ArrayList<>(books);
    sortedBooks.sort(Comparator.comparingInt(Book::getPopularity).reversed());
    return sortedBooks;
}

// User Management
public void registerUser(User user) {
    users.add(user);
    saveAllData();
}

public User login(String username, String password) {
    return users.stream()
            .filter(user -> user.getUsername().equals(username) && 
                           user.getPassword().equals(password))
            .findFirst()
            .orElse(null);
}

public Customer getCustomerByUsername(String username) {
    return (Customer) users.stream()
            .filter(user -> user instanceof Customer && 
                           user.getUsername().equals(username))
            .findFirst()
            .orElse(null);
}

public List<User> getAllCustomers() {
    return users.stream()
            .filter(user -> user instanceof Customer)
            .collect(Collectors.toList());
}

// Order Management
public String generateOrderId() {
    return "ORD" + (orderIdCounter++);
}

public void addOrder(Order order) {
    allOrders.add(order);
    saveAllData();
}

public List<Order> getAllOrders() {
    return new ArrayList<>(allOrders);
}

public List<Order> getPendingOrders() {
    return allOrders.stream()
            .filter(order -> order.getStatus().equals("PENDING"))
            .collect(Collectors.toList());
}

public Order getOrderById(String orderId) {
    return allOrders.stream()
            .filter(order -> order.getOrderId().equals(orderId))
            .findFirst()
            .orElse(null);
}

// Categories
public Set<String> getCategories() {
    return new HashSet<>(categories);
}

public void addCategory(String category) {
    categories.add(category);
    saveAllData();
}

// Reviews
public void addReview(Review review) {
    allReviews.add(review);
    saveAllData();
}

public List<Review> getReviewsForBook(String bookId) {
    return allReviews.stream()
            .filter(review -> review.getBookId().equals(bookId))
            .collect(Collectors.toList());
}

// Statistics
public Map<String, Integer> getCategorySalesStatistics() {
    Map<String, Integer> stats = new HashMap<>();
    for (Order order : allOrders) {
        if (order.getStatus().equals("CONFIRMED") || 
            order.getStatus().equals("SHIPPED")) {
            for (OrderItem item : order.getItems()) {
                String category = item.getBook().getCategory();
                stats.put(category, stats.getOrDefault(category, 0) + item.getQuantity());
            }
        }
    }
    return stats;
}

public List<Book> getTopSellingBooks(int limit) {
    return books.stream()
            .sorted(Comparator.comparingInt(Book::getPopularity).reversed())
            .limit(limit)
            .collect(Collectors.toList());
}

public double getTotalRevenue() {
    return allOrders.stream()
            .filter(order -> order.getStatus().equals("CONFIRMED") || 
                           order.getStatus().equals("SHIPPED"))
            .mapToDouble(Order::getTotalAmount)
            .sum();
}
}