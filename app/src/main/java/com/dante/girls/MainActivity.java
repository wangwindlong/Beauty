package com.dante.girls;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dante.girls.base.BaseActivity;
import com.dante.girls.base.Constants;
import com.dante.girls.ui.SettingsActivity;
import com.dante.girls.utils.Share;
import com.dante.girls.utils.UI;

import butterknife.BindView;
import moe.feng.alipay.zerosdk.AlipayZeroSdk;

public class MainActivity extends BaseActivity {
    public static final String MAIN_FRAGMENT_TAG = "main";
    private static final String TAG = "MainActivity";
    @BindView(R.id.fab)
    public FloatingActionButton fab;
    private boolean backPressed;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        super.initViews();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        replaceFragment(new MainActivityFragment(), MAIN_FRAGMENT_TAG);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AlipayZeroSdk.hasInstalledAlipayClient(getApplicationContext())) {
                    AlipayZeroSdk.startAlipayClient(MainActivity.this, Constants.ALI_PAY);
                }
            }
        });
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        Log.i(TAG, "onActivityReenter: " + (fragment == null ? "null" : "not null"));
        if (fragment != null) {
//            fragment.onReenter(new MessageEvent(data.getIntExtra("index", 0)));
        }
    }


    @Override
    public void onBackPressed() {
        doublePressBackToQuit();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        } else if (id == R.id.action_share) {
            Share.shareText(this, getString(R.string.share_app_description));

        }
        return super.onOptionsItemSelected(item);
    }

}
