package com.conestogac.assignment2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class NewPostActivity extends BaseActivity implements
        NewPostUploadTaskFragment.TaskCallbacks {
    public  static String tmpFileName = "Activity";
    public  static final String TAG_TASK_FRAGMENT = "newPostUploadTaskFragment";
    private static final String TAG = NewPostActivity.class.getSimpleName();
    private static final String EXTRA_FILENAME = "com.conestogac.assignment2.EXTRA_FILENAME";

    private static final int THUMBNAIL_MAX_DIMENSION = 640;
    private static final int FULL_SIZE_MAX_DIMENSION = 1280;
    private static final int REQUEST_EXTERNAL_STORAGE_CAM = 0;
    private static final int ACTION_TAKE_PHOTO = 1;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null ;
    private String getAlbumName() {
        return getString(R.string.album_name);
    }
    private String mCurrentPhotoPath;
    private File output = null;
    private ImageView image;
    private EditText edText;
    private Button btSave;

    private Bitmap mResizedBitmap;
    private Bitmap mThumbnail;
    private NewPostUploadTaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Create Activity");

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (NewPostUploadTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new NewPostUploadTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_write_message);
            image = (ImageView) findViewById(R.id.ivPhoto);
            edText = (EditText) findViewById(R.id.edText);
            btSave = (Button) findViewById(R.id.btSave);

            //to prevent keyboard showup at first screen following are set at LinearLayout
            //android:focusable="true"
            //android:focusableInTouchMode="true"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
            } else {
                mAlbumStorageDirFactory = new BaseAlbumDirFactory();
            }
        } else {
            output = (File)savedInstanceState.getSerializable(EXTRA_FILENAME);
            mCurrentPhotoPath = output.getAbsolutePath();
        }
    }

    /*
        before moving, save file handler
     */
    @Override
    protected  void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_FILENAME, output);
    }

    /*
        After taking picture, it will be caleed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        Log.d(TAG, "requestCode: " + requestCode + ", resultCode: " + resultCode);
        if(requestCode == ACTION_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if(output.exists()){
                setPic();
            }
        }
    }

    /*
        Called when user click on imageview
    */
    public void takePhoto(View view)
    {
        if (!isStoragePermissionGranted()) {
            return;
        }

        dispatchTakePictureIntent();
    }

    /*
        Read from file, decode to bmp, and resize to fit into image view
     */
    private boolean setPic() {
        int targetW = image.getMeasuredWidth();
        int targetH = image.getMeasuredHeight();

        //get full size to fit into imageview
        mResizedBitmap = decodeSampledBitmapFromFile(targetW, targetH);

        // Get thumbnail
        mThumbnail = decodeSampledBitmapFromFile(THUMBNAIL_MAX_DIMENSION, THUMBNAIL_MAX_DIMENSION);

        // Set imageview with decoded bmp.
        // center crop option is used not to hurt width/height ratio
        image.setImageBitmap(mResizedBitmap);
        return true;
    }

    /*
        decode to target size and return bitmap pointer
     */
    private Bitmap decodeSampledBitmapFromFile(int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);

        //in case of clear without taking photo
        if (bmOptions.outMimeType == null) {
            return null;
        }

        // Figure out which way needs to be reduced less
        int scaleFactor = calculateInSampleSize(bmOptions, targetW, targetH );
        Log.d(TAG, "scaleFactor:" + scaleFactor);

        // Set bitmap options to scale the image decode target
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // Decode the JPEG file into a Bitmap
        return BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);
    }
    /*
        Calculate Scale
        scale will be chosen to maintain full view of width.
     */
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /*
        get Storage permission
        From SDK 23, For special permission, user can remove permission after installation
        So it is needed to check permission at runtime
     */
    private  boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is already granted");
                return true;
            } else {
                //If permission is no granted, ask user to permit
                Log.v(TAG, "Show Rationale");
                if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "Displaying external storage permission rationale to provide additional context.");
                    Snackbar.make(edText, R.string.permission_rationale_cam, Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_CAM);
                                }
                            });
                } else {
                    Log.v(TAG, "Permission is revoked");
                    requestPermissions(new String[] {WRITE_EXTERNAL_STORAGE} , REQUEST_EXTERNAL_STORAGE_CAM);
                }
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted due to SDK");
            return true;
        }
        return false;
    }


     /*
        Callback received when a permissions request has been completed.
      */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_CAM) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            }
        }
    }

    /*
        Taking picture
    */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i(TAG, "Now, REQUEST_EXTERNAL_STORAGE is granted ");
        try {
            output = createImageFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
            Log.d(TAG, "dispatchTakePictureIntent");

        } catch (IOException e) {
            e.printStackTrace();
            output = null;
            mCurrentPhotoPath = null;
            Log.e(TAG, "dispatchTakePictureIntent something wrong!!");
        }
        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
    }


    /*
        Create Image File before uploading
     */
    private File createImageFile() throws IOException {
        String imageFileName = JPEG_FILE_PREFIX + tmpFileName + JPEG_FILE_SUFFIX;

        File fp = new File(getAlbumDir(), imageFileName);
        if (fp.exists()) {
            fp.delete();
        }

        Log.d(TAG, "createImageFile: " + fp.getAbsolutePath());
        mCurrentPhotoPath = fp.getAbsolutePath();
        return fp;
    }

    /*
        Delete Temp File
     */
    private void deleteTempFile() {
        Log.d(TAG, "deleteTempFile()"+mCurrentPhotoPath);
        if (mCurrentPhotoPath != null) {
            output = new File(mCurrentPhotoPath);
            output.delete();
        }
    }

    /*
        Return Album Dir File pointer. If Dir does not exist then create and return
     */
    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            Log.d(TAG, "getAlbumDir: "+storageDir);
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d(TAG, "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.d(TAG, "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /*
        When user select upload, it will upload to Firebase
        It will check whether text and image is not null
     */
    public void saveContent(View view) {
        //check activity is filled out
        if (TextUtils.isEmpty(edText.getText().toString())) {
            Toast.makeText(NewPostActivity.this, "Explain your activity.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //check activity photo is taken
        if (output == null) {
            Toast.makeText(NewPostActivity.this, "Activity photo is required.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //show uploading... dialog and disable button
        showProgressDialog(getString(R.string.upload_progress_message));
        btSave.setEnabled(false);

        //get system time
        Long timestamp = System.currentTimeMillis();

        //Set path of full image and thumbnail image
        String bitmapPath = "/" + FirebaseUtil.getCurrentUserId() + "/full/" + timestamp.toString() + "/";
        String thumbnailPath = "/" + FirebaseUtil.getCurrentUserId() + "/thumb/" + timestamp.toString() + "/";
        mTaskFragment.uploadPost(mResizedBitmap, bitmapPath, mThumbnail, thumbnailPath,
                Uri.fromFile(output).getLastPathSegment(), edText.getText().toString());
    }

    /*
        After uploading, this method will be called
     */
    @Override
    public void onPostUploaded(final String error) {
        NewPostActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btSave.setEnabled(true);
                dismissProgressDialog();
                if (error == null) {
                    Toast.makeText(NewPostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NewPostActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
