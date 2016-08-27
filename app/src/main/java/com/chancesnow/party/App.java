package com.chancesnow.party;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;

import com.chancesnow.party.middleware.PersistenceController;
import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;

import trikita.jedux.Action;
import trikita.jedux.Logger;
import trikita.jedux.Store;

public class App extends Application {

    public static final String PREFS_GENERAL = "PartyPrefs";

    public static final int PICK_PLAYLIST_REQUEST = 1;

    public static final LinearLayout.LayoutParams WRAP_CONTENT_LAYOUT = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    public static final LinearLayout.LayoutParams ZERO_LAYOUT = new LinearLayout.LayoutParams(0, 0, 0);
    public static final LinearLayout.LayoutParams FLEX_LAYOUT = new LinearLayout.LayoutParams(0, 0, 1);

    private static App instance;

    private Store<Action<AppAction, ?>, State> store;

    private SpotifyClient mSpotify;

    @Override
    public void onCreate() {
        super.onCreate();
        App.instance = this;

        PersistenceController persistenceController = new PersistenceController(this);
        State initialState = persistenceController.getSavedState();
        if (initialState == null) {
            initialState = State.Default.build();
        }

        this.store = new Store<>(new State.Reducer(), initialState,
                new Logger<>("Party"),
                persistenceController);

        Iconify.with(new MaterialCommunityModule());

        mSpotify = SpotifyClient.getInstance(getApplicationContext());
    }

    public SpotifyClient getSpotifyClient() {
        return mSpotify;
    }

    public static State getState() {
        return instance.store.getState();
    }

    public static State dispatch(Action<AppAction, ?> action) {
        return instance.store.dispatch(action);
    }

    public void confirmLogout(final Activity activityContext) {
        new AlertDialog.Builder(activityContext)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logout(activityContext);
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void logout(Activity activityContext) {
        dumpUserData();

        activityContext.startActivity(new Intent(activityContext, MainActivity.class));
    }

    public void dumpUserData() {
        mSpotify.expireToken();

        SharedPreferences state = getSharedPreferences(PREFS_GENERAL, 0);
        SharedPreferences.Editor stateEditor = state.edit();

        stateEditor.clear();

        stateEditor.putString(SpotifyClient.STATE_TOKEN, SpotifyClient.TOKEN_EXPIRED);

        stateEditor.commit();
    }

    public void openWebPage(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}