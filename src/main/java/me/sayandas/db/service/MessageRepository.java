package me.sayandas.db.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import me.sayandas.db.model.Message;
import me.sayandas.utils.LogUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@ToString
@EqualsAndHashCode
public class MessageRepository {

    private static MessageRepository messageRepository = null;
    @Setter
    private Connection connection;
    private final Logger log = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());

    private MessageRepository(){}

    public static synchronized MessageRepository getInstance(){
        if(Objects.isNull(messageRepository)) messageRepository = new MessageRepository();
        return MessageRepository.messageRepository;
    }

    public Message fetchById(String messageId){
        final String fetchQuery = "SELECT * FROM MESSAGE WHERE MESSAGE_ID = (?)";
        try(PreparedStatement pStat = connection.prepareStatement(fetchQuery)){
            pStat.setString(1, messageId);
            ResultSet rs = pStat.executeQuery();
            return this.convertResultSetToObject(rs).get(0);
        }
        catch(Exception e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred during fetch for table MESSAGE", e));
            throw new RuntimeException("Exception occurred when fetching Media Video", e);
        }
    }

    public void insertBulk(List<Message> messages) throws Exception {
        final String insertQuery = "INSERT INTO MESSAGE (MESSAGE_ID, MEDIA_ID, RECEIPT_HANDLE, STATE) VALUES (?, ?)";
        try(PreparedStatement pStat = connection.prepareStatement(insertQuery)){
            connection.setAutoCommit(false);
            for(Message m : messages){
                pStat.setString(1, m.getMessageId());
                pStat.setString(2, m.getMediaId());
                pStat.setString(3, m.getReceiptHandle());
                pStat.setString(4, m.getState().toString());
                pStat.addBatch();
            }
            pStat.executeBatch();
            connection.commit();
        }catch(SQLException e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred during bulk insert for table MESSAGE", e));
            throw new RuntimeException("Error occurred when inserting messages in bulk", e);
        }finally{
            connection.setAutoCommit(true);
        }
    }

    private List<Message> convertResultSetToObject(ResultSet rs) throws SQLException, JsonProcessingException {
        List<Message> messages = new ArrayList<>();
        while(rs.next()){
            String messageId = rs.getString("MESSAGE_ID");
            String mediaId = rs.getString("MEDIA_ID");
            String receiptHandle = rs.getString("RECEIPT_HANDLE");
            Message.MessageState state = Message.MessageState.from(rs.getString("STATE"));
            Date createdAt = rs.getDate("CREATED_AT");
            Date updatedAt = rs.getDate("UPDATED_AT");

            Message m = Message.builder()
                    .messageId(messageId)
                    .mediaId(mediaId)
                    .receiptHandle(receiptHandle)
                    .state(state)
                    .build();
            m.setCreatedAt(createdAt);
            m.setUpdatedAt(updatedAt);
            messages.add(m);
        }
        return messages;
    }

}
