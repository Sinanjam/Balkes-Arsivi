package com.sinanjam.arsiv;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "balkes_arsivi_prefs";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_TEXT_SIZE = "text_size_sp";
    private static final String CHANNEL_ID = "balkes_arsivi_save_channel";
    private static final int REQUEST_WRITE_STORAGE = 2210;
    private static final int REQUEST_NOTIFICATIONS = 2211;

    private final ArrayList<ArchiveItem> archiveItems = new ArrayList<ArchiveItem>();
    private SharedPreferences prefs;
    private boolean darkTheme;
    private int textSizeSp;
    private String screen = "home";
    private ArchiveItem currentItem;
    private int currentPhotoIndex = 0;
    private boolean pendingPhotoSave;
    private String pendingNotificationLocation;

    private static class PhotoItem {
        String asset;
        String caption;
        String sourceUrl;
    }

    private static class ArchiveItem {
        String title;
        String season;
        String summary;
        String content;
        String sourceUrl;
        String imageAsset;
        String imageCaption;
        String tables;
        int tableCount;
        int imageCount;
        ArrayList<PhotoItem> photos = new ArrayList<PhotoItem>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkTheme = prefs.getBoolean(KEY_DARK, false);
        textSizeSp = prefs.getInt(KEY_TEXT_SIZE, 18);
        createNotificationChannel();
        loadArchiveItems();
        showHome();
    }

    private void showHome() {
        screen = "home";
        currentItem = null;
        currentPhotoIndex = 0;
        applyBars();

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(pageBackground());

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.home_background);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(background, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        View overlay = new View(this);
        overlay.setBackgroundColor(darkTheme ? Color.argb(130, 25, 0, 0) : Color.argb(112, 255, 255, 255));
        root.addView(overlay, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        ImageView signature = new ImageView(this);
        signature.setImageResource(R.drawable.sinanjam_signature);
        signature.setAdjustViewBounds(true);
        signature.setScaleType(ImageView.ScaleType.FIT_CENTER);
        signature.setBackgroundColor(Color.TRANSPARENT);
        FrameLayout.LayoutParams signatureParams = new FrameLayout.LayoutParams(dp(112), dp(42), Gravity.START | Gravity.TOP);
        signatureParams.setMargins(dp(12), dp(14), 0, 0);
        root.addView(signature, signatureParams);

        Button themeButton = smallTopButton(darkTheme ? "☀ Aydınlık" : "☾ Karanlık");
        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                darkTheme = !darkTheme;
                prefs.edit().putBoolean(KEY_DARK, darkTheme).apply();
                showHome();
            }
        });
        FrameLayout.LayoutParams themeParams = new FrameLayout.LayoutParams(dp(122), dp(42), Gravity.END | Gravity.TOP);
        themeParams.setMargins(0, dp(14), dp(12), 0);
        root.addView(themeButton, themeParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(84), dp(20), dp(28));

        ImageView banner = new ImageView(this);
        banner.setImageResource(R.drawable.balkes_1966_banner);
        banner.setAdjustViewBounds(true);
        banner.setScaleType(ImageView.ScaleType.FIT_CENTER);
        banner.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams bannerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(112));
        bannerParams.setMargins(0, 0, 0, dp(24));
        content.addView(banner, bannerParams);

        TextView title = makeHomeTitle("Balkes Arşivi");
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(18));
        content.addView(title, titleParams);

        TextView archiveCard = homeCard("Balkes Arşivi");
        archiveCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveList(); } });
        content.addView(archiveCard, homeCardParams());

        TextView aboutCard = homeCard("Uygulama Hakkında");
        aboutCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showAbout(); } });
        content.addView(aboutCard, homeCardParams());

        root.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private void showArchiveList() {
        screen = "archive_list";
        currentItem = null;
        currentPhotoIndex = 0;
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());

        LinearLayout root = pageRoot();
        scrollView.addView(root);
        root.addView(sectionHeader("Balkes Arşivi"));
        root.addView(descriptionText("Balıkesirspor sezon arşivleri. Normal ÖzBalkesler haberleri bu pakete dahil edilmedi."));

        for (int i = 0; i < archiveItems.size(); i++) {
            final ArchiveItem item = archiveItems.get(i);
            View row = listCard(item);
            row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
            root.addView(row, listCardParams());
        }

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

    private void showArchiveDetail(ArchiveItem item, int photoIndex) {
        screen = "archive_detail";
        currentItem = item;
        if (item.photos.size() == 0) currentPhotoIndex = 0;
        else currentPhotoIndex = Math.max(0, Math.min(photoIndex, item.photos.size() - 1));
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        scrollView.addView(root);

        Button back = wideButton("← Ana Ekrana Dön");
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(back, compactButtonParams());

        root.addView(sectionHeader(item.title));
        String meta = "";
        if (hasText(item.season)) meta += item.season;
        if (item.imageCount > 0) meta += (meta.length() > 0 ? "  •  " : "") + item.imageCount + " fotoğraf";
        if (item.tableCount > 0) meta += (meta.length() > 0 ? "  •  " : "") + item.tableCount + " tablo";
        if (hasText(meta)) root.addView(descriptionText(meta));

        addPhotoGallery(root, item);
        addTextSizeControls(root);

        TextView body = new TextView(this);
        body.setText(hasText(item.content) ? item.content : item.summary);
        body.setTextSize(textSizeSp);
        body.setLineSpacing(0, 1.22f);
        body.setTextColor(textColor());
        body.setPadding(dp(16), dp(16), dp(16), dp(16));
        body.setBackground(roundedBox(cardBackground(), accentColor(), dp(18), dp(1)));
        root.addView(body, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (hasText(item.tables)) {
            root.addView(sectionSubHeader("Tablolar"));
            HorizontalScrollView tableScroll = new HorizontalScrollView(this);
            tableScroll.setFillViewport(true);
            TextView tableText = new TextView(this);
            tableText.setText(item.tables);
            tableText.setTypeface(Typeface.MONOSPACE);
            tableText.setTextSize(Math.max(11, textSizeSp - 4));
            tableText.setTextColor(textColor());
            tableText.setLineSpacing(0, 1.08f);
            tableText.setPadding(dp(14), dp(14), dp(14), dp(14));
            tableText.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
            tableScroll.addView(tableText, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sp.setMargins(0, dp(8), 0, dp(16));
            root.addView(tableScroll, sp);
        }

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

    private void addPhotoGallery(LinearLayout root, final ArchiveItem item) {
        if (item.photos.size() == 0) {
            ImageView image = new ImageView(this);
            image.setImageResource(R.drawable.sample_photo);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            root.addView(image, imageParams());
            root.addView(descriptionText("Balkes Arşivi görseli. Kaydetmek için görsele uzun bas."));
            return;
        }

        final PhotoItem photo = item.photos.get(currentPhotoIndex);
        ImageView image = new ImageView(this);
        Bitmap bitmap = loadAssetBitmap(photo.asset);
        if (bitmap != null) image.setImageBitmap(bitmap);
        else image.setImageResource(R.drawable.sample_photo);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setAdjustViewBounds(false);
        image.setBackgroundColor(Color.TRANSPARENT);
        image.setContentDescription("Balkes Arşivi görseli");
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                askSavePhoto();
                return true;
            }
        });
        root.addView(image, imageParams());

        String caption = hasText(photo.caption) ? photo.caption : item.title;
        root.addView(descriptionText("Fotoğraf " + (currentPhotoIndex + 1) + "/" + item.photos.size() + "\n" + caption + "\nKaydetmek için görsele uzun bas."));

        if (item.photos.size() > 1) {
            LinearLayout nav = new LinearLayout(this);
            nav.setOrientation(LinearLayout.HORIZONTAL);
            nav.setGravity(Gravity.CENTER);
            Button prev = pillButton("‹ Önceki");
            Button next = pillButton("Sonraki ›");
            prev.setEnabled(currentPhotoIndex > 0);
            next.setEnabled(currentPhotoIndex < item.photos.size() - 1);
            prev.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, currentPhotoIndex - 1); } });
            next.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, currentPhotoIndex + 1); } });
            nav.addView(prev, pillParams());
            nav.addView(next, pillParams());
            root.addView(nav);
        }
    }

    private void addTextSizeControls(LinearLayout root) {
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(0, dp(8), 0, dp(10));
        Button decrease = pillButton("A−");
        Button current = pillButton("Yazı: " + textSizeSp);
        Button increase = pillButton("A+");
        decrease.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { changeTextSize(-1); } });
        increase.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { changeTextSize(1); } });
        controls.addView(decrease, pillParams());
        controls.addView(current, pillParams());
        controls.addView(increase, pillParams());
        root.addView(controls);
    }

    private void showAbout() {
        screen = "about";
        currentItem = null;
        currentPhotoIndex = 0;
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(root);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.balkes_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(190), dp(190));
        logoParams.setMargins(0, 0, 0, dp(18));
        root.addView(logo, logoParams);

        root.addView(sectionHeader("Uygulama Hakkında"));
        TextView about = new TextView(this);
        String html = "<b>Balkes Arşivi</b><br><br>" +
                "Kapatılan Balkes Arşivi projesinden kurtarılan verilerle yapılmış Balıkesirspor Arşivi.<br><br>" +
                "Bu final pakette veriler uygulama içinde yerel tutulur: metinler, tablolar ve görseller APK içine gömülüdür.<br><br>" +
                "Normal ÖzBalkesler haberleri ayrılmış, yalnızca Balıkesirspor sezon arşivi sayfaları eklenmiştir.<br><br>" +
                "Vibecoding'ten faydalanılmıştır.<br><br>" +
                "Github, kaynak kodu ve iletişim: <a href=\"https://github.com/Sinanjam/Balkes-Arsivi.git\">https://github.com/Sinanjam/Balkes-Arsivi.git</a>";
        Spanned text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        else text = Html.fromHtml(html);
        about.setText(text);
        about.setMovementMethod(LinkMovementMethod.getInstance());
        about.setLinkTextColor(accentColor());
        about.setTextColor(textColor());
        about.setTextSize(textSizeSp);
        about.setLineSpacing(0, 1.18f);
        about.setPadding(dp(16), dp(16), dp(16), dp(16));
        about.setBackground(roundedBox(cardBackground(), accentColor(), dp(18), dp(1)));
        root.addView(about, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

    private void askSavePhoto() {
        new AlertDialog.Builder(this)
                .setTitle("Görsel kaydedilsin mi?")
                .setMessage("Bu görseli cihaz galerisine kaydetmek istiyor musunuz?")
                .setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) { savePhotoWithPermissionCheck(); }
                })
                .setNegativeButton("Hayır", null)
                .show();
    }

    private void savePhotoWithPermissionCheck() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingPhotoSave = true;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            return;
        }
        savePhotoNow();
    }

    private void savePhotoNow() {
        try {
            String location = writePhotoToGallery();
            Toast.makeText(this, "Görsel kaydedildi: " + location, Toast.LENGTH_LONG).show();
            showSavedNotification(location);
        } catch (Exception e) {
            Toast.makeText(this, "Görsel kaydedilemedi.", Toast.LENGTH_LONG).show();
        }
    }

    private String writePhotoToGallery() throws Exception {
        Bitmap bitmap = currentBitmapForSave();
        if (bitmap == null) throw new Exception("Görsel okunamadı");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "balkes_arsivi_" + stamp + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BalkesArsivi");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri item = resolver.insert(collection, values);
            if (item == null) throw new Exception("Konum açılamadı");
            OutputStream out = resolver.openOutputStream(item);
            if (out == null) throw new Exception("Dosya yazılamadı");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.close();
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(item, values, null, null);
            return "Pictures/BalkesArsivi/" + fileName;
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BalkesArsivi");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception("Klasör oluşturulamadı");
            File file = new File(dir, fileName);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.flush();
            out.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            return file.getAbsolutePath();
        }
    }

    private Bitmap currentBitmapForSave() {
        if (currentItem != null && currentItem.photos.size() > 0 && currentPhotoIndex >= 0 && currentPhotoIndex < currentItem.photos.size()) {
            Bitmap bitmap = loadAssetBitmap(currentItem.photos.get(currentPhotoIndex).asset);
            if (bitmap != null) return bitmap;
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.sample_photo);
    }

    private void showSavedNotification(String location) {
        pendingNotificationLocation = location;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
            return;
        }
        postSavedNotification(location);
    }

    private void postSavedNotification(String location) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Balkes Arşivi")
                .setContentText("Görsel kaydedildi")
                .setStyle(new Notification.BigTextStyle().bigText("Kaydedilen konum: " + location))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        manager.notify(1966, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Balkes Arşivi", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingPhotoSave) {
                pendingPhotoSave = false;
                savePhotoNow();
            } else {
                pendingPhotoSave = false;
                Toast.makeText(this, "Kaydetme izni verilmedi.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingNotificationLocation != null) {
                postSavedNotification(pendingNotificationLocation);
            }
            pendingNotificationLocation = null;
        }
    }

    private void changeTextSize(int delta) {
        textSizeSp = Math.max(13, Math.min(30, textSizeSp + delta));
        prefs.edit().putInt(KEY_TEXT_SIZE, textSizeSp).apply();
        if (currentItem != null) showArchiveDetail(currentItem, currentPhotoIndex); else showArchiveList();
    }

    @Override
    public void onBackPressed() {
        if ("home".equals(screen)) {
            new AlertDialog.Builder(this)
                    .setTitle("Çıkmak istiyor musunuz?")
                    .setMessage("Balkes Arşivi kapatılsın mı?")
                    .setPositiveButton("Evet", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface dialog, int which) { finish(); } })
                    .setNegativeButton("Hayır", null)
                    .show();
        } else {
            showHome();
        }
    }

    private void loadArchiveItems() {
        archiveItems.clear();
        try {
            String json = readAssetText("archive/archive_items.json");
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                ArchiveItem item = new ArchiveItem();
                item.title = o.optString("title");
                item.season = o.optString("season");
                item.summary = o.optString("summary");
                item.content = o.optString("content");
                item.sourceUrl = o.optString("sourceUrl");
                item.imageAsset = o.optString("imageAsset");
                item.imageCaption = o.optString("imageCaption");
                item.tables = o.optString("tables");
                item.tableCount = o.optInt("tableCount", 0);
                item.imageCount = o.optInt("imageCount", 0);
                JSONArray photos = o.optJSONArray("photos");
                if (photos != null) {
                    for (int p = 0; p < photos.length(); p++) {
                        JSONObject po = photos.getJSONObject(p);
                        PhotoItem photo = new PhotoItem();
                        photo.asset = po.optString("asset");
                        photo.caption = po.optString("caption");
                        photo.sourceUrl = po.optString("sourceUrl");
                        if (hasText(photo.asset)) item.photos.add(photo);
                    }
                }
                if (item.photos.size() == 0 && hasText(item.imageAsset)) {
                    PhotoItem photo = new PhotoItem();
                    photo.asset = item.imageAsset;
                    photo.caption = item.imageCaption;
                    item.photos.add(photo);
                }
                if (!hasText(item.title)) item.title = "Balkes Arşivi";
                if (!hasText(item.summary)) item.summary = item.content;
                if (!hasText(item.content)) item.content = item.summary;
                archiveItems.add(item);
            }
        } catch (Exception e) {
            ArchiveItem item = new ArchiveItem();
            item.title = "Balkes Arşivi";
            item.summary = "Balıkesirspor hafızasından seçilen arşiv kayıtları.";
            item.content = "Balıkesirspor hafızası; eski sezonlar, tribünler, fotoğraflar, maç günleri ve kırmızı-beyaz sevdanın ortak hatıralarından oluşur.";
            archiveItems.add(item);
        }
    }

    private String readAssetText(String path) throws Exception {
        InputStream in = getAssets().open(path);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        in.close();
        return out.toString("UTF-8");
    }

    private Bitmap loadAssetBitmap(String path) {
        if (!hasText(path)) return null;
        try {
            InputStream in = getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private View listCard(ArchiveItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
        card.setClickable(true);
        card.setFocusable(true);

        ImageView thumb = new ImageView(this);
        Bitmap bitmap = null;
        if (item.photos.size() > 0) bitmap = loadAssetBitmap(item.photos.get(0).asset);
        if (bitmap != null) thumb.setImageBitmap(bitmap);
        else thumb.setImageResource(R.drawable.sample_photo);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumb.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(dp(100), dp(82));
        thumbParams.setMargins(0, 0, dp(12), 0);
        card.addView(thumb, thumbParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(textColor());
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(16);
        title.setMaxLines(2);
        texts.addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        String meta = "";
        if (hasText(item.season)) meta += item.season;
        if (item.imageCount > 0) meta += (meta.length() > 0 ? " • " : "") + item.imageCount + " foto";
        if (item.tableCount > 0) meta += (meta.length() > 0 ? " • " : "") + item.tableCount + " tablo";
        if (hasText(meta)) {
            TextView season = new TextView(this);
            season.setText(meta);
            season.setTextColor(accentColor());
            season.setTextSize(13);
            season.setTypeface(Typeface.DEFAULT_BOLD);
            texts.addView(season, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        TextView snippet = new TextView(this);
        snippet.setText(makeSnippet(hasText(item.summary) ? item.summary : item.content, 125));
        snippet.setTextColor(secondaryTextColor());
        snippet.setTextSize(13);
        snippet.setLineSpacing(0, 1.08f);
        snippet.setMaxLines(3);
        texts.addView(snippet, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        card.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return card;
    }

    private LinearLayout pageRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(28));
        return root;
    }

    private TextView makeHomeTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextSize(30);
        view.setLetterSpacing(0.03f);
        view.setTextColor(darkTheme ? Color.WHITE : Color.rgb(125, 0, 0));
        view.setShadowLayer(darkTheme ? 8 : 0, 0, 2, Color.BLACK);
        return view;
    }

    private TextView homeCard(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(22);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextColor(textColor());
        view.setPadding(dp(18), dp(22), dp(18), dp(22));
        view.setBackground(roundedBox(cardBackground(), accentColor(), dp(22), dp(2)));
        view.setClickable(true);
        view.setFocusable(true);
        return view;
    }

    private TextView sectionHeader(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(accentColor());
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextSize(25);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, dp(8), 0, dp(12));
        return view;
    }

    private TextView sectionSubHeader(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(accentColor());
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextSize(20);
        view.setPadding(dp(4), dp(18), dp(4), dp(4));
        return view;
    }

    private TextView descriptionText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(secondaryTextColor());
        view.setTextSize(14);
        view.setLineSpacing(0, 1.12f);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(8), dp(4), dp(8), dp(10));
        return view;
    }

    private Button wideButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundedBox(accentColor(), accentColor(), dp(18), 0));
        return button;
    }

    private Button pillButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundedBox(accentColor(), accentColor(), dp(18), 0));
        return button;
    }

    private Button smallTopButton(String text) {
        Button button = pillButton(text);
        button.setTextSize(13);
        return button;
    }

    private LinearLayout.LayoutParams homeCardParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(16));
        return lp;
    }

    private LinearLayout.LayoutParams listCardParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        return lp;
    }

    private LinearLayout.LayoutParams wideButtonParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        lp.setMargins(0, dp(18), 0, 0);
        return lp;
    }

    private LinearLayout.LayoutParams compactButtonParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
        lp.setMargins(0, 0, 0, dp(12));
        return lp;
    }

    private LinearLayout.LayoutParams pillParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        return lp;
    }

    private LinearLayout.LayoutParams imageParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(252));
        lp.setMargins(0, dp(10), 0, dp(8));
        return lp;
    }

    private GradientDrawable roundedBox(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) drawable.setStroke(strokeWidth, stroke);
        return drawable;
    }

    private int accentColor() { return Color.rgb(178, 0, 0); }
    private int pageBackground() { return darkTheme ? Color.rgb(26, 18, 18) : Color.rgb(255, 248, 248); }
    private int cardBackground() { return darkTheme ? Color.rgb(45, 31, 31) : Color.WHITE; }
    private int textColor() { return darkTheme ? Color.WHITE : Color.rgb(28, 22, 22); }
    private int secondaryTextColor() { return darkTheme ? Color.rgb(224, 205, 205) : Color.rgb(92, 72, 72); }

    private void applyBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(darkTheme ? Color.rgb(65, 0, 0) : Color.rgb(178, 0, 0));
            getWindow().setNavigationBarColor(darkTheme ? Color.rgb(18, 0, 0) : Color.rgb(125, 0, 0));
        }
    }

    private boolean hasText(String text) { return text != null && text.trim().length() > 0; }

    private String makeSnippet(String text, int max) {
        if (!hasText(text)) return "";
        String s = text.replace('\n', ' ').replace('\r', ' ').trim();
        while (s.contains("  ")) s = s.replace("  ", " ");
        if (s.length() <= max) return s;
        return s.substring(0, max).trim() + "…";
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
