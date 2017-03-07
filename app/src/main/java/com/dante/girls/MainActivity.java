package com.dante.girls;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
<<<<<<< HEAD
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
=======
import android.util.SparseArray;
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
<<<<<<< HEAD
import android.widget.TextView;
=======
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f

import com.blankj.utilcode.utils.BarUtils;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.helper.RevealHelper;
import com.dante.girls.helper.Updater;
import com.dante.girls.lib.PopupDialogActivity;
import com.dante.girls.picture.FavoriteFragment;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.ui.SettingsActivity;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.SpUtil;
import com.dante.girls.utils.UiUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.Random;

import butterknife.BindView;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
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
import static com.dante.girls.net.API.TYPE_GANK;
import static com.dante.girls.utils.AppUtil.donate;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String MAIN_FRAGMENT_TAG = "main";
    public static final int DRAWER_CLOSE_DELAY = 230;
    private static final String TAG = "MainActivity";
    @BindView(R.id.fab)
    public FloatingActionButton fab;
    public ActionBarDrawerToggle toggle;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.container)
    ViewGroup container;
    @BindView(R.id.reveal)
    FrameLayout revealView;
    private boolean backPressed;
<<<<<<< HEAD
    private Handler mHandler = new Handler();
    private Runnable mRunnable = ()->backPressed = false;
    private MenuItem currentMenu;
    private SparseArray<Fragment> fragmentSparseArray;
//    private Updater updater;
=======
    private MenuItem currentMenu;
    private SparseArray<Fragment> fragmentSparseArray;
    private Updater updater;
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
    private boolean isFirst = true;
    private int placeHolderHeight;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initToolbar();
<<<<<<< HEAD
//        updater = Updater.getInstance(this);
//        updater.check();
        setupDrawer();
        initNavigationView();
        initFragments(savedInstanceState);

    }

    private void initToolbar() {
        if (getSupportActionBar() != null) {
            // Remove default title text
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Display icon in the toolbar
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            //去掉左侧的返回按钮
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            getSupportActionBar().setLogo(R.drawable.btg_icon_account);
//            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }
        toolbar.findViewById(R.id.leftImage).setOnClickListener(view -> drawerLayout.openDrawer(Gravity.LEFT));
=======
        updater = Updater.getInstance(this);
        updater.check();
        setupDrawer();
        initNavigationView();
        initFragments(savedInstanceState);
    }

    private void initToolbar() {
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = BarUtils.getActionBarHeight(this);
            toolbar.setLayoutParams(layoutParams);
            initFab();
            return;
        }
        toolbar.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        toolbar.getViewTreeObserver().removeOnPreDrawListener(this);
                        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                        toolbar.measure(widthSpec, heightSpec);
                        placeHolderHeight = toolbar.getHeight();
                        collapseToolbar();
                        return true;
                    }
                });
    }

    private void collapseToolbar() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(toolbar, "height", placeHolderHeight, BarUtils.getActionBarHeight(this));
        objectAnimator.setDuration(3000);
        objectAnimator.start();
