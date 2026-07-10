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

/**
 * Cyclone launcher screen: a WebView onto playvortex.io. This is the app's entry
 * point. When the user taps Play on the site, it fires a vortex:// URL which we
 * intercept in {@link #shouldOverride} and hand to the embedded MiceWine runtime.
 */
public class CycloneWebActivity extends AppCompatActivity {

    private static final String START_URL = "https://playvortex.io/home";
    private static final String ALLOWED_HOST_SUFFIX = "playvortex.io";

    // playvortex.io's navbar has no responsive collapse below ~900px; patch it
    // client-side so it doesn't overflow on a phone-width WebView.
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
    private ValueCallback<Uri[]> pendingFileCallback;

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
                injectNavCss(view);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showError();
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
    }

    /** Returns true if we handled the URL (WebView should not load it). */
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

    /**
     * Play was tapped on the website; it fired vortex://play?game=X&token=Y.
     * Launch the embedded runtime with the pre-configured "Vortex" game entry,
     * overriding its arguments with this fresh authenticated URI. The URI is
     * single-quoted so the '&' survives the shell command MiceWine builds.
     */
    private void onVortexUri(Uri uri) {
        String singleQuoted = "'" + uri.toString() + "'";
        // The session cookie (from login in this WebView) lets the runtime fetch
        // Vortex.exe on first launch.
        String sessionToken = extractSessionToken();
        Intent launch = new Intent(this, com.micewine.emu.activities.MainActivity.class);
        launch.putExtra("shortcutName", "Vortex");
        launch.putExtra("cycloneExeArgs", singleQuoted);
        launch.putExtra("cycloneSessionToken", sessionToken);
        launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(launch);
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
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
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
