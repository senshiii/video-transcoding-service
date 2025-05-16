package me.sayandas;


import static org.junit.jupiter.api.Assertions.*;

import me.sayandas.video.VideoResolution;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

public class VideoResolutionTest {



    @Test
    // TODO: Rewrite test
    public void givenExactResolution_ShouldReturnAllLowerResolutions(){
        // arrange
        List<VideoResolution> expectedBelow144 = List.of();
        List<VideoResolution> expectedBelow240 = List.of(VideoResolution.RES_144p);
        List<VideoResolution> expectedBelow360 = new ArrayList<>(expectedBelow240); expectedBelow360.add(VideoResolution.RES_240p);
        List<VideoResolution> expectedBelow480 = new ArrayList<>(expectedBelow360); expectedBelow480.add(VideoResolution.RES_360p);
        List<VideoResolution> expectedBelow720 = new ArrayList<>(expectedBelow480); expectedBelow720.add(VideoResolution.RES_480p);
        List<VideoResolution> expectedBelow1080 = new ArrayList<>(expectedBelow720); expectedBelow1080.add(VideoResolution.RES_720p);
        List<VideoResolution> expectedBelow1440 = new ArrayList<>(expectedBelow1080); expectedBelow1440.add(VideoResolution.RES_1080p);
        List<VideoResolution> expectedBelow2160 = new ArrayList<>(expectedBelow1440); expectedBelow2160.add(VideoResolution.RES_1440p);

        // act
        List<VideoResolution> actualBelow144 = VideoResolution.getTargetResolutions(VideoResolution.RES_144p);
        List<VideoResolution> actualBelow240 = VideoResolution.getTargetResolutions(VideoResolution.RES_240p);
        List<VideoResolution> actualBelow360 = VideoResolution.getTargetResolutions(VideoResolution.RES_360p);
        List<VideoResolution> actualBelow480 = VideoResolution.getTargetResolutions(VideoResolution.RES_480p);
        List<VideoResolution> actualBelow720 = VideoResolution.getTargetResolutions(VideoResolution.RES_720p);
        List<VideoResolution> actualBelow1080 = VideoResolution.getTargetResolutions(VideoResolution.RES_1080p);
        List<VideoResolution> actualBelow1440 = VideoResolution.getTargetResolutions(VideoResolution.RES_1440p);
        List<VideoResolution> actualBelow2160 = VideoResolution.getTargetResolutions(VideoResolution.RES_2160p);

        // assert
        assertEquals(expectedBelow144, actualBelow144);
        assertEquals(expectedBelow240, actualBelow240);
        assertEquals(expectedBelow360, actualBelow360);
        assertEquals(expectedBelow480, actualBelow480);
        assertEquals(expectedBelow720, actualBelow720);
        assertEquals(expectedBelow1080, actualBelow1080);
        assertEquals(expectedBelow1440, actualBelow1440);
        assertEquals(expectedBelow2160, actualBelow2160);
    }

    @Test
    public void givenUnknownResolution_shouldReturnAllLowerStandardResolutions(){
        List<VideoResolution> res = VideoResolution.getTargetResolutions(1200, 1000);

    }

}
