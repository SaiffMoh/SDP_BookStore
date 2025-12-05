// Order.java
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private Customer customer;
    private List<OrderItem> items;
    private double totalAmount;
    private String status;
    private LocalDateTime orderDate;

    public Order(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.orderDate = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    
    public Customer getCustomer() { return customer; }
    
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
                ", customer=" + customer.getUsername() +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}