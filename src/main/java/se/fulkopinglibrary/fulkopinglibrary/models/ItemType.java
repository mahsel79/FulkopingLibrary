package se.fulkopinglibrary.fulkopinglibrary.models;

public enum ItemType {
    BOOK,
    MAGAZINE,
    MEDIA;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
