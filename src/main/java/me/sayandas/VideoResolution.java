package me.sayandas;

import java.util.Arrays;
import java.util.Optional;

public enum VideoResolution {

    RES_4320p_8k(7680, 4320),
    RES_2160p_4K(3840, 2160),
    RES_1440p_2k(2560, 1440),
    RES_1080p_HD(1920, 1080),
    RES_720p_HD(1280, 720),
    RES_480p_SD(854, 480),
    RES_360p_SD(640, 360),
    RES_240p_SD(426, 240);

    private final int width;
    private final int height;

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

    public static Optional<VideoResolution> fromResolutionString(final String resolution){
        if(resolution == null) throw new NullPointerException("Input resolution is null");
        String[] values = resolution.split("x");
        return Arrays.stream(VideoResolution.values())
                .filter(res -> res.getWidth() == Integer.parseInt(values[0]) && res.getHeight() == Integer.parseInt(values[1]))
                .findFirst();
    }

}
