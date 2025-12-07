// Order.java
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerUsername; // Store username instead of Customer object
    private List<OrderItem> items;
    private double totalAmount;
    private String status;
    private LocalDateTime orderDate;

    public Order(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customerUsername = customer.getUsername();
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.orderDate = LocalDateTime.now();
    }
    
    // Default constructor for JSON
    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    
    public String getCustomerUsername() { return customerUsername; }
    
    // Helper method to get Customer object (used by GUI)
    public Customer getCustomer() {
        // This will be resolved by BookStoreSystem
        return BookStoreSystem.getInstance().getCustomerByUsername(customerUsername);
    }
    
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }
    
    public double getTotalAmount() { return totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getOrderDate() { return orderDate; }

    private void calculateTotal() {
        totalAmount = items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customer=" + customerUsername +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}