package wh.cmc.smart911beta13;


import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Menu;

public class MainActivity extends ListActivity{
	private LocalRecognitionService s;
	private ScheduleReceiver receiver = null;
	private ResponseReceiver callreceiver = null;
	private final String TAG = "Main activity";
	private ArrayAdapter<String> adapter_password, adapter_match;
	private List<String> list_password, list_match;

	private String Message = "HELP ME~SOS~(This is not a joke)";
	private static String call_number = "3473650704";
	private static String text_number = "3473650704";
	public static final String PARAM_ACTION_SERVICE = "PARAM_ACTION_SERVICE";
	private static final int REQUEST_CODE = 1234;
	
	/**********************************************/
	public static final long DETECTION_DURATION = 3000; 
	public static final long REPEAT_TIME = 6000;
	/**********************************************/
	
	private MainActivity _self;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    _self = this;
	    doBindService();
	    if (receiver == null) {
	    	IntentFilter filter = new IntentFilter(ScheduleReceiver.ACTION_RESP);
        	filter.addCategory(Intent.CATEGORY_DEFAULT);
        	receiver = new ScheduleReceiver();
        	registerReceiver(receiver, filter);
	    }
	    
	    if (callreceiver == null) {
		    IntentFilter callfilter = new IntentFilter(ResponseReceiver.ACTION_RESP);
	        callfilter.addCategory(Intent.CATEGORY_DEFAULT);
	        callreceiver = new ResponseReceiver();
	        registerReceiver(callreceiver, callfilter);
	    }
	  }

	@Override
	public void onDestroy(){
		if( receiver != null)
			unregisterReceiver(receiver);
		unbindService(mConnection);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	public class ResponseReceiver extends BroadcastReceiver {
    	public static final String ACTION_RESP =
    	      "com.example.intent.action.CALL_MESSAGE_PROCESSED";
    	
    	private void sendSMS(String _phoneNum) {
    	      SmsManager sms = SmsManager.getDefault();
    	      sms.sendTextMessage(_phoneNum, null, Message, null, null);
    	      Toast.makeText(_self, "Sent text message sent to " + _phoneNum,
		  				Toast.LENGTH_SHORT).show();
    	}
    	 
    	
    	private void call(String num) {
  	          Intent callIntent = new Intent(Intent.ACTION_CALL);
  	          String message = "tel:" + num;
  	          callIntent.setData(Uri.parse(message));
  	          startActivity(callIntent);
  	  	}
    	
       @Override
   	    public void onReceive(Context context, Intent intent) {
    	   sendSMS(text_number);
    	   call(call_number);
        }
   	}
	 
	

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder binder) {
	      s = ((LocalRecognitionService.MyBinder) binder).getService();
	      ((LocalRecognitionService.MyBinder) binder).registerHandler(new Handler());
	      Toast.makeText(MainActivity.this, "Connected",
	          Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	      s = null;
	    }
	};
	  
	  void doBindService() {
	    Boolean res = bindService(new Intent(this, LocalRecognitionService.class), mConnection,
	        Context.BIND_AUTO_CREATE);
	    if(res)
	    	Log.v(TAG,"Service is bound.");
	    else
	    	Log.v(TAG,"Service is not bound.");
	  }
	  
	  public void detectPassword(View view)
	  {
		  
		  EditText editText = (EditText) findViewById(R.id.msg_num);
          call_number = editText.getText().toString();
          editText = (EditText) findViewById(R.id.call_num);
          text_number = editText.getText().toString(); 
          Toast.makeText(this, "Saved numbers. Call to " + call_number +
        		  			   ". Text to " + text_number,
	  				Toast.LENGTH_SHORT).show();
          
          
		  if (list_password == null)
		    list_password = new ArrayList<String>();
		  if (adapter_password == null)
		    adapter_password = new ArrayAdapter<String>(this,
		        android.R.layout.simple_list_item_1, android.R.id.text1,
		        list_password);
		  
		  setListAdapter(adapter_password);
		  
		  PackageManager pm = getPackageManager();
	      List<ResolveInfo> activities = pm.queryIntentActivities(
	              new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
	      if (activities.size() == 0)
	      {
	   	   Log.v(TAG, "SHIT! Recognizer not present");
	   	   return;
	      }
		  
		  startVoiceRecognitionActivity();
	  }
	  
	  private void startVoiceRecognitionActivity()
	    {   
	        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Password Saving...");
	        startActivityForResult(intent, REQUEST_CODE);
	        
	    }
	  
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	    {
	        if (requestCode == REQUEST_CODE)
	        {
	        	if(resultCode == RESULT_OK){
	        		// Populate the wordsList with the String values the recognition engine thought it heard
	        		ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	                list_password.clear();
	                list_password.addAll(matches);
	                adapter_password.notifyDataSetChanged();
	                s.savePassword(matches);
	        	}
	        	else{
	        		Toast.makeText(this, "Can't recognize your passwords!",
		    				Toast.LENGTH_SHORT).show();
	        		Log.d(TAG, "result NOT ok");
	        	}
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	  
	  public void showServiceData(View view) {
	    if (s != null) {
	    	if(s.getWordList() != null){
	    		Toast.makeText(this, "Number of elements " + s.getWordList().size(),
	    				Toast.LENGTH_SHORT).show();
	    		
	    		if (list_match == null)
	    		    list_match = new ArrayList<String>();
	    		if(adapter_match == null)
	    			adapter_match = new ArrayAdapter<String>(this,
	    		        android.R.layout.simple_list_item_1, android.R.id.text1,
	    		        list_match);
	    		
	    		list_match.clear();
	    		list_match.addAll(s.getWordList());
	    		setListAdapter(adapter_match);
	    		adapter_match.notifyDataSetChanged();
	    	}
	    	else{
	    		Toast.makeText(this, "Wait..! No words were recognized.",
	    				Toast.LENGTH_SHORT).show();	    		
	    	}
	    }
	  }
	  
	  public void kickOff(View view){
		  	Toast.makeText(this, "You are in business!",
	  				Toast.LENGTH_SHORT).show();
	        Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        broadcastIntent.putExtra(PARAM_ACTION_SERVICE, true);
	        sendBroadcast(broadcastIntent);
	        Log.v(TAG, "Kicked off the scheduler!");
	  }
	  
	  public void stopService(View view){
		  Toast.makeText(this, "You are out of business!",
	  				Toast.LENGTH_SHORT).show();
		  Intent broadcastIntent = new Intent();
	        broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        broadcastIntent.putExtra(PARAM_ACTION_SERVICE, false);
	        sendBroadcast(broadcastIntent);
	        Log.v(TAG, "cancel the scheduler!");
	  }
}
