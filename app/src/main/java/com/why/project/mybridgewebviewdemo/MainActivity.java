package com.why.project.mybridgewebviewdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.why.project.mybridgewebviewdemo.customwebview.jsbridgewebview.MyBridgeWebView;
import com.why.project.mybridgewebviewdemo.customwebview.utils.WebviewGlobals;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	/**jsBridgeWebview*/
	private MyBridgeWebView myWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
		initDatas();
		initEvents();
	}

	private void initViews() {
		myWebView = findViewById(R.id.web_view);
	}

	private void initDatas() {
		myWebView.setDefaultHandler(new CustomBridgeHandler());//jsBridge
		//加载网址
		myWebView.loadLocalUrl("demo.html");
		initJsBridge();

		//实现webview只可滑动不可点击【项目中需要用到的时候再解开注释】
		//http://blog.csdn.net/mjjmjc/article/details/47105001
		//http://blog.csdn.net/qq_32452623/article/details/52304628
		/*myWebView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});*/
	}

	/*=================================jsBridge=================================*/
	/**
	 * 自定义BridgeHandler【用于接收html发送给Java的消息（通过data的值区分是哪个消息；function用于回调给js）】*/
	class CustomBridgeHandler implements BridgeHandler {
		@Override
		public void handler(String data, CallBackFunction function) {
			Log.i(TAG, "接收html发送给Java的消息（通过data的值区分是哪个消息）：" + data);
			if(data.equals("onPageFinished")){//onPageFinished 跟html中的保持一致
				// TODO 请求接口获取数据
				if(function != null){
					function.onCallBack("这个是onPageFinished方法执行后js发送给java消息后，java回调给js的初始值");
				}
			}else{
				if(data.equals("getNewData")){
					// TODO 请求接口获取数据
					if(function != null){
						function.onCallBack("这个是js发送给java消息后返回的数据");
					}
				}
			}
		}
	}

	//jsBridge 初始化一些事件监听
	private void initJsBridge(){
		//【js调用Java的方法】必须和js中的调用函数名相同，注册具体执行函数，类似java实现类。
		myWebView.registerHandler("functionInJava", new BridgeHandler() {
			@Override
			public void handler(String data, CallBackFunction function) {
				Log.i(TAG, "接收html发送给Java的数据：" + data);

				String callbackData = "这个是js调用java方法后返回的数据";
				function.onCallBack(callbackData);
			}
		});
	}

	private void initEvents() {
		findViewById(R.id.btn_calljsfunction).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Java调用js的方法
				//发送给js的参数，使用Json格式
				String params = "{\"username\":\"why\",\"password\":\"123456\"}";
				myWebView.callHandler("functionInJs",params, new CallBackFunction() {
					@Override
					public void onCallBack(String data) {
						// TODO Auto-generated method stub
						//处理html返回给Java的数据
						Toast.makeText(MainActivity.this,data,Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/*=========================================实现webview调用选择文件的功能==============================================*/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.w(TAG, "{onActivityResult}resultCode="+resultCode);
		Log.w(TAG, "{onActivityResult}requestCode="+requestCode);
		Log.w(TAG, "{onActivityResult}data="+data);
		if (resultCode == Activity.RESULT_OK) {
			//webview界面调用打开本地文件管理器选择文件的回调
			if (requestCode == WebviewGlobals.CHOOSE_FILE_REQUEST_CODE ) {
				Uri result = data == null ? null : data.getData();
				Log.w(TAG,"{onActivityResult}文件路径地址：" + result.toString());

				//如果mUploadMessage或者mUploadCallbackAboveL不为空，代表是触发input[type]类型的标签
				if (null != myWebView.getMyBridgeWebChromeClient().getmUploadMessage() || null != myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL()) {
					if (myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() != null) {
						onActivityResultAboveL(requestCode, data);//5.0++
					} else if (myWebView.getMyBridgeWebChromeClient().getmUploadMessage() != null) {
						myWebView.getMyBridgeWebChromeClient().getmUploadMessage().onReceiveValue(result);//将文件路径返回去，填充到input中
						myWebView.getMyBridgeWebChromeClient().setmUploadMessage(null);
					}
				}else{
					//此处代码是处理通过js方法触发的情况
					Log.w(TAG,"{onActivityResult}文件路径地址(js)：" + result.toString());
				}
			}
		}else if(resultCode == RESULT_CANCELED){//resultCode == RESULT_CANCELED 解决不选择文件，直接返回后无法再次点击的问题
			if (myWebView.getMyBridgeWebChromeClient().getmUploadMessage() != null) {
				myWebView.getMyBridgeWebChromeClient().getmUploadMessage().onReceiveValue(null);
				myWebView.getMyBridgeWebChromeClient().setmUploadMessage(null);
			}
			if (myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() != null) {
				myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL().onReceiveValue(null);
				myWebView.getMyBridgeWebChromeClient().setmUploadCallbackAboveL(null);
			}
		}
	}

	//5.0以上版本，由于api不一样，要单独处理
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultAboveL(int requestCode, Intent data) {

		if (myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL() == null) {
			return;
		}
		Uri result = null;
		if (requestCode == WebviewGlobals.CHOOSE_FILE_REQUEST_CODE) {//打开本地文件管理器选择图片
			result = data == null ? null : data.getData();
		}
		Log.w(TAG,"{onActivityResultAboveL}文件路径地址："+result.toString());
		myWebView.getMyBridgeWebChromeClient().getmUploadCallbackAboveL().onReceiveValue(new Uri[]{result});//将文件路径返回去，填充到input中
		myWebView.getMyBridgeWebChromeClient().setmUploadCallbackAboveL(null);
		return;
	}

}
