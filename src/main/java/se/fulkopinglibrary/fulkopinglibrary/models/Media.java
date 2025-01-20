package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Base class for all library media items
 */
public abstract class Media {
    public static final String TYPE_BOOK = "book";
    public static final String TYPE_MAGAZINE = "magazine";
    
    private final int id;
    private final String title;
    private boolean isAvailable;

    protected Media(int id, String title, boolean isAvailable) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        this.id = id;
        this.title = title;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Media media = (Media) o;
        return id == media.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public abstract String getType();
    
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
