package me.sayandas.db.model;

import lombok.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

@AllArgsConstructor
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class BaseEntity {

    private Date createdAt;
    private Date updatedAt;

    public BaseEntity(){
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}
