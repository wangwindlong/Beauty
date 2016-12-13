package com.dante.girls.net;

import com.dante.girls.model.Image;
import com.dante.girls.picture.PictureFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.dante.girls.net.API.TYPE_DB_RANK;

/**
 * Help to get&fetch image resources from api
 */

public class DataFetcher {
    private final NetService netService;
    public String url; //source url
    public int type; //picture type
    public int page;

    public DataFetcher(String url, int type, int page) {
        this.url = url;
        this.type = type;
        this.page = page;
        netService = NetService.getInstance(url);

    }

    public Observable<List<Image>> getGankObservable() {
        return netService.getGankApi().get(PictureFragment.LOAD_COUNT, page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .filter(new Func1<GankApi.Result<List<Image>>, Boolean>() {
                    @Override
                    public Boolean call(GankApi.Result<List<Image>> listResult) {
                        return !listResult.error;
                    }
                })
                .map(new Func1<GankApi.Result<List<Image>>, List<Image>>() {
                    @Override
                    public List<Image> call(GankApi.Result<List<Image>> listResult) {
                        return listResult.results;
                    }
                });
    }

    public Observable<List<Image>> getDBObservable() {
        Observable<ResponseBody> data;
        if (type == TYPE_DB_RANK) {
            data = netService.getDbApi().getRank(page);
        } else {
            data = netService.getDbApi().get(type, page);
        }
        return data
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
                            Elements elements = document.select("div[class=thumbnail] > div[class=img_single] > a > img");
                            final int size = elements.size();
                            for (int i = 0; i < size; i++) {
                                String url = elements.get(i).attr("src");
                                images.add(new Image(url, type));
                            }
                            return images;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return images;
                    }
                });
    }


}
