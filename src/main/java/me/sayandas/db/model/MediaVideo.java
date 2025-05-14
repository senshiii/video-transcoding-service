package me.sayandas.db.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.sayandas.video.VideoResolution;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class MediaVideo extends BaseEntity {

    private String mediaId;
    private String url;
    private Map<VideoResolution, String> transcodedVersions;
}
