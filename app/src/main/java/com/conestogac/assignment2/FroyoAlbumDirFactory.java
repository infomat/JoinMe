package com.conestogac.assignment2;

import android.os.Environment;

import java.io.File;

/**
 * This class extends Abtract AlbumSTorageDirFactory
 * which point to PICTURES folder
 * AlbumStorageDirFactory, BaseAlbumDirFactory, FroyoAlbumDirFactory should used together
 */
public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

    @Override
    public File getAlbumStorageDir(String albumName) {

        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}

