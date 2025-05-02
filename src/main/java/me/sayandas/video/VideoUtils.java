package me.sayandas.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class VideoUtils {

    private static Logger log = Logger.getLogger(VideoUtils.class.getName());

    public static void generateVideo(Path videoFile, int width, int height) throws IOException {
        FFprobe fFprobe = new FFprobe();
        System.out.println("Successfully loaded ffprobe");
        String ffmpegPath = System.getenv("ffmpeg_layer_path");
        log.finer("Env variable ffmpeg-layer-path: " + ffmpegPath);
        FFmpeg fFmpeg = new FFmpeg(ffmpegPath);
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
        String ffprobePath = System.getenv("ffprobe_layer_path");
        log.finer("Env variable ffprobe-layer-path: " + ffprobePath);
        FFprobe ffprobe = new FFprobe(ffprobePath);
        log.fine("Successfully loaded ffprobe");
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
