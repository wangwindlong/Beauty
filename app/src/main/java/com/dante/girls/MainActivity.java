package com.dante.girls;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
import com.dante.girls.picture.FavoriteFragment;
import com.dante.girls.ui.SettingFragment;
import com.dante.girls.ui.SettingsActivity;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.SPUtil;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.UI;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import butterknife.BindView;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

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
    private boolean backPressed;
    private MenuItem item;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initViews() {
        super.initViews();
        setupDrawer();
        initNavigationView();
        initMain();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UI.showSnack(fab, R.string.donate);
                if (AlipayZeroSdk.hasInstalledAlipayClient(getApplicationContext())) {
                    AlipayZeroSdk.startAlipayClient(MainActivity.this, Constants.ALI_PAY);
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState: ");
        if (item != null) {
            outState.putInt("itemId", item.getItemId());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int itemId = savedInstanceState.getInt("itemId", R.id.nav_beauty);
        Log.i(TAG, "onRestoreInstanceState: ");
        MenuItem item = navView.getMenu().findItem(itemId);
        navView.setCheckedItem(itemId);
        if (item != null) {
            onNavigationItemSelected(item);
        }
    }

    private void initMain() {
        MenuItem item = navView.getMenu().findItem(R.id.nav_beauty);
        onNavigationItemSelected(item);
    }

    private void replace(String[] titles, String[] types) {
        replaceFragment(MainActivityFragment.newInstance(titles, types), "");
    }

    private void setupDrawer() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            doublePressBackToQuit();
        }
    }

    private void doublePressBackToQuit() {
        if (backPressed) {
            super.onBackPressed();
            return;
        }
        backPressed = true;
        UI.showSnack(fab, R.string.leave_app);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressed = false;
            }
        }, 2000);
    }

    private void initNavigationView() {
        //load headerView's image
        Imager.load(this, R.drawable.head, (ImageView) navView.getHeaderView(0).findViewById(R.id.headImage));
        navView.setNavigationItemSelectedListener(this);
        boolean isSecretOn = SPUtil.getBoolean(SettingFragment.SECRET_MODE);
        navView.inflateMenu(R.menu.menu_main);
//        navView.setCheckedItem(R.id.nav_beauty);

        Menu menu = navView.getMenu();
        menu.getItem(0).setChecked(true);
        menu.getItem(0).setIcon(new IconicsDrawable(this).
                icon(GoogleMaterial.Icon.gmd_face)
                .color(getColor(R.color.pink)));

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
        if (id == R.id.nav_beauty) {
            String[] titles = getResources().getStringArray(R.array.db_titles);
            String[] types = {TYPE_GANK, TYPE_DB_RANK, TYPE_DB_BREAST, TYPE_DB_BUTT, TYPE_DB_LEG, TYPE_DB_SILK};
            replace(titles, types);
        } else if (id == R.id.nav_a) {
            String[] titles = getResources().getStringArray(R.array.a_titles);
            String[] types = {TYPE_A_ANIME, TYPE_A_FULI, TYPE_A_HENTAI, TYPE_A_UNIFORM, TYPE_A_ZATU};
            replace(titles, types);
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else if (id == R.id.nav_share) {
            Share.shareText(this, getString(R.string.share_app_description));
        } else if (id == R.id.nav_favorite) {
            replaceFragment(new FavoriteFragment(), Constants.FAVORITE);
        }

        this.item = item;
        drawerLayout.closeDrawers();
        return true;
    }

    public String getCurrentMenuTitle() {
        if (item == null) {
            item = navView.getMenu().getItem(0);
        }
        return item.getTitle().toString();
    }

}
