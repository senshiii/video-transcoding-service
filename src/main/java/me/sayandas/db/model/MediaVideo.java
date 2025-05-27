package me.sayandas.db.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.sayandas.video.VideoResolution;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@ToString(callSuper = true)
public class MediaVideo extends BaseEntity {

    private String mediaId;
    private Map<VideoResolution, String> transcodedVersions;
}
