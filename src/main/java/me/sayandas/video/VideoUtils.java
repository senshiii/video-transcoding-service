package me.sayandas.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoUtils {

    private static FFprobe ffprobe = null;
    private static FFmpeg ffmpeg = null;
    private static Logger log = Logger.getLogger(VideoUtils.class.getName());


    static {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        log.addHandler(ch);
        log.setLevel(Level.ALL);
        try {
            String ffmpegPath = System.getenv("ffmpeg_layer_path"), ffprobePath = System.getenv("ffprobe_layer_path");
            System.out.println("ffmpegPath = " + ffmpegPath);
            System.out.println("ffprobePath = " + ffprobePath);
            log.finest("Ffmpeg location (from env): " + ffmpegPath);
            log.finest("FFprobe location (from env): " + ffprobePath);
            ffmpeg = new FFmpeg(ffmpegPath);
            log.fine("Successfully loaded ffmpeg");
            ffprobe = new FFprobe(ffprobePath);
            log.fine("Successfully loaded ffprobe");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateVideo(Path videoFile, int width, int height) throws IOException {
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

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(ffmpegBuilder).run();
    }

    public static VideoResolutionProbeResult getVideoResolution(String videoFilePath) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(videoFilePath);
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
