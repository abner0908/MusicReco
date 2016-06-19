package com.app.abner0908.musicreco;

/**
 * Created by abner0908 on 16/6/18.
 */
public class myTrack {

    private int mId;
    private String mTrackName;
    private String mTrackId;
    private String mArtist;
    private String mAlbum;
    private String mAlbumId;

    public myTrack(int id, String trackName, String trackId, String artist, String album, String albumId) {
        mId = id;
        mTrackName = trackName;
        mTrackId = trackId;
        mArtist = artist;
        mAlbum = album;
        mAlbumId = albumId;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public String getTrackId() {
        return mTrackId;
    }

    public void setTrackId(String trackId) {
        mTrackId = trackId;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public String getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(String albumId) {
        mAlbumId = albumId;
    }
}
