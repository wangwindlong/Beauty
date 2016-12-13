package com.dante.girls.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dante.girls.R;


/**
 * Util to share data, make share intent, etc.
 */
public class Share {

    public static Intent getShareIntent(String shareText) {
        Intent textIntent = new Intent();
        textIntent.setAction(Intent.ACTION_SEND);
        textIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        textIntent.setType("text/plain");
        return textIntent;
    }
    public static Intent getShareImageIntent(Uri uri) {
        Intent image = new Intent();
        image.setAction(Intent.ACTION_SEND);
        image.putExtra(Intent.EXTRA_STREAM, uri);
        image.setType("image/*");
        return image;
    }

    public static void shareText(Context context, String text) {
        context.startActivity(
                Intent.createChooser(getShareIntent(text),
                        context.getString(R.string.share_to)));
    }

    public static void shareImage(Context context, Uri uri){
        if (uri == null) {
            return;
        }
        context.startActivity(
                Intent.createChooser(getShareImageIntent(uri),
                        context.getString(R.string.share_to)));
    }


}
