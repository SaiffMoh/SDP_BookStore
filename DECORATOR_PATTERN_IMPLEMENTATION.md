# Decorator Pattern Implementation

## Overview
The bookstore application now properly implements the **Decorator Pattern** for book features (Featured and Discounted).

## Pattern Structure

### 1. Component Interface: `Book`
Defines the common interface for both basic books and decorated books.

### 2. Concrete Component: `BasicBook`
- The base implementation of a book
- Contains core book properties (id, title, author, price, etc.)
- **Key Changes:**
  - `getPrice()` returns the base price only
  - `isFeatured()` returns `false` (decorators handle this)
  - `isDiscounted()` returns `false` (decorators handle this)
  - `getDiscountPercentage()` returns `0.0` (decorators handle this)

### 3. Abstract Decorator: `BookDecorator`
- Implements the `Book` interface
- Holds a reference to a `Book` object
- Delegates all method calls to the wrapped book

### 4. Concrete Decorators:

#### `FeaturedBook`
- Makes a book "featured"
- `isFeatured()` returns `true`
- Delegates other methods to wrapped book

#### `DiscountedBook`
- Applies a discount to a book
- `getPrice()` returns discounted price
- `isDiscounted()` returns `true`
- `getDiscountPercentage()` returns the discount percentage

## Usage Examples

### Creating a Basic Book
```java
Book book = new BasicBook("B001", "Clean Code", "Robert Martin", 
    45.99, "IT", 20, "1st Edition", "cover.jpg");
// book.getPrice() = 45.99
// book.isFeatured() = false
// book.isDiscounted() = false
```

### Creating a Discounted Book
```java
Book book = new BasicBook("B001", "Clean Code", "Robert Martin", 45.99, ...);
Book discountedBook = new DiscountedBook(book, 0.15); // 15% off
// discountedBook.getPrice() = 39.09 (45.99 * 0.85)
// discountedBook.isDiscounted() = true
// discountedBook.getDiscountPercentage() = 15.0
```

### Creating a Featured Book
```java
Book book = new BasicBook("B001", "Clean Code", "Robert Martin", 45.99, ...);
Book featuredBook = new FeaturedBook(book);
// featuredBook.isFeatured() = true
// featuredBook.getPrice() = 45.99 (no discount)
```

### Creating a Featured AND Discounted Book
```java
Book book = new BasicBook("B001", "Clean Code", "Robert Martin", 45.99, ...);
Book discountedBook = new DiscountedBook(book, 0.15);
Book featuredDiscountedBook = new FeaturedBook(discountedBook);
// featuredDiscountedBook.getPrice() = 39.09
// featuredDiscountedBook.isFeatured() = true
// featuredDiscountedBook.isDiscounted() = true
```

## Persistence Strategy

Since decorators are wrapper objects, we need a special strategy for JSON persistence:

### Saving Books
1. Extract the base `BasicBook` from any decorated book
2. Store decorator metadata in the base book's metadata fields (`featured`, `discountPercentage`)
3. Save the `BasicBook` to JSON

### Loading Books
1. Load `BasicBook` objects from JSON (with metadata)
2. Apply decorators based on metadata:
   - If `discountPercentage > 0`, wrap with `DiscountedBook`
   - If `featured == true`, wrap with `FeaturedBook`
3. Return properly decorated books

This is handled by `DataManager.saveBooks()` and `DataManager.loadBooks()`.

## Benefits of Proper Decorator Pattern

1. **Single Responsibility**: Each decorator has one job
2. **Open/Closed Principle**: Can add new decorators without modifying existing code
3. **Flexibility**: Can combine decorators in any order
4. **Runtime Composition**: Can add/remove features at runtime
5. **Clean Separation**: Base component knows nothing about decorations

## Key Files Modified

- [`BasicBook.java`](src/main/java/BasicBook.java) - Pure base component
- [`DataManager.java`](src/main/java/DataManager.java) - Handles decorator persistence
- [`OnlineBookStoreFX.java`](src/main/java/OnlineBookStoreFX.java) - Creates decorated books in UI
- [`BookStoreSystem.java`](src/main/java/BookStoreSystem.java) - Uses decorators for initial data
