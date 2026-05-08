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
<<<<<<< HEAD
=======
import android.os.Handler;
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
<<<<<<< HEAD
=======
import android.widget.EditText;
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
=======
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
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)

public class MainActivity extends Activity {
    private static final String PREFS = "balkes_arsivi_prefs";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_TEXT_SIZE = "text_size_sp";
<<<<<<< HEAD
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
=======
    private static final String KEY_REMOTE_JSON = "remote_archive_json";
    private static final String KEY_REMOTE_HASH = "remote_archive_hash";
    private static final String KEY_LAST_READ_ID = "last_read_id";
    private static final String KEY_FAVORITES = "favorite_ids";
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
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
    private boolean pendingPhotoSave;
    private String pendingNotificationLocation;

    private static class PhotoItem {
        String asset;
        String caption;
        String sourceUrl;
    }

<<<<<<< HEAD
    private static class ArchiveItem {
=======
    private static class AlbumPhoto {
        ArchiveItem item;
        PhotoItem photo;
        int photoIndex;
    }

    private static class ArchiveItem {
        String id;
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
=======
        handler = new Handler(getMainLooper());
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        darkTheme = prefs.getBoolean(KEY_DARK, false);
        textSizeSp = prefs.getInt(KEY_TEXT_SIZE, 18);
        createNotificationChannel();
        loadArchiveItems();
        showHome();
<<<<<<< HEAD
=======
        checkForGithubUpdates(false);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        overlay.setBackgroundColor(darkTheme ? Color.argb(130, 25, 0, 0) : Color.argb(112, 255, 255, 255));
=======
        overlay.setBackgroundColor(darkTheme ? Color.argb(132, 25, 0, 0) : Color.argb(115, 255, 255, 255));
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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

<<<<<<< HEAD
=======
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(84), dp(20), dp(28));
<<<<<<< HEAD
=======
        scroll.addView(content);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)

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
<<<<<<< HEAD
        archiveCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveList(); } });
        content.addView(archiveCard, homeCardParams());

=======
        archiveCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveList(""); } });
        content.addView(archiveCard, homeCardParams());

        TextView albumCard = homeCard("Fotoğraf Albümü");
        albumCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showPhotoAlbum(0); } });
        content.addView(albumCard, homeCardParams());

        TextView favoriteCard = homeCard("Favoriler");
        favoriteCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showFavoritesList(); } });
        content.addView(favoriteCard, homeCardParams());

        ArchiveItem last = findById(prefs.getString(KEY_LAST_READ_ID, ""));
        if (last != null) {
            TextView lastCard = homeCard("Son Okunan");
            lastCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(last, 0); } });
            content.addView(lastCard, homeCardParams());
        }

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        TextView aboutCard = homeCard("Uygulama Hakkında");
        aboutCard.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showAbout(); } });
        content.addView(aboutCard, homeCardParams());

<<<<<<< HEAD
        root.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private void showArchiveList() {
        screen = "archive_list";
        currentItem = null;
        currentPhotoIndex = 0;
=======
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
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        applyBars();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(pageBackground());
<<<<<<< HEAD

        LinearLayout root = pageRoot();
        scrollView.addView(root);
        root.addView(sectionHeader("Balkes Arşivi"));
        root.addView(descriptionText("Balıkesirspor sezon arşivleri. Normal ÖzBalkesler haberleri bu pakete dahil edilmedi."));

        for (int i = 0; i < archiveItems.size(); i++) {
            final ArchiveItem item = archiveItems.get(i);
            View row = listCard(item);
            row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
            root.addView(row, listCardParams());
=======
        LinearLayout root = pageRoot();
        scrollView.addView(root);

        root.addView(sectionHeader("Balkes Arşivi"));
        addSearchArea(root, currentQuery, false);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        Button update = pillButton("GitHub’dan Güncelle");
        update.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { checkForGithubUpdates(true); } });
        Button favs = pillButton("Favoriler");
        favs.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showFavoritesList(); } });
        actions.addView(update, pillParams());
        actions.addView(favs, pillParams());
        root.addView(actions);

        ArrayList<ArchiveItem> filtered = filterItems(currentQuery, false);
        if (filtered.size() == 0) {
            root.addView(descriptionText("Sonuç bulunamadı."));
        } else {
            for (int i = 0; i < filtered.size(); i++) {
                final ArchiveItem item = filtered.get(i);
                View row = listCard(item);
                row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
                root.addView(row, listCardParams());
            }
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        }

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
    }

