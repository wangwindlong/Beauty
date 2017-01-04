package com.dante.girls.picture;

import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.utils.UiUtils;

/**
 * A picture fragment for users' liked items.
 */

public class FavoriteFragment extends PictureFragment {

//    @Override
//    protected int initAdapterLayout() {
//        return R.layout.picture_item_fixed;
//    }

    @Override
    public void fetch() {
        adapter.notifyDataSetChanged();
        changeState(false);
    }

    @Override
    protected void AlwaysInit() {
        super.AlwaysInit();
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        p.setScrollFlags(0);
        toolbar.setLayoutParams(p);
    }

    @Override
    protected void initData() {
        imageType = Constants.FAVORITE;
        super.initData();

        if (images.isEmpty()) {
            UiUtils.showSnackLong(rootView, R.string.images_empty);
        }
        adapter.setEmptyView(LayoutInflater.from(context).inflate(R.layout.empty, (ViewGroup) rootView, false));
    }


}
