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

/**
 * Gank and DB beauty fragment.
 */
public abstract class BasePictureFragment extends RecyclerFragment {
    public static final int LOAD_COUNT_LARGE = 12;
    private static final String TAG = "PictureFragment";
    private static final int REQUEST_VIEW = 1;
    public static int LOAD_COUNT = 8;
    private static int PRELOAD_COUNT = 10;

    String url;
    boolean isFetching;
    boolean isInPost;
    String title;
    boolean isA;
    int page = 1;
    String info;//附加信息，这里暂时用于type为A区时的帖子地址
    BaseActivity context;
    String baseType;
    private StaggeredGridLayoutManager layoutManager;
    private PictureAdapter adapter;
    private RealmResults<Image> images;

    public static PictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        PictureFragment fragment = new PictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setInfo(String info) {
        this.info = info;
        isInPost = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        lastPosition = SPUtil.getInt(imageType + Constants.POSITION);
        Log.i(TAG, "onResume: type- " + imageType + " position- " + lastPosition);

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
        //baseType is for base url
        baseType = getArguments().getString(Constants.TYPE);
        context = (BaseActivity) getActivity();
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PictureAdapter(initAdapterLayout());
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (isA && !isInPost) {
                    startPost(getImage(i));
                    return;
                }
                startViewer(view, i);

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 50) {
                    return;
                }
                int[] spans = new int[layoutManager.getSpanCount()];
                firstPosition = layoutManager.findFirstVisibleItemPositions(spans)[0];
                lastPosition = layoutManager.findLastVisibleItemPositions(spans)[1];
            }
        });
    }

    public abstract int initAdapterLayout();

    private void startPost(Image image) {
        Log.i(TAG, "startPost: " + image.info);
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        PictureFragment fragment = PictureFragment.newInstance(imageType);
        fragment.setInfo(image.info);
        fragment.setTitle(image.title);
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.container, fragment, "aPost");
        transaction.addToBackStack("");
        transaction.commit();
    }

    private void startViewer(View view, int position) {
        Intent intent = new Intent(context.getApplicationContext(), ViewerActivity.class);
        intent.putExtra(Constants.TYPE, imageType);
        intent.putExtra(Constants.POSITION, position);

        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(context, view, adapter.getData().get(position).url);
        ActivityCompat.startActivityForResult(context, intent, REQUEST_VIEW, options.toBundle());
    }

    public abstract void fetch();//确定type，base url和解析数据

    protected void fetchImages(final Observable<List<Image>> observable) {
        subscription = observable
                .flatMap(new Func1<List<Image>, Observable<Image>>() {
                    @Override
                    public Observable<Image> call(List<Image> images) {
                        if (images==null){
                            subscription.unsubscribe();
                        }
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
        setTitle("");
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        imageType = baseType;
        Log.i(TAG, "AlwaysInit: imageType" + imageType);
        if (!TextUtils.isEmpty(info)) {
            imageType = info;
//            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        }

        page = SPUtil.getInt(imageType + Constants.PAGE, 1);
    }

    protected void setTitle(String title) {
        this.title = title;
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
//            fetch();
//            changeState(true);
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

    public Image getImage(int position) {
        return adapter.getData().get(position);
    }

}
