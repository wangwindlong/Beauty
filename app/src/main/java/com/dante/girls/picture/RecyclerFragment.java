package com.dante.girls.picture;


import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dante.girls.R;
import com.dante.girls.base.BaseFragment;
import com.dante.girls.base.Constants;
import com.dante.girls.utils.SPUtil;

import butterknife.BindView;
import rx.Subscription;

import static com.mikepenz.iconics.Iconics.TAG;

/**
 * All fragments have recyclerView & swipeRefresh must implement this.
 */
public abstract class RecyclerFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    boolean isFirst = true;   //whether is first time to enter fragment
    String imageType;               // imageType of recyclerView's content
    int lastPosition;       //last visible position
    int firstPosition;      //first visible position
    Subscription subscription;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_recycler;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            //restoring position when reentering fragment.
            lastPosition = SPUtil.getInt(imageType + Constants.POSITION);
            Log.i(TAG, "onViewStateRestored: type- "+ imageType +" lastPosition- "+ lastPosition);
            if (lastPosition > 0) {
                recyclerView.scrollToPosition(lastPosition);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: save type- "+ imageType +" position- " +firstPosition);
        SPUtil.save(imageType + Constants.POSITION, firstPosition);
    }

    @Override
    public void onDestroyView() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        Log.i(TAG, "onDestroyView: type- "+ imageType +" position- "+ firstPosition);
        super.onDestroyView();
    }

    @Override
    protected void initViews() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary),
                getColor(R.color.colorPrimaryDark), getColor(R.color.colorAccent));

        swipeRefresh.setOnRefreshListener(this);
    }

    public void changeRefresh(final boolean refreshState) {
        if (null != swipeRefresh) {
            swipeRefresh.setRefreshing(refreshState);
        }
    }

    public int getColor(int resId) {
        return ResourcesCompat.getColor(getResources(), resId, null);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


}
