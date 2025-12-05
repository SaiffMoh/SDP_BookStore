// FeaturedBook.java - DECORATOR PATTERN (Concrete Decorator)
// Based on Lecture 4 - Decorator Pattern
public class FeaturedBook extends BookDecorator {

    public FeaturedBook(Book book) {
        super(book);
    }

    @Override
    public double getPrice() {
        return decoratedBook.getPrice();
    }

    @Override
    public boolean isFeatured() {
        return true;
    }

    @Override
    public boolean isDiscounted() {
        return decoratedBook.isDiscounted();
    }

    @Override
    public double getDiscountPercentage() {
        return decoratedBook.getDiscountPercentage();
    }

    @Override
    public String toString() {
        return decoratedBook.toString() + " [FEATURED]";
    }
}