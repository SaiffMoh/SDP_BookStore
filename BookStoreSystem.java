// BookStoreSystem.java - SINGLETON PATTERN (Bonus)
// Based on Lecture 2 - Singleton Pattern
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

    private BookStoreSystem() {
        books = new ArrayList<>();
        users = new ArrayList<>();
        allOrders = new ArrayList<>();
        allReviews = new ArrayList<>();
        categories = new HashSet<>();
        orderIdCounter = 1000;
        initializeSystem();
    }

    public static synchronized BookStoreSystem getInstance() {
        if (instance == null) {
            instance = new BookStoreSystem();
        }
        return instance;
    }

    private void initializeSystem() {
        users.add(UserFactory.createAdmin("admin", "admin123"));
        
        categories.add("IT");
        categories.add("History");
        categories.add("Classics");
        categories.add("Science");
        categories.add("Fiction");
        
        // Add basic books, some will be decorated
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
        
        // Apply decorators - some books are discounted and/or featured
        books.add(new FeaturedBook(new DiscountedBook(book1, 0.15))); // Featured + 15% off
        books.add(new DiscountedBook(book2, 0.10)); // 10% off
        books.add(new FeaturedBook(book3)); // Featured
        books.add(book4); // Regular
        books.add(new DiscountedBook(book5, 0.20)); // 20% off
    }

    public void addBook(Book book) {
        books.add(book);
        categories.add(book.getCategory());
    }

    public void removeBook(String bookId) {
        books.removeIf(book -> book.getId().equals(bookId));
    }

    public void updateBook(Book updatedBook) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(updatedBook.getId())) {
                books.set(i, updatedBook);
                categories.add(updatedBook.getCategory());
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

    public void registerUser(User user) {
        users.add(user);
    }

    public User login(String username, String password) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username) && 
                               user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public List<User> getAllCustomers() {
        return users.stream()
                .filter(user -> user instanceof Customer)
                .collect(Collectors.toList());
    }

    public String generateOrderId() {
        return "ORD" + (orderIdCounter++);
    }

    public void addOrder(Order order) {
        allOrders.add(order);
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

    public Set<String> getCategories() {
        return new HashSet<>(categories);
    }

    public void addCategory(String category) {
        categories.add(category);
    }

    public void addReview(Review review) {
        allReviews.add(review);
    }

    public List<Review> getReviewsForBook(String bookId) {
        return allReviews.stream()
                .filter(review -> review.getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

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