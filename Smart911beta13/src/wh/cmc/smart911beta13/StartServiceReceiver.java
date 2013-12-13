package wh.cmc.smart911beta13;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceReceiver extends BroadcastReceiver {

	private final String TAG = "StartServiceReceiver";
	
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent service = new Intent(context, LocalRecognitionService.class);
    context.startService(service);
    Log.v(TAG, "Start Service");
  }
} 