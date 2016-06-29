package com.conestogac.assignment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.conestogac.assignment2.Model.Author;
import com.conestogac.assignment2.Model.Post;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * This call is a fragment for processing uploading new post
 * It will be run as Async Task for non blocking UI thread and
 * Call back onPostUploaded will be called after finishing or error is occurred.
 */
public class NewPostUploadTaskFragment extends Fragment {
    private static final String TAG = "NewPostTaskFragment";

    //define callback interface, callback implemented at caller;NewPostActivity class
    public interface TaskCallbacks {
        //To check: void onBitmapResized(Bitmap resizedBitmap, int mMaxDimension);
        void onPostUploaded(String error);
    }

    private Context mApplicationContext;
    private TaskCallbacks mCallbacks;

    //default contructor
    public NewPostUploadTaskFragment() {
        // Required empty public constructor
    }

    //create static to survive after exit
    public static NewPostUploadTaskFragment newInstance() {
        return new NewPostUploadTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across config changes.
        setRetainInstance(true);
    }
    //when fragement is called, set context
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /*
        Called from MainActivity for uploading
        It will call async task, UploadPostTask for uploading picture and set database
     */
    public void uploadPost(Bitmap bitmap, String inBitmapPath, Bitmap thumbnail,
                           String inThumbnailPath, String inFileName, String inPostText) {

        UploadPostTask uploadTask = new UploadPostTask(bitmap, inBitmapPath, thumbnail,
                            inThumbnailPath, inFileName, inPostText);
        uploadTask.execute();
    }

    /*
        Async task for uploading picture
        After finishing, callback,onPostUploaded() will be called
     */
    class UploadPostTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Bitmap> bitmapReference;
        private WeakReference<Bitmap> thumbnailReference;
        private String postText;
        private String fileName;

        public UploadPostTask(Bitmap bitmap, String inBitmapPath, Bitmap thumbnail, String inThumbnailPath,
                              String inFileName, String inPostText) {
            bitmapReference = new WeakReference<Bitmap>(bitmap);
            thumbnailReference = new WeakReference<Bitmap>(thumbnail);
            postText = inPostText;
            fileName = inFileName;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            //get full size bitmap reference
            Bitmap fullSize = bitmapReference.get();

            //get thumbnail bitmap reference
            final Bitmap thumbnail = thumbnailReference.get();

            //if one of ref is null, do not send
            if (fullSize == null || thumbnail == null) {
                return null;
            }

            //create firebase storage reference object
            FirebaseStorage storageRef = FirebaseStorage.getInstance();
            //set storage reference
            StorageReference photoRef = storageRef.getReferenceFromUrl("gs://" + getString(R.string.google_storage_bucket));
            //get system time
            Long timestamp = System.currentTimeMillis();
            //create storage reference to file using user id, and "full" or "thumb", and system time to store image
            final StorageReference fullSizeRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("full").child(timestamp.toString()).child(fileName + ".jpg");
            final StorageReference thumbnailRef = photoRef.child(FirebaseUtil.getCurrentUserId()).child("thumb").child(timestamp.toString()).child(fileName + ".jpg");
            Log.d(TAG, fullSizeRef.toString());
            Log.d(TAG, thumbnailRef.toString());

            //create byte array stream to be used during compression
            ByteArrayOutputStream fullSizeStream = new ByteArrayOutputStream();
            //compress BMP with JPEG with 90% quality by using stream buffer
            fullSize.compress(Bitmap.CompressFormat.JPEG, 90, fullSizeStream);

            //change into byte array to upload
            byte[] bytes = fullSizeStream.toByteArray();
            //write compressed data into fire storage
            fullSizeRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //After success, get download url
                    final Uri fullSizeUrl = taskSnapshot.getDownloadUrl();

                    //Upload thumbnail like full size image
                    ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, thumbnailStream);

                    //upload files
                    thumbnailRef.putBytes(thumbnailStream.toByteArray())
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Get database reference
                                    final DatabaseReference ref = FirebaseUtil.getBaseRef();

                                    //get post reference
                                    DatabaseReference postsRef = FirebaseUtil.getPostsRef();

                                    //get post key
                                    final String newPostKey = postsRef.push().getKey();

                                    //get thumbnail download url
                                    final Uri thumbnailUrl = taskSnapshot.getDownloadUrl();

                                    //get current user's object
                                    Author author = FirebaseUtil.getAuthor();

                                    //exception, if user didn't login
                                    if (author == null) {
                                        FirebaseCrash.logcat(Log.ERROR, TAG, "Couldn't upload post: Couldn't get signed in user.");
                                        mCallbacks.onPostUploaded(mApplicationContext.getString(
                                                R.string.error_user_not_signed_in));
                                        return;
                                    }

                                    //create post object to upload at databse
                                    Post newPost = new Post(author, fullSizeUrl.toString(), fullSizeRef.toString(),
                                            thumbnailUrl.toString(), thumbnailRef.toString(), postText, ServerValue.TIMESTAMP);

                                    //create Map to upload data
                                    Map<String, Object> updatedUserData = new HashMap<>();

                                    //Add post_id to current user's folder
                                    updatedUserData.put(FirebaseUtil.getPeoplePath() + author.getUid() + "/posts/"
                                            + newPostKey, true);

                                    //Add new post to post_id folder
                                    updatedUserData.put(FirebaseUtil.getPostsPath() + newPostKey,
                                            new ObjectMapper().convertValue(newPost, Map.class));

                                    ref.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                            if (firebaseError == null) {
                                                mCallbacks.onPostUploaded(null);
                                            } else {
                                                Log.e(TAG, "Unable to create new post: " + firebaseError.getMessage());
                                                FirebaseCrash.report(firebaseError.toException());
                                                mCallbacks.onPostUploaded(mApplicationContext.getString(
                                                        R.string.error_upload_task_create));
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload post to database.");
                            FirebaseCrash.report(e);
                            mCallbacks.onPostUploaded(mApplicationContext.getString(
                                    R.string.error_upload_task_create));
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Failed to upload post to database.");
                    FirebaseCrash.report(e);
                    mCallbacks.onPostUploaded(mApplicationContext.getString(
                            R.string.error_upload_task_create));
                }
            });
            // TODO: Refactor these insanely nested callbacks.
            return null;
        }
    }

}
