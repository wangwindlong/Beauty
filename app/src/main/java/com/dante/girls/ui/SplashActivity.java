package com.dante.girls.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.appcompat.R.anim;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dante.girls.MainActivity;
import com.dante.girls.R;
import com.dante.girls.net.API;
import com.dante.girls.net.NetService;
import com.dante.girls.base.Constants;
import com.dante.girls.utils.DateUtil;
import com.dante.girls.utils.Imager;
import com.dante.girls.utils.SPUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import okhttp3.ResponseBody;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;
    private static final String SPLASH = "splash";
    private ImageView splash;
    private String today;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        splash = (ImageView) findViewById(R.id.splash);

        if (SPUtil.getBoolean(SettingFragment.ORIGINAL_SPLASH)) {
            Glide.with(this).load(R.drawable.splash).crossFade(1500).into(splash);
            startAppDelay();
            return;
        }
        initSplash();
    }


    private void initSplash() {
        today = DateUtil.parseStandardDate(new Date());
        updateSplash();
        loadImage();
    }

    //Update url of splash, no load
    private void updateSplash() {
        if (today.equals(SPUtil.getString(Constants.DATE)) && !SPUtil.get(SPLASH, "").isEmpty()) {
            return;
        }
        NetService.getInstance(API.SPLASH).getDbApi().getSplash()
                .map(new Func1<ResponseBody, String>() {
                    @Override
                    public String call(ResponseBody responseBody) {
                        try {
                            return new JSONObject(responseBody.string()).getString("img");
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String url) {
                        SPUtil.save(SPLASH, url);
                        SPUtil.save(Constants.DATE, today);
                    }
                });


    }

    private void loadImage() {
        String url = SPUtil.get(SPLASH, "");
        if ("".equals(url)) {
            Glide.with(this).load(R.drawable.splash).crossFade(SPLASH_DURATION).into(splash);
        } else {
            Imager.load(url, R.anim.splash_anim, splash);
        }
        startAppDelay();
    }

    private void startAppDelay() {
        splash.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(anim.abc_grow_fade_in_from_bottom, anim.abc_shrink_fade_out_from_bottom);
                finish();
            }
        }, SPLASH_DURATION);
    }

    @Override
    public void onBackPressed() {
        //disable back button when showing splash
    }

}
