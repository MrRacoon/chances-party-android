package com.chancesnow.party;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chancesnow.party.PlaylistsFragment.OnPlaylistListListener;
import com.chancesnow.party.spotify.SpotifyClient;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaylistSimple} and makes a call to the
 * specified {@link OnPlaylistListListener}.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();

    private final List<PlaylistSimple> mValues;
    private final PlaylistsFragmentListener mListener;

    private Drawable mIconPlaceholder;
    private List<Integer> mUnloadedIcons;
    private int mSelectedIndex;
    private int mPlayingIndex;

    public PlaylistAdapter(List<PlaylistSimple> items, PlaylistsFragmentListener listener) {
        mValues = items;
        mListener = listener;
        mUnloadedIcons = new ArrayList<>();
        mSelectedIndex = -1;
        mPlayingIndex = -1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mIconPlaceholder =
                new IconDrawable(
                        recyclerView.getContext(), MaterialCommunityIcons.mdi_music_note)
                        .colorRes(R.color.colorAccentLightControl);
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

        int trackCount = mValues.get(position).tracks.total;
        String trackCountText = (trackCount == 1) ?
                holder.mView.getContext().getString(
                        R.string.track_count_singular,
                        numberFormat.format(trackCount)
                ) :
                holder.mView.getContext().getString(
                        R.string.track_count,
                        numberFormat.format(trackCount)
                );
        holder.mTrackCountLabel.setText(trackCountText);

        // Download the icon, if available
        String iconUrl = SpotifyClient.getLargestImage(mValues.get(position).images, 500);
        // If there is no icon under 500 square pixels, get the largest icon
        if (iconUrl == null)
            iconUrl = SpotifyClient.getLargestImage(mValues.get(position).images, Integer.MAX_VALUE);
        // Set the ViewHolder's icon
        if (iconUrl != null) {
            RequestCreator picassoIcon = Picasso
                    .with(holder.mIcon.getContext())
                    .load(iconUrl)
                    .placeholder(mIconPlaceholder);

            if (holder.mIcon.getWidth() > 0)
                picassoIcon.resize(holder.mIcon.getWidth(), holder.mIcon.getHeight());

            picassoIcon.into(holder.mIcon);
        } else {
            holder.mIcon.setImageDrawable(mIconPlaceholder);
            holder.mIcon.setScaleType(ImageView.ScaleType.CENTER);

            mUnloadedIcons.add(position);
        }

        holder.mView.setSelected(mSelectedIndex == position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldIndex = mSelectedIndex;
                mSelectedIndex = holder.getAdapterPosition();

                if (oldIndex >= 0)
                    notifyItemChanged(oldIndex);

                v.setSelected(true);

                if (mListener != null) {
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

    public void setSelectedIndex(int position) {
        int oldIndex = mSelectedIndex;

        mSelectedIndex = position;

        if (oldIndex >= 0)
            notifyItemChanged(oldIndex);

        if (position >= 0)
            notifyItemChanged(position);
    }

    public void setPlayingIndex(int position) {
        int oldIndex = mPlayingIndex;

        mPlayingIndex = position;

        if (oldIndex >= 0)
            notifyItemChanged(oldIndex);

        if (position >= 0)
            notifyItemChanged(position);
    }

    public void tryReloadUnloadedIcons() {
        if (mUnloadedIcons.size() > 0) {
            for (int position : mUnloadedIcons)
                notifyItemChanged(position);

            mUnloadedIcons.clear();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIcon;
        public final TextView mNameLabel;
        public final TextView mTrackCountLabel;
        public PlaylistSimple mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIcon = (ImageView) view.findViewById(R.id.playlist_icon);
            mNameLabel = (TextView) view.findViewById(R.id.playlist_name);
            mTrackCountLabel = (TextView) view.findViewById(R.id.playlist_trackCount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameLabel.getText() + "'";
        }
    }
}
