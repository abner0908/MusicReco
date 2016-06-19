package com.app.abner0908.musicreco;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.TrackSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MusicRecoActivity extends AppCompatActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "c27ff178ae9a48458b747ca0ffa83672";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "spotify-login-for-nextmusic://callback";
    private static final int REQUEST_CODE = 1337;
    public static final String TAG = MusicRecoActivity.class.getSimpleName();

    private AuthenticationResponse response = null;
    private FloatingActionButton fab_play;
    private boolean isStart = false;
    private boolean isPlay = false;
    private Player mPlayer = null;

    private ListView mListView;
    private TracksDbAdapter mDbAdapter;
    private TrackSimpleCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrangeActionBar();
        setContentView(R.layout.activity_music_reco);

        processAuthentication(); //to log into Spotify for authentication and get the access token

        arrangePlayButton(); //setting fab play button

        bindDatabaseWithView(); // binding ListView and SQLite
    }

    private void arrangePlayButton() {
        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        if (fab_play != null) {
            fab_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handlePlay(fab_play);
                }
            });
        }
    }

    private void arrangeActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
    }

    private void bindDatabaseWithView() {
        mListView = (ListView) findViewById(R.id.tracks_list_view);
        mListView.setDivider(null);
        mDbAdapter = new TracksDbAdapter(this);
        mDbAdapter.open();

        Cursor cursor = mDbAdapter.fetchAllTracks();
        //from columns defined in the db
        String[] from = new String[]{
                TracksDbAdapter.COL_TRACK_NAME
        };
        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };

        mCursorAdapter = new TrackSimpleCursorAdapter(
                MusicRecoActivity.this, //context
                R.layout.tracks_row, //the layout of the row
                cursor,
                from,
                to,
                0); // flag - not used

        mListView.setAdapter(mCursorAdapter);
    }


    private void getTracksFromAlbum() {
        if (response == null) {
            Log.d(TAG, "The spotify's web service responce don't exist.");
            return;
        }
        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(response.getAccessToken());
        SpotifyService service = api.getService();
        service.getAlbum("2E2qx9yrlw5VjWquzZ25vy", new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                Log.d("Album success", album.name);
                mDbAdapter.deleteAllTracks();
                Pager<TrackSimple> tracks = album.tracks;
                String artistNames = "";
                for (TrackSimple track : tracks.items) {
                    List<ArtistSimple> artists = track.artists;
                    artistNames = getNamesStrList(artists);
                    mDbAdapter.createTrack(track.name, track.id, artistNames, album.name, album.id);
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllTracks());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }

    private String getNamesStrList(List<ArtistSimple> artists) {
        String artistNames;
        StringBuilder builder = new StringBuilder();
        for (ArtistSimple artist : artists) {
            builder.append(artist.name + ", ");
        }
        artistNames = builder.toString();
        artistNames = artistNames.substring(0, artistNames.length() - 2);
        return artistNames;
    }

    private void handlePlay(FloatingActionButton fab_play) {
        if (mPlayer == null)
            return;
        if (!isStart) {
            mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
            Log.d(TAG, "A new song is played");
        } else {
            if (isPlay) {
                mPlayer.pause();
                Log.d(TAG, "The Music is paused");
            } else {
                mPlayer.resume();
                Log.d(TAG, "The Music is resumed");
            }
        }
    }

    private void processAuthentication() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MusicRecoActivity.this);
                        mPlayer.addPlayerNotificationCallback(MusicRecoActivity.this);
                        getTracksFromAlbum();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });

            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(TAG, "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(TAG, "Playback event received: " + eventType.name());
        if (eventType == EventType.TRACK_START) {
            fab_play.setImageResource(android.R.drawable.ic_media_pause);
            isStart = true;
        } else if (eventType == EventType.PLAY) {
            fab_play.setImageResource(android.R.drawable.ic_media_pause);
            isPlay = true;
        } else if (eventType == EventType.PAUSE) {
            fab_play.setImageResource(android.R.drawable.ic_media_play);
            isPlay = false;
        } else if (eventType == EventType.TRACK_END) {
            fab_play.setImageResource(android.R.drawable.ic_media_play);
            isStart = false;
            isPlay = false;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d(TAG, "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_reco, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_exit:
                exitApp();
                return true;
            case android.R.id.home:
                exitApp();
                return true;
            default:
                return false;
        }
    }

    protected void exitApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MusicRecoActivity.this);
        builder.setTitle("Information");
        builder.setMessage("Are you sure to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mDbAdapter.close();
                MusicRecoActivity.this.finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        exitApp();
        //super.onBackPressed();
    }
}