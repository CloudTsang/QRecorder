package extensions.webview;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebViewClient;


/*import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;*/

import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.meili.mnist.JsInteration;
import com.meili.mnist.R;

public class WebViewActivity extends Activity {
	public static final String EXTRA_URL = "extensions.webviewex.EXTRA_URL";
	public static final String EXTRA_HTML = "extensions.webviewex.EXTRA_HTML";
	public static final String EXTRA_FLOATING = "extensions.webviewex.EXTRA_FLOATING";
	public static final String EXTRA_URL_WHITELIST = "extensions.webviewex.EXTRA_URL_WHITELIST";
	public static final String EXTRA_URL_BLACKLIST = "extensions.webviewex.EXTRA_URL_BLACKLIST";
	public static final String EXTRA_USE_WIDE_PORT = "extensions.webviewex.EXTRA_USE_WIDE_PORT";
	public static final String EXTRA_MEDIA_PLAYBACK_REQUIRES_USER_GESTURE = "extensions.webviewex.EXTRA_MEDIA_PLAYBACK_REQUIRES_USER_GESTURE";

	private static final String TAG = "mnist";
	protected RelativeLayout webViewPlaceholder;
	public WebView webView;

	public Bundle savedInstanceState;

	protected String url;

	protected boolean mediaPlaybackRequiresUserGesture;
	protected int layoutResource;

	private ViewGroup mViewParent;
	private ProgressBar mPageLoadingProgressBar = null;

	public static WebViewActivity self;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Load parameters from intent
		Bundle extras = getIntent().getExtras();
		url = extras.getString(EXTRA_URL, "about:blank");
		mediaPlaybackRequiresUserGesture = extras.getBoolean(EXTRA_MEDIA_PLAYBACK_REQUIRES_USER_GESTURE);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		layoutResource = R.layout.activity_web_view_fullscreen;
		// Initialize the UI
		self = this;
		initUI();
	}

	protected void initUI()
	{
		// Load layout from resources
		setContentView(layoutResource);
		// Retrieve UI elements
		webViewPlaceholder = ((RelativeLayout)findViewById(R.id.webViewPlaceholder));
		// Initialize the WebView if necessary
		if (webView == null)
		{
			// Create the webview
			webView = new WebView(this);
//			webView = new ProgressbarWebView(this, null);
//			initProgressBar();
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setAllowFileAccess(true);
			webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
			webSettings.setDomStorageEnabled(true);
			webSettings.setAppCacheEnabled(false);
//			webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.4; MI PAD Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.146 Safari/537.36");
			Log.i("mnist", webSettings.getUserAgentString());

            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webView.setScrollbarFadingEnabled(true);
			webView.setFitsSystemWindows(true);

			webSettings.setLoadsImagesAutomatically(true);
			// Add the callback to handle new page loads

			webView.setWebViewClient(
					new WebViewClient() {
						@Override
						public boolean shouldOverrideUrlLoading (WebView view, String url) {
							Log.i(TAG, "shouldOverrideUrlLoading(): url = " + url);
							return true;
						}
					}
			);
			webView.setWebChromeClient(
					new WebChromeClient(){
						@Override
						public void onProgressChanged(WebView webView, int i) {
							super.onProgressChanged(webView, i);
							if(mPageLoadingProgressBar!=null){
								mPageLoadingProgressBar.setProgress(i);
							}
						}
						@Override
						public boolean onJsAlert(WebView arg0, String arg1, String arg2, JsResult arg3) {
							System.out.println("@onJsAlert");
							return super.onJsAlert(null, arg1, arg2, arg3);
						}
						@Override
						public boolean onJsConfirm(WebView arg0, String arg1, String arg2,
												   JsResult arg3) {
							return super.onJsConfirm(arg0, arg1, arg2, arg3);
						}
					}
			);

			if (savedInstanceState != null) {
				webView.restoreState(savedInstanceState);
			} else {
				Log.i("mnist", url);
				webView.addJavascriptInterface(new JsInteration(), "nativeapp");
				webView.loadUrl(url);
			}
		}

		// Attach the WebView to its placeholder
		webViewPlaceholder.addView(webView);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{

		Log.i(TAG, "onConfigurationChanged (newConfig = " + newConfig.toString() + ")");

		if (webView != null)
		{
			// Remove the WebView from the old placeholder
			webViewPlaceholder.removeView(webView);
		}

		super.onConfigurationChanged(newConfig);

		// Reinitialize the UI
		initUI();
	}

	public void setVisibility(final boolean v){
		RelativeLayout baseview = (RelativeLayout)findViewById(R.id.view_baseview);
		if(v){
//			self.onResume();
			self.setVisible(true);

            ViewCompat.setAlpha(baseview, 1);
			baseview.setVisibility(View.VISIBLE);
			webView.setVisibility(View.VISIBLE);
		}else{
            ViewCompat.setAlpha(baseview, 0);
			webView.setVisibility(View.GONE);
			baseview.setVisibility(View.GONE);
            self.setVisible(false);
//			self.onPause();
		}
    }

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		// Save the state of the WebView
		webView.saveState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		// Restore the state of the WebView
		Log.i("mnist", "onRestoreInstanceState");
		webView.restoreState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack())
			webView.goBack();
		else
			super.onBackPressed();
	}

	public void onClosePressed(View view) {
//		Log.i("mnist", webView.getUrl());
//		Log.i("mnist", webView.getOriginalUrl());
		finish();
	}

	@Override
	public void finish(){
		self = null;
		super.finish();
		webView.clearHistory();
		webView.loadUrl("about:blank");
		webViewPlaceholder.removeView(webView);
	}
}