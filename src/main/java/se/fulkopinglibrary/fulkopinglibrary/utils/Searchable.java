package se.fulkopinglibrary.fulkopinglibrary.utils;

import java.sql.Connection;
import java.util.List;

public interface Searchable<T> {
    List<T> search(Connection connection, String searchTerm, String searchType, int sortOption, int page, int pageSize) throws Exception;
    String getDisplayHeader();
    String getDisplayRow(T item);
}
