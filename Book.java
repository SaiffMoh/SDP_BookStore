// Book.java - Interface for Decorator Pattern
public interface Book {
    String getId();
    String getTitle();
    String getAuthor();
    double getPrice();
    double getOriginalPrice();
    String getCategory();
    int getStock();
    String getEdition();
    String getCoverImage();
    int getPopularity();
    boolean isFeatured();
    boolean isDiscounted();
    double getDiscountPercentage();
    
    void setId(String id);
    void setTitle(String title);
    void setAuthor(String author);
    void setPrice(double price);
    void setCategory(String category);
    void setStock(int stock);
    void setEdition(String edition);
    void setCoverImage(String coverImage);
    void setPopularity(int popularity);
    void incrementPopularity();
    
    // Add method to get the base book for JSON serialization
    BasicBook getBaseBook();
}