<<<<<<< HEAD
    private void showArchiveDetail(ArchiveItem item, int photoIndex) {
=======
    private void showFavoritesList() {
        screen = "favorites";
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
        if (favorites.size() == 0) {
            root.addView(descriptionText("Henüz favori eklenmedi."));
        } else {
            for (int i = 0; i < favorites.size(); i++) {
                final ArchiveItem item = favorites.get(i);
                View row = listCard(item);
                row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveDetail(item, 0); } });
                root.addView(row, listCardParams());
            }
        }

        Button archive = wideButton("Arşive Dön");
        archive.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showArchiveList(currentQuery); } });
        root.addView(archive, compactButtonParams());
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

        ImageView image = new ImageView(this);
        setImageFromPath(image, albumPhoto.photo.asset, R.drawable.sample_photo);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setAdjustViewBounds(false);
        image.setContentDescription("Balkes Arşivi fotoğrafı");
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                askSavePhoto();
                return true;
            }
        });
        root.addView(image, imageParams());

        String caption = hasText(albumPhoto.photo.caption) ? albumPhoto.photo.caption : albumPhoto.item.title;
        root.addView(descriptionText("Fotoğraf " + (currentAlbumIndex + 1) + "/" + albumPhotos.size() + "\n" + albumPhoto.item.title + "\n" + caption));

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

    private void addSearchArea(LinearLayout root, String query, final boolean favoritesOnly) {
        final EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setText(query == null ? "" : query);
        search.setHint("Sezon, takım, futbolcu, skor ara");
        search.setTextColor(textColor());
        search.setHintTextColor(secondaryTextColor());
        search.setTextSize(16);
        search.setPadding(dp(14), 0, dp(14), 0);
        search.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
        root.addView(search, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        Button ara = pillButton("Ara");
        Button temizle = pillButton("Temizle");
        ara.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (favoritesOnly) showFavoritesList();
                else showArchiveList(search.getText().toString());
            }
        });
        temizle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (favoritesOnly) showFavoritesList();
                else showArchiveList("");
            }
        });
        buttons.addView(ara, pillParams());
        buttons.addView(temizle, pillParams());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, dp(8), 0, dp(10));
        root.addView(buttons, bp);
    }

    private void showArchiveDetail(final ArchiveItem item, int photoIndex) {
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        screen = "archive_detail";
        currentItem = item;
        if (item.photos.size() == 0) currentPhotoIndex = 0;
        else currentPhotoIndex = Math.max(0, Math.min(photoIndex, item.photos.size() - 1));
<<<<<<< HEAD
        applyBars();

        ScrollView scrollView = new ScrollView(this);
=======
        prefs.edit().putString(KEY_LAST_READ_ID, item.id).apply();
        applyBars();

        final ScrollView scrollView = new ScrollView(this);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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

<<<<<<< HEAD
=======
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        final Button favorite = pillButton(isFavorite(item.id) ? "★ Favori" : "☆ Favori");
        Button album = pillButton("Albümde Aç");
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleFavorite(item.id);
                favorite.setText(isFavorite(item.id) ? "★ Favori" : "☆ Favori");
            }
        });
        album.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showPhotoAlbum(albumIndexFor(item, currentPhotoIndex)); }
        });
        actionRow.addView(favorite, pillParams());
        actionRow.addView(album, pillParams());
        root.addView(actionRow);

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        addPhotoGallery(root, item);
        addTextSizeControls(root);

        TextView body = new TextView(this);
