package com.dante.girl.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.girl.R;
import com.dante.girl.base.App;
import com.dante.girl.model.Image;
import com.dante.girl.net.NetService;

/**
 * loading img encapsulation.
 */
public class Imager {


    public static void loadDefer(final Fragment context, Image image, SimpleTarget<Bitmap> target) {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context)
                .load(glideUrl)
                .asBitmap()
                .error(R.drawable.placeholder)
                .crossFade()
                .into(target);
    }

    public static void load(final Fragment context, String url, ImageView target, RequestListener<String, Bitmap> listener) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .error(R.drawable.error_holder)
                .animate(R.anim.fade_in)
                .listener(listener)
                .into(target);
    }

    public static void loadWithHeader(final Fragment context, Image image, ImageView target, RequestListener<GlideUrl, Bitmap> listener) {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());

        Glide.with(context)
                .load(glideUrl)
                .asBitmap()
//                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_holder)
                .animate(R.anim.fade_in)
                .listener(listener)
                .into(target);
    }

    public static void loadWithHeader(final Context context, Image image, ImageView target) {
        GlideUrl glideUrl = new GlideUrl(image.url, new LazyHeaders.Builder()
                .addHeader("Referer", image.referer)
                .addHeader("User-Agent", NetService.AGENT).build());

        Glide.with(context)
                .load(glideUrl)
                .error(R.drawable.error_holder)
                .into(target);
    }


    public static void load(final Fragment context, String url, ImageView target) {
        Glide.with(context)
                .load(url)
                .crossFade()
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
