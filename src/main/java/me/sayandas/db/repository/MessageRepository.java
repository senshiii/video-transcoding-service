package me.sayandas.db.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Setter;
import me.sayandas.db.model.Message;
import me.sayandas.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class MessageRepository {

    private static MessageRepository messageRepository = null;
    @Setter
    private Connection connection;
    private final Logger log = LogManager.getLogger(this.getClass().getName());
    private final String TABLE_NAME = "TRANSCODE_SERVICE_MESSAGE";

    private MessageRepository(){}

    public static synchronized MessageRepository getInstance(){
        if(Objects.isNull(messageRepository)) messageRepository = new MessageRepository();
        return MessageRepository.messageRepository;
    }

    public Message fetchById(String messageId)throws Exception{
        final String fetchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE MESSAGE_ID = (?)";
        try(PreparedStatement pStat = connection.prepareStatement(fetchQuery)){
            a(fetchQuery);
            pStat.setString(1, messageId);
            ResultSet rs = pStat.executeQuery();
            return this.convertResultSetToObject(rs).get(0);
        }
        catch(Exception e){
            LogUtils.logAndThrowException("Error occurred during fetch for table " + TABLE_NAME, e, log);
        }
        return null;
    }

    public void insertBulk(List<Message> messages) throws Exception {
        final String insertQuery = "INSERT INTO " + TABLE_NAME + " (MESSAGE_ID, MEDIA_ID, RECEIPT_HANDLE, CREATED_AT, UPDATED_AT) VALUES (?,?,?,?,?)";
        try(PreparedStatement pStat = connection.prepareStatement(insertQuery)){
            a(insertQuery);
            connection.setAutoCommit(false);
            Timestamp t;
            for(Message m : messages){
                t = getCurrentTimestamp();
                pStat.setString(1, m.getMessageId());
                pStat.setString(2, m.getMediaId());
                pStat.setString(3, m.getReceiptHandle());
                pStat.setTimestamp(4, t);
                pStat.setTimestamp(5, t);
                pStat.addBatch();
            }
            pStat.executeBatch();
            connection.commit();
        }catch(SQLException e){
            LogUtils.logAndThrowException("Error occurred during bulk insert for table " + TABLE_NAME, e, log);
        }finally{
            connection.setAutoCommit(true);
        }
    }

    public int updateById(String messageId, Message data) throws Exception{
        boolean updateReceiptHandle = !(data.getReceiptHandle() == null || data.getReceiptHandle().isBlank());
        boolean updateState = !(data.getState() == null);
        if(!updateReceiptHandle && !updateState) return 0;
        List<String> updateList = new ArrayList<>();
        if(updateReceiptHandle) updateList.add("RECEIPT_HANDLE = (?)");
        if(updateState) updateList.add("STATE = (?)");
        updateList.add("UPDATED_AT = (?)");
        final String updateQuery = "UPDATE " + TABLE_NAME + " SET " + String.join(",", updateList) + " WHERE MESSAGE_ID = (?)";
        try(PreparedStatement pStat = connection.prepareStatement(updateQuery)){
            a(updateQuery);
            int paramIndex = 1;
            if(updateReceiptHandle) {
                pStat.setString(paramIndex++, data.getReceiptHandle());
            }
            if(updateState){
                pStat.setString(paramIndex++, data.getState().getValue());
            }
            Timestamp t = getCurrentTimestamp();
            pStat.setTimestamp(paramIndex++, t);
            pStat.setString(paramIndex, messageId);
            return pStat.executeUpdate();
        }catch(SQLException e){
            LogUtils.logAndThrowException("Error occurred during update for table " + TABLE_NAME, e, log);
        }
        return 0;
    }

    private List<Message> convertResultSetToObject(ResultSet rs) throws SQLException, JsonProcessingException {
        List<Message> messages = new ArrayList<>();
        while(rs.next()){
            String messageId = rs.getString("MESSAGE_ID");
            String mediaId = rs.getString("MEDIA_ID");
            String receiptHandle = rs.getString("RECEIPT_HANDLE");
            Message.MessageState state = Message.MessageState.from(rs.getString("STATE"));
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");
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

    private void a(String s){
        log.debug("Firing query {}", s);
    }

    private Timestamp getCurrentTimestamp(){
        return Timestamp.valueOf(LocalDateTime.now(ZoneId.of("GMT")));
    }

}
