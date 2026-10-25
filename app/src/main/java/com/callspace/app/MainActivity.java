package com.callspace.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 4101;
    private static final int EXPORT_REQUEST = 4102;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private String exportContents = "";
    private String exportName = "callspace-contacts.json";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setSupportZoom(false);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                String[] types = params.getAcceptTypes();
                boolean image = false;
                if (types != null) for (String type : types) if (type != null && type.toLowerCase().contains("image")) image = true;
                intent.setType(image ? "image/*" : "*/*");
                try { startActivityForResult(Intent.createChooser(intent, image ? "Choose a photo" : "Choose a file"), FILE_CHOOSER_REQUEST); }
                catch (ActivityNotFoundException e) { fileCallback = null; callback.onReceiveValue(null); Toast.makeText(MainActivity.this, "No compatible file picker found", Toast.LENGTH_LONG).show(); }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class AndroidBridge {
        @JavascriptInterface public void saveExport(String filename, String contents) {
            runOnUiThread(() -> {
                exportName = (filename == null || filename.trim().isEmpty()) ? "callspace-contacts.json" : filename;
                exportContents = contents == null ? "{}" : contents;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, exportName);
                try { startActivityForResult(intent, EXPORT_REQUEST); }
                catch (ActivityNotFoundException e) { Toast.makeText(MainActivity.this, "Unable to open save dialog", Toast.LENGTH_LONG).show(); }
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (fileCallback != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && data != null && data.getData() != null) results = new Uri[]{data.getData()};
                fileCallback.onReceiveValue(results);
                fileCallback = null;
            }
        } else if (requestCode == EXPORT_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                if (out != null) out.write(exportContents.getBytes(StandardCharsets.UTF_8));
                Toast.makeText(this, "Contacts exported", Toast.LENGTH_SHORT).show();
            } catch (Exception e) { Toast.makeText(this, "Could not export contacts", Toast.LENGTH_LONG).show(); }
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        if (webView != null) { webView.removeJavascriptInterface("AndroidBridge"); webView.destroy(); }
        super.onDestroy();
    }
}
