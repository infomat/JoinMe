package com.conestogac.assignment2;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteMessageActivity extends AppCompatActivity {
    private static final String TAG = WriteMessageActivity.class.getSimpleName();

    public  static String tmpFileName = "activity";
    public  static String mCurrentPhotoPath;

    private static final int REQUEST_EXTERNAL_STORAGE_CAM = 0;
    private static final int REQUEST_EXTERNAL_STORAGE_IMG = 1;
    private static final int REQUEST_EXTERNAL_STORAGE_GAL = 2;
    private static String[] PERMISSIONS_EXT_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_IMAGE = 2;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private File mAlbumStorageDir = new ;
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private static final String EXTRA_FILENME = "com.conestogac.assignment2.EXTRA_FILENAME";
    private static final String FILENAME = "ActivityPhoto.jpg";
    private static final int CONTENT_REQUEST = 1337;
    private File output = null;
    private Intent intent;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_message);

        image = (ImageView) findViewById(R.id.ivPhoto) ;

        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (savedInstanceState==null) {
            //using DCIM folder name create folder
            File dir =
                    Environment.getExternalStorageDirectory();

            dir.mkdirs();
            output = new File(dir, FILENAME);

        } else {
            //when come back from background, restore output file pointer
            output = (File)savedInstanceState.getSerializable(EXTRA_FILENME);
        }

        if (output.exists()) {
            //output.delete();
            image.setImageURI(Uri.fromFile(output));
        }
    }

    public void onClick(View view) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(intent, CONTENT_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //In case of this app goes background, save file  pointer
        outState.putSerializable(EXTRA_FILENME, output);
    }

    //After taking picture, it will be caleed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap mImageBitmap = (Bitmap) extras.get("data");
                image.setImageBitmap(mImageBitmap);
            }
        }
    }

    public void takePhoto(View view)
    {
        isStoragePermissionGranted(REQUEST_EXTERNAL_STORAGE_CAM);
    }

    private  void isStoragePermissionGranted(int REQ_CODE) {
        //From SDK 23, For special permission, user can remove permission after installation
        //So it is needed to check permission dynamically
        if (Build.VERSION.SDK_INT >= 23) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is already granted");
                if (REQ_CODE == REQUEST_EXTERNAL_STORAGE_CAM) {
                    dispatchTakePictureIntent();
                } else if (REQ_CODE == REQUEST_EXTERNAL_STORAGE_IMG){
                    SaveImage();
                } else if (REQ_CODE == REQUEST_EXTERNAL_STORAGE_GAL){
                    setIntentGallery();
                }
            } else {
                //If permission is no granted, ask user to permit
                Log.v(TAG, "Show Rationale");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "Displaying external storage permission rationale to provide additional context.");
                    ActivityCompat.requestPermissions(this,PERMISSIONS_EXT_STORAGE , REQ_CODE);
                } else {
                    Log.v(TAG, "Permission is revoked");
                    ActivityCompat.requestPermissions(this,PERMISSIONS_EXT_STORAGE , REQ_CODE);
                }
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted due to SDK");
        }
    }

    //Taking picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Log.i(TAG, "Now, REQUEST_EXTERNAL_STORAGE is granted ");

        File f;
        try {
            if (mCurrentPhotoPath == null) {
                Log.d(TAG, "Create New Temp File");
                f = createImageFile();
            } else {
                Log.d(TAG, "Reuse Temp File");
                f = new File(mCurrentPhotoPath);
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            Log.d(TAG, "dispatchTakePictureIntent");
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
            Log.e(TAG, "dispatchTakePictureIntent something wrong!!");
        }
        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
    }

    private File createImageFile() throws IOException {

        String imageFileName = JPEG_FILE_PREFIX + tmpFileName;
        Log.d(TAG, "createImageFile: " + imageFileName);

        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumDir());
        mCurrentPhotoPath = imageF.getAbsolutePath();
        return imageF;
    }

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
}
