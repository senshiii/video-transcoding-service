package me.sayandas.db.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.Message;
import me.sayandas.utils.LogUtils;
import me.sayandas.video.VideoResolution;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageRepositoryTest {

    static Connection connection;
    static final String DUMMY_URL = "http://dummy.com/abcd";
    static String testDataMessageId = UUID.randomUUID().toString(), testDataMediaId = UUID.randomUUID().toString();
    static final Logger log = LogUtils.getLoggerWithConsoleHandler(MessageRepositoryTest.class.getName());

    @BeforeAll
    public static void beforeAll() throws Exception {
        // Load Test DB Properties
        Properties dbProps = new Properties();
        dbProps.load(MessageRepositoryTest.class.getClassLoader().getResourceAsStream("db.properties"));
        System.out.println("DB properties = " + dbProps);

        // Create Test DB Connection
        connection = DriverManager.getConnection((String) dbProps.get("jdbc.url"),
                (String) dbProps.get("db.username"),
                (String) dbProps.get("db.password"));

        // Insert test data
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO MEDIA_VIDEO(MEDIA_ID, ORIGINAL_VIDEO_LOCATION, TRANSCODED_VERSIONS) VALUES (?,?,?)");
        statement.setString(1, testDataMediaId);
        statement.setString(2, DUMMY_URL);
        HashMap<VideoResolution, String> tv = new HashMap<>();
        tv.put(VideoResolution.RES_1440p, DUMMY_URL);
        tv.put(VideoResolution.RES_1080p, DUMMY_URL);
        tv.put(VideoResolution.RES_720p, DUMMY_URL);
        statement.setString(3, new ObjectMapper().writeValueAsString(tv));

        PreparedStatement statement1 = connection.prepareStatement(
                "INSERT INTO MESSAGE(MESSAGE_ID, MEDIA_ID, RECEIPT_HANDLE, RECEIVED_AT, RECEIVE_COUNT) VALUES (?,?,?,?,?)"
        );
        statement1.setString(1, testDataMessageId);
        statement1.setString(2, testDataMediaId);
        statement1.setString(3, UUID.randomUUID().toString());
        statement1.setString(4, Date.valueOf("2025-02-01").toString());
        statement1.setInt(5, 1);
        
        statement.execute();
        log.fine("Created test data object for media video table with media id = " + testDataMediaId);
        statement1.execute();
        log.fine("Created test data object for message table with message id = " + testDataMessageId);
    }

    @AfterAll
    public static void afterAll() throws SQLException {
        // Clean up Database
        Statement s = connection.createStatement();
        s.execute("DELETE FROM MESSAGE");
        s.execute("DELETE FROM MEDIA_VIDEO");
    }
    
    @Order(1)
    @Test
    public void givenId_ShouldFetchSingleRecord(){
        // arrange
        MessageRepository rep = MessageRepository.getInstance();
        rep.setConnection(connection);
        // act
        Message m = rep.fetchById(testDataMessageId, testDataMediaId);
        log.fine("Message object fetched from DB: " + m);
        // assert
        assertEquals(testDataMediaId, m.getMediaId());
        assertEquals(testDataMessageId, m.getMessageId());
    }
    
    @Order(2)
    @Test
    public void shouldInsertMessage() throws Exception {
        // arrange
        MessageRepository rep = MessageRepository.getInstance();
        rep.setConnection(connection);
        // act
        String messageId = UUID.randomUUID().toString();
        Message m = Message.builder()
                .messageId(messageId)
                .mediaId(testDataMediaId)
                .build();
        rep.insertBulk(List.of(m));
        // assert
        Message actualMessage = rep.fetchById(messageId, testDataMediaId);
        log.fine("Message object inserted in DB: " + actualMessage);
        assertEquals(actualMessage.getMessageId(), messageId);
        assertEquals(actualMessage.getMediaId(), testDataMediaId);
        assertNotNull(actualMessage.getCreatedAt());
        assertNotNull(actualMessage.getUpdatedAt());
    }

}
