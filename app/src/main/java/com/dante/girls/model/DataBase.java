package com.dante.girls.model;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

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

    public static Realm initRealm(Realm realm) {
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
        return realm.where(realmObjectClass).equalTo("id", id).findFirst();
    }


    public static Image getByUrl(Realm realm, String url) {
        initRealm(realm);
        return realm.where(Image.class).equalTo("url", url).findFirst();
    }

    public static Image getByUrl(String url) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Image.class).equalTo("url", url).findFirst();
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
        return realm.where(Image.class)
                .equalTo("type", type)
                .findAll()
                .sort("publishedAt", Sort.DESCENDING)
                ;
    }

    public static RealmResults<Image> findFavoriteImages(Realm realm) {
        realm = initRealm(realm);
        return realm.where(Image.class)
                .equalTo("isLiked", true)
                .findAll();
    }
}
