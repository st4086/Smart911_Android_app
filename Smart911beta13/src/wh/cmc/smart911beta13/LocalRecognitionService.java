package wh.cmc.smart911beta13;

import java.util.ArrayList;
import java.util.List;

import wh.cmc.smart911beta13.MainActivity.ResponseReceiver;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class LocalRecognitionService extends Service implements OnSignalsDetectedListener{

  private final IBinder mBinder = new MyBinder();
  private final String TAG = "LocalRecognitionService";
  private static LocalRecognitionService _service;
  private String[] CheckArray, PasswordArray = {"hello", "helo", "h", "halo", "hallow"};
  
  private Handler mHandler;
  private int check;
  ArrayList<String> arrlist_matches;
  
  
  private DetectorThread detectorThread;
  private RecorderThread recorderThread;
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
	Log.v(TAG, "Roger that. Start Service.");
	    
	_service = this;
	recorderThread = new RecorderThread();
	recorderThread.start();
	detectorThread = new DetectorThread(recorderThread);
	detectorThread.setOnSignalsDetectedListener(LocalRecognitionService._service);
	detectorThread.start();
	
	Log.v(TAG, "start detection");
	
	try {
		Thread.sleep(MainActivity.DETECTION_DURATION);
	} catch (InterruptedException e) {
		// 
		e.printStackTrace();
	}
	
	Log.v(TAG, "now going to stop detection");
	
	if (detectorThread != null) {
		detectorThread.stopDetection();
		detectorThread = null;
	}
	if (recorderThread != null) {
		recorderThread.stopRecording();
		recorderThread = null;
	}
	
	Log.v(TAG, "Stopped detection. Finished service.");
	return Service.START_NOT_STICKY;
  }

  //Create runnable for posting
  final Runnable mRunVR = new Runnable() {
      public void run() {
    	  prepare_and_start_VR();
      }
  };
  
  @Override
	public void onWhistleDetected() {
		//***********************//
	Log.v(TAG, "ohhhh Screaming was detected!");
	if (detectorThread != null) {
		detectorThread.stopDetection();
		detectorThread = null;
	}
	if (recorderThread != null) {
		recorderThread.stopRecording();
		recorderThread = null;
	}
	Intent broadcastIntent = new Intent();
    broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    broadcastIntent.putExtra(MainActivity.PARAM_ACTION_SERVICE, false);
    sendBroadcast(broadcastIntent);
    Log.v(TAG, "cancel the scheduler!");
	mHandler.post(mRunVR);
		//***********************//
	}
	
  
  private void prepare_and_start_VR(){
	  PackageManager pm = getPackageManager();
      List<ResolveInfo> activities = pm.queryIntentActivities(
              new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
      if (activities.size() == 0)
      {
   	   Log.v(TAG, "SHIT! Recognizer not present");
   	   return;
      }
      	
		check = 4;
		Log.v(TAG, "check: "+check);
		Log.v(TAG, "Aiite! Ready to go start recognition. ");
		
		start_VR();
  }
  
  private void start_VR()
  {
  	SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);       
    sr.setRecognitionListener(new listener());
  	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);        
  	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
  	intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.musicg.demo.android");
  	sr.startListening(intent);       
  }
  
  
  class listener implements RecognitionListener          
  {
           public void onReadyForSpeech(Bundle params)
           {
                    Log.d(TAG, "onReadyForSpeech");
           }
           public void onBeginningOfSpeech()
           {
                    Log.d(TAG, "onBeginningOfSpeech");
           }
           public void onRmsChanged(float rmsdB)
           {
                    ;//Log.d(TAG, "onRmsChanged");
           }
           public void onBufferReceived(byte[] buffer)
           {
                    ;//Log.d(TAG, "onBufferReceived");
           }
           public void onEndOfSpeech()
           {
                    Log.d(TAG, "onEndofSpeech");
           }
           public void onError(int error)
           {
                    Log.d(TAG,  "error " +  error + " check: " + check);
                    if(check > 0){
                   	 start_VR();
                   	 check--;
                    }
                    else{
                    	Log.d(TAG,  "Shit too many errors YOU are OUT!");
                    	Intent broadcastIntent = new Intent();
	        	        broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
	        	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	        	        broadcastIntent.putExtra(MainActivity.PARAM_ACTION_SERVICE, true);
	        	        sendBroadcast(broadcastIntent);
	        	        Log.v(TAG, "Kicked off the scheduler!");
        	        }
                    //}
           }
           public void onResults(Bundle results)                   
           {
               arrlist_matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
               if (arrlist_matches == null) {
                   Log.e(TAG, "No voice results");
                   if(check > 0){
                  	 start_VR();
                  	 check--;
                   }
                   else{	
                	    Intent broadcastIntent = new Intent();
            	        broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
            	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            	        broadcastIntent.putExtra(MainActivity.PARAM_ACTION_SERVICE, true);
            	        sendBroadcast(broadcastIntent);
            	        Log.v(TAG, "Kicked off the scheduler!");
                   }
               } else {
                   Log.d(TAG, "Printing matches: ");
                   for (String match : arrlist_matches) {
                       Log.d(TAG, match);
                   }
                   int Emergency = 0;
                   CheckArray = new String[arrlist_matches.size()];
       			 CheckArray = arrlist_matches.toArray(CheckArray);
       			 for(int i = 0; i < CheckArray.length; i++){
       	        	for(int j=0; j < PasswordArray.length; j++){
       	        		if(CheckArray[i].equals(PasswordArray[j])){
       	        			//***********************
       	        			Log.d(TAG, "Get U!!!");  //Got the emergency
       	        			Intent broadcastIntent = new Intent();
       	        			broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
       	        			broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
       	        			sendBroadcast(broadcastIntent);
       	        			
       	        		 Intent activity_intent = new Intent(_service, MainActivity.class);
       	        		 activity_intent.setAction(Intent.ACTION_MAIN);
       	        		 activity_intent.addCategory(Intent.CATEGORY_LAUNCHER);
       	        		 activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       	        		 startActivity(activity_intent);
       	        		 Log.v(TAG, "Acitivity is about to be activated");
       	        			Emergency = 1;
       	        			//***********************
       	        		}
       	        	}
       			 }
       			 if(Emergency == 0){
       	        	if(check > 0){
                        	 start_VR();
                        	 check--;
                         }
                         else{
                        	Intent broadcastIntent = new Intent();
                 	        broadcastIntent.setAction(ScheduleReceiver.ACTION_RESP);
                 	        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                 	        broadcastIntent.putExtra(MainActivity.PARAM_ACTION_SERVICE, true);
                 	        sendBroadcast(broadcastIntent);
                 	        Log.v(TAG, "Kicked off the scheduler!");
                         }
       			 }
               }
               //releaseSem();
           }
           
           public void onPartialResults(Bundle partialResults)
           {
                    Log.d(TAG, "onPartialResults");
           }
           public void onEvent(int eventType, Bundle params)
           {
                    Log.d(TAG, "onEvent " + eventType);
           }
  }
  
  @Override
  public IBinder onBind(Intent arg0) {
    return mBinder;
  }

  public class MyBinder extends Binder {
	void registerHandler(Handler handler) {
		    mHandler = handler;
	}
	
    LocalRecognitionService getService() {
      return LocalRecognitionService.this;
    }
  }

  public List<String> getWordList() {
    return arrlist_matches;
  }

  public void savePassword(ArrayList<String> ps){
	  PasswordArray = new String[ps.size()];
	  PasswordArray = ps.toArray(PasswordArray);
	  
  }
} 