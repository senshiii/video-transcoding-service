package me.sayandas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

    public ResultSet fetch(Connection connection, String query){
        try(Statement statement = connection.createStatement()){
            return statement.executeQuery(query);
        }catch(SQLException e){
            throw new RuntimeException("Error occurred during executing SQL statement", e);
        }
    }

}
