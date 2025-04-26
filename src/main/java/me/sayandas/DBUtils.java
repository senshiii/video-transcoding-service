package me.sayandas;

import java.sql.*;
import java.util.Map;

public class DBUtils {

    public static ResultSet read(Connection connection, String query){
        try(Statement statement = connection.createStatement()){
            return statement.executeQuery(query);
        }catch(SQLException e){
            throw new RuntimeException("Error occurred during executing SQL statement", e);
        }
    }

    public static boolean write(Connection connection, String insertQuery){
        try(Statement statement = connection.createStatement()){
            return statement.execute(insertQuery);
        }catch(SQLException e){
            throw new RuntimeException("Error occurred during inserting data", e);
        }
    }

    public static int update(Connection connection, String updateQuery){
        try(Statement statement = connection.createStatement()){
            return statement.executeUpdate(updateQuery);
        } catch (SQLException e) {
            throw new RuntimeException("Error occurred during update query", e);
        }
    }

    public static Map<String, Object> convertRSToMap(ResultSet rs){
        return null;
    }

    private void logAllWarnings(ResultSet rs){
        // TODO: Print all warnings
    }

}
