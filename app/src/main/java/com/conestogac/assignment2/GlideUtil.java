package com.conestogac.assignment2;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/*
    It is Util class to use Glide opensource which helps caching or decoding remote located images
    moreover it is east to use.

    http://google-opensource.blogspot.ca/2014/09/glide-30-media-management-library-for.html
 */
public class GlideUtil {
    public static void loadImage(String url, ImageView imageView) {
        Context context = imageView.getContext();
        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, R.color.blue_grey_500));
        Glide.with(context)
                .load(url)
                .placeholder(cd)
                .crossFade()
                .centerCrop()
                .into(imageView);
    }

    public static void loadProfileIcon(String url, ImageView imageView) {
        Context context = imageView.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_broken_image_white_24dp)
                .dontAnimate()
                .fitCenter()
                .into(imageView);
    }
}