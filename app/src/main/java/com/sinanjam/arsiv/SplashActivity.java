package com.sinanjam.arsiv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

public class SplashActivity extends Activity {
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
        handler.postDelayed(openRunnable, 1850);
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
