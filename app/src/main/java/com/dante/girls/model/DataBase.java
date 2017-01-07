package com.dante.girls.model;


import android.support.annotation.Nullable;
import android.util.Log;

import com.dante.girls.base.Constants;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Deals with cache, data
 */
public class DataBase {

    public static <T extends RealmObject> void save(Realm realm, List<T> realmObjects) {
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }

    private static Realm initRealm(Realm realm) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public static void save(Realm realm, RealmObject realmObject) {
        if (realmObject == null) {
            return;
        }
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> T getById(Realm realm, int id, Class<T> realmObjectClass) {
        return realm.where(realmObjectClass).equalTo(Constants.ID, id).findFirst();
    }

    public static boolean hasImage(@Nullable Realm realm, String url) {
        realm = initRealm(realm);
        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst() != null;
    }

    public static boolean hasImages(Realm realm, Collection<Image> list, String imageType) {
        boolean resut = realm.where(Image.class).equalTo(Constants.TYPE, imageType).findAll().containsAll(list);
        Log.d(TAG, "hasImages : " + resut);
        return resut;
    }


    public static Image getByUrl(Realm realm, String url) {
        initRealm(realm);
        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst();
    }

    public static Image getByUrl(String url) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst();
    }


    private static <T extends RealmObject> RealmResults<T> findAll(Realm realm, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public static <T extends RealmObject> void clear(Realm realm, Class<T> realmObjectClass) {
        realm.beginTransaction();
        findAll(realm, realmObjectClass).deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static RealmResults<Image> findImages(Realm realm, String type) {
        realm = initRealm(realm);
        if (Constants.FAVORITE.equals(type)) {
            return findFavoriteImages(realm);
        }
        return realm.where(Image.class)
                .equalTo(Constants.TYPE, type)
                .findAll()
//                .sort("publishedAt", Sort.DESCENDING)
                .sort(Constants.ID)
                ;
    }

    public static RealmResults<Image> findFavoriteImages(Realm realm) {
        realm = initRealm(realm);
        return realm.where(Image.class)
                .equalTo("isLiked", true)
                .findAll();
    }

    public static void clearAllImages() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Image.class);
        realm.commitTransaction();
    }
}
