package com.sinanjam.arsiv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class SplashActivity extends Activity {
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/Sinanjam/Balkes-Arsivi/releases/latest";
    private static final String LATEST_RELEASE_PAGE = "https://github.com/Sinanjam/Balkes-Arsivi/releases/latest";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean openedMain = false;

    private final Runnable openRunnable = new Runnable() {
        @Override public void run() { openMain(); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.rgb(35, 0, 0));
        getWindow().setNavigationBarColor(Color.rgb(20, 0, 0));

        FrameLayout root = new FrameLayout(this);
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(18, 0, 0), Color.rgb(105, 0, 0), Color.rgb(18, 0, 0)}
        );
        root.setBackground(background);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(30), dp(24), dp(30), dp(24));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.balkes_1966_banner);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(130));
        logoParams.setMargins(0, 0, 0, dp(22));
        content.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("Balkes Arşivi");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLetterSpacing(0.04f);
        title.setShadowLayer(10, 0, 2, Color.BLACK);
        content.addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView slogan = new TextView(this);
        slogan.setText("Kırmızı Beyaz tarih...");
        slogan.setTextColor(Color.rgb(255, 230, 230));
        slogan.setTextSize(18);
        slogan.setGravity(Gravity.CENTER);
        slogan.setTypeface(Typeface.DEFAULT_BOLD);
        slogan.setPadding(0, dp(12), 0, 0);
        slogan.setShadowLayer(8, 0, 2, Color.BLACK);
        content.addView(slogan, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        root.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        setContentView(root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openMain(); }
        });

        checkLatestRelease();
        handler.postDelayed(openRunnable, 2400);
    }

    private void checkLatestRelease() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    String json = downloadString(LATEST_RELEASE_API);
                    JSONObject obj = new JSONObject(json);
                    String tag = obj.optString("tag_name", "");
                    String name = obj.optString("name", "");
                    String url = obj.optString("html_url", LATEST_RELEASE_PAGE);
                    if (hasText(url) && !releaseMatchesCurrent(tag, name)) {
                        openLatestRelease(url);
                    }
                } catch (Exception ignored) { }
            }
        }).start();
    }

    private boolean releaseMatchesCurrent(String tag, String name) {
        String current = normalizeVersion(currentVersionName());
        String t = normalizeVersion(tag);
        String n = normalizeVersion(name);
        return (hasText(t) && (t.equals(current) || t.contains(current) || current.contains(t))) ||
                (hasText(n) && (n.equals(current) || n.contains(current) || current.contains(n)));
    }

    private String currentVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "2.0-final";
        }
    }

    private String normalizeVersion(String value) {
        if (value == null) return "";
        return value.toLowerCase(new Locale("tr", "TR"))
                .replace("balkes arşivi", "")
                .replace("balkes arsivi", "")
                .replace("version", "")
                .replace("sürüm", "")
                .replace("surum", "")
                .replace("release", "")
                .replaceAll("^[v\\s_-]+", "")
                .replaceAll("[^a-z0-9.]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .trim();
    }

    private void openLatestRelease(final String url) {
        handler.post(new Runnable() {
            @Override public void run() {
                if (openedMain) return;
                openedMain = true;
                handler.removeCallbacks(openRunnable);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private String downloadString(String urlText) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlText).openConnection();
        conn.setConnectTimeout(1200);
        conn.setReadTimeout(1500);
        conn.setRequestProperty("User-Agent", "BalkesArsivi-Android");
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        in.close();
        return out.toString("UTF-8");
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private void openMain() {
        if (openedMain) return;
        openedMain = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
