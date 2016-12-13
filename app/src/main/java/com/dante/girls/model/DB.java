package com.dante.girls.model;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Deals with cache, data
 */
public class DB {

    public static <T extends RealmObject> void save(Realm realm, List<T> realmObjects) {
        if (realm.isClosed()) {
            return;
        }
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }

    public static void save(Realm realm, RealmObject realmObject) {
        if (realm.isClosed()) {
            return;
        }
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
    }

    public static <T extends RealmObject> T getById(Realm realm, int id, Class<T> realmObjectClass) {
        return realm.where(realmObjectClass).equalTo("id", id).findFirst();
    }


    public static <T extends RealmObject> RealmResults<T> findAll(Realm realm, Class<T> realmObjectClass) {
        if (realm.isClosed()) {
            return null;
        }
        return realm.where(realmObjectClass).findAll();
    }

    public static <T extends RealmObject> void clear(Realm realm, Class<T> realmObjectClass) {
        realm.beginTransaction();
        findAll(realm, realmObjectClass).deleteAllFromRealm();
        realm.commitTransaction();
    }

    public static RealmResults<Image> getImages(Realm realm, int type) {
        if (realm.isClosed()) {
            return null;
        }
        return realm.where(Image.class)
                .equalTo("type", type)
                .findAll()
                .sort("publishedAt", Sort.DESCENDING);
    }
}
