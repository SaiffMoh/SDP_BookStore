// BookStoreFacade.java - FACADE PATTERN with Auto-Save
import java.util.*;

public class BookStoreFacade {
    private BookStoreSystem bookStore;
    
    public BookStoreFacade() {
        this.bookStore = BookStoreSystem.getInstance();
    }
    
    // ============== USER MANAGEMENT ==============
    
    public User registerCustomer(String username, String password, String address, String phone) {
        User customer = UserFactory.createCustomer(username, password, address, phone);
        bookStore.registerUser(customer);
        return customer;
    }
    
    public User login(String username, String password) {
        return bookStore.login(username, password);
    }
    
    public void updateCustomerInfo(Customer customer, String address, String phone) {
        customer.setAddress(address);
        customer.setPhone(phone);
        bookStore.saveAllData();
    }
    
    // ============== BOOK BROWSING (Customer) ==============
    
    public List<Book> browseAllBooks() {
        return bookStore.getAllBooks();
    }
    
    public List<Book> searchBooks(String query) {
        return bookStore.searchBooks(query);
    }
    
    public List<Book> filterBooksByCategory(String category) {
        return bookStore.filterByCategory(category);
    }
    
    public List<Book> sortBooksByPrice(boolean ascending) {
        return bookStore.sortByPrice(ascending);
    }
    
    public List<Book> sortBooksByPopularity() {
        return bookStore.sortByPopularity();
    }
    
    public Book getBookDetails(String bookId) {
        return bookStore.getBookById(bookId);
    }
    
    // ============== CART MANAGEMENT (Customer) ==============
    
    public void addToCart(Customer customer, Book book, int quantity) {
        if (quantity > book.getStock()) {
            throw new IllegalArgumentException("Not enough stock available");
        }
        customer.getCart().addItem(book, quantity);
        // No save needed - cart is transient
    }
    
    public void removeFromCart(Customer customer, String bookId) {
        customer.getCart().removeItem(bookId);
    }
    
    public void updateCartQuantity(Customer customer, String bookId, int quantity) {
        customer.getCart().updateQuantity(bookId, quantity);
    }
    
    public void clearCart(Customer customer) {
        customer.getCart().clear();
    }
    
    public double getCartTotal(Customer customer) {
        return customer.getCart().getTotal();
    }
    
    public List<OrderItem> getCartItems(Customer customer) {
        return customer.getCart().getItems();
    }
    
    // ============== ORDER MANAGEMENT (Customer) ==============
    
    public Order placeOrder(Customer customer) {
        if (customer.getCart().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        Order order = new Order(bookStore.generateOrderId(), customer);
        
        for (OrderItem item : customer.getCart().getItems()) {
            order.addItem(new OrderItem(item.getBook(), item.getQuantity()));
            
            Book book = item.getBook();
            book.setStock(book.getStock() - item.getQuantity());
            book.incrementPopularity();
        }
        
        bookStore.addOrder(order);
        customer.addOrder(order);
        customer.getCart().clear();
        bookStore.saveAllData();
        
        return order;
    }
    
    public void cancelOrder(Customer customer, String orderId) {
        Order order = bookStore.getOrderById(orderId);
        if (order != null && order.getStatus().equals("PENDING")) {
            order.setStatus("CANCELLED");
            
            for (OrderItem item : order.getItems()) {
                Book book = item.getBook();
                if (book != null) {
                    book.setStock(book.getStock() + item.getQuantity());
                }
            }
            bookStore.saveAllData();
        }
    }
    
    public List<Order> getCustomerOrderHistory(Customer customer) {
        return customer.getOrderHistory();
    }
    
    public Order getOrderDetails(String orderId) {
        return bookStore.getOrderById(orderId);
    }
    
    // ============== REVIEW MANAGEMENT (Customer) ==============
    
    public void addReview(Customer customer, String bookId, int rating, String comment) {
        Review review = new Review(bookId, customer.getUsername(), rating, comment);
        customer.addReview(review);
        bookStore.addReview(review);
        bookStore.saveAllData();
    }
    
    public List<Review> getBookReviews(String bookId) {
        return bookStore.getReviewsForBook(bookId);
    }
    
    // ============== BOOK MANAGEMENT (Admin) ==============
    
    public void addBook(String id, String title, String author, double price, 
                       String category, int stock, String edition, String coverImage) {
        Book book = new BasicBook(id, title, author, price, category, stock, edition, coverImage);
        bookStore.addBook(book);
    }
    
    public void addBookWithDecorators(Book book) {
        bookStore.addBook(book);
    }
    
    public void updateBook(Book book) {
        bookStore.updateBook(book);
    }
    
    public void deleteBook(String bookId) {
        bookStore.removeBook(bookId);
    }
    
    public void updateBookStock(String bookId, int newStock) {
        Book book = bookStore.getBookById(bookId);
        if (book != null) {
            book.setStock(newStock);
            bookStore.saveAllData();
        }
    }
    
    // ============== CATEGORY MANAGEMENT (Admin) ==============
    
    public Set<String> getAllCategories() {
        return bookStore.getCategories();
    }
    
    public void addCategory(String category) {
        bookStore.addCategory(category);
    }
    
    // ============== ORDER MANAGEMENT (Admin) ==============
    
    public List<Order> getAllOrders() {
        return bookStore.getAllOrders();
    }
    
    public List<Order> getPendingOrders() {
        return bookStore.getPendingOrders();
    }
    
    public void confirmOrder(String orderId) {
        Order order = bookStore.getOrderById(orderId);
        if (order != null) {
            order.setStatus("CONFIRMED");
            bookStore.saveAllData();
        }
    }
    
    public void shipOrder(String orderId) {
        Order order = bookStore.getOrderById(orderId);
        if (order != null && order.getStatus().equals("CONFIRMED")) {
            order.setStatus("SHIPPED");
            bookStore.saveAllData();
        }
    }
    
    public void cancelOrderByAdmin(String orderId) {
        Order order = bookStore.getOrderById(orderId);
        if (order != null && order.getStatus().equals("PENDING")) {
            order.setStatus("CANCELLED");
            
            for (OrderItem item : order.getItems()) {
                Book book = item.getBook();
                if (book != null) {
                    book.setStock(book.getStock() + item.getQuantity());
                }
            }
            bookStore.saveAllData();
        }
    }
    
    // ============== STATISTICS (Admin) ==============
    
    public Map<String, Integer> getCategorySalesStatistics() {
        return bookStore.getCategorySalesStatistics();
    }
    
    public List<Book> getTopSellingBooks(int limit) {
        return bookStore.getTopSellingBooks(limit);
    }
    
    public double getTotalRevenue() {
        return bookStore.getTotalRevenue();
    }
    
    public int getTotalOrdersCount() {
        return bookStore.getAllOrders().size();
    }
    
    public List<User> getAllCustomers() {
        return bookStore.getAllCustomers();
    }
}