package me.sayandas.utils;

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

}
