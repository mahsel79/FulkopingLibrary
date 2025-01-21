package se.fulkopinglibrary.fulkopinglibrary.models;

/**
 * Base class for all library media items
 */
public interface Media {
    ItemType getType();
    int getLoanDurationDays();
}
