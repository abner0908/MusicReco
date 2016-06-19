package com.app.abner0908.musicreco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by abner0908 on 16/6/18.
 */
public class TracksDbAdapter {
    //these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_TRACK_NAME = "track_name";
    public static final String COL_TRACK_ID = "track_id";
    public static final String COL_ARTIST = "artist";
    public static final String COL_ALBUM = "album";
    public static final String COL_ALBUM_ID = "album_id";

    //these are the corresponding indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_TRACKNAME = INDEX_ID + 1;
    public static final int INDEX_TRACKID = INDEX_ID + 2;
    public static final int INDEX_ARTIST = INDEX_ID + 3;
    public static final int INDEX_ALBUM = INDEX_ID + 4;
    public static final int INDEX_ALBUMID = INDEX_ID + 5;

    //used for logging
    private static final String TAG = "TracksDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "dba_tracks";
    private static final String TABLE_NAME = "tbl_tracks";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    public TracksDbAdapter(Context ctx) {
        mCtx = ctx;
    }

    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public void createTrack(String trackName,
                            String trackId,
                            String artist,
                            String album,
                            String albumId) {
        ContentValues values = new ContentValues();
        values.put(COL_TRACK_NAME, trackName);
        values.put(COL_TRACK_ID, trackId);
        values.put(COL_ARTIST, artist);
        values.put(COL_ALBUM, album);
        values.put(COL_ALBUM_ID, albumId);
        mDb.insert(TABLE_NAME, null, values);
    }

    //overloaded to take a track
    public long createTrack(myTrack track) {
        ContentValues values = new ContentValues();
        values.put(COL_TRACK_NAME, track.getTrackName());
        values.put(COL_TRACK_ID, track.getTrackId());
        values.put(COL_ARTIST, track.getArtist());
        values.put(COL_ALBUM, track.getAlbum());
        values.put(COL_ALBUM_ID, track.getAlbumId());

        return mDb.insert(TABLE_NAME, null, values);
    }

    public myTrack fetchTrackById(int id) {
        Cursor cursor = mDb.query(TABLE_NAME,
                new String[]{COL_ID,
                        COL_TRACK_NAME,
                        COL_TRACK_ID,
                        COL_ARTIST,
                        COL_ALBUM,
                        COL_ALBUM_ID},
                COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return new myTrack(
                cursor.getInt(INDEX_ID),
                cursor.getString(INDEX_TRACKNAME),
                cursor.getString(INDEX_TRACKID),
                cursor.getString(INDEX_ARTIST),
                cursor.getString(INDEX_ALBUM),
                cursor.getString(INDEX_ALBUMID)
        );
    }

    public Cursor fetchAllTracks() {
        Cursor mCursor = mDb.query(TABLE_NAME,
                new String[]{COL_ID,
                        COL_TRACK_NAME,
                        COL_TRACK_ID,
                        COL_ARTIST,
                        COL_ALBUM,
                        COL_ALBUM_ID},
                null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void updateTrack(myTrack track) {
        ContentValues values = new ContentValues();
        values.put(COL_TRACK_NAME, track.getTrackName());
        values.put(COL_TRACK_ID, track.getTrackId());
        values.put(COL_ARTIST, track.getArtist());
        values.put(COL_ALBUM, track.getAlbum());
        values.put(COL_ALBUM_ID, track.getAlbumId());
        mDb.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(track.getId())});
    }

    public void deleteTrackById(int nId) {
        mDb.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(nId)});
    }

    public void deleteAllTracks() {
        mDb.delete(TABLE_NAME, null, null);
    }

    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_TRACK_NAME + " TEXT, " +
                    COL_TRACK_ID + " TEXT, " +
                    COL_ARTIST + " TEXT, " +
                    COL_ALBUM + " TEXT, " +
                    COL_ALBUM_ID + " TEXT " + ");";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
