package com.dante.girl.picture;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dante.girl.MainActivity;
import com.dante.girl.R;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.net.API;
import com.dante.girl.net.DataFetcher;
import com.dante.girl.utils.SpUtil;
import com.dante.girl.utils.UiUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static com.dante.girl.net.API.TYPE_A_ANIME;
import static com.dante.girl.net.API.TYPE_A_FULI;
import static com.dante.girl.net.API.TYPE_A_HENTAI;
import static com.dante.girl.net.API.TYPE_A_UNIFORM;
import static com.dante.girl.net.API.TYPE_A_ZATU;
import static com.dante.girl.net.API.TYPE_DB_BREAST;
import static com.dante.girl.net.API.TYPE_DB_BUTT;
import static com.dante.girl.net.API.TYPE_DB_LEG;
import static com.dante.girl.net.API.TYPE_DB_RANK;
import static com.dante.girl.net.API.TYPE_DB_SILK;

/**
 * Custom appearance of picture list fragment
 */

public class CustomPictureFragment extends PictureFragment {
    private static final String TAG = "CustomPictureFragment";
    private static final int BUFFER_SIZE = 4;
    boolean inAPost;
    boolean isA;
    private boolean firstPage;
    private int size;
    private int old;
    private int error;
    private boolean isGank;
    private boolean isDB;

    public static CustomPictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        CustomPictureFragment fragment = new CustomPictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void addInfo(String info) {
        this.info = info;
        inAPost = true;
    }

    @Override
    protected void initViews() {
        super.initViews();
        imageType = TextUtils.isEmpty(info) ? baseType : info;
    }

    @Override
    protected void onImageClicked(View view, int position) {
        if (isA && !inAPost) {
            startPost(getImage(position));
            return;
        }
        super.onImageClicked(view, position);
    }

    @Override
    protected int initAdapterLayout() {
        int layout = R.layout.picture_item;
        switch (baseType) {
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                isA = true;
                layout = R.layout.post_item;
                if (inAPost) {
                    layout = R.layout.picture_item;
                }
        }
        return layout;
    }

    @Override
    public void fetch() {
        if (isFetching) {
            Log.e(TAG, "fetch: isFetching. return");
            return;
        }
        firstPage = page <= 1;

        DataFetcher fetcher;
        Observable<List<Image>> source;
        switch (baseType) {
            case TYPE_DB_BREAST:
            case TYPE_DB_BUTT:
            case TYPE_DB_LEG:
            case TYPE_DB_SILK:
            case TYPE_DB_RANK:
                isDB = true;
                url = API.DB_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getDouban();
                break;

            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                url = API.A_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = inAPost ? fetcher.getPicturesOfPost(info) : fetcher.getAPosts();
                break;

            default://imageType = 0, 代表GANK
                url = API.GANK;
                isGank = true;
                fetcher = new DataFetcher(url, imageType, page);
                source = fetcher.getGank();
                break;
        }
        fetchImages(source);
    }

