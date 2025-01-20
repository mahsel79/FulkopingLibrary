package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Base class for all library items with common properties
 */
public abstract class LibraryItem implements MediaType {
    private final int id;
    private final String title;
    private boolean isAvailable;

    protected LibraryItem(int id, String title, boolean isAvailable) {
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
        LibraryItem that = (LibraryItem) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
