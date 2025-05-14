use video_transcoder;
create table if not exists MEDIA_VIDEO (
	MEDIA_ID varchar(100) NOT NULL PRIMARY KEY,
    ORIGINAL_VIDEO_LOCATION TEXT NOT NULL,
    TRANSCODED_VERSIONS JSON,
    CREATED_AT timestamp default now() not null,
    UPDATED_AT timestamp default now() not null
);
create table if not exists MESSAGE(
	MESSAGE_ID varchar(100) NOT NULL,
    MEDIA_ID varchar(100) NOT NULL,
    RECEIPT_HANDLE varchar (1024),
    CREATED_AT timestamp default now() not null,
    UPDATED_AT timestamp default NOW() not null,
	RECEIVED_AT timestamp not null,
    RECEIVE_COUNT integer default 0,
    CONSTRAINT PRIMARY KEY (MESSAGE_ID, MEDIA_ID),
    CONSTRAINT fk_media_id FOREIGN KEY (MEDIA_ID) REFERENCES MEDIA_VIDEO(MEDIA_ID) ON DELETE CASCADE
);



