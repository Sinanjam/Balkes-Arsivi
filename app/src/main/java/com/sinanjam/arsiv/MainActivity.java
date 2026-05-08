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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "balkes_arsivi_prefs";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_TEXT_SIZE = "text_size_sp";
    private static final String KEY_REMOTE_JSON = "remote_archive_json";
    private static final String KEY_REMOTE_HASH = "remote_archive_hash";
    private static final String KEY_LAST_READ_ID = "last_read_id";
    private static final String KEY_FAVORITES = "favorite_ids";
    private static final String KEY_FAVORITE_PHOTOS = "favorite_photo_ids";
    private static final String KEY_LAST_UPDATE_TEXT = "last_update_text";
    private static final String KEY_READER_MODE = "reader_mode";
    private static final String CHANNEL_ID = "balkes_arsivi_save_channel";
    private static final int REQUEST_WRITE_STORAGE = 2210;
    private static final int REQUEST_NOTIFICATIONS = 2211;
    private static final String GITHUB_RAW_BASE = "https://raw.githubusercontent.com/Sinanjam/Balkes-Arsivi/main/app/src/main/assets/";
    private static final String REMOTE_ARCHIVE_URL = GITHUB_RAW_BASE + "archive/archive_items.json";

    private final ArrayList<ArchiveItem> archiveItems = new ArrayList<ArchiveItem>();
    private final ArrayList<AlbumPhoto> albumPhotos = new ArrayList<AlbumPhoto>();
    private SharedPreferences prefs;
    private Handler handler;
    private boolean darkTheme;
    private int textSizeSp;
    private String screen = "home";
    private String currentQuery = "";
    private ArchiveItem currentItem;
    private int currentPhotoIndex = 0;
    private int currentAlbumIndex = 0;
    private boolean pendingPhotoSave;
    private String pendingNotificationLocation;

    private static class PhotoItem {
        String asset;
        String caption;
        String sourceUrl;
    }

    private static class AlbumPhoto {
        ArchiveItem item;
        PhotoItem photo;
        int photoIndex;
    }

    private static class ArchiveItem {
        String id;
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
        handler = new Handler(getMainLooper());
        darkTheme = prefs.getBoolean(KEY_DARK, false);
        textSizeSp = prefs.getInt(KEY_TEXT_SIZE, 18);
        createNotificationChannel();
        loadArchiveItems();
        showHome();
        checkForGithubUpdates(false);
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
        overlay.setBackgroundColor(darkTheme ? Color.argb(132, 25, 0, 0) : Color.argb(115, 255, 255, 255));
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

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(84), dp(20), dp(28));
        scroll.addView(content);

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
        archiveCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveList(""); } });
        content.addView(archiveCard, homeCardParams());

        TextView albumCard = homeCard("Fotoğraf Albümü");
        albumCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showPhotoAlbum(0); } });
        content.addView(albumCard, homeCardParams());

        TextView favoriteCard = homeCard("Favoriler");
        favoriteCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showFavoritesList(); } });
        content.addView(favoriteCard, homeCardParams());

        TextView updateInfo = descriptionText(updateStatusText());
        LinearLayout.LayoutParams updateInfoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        updateInfoParams.setMargins(0, 0, 0, dp(8));
        content.addView(updateInfo, updateInfoParams);

        TextView aboutCard = homeCard("Uygulama Hakkında");
        aboutCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showAbout(); } });
        content.addView(aboutCard, homeCardParams());

        root.addView(scroll, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        signature.bringToFront();
        themeButton.bringToFront();
        setContentView(root);
    }

    private void showArchiveList(String query) {
        screen = "archive_list";
        currentItem = null;
        currentPhotoIndex = 0;
        currentQuery = query == null ? "" : query;
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        scrollView.addView(root);

        root.addView(sectionHeader("Balkes Arşivi"));
        root.addView(descriptionText(updateStatusText()));

        Button update = wideButton("Güncellemeyi Kontrol Et");
        update.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { checkForGithubUpdates(true); } });
        root.addView(update, compactButtonParams());

        final LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        addSearchArea(root, currentQuery, results);
        root.addView(results, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        populateArchiveResults(results, false);

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

    private void showFavoritesList() {
        screen = "favorites";
        currentQuery = "";
        currentItem = null;
        currentPhotoIndex = 0;
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        scrollView.addView(root);
        root.addView(sectionHeader("Favoriler"));

        ArrayList<ArchiveItem> favorites = filterItems("", true);
        root.addView(sectionSubHeader("Favori Yazılar"));
        if (favorites.size() == 0) {
            root.addView(emptyState("Henüz favori yazı yok", "Sevdiğin sezon arşivlerini yıldızla işaretleyebilirsin."));
        } else {
            for (int i = 0; i < favorites.size(); i++) {
                final ArchiveItem item = favorites.get(i);
                View row = listCard(item);
                row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
                root.addView(row, listCardParams());
            }
        }

        root.addView(sectionSubHeader("Favori Fotoğraflar"));
        ArrayList<AlbumPhoto> favPhotos = favoriteAlbumPhotos();
        if (favPhotos.size() == 0) {
            root.addView(emptyState("Henüz favori fotoğraf yok", "Fotoğraf ekranında kalp işaretine basarak albümünü oluşturabilirsin."));
        } else {
            for (int i = 0; i < favPhotos.size(); i++) {
                final AlbumPhoto ap = favPhotos.get(i);
                View row = photoFavoriteCard(ap);
                row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(ap.item, ap.photoIndex); } });
                root.addView(row, listCardParams());
            }
        }

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

    private void showPhotoAlbum(int index) {
        screen = "album";
        applyBars();
        if (albumPhotos.size() == 0) {
            Toast.makeText(this, "Albümde fotoğraf yok.", Toast.LENGTH_LONG).show();
            showHome();
            return;
        }
        currentAlbumIndex = Math.max(0, Math.min(index, albumPhotos.size() - 1));
        final AlbumPhoto albumPhoto = albumPhotos.get(currentAlbumIndex);
        currentItem = albumPhoto.item;
        currentPhotoIndex = albumPhoto.photoIndex;

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        scrollView.addView(root);

        Button home = wideButton("← Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, compactButtonParams());

        root.addView(sectionHeader("Fotoğraf Albümü"));

        ZoomableImageView image = new ZoomableImageView(this);
        setImageFromPath(image, albumPhoto.photo.asset, R.drawable.sample_photo);
        image.setAdjustViewBounds(false);
        image.setContentDescription("Balkes Arşivi fotoğrafı");
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                askSavePhoto();
                return true;
            }
        });
        root.addView(image, imageParams());

        root.addView(descriptionText("Fotoğraf " + (currentAlbumIndex + 1) + "/" + albumPhotos.size() + "  •  " + albumPhoto.item.title));
        addPhotoActionRow(root, albumPhoto.photo);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        Button prev = pillButton("‹ Önceki");
        Button open = pillButton("Yazıyı Aç");
        Button next = pillButton("Sonraki ›");
        prev.setEnabled(currentAlbumIndex > 0);
        next.setEnabled(currentAlbumIndex < albumPhotos.size() - 1);
        prev.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showPhotoAlbum(currentAlbumIndex - 1); } });
        open.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(albumPhoto.item, albumPhoto.photoIndex); } });
        next.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showPhotoAlbum(currentAlbumIndex + 1); } });
        nav.addView(prev, pillParams());
        nav.addView(open, pillParams());
        nav.addView(next, pillParams());
        root.addView(nav);

        setContentView(scrollView);
    }

    private void addSearchArea(LinearLayout root, String query, final LinearLayout results) {
        final EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setText(query == null ? "" : query);
        search.setHint("Yazdıkça ara: sezon, takım, futbolcu, skor");
        search.setTextColor(textColor());
        search.setHintTextColor(secondaryTextColor());
        search.setTextSize(16);
        search.setPadding(dp(14), 0, dp(14), 0);
        search.setBackground(roundedBox(cardBackground(), subtleStrokeColor(), dp(18), dp(1)));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        sp.setMargins(0, dp(8), 0, dp(12));
        root.addView(search, sp);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable editable) {
                currentQuery = editable == null ? "" : editable.toString();
                populateArchiveResults(results, false);
            }
        });
    }

    private void populateArchiveResults(LinearLayout results, boolean favoritesOnly) {
        results.removeAllViews();
        ArrayList<ArchiveItem> filtered = filterItems(currentQuery, favoritesOnly);
        if (filtered.size() == 0) {
            results.addView(emptyState(hasText(currentQuery) ? "Sonuç bulunamadı" : "Arşiv kaydı bulunamadı", hasText(currentQuery) ? "Farklı bir sezon, futbolcu veya skor deneyin." : "Veri güncellemesi yaparak tekrar deneyin."));
            return;
        }
        TextView count = descriptionText(filtered.size() + " sonuç");
        results.addView(count);
        for (int i = 0; i < filtered.size(); i++) {
            final ArchiveItem item = filtered.get(i);
            View row = listCard(item);
            row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
            results.addView(row, listCardParams());
        }
    }

    private void showArchiveDetail(final ArchiveItem item, int photoIndex) {
        screen = "archive_detail";
        currentItem = item;
        if (item.photos.size() == 0) currentPhotoIndex = 0;
        else currentPhotoIndex = Math.max(0, Math.min(photoIndex, item.photos.size() - 1));
        prefs.edit().putString(KEY_LAST_READ_ID, item.id).apply();
        applyBars();

        final ScrollView scrollView = new ScrollView(this);
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

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        final Button favorite = pillButton(isFavorite(item.id) ? "★ Yazı" : "☆ Yazı");
        Button album = pillButton("Albüm");
        final Button readerToggle = pillButton(readerMode() ? "Tam Görünüm" : "Sade Okuma");
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleFavorite(item.id);
                favorite.setText(isFavorite(item.id) ? "★ Yazı" : "☆ Yazı");
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showPhotoAlbum(albumIndexFor(item, currentPhotoIndex)); }
        });
        readerToggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                prefs.edit().putBoolean(KEY_READER_MODE, !readerMode()).apply();
                showArchiveDetail(item, currentPhotoIndex);
            }
        });
        actionRow.addView(favorite, pillParams());
        actionRow.addView(album, pillParams());
        actionRow.addView(readerToggle, pillParams());
        root.addView(actionRow);

        if (!readerMode()) addPhotoGallery(root, item);
        addTextSizeControls(root);

        TextView body = new TextView(this);
        body.setText(cleanReaderText(hasText(item.content) ? item.content : item.summary));
        body.setTextSize(textSizeSp);
        body.setLineSpacing(0, 1.22f);
        body.setTextColor(textColor());
        body.setPadding(dp(16), dp(16), dp(16), dp(16));
        body.setBackground(roundedBox(cardBackground(), accentColor(), dp(18), dp(1)));
        root.addView(body, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (!readerMode()) addPrettyTables(root, item.tables);

        Button top = wideButton("Başa Dön");
        top.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { scrollView.smoothScrollTo(0, 0); } });
        root.addView(top, compactButtonParams());

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);

        final String scrollKey = "scroll_" + item.id;
        scrollView.post(new Runnable() {
            @Override public void run() { scrollView.scrollTo(0, prefs.getInt(scrollKey, 0)); }
        });
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                prefs.edit().putInt(scrollKey, scrollY).apply();
            }
        });
    }

    private void addPhotoGallery(LinearLayout root, final ArchiveItem item) {
        if (item.photos.size() == 0) {
            ZoomableImageView image = new ZoomableImageView(this);
            image.setImageResource(R.drawable.sample_photo);
            root.addView(image, imageParams());
            return;
        }

        final PhotoItem photo = item.photos.get(currentPhotoIndex);
        ZoomableImageView image = new ZoomableImageView(this);
        setImageFromPath(image, photo.asset, R.drawable.sample_photo);
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

        root.addView(descriptionText("Fotoğraf " + (currentPhotoIndex + 1) + "/" + item.photos.size() + "  •  yakınlaştırmak için çift parmak kullan"));
        addPhotoActionRow(root, photo);

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

    private void addPhotoActionRow(LinearLayout root, final PhotoItem photo) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        final Button favPhoto = pillButton(isPhotoFavorite(photo.asset) ? "♥ Foto" : "♡ Foto");
        Button share = pillButton("Görseli Paylaş");
        favPhoto.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                togglePhotoFavorite(photo.asset);
                favPhoto.setText(isPhotoFavorite(photo.asset) ? "♥ Foto" : "♡ Foto");
            }
        });
        share.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { shareCurrentPhoto(); } });
        row.addView(favPhoto, pillParams());
        row.addView(share, pillParams());
        root.addView(row);
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

    private void addPrettyTables(LinearLayout root, String markdown) {
        if (!hasText(markdown)) return;
        root.addView(sectionSubHeader("Tablolar"));
        String[] chunks = markdown.split("(?=Tablo \\d+)");
        int shown = 0;
        for (int c = 0; c < chunks.length; c++) {
            String chunk = chunks[c].trim();
            if (!hasText(chunk) || chunk.indexOf('|') < 0) continue;
            shown++;
            String title = "Tablo " + shown;
            String firstLine = chunk.split("\\n", 2)[0].trim();
            if (firstLine.startsWith("Tablo")) title = firstLine;
            root.addView(tableTitle(title));

            HorizontalScrollView hsv = new HorizontalScrollView(this);
            hsv.setFillViewport(true);
            LinearLayout table = new LinearLayout(this);
            table.setOrientation(LinearLayout.VERTICAL);
            table.setPadding(dp(4), dp(4), dp(4), dp(4));
            table.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
            hsv.addView(table, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            String[] lines = chunk.split("\\n");
            boolean headerPainted = false;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("|") || line.indexOf("---") >= 0) continue;
                ArrayList<String> cells = parseMarkdownRow(line);
                if (cells.size() == 0 || rowIsEmpty(cells)) continue;
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                boolean header = !headerPainted;
                int rowFill = header ? accentColor() : (table.getChildCount() % 2 == 0 ? cardBackground() : softTableAltColor());
                for (int j = 0; j < cells.size(); j++) {
                    TextView cell = new TextView(this);
                    cell.setText(cells.get(j));
                    cell.setTextSize(Math.max(12, textSizeSp - 4));
                    cell.setTextColor(header ? Color.WHITE : textColor());
                    cell.setTypeface(header ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    cell.setGravity(Gravity.CENTER_VERTICAL);
                    cell.setMinWidth(j == 1 ? dp(142) : dp(72));
                    cell.setPadding(dp(8), dp(7), dp(8), dp(7));
                    cell.setBackground(roundedBox(rowFill, header ? accentColor() : subtleStrokeColor(), dp(6), dp(1)));
                    LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    cp.setMargins(dp(2), dp(2), dp(2), dp(2));
                    row.addView(cell, cp);
                }
                table.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                headerPainted = true;
            }
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sp.setMargins(0, dp(8), 0, dp(16));
            root.addView(hsv, sp);
        }
    }

    private ArrayList<String> parseMarkdownRow(String line) {
        ArrayList<String> out = new ArrayList<String>();
        String s = line.trim();
        if (s.startsWith("|")) s = s.substring(1);
        if (s.endsWith("|")) s = s.substring(0, s.length() - 1);
        String[] parts = s.split("\\|", -1);
        for (int i = 0; i < parts.length; i++) out.add(parts[i].trim());
        return out;
    }

    private boolean rowIsEmpty(ArrayList<String> cells) {
        for (int i = 0; i < cells.size(); i++) if (hasText(cells.get(i))) return false;
        return true;
    }

    private TextView tableTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(accentColor());
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextSize(16);
        view.setPadding(dp(4), dp(10), dp(4), dp(2));
        return view;
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
            String path = currentItem.photos.get(currentPhotoIndex).asset;
            Bitmap bitmap = loadAssetBitmap(path);
            if (bitmap != null) return bitmap;
            bitmap = loadCachedBitmap(path);
            if (bitmap != null) return bitmap;
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.sample_photo);
    }

    private void shareCurrentPhoto() {
        try {
            Bitmap bitmap = currentBitmapForSave();
            if (bitmap == null) throw new Exception("Görsel okunamadı");
            File dir = new File(getCacheDir(), "share");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "balkes_arsivi_paylas.jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out);
            out.flush();
            out.close();
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Görseli Paylaş"));
        } catch (Exception e) {
            Toast.makeText(this, "Görsel paylaşılamadı.", Toast.LENGTH_LONG).show();
        }
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
        if (currentItem != null) showArchiveDetail(currentItem, currentPhotoIndex);
        else if ("archive_list".equals(screen)) showArchiveList(currentQuery);
        else showHome();
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


    private void showLoadingPage(String title, String subtitle) {
        screen = "loading";
        applyBars();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
        LinearLayout root = pageRoot();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(root);
        root.addView(sectionHeader(title));
        root.addView(descriptionText(subtitle));
        root.addView(skeletonBlock(dp(72)));
        root.addView(skeletonBlock(dp(112)));
        root.addView(skeletonBlock(dp(72)));
        setContentView(scrollView);
    }

    private View skeletonBlock(int height) {
        View v = new View(this);
        v.setBackground(roundedBox(darkTheme ? Color.rgb(58, 38, 38) : Color.rgb(255, 235, 235), subtleStrokeColor(), dp(18), dp(1)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(0, dp(8), 0, dp(8));
        v.setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) v.setElevation(dp(2));
        return v;
    }

    private View emptyState(String title, String subtitle) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(16), dp(18), dp(16), dp(18));
        box.setBackground(roundedBox(cardBackground(), subtleStrokeColor(), dp(20), dp(1)));
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(textColor());
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(17);
        t.setGravity(Gravity.CENTER);
        box.addView(t, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView s = descriptionText(subtitle);
        box.addView(s, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return box;
    }

    private String updateStatusText() {
        String last = prefs.getString(KEY_LAST_UPDATE_TEXT, "");
        if (!hasText(last)) return "Sürüm 1.7 Kırmızı Beyaz Hafıza • yerel arşiv hazır";
        return "Sürüm 1.7 Kırmızı Beyaz Hafıza • son güncelleme: " + last;
    }

    private String nowText() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", new Locale("tr", "TR")).format(new Date());
    }

    private boolean readerMode() {
        return prefs.getBoolean(KEY_READER_MODE, false);
    }

    private void loadArchiveItems() {
        archiveItems.clear();
        boolean loaded = false;
        String cached = prefs.getString(KEY_REMOTE_JSON, "");
        if (hasText(cached)) loaded = parseArchiveItems(cached);
        if (!loaded) {
            try {
                loaded = parseArchiveItems(readAssetText("archive/archive_items.json"));
            } catch (Exception ignored) { }
        }
        if (!loaded) {
            ArchiveItem item = new ArchiveItem();
            item.id = "balkes_arsivi";
            item.title = "Balkes Arşivi";
            item.summary = "Balıkesirspor arşivi.";
            item.content = "Balıkesirspor arşivi.";
            archiveItems.add(item);
        }
        collectAlbumPhotos();
    }

    private boolean parseArchiveItems(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.getJSONArray("items");
            ArrayList<ArchiveItem> parsed = new ArrayList<ArchiveItem>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                ArchiveItem item = new ArchiveItem();
                item.id = o.optString("id");
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
                        photo.caption = cleanReaderText(po.optString("caption"));
                        photo.sourceUrl = po.optString("sourceUrl");
                        if (hasText(photo.asset)) item.photos.add(photo);
                    }
                }
                if (item.photos.size() == 0 && hasText(item.imageAsset)) {
                    PhotoItem photo = new PhotoItem();
                    photo.asset = item.imageAsset;
                    photo.caption = cleanReaderText(item.imageCaption);
                    item.photos.add(photo);
                }
                if (!hasText(item.id)) item.id = safeId(item.title + "_" + i);
                if (!hasText(item.title)) item.title = "Balkes Arşivi";
                if (!hasText(item.summary)) item.summary = item.content;
                if (!hasText(item.content)) item.content = item.summary;
                parsed.add(item);
            }
            if (parsed.size() == 0) return false;
            archiveItems.clear();
            archiveItems.addAll(parsed);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkForGithubUpdates(final boolean manual) {
        if (manual) showLoadingPage("GitHub arşivi kontrol ediliyor", "Yeni metin ve fotoğraf verileri taranıyor...");
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    final String remote = downloadString(REMOTE_ARCHIVE_URL);
                    if (!hasText(remote) || !remote.contains("\"items\"")) throw new Exception("Veri okunamadı");
                    final String hash = sha1(remote);
                    String old = prefs.getString(KEY_REMOTE_HASH, "");
                    if (!hash.equals(old)) {
                        JSONObject test = new JSONObject(remote);
                        test.getJSONArray("items");
                        prefs.edit().putString(KEY_REMOTE_JSON, remote).putString(KEY_REMOTE_HASH, hash).putString(KEY_LAST_UPDATE_TEXT, nowText()).apply();
                        handler.post(new Runnable() {
                            @Override public void run() {
                                loadArchiveItems();
                                Toast.makeText(MainActivity.this, "Arşiv güncellendi.", Toast.LENGTH_LONG).show();
                                if (manual) showArchiveList(currentQuery);
                                else if ("archive_list".equals(screen)) showArchiveList(currentQuery);
                                else if ("favorites".equals(screen)) showFavoritesList();
                                else if ("home".equals(screen)) showHome();
                            }
                        });
                    } else if (manual) {
                        handler.post(new Runnable() { @Override public void run() { Toast.makeText(MainActivity.this, "Arşiv güncel.", Toast.LENGTH_LONG).show(); showArchiveList(currentQuery); } });
                    }
                } catch (Exception e) {
                    if (manual) handler.post(new Runnable() { @Override public void run() { Toast.makeText(MainActivity.this, "GitHub verisi alınamadı.", Toast.LENGTH_LONG).show(); showArchiveList(currentQuery); } });
                }
            }
        }).start();
    }

    private String downloadString(String urlText) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlText).openConnection();
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("User-Agent", "BalkesArsivi-Android");
        InputStream in = conn.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
        in.close();
        return out.toString("UTF-8");
    }

    private void collectAlbumPhotos() {
        albumPhotos.clear();
        for (int i = 0; i < archiveItems.size(); i++) {
            ArchiveItem item = archiveItems.get(i);
            for (int p = 0; p < item.photos.size(); p++) {
                AlbumPhoto ap = new AlbumPhoto();
                ap.item = item;
                ap.photo = item.photos.get(p);
                ap.photoIndex = p;
                albumPhotos.add(ap);
            }
        }
    }

    private ArrayList<ArchiveItem> filterItems(String query, boolean favoritesOnly) {
        ArrayList<ArchiveItem> out = new ArrayList<ArchiveItem>();
        String q = query == null ? "" : query.trim().toLowerCase(new Locale("tr", "TR"));
        for (int i = 0; i < archiveItems.size(); i++) {
            ArchiveItem item = archiveItems.get(i);
            if (favoritesOnly && !isFavorite(item.id)) continue;
            if (!hasText(q) || searchableText(item).contains(q)) out.add(item);
        }
        return out;
    }

    private String searchableText(ArchiveItem item) {
        return ((item.title == null ? "" : item.title) + " " +
                (item.season == null ? "" : item.season) + " " +
                (item.summary == null ? "" : item.summary) + " " +
                (item.content == null ? "" : item.content)).toLowerCase(new Locale("tr", "TR"));
    }

    private boolean isFavorite(String id) {
        return favoriteSet().contains(id);
    }

    private void toggleFavorite(String id) {
        Set<String> set = favoriteSet();
        if (set.contains(id)) set.remove(id); else set.add(id);
        prefs.edit().putString(KEY_FAVORITES, joinSet(set)).apply();
    }

    private Set<String> favoriteSet() {
        HashSet<String> set = new HashSet<String>();
        String raw = prefs.getString(KEY_FAVORITES, "");
        if (!hasText(raw)) return set;
        String[] parts = raw.split("\\|");
        for (int i = 0; i < parts.length; i++) if (hasText(parts[i])) set.add(parts[i]);
        return set;
    }

    private String joinSet(Set<String> set) {
        StringBuilder b = new StringBuilder();
        for (String s : set) {
            if (b.length() > 0) b.append('|');
            b.append(s);
        }
        return b.toString();
    }


    private boolean isPhotoFavorite(String photoId) {
        return photoFavoriteSet().contains(photoId);
    }

    private void togglePhotoFavorite(String photoId) {
        if (!hasText(photoId)) return;
        Set<String> set = photoFavoriteSet();
        if (set.contains(photoId)) set.remove(photoId); else set.add(photoId);
        prefs.edit().putString(KEY_FAVORITE_PHOTOS, joinSet(set)).apply();
    }

    private Set<String> photoFavoriteSet() {
        HashSet<String> set = new HashSet<String>();
        String raw = prefs.getString(KEY_FAVORITE_PHOTOS, "");
        if (!hasText(raw)) return set;
        String[] parts = raw.split("\\|");
        for (int i = 0; i < parts.length; i++) if (hasText(parts[i])) set.add(parts[i]);
        return set;
    }

    private ArrayList<AlbumPhoto> favoriteAlbumPhotos() {
        ArrayList<AlbumPhoto> out = new ArrayList<AlbumPhoto>();
        Set<String> favs = photoFavoriteSet();
        for (int i = 0; i < albumPhotos.size(); i++) {
            AlbumPhoto ap = albumPhotos.get(i);
            if (ap.photo != null && favs.contains(ap.photo.asset)) out.add(ap);
        }
        return out;
    }

    private ArchiveItem findById(String id) {
        if (!hasText(id)) return null;
        for (int i = 0; i < archiveItems.size(); i++) if (id.equals(archiveItems.get(i).id)) return archiveItems.get(i);
        return null;
    }

    private int albumIndexFor(ArchiveItem item, int photoIndex) {
        for (int i = 0; i < albumPhotos.size(); i++) {
            AlbumPhoto ap = albumPhotos.get(i);
            if (ap.item == item && ap.photoIndex == photoIndex) return i;
        }
        return 0;
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

    private Bitmap loadCachedBitmap(String path) {
        try {
            File f = cacheFileFor(path);
            if (!f.exists()) return null;
            FileInputStream in = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private void setImageFromPath(final ImageView image, final String path, int fallback) {
        image.setTag(path == null ? "" : path);
        Bitmap bitmap = loadAssetBitmap(path);
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            return;
        }
        bitmap = loadCachedBitmap(path);
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            return;
        }
        image.setImageResource(fallback);
        if (!hasText(path)) return;
        new Thread(new Runnable() {
            @Override public void run() {
                final Bitmap downloaded = downloadAndCacheBitmap(path);
                if (downloaded == null) return;
                handler.post(new Runnable() {
                    @Override public void run() {
                        Object tag = image.getTag();
                        if (tag != null && tag.toString().equals(path)) image.setImageBitmap(downloaded);
                    }
                });
            }
        }).start();
    }

    private Bitmap downloadAndCacheBitmap(String path) {
        try {
            String url = GITHUB_RAW_BASE + encodePath(path);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "BalkesArsivi-Android");
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            in.close();
            byte[] data = out.toByteArray();
            File f = cacheFileFor(path);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

    private File cacheFileFor(String path) throws Exception {
        File dir = new File(getFilesDir(), "github_media");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, sha1(path) + ".img");
    }

    private String encodePath(String path) throws Exception {
        String[] parts = path.split("/");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) b.append('/');
            b.append(URLEncoder.encode(parts[i], "UTF-8").replace("+", "%20"));
        }
        return b.toString();
    }

    private View photoFavoriteCard(AlbumPhoto ap) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(roundedBox(cardBackground(), accentColor(), dp(18), dp(1)));
        card.setClickable(true);
        card.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) card.setElevation(dp(2));

        ImageView thumb = new ImageView(this);
        setImageFromPath(thumb, ap.photo.asset, R.drawable.sample_photo);
        thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(dp(96), dp(76));
        thumbParams.setMargins(0, 0, dp(12), 0);
        card.addView(thumb, thumbParams);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("♥ " + ap.item.title);
        title.setTextColor(textColor());
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(15);
        title.setMaxLines(2);
        texts.addView(title);
        TextView meta = new TextView(this);
        meta.setText("Fotoğraf " + (ap.photoIndex + 1) + "/" + Math.max(1, ap.item.photos.size()));
        meta.setTextColor(accentColor());
        meta.setTypeface(Typeface.DEFAULT_BOLD);
        meta.setTextSize(13);
        texts.addView(meta);
        card.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return card;
    }

    private void setHighlightedText(TextView view, String text, String query) {
        if (!hasText(query)) {
            view.setText(text);
            return;
        }
        String q = query.trim().toLowerCase(new Locale("tr", "TR"));
        String lower = (text == null ? "" : text).toLowerCase(new Locale("tr", "TR"));
        int start = lower.indexOf(q);
        if (start < 0) {
            view.setText(text);
            return;
        }
        SpannableString span = new SpannableString(text);
        int end = Math.min(text.length(), start + q.length());
        span.setSpan(new BackgroundColorSpan(darkTheme ? Color.rgb(115, 0, 0) : Color.rgb(255, 225, 225)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(span);
    }

    private View listCard(ArchiveItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
        card.setClickable(true);
        card.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) card.setElevation(dp(2));

        ImageView thumb = new ImageView(this);
        if (item.photos.size() > 0) setImageFromPath(thumb, item.photos.get(0).asset, R.drawable.sample_photo);
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
        setHighlightedText(title, (isFavorite(item.id) ? "★ " : "") + item.title, currentQuery);
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
        setHighlightedText(snippet, makeSnippet(cleanReaderText(hasText(item.summary) ? item.summary : item.content), 125), currentQuery);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) view.setElevation(dp(4));
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
        button.setBackground(roundedBox(accentColor(), Color.rgb(110, 0, 0), dp(22), dp(1)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) button.setElevation(dp(3));
        return button;
    }

    private Button pillButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundedBox(accentColor(), Color.rgb(110, 0, 0), dp(22), dp(1)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) button.setElevation(dp(3));
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
    private int subtleStrokeColor() { return darkTheme ? Color.rgb(90, 60, 60) : Color.rgb(235, 210, 210); }
    private int softTableAltColor() { return darkTheme ? Color.rgb(54, 37, 37) : Color.rgb(255, 245, 245); }

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

    private String cleanReaderText(String text) {
        if (!hasText(text)) return "";
        String s = text;
        s = s.replace("Önemli Duyuru: Bu sayfada verilen bilgiler tamamen\nbalkesarsivi.com\nadresinden alınmıştır. Bu bilgilerin ve sayfanın kaybolmaması adına yedekleme amaçlı paylaşılmıştır. Hiç bir menfaat söz konusu değildir. Ancak bu yazının telif hakkının\nbalkesarsivi.com\nadresine ait olduğunu bilmenizde fayda var.\n", "");
        s = s.replace("Önemli Duyuru: Bu sayfada verilen bilgiler tamamen balkesarsivi.com adresinden alınmıştır. Bu bilgilerin ve sayfanın kaybolmaması adına yedekleme amaçlı paylaşılmıştır. Hiç bir menfaat söz konusu değildir. Ancak bu yazının telif hakkının balkesarsivi.com adresine ait olduğunu bilmenizde fayda var.", "");
        s = s.replaceAll("(?is)Önemli\\s+Duyuru:.*?bilmenizde\\s+fayda\\s+var\\.?", "");
        return s.trim();
    }

    private String safeId(String raw) {
        if (!hasText(raw)) return "item" + System.currentTimeMillis();
        String s = raw.toLowerCase(new Locale("tr", "TR"));
        s = s.replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u').replace('ş', 's').replace('ö', 'o').replace('ç', 'c');
        s = s.replace('İ', 'i').replace('Ğ', 'g').replace('Ü', 'u').replace('Ş', 's').replace('Ö', 'o').replace('Ç', 'c');
        s = s.replaceAll("[^a-z0-9]+", "_");
        if (s.length() > 80) s = s.substring(0, 80);
        return s;
    }

    private String sha1(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(text.getBytes("UTF-8"));
        StringBuilder b = new StringBuilder();
        for (byte by : digest) b.append(String.format(Locale.US, "%02x", by));
        return b.toString();
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    public class ZoomableImageView extends ImageView {
        private final Matrix matrix = new Matrix();
        private final ScaleGestureDetector detector;
        private float userScale = 1f;
        private float lastX = 0f;
        private float lastY = 0f;
        private boolean dragging = false;

        public ZoomableImageView(Context context) {
            super(context);
            setScaleType(ImageView.ScaleType.MATRIX);
            setBackgroundColor(Color.TRANSPARENT);
            detector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector d) {
                    float factor = d.getScaleFactor();
                    float next = Math.max(1f, Math.min(4.5f, userScale * factor));
                    factor = next / userScale;
                    userScale = next;
                    matrix.postScale(factor, factor, d.getFocusX(), d.getFocusY());
                    setImageMatrix(matrix);
                    return true;
                }
            });
        }

        @Override public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
            post(new Runnable() { @Override public void run() { resetBaseMatrix(); } });
        }

        @Override public void setImageResource(int resId) {
            super.setImageResource(resId);
            post(new Runnable() { @Override public void run() { resetBaseMatrix(); } });
        }

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            resetBaseMatrix();
        }

        private void resetBaseMatrix() {
            if (getDrawable() == null || getWidth() <= 0 || getHeight() <= 0) return;
            int dw = getDrawable().getIntrinsicWidth();
            int dh = getDrawable().getIntrinsicHeight();
            if (dw <= 0 || dh <= 0) return;
            float fit = Math.max((float) getWidth() / (float) dw, (float) getHeight() / (float) dh);
            float dx = (getWidth() - dw * fit) / 2f;
            float dy = (getHeight() - dh * fit) / 2f;
            matrix.reset();
            matrix.postScale(fit, fit);
            matrix.postTranslate(dx, dy);
            userScale = 1f;
            setImageMatrix(matrix);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            detector.onTouchEvent(event);
            if (event.getPointerCount() == 1 && userScale > 1f) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        dragging = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (dragging) {
                            float dx = event.getX() - lastX;
                            float dy = event.getY() - lastY;
                            matrix.postTranslate(dx, dy);
                            setImageMatrix(matrix);
                            lastX = event.getX();
                            lastY = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        dragging = false;
                        break;
                }
            }
            return true;
        }
    }
}
