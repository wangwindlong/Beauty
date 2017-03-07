package com.dante.girls.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.bugtags.library.Bugtags;
import com.dante.girls.MainActivity;
import com.dante.girls.R;

import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * BaseActivity includes a base layoutId, init its toolbar (if the layout has one)
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public Realm realm;
    public Toolbar toolbar;
    public TextView titleTv;
    private boolean isShowToolbar = true;
    private Fragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews(savedInstanceState);
    }

    protected abstract int initLayoutId();

    /**
     * MUST override and call the SUPER method
     */
    protected void initViews(@Nullable Bundle savedInstanceState) {
        setContentView(initLayoutId());
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        initAppBar();
        initSDK();
    }

    private void initSDK() {

    }


    public void initAppBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            titleTv = (TextView) toolbar.findViewById(R.id.title_tv);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, tag);
        transaction.commit();
    }

    public void toggleToolbar() {
        if (isShowToolbar) {
            hideToolbar();
        } else {
            showToolbar();
        }
    }

    public void hideToolbar() {
        if (toolbar != null) {
            isShowToolbar = false;
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        }
    }

    public void showToolbar() {
        if (toolbar != null) {
            isShowToolbar = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        currentFragment = null;
    }

    public void setToolbarTitle(String title) {
        if (titleTv != null) {
            titleTv.setText(title);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this, ev);
        return super.dispatchTouchEvent(ev);
    }

    public void setMainFragment(int id, SparseArray<Fragment> array, boolean first) {
        Fragment fragment = array.get(id);
//        if (fragment == null || currentFragment == fragment) {
//            return;
//        }
//        Fragment old = currentFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //Main fragment 不需要动画
        //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
//        if (old != null) {
//            transaction.hide(old);
//        }
//        if (fragment.isAdded()) {
//            transaction.show(fragment);
//            transaction.commit();
//            return;
//        }
        if (first) {
            transaction.add(R.id.container, fragment, String.valueOf(id));
        } else {
            transaction.replace(R.id.container, fragment, String.valueOf(id));
        }
        transaction.commit();

        this.currentFragment = fragment;
    }

    public void switchMenu(int id, SparseArray<Fragment> array) {
        Fragment fragment = array.get(id);
        if (fragment == null || currentFragment == fragment) {
            return;
        }
        Fragment old = currentFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (old != null) {
            transaction.hide(old);
        }

        if (fragment.isAdded()) {
            transaction.show(fragment);
            transaction.commit();
        } else {
            new Handler().postDelayed(() -> {
                transaction.add(R.id.container, fragment, String.valueOf(id));
                transaction.commit();
            }, MainActivity.DRAWER_CLOSE_DELAY);
        }
        currentFragment = fragment;
    }
}
