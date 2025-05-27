package me.sayandas.db.model;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@AllArgsConstructor
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class BaseEntity {

    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BaseEntity(){
        this.createdAt = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("GMT")));
        this.updatedAt = Timestamp.valueOf(LocalDateTime.now(ZoneId.of("GMT")));
    }
}
