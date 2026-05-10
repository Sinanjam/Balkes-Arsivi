package com.sinanjam.arsiv;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class UpdateCheckReceiver extends BroadcastReceiver {
    private static final String ACTION_CHECK = "com.sinanjam.arsiv.CHECK_LATEST_RELEASE";
    private static final String PREFS = "balkes_arsivi_update_checker";
    private static final String KEY_LAST_NOTIFIED = "last_notified_release";
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/Sinanjam/Balkes-Arsivi/releases/latest";
    private static final String LATEST_RELEASE_PAGE = "https://github.com/Sinanjam/Balkes-Arsivi/releases/latest";
    private static final String CHANNEL_ID = "balkes_arsivi_update_channel";
    private static final long CHECK_INTERVAL_MS = 30L * 60L * 1000L;

    public static void schedule(Context context) {
        try {
            Context app = context.getApplicationContext();
            AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;
            Intent intent = new Intent(app, UpdateCheckReceiver.class);
            intent.setAction(ACTION_CHECK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    app,
                    19660214,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag()
            );
            long first = System.currentTimeMillis() + CHECK_INTERVAL_MS;
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, first, CHECK_INTERVAL_MS, pendingIntent);
        } catch (Throwable ignored) { }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        String action = intent == null ? "" : intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            schedule(app);
            return;
        }
        schedule(app);
        new Thread(new Runnable() {
            @Override public void run() {
                checkLatestAndNotify(app);
            }
        }).start();
    }

    private void checkLatestAndNotify(Context context) {
        try {
            String json = downloadString(LATEST_RELEASE_API);
            JSONObject obj = new JSONObject(json);
            String tag = obj.optString("tag_name", "");
            String name = obj.optString("name", "");
            String url = obj.optString("html_url", LATEST_RELEASE_PAGE);
            String current = currentVersionName(context);
            if (!hasText(url)) url = LATEST_RELEASE_PAGE;
            if (releaseMatchesCurrent(current, tag, name)) return;

            String releaseKey = hasText(tag) ? tag : (hasText(name) ? name : url);
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String last = prefs.getString(KEY_LAST_NOTIFIED, "");
            if (releaseKey.equals(last)) return;

            postUpdateNotification(context, url, hasText(tag) ? tag : name);
            prefs.edit().putString(KEY_LAST_NOTIFIED, releaseKey).apply();
        } catch (Throwable ignored) { }
    }

    private void postUpdateNotification(Context context, String url, String releaseName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            createChannel(manager);

            Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    19660215,
                    open,
                    PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag()
            );

            String text = hasText(releaseName)
                    ? "Yeni sürüm hazır: " + releaseName
                    : "Yeni sürüm hazır.";

            Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? new Notification.Builder(context, CHANNEL_ID)
                    : new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Balkes Arşivi güncellemesi")
                    .setContentText("Yeni sürüm yayınlandı")
                    .setStyle(new Notification.BigTextStyle().bigText(text + " İndirmek için dokun."))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
            manager.notify(21415, builder.build());
        } catch (Throwable ignored) { }
    }

    private void createChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Balkes Arşivi Güncellemeleri",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Yeni Balkes Arşivi sürümü yayınlandığında bildirim gönderir.");
            manager.createNotificationChannel(channel);
        }
    }

    private String currentVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "";
        }
    }

    private boolean releaseMatchesCurrent(String currentVersion, String tag, String name) {
        String current = normalizeVersion(currentVersion);
        String t = normalizeVersion(tag);
        String n = normalizeVersion(name);
        return (hasText(t) && (t.equals(current) || t.contains(current) || current.contains(t))) ||
                (hasText(n) && (n.equals(current) || n.contains(current) || current.contains(n)));
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

    private String downloadString(String urlText) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlText).openConnection();
        conn.setConnectTimeout(9000);
        conn.setReadTimeout(12000);
        conn.setRequestProperty("User-Agent", "BalkesArsivi-Android-UpdateChecker");
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        in.close();
        return out.toString("UTF-8");
    }

    private static int immutableFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
