module se.fulkopinglibrary.fulkopinglibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires java.desktop;
    requires com.zaxxer.hikari;

    opens se.fulkopinglibrary.fulkopinglibrary to javafx.fxml;
    exports se.fulkopinglibrary.fulkopinglibrary;
}
