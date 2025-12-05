// OrderItem.java
public class OrderItem {
    private Book book;
    private int quantity;
    private double priceAtPurchase;

    public OrderItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
        this.priceAtPurchase = book.getPrice();
    }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getPriceAtPurchase() { return priceAtPurchase; }
    
    public double getSubtotal() {
        return priceAtPurchase * quantity;
    }

    @Override
    public String toString() {
        return book.getTitle() + " x " + quantity + " = $" + 
               String.format("%.2f", getSubtotal());
    }
}