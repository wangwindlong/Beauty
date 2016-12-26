package com.dante.girls.picture;

import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DataBase;
import com.dante.girls.utils.UI;

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
        images = DataBase.findFavoriteImages(realm);
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        p.setScrollFlags(0);
        toolbar.setLayoutParams(p);
    }

    @Override
    protected void initData() {
        super.initData();
        imageType = Constants.FAVORITE;
        if (images.isEmpty()) {
            UI.showSnackLong(rootView, R.string.images_empty);
        }
        adapter.setEmptyView(LayoutInflater.from(context).inflate(R.layout.empty, (ViewGroup) rootView, false));
    }


}
