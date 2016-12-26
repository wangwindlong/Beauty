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
import com.dante.girls.utils.SPUtil;
import com.dante.girls.utils.UI;

import java.util.List;

import io.realm.Realm;
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
    private static final int BUFFER_SIZE = 2;
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
        log("isA " + isA, "::: isInpost " + isInPost);
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
                layout = R.layout.picture_item_fixed;
                if (!isInPost) {
                    layout = R.layout.post_item;
                }
        }
        return layout;
    }

    @Override
    public void fetch() {
        if (isFetching) {
            return;
        }
        if (page <= 1) {
            imageList = realm.copyFromRealm(images);
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
                        return Observable.from(images);
                    }
                })
                .map(new Func1<Image, Image>() {
                    @Override
                    public Image call(Image image) {
                        if (!isA) {
                            //不是A区，需要预加载
                            return Image.getFixedImage(context, image, imageType, page);
                        }
                        return image;
                    }
                })
                .buffer(BUFFER_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Image>>() {
                    int oldSize;
                    int newSize;

                    @Override
                    public void onStart() {
                        isFetching = true;
                        oldSize = images.size();
                        log("old size", oldSize);
                    }

                    @Override
                    public void onCompleted() {
                        images = DataBase.findImages(realm, imageType);
                        newSize = images.size();
                        int add = newSize - oldSize;
                        changeState(false);
                        adapter.loadMoreComplete();
//                        if (isA && !isInPost) {
//                            sortData(add);//每次刷新第一页的时候给图片排序
//                        }

                        if (add == 0) {
                            if (isInPost) adapter.loadMoreEnd(true);
                            Log.w(TAG, "onCompleted: old new size are the same");
                            log("newsize ", newSize);
                        } else {
                            //获取到数据了，下一页
                            SPUtil.save(imageType + Constants.PAGE, page);
                            adapter.notifyItemRangeChanged(oldSize, newSize);
                        }
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
                        if (imageList != null) imageList.addAll(0, list);
                        DataBase.save(realm, list);

                    }
                });
    }

    private void sortData(final int added) {
        if (imageList == null || imageList.size() == 0) {
            return;
        }
        if (page > 1) {
            return;
        }

        Log.i(TAG, "execute: before sort " + images.first().url);
        realm.executeTransactionAsync(new Realm.Transaction() {
            public void execute(Realm realm) {
                for (int i = 0; i < imageList.size(); i++) {
                    Image image = imageList.get(i);
                    Image data = realm.where(Image.class).equalTo(Constants.ID, image.id).findFirst();
                    if (data != null) {
                        data.setId(i);//id作为序号: 1, 2, 3 ...
                    }
                }
            }

        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                images.sort(Constants.ID);
                Log.i(TAG, "execute: after sort " + images.first().url);
                adapter.notifyItemRangeInserted(0, added);
                Log.i(TAG, "onSuccess: sortData " + added + " inserted");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInPost && images.size() > 8) {
            log("10 image ", images.get(7).title);
        }
    }

    @Override
    protected void AlwaysInit() {
        super.AlwaysInit();
        switch (baseType) {
            case TYPE_A_ANIME:
            case TYPE_A_FULI:
            case TYPE_A_HENTAI:
            case TYPE_A_ZATU:
            case TYPE_A_UNIFORM:
                isA = true;
                recyclerView.setBackgroundColor(getColor(R.color.cardview_dark_background));
        }

        //在A区帖子中，改变toolbar的样式
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (isInPost) {
            p.setScrollFlags(0);
            ((MainActivity) context).changeNavigator(false);

        } else {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
            ((MainActivity) context).changeNavigator(true);
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
                page = SPUtil.getInt(imageType + Constants.PAGE, 1);
                page++;
                fetch();
            }
        });
    }

}
