package me.sayandas.db.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import me.sayandas.db.model.MediaVideo;
import me.sayandas.utils.LogUtils;
import me.sayandas.video.VideoResolution;

import java.sql.*;
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
        final String insertQuery = "INSERT INTO "+TABLE_NAME+" (MEDIA_ID, TRANSCODED_VERSIONS) VALUES (?, ?, ?)";
        try(PreparedStatement ps = connection.prepareStatement(insertQuery)){
            ps.setString(1, mediaVideo.getMediaId());
            ps.setObject(3, objectMapper.writeValueAsString(mediaVideo.getTranscodedVersions()));
            ps.execute();
        }catch(SQLException e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred during insert for table " + TABLE_NAME, e));
            throw new RuntimeException("Error occurred during insert for table " + TABLE_NAME, e);
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
            log.severe(LogUtils.getFullErrorMessage("Error occurred during fetch for table " + TABLE_NAME, e));
            throw new RuntimeException("Exception occurred when fetching Media Video", e);
        }
    }

    public int updateTranscodedVersionsJsonById(String id, VideoResolution key, String value)throws SQLException{
        String sql = "UPDATE " + TABLE_NAME + " SET TRANSCODED_VERSIONS = JSON_SET(TRANSCODED_VERSIONS, (?), (?)) WHERE MEDIA_ID = (?)";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, "$." + key.toString());
            ps.setString(2, value);
            ps.setString(3, id);
            return ps.executeUpdate();
        }catch(SQLException e){
            log.severe(LogUtils.getFullErrorMessage("Error occured when updaing JSON key for table " + TABLE_NAME, e));
            throw e;
        }
    }

    private List<MediaVideo> convertResultSetToObject(ResultSet rs) throws SQLException, JsonProcessingException {
        List<MediaVideo> mediaVideos = new ArrayList<>();
        while(rs.next()){
            String mediaId = rs.getString("MEDIA_ID");
            String transcodedVersions = rs.getString("TRANSCODED_VERSIONS");
            Date createdAt = rs.getDate("CREATED_AT");
            Date updatedAt = rs.getDate("UPDATED_AT");

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

}
