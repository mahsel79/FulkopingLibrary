package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Implementation of the MediaType interface representing different types of media in the library.
 * Each media type has a specific loan duration and type name.
 */
public class MediaTypeImpl implements MediaType {
    private final String type;
    private final int loanDurationDays;

    /**
     * Constructs a new MediaTypeImpl with the specified type and loan duration.
     * 
     * @param type The type of media (e.g., DVD, CD, Blu-ray)
     * @param loanDurationDays The maximum number of days this media type can be borrowed
     */
    public MediaTypeImpl(String type, int loanDurationDays) {
        this.type = type;
        this.loanDurationDays = loanDurationDays;
    }

    /**
     * Returns the type of media.
     * @return The media type as a string
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Returns the maximum loan duration in days for this media type.
     * @return The loan duration in days
     */
    public int getLoanDurationDays() {
        return loanDurationDays;
    }

    // Predefined media types with their respective loan durations
    public static final MediaTypeImpl DVD = new MediaTypeImpl("DVD", 10);
    public static final MediaTypeImpl CD = new MediaTypeImpl("CD", 10);
    public static final MediaTypeImpl BLURAY = new MediaTypeImpl("Blu-ray", 10);
    public static final MediaTypeImpl VINYL = new MediaTypeImpl("Vinyl", 10);
    public static final MediaTypeImpl AUDIOBOOK = new MediaTypeImpl("Audiobook", 30);
    public static final MediaTypeImpl BOOK = new MediaTypeImpl("Book", 30);
    public static final MediaTypeImpl MAGAZINE = new MediaTypeImpl("Magazine", 10);

    /**
     * Returns a string representation of the media type.
     * @return The media type as a string
     */
    @Override
    public String toString() {
        return type;
    }

    /**
     * Converts a string to the corresponding MediaTypeImpl instance.
     * @param type The string representation of the media type
     * @return The corresponding MediaTypeImpl instance
     * @throws IllegalArgumentException if the type is null or unknown
     */
    public static MediaTypeImpl fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Media type cannot be null");
        }
        
        switch (type.toUpperCase()) {
            case "DVD": return DVD;
            case "CD": return CD;
            case "BLU-RAY":
            case "BLURAY": return BLURAY;
            case "VINYL": return VINYL;
            case "AUDIOBOOK": return AUDIOBOOK;
            case "BOOK": return BOOK;
            case "MAGAZINE": return MAGAZINE;
            default: 
                throw new IllegalArgumentException("Unknown media type: " + type);
        }
    }
}
