package com.dante.girls.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.girls.R;
import com.dante.girls.base.App;

/**
 * loading img encapsulation.
 */
public class Imager {

    public static void load(Context context, String url, ImageView view) {
        Glide.with(context)
                .load(url)
//                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade()
                .into(view);
    }

    public static void loadDefer(final Fragment context, String url, SimpleTarget<Bitmap> target) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(target);
    }

    public static void load(final Fragment context, String url, ImageView target, RequestListener<String, Bitmap> listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .thumbnail(0.5f)
                .animate(R.anim.fade_in)
                .listener(listener)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(target);
    }

    public static void load(Context context, int resourceId, ImageView view) {
        Glide.with(context)
                .load(resourceId)
                .crossFade()
                .into(view);
    }


    public static void load(String url, int animationId, ImageView view) {
        Glide.with(App.context)
                .load(url)
                .animate(animationId)
                .into(view);
    }

}
