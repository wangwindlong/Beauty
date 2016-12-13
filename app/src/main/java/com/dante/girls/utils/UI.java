package com.dante.girls.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Util to show hint such as snackBar or dialog.
 */
public class UI {

    public static void showSnack(View rootView, int textId) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_SHORT).show();
        }
    }
    public static void showSnack(View rootView, String text) {
        if (null != rootView) {
            Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void showSnackLong(View rootView, int textId) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_LONG).show();
        }
    }
}
