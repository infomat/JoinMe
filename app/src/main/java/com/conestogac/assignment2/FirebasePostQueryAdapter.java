package com.conestogac.assignment2;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.conestogac.assignment2.Model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
    This class a adapter class extends from RecylerView.Adapater
    After getting querried result, it will setup view with viewhodler within onBindViewHolder()
    and set up listener.
    After setup, onSetupView() will be called through interface to fill out the data at viewholder
 */
public class FirebasePostQueryAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private final String TAG = "PostQueryAdapter";
    private List<String> mPostPaths;
    private OnSetupViewListener mOnSetupViewListener;

    //paths of post to be displayed will be handed from PostFragment
    //set up "setup view" callback
    public FirebasePostQueryAdapter(List<String> paths, OnSetupViewListener onSetupViewListener) {
        if (paths == null || paths.isEmpty()) {
            mPostPaths = new ArrayList<>();
        } else {
            mPostPaths = paths;
        }
        mOnSetupViewListener = onSetupViewListener;
    }

    //create view holder, which layout is defined at post_item.xml
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(v);
    }

    public void setPaths(List<String> postPaths) {
        mPostPaths = postPaths;
        notifyDataSetChanged();
    }

    public String getPaths(int position) {
        return mPostPaths.get(position);
    }

    public void addItem(String path) {
        mPostPaths.add(path);
        notifyItemInserted(mPostPaths.size());
    }

    //To remove item from mPostPaths which is a list of postkey to be displayed
    //this will be used for swiping to remove cardview after swiping; onSwiped()
    public void removeItem(int location) {
        mPostPaths.remove(location);
        notifyItemRemoved(mPostPaths.size());
    }

    //After quering and getting data from the server, view will be setup with received information
    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        DatabaseReference ref = FirebaseUtil.getPostsRef().child(mPostPaths.get(position));
        // TODO: Fix this so async event won't bind the wrong view post recycle.
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Log.d(TAG, "post key: " + dataSnapshot.getKey());

                mOnSetupViewListener.onSetupView(holder, post, holder.getAdapterPosition(),
                            dataSnapshot.getKey());

                //For swiping -> to get key for swiped item
                holder.setPostKey(dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(TAG, "Error occurred: " + firebaseError.getMessage());
            }
        };
        ref.addValueEventListener(postListener);
        holder.mPostRef = ref;
        holder.mPostListener = postListener;
    }

    @Override
    public void onViewRecycled(PostViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mPostRef.removeEventListener(holder.mPostListener);
    }

    //This will be called during the setup view to decide how many viewhodler will be created
    @Override
    public int getItemCount() {
        return mPostPaths.size();
    }

    public interface OnSetupViewListener {
        void onSetupView(PostViewHolder holder, Post post, int position, String postKey);
    }
}
