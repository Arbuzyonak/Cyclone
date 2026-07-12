package io.github.arbuzyonak.cyclone;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.micewine.emu.R;

import java.io.File;

public class CycloneWebActivity extends AppCompatActivity {

    private static final String START_URL = "https://playvortex.io/home";
    private static final String ALLOWED_HOST_SUFFIX = "playvortex.io";

    private static final String NAV_OVERRIDE_CSS =
        ".navbar { flex-wrap: wrap !important; height: auto !important; row-gap: 0.5rem !important; padding: 0.5rem 0.75rem !important; }" +
        ".navbar-search { order: 3; flex-basis: 100%; }" +
        ".navbar-search form { max-width: 100% !important; }" +
        ".navbar-actions { gap: 0.375rem !important; flex-wrap: wrap !important; }" +
        ".btn-download-nav, .btn-discord, .btn-signout-sm { padding: 0.25rem 0.5rem !important; font-size: 0.75rem !important; }" +
        "a.btn-download-nav[href=\\\"/download\\\"] { display: none !important; }";

    private WebView webView;
    private ProgressBar progressBar;
    private View errorView;
    private String lastLoadedUrl = START_URL;
    private boolean triedCache = false;
    private ValueCallback<Uri[]> pendingFileCallback;
    private volatile boolean clientUpdateRunning = false;

