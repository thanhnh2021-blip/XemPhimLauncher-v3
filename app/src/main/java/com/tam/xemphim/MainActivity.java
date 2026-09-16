package com.tam.xemphim;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends Activity {
    public static final String PREFS = "xemphim";
    public static final String KEY_ACTIVE = "youtube_session";
    public static final String BRAVE = "com.brave.browser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ACTIVE, true)
                .apply();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://m.youtube.com"));
        intent.setPackage(BRAVE);
        startActivity(intent);

        finish();
    }
}
