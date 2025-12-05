// Review.java
import java.time.LocalDateTime;

public class Review {
    private String bookId;
    private String customerUsername;
    private int rating;
    private String comment;
    private LocalDateTime reviewDate;

    public Review(String bookId, String customerUsername, int rating, String comment) {
        this.bookId = bookId;
        this.customerUsername = customerUsername;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comment = comment;
        this.reviewDate = LocalDateTime.now();
    }

    public String getBookId() { return bookId; }
    public String getCustomerUsername() { return customerUsername; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getReviewDate() { return reviewDate; }

    @Override
    public String toString() {
        return "Review by " + customerUsername + " - Rating: " + rating + "/5\n" +
               "Comment: " + comment + "\n" +
               "Date: " + reviewDate;
    }
}