package me.sayandas;

import me.sayandas.video.VideoResolution;
import me.sayandas.video.VideoResolutionProbeResult;
import me.sayandas.video.VideoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;


public class App 
{
    public static void main( String[] args ) throws IOException, SQLException {
        Connection dbConn = DriverManager.getConnection(
                "jdbc:mysql://video-transcoder-service.c4xok8sm2nsh.us-east-1.rds.amazonaws.com:3306/video_transcoding_service",
                "admin",
                "a1d2m3i4n5");
    }
}