    //预加载Image，然后刷新列表
    protected void fetchImages(final Observable<List<Image>> source) {
        subscription = source
                .observeOn(Schedulers.io())
                .filter(list -> list.size() > 0)
                .flatMap(Observable::from)
                .map(image -> {
                    if (!isA || inAPost) {
                        //不是A区，需要预加载
                        try {
                            image = Image.getFixedImage(this, image, imageType);
                            if (!isGank && !DataBase.hasImage(image.url)) {
                                Date date = new Date();
                                if (page <= 1) {
                                    //如果是刷新，则把新图片的日期都往前设10s
                                    Image i = new Image();
                                    i.setPublishedAt(date);
                                    RealmResults<Image> images = DataBase.findImages(null, imageType);
                                    Date d = images.first(i).getPublishedAt();
                                    if (d != null) {
                                        d.setTime(d.getTime() - 10000);
                                    }
                                    date = d;
                                }
                                image.setPublishedAt(date);
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            Log.d(TAG, "fetchImages: " + e.getMessage());
                        }
                        if (inAPost) {
                            image.big = true;
                        }
                    }
                    return image;
                })
                .buffer(BUFFER_SIZE)
                .compose(applySchedulers())
                .subscribe(new Subscriber<List<Image>>() {
                    int oldSize;
                    @Override
                    public void onStart() {
                        oldSize = images.size();
                    }

                    @Override
                    public void onCompleted() {
                        changeState(false);
                        adapter.loadMoreComplete();
                        images = DataBase.findImages(realm, imageType);
                        int add = images.size() - oldSize;
                        log("onCompleted add" + add);
                        if (add > 0) {
                            if (page == 1) {
                                adapter.setNewData(images);
                            } else {
                                adapter.notifyItemRangeInserted(oldSize, add);
                            }
                        } else {
                            if (page > 1 && inAPost) adapter.loadMoreEnd();
                        }
                        page++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        error++;
                        if (page > 1 && images.size() > 3 && inAPost) {
                            adapter.loadMoreEnd(true);
                        } else {
                            adapter.loadMoreFail();
                            if (error > 2) {
                                UiUtils.showSnackLong(rootView, R.string.net_error);
                            } else {
                                UiUtils.showSnack(rootView, R.string.load_fail);
                            }
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Image> list) {
                        DataBase.save(realm, list);
                    }
                });
        compositeSubscription.add(subscription);
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        if (isA) recyclerView.setBackgroundColor(getColor(R.color.black_back));
    }

//    @Override
//    public void onDestroyView() {
//        if (adapter.isLoading()) {
//            adapter.loadMoreComplete();
//        }
//        super.onDestroyView();
//    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);//fragment被show或者hide时调用
        if (!hidden && context != null) {
            setupToolbar();
            ((MainActivity) context).changeDrawer(!inAPost);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) context).changeDrawer(!inAPost);
    }

    private void setupToolbar() {
        //在A区帖子中，改变toolbar的样式
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (inAPost) {
            p.setScrollFlags(0);
            ((MainActivity) context).changeNavigator(false);
            context.setToolbarTitle(title);
        } else {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            ((MainActivity) context).changeNavigator(true);
            context.setToolbarTitle(((MainActivity) context).getCurrentMenuTitle());
        }
        toolbar.setLayoutParams(p);
    }

    private void startPost(Image image) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        CustomPictureFragment fragment = CustomPictureFragment.newInstance(imageType);
        fragment.addInfo(image.info);//帖子地址，也是imageType
        fragment.setTitle(image.title);//用于toolbar的标题
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.hide(this);
        transaction.add(R.id.container, fragment, "aPost");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        SpUtil.save(imageType + Constants.PAGE, page);
    }

    @Override
    protected void initData() {
        super.initData();
        setupToolbar();
        page = SpUtil.getInt(imageType + Constants.PAGE, 1);
        adapter.setOnLoadMoreListener(() -> {
            if (isFetching) {
                UiUtils.showSnack(rootView, R.string.is_loading);
                return;
            }
            fetch();
            changeState(true);
        }, recyclerView);
        adapter.disableLoadMoreIfNotFullPage(recyclerView);
        if (images.isEmpty()) {
            fetch();
            changeState(true);
        }
    }

//    @Override
//    public void onChange(RealmResults<Image> collection, OrderedCollectionChangeSet changeSet) {
//        // `null`  means the async query returns the first time.
//        if (isDB) {
//            return;
//        }
//        if (changeSet == null) {
//            adapter.notifyDataSetChanged();
//            return;
//        }
//        // For deletions, the adapter has to be notified in reverse order.
//        OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
//        for (int i = deletions.length - 1; i >= 0; i--) {
//            OrderedCollectionChangeSet.Range range = deletions[i];
//            adapter.notifyItemRangeRemoved(range.startIndex, range.length);
//        }
//
//        OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
//        for (OrderedCollectionChangeSet.Range range : insertions) {
//            adapter.notifyItemRangeInserted(range.startIndex, range.length);
//            if (page == 1) {
//                recyclerView.scrollToPosition(0);
//            }
//        }
//
////        OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
////        for (OrderedCollectionChangeSet.Range range : modifications) {
////            log("no notifyItemRangeChanged " + range.startIndex + " to " + range.length);
////            adapter.notifyItemRangeChanged(range.startIndex, range.length);
////        }
//    }
}
