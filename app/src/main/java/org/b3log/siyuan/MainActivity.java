 /*
 * SiYuan - 源于思考，饮水思源
 * Copyright (c) 2020-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.siyuan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.view.WindowManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX;

import org.apache.commons.io.FileUtils;
import org.b3log.siyuan.appUtils.HWs;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import mobile.Mobile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 主程序.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.4.17, Jun 26, 2023
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity implements com.blankj.utilcode.util.Utils.OnAppStatusChangedListener {

    private WebView webView;
    private ImageView bootLogo;
    private ProgressBar bootProgressBar;
    private TextView bootDetailsText;
    private String webViewVer;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int REQUEST_SELECT_FILE = 100;
    private long exitTime;

    private boolean isFirstRun() {
        final String dataDir = getFilesDir().getAbsolutePath();
        final String appDir = dataDir + "/app";
        final File appDirFile = new File(appDir);
        return !appDirFile.exists();
    }

    @Override
    public void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (null != webView) {
            final String blockURL = intent.getStringExtra("blockURL");
            if (!StringUtils.isEmpty(blockURL)) {
                webView.evaluateJavascript("javascript:window.openFileByURL('" + blockURL + "')", null);
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.i("boot", "create main activity");

        // 拉起内核
        startKernel();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isFirstRun()) {
            Intent InitActivity = new Intent(this, org.b3log.siyuan.permission.InitActivity.class);
            InitActivity.putExtra("contentViewId", R.layout.init_activity);
            InitActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(InitActivity);
        }

        CrashReport.initCrashReport(getApplicationContext(), "26ae2b5fb4", true);

        // 这段代码并不会直接导致高刷率的生效，它只是在获取支持的显示模式中寻找高刷率最大的模式，并将其设置为首选模式。
        Display display = null;
        display = this.getDisplay(); // 等效于 getApplicationContext().getDisplay() 因为Activity已经实现了Context接口，所以用 this 替换
        if (display != null) {
            Display.Mode[] modes = display.getSupportedModes();
            Display.Mode preferredMode = modes[0];
            for (Display.Mode mode : modes) {
                Log.d("MainActivity Display", "supported mode: " + mode.toString());
                if (mode.getRefreshRate() > preferredMode.getRefreshRate() && mode.getPhysicalWidth() >= preferredMode.getPhysicalWidth()) {
                    preferredMode = mode;
                }
            }
            Log.d("MainActivity Display", "preferredMode mode: " + preferredMode.toString());
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.preferredDisplayModeId = preferredMode.getModeId();
            getWindow().setAttributes(params);
        }

        // 初始化 UI 元素
        initUIElements();

        // 初始化外观资源
        initAppearance();

        AppUtils.registerAppStatusChangedListener(this);
//        WebView.setWebContentsDebuggingEnabled(true);

        // 注册软键盘顶部跟随工具栏
        Utils.registerSoftKeyboardToolbar(this, webView);

        // 沉浸式状态栏设置
        UltimateBarX.statusBarOnly(this).transparent().light(false).color(Color.parseColor("#1e1e1e")).apply();
        ((ViewGroup) webView.getParent()).setPadding(0, UltimateBarX.getStatusBarHeight(), 0, 0);

        KeyboardUtils.fixAndroidBug5497(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions https://github.com/googlesamples/easypermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private void initUIElements() {
        bootLogo = findViewById(R.id.bootLogo);
        bootProgressBar = findViewById(R.id.progressBar);
        bootDetailsText = findViewById(R.id.bootDetails);
        webView = findViewById(R.id.webView);
        webView.setBackgroundColor(Color.parseColor("#1e1e1e"));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // 在加载页面出现错误时进行处理
                if (error != null) {
                    Log.e("WebViewClient", "onReceivedError: " + error.getDescription());
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // 页面开始加载时调用
                Log.d("WebViewClient", "onPageStarted: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成时调用
                Log.d("WebViewClient", "onPageFinished: " + url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(final WebView mWebView, final ValueCallback<Uri[]> filePathCallback, final FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;
                final Intent intent = fileChooserParams.createIntent();
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (final Exception e) {
                    uploadMessage = null;
                    Toast.makeText(getApplicationContext(), "Cannot open file chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) { // 当网页请求其他权限时，会回调此方法以询问用户是否允许
                request.grant(request.getResources());
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) { // 当 WebView 加载页面时，会多次回调此方法以报告加载进度
                // 增加Javascript异常监控
                CrashReport.setJavascriptMonitor(webView, true);
                super.onProgressChanged(webView, progress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                String formattedDate = sdf.format(date);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("onJsAlert from WebView")
                        .setMessage("\n--------------------------------------------\n" + message + "\n--------------------------------------------\n\n* " + view.getTitle() + "\n* " + formattedDate)
                        .setPositiveButton("OK", (dialog, which) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

        });

        webView.setOnDragListener((v, event) -> {
            // 禁用拖拽 https://github.com/siyuan-note/siyuan/issues/6436
            return DragEvent.ACTION_DRAG_ENDED != event.getAction();
        });

        final WebSettings ws = webView.getSettings();
        checkWebViewVer(ws);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showBootIndex() {
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
                final Uri uri = request.getUrl();
                final String url = uri.toString();
                if (url.contains("127.0.0.1")) {
                    view.loadUrl(url);
                    return true;
                }

                if (url.contains("siyuan://api/system/exit")) {
                    exit();
                    return true;
                }

                if (uri.getScheme().toLowerCase().startsWith("http")) {
                    final Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i); // https://developer.android.google.cn/training/app-links/verify-android-applinks?hl=zh-cn
                    // 从 Android 12 开始，经过验证的链接现在会自动在相应的应用中打开，以获得更简化、更快速的用户体验。谷歌还更改了未经Android应用链接验证或用户手动批准的链接的默认处理方式。谷歌表示，Android 12将始终在默认浏览器中打开此类未经验证的链接，而不是向您显示应用程序选择对话框。
                    return true;
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                new Handler().postDelayed(() -> {
                    bootLogo.setVisibility(View.GONE);
                    bootProgressBar.setVisibility(View.GONE);
                    bootDetailsText.setVisibility(View.GONE);
                    final ImageView bootLogo = findViewById(R.id.bootLogo);
                    bootLogo.setVisibility(View.GONE);
                }, 666);
            }
        });

        final JSAndroid JSAndroid = new JSAndroid(this);
        webView.addJavascriptInterface(JSAndroid, "JSAndroid");
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        final WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ws.setTextZoom(100);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUserAgentString("SiYuan-Sillot/" + Utils.version + " https://b3log.org/siyuan Android " + ws.getUserAgentString());

        waitFotKernelHttpServing();
        WebView.setWebContentsDebuggingEnabled(true);
        webView.loadUrl("http://127.0.0.1:58131/appearance/boot/index.html?v=" + Utils.version);

        new Thread(this::keepLive).start();
    }

    private Handler bootHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            final String cmd = msg.getData().getString("cmd");
            if ("startKernel".equals(cmd)) {
                bootKernel();
            } else {
                showBootIndex();
            }
        }
    };

    private void startKernel() {
        final Bundle b = new Bundle();
        b.putString("cmd", "startKernel");
        final Message msg = new Message();
        msg.setData(b);
        bootHandler.sendMessage(msg);
    }

    private void bootKernel() {
        if (Mobile.isHttpServing()) {
            Log.i("boot", "kernel HTTP server is running");
            showBootIndex();
            return;
        }

        final String appDir = getFilesDir().getAbsolutePath() + "/app";
        final Locale locale = getResources().getConfiguration().locale;
        final String workspaceBaseDir = getExternalFilesDir(null).getAbsolutePath();
        final String timezone = TimeZone.getDefault().getID();
        new Thread(() -> {
            final String localIPs = Utils.getIPAddressList();
            String lang = locale.getLanguage() + "_" + locale.getCountry();
            if (lang.toLowerCase().contains("cn")) {
                lang = "zh_CN";
            } else {
                lang = "en_US";
            }

            Mobile.startKernel("android", appDir, workspaceBaseDir, timezone, localIPs, lang,
                    Build.VERSION.RELEASE +
                            "/SDK " + Build.VERSION.SDK_INT +
                            "/WebView " + webViewVer +
                            "/Manufacturer " + android.os.Build.MANUFACTURER +
                            "/Brand " + android.os.Build.BRAND);
        }).start();

        final Bundle b = new Bundle();
        b.putString("cmd", "bootIndex");
        final Message msg = new Message();
        msg.setData(b);
        bootHandler.sendMessage(msg);
    }

    /**
     * 通知栏保活。
     */
    private void keepLive() {
        while (true) {
            try {
                final Intent intent = new Intent(MainActivity.this, KeepLiveService.class);
                ContextCompat.startForegroundService(this, intent);
                sleep(31 * 1000);
                stopService(intent);
            } catch (final Throwable t) {
            }
        }
    }

    /**
     * 等待内核 HTTP 服务伺服。
     */
    private void waitFotKernelHttpServing() {
        while (true) {
            sleep(10);
            if (Mobile.isHttpServing()) {
                break;
            }
        }
    }

    private void initAppearance() {
        if (needUnzipAssets()) {
            bootLogo.setVisibility(View.VISIBLE);
            // 不要进度条更平滑一些
            //bootProgressBar.setVisibility(View.VISIBLE);
            //bootDetailsText.setVisibility(View.VISIBLE);

            final String dataDir = getFilesDir().getAbsolutePath();
            final String appDir = dataDir + "/app";
            final File appVerFile = new File(appDir, "VERSION");

            setBootProgress("Clearing appearance...", 20);
            try {
                FileUtils.deleteDirectory(new File(appDir));
            } catch (final Exception e) {
                Log.wtf("boot", "delete dir [" + appDir + "] failed, exit application", e);
                exit();
                return;
            }

            setBootProgress("Initializing appearance...", 60);
            Utils.unzipAsset(getAssets(), "app.zip", appDir + "/app");

            try {
                FileUtils.writeStringToFile(appVerFile, Utils.version, StandardCharsets.UTF_8);
            } catch (final Exception e) {
                Log.w("boot", "write version failed", e);
            }

            setBootProgress("Booting kernel...", 80);
        }


//        Intent InitActivity = new Intent(this, org.b3log.siyuan.permission.InitActivity.class);
//        InitActivity.putExtra("contentViewId", R.layout.init_activity);
//        InitActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(InitActivity);
    }

    private void setBootProgress(final String text, final int progressPercent) {
        runOnUiThread(() -> {
            bootDetailsText.setText(text);
            bootProgressBar.setProgress(progressPercent);
        });
    }

    private void sleep(final long time) {
        try {
            Thread.sleep(time);
        } catch (final Exception e) {
            Log.e("runtime", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        // 不要管报错，写 super call 就失效了
        if (Utils.isPad(getApplicationContext())) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                HWs.getInstance().vibratorWaveform(this, new long[]{0, 30, 25, 40, 25, 10}, new int[]{2, 4, 3, 2, 2, 2}, -1);
                exit();
                sleep(200);
                System.exit(0);
            }
        } else {
            webView.evaluateJavascript("javascript:window.goBack()", null);
        }
        HWs.getInstance().vibratorWaveform(this, new long[]{0, 30, 25, 40, 25}, new int[]{9, 2, 1, 7, 2}, -1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (null == uploadMessage) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }

        // 以下代码参考自 https://github.com/mgks/os-fileup/blob/master/app/src/main/java/mgks/os/fileup/MainActivity.java MIT license
        if (requestCode == REQUEST_SELECT_FILE) {
            Uri[] results = null;
            ClipData clipData;
            String stringData;

            try {
                clipData = intent.getClipData();
                stringData = intent.getDataString();
            } catch (Exception e) {
                clipData = null;
                stringData = null;
            }

            if (clipData != null) {
                final int numSelectedFiles = clipData.getItemCount();
                results = new Uri[numSelectedFiles];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    results[i] = clipData.getItemAt(i).getUri();
                }
            } else {
                try {
                    Bitmap cam_photo = (Bitmap) intent.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    cam_photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    stringData = MediaStore.Images.Media.insertImage(this.getContentResolver(), cam_photo, null, null);
                } catch (Exception ignored) {
                }

                if (!StringUtils.isEmpty(stringData)) {
                    results = new Uri[]{Uri.parse(stringData)};
                }
            }

            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private boolean needUnzipAssets() {
        final String dataDir = getFilesDir().getAbsolutePath();
        final String appDir = dataDir + "/app";
        final File appDirFile = new File(appDir);
        appDirFile.mkdirs();

        boolean ret = true;
        final File appVerFile = new File(appDir, "VERSION");
        if (appVerFile.exists()) {
            try {
                final String ver = FileUtils.readFileToString(appVerFile, StandardCharsets.UTF_8);
                ret = !ver.equals(Utils.version);
            } catch (final Exception e) {
                Log.w("boot", "check version failed", e);
            }
        }
        return ret;
    }

    @Override
    protected void onDestroy() {
        Log.i("boot", "destroy main activity");
        super.onDestroy();
        KeyboardUtils.unregisterSoftInputChangedListener(getWindow());
        AppUtils.unregisterAppStatusChangedListener(this);
        if (null != webView) {
            webView.removeAllViews();
            webView.destroy();
        }
    }

    @Override
    public void onForeground(Activity activity) {
        startSyncData();
        if (null != webView) {
            webView.evaluateJavascript("javascript:window.reconnectWebSocket()", null);
        }
    }

    @Override
    public void onBackground(Activity activity) {
        startSyncData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void exit() {
        finishAffinity();
        finishAndRemoveTask();
    }

    private void checkWebViewVer(final WebSettings ws) {
        // Android check WebView version 75+ https://github.com/siyuan-note/siyuan/issues/7840
        final String ua = ws.getUserAgentString();
        if (ua.contains("Chrome/")) {
            final int minVer = 95;
            try {
                final String chromeVersion = ua.split("Chrome/")[1].split(" ")[0];
                if (chromeVersion.contains(".")) {
                    final String[] chromeVersionParts = chromeVersion.split("\\.");
                    webViewVer = chromeVersionParts[0];
                    if (Integer.parseInt(webViewVer) < minVer) {
                        Toast.makeText(this, "WebView version " + webViewVer + " is too low, please upgrade to " + minVer + "+", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (final Exception e) {
                Log.e("boot", "check webview version failed", e);
                Toast.makeText(this, "Check WebView version failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private static boolean syncing;

    public static void startSyncData() {
        new Thread(MainActivity::syncData).start();
    }

    public static void syncData() {
        try {
            if (syncing) {
                Log.i("sync", "data is syncing...");
                return;
            }
            syncing = true;
            final OkHttpClient client = new OkHttpClient();
            final RequestBody body = RequestBody.create(null, new JSONObject().
                    put("mobileSwitch", true).toString());
            final Request request = new Request.Builder().url("http://127.0.0.1:58131/api/sync/performSync").method("POST", body).build();
            final Response response = client.newCall(request).execute();
            response.close();
        } catch (final Throwable e) {
            Log.e("sync", "data sync failed", e);
        } finally {
            syncing = false;
        }
    }
}