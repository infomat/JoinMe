package com.conestogac.assignment2;

import java.io.File;

/**
 * This is Abstract class which points to the class depends on SDK version
 * AlbumStorageDirFactory, BaseAlbumDirFactory, FroyoAlbumDirFactory should used together
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}

