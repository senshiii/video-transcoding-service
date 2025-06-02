package me.sayandas.utils;

import java.sql.SQLException;
import java.util.Objects;

public class LogUtils {

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

    public static void logAndThrowException(String logMessagePrefix, Exception e, org.apache.logging.log4j.Logger logger)throws Exception{
        String errorMessage = "";
        if(e instanceof SQLException) errorMessage = getFullErrorMessage((SQLException) e) ;
        else errorMessage = getFullErrorMessage(e);
        logger.error(logMessagePrefix + " {}", errorMessage);
    }

}
