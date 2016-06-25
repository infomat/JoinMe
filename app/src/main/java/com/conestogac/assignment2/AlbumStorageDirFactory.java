package com.conestogac.assignment2;

import java.io.File;

/**
 * Created by infomat on 16-06-25.
 * This is Abstract class which points to the class depends on SDK version
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}

