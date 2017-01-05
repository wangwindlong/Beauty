package com.dante.girls.picture;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.dante.girls.R;
import com.dante.girls.base.BaseFragment;
import com.dante.girls.base.Constants;
import com.dante.girls.helper.BlurBuilder;
import com.dante.girls.lib.TouchImageView;
import com.dante.girls.model.DataBase;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.utils.BitmapUtil;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.tbruyelle.rxpermissions.RxPermissions;

import butterknife.BindView;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;


/**
 * Photo view fragment.
 */
public class ViewerFragment extends BaseFragment implements View.OnLongClickListener, View.OnClickListener {

    private static final String TAG = "test";
    @BindView(R.id.headImage)
    TouchImageView imageView;
    CompositeSubscription tasks = new CompositeSubscription();
    @BindView(R.id.likeBtn)
    LikeButton likeBtn;
    private ViewerActivity context;
    private Bitmap bitmap;
    private String url;

    public static ViewerFragment newInstance(String url) {
        ViewerFragment fragment = new ViewerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void initViews() {
        context = (ViewerActivity) getActivity();
        url = getArguments().getString(Constants.URL);
        ViewCompat.setTransitionName(imageView, url);
        load(url);
        likeBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                realm.beginTransaction();
                DataBase.getByUrl(realm, url).setLiked(true);
                realm.commitTransaction();

                if (SpUtil.getBoolean(SettingFragment.LIKE_DOWNLOAD)) {
                    save(bitmap);
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                realm.beginTransaction();
                DataBase.getByUrl(realm, url).setLiked(false);
                realm.commitTransaction();
            }
        });
    }

    private void load(String url) {
        Imager.loadDefer(this, url, new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap b, GlideAnimation<? super Bitmap> arg1) {
                bitmap = b;
                imageView.setImageBitmap(b);
                context.supportStartPostponedEnterTransition();
                likeBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.hint).
                setMessage(R.string.browse_picture_hint).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).
                create().show();
    }


    @Override
    protected void initData() {
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public boolean onLongClick(View v) {
        blur(bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final String[] items = {getString(R.string.share_to), getString(R.string.save_img)};
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                share(bitmap);
            } else if (which == 1) {
                context.hideSystemUi();
                save(bitmap);
            }
        }).setOnDismissListener(dialogInterface -> {
            imageView.setImageBitmap(bitmap);
            context.hideSystemUi();
        }).show();
        return true;
    }

    private void blur(Bitmap bitmap) {
        Subscription subscription = Observable.just(bitmap)
                .map(BlurBuilder::blur)
                .compose(applySchedulers())
                .subscribe(bitmap1 -> {
                    imageView.setImageBitmap(bitmap1);
                });
        tasks.add(subscription);
    }

    private void save(final Bitmap bitmap) {
        RxPermissions permissions = new RxPermissions(context);
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> BitmapUtil.writeToFile(bitmap))
                .compose(applySchedulers())
                .subscribe(file -> {
                    if (file != null && file.exists()) {
                        UiUtils.showSnack(rootView, getString(R.string.save_img_success)
                                + file.getPath());

                    } else {
                        UiUtils.showSnack(rootView, R.string.save_img_failed);
                    }
                });
        tasks.add(subscription);
    }

    private void share(final Bitmap bitmap) {
        final RxPermissions permissions = new RxPermissions(context);
        Subscription subscription = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map(granted -> {
                    if (granted) {
                        return BitmapUtil.bitmapToUri(bitmap);
                    }
                    return null;

                })
                .compose(applySchedulers())
                .subscribe(uri -> {
                    Share.shareImage(context, uri);
                });
        tasks.add(subscription);
    }

    @Override
    public void onClick(View v) {
        if (!SpUtil.getBoolean(Constants.HAS_HINT)) {
            showHint();
            SpUtil.save(Constants.HAS_HINT, true);
        } else {
            context.supportFinishAfterTransition();
        }
    }

    public View getSharedElement() {
        return imageView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tasks.unsubscribe();
    }

}
