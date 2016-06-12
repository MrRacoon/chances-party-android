package com.chancesnow.party;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chancesnow.party.PlaylistFragment.OnPlaylistListListener;

import java.text.NumberFormat;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaylistSimple} and makes a call to the
 * specified {@link OnPlaylistListListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewAdapter.ViewHolder> {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    private final List<PlaylistSimple> mValues;
    private final PlaylistFragment.OnPlaylistListListener mListener;

    public PlaylistViewAdapter(List<PlaylistSimple> items, PlaylistFragment.OnPlaylistListListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameLabel.setText(mValues.get(position).name);
        holder.mTrackCountLabel.setText(
                holder.mView.getContext().getString(
                        R.string.track_count,
                        numberFormat.format(mValues.get(position).tracks.total)
                ));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPlaylistSelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public List<PlaylistSimple> getPlaylists() {
        return mValues;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameLabel;
        public final TextView mTrackCountLabel;
        public PlaylistSimple mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameLabel = (TextView) view.findViewById(R.id.playlist_name);
            mTrackCountLabel = (TextView) view.findViewById(R.id.playlist_trackCount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameLabel.getText() + "'";
        }
    }
}
