package se.fulkopinglibrary.fulkopinglibrary.models;

public class MediaItem extends LibraryItem implements Media {
    private String director;
    private String catalogNumber;
    private MediaTypeImpl mediaType;

    public MediaItem(int id, String title, boolean isAvailable,
                    String director, String catalogNumber, MediaTypeImpl mediaType) {
        super(id, title, isAvailable);
        this.director = director;
        this.catalogNumber = catalogNumber;
        this.mediaType = mediaType;
        setType(ItemType.MEDIA);
        setMediaType(mediaType);
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public MediaTypeImpl getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaTypeImpl mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public ItemType getType() {
        if (mediaType == null) {
            return ItemType.MEDIA;
        }
        try {
            return ItemType.valueOf(mediaType.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ItemType.MEDIA;
        }
    }

    @Override
    public int getLoanDurationDays() {
        return 10; // Media items have 10 day loan period
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "director='" + director + '\'' +
                ", catalogNumber='" + catalogNumber + '\'' +
                ", mediaType=" + mediaType +
                '}';
    }
}
