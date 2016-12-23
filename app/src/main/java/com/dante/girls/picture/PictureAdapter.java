package com.dante.girls.picture;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.girls.R;
import com.dante.girls.lib.RatioImageView;
import com.dante.girls.model.Image;
import com.dante.girls.utils.Imager;

/**
 * Adapter of picture list.
 */

class PictureAdapter extends BaseQuickAdapter<Image, BaseViewHolder> {

    PictureAdapter(int layoutId) {
        super(layoutId, null);
//        setHasStableIds(true);
    }

    @Override
    protected void convert(final BaseViewHolder holder, final Image image) {
        final ImageView imageView = holder.getView(R.id.picture);
        if (imageView instanceof RatioImageView) {
            ((RatioImageView) imageView).setOriginalSize(image.width, image.height);
        }
        ViewCompat.setTransitionName(imageView, image.url);
        final TextView title = holder.getView(R.id.title);
        final View post = holder.getView(R.id.post);
        if (title != null) {
            title.setText("");
        }
        Glide.with(mContext)
                .load(image.url)
                .priority(Priority.HIGH)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Imager.load(mContext, image.url, imageView);
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        //for post item
                        if (title != null) {
                            title.setText(image.title);
                            post.setVisibility(View.VISIBLE);
                        }
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);

    }

}
