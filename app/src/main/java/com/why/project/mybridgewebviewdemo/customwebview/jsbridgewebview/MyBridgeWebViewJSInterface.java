package com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.why.project.mybridgewebviewdemo.customwebview.utils.WebviewGlobals;


/**
 * Created by HaiyuKing
 * Used Java和Java script交互的工具类
 */

public class MyBridgeWebViewJSInterface {


    private static final String TAG = MyBridgeWebViewJSInterface.class.getSimpleName();

    private static MyBridgeWebViewJSInterface instance;
    /**
     * 依赖的窗口
     */
    private Context context;
    private static MyBridgeWebView myBridgeWebView;


    public static MyBridgeWebViewJSInterface getInstance(Context context, MyBridgeWebView myBridgeWebView) {
        if (instance == null) {
            instance = new MyBridgeWebViewJSInterface();
        }
        instance.context = context;
        instance.myBridgeWebView = myBridgeWebView;
        return instance;
    }

    /*======================================实际项目中用到的方法================================================*/

    /**404界面的刷新【js调用的方法必须添加@JavascriptInterface】*/
    @JavascriptInterface
    public void refresh(){
        Log.e(TAG,"mWebView.getRefreshUrl()="+myBridgeWebView.getRefreshUrl());
        //两个使用webview的地方不在同一个线程，所以需要特别处理下
        //方案一【不合适，如果不是在activity中调用呢，在DialogFragment中调用，可以会出现问题】
        /*((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myWebView.loadWebUrl(myWebView.getRefreshUrl());
            }
        });*/
        //方案二
        myBridgeWebView.post(new Runnable() {
            @Override
            public void run() {
                myBridgeWebView.loadUrl(myBridgeWebView.getRefreshUrl());
            }
        });
    }

    /**
     * 打开文件管理器选择文件的Intent
     */
    public void chooseFile() {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        String IMAGE_UNSPECIFIED = "*/*";
        innerIntent.setType(IMAGE_UNSPECIFIED); // 查看类型
        Intent wrapperIntent = Intent.createChooser(innerIntent, "File Browser");
        ((Activity) context).startActivityForResult(wrapperIntent, WebviewGlobals.CHOOSE_FILE_REQUEST_CODE);
    }
}
