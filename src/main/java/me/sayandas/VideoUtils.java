package me.sayandas;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.nio.file.Path;

public class VideoUtils {

    public static void generateForResolution(Path videoFile, int width, int height) throws IOException {
        FFprobe fFprobe = new FFprobe("C:\\ProgramData\\chocolatey\\bin\\ffprobe.exe");
        System.out.println("Successfully loaded ffprobe");
        FFmpeg fFmpeg = new FFmpeg("C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe");
        System.out.println("Successfully loaded ffmpeg");

        System.out.println("videoFile = " + videoFile);

        String sourceFileName = videoFile.getFileName().toString();
        String fileNameWithoutExt = sourceFileName.substring(0, sourceFileName.lastIndexOf("."));
        String outputFileName = fileNameWithoutExt + "_output_" + width + "_" + height;
        outputFileName = outputFileName + sourceFileName.substring(sourceFileName.lastIndexOf("."));
        Path outputVideoFile = videoFile.getParent().resolve(outputFileName);
        System.out.println("outputVideoFile = " + outputVideoFile);

        FFmpegBuilder ffmpegBuilder = new FFmpegBuilder()
                .setInput(videoFile.toString())
                .addOutput(outputVideoFile.toString())
                .setFormat("mp4")
                .setVideoResolution(width, height)
                .setVideoFrameRate(30D)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
        executor.createJob(ffmpegBuilder).run();
    }

}
