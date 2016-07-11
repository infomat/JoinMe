package com.conestogac.assignment2;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.conestogac.assignment2.Model.Author;
import com.conestogac.assignment2.Model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/*
 * Fragment to display list of posts
 */
public class PostFragment extends Fragment
    implements SetDistanceFragment.SetDistanceDialogListener{

    public static final String TAG = "PostFragment";
    public static final String LOCATION_EXTRA = "location";
    private static final String KEY_LAYOUT_POSITION = "layoutPosition";
    private static int displayMode = 0;

    private int mRecyclerViewPosition = 0;
    private OnPostSelectedListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<PostViewHolder> mAdapter;
    private TextView mEmptyView;
    private Location curLocation;
    private long distanceSetting;

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
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        distanceSetting = sharedPref.getInt(getString(R.string.saved_distance), 0);
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
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_list_item);

        //To Process actionbar event
        setHasOptionsMenu(true);

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

        // Get current location to calculate distanceSetting
        curLocation = ListPostActivity.currentLocation;

        //Get post
        //Todo with setting, get all posts or preferred posts
        Log.d(TAG, "Restoring recycler view position (all): " + mRecyclerViewPosition);

        showNew();

        //Item Touch Helper for swiping
        //https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.html
        //Set callback as simpleCallbackItemTouchHelper and attach Recyle view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallbackItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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

    // This is called when the dialog is completed and the results have been passed
    @Override
    public void onFinishSetDistanceDialog(int distanceSetting) {
        //update setting distance 
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_distance), distanceSetting);
        editor.commit();
        this.distanceSetting = distanceSetting;
        getActivity().setTitle("Distance: "+ String.valueOf(distanceSetting) + "kms");
        showNew();
    }


    /*
        Show all when user select All menu on Actionbar
    */
    private void showAll() {
        Query allPostsQuery = FirebaseUtil.getPostsRef();

        mAdapter = getFirebaseRecyclerAdapter(allPostsQuery);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    /*
          Show according to distance filter
      */
    private void showNew() {
        Query allPostsQuery = FirebaseUtil.getPostsRef();

        allPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot likeSnapshot) {
                float[] results = {0};
                final List<String> postPaths = new ArrayList<String>();
                for (DataSnapshot snapshot : likeSnapshot.getChildren()) {
                    Log.d(TAG, "adding post key: " + snapshot.getKey());
                    Log.d(TAG, "snapshot value: " +snapshot.getValue());

                    Post post = snapshot.getValue(Post.class);
                    //Calculate Distance
                    Location.distanceBetween(Double.valueOf(post.getLocation().getlatitude()),
                            Double.valueOf(post.getLocation().getlongitude()),
                            curLocation.getLatitude(),
                            curLocation.getLongitude(), results);

                    if ((distanceSetting == 0)|| (results[0] <= distanceSetting * 1000))
                        postPaths.add(snapshot.getKey());
                }

                //send list of liked post id to FirebasePostQueryAdapter
                mAdapter = new FirebasePostQueryAdapter(postPaths, new FirebasePostQueryAdapter.OnSetupViewListener() {
                    @Override
                    public void onSetupView(PostViewHolder holder, Post post, int position, String postKey) {
                        setupPost(holder, post, position, postKey);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    /*
        Show like when user select like menu on Actionbar
        Todo: Set Observer on delete
    */
    private void showLike() {
        final DatabaseReference postLikeRef = FirebaseUtil.getLikesRef();
        //Only Likes
        postLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot likeSnapshot) {
                final List<String> postPaths = new ArrayList<String>();
                for (DataSnapshot snapshot : likeSnapshot.getChildren()) {
                    Log.d(TAG, "adding post key: " + snapshot.getKey());
                    Log.d(TAG, "snapshot value: " +snapshot.getValue());
                    if (snapshot.getValue().toString().contains(FirebaseUtil.getCurrentUserId()))
                        if (snapshot.getValue().toString().contains("=1"))
                            //Calculate Distance
                            postPaths.add(snapshot.getKey());
                }

                //send list of liked post id to FirebasePostQueryAdapter
                mAdapter = new FirebasePostQueryAdapter(postPaths, new FirebasePostQueryAdapter.OnSetupViewListener() {
                    @Override
                    public void onSetupView(PostViewHolder holder, Post post, int position, String postKey) {
                        setupPost(holder, post, position, postKey);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    /*
        Show like when user select dislike menu on Actionbar
        Todo: Set Observer on delete
    */
    private void showDislike() {
        final DatabaseReference postLikeRef = FirebaseUtil.getLikesRef();
        //Only Likes
        postLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot likeSnapshot) {
                final List<String> postPaths = new ArrayList<String>();
                for (DataSnapshot snapshot : likeSnapshot.getChildren()) {
                    Log.d(TAG, "adding post key: " + snapshot.getKey());
                    Log.d(TAG, "snapshot value: " +snapshot.getValue());
                    if (snapshot.getValue().toString().contains(FirebaseUtil.getCurrentUserId()))
                        if (snapshot.getValue().toString().contains("=2"))
                            postPaths.add(snapshot.getKey());
                }

                //send list of liked post id to FirebasePostQueryAdapter
                mAdapter = new FirebasePostQueryAdapter(postPaths, new FirebasePostQueryAdapter.OnSetupViewListener() {
                    @Override
                    public void onSetupView(PostViewHolder holder, Post post, int position, String postKey) {
                        setupPost(holder, post, position, postKey);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
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
        postViewHolder.setIcon(post.getAuthor().getEmail());
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
            postViewHolder.setDistance((float)(results[0]/1000.0));
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
                int likesCount = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if ((long) child.getValue() == 1) {
                        likesCount++;
                    }
                }
                postViewHolder.setNumLikes(likesCount);

                //Todo User likes, none, dislikes
                //if current user has likes, then set likes
                //if current user has dislikes then set dislikes
                //else set none

                if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId()) &&
                        (long)dataSnapshot.child(FirebaseUtil.getCurrentUserId()).getValue() == 1) {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus_LIKED, getActivity());
                } else if (dataSnapshot.hasChild(FirebaseUtil.getCurrentUserId()) &&
                        (long)dataSnapshot.child(FirebaseUtil.getCurrentUserId()).getValue() == 2) {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus_NOTLIKED, getActivity());
                } else {
                    postViewHolder.setLikeStatus(PostViewHolder.LikeStatus_NONE, getActivity());
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
                mListener.onPostChangeLikeStatus(postKey);
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

    /*
        https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.SimpleCallback.html
        Simple wrapper to the default Callback with drag, swipe directions
     */
    ItemTouchHelper.SimpleCallback simpleCallbackItemTouchHelper = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT){

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }


        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            String keyToChange;

            if (mAdapter instanceof FirebaseRecyclerAdapter) {
                keyToChange = ((FirebaseRecyclerAdapter) mAdapter).getRef(position).getKey();
            } else {
                keyToChange = ((PostViewHolder)viewHolder).getPostKey();
            }

            //remove selected item from adapter when selected
            ((FirebasePostQueryAdapter) mAdapter).removeItem(position);

            if (direction == ItemTouchHelper.LEFT) {
                Log.d(TAG, "Swipe LEFT  "+position);
                mListener.onPostLike(keyToChange);
            } else if (direction == ItemTouchHelper.RIGHT){
                Log.d(TAG, "Swipe RIGHT "+position);
                mListener.onPostDisLike(keyToChange);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_show_all:
                showMessage("Show New within Distance setting");
                showNew();
                break;

            case R.id.action_show_dislike:
                showMessage("Show what you set as Dislike regardless of Distance");
                showDislike();
                break;

            case R.id.action_show_like:
                showMessage("Show what you set as Like regardless of Distance");
                showLike();
                break;

            case R.id.action_setting_distance:
                setTargetFragment(this, 1);
                SetDistanceFragment setDistanceFragment = SetDistanceFragment.newInstance("Set Distance");
                // SETS the target fragment for use later when sending results
                setDistanceFragment.setTargetFragment(PostFragment.this, 300);
                setDistanceFragment.show(getActivity().getSupportFragmentManager(), "fragment_edit_name");

                break;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg,
                Toast.LENGTH_SHORT).show();
    }
    /**
      This interface must be implemented by activities that contain this
      fragment to allow an interaction in this fragment to be communicated
      to the activity and potentially other fragments contained in that
      activity.
     */
    public interface OnPostSelectedListener {
        void onPostChangeLikeStatus(String postKey);
        void onPostLike(String postKey);
        void onPostDisLike(String postKey);
    }
}
