package com.dante.girls.picture;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DataBase;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.net.DataFetcher;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
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
 * Custom appearance of picture list fragment
 */

public class CustomPictureFragment extends PictureFragment {
    private static final String TAG = "CustomPictureFragment";
    private static final int BUFFER_SIZE = 2;
    boolean inAPost;
    boolean isA;
    private boolean firstPage;
    private int size;
    private int oldSize;
    private int error;

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
                if (firstPage) {
                    LOAD_COUNT = LOAD_COUNT_LARGE;
                }
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
                .flatMap(new Func1<List<Image>, Observable<Image>>() {
                    @Override
                    public Observable<Image> call(List<Image> images) {
                        return Observable.from(images);
                    }
                })
                .map(image -> {
                    if (!isA || inAPost) {
                        //不是A区，需要预加载
                        return Image.getFixedImage(context, image, imageType, page);
                    }
                    return image;
                })
                .doOnNext(image -> {
                    if (page <= 1) {
                        if (imageList.size() > 0) {
                            int firstId = imageList.get(0).id;
                            image.setId(firstId - 1);
                            imageList.add(0, image);
                        }
                    }
                })
                .compose(applySchedulers())
                .subscribe(new Subscriber<Image>() {

                    @Override
                    public void onStart() {
                        oldSize = images.size();
                        log("old size", oldSize);
                    }

                    @Override
                    public void onCompleted() {
                        changeState(false);
                        if (!adapter.isLoadMoreEnable()) {
                            adapter.setEnableLoadMore(true);
                        }
                        size = DataBase.findImages(realm, imageType).size();
                        int add = size - oldSize;
                        if (add == 0) {
                            log("onCompleted: old new size are the same");
                            if (inAPost) adapter.loadMoreEnd(true);
                            if (!firstPage) {
                                adapter.loadMoreFail();
                                page++;
                            }
                        } else {
                            firstFetch = false;
                            adapter.loadMoreComplete();
                        }
                        Log.i(TAG, "onCompleted: page " + page + ", size: " + size);
                        if (!firstPage) {
                            SpUtil.save(imageType + Constants.PAGE, page);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        changeState(false);
                        adapter.loadMoreFail();
                        log("onError" + page);
                        error++;
                        if (error > 3) {
                            UiUtils.showSnackLong(rootView, R.string.net_error);
                        } else {
                            UiUtils.showSnack(rootView, R.string.load_fail);
                        }
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Image list) {
                        DataBase.save(realm, list);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void sortData(final int added) {
        if (imageList.size() == 0) {
            return;
        }
        Log.i(TAG, "execute: before sort " + images.first().url);
        realm.executeTransactionAsync(realm -> {
            for (int i = 0; i < imageList.size(); i++) {
                Image image = imageList.get(i);
                Image data = realm.where(Image.class).equalTo(Constants.URL, image.url).findFirst();
                if (data != null) {
                    data.setId(i);//id作为序号: 1, 2, 3 ...
                    Log.i(TAG, "sortData: id: " + i + "   url:" + data.url);
                }
            }
        }, () -> {
            images.sort(Constants.ID);
            adapter.notifyItemRangeInserted(0, added);
            Log.i(TAG, "execute: after sort " + images.first().url);
            Log.i(TAG, "onSuccess: sortData " + added + " inserted");
            imageList = null;
        });
    }

    @Override
    protected void onCreateView() {
        super.onCreateView();
        if (isA) recyclerView.setBackgroundColor(getColor(R.color.cardview_dark_background));
        setupToolbar();
    }

    @Override
    public void onDestroyView() {
        if (adapter.isLoading()) {
            adapter.loadMoreComplete();
        }
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);//fragment被show或者hide时调用
        if (!hidden) {
            setupToolbar();
            ((MainActivity) context).changeDrawer(!inAPost);
        }
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
    protected void initData() {
        super.initData();
        ((MainActivity) context).changeDrawer(!inAPost);
        images.addChangeListener(new RealmChangeListener<RealmResults<Image>>() {
            int before;

            @Override
            public void onChange(RealmResults<Image> element) {
                if (before == 0) {
                    before = oldSize;
                }
                size = element.size();
                int add = size - before;
                if (!firstPage || firstFetch || inAPost || !isFetching) {
                    adapter.notifyItemRangeInserted(before, add);
                } else {
                    before = 0;//刷新首页，新数据插入到开头位置
                    adapter.notifyItemRangeInserted(before, add);
                    recyclerView.smoothScrollToPosition(0);
                }
                before = size;
            }
        });
        adapter.setOnLoadMoreListener(() -> {

            page = SpUtil.getInt(imageType + Constants.PAGE, 1);
            page++;
            log("load more ", page);
            fetch();
        });
        if (images.isEmpty()) {
            adapter.setEnableLoadMore(false);
            firstFetch = true;
            fetch();
            changeState(true);
        }
    }

}
