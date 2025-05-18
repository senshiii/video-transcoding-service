package me.sayandas.db.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.MediaVideo;
import me.sayandas.db.model.Message;
import static org.junit.jupiter.api.Assertions.*;

import me.sayandas.utils.LogUtils;
import me.sayandas.video.VideoResolution;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageVideoRepositoryTest {

    static Connection connection;
    static final String DUMMY_URL = "http://dummy.com/abcd";
    static String testDataMedaId = UUID.randomUUID().toString();
    static final Logger log = LogUtils.getLoggerWithConsoleHandler(MessageVideoRepositoryTest.class.getName());

    @BeforeAll
    public static void beforeAll() throws Exception {
        // Load Test DB Properties
        Properties dbProps = new Properties();
        dbProps.load(MessageVideoRepositoryTest.class.getClassLoader().getResourceAsStream("db.properties"));
        System.out.println("DB properties = " + dbProps);

        // Create Test DB Connection
        connection = DriverManager.getConnection((String) dbProps.get("jdbc.url"),
                (String) dbProps.get("db.username"),
                (String) dbProps.get("db.password"));

        // Insert test data
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO MEDIA_VIDEO(MEDIA_ID, ORIGINAL_VIDEO_LOCATION, TRANSCODED_VERSIONS) VALUES (?,?,?)");
        statement.setString(1, testDataMedaId);
        statement.setString(2, DUMMY_URL);
        HashMap<VideoResolution, String> tv = new HashMap<>();
        tv.put(VideoResolution.RES_1440p, DUMMY_URL);
        tv.put(VideoResolution.RES_1080p, DUMMY_URL);
        tv.put(VideoResolution.RES_720p, DUMMY_URL);
        statement.setString(3, new ObjectMapper().writeValueAsString(tv));

        statement.execute();
        log.fine("Created test data object for media video table with media id = " + testDataMedaId);

    }

    @AfterAll
    public static void afterAll() throws SQLException {
        // Clean up Database
        Statement s = connection.createStatement();
        s.execute("DELETE FROM MEDIA_VIDEO");
    }

    @Test
    @Order(1)
    public void shouldFetchSingleMediaVideoById() throws Exception{
        // arrange
        MediaVideoRepository rep = MediaVideoRepository.getInstance();
        rep.setConnection(connection);
        // act
        MediaVideo mv = rep.fetchById(testDataMedaId);
        log.fine("Media video object fetched from Database: " + mv);

        // assert
        assertEquals(mv.getMediaId(), testDataMedaId);
    }


    @Test
    @Order(2)
    public void shouldInsertMediaVideoObject() throws Exception {
        // arrange
        MediaVideoRepository mediaVideoRepository = MediaVideoRepository.getInstance();
        mediaVideoRepository.setConnection(connection);

        Map<VideoResolution, String> transcodedVersions = new HashMap<>();
        transcodedVersions.put(VideoResolution.RES_720p, DUMMY_URL);
        transcodedVersions.put(VideoResolution.RES_1080p, DUMMY_URL);
        transcodedVersions.put(VideoResolution.RES_1440p, DUMMY_URL);

        String id = UUID.randomUUID().toString();

        MediaVideo m = MediaVideo.builder()
                .mediaId(id)
                .transcodedVersions(transcodedVersions)
                .build();

        // act
        mediaVideoRepository.insert(m);

        // assert
        MediaVideo actual = mediaVideoRepository.fetchById(id);
        log.fine("Media Video object inserted in DB: " + actual);
        assertEquals(m.getMediaId(), actual.getMediaId());

    }

    @Test
    public void shouldUpdateJSON() throws Exception{
        // arrange
        MediaVideoRepository mediaVideoRepository = MediaVideoRepository.getInstance();
        mediaVideoRepository.setConnection(connection);
        String value = "Updated Value";
        // act
        int rowsAffected = mediaVideoRepository.updateTranscodedVersionsJsonById(testDataMedaId, VideoResolution.RES_1080p, value);
        // assert
        MediaVideo m = mediaVideoRepository.fetchById(testDataMedaId);
        assertEquals(1, rowsAffected);
        assertNotNull(m.getTranscodedVersions().get(VideoResolution.RES_1080p));
        assertEquals(m.getTranscodedVersions().get(VideoResolution.RES_1080p), value);
    }

}
