package com.conestogac.assignment2;

import android.os.Environment;

import java.io.File;

/**
 * This class extends Abtract AlbumStorageDirFactory to support old version's DCIM folder
 * AlbumStorageDirFactory, BaseAlbumDirFactory, FroyoAlbumDirFactory should used together
 */
public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

    // Standard storage location for digital camera files
    private static final String CAMERA_DIR = "/DCIM/Camera/";

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()
                        + CAMERA_DIR
                        + albumName
        );
    }
}