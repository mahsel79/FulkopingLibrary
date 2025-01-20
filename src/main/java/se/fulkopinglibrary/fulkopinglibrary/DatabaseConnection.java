package se.fulkopinglibrary.fulkopinglibrary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connections using connection pooling
 */
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    static {
        try {
            // Get database credentials from environment variables
            String dbUrl = System.getenv("DB_URL");
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            
            if (dbUrl == null || dbUser == null || dbPassword == null) {
                throw new ExceptionInInitializerError("Database environment variables not configured");
            }
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);
            config.setLeakDetectionThreshold(5000);
            
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
