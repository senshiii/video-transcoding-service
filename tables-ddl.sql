drop database if exists video_transcoding_service;
create database if not exists video_transcoding_service;
use video_transcoding_service;
drop table if exists TRANSCODE_SERVICE_VIDEO;
create table if not exists TRANSCODE_SERVICE_VIDEO (
	MEDIA_ID varchar(100) NOT NULL PRIMARY KEY,
    TRANSCODED_VERSIONS JSON,
    CREATED_AT timestamp default now() not null,
    UPDATED_AT timestamp default now() not null
);
drop table if exists TRANSCODE_SERVICE_MESSAGE;
create table if not exists TRANSCODE_SERVICE_MESSAGE(
	MESSAGE_ID varchar(100) NOT NULL,
    MEDIA_ID varchar(100) NOT NULL,
    RECEIPT_HANDLE varchar (1024),
    STATE ENUM('CREATED', 'PROCESSED', 'FAILED') default 'CREATED',
    CREATED_AT timestamp default now() not null,
    UPDATED_AT timestamp default NOW() not null,
	CONSTRAINT PRIMARY KEY (MESSAGE_ID),
    CONSTRAINT fk_media_id FOREIGN KEY (MEDIA_ID) REFERENCES TRANSCODE_SERVICE_VIDEO(MEDIA_ID) ON DELETE CASCADE
);