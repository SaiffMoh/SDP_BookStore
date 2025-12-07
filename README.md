
# SDP_BookStore

## Project Overview

SDP_BookStore is a comprehensive Java application for managing an online book store. It supports customer and admin roles, book browsing, shopping cart, order management, reviews, and category/statistics management. The project features a modern GUI built with Java Swing and uses JSON for persistent data storage.

---

## Features

- **Customer:**
  - Sign up and login
  - Browse, search, and filter books
  - Add books to cart and place orders
  - View order history
  - Add and view reviews

- **Admin:**
  - Manage books (add, update, delete)
  - Manage categories
  - View and manage orders
  - View sales statistics

- **General:**
  - Full GUI (Swing)
  - JSON-based data persistence
  - Separation of concerns (business logic, data, presentation)

---

## Design Patterns Used

### 1. Factory Method Pattern
- **Class:** `UserFactory`
- **Purpose:** Encapsulates user creation logic for `Customer` and `Admin` objects.
- **Benefit:** Centralizes and abstracts user instantiation, making it easy to add new user types.

### 2. Facade Pattern
- **Class:** `BookStoreFacade`
- **Purpose:** Provides a simplified interface for all book store operations (user management, book browsing, cart, orders, reviews, admin tasks).
- **Benefit:** Hides the complexity of the underlying system (`BookStoreSystem`, cart, orders, etc.), allowing the GUI and other clients to interact with a single, easy-to-use API.

### 3. Decorator Pattern
- **Classes:** `Book` (interface), `BasicBook` (concrete component), `BookDecorator` (abstract decorator), `DiscountedBook`, `FeaturedBook` (concrete decorators)
- **Purpose:** Adds flexible features (discounts, featured status) to books at runtime without modifying the base class.
- **Benefit:** Enables dynamic extension of book functionality (e.g., marking as featured, applying discounts) while keeping code modular and maintainable.

### 4. Singleton Pattern
- **Class:** `BookStoreSystem`
- **Purpose:** Ensures a single instance of the core system managing books, users, orders, reviews, and categories.
- **Benefit:** Guarantees consistent state and centralized data management throughout the application.

---

## Main Classes

- `OnlineBookStoreGUI`: Main GUI class, handles all user interactions and views.
- `BookStoreFacade`: Facade for all business logic and data operations.
- `BookStoreSystem`: Singleton class managing all core data (books, users, orders, reviews, categories).
- `DataManager`: Handles JSON-based persistence for all entities.
- `UserFactory`: Factory for creating `Customer` and `Admin` users.
- `Book`, `BasicBook`, `BookDecorator`, `DiscountedBook`, `FeaturedBook`: Decorator pattern for flexible book features.
- `Customer`, `Admin`: User types.
- `Order`, `OrderItem`, `Review`, `ShoppingCart`: Core business entities.

---

## Data Persistence

- All data (books, users, orders, reviews, categories, config) is stored in JSON files under the `bookstore_data/` directory.
- The `DataManager` class handles serialization and deserialization.

---

## How to Run

1. Compile all `.java` files in the project directory.
2. Run `OnlineBookStoreGUI.java` to launch the application:
   ```
   javac *.java
   java OnlineBookStoreGUI
   ```

---

## Requirements Met

- ✅ Customer: Sign up, login, browse, search, filter, cart, orders, reviews
- ✅ Admin: Manage books, categories, orders, statistics
- ✅ Full GUI with Swing
- ✅ 3+ Design Patterns implemented

---

## Credits

- Developed by SaiffMoh
- Based on SDP lectures and design pattern best practices