package com.dante.girls.picture;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.base.Constants;
import com.dante.girls.model.DataBase;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.net.DataFetcher;
import com.dante.girls.utils.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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
    boolean isInPost;
    boolean isA;

    public static CustomPictureFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(Constants.TYPE, type);
        CustomPictureFragment fragment = new CustomPictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setInfo(String info) {
        this.info = info;
        isInPost = true;
    }

    @Override
    protected void initViews() {
        super.initViews();
        imageType = TextUtils.isEmpty(info) ? baseType : info;
    }

    @Override
    protected void onImageClicked(View view, int position) {
        if (isA && !isInPost) {
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
                if (isInPost) return layout;
                layout = R.layout.post_item;
        }
        return layout;
    }

    @Override
    public void fetch() {
        if (isFetching) {
            return;
        }

        Observable<List<Image>> source;
        DataFetcher fetcher;
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
                isA = true;
                url = API.A_BASE;
                fetcher = new DataFetcher(url, imageType, page);
                source = isInPost ? fetcher.getPicturesOfPost(info) : fetcher.getAPosts();
                break;

            default://imageType = 0, 代表GANK
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

    //预加载Image，然后刷新列表
    protected void fetchImages(final Observable<List<Image>> source) {
        subscription = source
                .flatMap(new Func1<List<Image>, Observable<Image>>() {
                    @Override
                    public Observable<Image> call(List<Image> images) {
                        if (images == null) {
                            if (isInPost) adapter.loadMoreEnd(true);
                            subscription.unsubscribe();
                            images = new ArrayList<>();
                        }
                        return Observable.from(images);
                    }
                })
                .map(new Func1<Image, Image>() {
                    @Override
                    public Image call(Image image) {
                        if (image != null) {
                            try {
                                return Image.getFixedImage(context, image, imageType, page);
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return image;
                    }
                })
                .buffer(2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Image>>() {
                    int oldSize;
                    int newSize;

                    @Override
                    public void onStart() {
                        isFetching = true;
                        oldSize = images.size();
                    }

                    @Override
                    public void onCompleted() {
                        newSize = images.size();
                        changeState(false);
                        adapter.loadMoreComplete();
                        if (oldSize == newSize) {
                            if (isInPost) adapter.loadMoreEnd(true);
                            Log.e(TAG, "onCompleted: old new size are the same, means we got duplicate data.");
                        } else {
                            //获取到数据了，下一页
                            page++;
                        }
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
                    public void onNext(List<Image> list) {
                        DataBase.save(realm, list);
                    }
                });
    }

    @Override
    protected void AlwaysInit() {
        super.AlwaysInit();

        //在A区帖子中，改变toolbar的样式
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (isInPost) {
            p.setScrollFlags(0);
            ((MainActivity) context).toggle.setDrawerIndicatorEnabled(false);
            ((MainActivity) context).toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.onBackPressed();
                }
            });
        } else {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            ((MainActivity) context).toggle.setDrawerIndicatorEnabled(true);
        }
        toolbar.setLayoutParams(p);
    }


    private void startPost(Image image) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        CustomPictureFragment fragment = CustomPictureFragment.newInstance(imageType);
        fragment.setInfo(image.info);//帖子地址，也是imageType
        fragment.setTitle(image.title);//用于toolbar的标题
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right
                , android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.container, fragment, "aPost");
        transaction.addToBackStack("");
        transaction.commit();
    }

    @Override
    protected void initData() {
        super.initData();
        if (images.isEmpty()) {
            page = 1;
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

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.i(TAG, "onViewStateRestored: ");
        adapter.loadMoreComplete();
    }
}
