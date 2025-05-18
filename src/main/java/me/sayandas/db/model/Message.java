package me.sayandas.db.model;

import lombok.*;

import java.util.Date;

import static software.amazon.awssdk.http.HttpStatusCode.CREATED;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Message extends BaseEntity {

    @Getter
    @ToString
    public enum MessageState {
        CREATED("CREATED"), PROCESSED("PROCESSED"), FAILED("FAILED");
        private final String state;
        MessageState(String state){
            this.state = state;
        }
        public static MessageState from(String value){
            if(value == null || value.isEmpty()) throw new IllegalArgumentException("Cannot resolve message state for empty value");
            if(value.equalsIgnoreCase("CREATED")) return CREATED;
            else if(value.equalsIgnoreCase("PROCESSED")) return PROCESSED;
            else if(value.equalsIgnoreCase("FAILED")) return FAILED;
            else throw new IllegalArgumentException("Cannot resolve " + value + " to  a valid message state");
        }
    }

    private String messageId;
    private String mediaId;
    private String receiptHandle;
    private MessageState state;

}
