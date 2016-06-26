package com.conestogac.assignment2;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class WriteMessageActivity extends AppCompatActivity {
    private static final String TAG = WriteMessageActivity.class.getSimpleName();
    private static final String EXTRA_FILENAME = "com.conestogac.assignment2.EXTRA_FILENAME";
    public  static String tmpFileName = "Activity";

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
    private Button btReset;
    private Button btSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Create Activity");
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_write_message);
            image = (ImageView) findViewById(R.id.ivPhoto);
            edText = (EditText) findViewById(R.id.edText);
            btReset = (Button) findViewById(R.id.btReset);
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

                //to enable user can reset image.
                btReset.setVisibility(View.VISIBLE);
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

    private boolean setPic() {
        int targetW = image.getMeasuredWidth();
        int targetH = image.getMeasuredHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);

        //in case of clear without taking photo
        if (bmOptions.outMimeType == null) return false;

        // Figure out which way needs to be reduced less
        int scaleFactor = calculateInSampleSize(bmOptions, targetW, targetH );
        Log.d(TAG, "scaleFactor:" + scaleFactor);

        // Set bitmap options to scale the image decode target
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

		// Decode the JPEG file into a Bitmap
        Bitmap myBitmap = BitmapFactory.decodeFile(output.getAbsolutePath(), bmOptions);

        //To fit to the width of imageview, scale
        Bitmap result = Bitmap.createScaledBitmap(myBitmap, targetW, targetH, false);
        image.setImageBitmap(myBitmap);
        return true;
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
        Create Temporary Image File before uploading
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
        When user select reset button, replace image with default icon
        and set output file pointer to null which is checked inside of takePhoto()
        whether call camera or not
     */

    public void resetContent(View view) {
        output = null;
        image.setImageResource(R.drawable.ic_photo_camera_white_48dp);
        edText.setText("");
    }

    /*
        When user select upload, it will upload to Firebase
        It will check whether text and image is not null
     */
    public void saveContent(View view) {
        //check activity is filled out
        if (TextUtils.isEmpty(edText.getText().toString())) {
            Toast.makeText(WriteMessageActivity.this, "Explain your activity.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //check activity photo is taken
        if (output == null) {
            Toast.makeText(WriteMessageActivity.this, "Activity photo is needed.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        btReset.setEnabled(false);
        btSave.setEnabled(false);

    }
}
