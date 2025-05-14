package me.sayandas.utils;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogUtils {

    public static Logger getLoggerWithConsoleHandler(String loggerName){
        return LogUtils.getLoggerWithConsoleHandler(loggerName, Level.ALL);
    }

    public static Logger getLoggerWithConsoleHandler(String loggerName, Level level){
        Logger logger = Logger.getLogger(loggerName);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(level);
        logger.addHandler(ch);
        logger.setLevel(level);
        return logger;
    }

    public static String getFullErrorMessage(String messagePrefix, Exception e){
        StringBuilder sb = new StringBuilder();
        sb.append(messagePrefix);
        sb.append(" ");
        sb.append(e.getMessage());
        if(Objects.nonNull(e.getCause())){
            sb.append("Inner Cause: ");
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public static String getFullErrorMessage(String messagePrefix, SQLException e){
        StringBuilder sb = new StringBuilder();
        sb.append(messagePrefix)
                . append(" - ")
                .append("SQL State: ").append(e.getSQLState()).
                append(e.getMessage());

        SQLException nextEx = e.getNextException();
        while(Objects.nonNull(nextEx)){
            sb.append(".").append(nextEx.getMessage());
        }

        if(Objects.nonNull(e.getCause())){
            sb.append("Inner Cause: ");
            sb.append(e.getMessage());
        }

        return sb.toString();
    }

}
