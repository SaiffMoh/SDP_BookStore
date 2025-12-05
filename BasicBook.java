// BasicBook.java - DECORATOR PATTERN (Concrete Component)
// Based on Lecture 4 - Decorator Pattern
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
    public double getPrice() { return price; }
    
    @Override
    public double getOriginalPrice() { return price; }
    
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
    public boolean isFeatured() { return false; }
    
    @Override
    public boolean isDiscounted() { return false; }
    
    @Override
    public double getDiscountPercentage() { return 0.0; }

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