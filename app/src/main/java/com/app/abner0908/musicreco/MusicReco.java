package com.app.abner0908.musicreco;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

public class MusicReco extends AppCompatActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "c27ff178ae9a48458b747ca0ffa83672";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "spotify-login-for-nextmusic://callback";
    private static final int REQUEST_CODE = 1337;
    public static final String LOG_TAG = MusicReco.class.getSimpleName();;
    private boolean isStart = false;
    private boolean isPlay = false;
    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
//        actionBar.setIcon(R.mipmap.ic_launcher);
        setContentView(R.layout.activity_music_reco);
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        final FloatingActionButton fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        fab_play.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (mPlayer == null)
                                                return;

                                            if (!isStart) {
                                                mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                                                fab_play.setImageResource(android.R.drawable.ic_media_pause);
                                                isPlay = !isPlay;
                                                isStart = !isStart;
                                                Log.d(LOG_TAG, "A new song is played");
                                            }

                                            if (isStart) {
                                                if (isPlay) {
                                                    mPlayer.pause();
                                                    fab_play.setImageResource(android.R.drawable.ic_media_play);
                                                    isPlay = !isPlay;
                                                    Log.d(LOG_TAG, "The Music is paused");
                                                } else {
                                                    mPlayer.resume();
                                                    fab_play.setImageResource(android.R.drawable.ic_media_pause);
                                                    isPlay = !isPlay;
                                                    Log.d(LOG_TAG, "The Music is resumed");
                                                }
                                            }
                                        }

                                    }

        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MusicReco.this);
                        mPlayer.addPlayerNotificationCallback(MusicReco.this);
                        //mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(LOG_TAG, "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d(LOG_TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d(LOG_TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(LOG_TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(LOG_TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(LOG_TAG, "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(LOG_TAG, "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d(LOG_TAG, "Playback error received: " + errorType.name());
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}