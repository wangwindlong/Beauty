package com.dante.girls.net;

import android.util.Log;

import com.dante.girls.model.DB;
import com.dante.girls.model.Image;
import com.dante.girls.picture.PictureFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.dante.girls.net.API.TYPE_DB_RANK;

/**
 * Help to get&fetch image resources from api
 */

public class DataFetcher {
    private final NetService netService;
    public String url; //source url
    public String type; //picture type
    public int page;

    public DataFetcher(String url, String type, int page) {
        this.url = url;
        this.type = type;
        this.page = page;
        netService = NetService.getInstance(url);

    }

    public Observable<List<Image>> getGank() {
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

    public Observable<List<Image>> getDouban() {
        Observable<ResponseBody> data;
        if (Objects.equals(type, TYPE_DB_RANK)) {
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
                                String src = elements.get(i).attr("src");
                                images.add(new Image(src, type));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return images;
                    }
                });
    }


    public Observable<List<Image>> getAPosts() {
        //get all posts' pictures
        return netService.getaApi().getPosts(type, page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
                            Elements elements = document.select("div[class=content] > a");
                            final int size = elements.size();
                            Log.i(TAG, "call: size" + size);
                            String url = elements.last().select("img").first().attr("src");
                            if (DB.getByUrl(url) != null) {
                                Log.i(TAG, "getAPosts: find saved image!");
                                return null;//最后一个元素在数据库里已经保存了，那么不需要继续解析。
                            }

                            for (int i = 0; i < size; i++) {
                                Element link = elements.get(i);
                                String postUrl = link.attr("href").replace(API.A_BASE, "");
                                String title = link.attr("title");
                                String src = link.select("img").first().attr("src");
                                Image image = new Image(src, type);
                                image.setInfo(postUrl);
                                image.setTitle(title);
                                images.add(image);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return images;
                    }
                });


    }

    public Observable<List<Image>> getPicturesOfPost(String info) {
        //get all images in this post
        Log.i(TAG, "getAPost : " + info);

        return netService.getaApi().getPictures(info, page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
                            Elements elements = document.select("div[class=post] > p > a > img");
                            final int size = elements.size();
                            String url = elements.last().attr("src");
                            if (DB.getByUrl(url) != null) {
                                Log.i(TAG, "getPicturesOfPost: find saved image!");
                                return null;
                            }

                            Log.i(TAG, "call: size" + size);
                            for (int i = 0; i < size; i++) {
                                String src = elements.get(i).attr("src");
//                                Log.i(TAG, "Inpost imgUrl: " + src + " type-" + type);
                                Image image = new Image(src, type);
                                images.add(image);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return images;
                    }
                });

    }

}
