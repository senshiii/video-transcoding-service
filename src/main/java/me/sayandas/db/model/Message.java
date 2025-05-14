package me.sayandas.db.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Message extends BaseEntity {

    private String messageId;
    private String mediaId;
    private String receiptHandle;
    private Date receivedAt;
    private Integer receiveCount;

}
