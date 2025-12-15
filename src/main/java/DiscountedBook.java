// DiscountedBook.java - Concrete Decorator (no changes)
public class DiscountedBook extends BookDecorator {
    private double discountPercentage;

    public DiscountedBook(Book book, double discountPercentage) {
        super(book);
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double getPrice() {
        return decoratedBook.getPrice() * (1 - discountPercentage);
    }

    @Override
    public boolean isDiscounted() {
        return true;
    }

    @Override
    public double getDiscountPercentage() {
        return discountPercentage * 100;
    }

    @Override
    public boolean isFeatured() {
        return decoratedBook.isFeatured();
    }

    @Override
    public String toString() {
        return decoratedBook.toString() + " [DISCOUNTED " + 
               (int)(discountPercentage * 100) + "% OFF]";
    }
}