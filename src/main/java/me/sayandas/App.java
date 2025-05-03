package me.sayandas;

import me.sayandas.video.VideoResolution;
import me.sayandas.video.VideoResolutionProbeResult;
import me.sayandas.video.VideoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, SQLException {
//        VideoUtils.generateForResolution(
//                Paths.get("C:\\Users\\sayan\\Videos\\Captures\\F1 23 2025-04-01 22-50-28.mp4"),
//                VideoResolution.RES_240p_SD.getWidth(), VideoResolution.RES_240p_SD.getHeight());

//        Properties dbConnProps = new Properties();
//        dbConnProps.put("user", "admin");
//        dbConnProps.put("password", "a1d2m3i4n5");
//        System.out.println("dbConnProps = " + dbConnProps);
//        System.out.println("JDBC Drivers: " + DriverManager.drivers().collect(Collectors.toList()));
//        Connection dbConn = DriverManager.getConnection(
//                "jdbc:mysql://videoprocessing-db.c4xok8sm2nsh.us-east-1.rds.amazonaws.com:3306", dbConnProps);
//        "C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe"
        System.out.println("Inside main");
            String videoFilePath = "C:\\Users\\sayan\\Videos\\Captures\\F1 23 2025-04-01 22-50-28.mp4";
        VideoResolutionProbeResult res = VideoUtils.getVideoResolution(videoFilePath);
        System.out.println("Input Video Resolution (Result from probe) = " + res);
        List<VideoResolution> targetResolutions = VideoResolution.fetchAllResolutionsBelow(VideoResolution.from(
                        res.width(),
                        res.height()
        ));
        System.out.println("targetResolutions = " + targetResolutions);
        for(VideoResolution targetRes: targetResolutions){
            VideoUtils.generateVideo(Paths.get(videoFilePath), targetRes.getWidth(), targetRes.getHeight());
        }
    }
}
