package com.dante.girl.lib;

import com.chad.library.adapter.base.loadmore.LoadMoreView;

/**
 * Created by yons on 17/12/7.
 */

public class MyLoadMoreView extends LoadMoreView {
    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    protected int getLoadingViewId() {
        return 0;
    }

    @Override
    protected int getLoadFailViewId() {
        return 0;
    }

    @Override
    protected int getLoadEndViewId() {
        return 0;
    }
}
