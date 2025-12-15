// OrderItem.java
public class OrderItem {
    private String bookId; // Store book ID instead of Book object
    private String bookTitle;
    private int quantity;
    private double priceAtPurchase;

    public OrderItem(Book book, int quantity) {
        this.bookId = book.getId();
        this.bookTitle = book.getTitle();
        this.quantity = quantity;
        this.priceAtPurchase = book.getPrice();
    }
    
    // Default constructor for JSON
    public OrderItem() {
    }

    // Helper method to get Book object (used by GUI)
    public Book getBook() {
        Book book = BookStoreSystem.getInstance().getBookById(bookId);
        if (book == null) {
            // Create a temporary book if original is deleted
            BasicBook tempBook = new BasicBook();
            tempBook.setId(bookId);
            tempBook.setTitle(bookTitle);
            tempBook.setPrice(priceAtPurchase);
            return tempBook;
        }
        return book;
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