package com.dante.girls.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

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

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.dante.girls.net.API.TYPE_A_ANIME;
import static com.dante.girls.net.API.TYPE_A_FULI;
import static com.dante.girls.net.API.TYPE_A_HENTAI;
import static com.dante.girls.net.API.TYPE_A_UNIFORM;
import static com.dante.girls.net.API.TYPE_A_ZATU;
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
    private static final String TAG = "PictureFragment";
    private static final int REQUEST_VIEW = 1;
    public static int LOAD_COUNT = 8;
    private static int PRELOAD_COUNT = 10;

    private String url;
    private int page = 1;
    private StaggeredGridLayoutManager layoutManager;
    private PictureAdapter adapter;
    private BaseActivity context;
    private RealmResults<Image> images;
    private boolean isFetching;
    private boolean isInPost;
    private boolean isA;
    private String info;//附加信息，这里暂时用于type为A区时的帖子地址
    private String imageType;

    public static PictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        PictureFragment fragment = new PictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setInfo(String info) {
        this.info = info;
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
        SPUtil.save(imageType + Constants.PAGE, page);
        super.onDestroyView();
    }

    @Override
    protected void initViews() {
        super.initViews();
        type = getArguments().getString(Constants.TYPE);
        context = (BaseActivity) getActivity();
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        int layout = R.layout.picture_item;
        switch (type) {
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                isA = true;
                if (isInPost) return;
                layout = R.layout.post_item;
                Log.i(TAG, "initViews: post layout");
        }
        adapter = new PictureAdapter(layout);
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (isA && !isInPost) {
                    startPost(getData(i).info);
                    return;
                }
                startViewer(view, i);

            }
        });
    }

    private void startPost(String postInfo) {
        Log.i(TAG, "startPost: " + postInfo);
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        PictureFragment fragment = PictureFragment.newInstance(type);
        fragment.setInfo(postInfo);
        transaction.replace(R.id.container, fragment, "aPost");
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_out_right, android.R.anim.slide_in_left);
        transaction.addToBackStack("");
        transaction.commit();
    }

    private void startViewer(View view, int position) {
        Intent intent = new Intent(context, ViewerActivity.class);
        intent.putExtra(Constants.TYPE, imageType);
        intent.putExtra(Constants.POSITION, position);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context, view, adapter.getData().get(position).url);
        ActivityCompat.startActivityForResult(context, intent, REQUEST_VIEW, options.toBundle());
    }


    private void fetch() {
        if (isFetching) {
            return;
        }

        Observable<List<Image>> source;
        DataFetcher fetcher;
        switch (type) {
            case TYPE_DB_BREAST:
            case TYPE_DB_BUTT:
            case TYPE_DB_LEG:
            case TYPE_DB_SILK:
            case TYPE_DB_RANK:
                url = API.DB_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getDouban();
                break;
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                isA = true;
                url = API.A_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = isInPost ? fetcher.getPicturesOfPost(info) : fetcher.getAPosts();
                break;
            default://type = 0, 代表GANK
                url = API.GANK;
                if (page <= 1) {
                    LOAD_COUNT = LOAD_COUNT_LARGE;
                }
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getGank();
                break;
        }

        fetchImages(source);
    }

    private void fetchImages(final Observable<List<Image>> observable) {
        if (observable == null) return;

        subscription = observable
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
                            return Image.getFixedImage(context, image, imageType, page);
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
                    int newSize;

                    @Override
                    public void onStart() {
                        isFetching = true;
                        oldSize = images.size();
                        Log.i(TAG, "onStart: oldSize " + oldSize);
                    }

                    @Override
                    public void onCompleted() {
                        newSize = images.size();
                        page++;
                        changeState(false);
                        adapter.loadMoreComplete();
                        if (oldSize == newSize) {
                            adapter.loadMoreEnd(true);
                        }
                        Log.i(TAG, "onCompleted: newSize " + newSize);
                        adapter.notifyItemRangeChanged(oldSize, images.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        adapter.loadMoreComplete();
                        UI.showSnack(rootView, R.string.load_fail);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Image image) {
                        DB.save(context.realm, image);
                        Log.i(TAG, "onStart: " + image.url);
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
        imageType = type;
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        if (!TextUtils.isEmpty(info)) {
            isInPost = true;
            imageType = info;
            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        }

        page = SPUtil.getInt(imageType + Constants.PAGE, 1);
    }

    @Override
    protected void initData() {
        Log.i(TAG, "imageType: " + imageType);
        initFab();
        images = DB.getImages(context.realm, imageType);
        adapter.setNewData(images);
        if (images.isEmpty()) {
            page = 1;
            fetch();
            changeState(true);
        } else if (!isInPost) {
            fetch();
            changeState(true);
        }

        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                fetch();
            }
        });

    }

    private void initFab() {
        final FloatingActionButton button = ((MainActivity) getActivity()).fab;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (button.isShown()) {
                    if (dy > 20) {
                        button.hide();
                    }
                } else {
                    if (dy < -20) {
                        button.show();
                    }
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        fetch();
    }

    public Image getData(int position) {
        return adapter.getData().get(position);
    }

}
