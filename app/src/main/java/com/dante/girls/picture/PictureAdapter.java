package com.dante.girls.picture;

import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.widget.TextView;

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
    }

    @Override
    protected void convert(BaseViewHolder holder, Image image) {
        RatioImageView imageView = holder.getView(R.id.picture);
        imageView.setOriginalSize(image.width, image.height);
        ViewCompat.setTransitionName(imageView, image.url);
        Imager.load(mContext, image.url, imageView);

        //for post item
        TextView title = holder.getView(R.id.title);
        Log.i(TAG, "convert:  ");
        if (title != null) {
            Log.i(TAG, "convert: title "+image.title);
            title.setText(image.title);
        }
    }

}
