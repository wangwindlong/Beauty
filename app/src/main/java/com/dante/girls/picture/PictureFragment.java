package com.dante.girls.picture;

import android.text.TextUtils;
import android.util.Log;

import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.model.Image;
import com.dante.girls.net.API;
import com.dante.girls.net.DataFetcher;

import java.util.List;

import rx.Observable;

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
 * Created by yons on 16/12/19.
 */

public class PictureFragment extends BasePictureFragment {


    @Override
    public int initAdapterLayout() {
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

    @Override
    protected void AlwaysInit() {
        super.AlwaysInit();
        if (TextUtils.isEmpty(title)) {
            title = ((MainActivity) context).getCurrentMenu().getTitle().toString();
        }
        Log.i("test", "setTitle: " + title);
        context.setToolbarTitle(title);
    }
}