<<<<<<< HEAD
        body.setText(hasText(item.content) ? item.content : item.summary);
=======
        body.setText(cleanReaderText(hasText(item.content) ? item.content : item.summary));
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
        body.setTextSize(textSizeSp);
        body.setLineSpacing(0, 1.22f);
        body.setTextColor(textColor());
        body.setPadding(dp(16), dp(16), dp(16), dp(16));
        body.setBackground(roundedBox(cardBackground(), accentColor(), dp(18), dp(1)));
        root.addView(body, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

<<<<<<< HEAD
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
=======
        addPrettyTables(root, item.tables);

        Button top = wideButton("Başa Dön");
        top.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { scrollView.smoothScrollTo(0, 0); } });
        root.addView(top, compactButtonParams());
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)

        Button home = wideButton("Ana Ekrana Dön");
        home.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });
        root.addView(home, wideButtonParams());
        setContentView(scrollView);
<<<<<<< HEAD
=======

        final String scrollKey = "scroll_" + item.id;
        scrollView.post(new Runnable() {
            @Override public void run() { scrollView.scrollTo(0, prefs.getInt(scrollKey, 0)); }
        });
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                prefs.edit().putInt(scrollKey, scrollY).apply();
            }
        });
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
    }

    private void addPhotoGallery(LinearLayout root, final ArchiveItem item) {
        if (item.photos.size() == 0) {
            ImageView image = new ImageView(this);
            image.setImageResource(R.drawable.sample_photo);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            root.addView(image, imageParams());
<<<<<<< HEAD
            root.addView(descriptionText("Balkes Arşivi görseli. Kaydetmek için görsele uzun bas."));
=======
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
            return;
        }

        final PhotoItem photo = item.photos.get(currentPhotoIndex);
        ImageView image = new ImageView(this);
<<<<<<< HEAD
        Bitmap bitmap = loadAssetBitmap(photo.asset);
        if (bitmap != null) image.setImageBitmap(bitmap);
        else image.setImageResource(R.drawable.sample_photo);
=======
        setImageFromPath(image, photo.asset, R.drawable.sample_photo);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        root.addView(descriptionText("Fotoğraf " + (currentPhotoIndex + 1) + "/" + item.photos.size() + "\n" + caption + "\nKaydetmek için görsele uzun bas."));
=======
        root.addView(descriptionText("Fotoğraf " + (currentPhotoIndex + 1) + "/" + item.photos.size() + "\n" + caption));
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)

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

