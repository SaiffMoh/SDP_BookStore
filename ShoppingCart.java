// ShoppingCart.java - FIXED
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private List<OrderItem> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public void addItem(Book book, int quantity) {
        // Check if book with same ID already exists
        for (OrderItem item : items) {
            if (item.getBook().getId().equals(book.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Add new item - create OrderItem that captures current book state
        items.add(new OrderItem(book, quantity));
    }

    public void removeItem(String bookId) {
        items.removeIf(item -> item.getBook().getId().equals(bookId));
    }

    public void updateQuantity(String bookId, int quantity) {
        for (OrderItem item : items) {
            if (item.getBook().getId().equals(bookId)) {
                if (quantity <= 0) {
                    removeItem(bookId);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public void clear() {
        items.clear();
    }

    public List<OrderItem> getItems() {
        // Return a copy of the list
        return new ArrayList<>(items);
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    public int getItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}