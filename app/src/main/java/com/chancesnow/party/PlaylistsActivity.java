package com.chancesnow.party;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;

import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

public class PlaylistsActivity extends AppCompatActivity
        implements PlaylistsFragment.OnPlaylistListListener {

    private SpotifyClient mSpotify;

    private View mPlaylistsActivity;

    private Toolbar mToolbar;
    private ActionBar mActionBar;
    private Menu mToolbarMenu;
    private MenuItem mRefreshMenuItem;
    private MenuItem mNowPlayingMenuItem;

    private View mLoadingView;

    private PlaylistsFragment mPlaylistsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlists);

        mSpotify = ((PartyApplication) getApplication()).getSpotifyClient();

        mPlaylistsActivity = findViewById(R.id.playlists);

        mToolbar = (Toolbar) findViewById(R.id.playlists_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.select_playlist);

        mLoadingView = findViewById(R.id.playlists_loading);

        getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();
        mPlaylistsFragment.loadPlaylists();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        mToolbarMenu = menu;

        mRefreshMenuItem = menu.findItem(R.id.action_refresh).setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_refresh)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setVisible(true);

        mNowPlayingMenuItem = menu.findItem(R.id.action_now_playing).setIcon(
                new IconDrawable(this, MaterialCommunityIcons.mdi_playlist_play)
                        .colorRes(R.color.colorAccentLight)
                        .actionBarSize())
                .setVisible(false);

        // TODO: Show the now playing action if intent sourced from queue activity

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mPlaylistsFragment.loadPlaylists();

                return true;
            case R.id.action_logout:
                ((PartyApplication) getApplication()).logout(this, mPlaylistsActivity);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttached(PlaylistsFragment fragment) {
        mPlaylistsFragment = fragment;
    }

    @Override
    public void onPlaylistLoadError(SpotifyError spotifyError) {
        // The access token has expired
        if (spotifyError.getRetrofitError().getResponse().getStatus() == 401 &&
                spotifyError.getErrorDetails().message.contains("token expired")) {
            getFragmentManager().beginTransaction().hide(mPlaylistsFragment).commit();

            mSpotify.expireToken();

            // TODO: Navigate to main activity with logout intent

            return;
        }

        Log.d("d", spotifyError.toString());
        Snackbar.make(
                        mPlaylistsActivity,
                        spotifyError.getErrorDetails().message, Snackbar.LENGTH_LONG
                ).show();
    }

    @Override
    public void onPlaylistsLoaded() {
        mLoadingView.setVisibility(View.GONE);

        getFragmentManager().beginTransaction().show(mPlaylistsFragment).commit();
    }

    @Override
    public void onPlaylistSelected(PlaylistSimple item) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                .hide(mPlaylistsFragment).commit();

        mToolbar.setTitle(R.string.queue);

        // TODO: Move this to queue activity
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: Navigate to queue activity

                // TODO: Setup queue stuff?
            }
        }, 400);
    }
}