    private final ActivityResultLauncher<String> filePicker =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            Uri[] result = (uri != null) ? new Uri[]{uri} : null;
            if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(result);
            pendingFileCallback = null;
        });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cyclone_web);

        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        errorView = findViewById(R.id.error_view);
        findViewById(R.id.retry_button).setOnClickListener(v -> {
            hideError();
            webView.loadUrl(lastLoadedUrl);
        });
        // escape hatch for a wedged session: long-press retry to sign out completely
        findViewById(R.id.retry_button).setOnLongClickListener(v -> {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
            webView.clearCache(true);
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            hideError();
            webView.loadUrl(START_URL);
            return true;
        });

        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.addJavascriptInterface(new CycloneBridge(), "CycloneBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverride(request.getUrl(), request.isForMainFrame());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url != null) lastLoadedUrl = url;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                if (errorView.getVisibility() == View.VISIBLE) hideError();
                triedCache = false;
                view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                injectNavCss(view);
                injectUpdateButton(view);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (!request.isForMainFrame()) return;
                // offline: retry once straight from cache so the library still shows
                if (!triedCache) {
                    triedCache = true;
                    view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    view.post(() -> view.loadUrl(lastLoadedUrl));
                    return;
                }
                showError();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 500) showError();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility((newProgress > 0 && newProgress < 100) ? View.VISIBLE : View.GONE);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (pendingFileCallback != null) pendingFileCallback.onReceiveValue(null);
                pendingFileCallback = callback;
                filePicker.launch("image/*");
                return true;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            String cookie = CookieManager.getInstance().getCookie(url);
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.addRequestHeader("Cookie", cookie);
            req.addRequestHeader("User-Agent", userAgent);
            req.setMimeType(mimeType);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(req);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack(); else finish();
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(START_URL);
        }

        maybeShowWelcome();
        checkForAppUpdate();
    }

    private void maybeShowWelcome() {
        android.content.SharedPreferences sp = getSharedPreferences("cyclone", MODE_PRIVATE);
        if (sp.getBoolean("welcomeShown", false)) return;
        sp.edit().putBoolean("welcomeShown", true).apply();
        new android.app.AlertDialog.Builder(this, R.style.Theme_Cyclone_Dialog)
                .setTitle("Welcome to Cyclone")
                .setMessage("Sign in to your Vortex account, then hit Play on a game."
                        + "\n\nThe first launch downloads the runtime (about 400 MB) and can take a few minutes."
                        + "\n\nIn game: drag to look around, use the joystick to move, and the top-corner button opens the menu.")
                .setPositiveButton("Got it", null)
                .show();
    }

    // Checks GitHub releases at most once a day; compat builds only look at -compat
    // tags so nobody gets pointed at an APK for the wrong GPU.
    private void checkForAppUpdate() {
        android.content.SharedPreferences sp = getSharedPreferences("cyclone", MODE_PRIVATE);
        long last = sp.getLong("updateCheckAt", 0);
        if (System.currentTimeMillis() - last < 24L * 60 * 60 * 1000) return;
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url("https://api.github.com/repos/Arbuzyonak/Cyclone/releases?per_page=15")
                        .header("Accept", "application/vnd.github+json")
                        .build();
                try (okhttp3.Response resp = client.newCall(req).execute()) {
                    if (!resp.isSuccessful() || resp.body() == null) return;
                    String body = resp.body().string();
                    sp.edit().putLong("updateCheckAt", System.currentTimeMillis()).apply();
                    boolean compat = com.micewine.emu.BuildConfig.USE_SYSTEM_VULKAN;
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("\"tag_name\"\\s*:\\s*\"(v?[0-9.]+(-compat)?)\"").matcher(body);
                    long best = -1;
                    String bestTag = null;
                    while (m.find()) {
                        boolean tagCompat = m.group(2) != null;
                        if (tagCompat != compat) continue;
                        long v = versionScore(m.group(1));
                        if (v > best) {
                            best = v;
                            bestTag = m.group(1);
                        }
                    }
                    if (bestTag == null || best <= versionScore(com.micewine.emu.BuildConfig.VERSION_NAME)) return;
                    final String tag = bestTag;
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        new android.app.AlertDialog.Builder(this, R.style.Theme_Cyclone_Dialog)
                                .setTitle("Update available")
                                .setMessage("Cyclone " + tag.replace("v", "").replace("-compat", "")
                                        + " is out. Get it from GitHub?")
                                .setPositiveButton("Open", (d, w) -> startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/Arbuzyonak/Cyclone/releases/tag/" + tag))))
                                .setNegativeButton("Later", null)
                                .show();
                    });
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private static long versionScore(String s) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(s);
        if (!m.find()) return -1;
        return Long.parseLong(m.group(1)) * 1_000_000L + Long.parseLong(m.group(2)) * 1000L + Long.parseLong(m.group(3));
    }

    private boolean shouldOverride(Uri url, boolean isMainFrame) {
        String scheme = url.getScheme();
        if ("vortex".equalsIgnoreCase(scheme)) {
            onVortexUri(url);
            return true;
        }
        if (!isMainFrame || isAllowedHost(url)) {
            return false;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, url));
        return true;
    }

    private void onVortexUri(Uri uri) {
        android.content.SharedPreferences sp = getSharedPreferences("cyclone", MODE_PRIVATE);
        if (!sp.getBoolean("firstLaunchHintShown", false)) {
            sp.edit().putBoolean("firstLaunchHintShown", true).apply();
            new android.app.AlertDialog.Builder(this, R.style.Theme_Cyclone_Dialog)
                    .setTitle("First launch")
                    .setMessage("The first time you play, Vortex may ask you to sign in from a "
                            + "browser instead of joining. If that happens, press Menu to leave, "
                            + "then tap Play again — it joins the second time.")
                    .setCancelable(false)
                    .setPositiveButton("Play", (d, w) -> doLaunch(uri))
                    .show();
            return;
        }
        doLaunch(uri);
    }

    private void doLaunch(Uri uri) {
        String singleQuoted = "'" + uri.toString() + "'";
        String sessionToken = extractSessionToken();
        Intent launch = new Intent(this, com.micewine.emu.activities.MainActivity.class);
        launch.putExtra("shortcutName", "Vortex");
        launch.putExtra("cycloneExeArgs", singleQuoted);
        launch.putExtra("cycloneSessionToken", sessionToken);
        launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(launch);
        // Leave the WebView on the home page, not the bare "Launching…/download the app"
        // page, so returning from the game (Menu button) lands on the main page.
        webView.post(() -> webView.loadUrl(START_URL));
    }

    private String extractSessionToken() {
        String cookies = CookieManager.getInstance().getCookie("https://playvortex.io");
        if (cookies == null) return "";
        for (String c : cookies.split(";")) {
            String t = c.trim();
            if (t.startsWith("session_token=")) return t.substring("session_token=".length());
        }
        return "";
    }

    private boolean isAllowedHost(Uri url) {
        String host = url.getHost();
        if (host == null) return false;
        return host.equals(ALLOWED_HOST_SUFFIX) || host.endsWith("." + ALLOWED_HOST_SUFFIX);
    }

    private void injectNavCss(WebView view) {
        String js =
            "(function(){" +
            "if(document.getElementById('cyclone-nav-override'))return;" +
            "var st=document.createElement('style');" +
            "st.id='cyclone-nav-override';" +
            "st.textContent=\"" + NAV_OVERRIDE_CSS + "\";" +
            "document.head.appendChild(st);})();";
        view.evaluateJavascript(js, null);
    }

    private void injectUpdateButton(WebView view) {
        String js =
            "(function(){" +
            "if(document.getElementById('cyclone-update-btn'))return;" +
            "var root=document.querySelector('.navbar-actions')||document;" +
            "var els=root.querySelectorAll('a,button');" +
            "var settings=null;" +
            "for(var i=0;i<els.length;i++){if(els[i].textContent.trim()==='Settings'){settings=els[i];break;}}" +
            "if(!settings)return;" +
            "var b=settings.cloneNode(false);" +
            "b.id='cyclone-update-btn';" +
            "b.removeAttribute('href');" +
            "b.textContent='Update';" +
            "b.style.cursor='pointer';" +
            "b.addEventListener('click',function(e){e.preventDefault();CycloneBridge.updateClient();});" +
            "settings.parentNode.insertBefore(b,settings.nextSibling);" +
            "var h=settings.cloneNode(false);" +
            "h.id='cyclone-doctor-btn';" +
            "h.removeAttribute('href');" +
            "h.textContent='Help';" +
            "h.style.cursor='pointer';" +
            "h.addEventListener('click',function(e){e.preventDefault();CycloneBridge.openDoctor();});" +
            "b.parentNode.insertBefore(h,b.nextSibling);})();";
        view.evaluateJavascript(js, null);
    }

    private void setUpdateButtonLabel(String label) {
        String js =
            "(function(){var b=document.getElementById('cyclone-update-btn');" +
            "if(b)b.textContent='" + label + "';})();";
        webView.evaluateJavascript(js, null);
    }

    private class CycloneBridge {
        @JavascriptInterface
        public void openDoctor() {
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) CycloneDoctor.show(CycloneWebActivity.this);
            });
        }

        @JavascriptInterface
        public void updateClient() {
            if (clientUpdateRunning) return;
            clientUpdateRunning = true;
            runOnUiThread(() -> {
                setUpdateButtonLabel("Updating…");
                Toast.makeText(CycloneWebActivity.this, R.string.cyclone_update_started, Toast.LENGTH_SHORT).show();
            });
            new Thread(this::doUpdate).start();
        }

        private void doUpdate() {
            String token = extractSessionToken();
            boolean anyPrefix = false;
            boolean ok = true;
            File[] prefixes = com.micewine.emu.activities.MainActivity.winePrefixesDir.listFiles();
            if (prefixes != null) {
                for (File prefix : prefixes) {
                    if (!prefix.isDirectory()) continue;
                    File gameDir = new File(prefix, "drive_c/Vortex");
                    if (!gameDir.getParentFile().exists()) continue;
                    anyPrefix = true;
                    File exe = new File(gameDir, "Vortex.exe");
                    // downloadVortexExe guards against downgrades: it keeps the installed
                    // build unless the download is strictly newer, so this never reverts a
                    // manually-installed newer client to the stale /download/windows base.
                    if (!com.micewine.emu.activities.MainActivity.downloadVortexExe(exe, token)) {
                        ok = false;
                    }
                }
            }
            final boolean downloaded = ok && anyPrefix;
            final boolean nothingToUpdate = !anyPrefix;
            runOnUiThread(() -> {
                setUpdateButtonLabel("Update");
                int msg = nothingToUpdate ? R.string.cyclone_update_nothing
                        : downloaded ? R.string.cyclone_update_done : R.string.cyclone_update_failed;
                Toast.makeText(CycloneWebActivity.this, msg, Toast.LENGTH_LONG).show();
            });
            clientUpdateRunning = false;
        }
    }

    private void showError() {
        webView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        // resumeTimers/pauseTimers are process-global; this is the only WebView, and
        // freezing it keeps the page from burning CPU/RAM while the game runs.
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
