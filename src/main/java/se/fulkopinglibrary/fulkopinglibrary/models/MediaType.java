package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Interface for media type information including loan duration
 */
public interface MediaType {
    String TYPE_BOOK = "book";
    String TYPE_MAGAZINE = "magazine";
    String TYPE_MEDIA = "media";
    
    String getType();
    int getLoanDurationDays();
}
