package com.dante.girls.net;

import android.util.Log;

import com.dante.girls.model.Image;
import com.dante.girls.picture.CustomPictureFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private int page;

    public DataFetcher(String url, String type, int page) {
        this.url = url;
        this.type = type;
        this.page = page;
        netService = NetService.getInstance(url);

    }

    public Observable<List<Image>> getGank() {
        return netService.getGankApi().get(CustomPictureFragment.LOAD_COUNT, page)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
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
        if (TYPE_DB_RANK.equals(type)) {
            data = netService.getDbApi().getRank(page);
        } else {
            data = netService.getDbApi().get(type, page);
        }
        return data
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
//                            Elements elements = document.select("div[class=thumbnail] > div[class=img_single] > a > img");
                            Elements elements = document.select("div[class=thumbnail] div[class=img_single] img");
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
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
                            Elements elements = document.select("div[class=content]  a");
//                            final int size = elements.size();
                            Log.i(TAG, "call: " + type + elements.size());
                            if (elements == null || elements.size() == 0) {
                                //fuli页面解析失败，经发现，HTML结构跟其他页面不同，这是临时办法
                                elements = document.select("article[class=angela--post-home]");
                                Log.i(TAG, "call: posts size " + elements.size());

                                for (Element e : elements) {
                                    Element thumb = e.getElementsByClass("block-image").first();
                                    Element title = e.getElementsByClass("angela-title").first();
                                    String href = title.attr("href").replace(API.A_BASE, "");
                                    String t = title.attr("title");
                                    String style = thumb.attr("style");
                                    Log.i(TAG, "call: style " + style);
                                    String url = Pattern.compile("\\(([^)]+)\\)").matcher(style).group(1);
                                    Log.i(TAG, "call: style url " + url);
                                    Log.i(TAG, "call: style url group " + Pattern.compile("\\(([^)]+)\\)").matcher(style).group());
                                    Image image = new Image(url, type);
                                    image.setInfo(href);
                                    image.setTitle(t);
                                    images.add(image);
                                }
                                return images;
                            }

                            String url = elements.last().select("img").first().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getAPosts: find saved image!");
//                                return null;//最后一个元素在数据库里已经保存了，那么不需要继续解析。
//                            }

                            for (int i = 0; i < elements.size(); i++) {
                                Element link = elements.get(i);
                                String postUrl = link.attr("href").replace(API.A_BASE, "");
                                String title = link.attr("title");
                                String src = link.select("img").first().attr("src");
                                if (src.endsWith("!thumb")) {
                                    src = src.replace("!thumb", "");
                                }
                                Image image = new Image(src, type);
                                image.setInfo(postUrl);
                                image.setTitle(title);
                                images.add(image);
                                if (i == elements.size()) {

                                }

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
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .map(new Func1<ResponseBody, List<Image>>() {
                    @Override
                    public List<Image> call(ResponseBody responseBody) {
                        List<Image> images = new ArrayList<>();
                        try {
                            Document document = Jsoup.parse(responseBody.string());
                            Elements elements = document.select("div[class=post] img");
                            Elements test = document.select("div[class=post] > p > a > img");
                            final int size = elements.size();
                            Log.i(TAG, "call: test " + test.size());
                            String url = elements.last().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getPicturesOfPost: find saved image!");
//                                return null;
//                            }

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
