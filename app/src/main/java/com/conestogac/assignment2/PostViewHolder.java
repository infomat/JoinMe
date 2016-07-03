package com.conestogac.assignment2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import it.sephiroth.android.library.tooltip.Tooltip;
import it.sephiroth.android.library.tooltip.Tooltip.AnimationBuilder;
/**
 * This class is to hold a Post uploaded by user
 * It extends RecyclerView to reuse view template
 * without recreating it. It will enhance performance
 * Layout feed_item is related with this class
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    public final static String TAG = "PostViewHolder";
    private final View mView;
    private PostClickListener mListener;
    public DatabaseReference mPostRef;
    public ValueEventListener mPostListener;

    public static final int LikeStatus_NONE = 0;
    public static final int LikeStatus_LIKED = 1;
    public static final int LikeStatus_NOTLIKED = 2;

    private final ImageView mLikeIcon;
    private static final int POST_TEXT_MAX_LINES = 6;
    private ImageView mPhotoView;
    private ImageView mIconView;
    private TextView mAuthorView;
    private TextView mPostTextView;
    private TextView mTimestampView;
    private TextView mNumLikesView;
    private TextView mDistanceView;
    public String mPostKey;
    public ValueEventListener mLikeListener;
    private Tooltip.TooltipView mCurrentTooltip;


    public PostViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mIconView = (ImageView) mView.findViewById(R.id.post_author_icon);
        mPhotoView = (ImageView) itemView.findViewById(R.id.post_photo);
        mAuthorView = (TextView) mView.findViewById(R.id.post_author_name);
        mPostTextView = (TextView) itemView.findViewById(R.id.post_text);
        mTimestampView = (TextView) itemView.findViewById(R.id.post_timestamp);
        mNumLikesView = (TextView) itemView.findViewById(R.id.post_num_likes);
        mDistanceView = (TextView) itemView.findViewById(R.id.post_distance);

        mLikeIcon = (ImageView) itemView.findViewById(R.id.post_like_icon);
        mLikeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.toggleLike();
            }
        });
    }

    //Set user icon's onClickListener to show user detail
    public void setIcon(final String authorEmail) {
        mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserDetail(view,authorEmail);
            }
        });
    }

    public void setPhoto(String url) {
        GlideUtil.loadImage(url, mPhotoView);
    }

    public void setAuthor(String author) {
        if (author == null || author.isEmpty()) {
            author = mView.getResources().getString(R.string.user_info_no_name);
        }
        mAuthorView.setText(author);
    }

    public void setText(final String text) {
        if (text == null || text.isEmpty()) {
            mPostTextView.setVisibility(View.GONE);
            return;
        } else {
            mPostTextView.setVisibility(View.VISIBLE);
            mPostTextView.setText(text);
            mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
            mPostTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPostTextView.getMaxLines() == POST_TEXT_MAX_LINES) {
                        mPostTextView.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        mPostTextView.setMaxLines(POST_TEXT_MAX_LINES);
                    }
                }
            });
        }
    }

    private void showUserDetail(View view, String authorEmail) {
        Context context = mView.getContext();
        //https://github.com/sephiroth74/android-target-tooltip
        //Open source to display author email as a tooltips  if user select author icon
        //todo Due to library bug, if '.' is inclded, it displays blank
        Tooltip.make(
                context, new Tooltip.Builder(101)
                        .anchor(view, Tooltip.Gravity.BOTTOM)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(AnimationBuilder.DEFAULT)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(authorEmail.replace('.',','))
                        .maxWidth(600)
                        .withArrow(true)
                        .withOverlay(false)
                        .build()
        ).show();
        Log.d(TAG, "Author email: "+authorEmail);
    }

    public void setTimestamp(String timestamp) {
        mTimestampView.setText(timestamp);
    }

    public void setNumLikes(long numLikes) {
        String suffix = numLikes == 1 ? " like" : " likes";
        mNumLikesView.setText(numLikes + suffix);
    }

    public void setDistance(float distance) {
        String prefix = "Distance: ";
        String suffix = " km";
        String distanceStr = prefix+ String.format("%.2f",distance) + suffix;

        mDistanceView.setText(distanceStr);
    }

    public void setPostClickListener(PostClickListener listener) {
        mListener = listener;
    }

    public void setLikeStatus(int status, Context context) {
        int image;
        if (status == LikeStatus_LIKED) {
            image = R.drawable.heart_full;
        } else if (status == LikeStatus_NOTLIKED) {
            image = R.drawable.ic_thumb_down_black_36dp;
        } else {
            image = R.drawable.heart_empty;
        }
        mLikeIcon.setImageDrawable(ContextCompat.getDrawable(context,image));
    }

    public interface PostClickListener {
        void toggleLike();
    }
}
