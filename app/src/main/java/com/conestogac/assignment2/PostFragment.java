package com.conestogac.assignment2;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.conestogac.assignment2.Model.Author;
import com.conestogac.assignment2.Model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
 * Fragment to display list of posts
 */
public class PostFragment extends Fragment {

    public static final String TAG = "PostFragment";
    public static final String LOCATION_EXTRA = "location";
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";

    private int mRecyclerViewPosition = 0;
    private OnPostSelectedListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<PostViewHolder> mAdapter;

    private Location curLocation;

    public PostFragment() {
        // Required empty public constructor
    }


    //depending on type, postfragment will be created
    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();

        return fragment;
    }

    /*
        It is call at first when activity call fragment,
        and save activity context for future reference
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPostSelectedListener) {
            mListener = (OnPostSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPostSelectedListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
        Inflate fragement view via layout fragment_post.xml which has recycler view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        rootView.setTag(TAG);

        // Add view under rootView
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        return rootView;
    }


    /*
        When hosting Activy(FeedActivity) views are created and hosting activity if functional,
        so, here all acivity related tasks are done.

        To display list of posts, RecyclerView is used which is a more advanced and flexible version
        of Listview. It is a container of displaying large datasets that can be scrolled very efficiently
        by mainainting a limited number of view which is proper to use when its elements change at runtime
        based on user action or network events

        It provides Layoutmanager for positioning items and default animations for common item
        operations, such as removal or addition of items

        RecylerView (Layoutmanger) -> Adapter -> Dataset
        It needs to specify layoutmanager and adapter
        Layout manager postions item views inside a RecycleView and determines when to reuse item view
        that are no longer visible to the user. To reuse a view, a lauout manager may ask the adapter
        to replace the contents of the view with a different eleemtns from the dataset. Recycling views in this
        manner improves performance by avoiding the creation of unnecesary views or performace expensive
        findViewByID() lookups. LayoutManager can be Linear, Grid, Staggered
        Ref. https://developer.android.com/training/material/lists-cards.html
     */

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        //The list of fills its content starting from the bottom of the view
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        //Set Activity Linearlayout manager as Recyclerview
        mRecyclerView.setLayoutManager(linearLayoutManager);

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mRecyclerViewPosition = (int) savedInstanceState
                    .getSerializable(KEY_LAYOUT_POSITION);
            mRecyclerView.scrollToPosition(mRecyclerViewPosition);
        }

        // Get current location
        curLocation = FeedsActivity.currentLocation;


        //Get all post
        //Todo with settting all posts or preferred posts
        Log.d(TAG, "Restoring recycler view position (all): " + mRecyclerViewPosition);
        Query allPostsQuery = FirebaseUtil.getPostsRef();
        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);

        //Set observer on onItemRangeInserted
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    /*
        Clean up adapter
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null && mAdapter instanceof FirebaseRecyclerAdapter) {
            ((FirebaseRecyclerAdapter) mAdapter).cleanup();
        }
    }

    /*
        Set Listener as null
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    /*
        Restore postion after getting back
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        int recyclerViewScrollPosition = getRecyclerViewScrollPosition();
        Log.d(TAG, "Recycler view scroll position: " + recyclerViewScrollPosition);
        savedInstanceState.putSerializable(KEY_LAYOUT_POSITION, recyclerViewScrollPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    /*
        Setup Firebase Recycle Adapter with Post and ViewHolder
     */
    private FirebaseRecyclerAdapter<Post, PostViewHolder> getFirebaseRecyclerAdapter(Query query) {
        return new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class, R.layout.post_item, PostViewHolder.class, query) {
            @Override
            public void populateViewHolder(final PostViewHolder postViewHolder,
                                           final Post post, final int position) {
                setupPost(postViewHolder, post, position, null);
            }

            @Override
            public void onViewRecycled(PostViewHolder holder) {
                super.onViewRecycled(holder);
                //todo
//                FirebaseUtil.getLikesRef().child(holder.mPostKey).removeEventListener(holder.mLikeListener);
            }
        };
    }

    /*
        Setup post which is called from FirebbaseRecyclerAdapter's populateViewHolder
        This will map Post value into Viewholder
     */
    private void setupPost(final PostViewHolder postViewHolder, final Post post, final int position, final String inPostKey) {
        postViewHolder.setPhoto(post.getThumb_url());
        postViewHolder.setText(post.getText());
        postViewHolder.setTimestamp(DateUtils.getRelativeTimeSpanString(
                (long) post.getTimestamp()).toString());

        final String postKey;
        float[] results = {0};

        if (curLocation != null) {
            Log.d(TAG,Double.valueOf(post.getLocation().getlatitude())+":" +
                    Double.valueOf(post.getLocation().getlongitude())+" to "+
                            curLocation.getLatitude() + ":" +
                            curLocation.getLongitude());
            Location.distanceBetween(Double.valueOf(post.getLocation().getlatitude()),
                    Double.valueOf(post.getLocation().getlongitude()),
                    curLocation.getLatitude(),
                    curLocation.getLongitude(), results);
            postViewHolder.setDistance(results[0]);
        } else {
            postViewHolder.setDistance(0);
        }
        Log.d(TAG, "Distance: "+results[0]);

        //get post key by using position
        if (mAdapter instanceof FirebaseRecyclerAdapter) {
            postKey = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
        } else {
            postKey = inPostKey;
        }

        //get author and set
        Author author = post.getAuthor();
        postViewHolder.setAuthor(author.getFullname());
        Log.d(TAG, "Author Name: "+author.getFullname());

        //setup listener for data changes (Likes)
        ValueEventListener likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postViewHolder.setNumLikes(dataSnapshot.getChildrenCount());

                //Todo User likes, none, dislikes
                if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId())) {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus.LIKED, getActivity());
                } else {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus.NOTLIKED, getActivity());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //add like listener to posted item
        FirebaseUtil.getLikesRef().child(postKey).addValueEventListener(likeListener);
        postViewHolder.mLikeListener = likeListener;

        //set up for click listener for Like item
        postViewHolder.setPostClickListener(new PostViewHolder.PostClickListener() {
            @Override
            public void toggleLike() {
                Log.d(TAG, "Like position: " + position);
                mListener.onPostLike(postKey);
            }
        });
    }



    private int getRecyclerViewScrollPosition() {
        int scrollPosition = 0;
        // TODO: Is null check necessary?
        if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }
        return scrollPosition;
    }
    /**
      This interface must be implemented by activities that contain this
      fragment to allow an interaction in this fragment to be communicated
      to the activity and potentially other fragments contained in that
      activity.
     */
    public interface OnPostSelectedListener {
        void onPostLike(String postKey);
    }

}
