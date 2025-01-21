package se.fulkopinglibrary.fulkopinglibrary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.fulkopinglibrary.fulkopinglibrary.utils.LoggerUtil;

/**
 * Manages database connections for the Fulköping Library system.
 * 
 * <p>This class implements a connection pool using HikariCP to efficiently manage database
 * connections. It provides methods to get connections from the pool and release them when done.
 * The class uses a singleton pattern to ensure a single connection pool instance across the
 * application.</p>
 *
 * <p>Connection parameters are loaded from environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * and the pool is configured with optimal settings for the library system.</p>
 *
 * @author Library Development Team
 * @version 1.0
 */
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    private static final Logger logger = LoggerUtil.getLogger(DatabaseConnection.class);

    static {
        try {
            // Load .env file if it exists
            // Load .env file if it exists
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
            
            // Get database credentials from environment variables
            String dbUrl = dotenv.get("DB_URL");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");
            
            // Additional debug logging
            System.out.println("JDBC URL: " + dbUrl);
            System.out.println("Username: " + dbUser);
            System.out.println("Password: " + (dbPassword != null ? "*****" : "null"));
            
            logger.config("DB_URL: " + dbUrl);
            logger.config("DB_USER: " + dbUser);
            logger.config("DB_PASSWORD: " + (dbPassword != null ? "*****" : "null"));
            
            if (dbUrl == null || dbUser == null || dbPassword == null) {
                logger.severe("Missing database environment variables:");
                logger.severe("DB_URL: " + dbUrl);
                logger.severe("DB_USER: " + dbUser);
                logger.severe("DB_PASSWORD: " + (dbPassword != null ? "*****" : "null"));
                throw new ExceptionInInitializerError("Database environment variables not configured");
            }
            
            // Log MySQL driver version
            try {
                Class<?> mysqlDriverClass = Class.forName("com.mysql.cj.jdbc.Driver");
                logger.info("MySQL Driver loaded: " + mysqlDriverClass.getPackage().getImplementationVersion());
            } catch (ClassNotFoundException e) {
                logger.severe("MySQL Driver not found!");
                throw new ExceptionInInitializerError("MySQL Driver not found: " + e.getMessage());
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(30000);

            config.setLeakDetectionThreshold(30000);
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            logger.config("Initializing HikariCP connection pool with configuration:");
            logger.config("Max pool size: " + config.getMaximumPoolSize());
            logger.config("Min idle connections: " + config.getMinimumIdle());
            logger.config("Connection timeout: " + config.getConnectionTimeout());
            
            dataSource = new HikariDataSource(config);
            logger.info("Successfully initialized database connection pool");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize database connection pool", e);
            throw new ExceptionInInitializerError("Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing database connection pool");
            
            // Add HikariCP configuration to handle abandoned connections
            dataSource.getHikariConfigMXBean().setIdleTimeout(30000);
            dataSource.getHikariConfigMXBean().setMaxLifetime(60000);
            dataSource.getHikariConfigMXBean().setLeakDetectionThreshold(5000);
            
            // Close the connection pool
            dataSource.close();
            
            // Forcefully interrupt MySQL connection cleanup thread
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            for (Thread thread : threads) {
                if (thread != null && thread.getName().contains("mysql-cj-abandoned-connection-cleanup")) {
                    thread.interrupt();
                }
            }
            
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Tests the database connection by executing a simple query
     * @return true if connection test succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Testing database connection...");
            if (conn.isValid(5)) {
                logger.info("Connection test successful");
                return true;
            }
            logger.warning("Connection test failed - connection not valid");
            return false;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Connection test failed", e);
            return false;
        }
    }
}
