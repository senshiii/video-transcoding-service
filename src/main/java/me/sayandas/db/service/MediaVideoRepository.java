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

    private Connection conn;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger log = LogUtils.getLoggerWithConsoleHandler(this.getClass().getName());
    private static MediaVideoRepository mediaVideoRepository = null;

    private MediaVideoRepository(){}

    private MediaVideoRepository(Connection connection){
        this.conn = connection;
    }

    public void setConnection(Connection connection){
        this.conn = connection;
    }

    public static synchronized MediaVideoRepository getInstance(){
        if(Objects.isNull(MediaVideoRepository.mediaVideoRepository))
            MediaVideoRepository.mediaVideoRepository = new MediaVideoRepository();
        return MediaVideoRepository.mediaVideoRepository;
    }

    public void insert(MediaVideo mediaVideo) throws Exception{
        final String insertQuery = "INSERT INTO MEDIA_VIDEO (MEDIA_ID, ORIGINAL_VIDEO_LOCATION, TRANSCODED_VERSIONS) VALUES (?, ?, ?)";
        try(PreparedStatement ps = conn.prepareStatement(insertQuery)){
            ps.setString(1, mediaVideo.getMediaId());
            ps.setString(2, mediaVideo.getUrl());
            ps.setObject(3, objectMapper.writeValueAsString(mediaVideo.getTranscodedVersions()));
            ps.execute();
        }catch(SQLException e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred during insert for table MEDIA_VIDEO", e));
            throw new RuntimeException("Exception occurred when inserting media video", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        finally{
            conn.setAutoCommit(true);
        }
    }

    public MediaVideo fetchById(String id) throws Exception{
        final String fetchQuery = "SELECT * FROM MEDIA_VIDEO WHERE MEDIA_ID = (?)";
        try(PreparedStatement ps = conn.prepareStatement(fetchQuery)){
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return this.convertResultSetToObject(rs).get(0);
        }
        catch(Exception e){
            log.severe(LogUtils.getFullErrorMessage("Error occurred during fetch for table MEDIA_VIDEO", e));
            throw new RuntimeException("Exception occurred when fetching Media Video", e);
        }
    }

    public int updateTranscodedVersionsJsonById(String id, VideoResolution key, String value){
        String sql = "UPDATE MEDIA_VIDEO SET TRANSCODED_VERSIONS = JSON_SET(TRANSCODED_VERSIONS, (?), (?)) WHERE MEDIA_ID = (?)";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, "$." + key.toString());
            ps.setString(2, value);
            ps.setString(3, id);
            return ps.executeUpdate();
        }catch(SQLException e){
            log.severe(LogUtils.getFullErrorMessage("Error occured when updaing JSON key for table MEDIA_VIDEO", e));
        }
        return 0;
    }


    private List<MediaVideo> convertResultSetToObject(ResultSet rs) throws SQLException, JsonProcessingException {
        List<MediaVideo> mediaVideos = new ArrayList<>();
        while(rs.next()){
            String mediaId = rs.getString("MEDIA_ID");
            String originalVideoLocation = rs.getString("ORIGINAL_VIDEO_LOCATION");
            String transcodedVersions = rs.getString("TRANSCODED_VERSIONS");
            Date createdAt = rs.getDate("CREATED_AT");
            Date updatedAt = rs.getDate("UPDATED_AT");

            TypeReference<Map<VideoResolution, String>> typeReference = new TypeReference<Map<VideoResolution, String>>() {};
            MediaVideo mv = MediaVideo.builder()
                    .mediaId(mediaId)
                    .url(originalVideoLocation)
                    .transcodedVersions(objectMapper.readValue(transcodedVersions, typeReference))
                    .build();
            mv.setCreatedAt(createdAt);
            mv.setUpdatedAt(updatedAt);
            mediaVideos.add(mv);
        }
        return mediaVideos;
    }

}
