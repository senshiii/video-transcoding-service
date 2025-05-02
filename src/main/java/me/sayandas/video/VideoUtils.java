package me.sayandas.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;
import java.nio.file.Path;

public class VideoUtils {

    public static void generateVideo(Path videoFile, int width, int height) throws IOException {
        FFprobe fFprobe = new FFprobe();
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

    public static VideoResolutionProbeResult getVideoResolution(String videoFilePath) throws IOException {
        FFprobe ffprobe = new FFprobe("C:\\ProgramData\\chocolatey\\bin\\ffprobe.exe");
        FFmpegProbeResult probeResult = ffprobe.probe(videoFilePath);
        System.out.println(probeResult.getStreams());
        FFmpegStream stream = probeResult.getStreams().get(0);
        System.out.println("stream.width = " + stream.width);
        System.out.println("stream.height = " + stream.height);
        System.out.println("stream.codec_name = " + stream.codec_name);
        System.out.println("stream.codec_type = " + stream.codec_type);
        System.out.println("stream.bit_rate = " + stream.bit_rate);
        System.out.println("stream.duration = " + stream.duration);
        System.out.println("stream.duration_ts = " + stream.duration_ts);
        return new VideoResolutionProbeResult(stream.width, stream.height);
    }


}
