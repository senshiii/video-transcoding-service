package me.sayandas.video;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum VideoResolution {

    RES_2160p(3840, 2160),
    RES_1440p(2560, 1440),
    RES_1080p(1920, 1080),
    RES_720p(1280, 720),
    RES_480p(854, 480),
    RES_360p(640, 360),
    RES_240p(426, 240),
    RES_144p(256, 144);

    private final int width;
    private final int height;
    private static Logger logger = Logger.getLogger(VideoResolution.class.getName());

    VideoResolution(int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

    public static VideoResolution from(int width, int height){
        return Arrays.stream(VideoResolution.values())
                .filter(res -> res.getWidth() == width && res.getHeight() == height)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot resolve resolution"));
    }

    public static List<VideoResolution> fetchAllResolutionsBelow(VideoResolution vidRes){
        logger.log(Level.FINER, "Inside method fetchAllResolutionBelow. Input Resolution - " + vidRes);
        List<VideoResolution> allResolutions = List.of(RES_144p, RES_240p, RES_360p, RES_480p, RES_720p, RES_1080p, RES_1440p, RES_2160p);
        int index = allResolutions.indexOf(vidRes);
        logger.log(Level.FINER, "Index of video resolution - " + index);
        if(index == -1) throw new IllegalArgumentException("Resolution not supported");
        if(index == 0) return List.of();
        return allResolutions.subList(0, index);
    }

}
