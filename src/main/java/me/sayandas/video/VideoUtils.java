package me.sayandas.video;

import me.sayandas.function.LambdaEnvVariables;
import me.sayandas.utils.LogUtils;
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
    private static Logger log = LogUtils.getLoggerWithConsoleHandler(VideoUtils.class.getName());

    static {
        try {
            String ffmpegPath = System.getenv(LambdaEnvVariables.FFMPEG_LAYER_PATH),
                    ffprobePath = System.getenv(LambdaEnvVariables.FFPROBE_LAYER_PATH);
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

    public static void generateVideo(Path videoFile, Path outputFile, VideoResolution resolution) throws IOException {
        System.out.println("videoFile = " + videoFile);
        FFmpegBuilder ffmpegBuilder = new FFmpegBuilder()
                .setInput(videoFile.toString())
                .addOutput(outputFile.toString())
                .setFormat("mp4")
                .setVideoResolution(resolution.getWidth(), resolution.getHeight())
                .setVideoFrameRate(30D)
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(ffmpegBuilder).run();
    }

    public static VideoResolutionProbeResult getVideoResolution(String videoFilePath) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(videoFilePath);
        FFmpegStream stream = probeResult.getStreams().get(0);
//        System.out.println("stream.width = " + stream.width);
//        System.out.println("stream.height = " + stream.height);
//        System.out.println("stream.codec_name = " + stream.codec_name);
//        System.out.println("stream.codec_type = " + stream.codec_type);
//        System.out.println("stream.bit_rate = " + stream.bit_rate);
//        System.out.println("stream.duration = " + stream.duration);
//        System.out.println("stream.duration_ts = " + stream.duration_ts);
        return new VideoResolutionProbeResult(stream.width, stream.height);
    }


}
