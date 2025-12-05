// UserFactory.java - FACTORY METHOD PATTERN
// Based on Lecture 3 - Factory Pattern
public class UserFactory {
    
    public static User createUser(String userType, String username, 
                                   String password, String address, String phone) {
        if (userType == null || userType.isEmpty()) {
            return null;
        }
        
        if (userType.equalsIgnoreCase("CUSTOMER")) {
            return new Customer(username, password, address, phone);
        } else if (userType.equalsIgnoreCase("ADMIN")) {
            return new Admin(username, password);
        }
        
        return null;
    }
    
    public static User createCustomer(String username, String password, 
                                      String address, String phone) {
        return new Customer(username, password, address, phone);
    }
    
    public static User createAdmin(String username, String password) {
        return new Admin(username, password);
    }
}