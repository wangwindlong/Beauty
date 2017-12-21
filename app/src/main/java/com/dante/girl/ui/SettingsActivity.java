package com.dante.girl.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dante.girl.R;
import com.dante.girl.base.BaseActivity;

public class SettingsActivity extends BaseActivity {

    @Override
    protected int initLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
//        setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
