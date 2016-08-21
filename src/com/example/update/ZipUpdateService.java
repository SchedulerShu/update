package com.example.update;

import com.example.update.project.IProject;
import com.example.update.project.ProSCS05;
import com.example.update.utils.FileUtils;
import com.example.update.utils.FileUtils.OnCheckListener;
import com.example.update.utils.FileUtils.OnCopyListener;
import com.example.update.utils.UpdateUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ZipUpdateService extends Service implements OnClickListener {
	public static final String START_ACTION = "com.yftech.ACTION_ZIP_UPDATE";
	public static final String UPDATE_ACTION = "com.yftech.ACTIONI_UPDATE";
	private static boolean isUpdateing = false;
	public final static String TAG = "ZipUpdate";
	String path;
	Handler mHandler;

	IProject mProject;
	FileUtils fileUtils;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
		fileUtils = new FileUtils(mHandler);
		fileUtils.setOnCheckListener(mOnCheckListener);
		fileUtils.setOnCopyListener(mOnCopyListener);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addDataScheme("file");
		registerReceiver(br, intentFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			path = intent.getStringExtra("path");
		}
		if (path == null) {
			path = "/mnt/ext_sdcard2";
		}
		
		Log.d(TAG, "path :"+path);
		mProject = new ProSCS05(this, path);
		if (mProject == null || path == null)
			Log.d(TAG, "mProject  is null");
		Log.d(TAG, "cur project:" + mProject.toString());
		if (isUpdateing) {
			Log.w(TAG, "Current is updating, return! ");
			return super.onStartCommand(intent, flags, startId);
		}
		isUpdateing = true;
		showDialog();

		if (!mProject.isOsUpdateFileExist()) {
			if (rootView != null) {
				TextView mTextView = (TextView) rootView.findViewById(R.id.tv_os);
				setTextViewText(mTextView, "系统升级文件不存在！", Color.YELLOW);
				Button agree = (Button) rootView.findViewById(R.id.agree);
				agree.setClickable(false);
			}
		}
		checkToUpdateOs(path);
		return super.onStartCommand(intent, flags, startId);
	}

	private void setTextViewText(TextView tv, String text, int color) {
		tv.setTextColor(color);
		tv.setText(text);
	}

	private void checkToUpdateOs(String path) {
		if (!mProject.isOsUpdateFileExist())
			return;
		String version = mProject.getOsVersionFromFile();
		if (version == null)
			return;
		String curVersion = mProject.getOsVersion();
		Log.d(TAG, "App version:" + version);
		TextView mTextView = (TextView) rootView.findViewById(R.id.tv_os);
		if (version.contains(curVersion)) {
			if (mTextView != null) {
			}
		} else {
			if (mTextView != null) {
				mTextView.setTextColor(Color.WHITE);
				mTextView.setText("系统软件检测到新版本!");
			}
		}
	}

	WindowManager mWindowManager;

	View tipView = null;
	TextView tipTextView = null;
	ProgressBar tipProgress = null;

	private void showUpdateing(String msg) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		}
		{
			LayoutParams mLayoutParams = new LayoutParams();
			mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE
					| LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
			mLayoutParams.width = LayoutParams.MATCH_PARENT;
			mLayoutParams.height = LayoutParams.MATCH_PARENT;
			mLayoutParams.type |= WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
			mLayoutParams.gravity = Gravity.CENTER;
			tipView = LayoutInflater.from(this).inflate(R.layout.tip_layout, null);
			tipTextView = (TextView) tipView.findViewById(R.id.tip_tv);
			tipProgress = (ProgressBar) tipView.findViewById(R.id.tip_progress);
			mWindowManager.addView(tipView, mLayoutParams);
		}
	}

	private void updateTipMsg(final String msg, final int color) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "updateTipMsg:" + (tipTextView == null) + "  " + msg);
				if (tipTextView != null) {
					tipTextView.setTextColor(color);
					tipTextView.setText(msg);
				}
			}
		});
	}

	private void updateTipMsg(String msg) {
		updateTipMsg(msg, Color.WHITE);
	}

	private boolean updateArm(final String path) {
		if (!mProject.isOsUpdateFileExist())
			return false;
		showUpdateing("");
		fileUtils.copy(path);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.agree:
			dismissDialog();
			confirm();
			break;
		case R.id.cancel:
			isUpdateing = false;
			dismissDialog();
			// uninstallSelf();
			break;
		}
	}

	private void confirm() {
		if (mProject.isOsUpdateFileExist()) {
			updateArm(path);
		}
	}

	View rootView;

	private void showDialog() {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		}
		LayoutParams mLayoutParams = new LayoutParams();
		mLayoutParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_LAYOUT_NO_LIMITS
				| LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mLayoutParams.width = LayoutParams.MATCH_PARENT;
		mLayoutParams.height = LayoutParams.MATCH_PARENT;
		mLayoutParams.type |= WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
		mLayoutParams.gravity = Gravity.CENTER;
		rootView = LayoutInflater.from(this).inflate(R.layout.fullscreen, null);
		rootView.findViewById(R.id.cancel).setOnClickListener(this);
		rootView.findViewById(R.id.agree).setOnClickListener(this);
		mWindowManager.addView(rootView, mLayoutParams);
		Intent intent = new Intent(this, FullscreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
	}

	private void dismissDialog() {
		if (rootView != null && mWindowManager != null) {
			try {
				mWindowManager.removeView(rootView);
			} catch (Exception e) {
			}
		}
	}

	private BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "action:" + action);
			if (Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_REMOVED.equals(action)
					|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				Log.d(TAG, "path:" + path);
				if (path != null && path.contains(intent.getData().getPath())) {
					// uninstallSelf();
				}
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(br);
	};

	OnCheckListener mOnCheckListener = new OnCheckListener() {
		@Override
		public void onProgress(long progress) {
			tipProgress.setProgress((int) progress);
		}

		@Override
		public void onEnd(String msg) {
			if (msg != null) {
				tipProgress.setVisibility(View.GONE);
				updateTipMsg("升级失败：" + msg);
			} else {
				tipProgress.setVisibility(View.GONE);
				updateTipMsg("正在重启进行升级......");

				UpdateUtils.update(UpdateUtils.UPDATE_OS);

			}
		}

		@Override
		public void onCheckStart(long length) {
			tipProgress.setVisibility(View.VISIBLE);
			tipProgress.setMax((int) length);
			updateTipMsg("请勿关机、正在校验软件......");
		}
	};
	OnCopyListener mOnCopyListener = new OnCopyListener() {
		@Override
		public void onCopyStart(long length) {
			tipProgress.setVisibility(View.VISIBLE);
			tipProgress.setMax((int) length);
			updateTipMsg("请勿关机、正在拷贝软件......");
		}

		@Override
		public void onCopyProgress(long progress) {
			tipProgress.setProgress((int) progress);
		}

		@Override
		public void onCopyEnd(String msg) {
			if (msg != null) {
				tipProgress.setVisibility(View.GONE);
				updateTipMsg("升级失败：" + msg);
			} else {
				fileUtils.check(path);
			}
		}
	};

	public static String getSdkVersion() {
		Log.d(TAG, "os version: " + android.os.Build.VERSION.RELEASE);
		return android.os.Build.VERSION.RELEASE;
	}
}
