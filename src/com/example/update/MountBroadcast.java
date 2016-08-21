package com.example.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MountBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent paramIntent) {
		if ("android.intent.action.MEDIA_MOUNTED".equals(paramIntent.getAction())) {
			
			String path = paramIntent.getData().getPath();
			Log.d("shufu", "param :" + path);
			Intent localIntent = new Intent("com.yftech.ACTION_ZIP_UPDATE");
			localIntent.putExtra("path", path);
			context.startService(localIntent);
		}
	}

}
