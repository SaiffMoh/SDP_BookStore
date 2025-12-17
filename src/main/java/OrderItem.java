// OrderItem.java
public class OrderItem {
    private String bookId; // Store book ID instead of Book object
    private String bookTitle;
    private String bookAuthor;
    private String bookCategory;
    private int quantity;
    private double priceAtPurchase;

    public OrderItem(Book book, int quantity) {
        this.bookId = book.getId();
        this.bookTitle = book.getTitle();
        this.bookAuthor = book.getAuthor();
        this.bookCategory = book.getCategory();
        this.quantity = quantity;
        this.priceAtPurchase = book.getPrice();
    }
    
    // Default constructor for JSON
    public OrderItem() {
    }

    // Helper method to get Book object (used by GUI)
    // Note: Book should be provided by the calling context (via Facade)
    // This is kept for backward compatibility with existing saved orders
    public Book getBook() {
        // Return a temporary book representation with stored data
        // The actual Book object should be retrieved through the Facade layer
        BasicBook tempBook = new BasicBook();
        tempBook.setId(bookId);
        tempBook.setTitle(bookTitle);
        tempBook.setAuthor(bookAuthor != null ? bookAuthor : "Unknown");
        tempBook.setCategory(bookCategory != null ? bookCategory : "");
        tempBook.setPrice(priceAtPurchase);
        return tempBook;
    }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getPriceAtPurchase() { return priceAtPurchase; }
    
    public double getSubtotal() {
        return priceAtPurchase * quantity;
    }

    @Override
    public String toString() {
        return bookTitle + " x " + quantity + " = $" + 
               String.format("%.2f", getSubtotal());
    }
}