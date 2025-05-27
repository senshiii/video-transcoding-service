package me.sayandas.db.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.sayandas.db.model.Message;
import me.sayandas.utils.LogUtils;
import me.sayandas.video.VideoResolution;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

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
        log.finest("DB properties = " + dbProps);

        // Create Test DB Connection
        connection = DriverManager.getConnection((String) dbProps.get("jdbc.url"),
                (String) dbProps.get("db.username"),
                (String) dbProps.get("db.password"));

        // Insert test data
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO TRANSCODE_SERVICE_VIDEO(MEDIA_ID, TRANSCODED_VERSIONS,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?)");
        statement.setString(1, testDataMediaId);
        HashMap<VideoResolution, String> tv = new HashMap<>();
        tv.put(VideoResolution.RES_1440p, DUMMY_URL);
        tv.put(VideoResolution.RES_1080p, DUMMY_URL);
        tv.put(VideoResolution.RES_720p, DUMMY_URL);
        statement.setString(2, new ObjectMapper().writeValueAsString(tv));
        Timestamp t = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("GMT")));
        statement.setTimestamp(3,t);
        statement.setTimestamp(4,t);

        PreparedStatement statement1 = connection.prepareStatement(
                "INSERT INTO TRANSCODE_SERVICE_MESSAGE(MESSAGE_ID,MEDIA_ID,RECEIPT_HANDLE,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?)"
        );
        statement1.setString(1, testDataMessageId);
        statement1.setString(2, testDataMediaId);
        statement1.setString(3, UUID.randomUUID().toString());
//        log.finest("t = " + t);
        statement1.setTimestamp(4, t);
        statement1.setTimestamp(5, t);

        statement.execute();
        log.fine("Created test data object for video table with media id = " + testDataMediaId);
        statement1.execute();
        log.fine("Created test data object for message table with message id = " + testDataMessageId);
    }

    @AfterAll
    public static void afterAll() throws SQLException {
        // Clean up Database
        Statement s = connection.createStatement();
        s.execute("DELETE FROM TRANSCODE_SERVICE_MESSAGE");
        s.execute("DELETE FROM TRANSCODE_SERVICE_VIDEO");
    }
    
    @Order(1)
    @Test
    public void givenId_ShouldFetchSingleRecord() throws Exception {
        // arrange
        MessageRepository rep = MessageRepository.getInstance();
        rep.setConnection(connection);
        // act
        Message m = rep.fetchById(testDataMessageId);
        log.fine("Message object fetched from DB: " + m);
        // assert
        assertEquals(testDataMediaId, m.getMediaId());
        assertEquals(testDataMessageId, m.getMessageId());
    }
    
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
        Message actualMessage = rep.fetchById(messageId);
        log.fine("Message object inserted in DB: " + actualMessage);
        assertEquals(actualMessage.getMessageId(), messageId);
        assertEquals(actualMessage.getMediaId(), testDataMediaId);
        assertNotNull(actualMessage.getCreatedAt());
        assertNotNull(actualMessage.getUpdatedAt());
    }

    @Test
    public void givenUpdatedReceiptHandle_shouldUpdateMessage() throws Exception {
        // arrange
        MessageRepository rep = MessageRepository.getInstance();
        rep.setConnection(connection);
        Message expectedMessage = rep.fetchById(testDataMessageId);
        log.finest("expectedMessage = " + expectedMessage);
        
        // act
        String rh = UUID.randomUUID().toString();
        expectedMessage.setReceiptHandle(rh);
//        log.finest("expectedMessage with new receipt handle = " + expectedMessage);
        Thread.sleep(2000);
        int nRows = rep.updateById(testDataMessageId, expectedMessage);
        Message actualMessage = rep.fetchById(testDataMessageId);
//        log.finest("actualMessage = " + actualMessage);

        // assert
        assertEquals(1, nRows);
        assertNotNull(actualMessage);
        assertEquals(rh, actualMessage.getReceiptHandle());
        assertTrue(actualMessage.getUpdatedAt().after(expectedMessage.getUpdatedAt()));

    }


}
