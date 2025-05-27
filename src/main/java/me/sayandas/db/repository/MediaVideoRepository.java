package me.sayandas.db.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import me.sayandas.db.model.MediaVideo;
import me.sayandas.utils.LogUtils;
import me.sayandas.video.VideoResolution;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@ToString
@EqualsAndHashCode
public class MediaVideoRepository {

    @Setter
    private Connection connection;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());
    private static MediaVideoRepository mediaVideoRepository = null;
    private final String TABLE_NAME = "TRANSCODE_SERVICE_VIDEO";

    private MediaVideoRepository(){}

    public static synchronized MediaVideoRepository getInstance(){
        if(Objects.isNull(MediaVideoRepository.mediaVideoRepository))
            MediaVideoRepository.mediaVideoRepository = new MediaVideoRepository();
        return MediaVideoRepository.mediaVideoRepository;
    }

    public void insert(MediaVideo mediaVideo) throws Exception{
        final String insertQuery = "INSERT INTO "+TABLE_NAME+" (MEDIA_ID, TRANSCODED_VERSIONS,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?)";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setString(1, mediaVideo.getMediaId());
            ps.setObject(2, objectMapper.writeValueAsString(mediaVideo.getTranscodedVersions()));
            Timestamp t = getCurrentTimestamp();
            ps.setTimestamp(3, t);
            ps.setTimestamp(4, t);
            ps.execute();
        }catch(SQLException e){
            LogUtils.logAndThrowException("Error occurred during bulk insert for table " + TABLE_NAME, e, log);
        }finally{
            connection.setAutoCommit(true);
        }
    }

    public MediaVideo fetchById(String id) throws Exception{
        final String fetchQuery = "SELECT * FROM " + TABLE_NAME + " WHERE MEDIA_ID = (?)";
        try(PreparedStatement ps = connection.prepareStatement(fetchQuery)){
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return this.convertResultSetToObject(rs).get(0);
        }
        catch(Exception e){
            LogUtils.logAndThrowException("Error occurred during bulk insert for table " + TABLE_NAME, e, log);
        }
        return null;
    }

    public int updateTranscodedVersionsJsonById(String id, VideoResolution key, String value) throws Exception {
        String sql = "UPDATE " + TABLE_NAME + " SET TRANSCODED_VERSIONS = JSON_SET(TRANSCODED_VERSIONS, (?), (?)), UPDATED_AT = (?) WHERE MEDIA_ID = (?)";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, "$." + key.toString());
            ps.setString(2, value);
            ps.setTimestamp(3, getCurrentTimestamp());
            ps.setString(4, id);
            return ps.executeUpdate();
        }catch(SQLException e){
            LogUtils.logAndThrowException("Error occurred during bulk insert for table " + TABLE_NAME, e, log);
        }
        return 0;
    }

    private List<MediaVideo> convertResultSetToObject(ResultSet rs) throws SQLException, JsonProcessingException {
        List<MediaVideo> mediaVideos = new ArrayList<>();
        while(rs.next()){
            String mediaId = rs.getString("MEDIA_ID");
            String transcodedVersions = rs.getString("TRANSCODED_VERSIONS");
            Timestamp createdAt = rs.getTimestamp("CREATED_AT");
            Timestamp updatedAt = rs.getTimestamp("UPDATED_AT");

            TypeReference<Map<VideoResolution, String>> typeReference = new TypeReference<Map<VideoResolution, String>>() {};
            MediaVideo mv = MediaVideo.builder()
                    .mediaId(mediaId)
                    .transcodedVersions(objectMapper.readValue(transcodedVersions, typeReference))
                    .build();
            mv.setCreatedAt(createdAt);
            mv.setUpdatedAt(updatedAt);
            mediaVideos.add(mv);
        }
        return mediaVideos;
    }

    private Timestamp getCurrentTimestamp(){
        return Timestamp.valueOf(LocalDateTime.now(ZoneId.of("GMT")));
    }

}