<<<<<<< HEAD
=======
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
                for (int j = 0; j < cells.size(); j++) {
                    TextView cell = new TextView(this);
                    cell.setText(cells.get(j));
                    cell.setTextSize(Math.max(12, textSizeSp - 4));
                    cell.setTextColor(header ? Color.WHITE : textColor());
                    cell.setTypeface(header ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    cell.setGravity(Gravity.CENTER_VERTICAL);
                    cell.setMinWidth(j == 1 ? dp(142) : dp(72));
                    cell.setPadding(dp(8), dp(7), dp(8), dp(7));
                    cell.setBackground(roundedBox(header ? accentColor() : cardBackground(), header ? accentColor() : subtleStrokeColor(), dp(6), dp(1)));
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

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
                "Bu final pakette veriler uygulama içinde yerel tutulur: metinler, tablolar ve görseller APK içine gömülüdür.<br><br>" +
                "Normal ÖzBalkesler haberleri ayrılmış, yalnızca Balıkesirspor sezon arşivi sayfaları eklenmiştir.<br><br>" +
=======
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
            Bitmap bitmap = loadAssetBitmap(currentItem.photos.get(currentPhotoIndex).asset);
=======
            String path = currentItem.photos.get(currentPhotoIndex).asset;
            Bitmap bitmap = loadAssetBitmap(path);
            if (bitmap != null) return bitmap;
            bitmap = loadCachedBitmap(path);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        if (currentItem != null) showArchiveDetail(currentItem, currentPhotoIndex); else showArchiveList();
=======
        if (currentItem != null) showArchiveDetail(currentItem, currentPhotoIndex);
        else if ("archive_list".equals(screen)) showArchiveList(currentQuery);
        else showHome();
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        try {
            String json = readAssetText("archive/archive_items.json");
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                ArchiveItem item = new ArchiveItem();
=======
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
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
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

=======
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
        if (manual) Toast.makeText(this, "GitHub kontrol ediliyor...", Toast.LENGTH_SHORT).show();
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
                        prefs.edit().putString(KEY_REMOTE_JSON, remote).putString(KEY_REMOTE_HASH, hash).apply();
                        handler.post(new Runnable() {
                            @Override public void run() {
                                loadArchiveItems();
                                Toast.makeText(MainActivity.this, "Arşiv güncellendi.", Toast.LENGTH_LONG).show();
                                if ("archive_list".equals(screen)) showArchiveList(currentQuery);
                                else if ("favorites".equals(screen)) showFavoritesList();
                            }
                        });
                    } else if (manual) {
                        handler.post(new Runnable() { @Override public void run() { Toast.makeText(MainActivity.this, "Arşiv güncel.", Toast.LENGTH_LONG).show(); } });
                    }
                } catch (Exception e) {
                    if (manual) handler.post(new Runnable() { @Override public void run() { Toast.makeText(MainActivity.this, "GitHub verisi alınamadı.", Toast.LENGTH_LONG).show(); } });
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

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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

<<<<<<< HEAD
=======
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

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
    private View listCard(ArchiveItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(roundedBox(cardBackground(), accentColor(), dp(16), dp(1)));
        card.setClickable(true);
        card.setFocusable(true);

        ImageView thumb = new ImageView(this);
<<<<<<< HEAD
        Bitmap bitmap = null;
        if (item.photos.size() > 0) bitmap = loadAssetBitmap(item.photos.get(0).asset);
        if (bitmap != null) thumb.setImageBitmap(bitmap);
=======
        if (item.photos.size() > 0) setImageFromPath(thumb, item.photos.get(0).asset, R.drawable.sample_photo);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        title.setText(item.title);
=======
        title.setText((isFavorite(item.id) ? "★ " : "") + item.title);
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
        snippet.setText(makeSnippet(hasText(item.summary) ? item.summary : item.content, 125));
=======
        snippet.setText(makeSnippet(cleanReaderText(hasText(item.summary) ? item.summary : item.content), 125));
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
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
<<<<<<< HEAD
=======
    private int subtleStrokeColor() { return darkTheme ? Color.rgb(90, 60, 60) : Color.rgb(235, 210, 210); }
>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)

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

<<<<<<< HEAD
=======
    private String cleanReaderText(String text) {
        if (!hasText(text)) return "";
        String s = text;
        s = s.replace("Önemli Duyuru: Bu sayfada verilen bilgiler tamamen\nbalkesarsivi.com\nadresinden alınmıştır. Bu bilgilerin ve sayfanın kaybolmaması adına yedekleme amaçlı paylaşılmıştır. Hiç bir menfaat söz konusu değildir. Ancak bu yazının telif hakkının\nbalkesarsivi.com\nadresine ait olduğunu bilmenizde fayda var.\n", "");
        s = s.replace("Önemli Duyuru: Bu sayfada verilen bilgiler tamamen balkesarsivi.com adresinden alınmıştır. Bu bilgilerin ve sayfanın kaybolmaması adına yedekleme amaçlı paylaşılmıştır. Hiç bir menfaat söz konusu değildir. Ancak bu yazının telif hakkının balkesarsivi.com adresine ait olduğunu bilmenizde fayda var.", "");
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

>>>>>>> 6d4f6d5 (Add GitHub hybrid archive version 1.6)
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
