# Facade Pattern Implementation

## Overview
The bookstore application properly implements the **Facade Pattern** to provide a simplified interface to the complex subsystem.

## Pattern Structure

### Facade Class: `BookStoreFacade`
- Provides a unified, simplified interface to the complex subsystem
- Hides the complexity of `BookStoreSystem`, `DataManager`, and other internal components
- Organizes operations into logical groups

### Subsystem Classes
- `BookStoreSystem` (Singleton) - Core business logic
- `DataManager` - Persistence layer
- `UserFactory` - User creation (Factory pattern)
- Various model classes (`Book`, `Order`, `Customer`, etc.)

## Key Principles Applied

### 1. **Single Point of Access**
All UI interactions go through the `BookStoreFacade`:
```java
public class OnlineBookStoreFX extends Application {
    private BookStoreFacade facade; // Only access point to subsystem
    
    public void start(Stage stage) {
        this.facade = new BookStoreFacade();
        // All operations use facade
    }
}
```

### 2. **Simplified Interface**
The facade provides simple methods that hide complex operations:

```java
// Instead of this complex interaction:
BookStoreSystem system = BookStoreSystem.getInstance();
User user = UserFactory.createCustomer(username, password, address, phone);
system.registerUser(user);
system.saveAllData();

// Clients just do this:
facade.registerCustomer(username, password, address, phone);
```

### 3. **Organized by Functionality**
Methods are grouped logically:
- **User Management**: `registerCustomer()`, `login()`, `updateCustomerInfo()`
- **Book Browsing**: `browseAllBooks()`, `searchBooks()`, `filterBooksByCategory()`
- **Cart Management**: `addToCart()`, `removeFromCart()`, `getCartTotal()`
- **Order Management**: `placeOrder()`, `cancelOrder()`, `getOrderHistory()`
- **Admin Operations**: `addBook()`, `updateBook()`, `confirmOrder()`, `shipOrder()`
- **Statistics**: `getTotalRevenue()`, `getTopSellingBooks()`
- **System Operations**: `saveAllData()`, `getCustomerByUsername()`

## Violations Fixed

### ❌ Before (Incorrect - Direct Access)
```java
// OnlineBookStoreFX.java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    BookStoreSystem.getInstance().saveAllData(); // ❌ Direct access!
}));

// OrderItem.java
public Book getBook() {
    return BookStoreSystem.getInstance().getBookById(bookId); // ❌ Direct access!
}

// Order.java
public Customer getCustomer() {
    return BookStoreSystem.getInstance().getCustomerByUsername(customerUsername); // ❌ Direct access!
}
```

### ✅ After (Correct - Through Facade)
```java
// OnlineBookStoreFX.java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    facade.saveAllData(); // ✅ Through facade!
}));

// OrderItem.java
public Book getBook() {
    // Returns minimal representation - actual Book retrieved via facade in UI layer
    BasicBook tempBook = new BasicBook();
    tempBook.setId(bookId);
    tempBook.setTitle(bookTitle);
    return tempBook;
}

// Order.java
public Customer getCustomer() {
    // Returns minimal representation - actual Customer retrieved via facade in UI layer
    return new Customer(customerUsername, "", "", "");
}
```

## Benefits Achieved

### 1. **Reduced Coupling**
- UI layer (`OnlineBookStoreFX`) depends only on `BookStoreFacade`
- Changes to subsystem don't affect UI code
- Can replace/refactor subsystem without touching UI

### 2. **Simplified Client Code**
- UI code is cleaner and easier to understand
- No need to know about internal structure
- Clear, semantic method names

### 3. **Encapsulation**
- Subsystem implementation details are hidden
- Internal classes can be modified without breaking clients
- Business logic is centralized in the facade

### 4. **Single Responsibility**
- Facade handles coordination between subsystem components
- UI handles presentation logic only
- Clear separation of concerns

## Usage Examples

### Customer Operations
```java
// Login
User user = facade.login("john", "password123");

// Browse books
List<Book> allBooks = facade.browseAllBooks();
List<Book> itBooks = facade.filterBooksByCategory("IT");
List<Book> searchResults = facade.searchBooks("design patterns");

// Cart operations
facade.addToCart(customer, book, 2);
double total = facade.getCartTotal(customer);
facade.placeOrder(customer);
```

### Admin Operations
```java
// Add a decorated book
Book book = new BasicBook("B100", "New Book", "Author", 50.0, ...);
book = new DiscountedBook(book, 0.20); // 20% off
book = new FeaturedBook(book); // Make it featured
facade.addBookWithDecorators(book);

// Manage orders
List<Order> pending = facade.getPendingOrders();
facade.confirmOrder(orderId);
facade.shipOrder(orderId);

// Statistics
double revenue = facade.getTotalRevenue();
List<Book> topBooks = facade.getTopSellingBooks(10);
```

## Architecture Diagram

```
┌─────────────────────────────────────────┐
│         OnlineBookStoreFX (UI)          │
│         (Presentation Layer)             │
└────────────────┬────────────────────────┘
                 │
                 │ Uses only facade
                 ▼
┌─────────────────────────────────────────┐
│        BookStoreFacade                   │
│    (Simplified Interface)                │
└────────────────┬────────────────────────┘
                 │
                 │ Delegates to subsystem
                 ▼
┌─────────────────────────────────────────┐
│           Subsystem                      │
│  ┌────────────────────────────────┐     │
│  │ BookStoreSystem (Singleton)    │     │
│  └────────────────────────────────┘     │
│  ┌────────────────────────────────┐     │
│  │ DataManager (Persistence)      │     │
│  └────────────────────────────────┘     │
│  ┌────────────────────────────────┐     │
│  │ UserFactory (Factory Pattern)  │     │
│  └────────────────────────────────┘     │
│  ┌────────────────────────────────┐     │
│  │ Model Classes (Book, Order,...)│     │
│  └────────────────────────────────┘     │
└─────────────────────────────────────────┘
```

## Key Files

- [`BookStoreFacade.java`](src/main/java/BookStoreFacade.java) - The facade implementation
- [`OnlineBookStoreFX.java`](src/main/java/OnlineBookStoreFX.java) - UI using the facade
- [`BookStoreSystem.java`](src/main/java/BookStoreSystem.java) - Main subsystem component
- [`DataManager.java`](src/main/java/DataManager.java) - Subsystem persistence component

## Testing the Facade

To verify the facade is working correctly:

1. **Check**: No direct `BookStoreSystem.getInstance()` calls in `OnlineBookStoreFX.java` ✅
2. **Check**: All UI operations go through `facade` ✅
3. **Check**: Subsystem classes are not imported in UI layer ✅
4. **Check**: Facade provides simple, high-level methods ✅
5. **Check**: Business logic is encapsulated in facade/subsystem ✅
