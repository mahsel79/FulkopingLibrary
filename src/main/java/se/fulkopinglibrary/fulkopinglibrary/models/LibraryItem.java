package se.fulkopinglibrary.fulkopinglibrary.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


/**
 * Base class for all library items with common properties
 */
public abstract class LibraryItem {
    private static final int RESERVATION_PERIOD_DAYS = 30;
    
    private final int id;
    private final String title;
    private boolean isAvailable;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private LocalDate reservationDate;
    protected MediaType mediaType;
    protected ItemType type;
    private int loanPeriodDays;

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

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public void setLoanPeriodDays(int days) {
        this.loanPeriodDays = days;
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

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getReservationExpirationDate() {
        if (reservationDate == null) {
            return null;
        }
        return reservationDate.plus(RESERVATION_PERIOD_DAYS, ChronoUnit.DAYS);
    }

    public int getLoanDurationDays() {
        return mediaType != null ? mediaType.getLoanDurationDays() : loanPeriodDays;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isAvailable=" + isAvailable +
                ", loanDate=" + loanDate +
                ", returnDate=" + returnDate +
                ", reservationDate=" + reservationDate +
                ", reservationExpires=" + getReservationExpirationDate() +
                ", loanDurationDays=" + getLoanDurationDays() +
                '}';
    }
}
