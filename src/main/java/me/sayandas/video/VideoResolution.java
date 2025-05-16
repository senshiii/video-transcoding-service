package me.sayandas.video;

import lombok.Getter;
import me.sayandas.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    @Getter
    private final int width;
    @Getter
    private final int height;
    private static final Logger log = LogUtils.getLoggerWithConsoleHandler(VideoResolution.class.getName());

    VideoResolution(int width, int height){
        this.width = width;
        this.height = height;
    }

    public static VideoResolution from(int width, int height){
        return Arrays.stream(VideoResolution.values())
                .filter(res -> res.getWidth() == width && res.getHeight() == height)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot resolve resolution from width = " + width + " and height = " + height));
    }

    public static List<VideoResolution> getTargetResolutions(VideoResolution res){
        return getTargetResolutions(res.getWidth(), res.getHeight());
    }

    public static List<VideoResolution> getTargetResolutions(int width, int height){
        VideoResolution vidRes = null;
        boolean isStdRes = true;
        List<VideoResolution> allResolutions = Arrays.asList(VideoResolution.values());
        List<VideoResolution> targetResolutions = new ArrayList<>();
        allResolutions.sort(Comparator.comparingInt(VideoResolution::getHeight));
        int index;
        // try to get standard resolution object
        try{
            vidRes = VideoResolution.from(width, height);
        }catch(IllegalArgumentException ex){
            isStdRes = false;
        }
        if(isStdRes) {
            index = allResolutions.indexOf(vidRes);
            targetResolutions.add(vidRes);
        }else{
            // find closest standard resolution and lower resolutions
            index = allResolutions.size() - 1;
            while(allResolutions.get(index).getHeight() > height){
                index--;
            }
            index++;
        }
        targetResolutions.addAll(allResolutions.subList(0, index));
        return targetResolutions;
    }

}
