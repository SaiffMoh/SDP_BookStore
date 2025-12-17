// BasicBook.java - Concrete Component for Decorator
public class BasicBook implements Book {
    private String id;
    private String title;
    private String author;
    private double price;
    private String category;
    private int stock;
    private String edition;
    private String coverImage;
    private int popularity;
    // Note: For JSON serialization/deserialization, we store decorator metadata
    // These are NOT used by BasicBook itself - only for persistence
    private boolean featured;
    private double discountPercentage;

    public BasicBook(String id, String title, String author, double price, 
                     String category, int stock, String edition, String coverImage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.edition = edition;
        this.coverImage = coverImage;
        this.popularity = 0;
        // Metadata for persistence only
        this.featured = false;
        this.discountPercentage = 0.0;
    }
    
    // Default constructor for JSON deserialization
    public BasicBook() {
        this.popularity = 0;
        this.featured = false;
        this.discountPercentage = 0.0;
    }

    @Override
    public String getId() { return id; }
    
    @Override
    public void setId(String id) { this.id = id; }
    
    @Override
    public String getTitle() { return title; }
    
    @Override
    public void setTitle(String title) { this.title = title; }
    
    @Override
    public String getAuthor() { return author; }
    
    @Override
    public void setAuthor(String author) { this.author = author; }
    
    @Override
    public double getPrice() { 
        return price; // Base component always returns base price
    }
    
    @Override
    public double getOriginalPrice() { 
        return price; // Always return base price, not discounted
    }
    
    @Override
    public void setPrice(double price) { this.price = price; }
    
    @Override
    public String getCategory() { return category; }
    
    @Override
    public void setCategory(String category) { this.category = category; }
    
    @Override
    public int getStock() { return stock; }
    
    @Override
    public void setStock(int stock) { this.stock = stock; }
    
    @Override
    public String getEdition() { return edition; }
    
    @Override
    public void setEdition(String edition) { this.edition = edition; }
    
    @Override
    public String getCoverImage() { return coverImage; }
    
    @Override
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    @Override
    public int getPopularity() { return popularity; }
    
    @Override
    public void setPopularity(int popularity) { this.popularity = popularity; }
    
    @Override
    public void incrementPopularity() { this.popularity++; }
    
    @Override
    public boolean isFeatured() { 
        return false; // Base component is never featured - use FeaturedBook decorator
    }
    
    // For JSON persistence metadata only - DO NOT use in business logic
    public void setFeatured(boolean featured) { this.featured = featured; }
    
    // Package-private getter for persistence metadata only
    boolean getFeaturedMetadata() { return this.featured; }
    
    @Override
    public boolean isDiscounted() { 
        return false; // Base component is never discounted - use DiscountedBook decorator
    }
    
    @Override
    public double getDiscountPercentage() { 
        return 0.0; // Base component has no discount - use DiscountedBook decorator
    }
    
    // For JSON persistence metadata only - DO NOT use in business logic
    public void setDiscountPercentage(double discountPercentage) { 
        this.discountPercentage = discountPercentage; 
    }
    
    // Package-private getter for persistence metadata only
    double getDiscountPercentageMetadata() { return this.discountPercentage; }
    
    @Override
    public BasicBook getBaseBook() { return this; }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }
}