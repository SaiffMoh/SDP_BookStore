// DataManager.java - JSON-based Data Persistence
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataManager {
    private static final String DATA_DIR = "bookstore_data";
    private static final String USERS_FILE = DATA_DIR + "/users.json";
    private static final String BOOKS_FILE = DATA_DIR + "/books.json";
    private static final String ORDERS_FILE = DATA_DIR + "/orders.json";
    private static final String REVIEWS_FILE = DATA_DIR + "/reviews.json";
    private static final String CATEGORIES_FILE = DATA_DIR + "/categories.json";
    private static final String CONFIG_FILE = DATA_DIR + "/config.json";
    
    private Gson gson;

    public DataManager() {
        createDataDirectory();
        initializeGson();
    }

    private void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (created) {
                System.out.println("✓ Created data directory: " + DATA_DIR);
            }
        }
    }

    private void initializeGson() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // ============== SAVE METHODS ==============

    public void saveUsers(List<User> users) {
        // Convert User objects into serializable UserData objects that include userType
        List<UserData> userDataList = new ArrayList<>();
        for (User u : users) {
            UserData ud = new UserData();
            ud.username = u.getUsername();
            ud.password = u.getPassword();
            ud.userType = u.getUserType();

            if (u instanceof Customer) {
                Customer c = (Customer) u;
                ud.address = c.getAddress();
                ud.phone = c.getPhone();
                ud.orderHistory = new ArrayList<>(c.getOrderHistory());
                ud.reviews = new ArrayList<>(c.getReviews());
            } else if (u instanceof Admin) {
                // Admin has no extra fields for now; keep empty lists to preserve structure
                ud.orderHistory = new ArrayList<>();
                ud.reviews = new ArrayList<>();
            } else {
                // Unknown user type: attempt to preserve minimal info
                ud.orderHistory = new ArrayList<>();
                ud.reviews = new ArrayList<>();
            }
            userDataList.add(ud);
        }

        try (Writer writer = new FileWriter(USERS_FILE)) {
            gson.toJson(userDataList, writer);
            System.out.println("✓ Saved " + userDataList.size() + " users");
        } catch (IOException e) {
            System.err.println("✗ Error saving users: " + e.getMessage());
        }
    }

    public void saveBooks(List<Book> books) {
        // Convert decorated books to BasicBook for JSON storage
        // Extract decorator information and store in BasicBook metadata fields
        List<BasicBook> basicBooks = new ArrayList<>();
        for (Book book : books) {
            BasicBook baseBook = book.getBaseBook();
            
            // Store decorator metadata for persistence
            baseBook.setFeatured(book.isFeatured());
            if (book.isDiscounted()) {
                baseBook.setDiscountPercentage(book.getDiscountPercentage() / 100.0);
            } else {
                baseBook.setDiscountPercentage(0.0);
            }
            
            basicBooks.add(baseBook);
        }
        
        try (Writer writer = new FileWriter(BOOKS_FILE)) {
            gson.toJson(basicBooks, writer);
            System.out.println("✓ Saved " + basicBooks.size() + " books");
        } catch (IOException e) {
            System.err.println("✗ Error saving books: " + e.getMessage());
        }
    }

    public void saveOrders(List<Order> orders) {
        try (Writer writer = new FileWriter(ORDERS_FILE)) {
            gson.toJson(orders, writer);
            System.out.println("✓ Saved " + orders.size() + " orders");
        } catch (IOException e) {
            System.err.println("✗ Error saving orders: " + e.getMessage());
        }
    }

    public void saveReviews(List<Review> reviews) {
        try (Writer writer = new FileWriter(REVIEWS_FILE)) {
            gson.toJson(reviews, writer);
            System.out.println("✓ Saved " + reviews.size() + " reviews");
        } catch (IOException e) {
            System.err.println("✗ Error saving reviews: " + e.getMessage());
        }
    }

    public void saveCategories(Set<String> categories) {
        try (Writer writer = new FileWriter(CATEGORIES_FILE)) {
            gson.toJson(categories, writer);
            System.out.println("✓ Saved " + categories.size() + " categories");
        } catch (IOException e) {
            System.err.println("✗ Error saving categories: " + e.getMessage());
        }
    }

    public void saveConfig(Map<String, Object> config) {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
            System.out.println("✓ Saved configuration");
        } catch (IOException e) {
            System.err.println("✗ Error saving config: " + e.getMessage());
        }
    }

    // ============== LOAD METHODS ==============

    public List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved users found");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type userListType = new TypeToken<List<UserData>>(){}.getType();
            List<UserData> userData = gson.fromJson(reader, userListType);
            
            List<User> users = new ArrayList<>();
            for (UserData data : userData) {
                String type = data.userType;

                // If userType is missing (old files), try to infer: common default admin username
                if (type == null) {
                    if (data.username != null && data.username.equalsIgnoreCase("admin")) {
                        type = "ADMIN";
                        System.out.println("⚠ Infering userType=ADMIN for username '" + data.username + "'");
                    } else if ((data.address == null || data.address.isEmpty()) &&
                               (data.phone == null || data.phone.isEmpty()) &&
                               (data.orderHistory == null || data.orderHistory.isEmpty()) &&
                               (data.reviews == null || data.reviews.isEmpty())) {
                        // Heuristic: no customer-specific data -> treat as ADMIN
                        type = "ADMIN";
                        System.out.println("⚠ Infering userType=ADMIN for username '" + data.username + "' (no customer fields)");
                    } else {
                        type = "CUSTOMER";
                        System.out.println("⚠ Infering userType=CUSTOMER for username '" + data.username + "'");
                    }
                }

                if ("ADMIN".equals(type)) {
                    users.add(new Admin(data.username, data.password));
                } else {
                    Customer customer = new Customer(data.username, data.password, 
                                                    data.address, data.phone);
                    if (data.orderHistory != null) {
                        customer.getOrderHistory().addAll(data.orderHistory);
                    }
                    if (data.reviews != null) {
                        customer.getReviews().addAll(data.reviews);
                    }
                    users.add(customer);
                }
            }
            System.out.println("✓ Loaded " + users.size() + " users");
            return users;
        } catch (IOException e) {
            System.err.println("✗ Error loading users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Book> loadBooks() {
        File file = new File(BOOKS_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved books found");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type bookListType = new TypeToken<List<BasicBook>>(){}.getType();
            List<BasicBook> basicBooks = gson.fromJson(reader, bookListType);
            
            // Apply decorators based on metadata
            List<Book> books = new ArrayList<>();
            for (BasicBook basicBook : basicBooks) {
                Book book = basicBook;
                
                // Apply discount decorator if needed
                if (basicBook.getDiscountPercentageMetadata() > 0) {
                    book = new DiscountedBook(book, basicBook.getDiscountPercentageMetadata());
                }
                
                // Apply featured decorator if needed
                if (basicBook.getFeaturedMetadata()) {
                    book = new FeaturedBook(book);
                }
                
                books.add(book);
            }
            
            System.out.println("✓ Loaded " + books.size() + " books");
            return books;
        } catch (IOException e) {
            System.err.println("✗ Error loading books: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Order> loadOrders() {
        File file = new File(ORDERS_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved orders found");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type orderListType = new TypeToken<List<Order>>(){}.getType();
            List<Order> orders = gson.fromJson(reader, orderListType);
            System.out.println("✓ Loaded " + orders.size() + " orders");
            return orders;
        } catch (IOException e) {
            System.err.println("✗ Error loading orders: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> loadReviews() {
        File file = new File(REVIEWS_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved reviews found");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type reviewListType = new TypeToken<List<Review>>(){}.getType();
            List<Review> reviews = gson.fromJson(reader, reviewListType);
            System.out.println("✓ Loaded " + reviews.size() + " reviews");
            return reviews;
        } catch (IOException e) {
            System.err.println("✗ Error loading reviews: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Set<String> loadCategories() {
        File file = new File(CATEGORIES_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved categories found");
            return new HashSet<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type categorySetType = new TypeToken<Set<String>>(){}.getType();
            Set<String> categories = gson.fromJson(reader, categorySetType);
            System.out.println("✓ Loaded " + categories.size() + " categories");
            return categories;
        } catch (IOException e) {
            System.err.println("✗ Error loading categories: " + e.getMessage());
            return new HashSet<>();
        }
    }

    public Map<String, Object> loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            System.out.println("ℹ No saved config found");
            return new HashMap<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type configType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> config = gson.fromJson(reader, configType);
            System.out.println("✓ Loaded configuration");
            return config;
        } catch (IOException e) {
            System.err.println("✗ Error loading config: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ============== HELPER CLASSES ==============

    // Helper class for JSON serialization of User data
    private static class UserData {
        String username;
        String password;
        String userType;
        String address;
        String phone;
        List<Order> orderHistory;
        List<Review> reviews;
    }

    // Adapter for LocalDateTime JSON serialization
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, 
                                                         JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, 
                                    JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, 
                                        JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}