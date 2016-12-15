package com.dante.girls.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DB;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.net.DataFetcher;
import com.dante.girls.utils.SPUtil;
import com.dante.girls.utils.UI;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.dante.girls.net.API.TYPE_DB_BREAST;
import static com.dante.girls.net.API.TYPE_DB_BUTT;
import static com.dante.girls.net.API.TYPE_DB_LEG;
import static com.dante.girls.net.API.TYPE_DB_RANK;
import static com.dante.girls.net.API.TYPE_DB_SILK;

/**
 * Gank and DB beauty fragment.
 */
public class PictureFragment extends RecyclerFragment {

    public static final int LOAD_COUNT_LARGE = 12;
    private static final int REQUEST_VIEW = 1;
    public static int LOAD_COUNT = 8;
    private static int PRELOAD_COUNT = 10;

    private String url;
    private int page = 1;
    private StaggeredGridLayoutManager layoutManager;
    private PictureAdapter adapter;
    private BaseActivity context;
    private RealmResults<Image> datas;
    private boolean isFetching;

    public static PictureFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(Constants.TYPE, type);
        PictureFragment fragment = new PictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        lastPosition = SPUtil.getInt(type + Constants.POSITION);
        recyclerView.scrollToPosition(lastPosition > 0 ? lastPosition : 0);
    }

    @Override
    public void onPause() {
        firstPosition = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()])[0];
        super.onPause();
        changeRefresh(false);
    }

    @Override
    public void onDestroyView() {
        SPUtil.save(type + Constants.PAGE, page);
        super.onDestroyView();
    }

    @Override
    protected void initViews() {
        super.initViews();
        context = (BaseActivity) getActivity();
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PictureAdapter();
        adapter.setLoadingView(LayoutInflater.from(context).inflate(R.layout.empty, (ViewGroup) rootView, false));
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void SimpleOnItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                startViewer(view, i);
            }
        });
        type = getArguments().getInt(Constants.TYPE);
        page = SPUtil.getInt(type + Constants.PAGE);
        page = page == 0 ? 1 : page;
    }

    private void startViewer(View view, int position) {
        Intent intent = new Intent(context, ViewerActivity.class);
        intent.putExtra(Constants.TYPE, type);
        intent.putExtra(Constants.POSITION, position);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context, view, adapter.getData().get(position).url);
        ActivityCompat.startActivityForResult(context, intent, REQUEST_VIEW, options.toBundle());
    }


    private void fetch(boolean fresh) {
        if (isFetching) {
            return;
        }
        if (fresh) {
            page = 1;
        }
        switch (type) {
            case TYPE_DB_BREAST:
            case TYPE_DB_BUTT:
            case TYPE_DB_LEG:
            case TYPE_DB_SILK:
            case TYPE_DB_RANK:
                url = API.DB_BASE;
                break;
            default://type = 0, 代表GANK
                url = API.GANK;
                if (fresh) {
                    LOAD_COUNT = LOAD_COUNT_LARGE;
                }
                break;
        }
        DataFetcher fetcher = new DataFetcher(url, type, page);
        Observable<List<Image>> source = (type == 0) ? fetcher.getGankObservable() : fetcher.getDBObservable();
        fetchImages(source);

    }

    private void fetchImages(final Observable<List<Image>> observable) {
        subscription =observable
                .flatMap(new Func1<List<Image>, Observable<Image>>() {
                    @Override
                    public Observable<Image> call(List<Image> images) {
                        return Observable.from(images);
                    }
                })
                .map(new Func1<Image, Image>() {
                    @Override
                    public Image call(Image image) {
                        try {
                            return Image.getFixedImage(context, image, type, page);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Image>() {
                    int oldSize;

                    @Override
                    public void onStart() {
                        changeState(true);
                        oldSize = datas.size();
                    }

                    @Override
                    public void onCompleted() {
                        page++;
                        changeState(false);
                        adapter.hiedLoadingMore();
                        adapter.notifyItemRangeChanged(oldSize  , datas.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        UI.showSnack(rootView, R.string.load_fail);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Image image) {
                        DB.save(context.realm, image);
                    }
                });
    }

    private void changeState(boolean fetching) {
        isFetching = fetching;
        changeRefresh(isFetching);
    }

    @Override
    protected void AlwaysInit() {
        super.AlwaysInit();
        Log.i(TAG, "AlwaysInit: ");
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (datas == null) {
                    fetch(true);
                } else {
                    fetch(datas.isEmpty());
                }
                Log.i(TAG, "onLoadMoreRequested: " + page);
            }
        });
    }

    @Override
    protected void initData() {
        datas = DB.getImages(context.realm, type);
        adapter.setNewData(datas);
        initFab();
    }

    private void initFab() {
        final FloatingActionButton button = ((MainActivity) getActivity()).fab;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (button.isShown()) {
                    if (dy > 0) {
                        button.hide();
                    }
                } else {
                    if (dy < 0) {
                        button.show();
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        fetch(true);
    }

    public Image getData(int position) {
        return adapter.getData().get(position);
    }

}
