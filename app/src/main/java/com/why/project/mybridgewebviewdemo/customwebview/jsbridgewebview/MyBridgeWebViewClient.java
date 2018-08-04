package com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;


/**
 * Used 主要处理解析，渲染网页等浏览器做的事情。
 * 帮助WebView处理各种通知、请求事件：比如页面加载的开始、结束、失败时的对话框提示
 */
public class MyBridgeWebViewClient extends BridgeWebViewClient {

	private static final String TAG = MyBridgeWebViewClient.class.getSimpleName();

	/**依赖的窗口*/
	private Context context;
	private MyBridgeWebView myBridgeWebView;

	/**进度加载对话框*/
	private MyBridgeWebViewProgressDialog progressDialog;

	private boolean blockLoadingNetworkImage=false;//WebView 图片延迟加载

	private boolean needClearHistory = false;//是否需要清除历史记录

	public MyBridgeWebViewClient(Context context, MyBridgeWebView myBridgeWebView) {
		super(myBridgeWebView);
		this.context = context;
		this.myBridgeWebView = myBridgeWebView;
	}

	/**
	 * 重写此方法表明点击网页内的链接由自己处理，而不是新开Android的系统browser中响应该链接。
	 */
	@Override
	public boolean shouldOverrideUrlLoading(WebView webView, String url) {
		Log.e(TAG,"{shouldOverrideUrlLoading}url="+url);
		if(url.startsWith("tel:")) {
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
			context.startActivity(intent);
			return true;
		}else{
			/*//view.loadUrl(url);//根据传入的参数再去加载新的网页
			return false;//表示当前的webview可以处理打开新网页的请求，不用借助系统浏览器,【false 显示frameset, true 不显示Frameset】*/
			return super.shouldOverrideUrlLoading(webView,url);//为了实现Bridge
		}
	}

	/**
	 * 网页加载开始时调用，显示加载提示旋转进度条
	 */
	@Override
	public void onPageStarted(WebView webView, String url, Bitmap bitmap) {
		super.onPageStarted(webView, url, bitmap);
		Log.e(TAG,"{onPageStarted}url="+url);
		showProgressDialog();
	}

	/**
	 * 网页加载完成时调用，比如：隐藏加载提示旋转进度条*/
	@Override
	public void onPageFinished(WebView webView, String url) {
		super.onPageFinished(webView, url);
		Log.e(TAG,"{onPageFinished}url="+url);
		dismissProgressDialog();

		CookieManager cookieManager = CookieManager.getInstance();
		String CookieStr = cookieManager.getCookie(url);
		Log.e(TAG, "{Cookie:}" + CookieStr);

		//执行js的方法，让js发送初始化完成后的消息给Java【这个比较特殊，实际项目中可能用的到】
		//sendMegToJavaFunctionInJs 类似handle的message.what
		//data 类似handle的mseeage.obj
		//CallBackFunction 回调函数
		//jsBridge
		myBridgeWebView.callHandler("sendMegToJavaFunctionInJs","", new CallBackFunction() {
			@Override
			public void onCallBack(String data) {
				// TODO Auto-generated method stub
				Log.i(TAG, "" + data);
			}
		});
	}

	/**
	 * 网页加载失败时调用，隐藏加载提示旋转进度条
	 * 捕获的是 文件找不到，网络连不上，服务器找不到等问题
	 */

	@Override
	public void onReceivedError(WebView webView, int errorCode,
								String description, String failingUrl) {
		super.onReceivedError(webView, errorCode, description, failingUrl);
		Log.e(TAG,"{onReceivedError}failingUrl="+failingUrl);
		dismissProgressDialog();

		myBridgeWebView.setRefreshUrl(failingUrl);//保存网络异常时的URL地址，用于刷新
		myBridgeWebView.loadLocalUrl("404.html");
	}

	/**
	 * 直接捕获到404
	 */
	@Override
	public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
		super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
		String url = "";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			url = webResourceRequest.getUrl().toString();
		} else {
			url = webResourceRequest.toString();
		}
		Log.e(TAG,"{onReceivedHttpError}url="+url);
	}

	/**
	 * 显示进度加载对话框
	 * param msg 显示内容
	 */
	public void showProgressDialog() {
		try {
			if (progressDialog == null) {
				progressDialog = new MyBridgeWebViewProgressDialog(context);
			}
			progressDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 隐藏进度加载对话框
	 */
	public void dismissProgressDialog() {
		try {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*=================================根据需要清除历史记录=================================*/
	@Override
	public void doUpdateVisitedHistory(WebView webView, String url, boolean isReload) {
		super.doUpdateVisitedHistory(webView, url, isReload);
		Log.w(TAG, "{doUpdateVisitedHistory}needClearHistory="+needClearHistory);
		if(needClearHistory){
			webView.clearHistory();//清除历史记录
			needClearHistory = false;
		}
	}

	public void setNeedClearHistory(boolean needClearHistory) {
		this.needClearHistory = needClearHistory;
	}

	/**扩充数据库的容量*/
	public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
		quotaUpdater.updateQuota(estimatedSize * 2);
	}
}
