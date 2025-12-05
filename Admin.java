// Admin.java
public class Admin implements User {
    private String username;
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getUsername() { return username; }
    
    @Override
    public String getPassword() { return password; }
    
    @Override
    public String getUserType() { return "ADMIN"; }

    @Override
    public String toString() {
        return "Admin{username='" + username + "'}";
    }
}