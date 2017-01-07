package com.dante.girls;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
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

import org.polaric.colorful.Colorful;

import java.util.Random;

import butterknife.BindView;

import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static android.support.design.widget.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
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
    private static final String TAG = "MainActivity";
    @BindView(R.id.fab)
    public FloatingActionButton fab;
    public ActionBarDrawerToggle toggle;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.reveal)
    FrameLayout revealView;
    private boolean backPressed;
    private MenuItem currentMenu;
    private SparseArray<Fragment> fragmentSparseArray;
    private Updater updater;
    private Fragment currentFragment;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        super.initViews();
        updater = Updater.getInstance(this);
        updater.check();
        setupDrawer();
        initNavigationView();
        initFragments();
        initFab();
    }

    private void initFab() {
        if (SpUtil.getBoolean(SettingFragment.SECRET_MODE)) {
            fab.setVisibility(View.GONE);
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            fab.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog alertDialog = builder.setTitle(R.string.hint)
                        .setMessage(R.string.thanks_for_donation)
                        .setPositiveButton(R.string.donate, (dialogInterface, i) -> donate(MainActivity.this))
                        .create();
                alertDialog.getWindow().getAttributes().windowAnimations = R.style.SlideDialog;
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
//        Log.d(TAG, "onSaveInstanceState: ");
//        if (currentMenu != null) {
//            outState.putInt("itemId", currentMenu.getItemId());
//        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int itemId = savedInstanceState.getInt("itemId", R.id.nav_beauty);
//        Log.i(TAG, "onRestoreInstanceState: ");
//        MenuItem currentMenu = navView.getMenu().findItem(itemId);
//        navView.setCheckedItem(itemId);
//        if (currentMenu != null) {
//            onNavigationItemSelected(currentMenu);
//        }
//    }

    private void initFragments() {
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
        putFragment(R.id.nav_beauty, titles, types);
        //二次元
        titles = getResources().getStringArray(R.array.a_titles);
        types = new String[]{TYPE_A_ANIME, TYPE_A_FULI, TYPE_A_HENTAI, TYPE_A_UNIFORM, TYPE_A_ZATU};
        putFragment(R.id.nav_a, titles, types);
        //favorite
        fragmentSparseArray.put(R.id.nav_favorite, new FavoriteFragment());
        //Main
        setFragment(R.id.nav_beauty, fragmentSparseArray);
    }


    private void putFragment(int navId, String[] titles, String[] types) {
        fragmentSparseArray.put(navId, MainTabsFragment.newInstance(titles, types));
    }

    private void setupDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        doublePressBackToQuit();
    }

    private void doublePressBackToQuit() {
        if (backPressed || getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UiUtils.showSnack(fab, R.string.leave_app);
        new Handler().postDelayed(() -> backPressed = false, 2000);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initNavigationView() {
        Colorful.config(this)
                .translucent(true)
                .apply();

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
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.nav_share:
                String text = SpUtil.get(Constants.SHARE_APP, getString(R.string.share_app_description));
                Share.shareText(this, text);
                break;
            default:
                currentMenu = item;
                setToolbarTitle(getCurrentMenuTitle());
                setFragment(id, fragmentSparseArray);
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private void replace(String[] titles, String[] types) {
        replaceFragment(MainTabsFragment.newInstance(titles, types), "");
    }

    public void changeNavigator(boolean enable) {
        if (enable) {
            toggle.setDrawerIndicatorEnabled(true);
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
        updater.release();
        super.onDestroy();
    }
}
