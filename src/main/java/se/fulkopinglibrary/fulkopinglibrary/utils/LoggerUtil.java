package se.fulkopinglibrary.fulkopinglibrary.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerUtil {
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(LoggerUtil.class.getName());
        logger.setUseParentHandlers(false);
        
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s: %s%n",
                        record.getLevel(),
                        record.getSourceClassName(),
                        record.getMessage());
            }
        });
        
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }
    
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
}
