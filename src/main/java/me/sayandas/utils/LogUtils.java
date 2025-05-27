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

    public static String getFullErrorMessage(Exception e){
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage());
        if(Objects.nonNull(e.getCause())){
            sb.append("Inner Cause: ");
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public static String getFullErrorMessage(SQLException e){
        StringBuilder sb = new StringBuilder();
        sb.append("SQL State: ").append(e.getSQLState()).append(e.getMessage());
        sb.append(" ");
        SQLException nextEx = e.getNextException();
        while(Objects.nonNull(nextEx)){
            sb.append(".").append(nextEx.getMessage());
            nextEx = nextEx.getNextException();
        }

        if(Objects.nonNull(e.getCause())){
            sb.append("Inner Cause: ");
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    public static void logAndThrowException(String logMessagePrefix, Exception e, Logger logger)throws Exception{
        String errorMessage = "";
        if(e instanceof SQLException) errorMessage = getFullErrorMessage((SQLException) e) ;
        else errorMessage = getFullErrorMessage(e);
        logger.severe(logMessagePrefix + " " + errorMessage);
    }

}
