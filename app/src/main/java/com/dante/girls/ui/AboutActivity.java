package com.dante.girls.ui;

import android.content.Intent;
import android.widget.TextView;

import com.dante.girls.BuildConfig;
import com.dante.girls.R;
import com.dante.girls.base.BaseActivity;
import com.dante.girls.utils.AppUtils;
import com.dante.girls.utils.UI;

import butterknife.BindView;

/**
 * about the author and so on.
 */
public class AboutActivity extends BaseActivity {
    @BindView(R.id.versionName)
    TextView versionName;

    @Override
    protected int initLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews() {
        super.initViews();
        versionName.setText(String.format(R.string.version + " %s", BuildConfig.VERSION_NAME));
        versionName.append(" " + BuildConfig.VERSION_NAME);
    }

    @Override
    public void startActivity(Intent intent) {
        if (AppUtils.isIntentSafe(intent)) {
            super.startActivity(intent);
        } else {
            UI.showSnack(versionName, R.string.email_not_install);
        }
    }

}