//        toolbar.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(placeHolderHeight, BarUtils.getActionBarHeight(this));
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            toolbar.setLayoutParams(layoutParams);

        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                initFab();
            }
        });
        animator.start();
    }

    private void initFab() {
        if (SpUtil.getBoolean(SettingFragment.SECRET_MODE)) {
            return;
        }
        fab.animate().setStartDelay(500)
                .setDuration(400).scaleY(1).scaleX(1).start();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            fab.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setTitle(R.string.hint)
                        .setMessage(R.string.thanks_for_donation)
                        .setPositiveButton(R.string.donate, (dialogInterface, i) -> donate(MainActivity.this))
                        .create();
//                alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlideDialog;
                alertDialog.show();
            });
            return;
        }
        if (new Random().nextBoolean()) {
            //Morph transition
            fab.setOnClickListener(v -> {
                Intent login = PopupDialogActivity.getStartIntent(MainActivity.this, PopupDialogActivity.MORPH_TYPE_FAB);
                ActivityOptionsCompat options = null;
                options = ActivityOptionsCompat.makeSceneTransitionAnimation
                        (MainActivity.this, fab, getString(R.string.transition_morph_view));
                startActivity(login, options.toBundle());
            });
        } else {
            //Reveal animation
            final RevealHelper helper = new RevealHelper(this, revealView)
                    .hide(container)
                    .button(fab)
                    .onRevealEnd(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toggleToolbarFlag(false);
                            toggle.setDrawerIndicatorEnabled(false);
                            donate(MainActivity.this);
                        }
                    }).onUnrevealEnd(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            changeNavigator(true);
                        }
                    }).build();
            toggle.setToolbarNavigationClickListener(v -> helper.unreveal());
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (currentMenu != null) {
//            outState.putInt("itemId", currentMenu.getItemId());
//        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int itemId = savedInstanceState.getInt("itemId", R.id.nav_beauty);
//        MenuItem currentMenu = navView.getMenu().findItem(itemId);
//        navView.setCheckedItem(itemId);
//        if (currentMenu != null) {
//            onNavigationItemSelected(currentMenu);
//        }
//    }

    private void initFragments(Bundle savedInstanceState) {
        if (fragmentSparseArray == null) {
            String[] titles, types;
            fragmentSparseArray = new SparseArray<>();
            String[] all = getResources().getStringArray(R.array.db_titles);
            if (SpUtil.getBoolean(SettingFragment.SECRET_MODE)) {
                //Gank & Douban
                titles = all;
                types = new String[]{TYPE_GANK, TYPE_DB_RANK, TYPE_DB_BREAST, TYPE_DB_BUTT, TYPE_DB_LEG, TYPE_DB_SILK};
            } else {
                titles = new String[]{all[0]};
                types = new String[]{TYPE_GANK};
            }
            fragmentSparseArray.put(R.id.nav_beauty, MainTabsFragment.newInstance(titles, types));

            //二次元
            titles = getResources().getStringArray(R.array.a_titles);
            types = new String[]{TYPE_A_ANIME, TYPE_A_FULI, TYPE_A_HENTAI, TYPE_A_UNIFORM, TYPE_A_ZATU};
            fragmentSparseArray.put(R.id.nav_a, MainTabsFragment.newInstance(titles, types));
            //favorite
            fragmentSparseArray.put(R.id.nav_favorite, new FavoriteFragment());
        }
        setMainFragment(R.id.nav_beauty, fragmentSparseArray, savedInstanceState == null);
    }

    private void setupDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
<<<<<<< HEAD
//        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
//                R.drawable.btg_icon_account, R.drawable.btg_icon_account);
        toggle.setDrawerIndicatorEnabled(false); //disable "hamburger to arrow" drawable
=======
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            return;
        }
        doublePressBackToQuit();
    }

    private void doublePressBackToQuit() {
        if (backPressed) {
<<<<<<< HEAD
            mHandler.removeCallbacks(mRunnable);
=======
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UiUtils.showSnack(fab, R.string.leave_app);
<<<<<<< HEAD
        mHandler.postDelayed(mRunnable, 2000);
=======
        new Handler().postDelayed(() -> backPressed = false, 2000);
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initNavigationView() {
//        Colorful.config(this)
//                .translucent(true)
//                .apply();

        //load headerView's image
        Imager.load(this, R.drawable.head, (ImageView) navView.getHeaderView(0).findViewById(R.id.headImage));
        navView.setNavigationItemSelectedListener(this);
        boolean isSecretOn = SpUtil.getBoolean(SettingFragment.SECRET_MODE);
        navView.inflateMenu(R.menu.menu_main);
//        navView.setCheckedItem(R.id.nav_beauty);
        Menu menu = navView.getMenu();
        menu.getItem(0).setChecked(true);
        menu.getItem(0).setIcon(new IconicsDrawable(this).
                icon(GoogleMaterial.Icon.gmd_face)
                .color(ContextCompat.getColor(this, R.color.pink)));

        menu.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_collections));

        menu.getItem(2).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_favorite)
                .color(Color.RED));

        Menu sub = menu.getItem(3).getSubMenu();
        sub.getItem(0).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_share)
                .color(Color.DKGRAY));
        sub.getItem(1).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(Color.GRAY));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_setting:
                new Handler().postDelayed(() -> {
                    changeDrawer(false);
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }, DRAWER_CLOSE_DELAY);
                break;
            case R.id.nav_share:
                String text = SpUtil.get(Updater.SHARE_APP, getString(R.string.share_app_description));
                Share.shareText(this, text);
                break;
            default:
                currentMenu = item;
                setToolbarTitle(getCurrentMenuTitle());
                switchMenu(id, fragmentSparseArray);
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }


    public void changeNavigator(boolean enable) {
        if (toggle == null) return;
        if (enable) {
<<<<<<< HEAD
            //去掉汉堡包图标
//            toggle.setDrawerIndicatorEnabled(true);
            toggle.setDrawerIndicatorEnabled(false);
=======
            toggle.setDrawerIndicatorEnabled(true);
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
        } else {
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(v -> onBackPressed());
        }
    }

    public void toggleToolbarFlag(boolean scroll) {
        AppBarLayout.LayoutParams p = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        if (scroll) {
            p.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
        } else {
            p.setScrollFlags(0);
        }
        toolbar.setLayoutParams(p);
    }

    public String getCurrentMenuTitle() {
        if (currentMenu == null) {
            currentMenu = navView.getMenu().getItem(0);
        }
        return currentMenu.getTitle().toString();
    }

    @Override
    protected void onDestroy() {
<<<<<<< HEAD
//        updater.release();
=======
        updater.release();
>>>>>>> f4cc4ff091ba094e874d383f7abd07531356866f
        super.onDestroy();
    }

    Intent get(Class clz) {
        return new Intent(getApplicationContext(), clz);
    }

    public void changeDrawer(boolean enable) {
        drawerLayout.setDrawerLockMode(enable ?
                DrawerLayout.LOCK_MODE_UNLOCKED : LOCK_MODE_LOCKED_CLOSED);

    }
}
