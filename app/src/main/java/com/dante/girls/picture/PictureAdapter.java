package com.dante.girls.picture;

import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position) + 87;
    }

    @Override
    protected void convert(final BaseViewHolder holder, final Image image) {
        final ImageView imageView = holder.getView(R.id.picture);
        if (imageView instanceof RatioImageView && image.width != 0) {
            ((RatioImageView) imageView).setOriginalSize(image.width, image.height);
        }
        ViewCompat.setTransitionName(imageView, image.url);

        final View post = holder.getView(R.id.post);
        final TextView title = holder.getView(R.id.title);
        if (post != null) {
            String text = image.title
                    .replace("A区：", "")
                    .replace("APIC.IN", "")
                    .replace("-A区", "")
                    .replace("APIC-IN", "")
                    .replace("下载", "")
                    .replace("动漫", "")
                    .replace("壁纸", "")
                    .replace("图片", "")
                    .trim();
            title.setText(text);
            title.setSelected(true);
        }
        final long start = System.currentTimeMillis();
        Log.i(TAG, "load image-----------------");
        Imager.load(mContext, image.url, imageView, new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                final long loaded = System.currentTimeMillis();
                Log.i(TAG, "load image OK, duration: " + (loaded - start));

                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int color = palette.getDarkMutedColor(ContextCompat.getColor(mContext, R.color.cardview_dark_background));
                        if (post != null) {
                            title.setBackgroundColor(color);
                            title.setVisibility(View.VISIBLE);
                            Log.i(TAG, "load image Palette generated: " + (System.currentTimeMillis() - loaded));
                        }
                    }
                });
                return false;
            }
        });

    }

}
