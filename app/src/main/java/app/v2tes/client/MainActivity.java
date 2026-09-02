package app.v2tes.client;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
  private WebView webView;
  private int insetTopPx;
  private int insetBottomPx;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    float density = getResources().getDisplayMetrics().density;
    insetTopPx = Math.round(52 * density);
    insetBottomPx = Math.round(28 * density);

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(Color.parseColor("#07090C"));
    window.setNavigationBarColor(Color.parseColor("#07090C"));
    if (Build.VERSION.SDK_INT >= 28) {
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
      window.setAttributes(lp);
    }
    if (Build.VERSION.SDK_INT >= 29) {
      window.setNavigationBarContrastEnforced(false);
    }
    if (Build.VERSION.SDK_INT >= 30) {
      window.setDecorFitsSystemWindows(false);
    } else {
      window
          .getDecorView()
          .setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    webView = new WebView(this);
    webView.setBackgroundColor(Color.parseColor("#07090C"));
    webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    webView.setVerticalScrollBarEnabled(false);
    webView.setHorizontalScrollBarEnabled(false);
    if (Build.VERSION.SDK_INT >= 26) {
      webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true);
    }

    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setDatabaseEnabled(true);
    settings.setAllowFileAccess(true);
    settings.setAllowContentAccess(true);
    settings.setMediaPlaybackRequiresUserGesture(false);
    settings.setLoadWithOverviewMode(true);
    settings.setUseWideViewPort(true);
    settings.setSupportZoom(false);
    settings.setBuiltInZoomControls(false);
    settings.setDisplayZoomControls(false);
    settings.setTextZoom(100);
    settings.setDefaultTextEncodingName("utf-8");
    settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    settings.setUserAgentString(settings.getUserAgentString() + " v2TeS-Android/1.5.0");
    if (Build.VERSION.SDK_INT >= 21) {
      settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    if (Build.VERSION.SDK_INT >= 17) {
      settings.setOffscreenPreRaster(true);
    }
    disableForcedDark(settings);

    webView.setOnApplyWindowInsetsListener(
        (v, insets) -> {
          if (Build.VERSION.SDK_INT >= 30) {
            android.graphics.Insets bars =
                insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            if (bars.top > 0) insetTopPx = bars.top;
            if (bars.bottom > 0) insetBottomPx = bars.bottom;
          } else {
            if (insets.getSystemWindowInsetTop() > 0) insetTopPx = insets.getSystemWindowInsetTop();
            if (insets.getSystemWindowInsetBottom() > 0)
              insetBottomPx = insets.getSystemWindowInsetBottom();
          }
          injectInsets();
          return insets;
        });

    webView.setWebChromeClient(new WebChromeClient());
    webView.setWebViewClient(
        new WebViewClient() {
          @Override
          public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) return null;
            String host = request.getUrl().getHost();
            if (host == null) return null;
            if (!host.equals("localhost") && !host.equals("127.0.0.1")) return null;
            String path = request.getUrl().getPath();
            if (path == null || path.equals("/") || path.length() == 0) path = "/index.html";
            String asset = "www" + path;
            try {
              InputStream stream = getAssets().open(asset);
              return new WebResourceResponse(mimeFor(path), "utf-8", 200, "OK", corsHeaders(), stream);
            } catch (Exception missing) {
              if (path.startsWith("/assets/")) {
                return new WebResourceResponse(
                    "text/plain",
                    "utf-8",
                    404,
                    "Not Found",
                    corsHeaders(),
                    new ByteArrayInputStream(new byte[0]));
              }
              try {
                InputStream fallback = getAssets().open("www/index.html");
                return new WebResourceResponse("text/html", "utf-8", 200, "OK", corsHeaders(), fallback);
              } catch (Exception ignored) {
                return null;
              }
            }
          }

          @Override
          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request == null || request.getUrl() == null) return false;
            String host = request.getUrl().getHost();
            return host != null && !host.equals("localhost") && !host.equals("127.0.0.1");
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            injectInsets();
          }
        });

    setContentView(webView);
    webView.loadUrl("http://localhost/index.html?native=1");
  }

  private void disableForcedDark(WebSettings settings) {
    if (Build.VERSION.SDK_INT >= 29) {
      try {
        settings.setForceDark(WebSettings.FORCE_DARK_OFF);
      } catch (Throwable ignored) {
      }
    }
    try {
      Method m = WebSettings.class.getMethod("setAlgorithmicDarkeningAllowed", boolean.class);
      m.invoke(settings, false);
    } catch (Throwable ignored) {
    }
  }

  private void injectInsets() {
    if (webView == null) return;
    String js =
        "document.documentElement.classList.add('native-app');"
            + "window.__V2TES_NATIVE=true;"
            + "document.documentElement.style.setProperty('--v2tes-top','"
            + insetTopPx
            + "px');"
            + "document.documentElement.style.setProperty('--v2tes-bottom','"
            + insetBottomPx
            + "px');";
    webView.evaluateJavascript(js, null);
  }

  @Override
  public void onBackPressed() {
    if (webView != null && webView.canGoBack()) {
      webView.goBack();
      return;
    }
    super.onBackPressed();
  }

  @Override
  protected void onDestroy() {
    if (webView != null) {
      webView.loadUrl("about:blank");
      webView.destroy();
    }
    super.onDestroy();
  }

  private static Map<String, String> corsHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Cache-Control", "no-cache");
    return headers;
  }

  private static String mimeFor(String path) {
    String lower = path.toLowerCase();
    if (lower.endsWith(".html")) return "text/html";
    if (lower.endsWith(".js")) return "application/javascript";
    if (lower.endsWith(".css")) return "text/css";
    if (lower.endsWith(".svg")) return "image/svg+xml";
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".webp")) return "image/webp";
    if (lower.endsWith(".webmanifest")) return "application/manifest+json";
    if (lower.endsWith(".json")) return "application/json";
    if (lower.endsWith(".woff2")) return "font/woff2";
    if (lower.endsWith(".ico")) return "image/x-icon";
    return "application/octet-stream";
  }
}
