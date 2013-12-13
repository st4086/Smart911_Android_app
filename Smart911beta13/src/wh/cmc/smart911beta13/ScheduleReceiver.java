package wh.cmc.smart911beta13;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {
	private final String TAG = "Schedule Receiver";
	
	  public static final String ACTION_RESP =
	    	      "com.example.intent.action.MESSAGE_PROCESSED";

	  @Override
	  public void onReceive(Context context, Intent intent) {
		  if(intent.getBooleanExtra(MainActivity.PARAM_ACTION_SERVICE, false)) 
			  setAlarm(context);
		  else
			  cancelAlarm(context);
	  }
	  
	  private void setAlarm(Context context) {
		  AlarmManager service = (AlarmManager) context
			        .getSystemService(Context.ALARM_SERVICE);
			    Intent i = new Intent(context, StartServiceReceiver.class);
			    PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
			        PendingIntent.FLAG_CANCEL_CURRENT);
			    Calendar cal = Calendar.getInstance();
			    // Start 30 seconds after boot completed
			    cal.add(Calendar.SECOND, 1);
			    //
			    // Fetch every 30 seconds
			    // InexactRepeating allows Android to optimize the energy consumption
			    service.setRepeating(AlarmManager.RTC_WAKEUP,
			        cal.getTimeInMillis(), MainActivity.REPEAT_TIME, pending);

			    Log.v(TAG, "Finished setting alarm manager");
			    // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
			    // REPEAT_TIME, pending);		  
	  }
	  
	  private void cancelAlarm(Context context) {
		  Intent istop = new Intent(context, StartServiceReceiver.class);
		  PendingIntent senderstop = PendingIntent.getBroadcast(context,
				  0, istop, PendingIntent.FLAG_CANCEL_CURRENT);
		  AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		  service.cancel(senderstop);
		  Log.v(TAG, "Finished canceling alarm manager");
	  }
} 
