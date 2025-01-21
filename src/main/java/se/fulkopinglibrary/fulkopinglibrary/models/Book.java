package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Represents a book in the library system
 */
public class Book extends LibraryItem implements BookItem {
    private final String author;
    private final String isbn;

    public Book(int id, String title, String author, String isbn, boolean isAvailable) {
        super(id, title, isAvailable);
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Author cannot be null or blank");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
        this.author = author;
        this.isbn = isbn;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getIsbn() {
        return isbn;
    }

    @Override
    public ItemType getType() {
        return ItemType.BOOK;
    }

    @Override
    public int getLoanDurationDays() {
        return mediaType != null ? mediaType.getLoanDurationDays() : 30;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") + 
               ", author='" + author + '\'' +
               ", isbn='" + isbn + '\'' +
               '}';
    }
}
