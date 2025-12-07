// BookDecorator.java - Abstract Decorator
public abstract class BookDecorator implements Book {
    protected Book decoratedBook;

    public BookDecorator(Book book) {
        this.decoratedBook = book;
    }

    @Override
    public String getId() { return decoratedBook.getId(); }
    
    @Override
    public void setId(String id) { decoratedBook.setId(id); }
    
    @Override
    public String getTitle() { return decoratedBook.getTitle(); }
    
    @Override
    public void setTitle(String title) { decoratedBook.setTitle(title); }
    
    @Override
    public String getAuthor() { return decoratedBook.getAuthor(); }
    
    @Override
    public void setAuthor(String author) { decoratedBook.setAuthor(author); }
    
    @Override
    public double getOriginalPrice() { return decoratedBook.getOriginalPrice(); }
    
    @Override
    public void setPrice(double price) { decoratedBook.setPrice(price); }
    
    @Override
    public String getCategory() { return decoratedBook.getCategory(); }
    
    @Override
    public void setCategory(String category) { decoratedBook.setCategory(category); }
    
    @Override
    public int getStock() { return decoratedBook.getStock(); }
    
    @Override
    public void setStock(int stock) { decoratedBook.setStock(stock); }
    
    @Override
    public String getEdition() { return decoratedBook.getEdition(); }
    
    @Override
    public void setEdition(String edition) { decoratedBook.setEdition(edition); }
    
    @Override
    public String getCoverImage() { return decoratedBook.getCoverImage(); }
    
    @Override
    public void setCoverImage(String coverImage) { decoratedBook.setCoverImage(coverImage); }
    
    @Override
    public int getPopularity() { return decoratedBook.getPopularity(); }
    
    @Override
    public void setPopularity(int popularity) { decoratedBook.setPopularity(popularity); }
    
    @Override
    public void incrementPopularity() { decoratedBook.incrementPopularity(); }
    
    @Override
    public BasicBook getBaseBook() { return decoratedBook.getBaseBook(); }
    
    @Override
    public abstract double getPrice();
    
    @Override
    public abstract boolean isFeatured();
    
    @Override
    public abstract boolean isDiscounted();
    
    @Override
    public abstract double getDiscountPercentage();
}