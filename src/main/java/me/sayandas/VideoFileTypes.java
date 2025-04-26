package me.sayandas;

public enum VideoFileTypes {

    MP4("mp4"),
    MOV("mov"),
    AVI("avi"),
    WMV("wmv"),
    AVCHD("AVCHD"),
    WebM("WebM"),
    MLV("mkv");

    private final String videoFileType;

    VideoFileTypes(String type){
        this.videoFileType = type;
    }

    public String getVideoFileType(){
        return this.videoFileType;
    }

}
