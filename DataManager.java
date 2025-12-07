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
        try (Writer writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
            System.out.println("✓ Saved " + users.size() + " users");
        } catch (IOException e) {
            System.err.println("✗ Error saving users: " + e.getMessage());
        }
    }

    public void saveBooks(List<Book> books) {
        // Convert decorated books to BasicBook for JSON storage
        List<BasicBook> basicBooks = new ArrayList<>();
        for (Book book : books) {
            basicBooks.add(book.getBaseBook());
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
                if ("ADMIN".equals(data.userType)) {
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
            List<BasicBook> books = gson.fromJson(reader, bookListType);
            System.out.println("✓ Loaded " + books.size() + " books");
            return new ArrayList<>(books);
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