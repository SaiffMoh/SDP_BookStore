// BookStoreFacade.java - FACADE PATTERN with Auto-Save
import java.util.*;

public class BookStoreFacade {
    private BookStoreSystem bookStore;
    private String currentUsername;
    private String currentUserType;
    
    public BookStoreFacade() {
        this.bookStore = BookStoreSystem.getInstance();
    }
    
    // ============== USER MANAGEMENT ==============
    
    public boolean registerCustomer(String username, String password, String address, String phone) {
        User customer = UserFactory.createCustomer(username, password, address, phone);
        bookStore.registerUser(customer);
        return true;
    }
    
    public String login(String username, String password) {
        User user = bookStore.login(username, password);
        if (user != null) {
            this.currentUsername = username;
            this.currentUserType = user.getUserType();
            return user.getUserType();
        }
        return null;
    }
    
    public void logout() {
        this.currentUsername = null;
        this.currentUserType = null;
    }
    
    public String getCurrentUsername() {
        return currentUsername;
    }
    
    public String getCurrentUserType() {
        return currentUserType;
    }
    
    public void updateCustomerInfo(String address, String phone) {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            customer.setAddress(address);
            customer.setPhone(phone);
            bookStore.saveAllData();
        }
    }
    
    public Map<String, String> getCustomerInfo() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            Map<String, String> info = new HashMap<>();
            info.put("username", customer.getUsername());
            info.put("address", customer.getAddress());
            info.put("phone", customer.getPhone());
            return info;
        }
        return null;
    }
    
    // ============== BOOK BROWSING ==============
    
    public List<Map<String, Object>> browseAllBooks() {
        return convertBooksToDTO(bookStore.getAllBooks());
    }
    
    public List<Map<String, Object>> searchBooks(String query) {
        return convertBooksToDTO(bookStore.searchBooks(query));
    }
    
    public List<Map<String, Object>> filterBooksByCategory(String category) {
        return convertBooksToDTO(bookStore.filterByCategory(category));
    }
    
    public List<Map<String, Object>> sortBooksByPrice(boolean ascending) {
        return convertBooksToDTO(bookStore.sortByPrice(ascending));
    }
    
    public List<Map<String, Object>> sortBooksByPopularity() {
        return convertBooksToDTO(bookStore.sortByPopularity());
    }
    
    public Map<String, Object> getBookDetails(String bookId) {
        Book book = bookStore.getBookById(bookId);
        return book != null ? convertBookToDTO(book) : null;
    }
    
    private List<Map<String, Object>> convertBooksToDTO(List<Book> books) {
        List<Map<String, Object>> dtoList = new ArrayList<>();
        for (Book book : books) {
            dtoList.add(convertBookToDTO(book));
        }
        return dtoList;
    }
    
    private Map<String, Object> convertBookToDTO(Book book) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", book.getId());
        dto.put("title", book.getTitle());
        dto.put("author", book.getAuthor());
        dto.put("price", book.getPrice());
        dto.put("originalPrice", book.getOriginalPrice());
        dto.put("category", book.getCategory());
        dto.put("stock", book.getStock());
        dto.put("edition", book.getEdition());
        dto.put("coverImage", book.getCoverImage());
        dto.put("popularity", book.getPopularity());
        dto.put("isFeatured", book.isFeatured());
        dto.put("isDiscounted", book.isDiscounted());
        dto.put("discountPercentage", book.getDiscountPercentage());
        return dto;
    }
    
    // ============== CART MANAGEMENT ==============
    
    public void addToCart(String bookId, int quantity) {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        Book book = bookStore.getBookById(bookId);
        if (customer != null && book != null) {
            if (quantity > book.getStock()) {
                throw new IllegalArgumentException("Not enough stock available");
            }
            customer.getCart().addItem(book, quantity);
        }
    }
    
    public void removeFromCart(String bookId) {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            customer.getCart().removeItem(bookId);
        }
    }
    
    public void updateCartQuantity(String bookId, int quantity) {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            customer.getCart().updateQuantity(bookId, quantity);
        }
    }
    
    public void clearCart() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            customer.getCart().clear();
        }
    }
    
    public double getCartTotal() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        return customer != null ? customer.getCart().getTotal() : 0.0;
    }
    
    public List<Map<String, Object>> getCartItems() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer == null) return new ArrayList<>();
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : customer.getCart().getItems()) {
            Map<String, Object> itemDTO = new HashMap<>();
            itemDTO.put("book", convertBookToDTO(item.getBook()));
            itemDTO.put("quantity", item.getQuantity());
            itemDTO.put("subtotal", item.getSubtotal());
            items.add(itemDTO);
        }
        return items;
    }
    
    // ============== ORDER MANAGEMENT ==============
    
    public String placeOrder() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer == null || customer.getCart().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        Order order = new Order(bookStore.generateOrderId(), customer);
        
        for (OrderItem item : customer.getCart().getItems()) {
            order.addItem(new OrderItem(item.getBook(), item.getQuantity()));
            
            // Get the actual book from the bookstore, not the temporary one from OrderItem
            Book actualBook = bookStore.getBookById(item.getBook().getId());
            if (actualBook != null) {
                actualBook.setStock(actualBook.getStock() - item.getQuantity());
                // Increment popularity by the quantity ordered
                actualBook.setPopularity(actualBook.getPopularity() + item.getQuantity());
            }
        }
        
        bookStore.addOrder(order);
        customer.addOrder(order);
        customer.getCart().clear();
        bookStore.saveAllData();
        
        return order.getOrderId();
    }
    
    public void cancelOrder(String orderId) {
        Order order = bookStore.getOrderById(orderId);
        if (order != null && order.getStatus().equals("PENDING")) {
            order.setStatus("CANCELLED");
            
            for (OrderItem item : order.getItems()) {
                // Get the actual book from the bookstore, not the temporary one from OrderItem
                Book actualBook = bookStore.getBookById(item.getBook().getId());
                if (actualBook != null) {
                    actualBook.setStock(actualBook.getStock() + item.getQuantity());
                    // Decrement popularity since this sale was cancelled
                    actualBook.setPopularity(Math.max(0, actualBook.getPopularity() - item.getQuantity()));
                }
            }
            bookStore.saveAllData();
        }
    }
    
    public List<Map<String, Object>> getCustomerOrderHistory() {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer == null) return new ArrayList<>();
        
        // Get fresh order data from the system to ensure we have the latest status
        List<Order> allOrders = bookStore.getAllOrders();
        List<Map<String, Object>> customerOrders = new ArrayList<>();
        
        for (Order order : allOrders) {
            if (order.getCustomer().getUsername().equals(currentUsername)) {
                customerOrders.add(convertOrderToDTO(order));
            }
        }
        
        return customerOrders;
    }
    
    public Map<String, Object> getOrderDetails(String orderId) {
        Order order = bookStore.getOrderById(orderId);
        return order != null ? convertOrderToDTO(order) : null;
    }
    
    private Map<String, Object> convertOrderToDTO(Order order) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("orderId", order.getOrderId());
        dto.put("customerUsername", order.getCustomer().getUsername());
        dto.put("orderDate", order.getOrderDate().toString());
        dto.put("status", order.getStatus());
        dto.put("totalAmount", order.getTotalAmount());
        
        List<Map<String, Object>> itemsDTO = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("book", convertBookToDTO(item.getBook()));
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("priceAtPurchase", item.getPriceAtPurchase());
            itemMap.put("subtotal", item.getSubtotal());
            itemsDTO.add(itemMap);
        }
        dto.put("items", itemsDTO);
        
        return dto;
    }
    
    // ============== REVIEW MANAGEMENT ==============
    
    public void addReview(String bookId, int rating, String comment) {
        Customer customer = bookStore.getCustomerByUsername(currentUsername);
        if (customer != null) {
            Review review = new Review(bookId, currentUsername, rating, comment);
            customer.addReview(review);
            bookStore.addReview(review);
            bookStore.saveAllData();
        }
    }
    
    public List<Map<String, Object>> getBookReviews(String bookId) {
        List<Review> reviews = bookStore.getReviewsForBook(bookId);
        List<Map<String, Object>> reviewsDTO = new ArrayList<>();
        
        for (Review review : reviews) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("bookId", review.getBookId());
            dto.put("customerUsername", review.getCustomerUsername());
            dto.put("rating", review.getRating());
            dto.put("comment", review.getComment());
            dto.put("reviewDate", review.getReviewDate().toString());
            reviewsDTO.add(dto);
        }
        
        return reviewsDTO;
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
    
    public List<Map<String, Object>> getAllOrders() {
        List<Order> orders = bookStore.getAllOrders();
        List<Map<String, Object>> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            ordersDTO.add(convertOrderToDTO(order));
        }
        return ordersDTO;
    }
    
    public List<Map<String, Object>> getPendingOrders() {
        List<Order> orders = bookStore.getPendingOrders();
        List<Map<String, Object>> ordersDTO = new ArrayList<>();
        for (Order order : orders) {
            ordersDTO.add(convertOrderToDTO(order));
        }
        return ordersDTO;
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
                // Get the actual book from the bookstore, not the temporary one from OrderItem
                Book actualBook = bookStore.getBookById(item.getBook().getId());
                if (actualBook != null) {
                    actualBook.setStock(actualBook.getStock() + item.getQuantity());
                    // Decrement popularity since this sale was cancelled
                    actualBook.setPopularity(Math.max(0, actualBook.getPopularity() - item.getQuantity()));
                }
            }
            bookStore.saveAllData();
        }
    }
    
    // ============== STATISTICS (Admin) ==============
    
    public Map<String, Integer> getCategorySalesStatistics() {
        return bookStore.getCategorySalesStatistics();
    }
    
    public List<Map<String, Object>> getTopSellingBooks(int limit) {
        return convertBooksToDTO(bookStore.getTopSellingBooks(limit));
    }
    
    public double getTotalRevenue() {
        return bookStore.getTotalRevenue();
    }
    
    public int getTotalOrdersCount() {
        return bookStore.getAllOrders().size();
    }
    
    public int getCompletedOrdersCount() {
        return (int) bookStore.getAllOrders().stream()
            .filter(order -> order.getStatus().equals("CONFIRMED") || 
                           order.getStatus().equals("SHIPPED") ||
                           order.getStatus().equals("DELIVERED"))
            .count();
    }
    
    public int getPendingOrdersCount() {
        return bookStore.getPendingOrders().size();
    }
    
    public int getCancelledOrdersCount() {
        return (int) bookStore.getAllOrders().stream()
            .filter(order -> order.getStatus().equals("CANCELLED"))
            .count();
    }
    
    public List<Map<String, String>> getAllCustomers() {
        List<User> customers = bookStore.getAllCustomers();
        List<Map<String, String>> customersDTO = new ArrayList<>();
        
        for (User user : customers) {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                Map<String, String> dto = new HashMap<>();
                dto.put("username", customer.getUsername());
                dto.put("address", customer.getAddress());
                dto.put("phone", customer.getPhone());
                customersDTO.add(dto);
            }
        }
        
        return customersDTO;
    }
    
    // ============== SYSTEM OPERATIONS ==============
    
    public void saveAllData() {
        bookStore.saveAllData();
    }
}