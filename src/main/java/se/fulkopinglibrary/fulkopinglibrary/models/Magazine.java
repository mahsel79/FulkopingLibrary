package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Represents a magazine in the library system
 */
public class Magazine extends LibraryItem implements MediaType {
    private final String publisher;
    private final String issn;

    public Magazine(int id, String title, String publisher, String issn, boolean isAvailable) {
        super(id, title, isAvailable);
        if (publisher == null || publisher.isBlank()) {
            throw new IllegalArgumentException("Publisher cannot be null or blank");
        }
        if (issn == null || issn.isBlank()) {
            throw new IllegalArgumentException("ISSN cannot be null or blank");
        }
        this.publisher = publisher;
        this.issn = issn;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getIssn() {
        return issn;
    }

    @Override
    public String getType() {
        return MediaType.TYPE_MAGAZINE;
    }

    @Override
    public String toString() {
        return "Magazine{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", publisher='" + publisher + '\'' +
                ", issn='" + issn + '\'' +
                ", isAvailable=" + isAvailable() +
                '}';
    }

    @Override
    public int getLoanDurationDays() {
        return 10;
    }
}
