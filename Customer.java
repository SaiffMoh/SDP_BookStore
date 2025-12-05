// Customer.java
import java.util.ArrayList;
import java.util.List;

public class Customer implements User {
    private String username;
    private String password;
    private String address;
    private String phone;
    private ShoppingCart cart;
    private List<Order> orderHistory;
    private List<Review> reviews;

    public Customer(String username, String password, String address, String phone) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.cart = new ShoppingCart();
        this.orderHistory = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    @Override
    public String getUsername() { return username; }
    
    @Override
    public String getPassword() { return password; }
    
    @Override
    public String getUserType() { return "CUSTOMER"; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public ShoppingCart getCart() { return cart; }
    
    public List<Order> getOrderHistory() { return orderHistory; }
    
    public void addOrder(Order order) {
        orderHistory.add(order);
    }
    
    public List<Review> getReviews() { return reviews; }
    
    public void addReview(Review review) {
        reviews.add(review);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "username='" + username + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}