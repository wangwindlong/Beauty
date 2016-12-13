package com.dante.girls.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.ExecutionException;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yons on 16/2/17.
 */
public class Image extends RealmObject {
    public int id;
    public int type;//Gank or DB
    public String publishedAt;
    @PrimaryKey
    public String url;
    public int width;
    public int height;

    public Image(String url) {
        this.url = url;
    }

    public Image() {
    }

    public Image(String url, int type) {
        this.url = url;
        this.type = type;
    }

    public Image(String url, int type, int id) {
        this.url = url;
        this.type = type;
        this.id = id;
    }

    public static Image getFixedImage(Context context, Image image, int type, int page) throws ExecutionException, InterruptedException {
        Bitmap bitmap = getBitmap(context, image.url);
        image.setWidth(bitmap.getWidth());
        image.setHeight(bitmap.getHeight());
        image.setType(type);
        if (image.id == 0) {
            image.id = type + page + image.width + image.height;
        }
        return image;
    }

    public static Bitmap getBitmap(Context context, String url) throws InterruptedException, ExecutionException {
        return Glide.with(context).load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
