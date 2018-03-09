package com.dante.girl.net;

import android.text.TextUtils;
import android.util.Log;

import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.picture.CustomPictureFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.dante.girl.net.API.TYPE_DB_RANK;

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
                .filter(listResult -> !listResult.error)
                .map(listResult -> {
                    List<Image> data = new ArrayList<>();
                    for (Image image :
                            listResult.results) {
                        if (!DataBase.hasImage(image.url)) {
                            data.add(image);
                        } else {

                        }
                    }
                    return data;
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
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
//                            Elements elements = document.select("div[class=thumbnail] > div[class=img_single] > a > img");
                        Elements elements = document.select("div[class=thumbnail] div[class=img_single] img");
                        final int size = elements.size();
                        for (int i = 0; i < size; i++) {
                            String src = elements.get(i).attr("src").trim();
                            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                                continue;
                            }
                            images.add(new Image(src, type));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });
    }

    private boolean isPictureUrl(String src) {
        if (TextUtils.isEmpty(src)) {
            return false;
        }
        return src.contains(".jpg") || src.contains(".png");
    }


    public Observable<List<Image>> getAPosts() {
        //get all posts' pictures
        return netService.getaApi().getPosts(type, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        Elements elements = document.select("div[class=content]  a");
//                            final int size = elements.size();
                        Log.i(TAG, "getAPosts: " + type + elements.size());
//                            String url = elements.last().select("img").first().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getAPosts: find saved image!");
//                                return null;//最后一个元素在数据库里已经保存了，那么不需要继续解析。
//                            }

                        for (int i = 0; i < elements.size(); i++) {
                            Element link = elements.get(i);
                            String postUrl = link.attr("href").replace(API.A_BASE, "");
                            String title = link.attr("title");
                            if (link.select("img").size() > 0) {
                                String src = link.select("img").first().attr("src").trim();
                                src = dealWithImgUrl(src);
//                                if (src.endsWith("!thumb")) {
//                                    Log.i(TAG, "load image: thumb url " + src);
//                                    src = src.replace("!thumb", "");
//                                }

                                if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                                    continue;
                                }
                                Image image = new Image(src, type);
                                image.setInfo(postUrl);
                                image.setReferer(String.format("http://www.apic.in/%s/page/%s", type, page));
                                image.setTitle(title);
                                images.add(image);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });


    }

    public Observable<List<Image>> getPicturesOfPost(String info) {
        //get all images in this post
        Log.d(TAG, "getPicturesOfPost : " + info);
        return netService.getaApi().getPictures(info, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        Elements article = document.select("article.article-content");
                        Elements elements = article.select("img[src]");
                        Elements test = document.select("div[class=article-content] > p > a > img");
                        final int size = elements.size();
//                            String url = elements.last().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getPicturesOfPost: find saved image!");
//                                return null;
//                            }
                        for (int i = 0; i < size; i++) {
                            String src = elements.get(i).attr("src").trim();
                            src = dealWithImgUrl(src);
                            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                                continue;
                            }
                            Image image = new Image(src, type);
                            image.setReferer(info);
                            images.add(image);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });

    }

    private String dealWithImgUrl(String src) {
//        if (BuildConfig.DEBUG) return src;
//        if (src.endsWith("!orgin")) {
//            src = src.replace("!orgin", "");
//        } else if (src.endsWith("!acgfiindex")) {
//            src = src.replace("!acgfiindex", "");
//
//        } else if (!src.endsWith(".jpg") && !src.endsWith("png")) {
//            if (src.contains(".jpg")) {
//                src = src.substring(0, src.lastIndexOf(".jpg") + 4);
//            } else if (src.contains(".png")) {
//                src = src.substring(0, src.lastIndexOf(".png") + 4);
//            }
//        }
        return src;
    }
    /*
        Elements links = doc.select("a[href]"); // 具有 href 属性的链接

        Elements pngs = doc.select("img[src$=.png]");//所有引用png图片的元素

        Element masthead = doc.select("div.masthead").first();  // 找出定义了 class=masthead 的元素

        Elements resultLinks = doc.select("h3.r > a"); // direct a after h3
     */

}
