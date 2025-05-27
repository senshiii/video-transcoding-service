package me.sayandas.db.model;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@ToString(callSuper = true)
public class Message extends BaseEntity {

    @Getter
    @ToString
    public enum MessageState {
        CREATED("CREATED"), PROCESSED("PROCESSED"), FAILED("FAILED");
        private final String value;
        MessageState(String state){
            this.value = state;
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
