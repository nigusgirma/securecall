package com.portsip;

import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.portsip.main.ConnectedCall;
import com.portsip.main.HomeTabActivity;
import com.portsip.main.SendSms;
import com.portsip.main.Utils;
import com.portsip.bulk.chooseservice;
import com.portsip.service.PortSipService;
import com.portsip.service.PortSipService.MyBinder;
import com.portsip.util.Line;
import com.portsip.util.Session;
import com.portsip.util.SipContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import helper.SessionManager;

public class MyApplication extends Application implements OnPortSIPEvent{
	public static final String TAG = MyApplication.class.getSimpleName();
	private static ArrayList<String> userDetail=new ArrayList<String>();
	private static int FM_NOTIFICATION_ID=1000;
	private static ArrayList<String> phone_list=new ArrayList<>();
	private RequestQueue mRequestQueue;
	private static MyApplication mInstance;
	private String globalVariable;
	private static String gName;
	private static String gPhone;
	private static String gPassword;
	private static String gEmail;
	private boolean dialogFlag;
	private static boolean gFlag;
	private static boolean gDialogFlag;
	Ringtone r;
	boolean statusFlag;
	Intent srvIntent = null;
	PortSipService portSrv = null;
	MyServiceConnection connection = null;
	PortSipSdk sdk;
	boolean conference = false;
	private boolean _SIPLogined = false;// record register status
	HomeTabActivity callscreen;
	chooseservice serv;
	MainActivity mainActivity;
	ConnectedCall connectedCall;
	HomeTabActivity hometabActivity;
	SendSms sendSms;
	AlertDialog alertDialog2;
	Calling Callingact;
	private String inputNumber;
	private SurfaceView remoteSurfaceView = null;
	private SurfaceView localSurfaceView = null;
	static final private Line[] _CallSessions = new Line[Line.MAX_LINES];// record
	// all
	// audio-video
	// sessions
	static final private ArrayList<SipContact> contacts = new ArrayList<SipContact>();
	private Line _CurrentlyLine = _CallSessions[Line.LINE_BASE];// active line
	static final String SIP_ADDRRE_PATTERN = "^(sip:)(\\+)?[a-z0-9]+([_\\.-][a-z0-9]+)*@([a-z0-9]+([\\.-][a-z0-9]+)*)+\\.[a-z]{2,}(:[0-9]{2,5})?$";
	public static final String SESSION_CHANG = MyApplication.class
			.getCanonicalName() + "Session change";
	public static final String CONTACT_CHANG = MyApplication.class
			.getCanonicalName() + "Contact change";
	private boolean flag;

	public void sendSessionChangeMessage(String message, String action) {
		//Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.it_icon)
						.setContentTitle("SecureCall");
		//				.setContentText(message).setSound(soundUri);
		Intent notificationIntent;
		try {
			notificationIntent = new Intent(this, MainActivity.class);
		}catch (Exception e){
			notificationIntent = new Intent(this, HomeTabActivity.class);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(contentIntent);
		// Add as notification
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(FM_NOTIFICATION_ID, builder.build());
		Intent broadIntent = new Intent(action);
		broadIntent.putExtra("description", message);
		sendBroadcast(broadIntent);

	}
	public Line[] getLines() {
		return _CallSessions;
	}
	List<SipContact> getSipContacts() {
		return contacts;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		sdk = new PortSipSdk();
		srvIntent = new Intent(this, PortSipService.class);
		connection = new MyServiceConnection();
		sdk.setOnPortSIPEvent(this);
		localSurfaceView = Renderer.CreateLocalRenderer(this);
		remoteSurfaceView = Renderer.CreateRenderer(this, true);
		bindService(srvIntent, connection, BIND_AUTO_CREATE);
		for (int i = 0; i < _CallSessions.length; i++) {
			_CallSessions[i] = new Line(i);
		}
		//_CallSessions[0];
	}
	@Override
	public void onTerminate() {
		super.onTerminate();
		unbindService(connection);
		connection = null;
	}
	public SurfaceView getRemoteSurfaceView() {
		return remoteSurfaceView;
	}

	public SurfaceView getLocalSurfaceView() {
		return localSurfaceView;
	}
	public PortSipSdk getPortSIPSDK() {
		return sdk;
	}

	public void setStatusFlag(boolean flag) {
		this.statusFlag = flag;
	}

	public boolean getStatusFlag(){return this.statusFlag;}

	class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyBinder binder = (MyBinder) service;

			portSrv = binder.getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			portSrv = null;
		}
	}
	public boolean isOnline() {
		return _SIPLogined;
	}
	public void setConfrenceMode(boolean state) {
		conference = state;
	}
	public boolean isConference() {
		return conference;
	}
	public void setOnlineState(boolean state) {
		_SIPLogined = state;
	}
	public void showTipMessage(String text){
		if (mainActivity != null) {
			NumpadFragment fragment = mainActivity.getNumpadFragment();
			if (fragment != null) {
				fragment.showTips(text);
			}
		}
	}
	public int answerSessionCall(Line sessionLine, boolean videoCall){
		long seesionId = sessionLine.getSessionId();
		int rt = PortSipErrorcode.INVALID_SESSION_ID;
		if(seesionId != PortSipErrorcode.INVALID_SESSION_ID) {
			rt = sdk.answerCall(sessionLine.getSessionId(), videoCall);
		}
		if(rt == 0){
			sessionLine.setSessionState(true);
			setCurrentLine(sessionLine);
			if(videoCall) {
				sessionLine.setVideoState(true);
			}else{
				sessionLine.setVideoState(false);
			}
			updateSessionVideo();
			if (conference) {
				sdk.joinToConference(sessionLine.getSessionId());
			}
			showTipMessage(sessionLine.getLineName()
					+ ": Call established");
		}else{
			sessionLine.reset();
			showTipMessage(sessionLine.getLineName()
					+ ": failed to answer call !");
		}
		return rt;
	}
	public void updateSessionVideo(){
		if( mainActivity != null) {
			VideoCallFragment fragment = mainActivity.getVideoCallFragment();
			if (fragment != null) {
				fragment.updateVideo();
			}
		}
	}
	//register event listener
	@Override
	public void onRegisterSuccess(String reason, int code) {
		_SIPLogined = true;
		if (mainActivity != null) {
			HomeFragment fragment;
			fragment = mainActivity.getHomeFragment();
			if (fragment != null) {
				fragment.onRegisterSuccess(reason, code);
			}
		}
	}
	@Override
	public void onRegisterFailure(String reason, int code) {
		_SIPLogined = false;

		if (mainActivity != null) {
			HomeFragment fragment;
			fragment = mainActivity.getHomeFragment();
			if (fragment != null) {
				fragment.onRegisterFailure(reason, code);
			}
		}
	}
	// call event listener
	@Override
    public void onInviteIncoming(long sessionId, final String callerDisplayName,String caller, final String calleeDisplayName,String callee,
								 String audioCodecs, String videoCodecs, boolean existsAudio,
								 boolean existsVideo) {

		final Line tempSession = findIdleLine();
		if (tempSession == null)// all sessions busy
		{
			sdk.rejectCall(sessionId, 486);
			return;
		} else {
			tempSession.setRecvCallState(true);
		}
		if (existsVideo) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		if (existsAudio) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		tempSession.setSessionId(sessionId);
		tempSession.setVideoState(existsVideo);
		String comingCallTips = " Incoming Call \n\n\n   "+ callerDisplayName;// + "<" + caller +">";
		tempSession.setDescriptionString(comingCallTips);
		sendSessionChangeMessage(comingCallTips, SESSION_CHANG);
		setCurrentLine(tempSession);
		if(existsVideo){
			updateSessionVideo();
			final Line curSession = tempSession;
			AlertDialog alertDialog = new AlertDialog.Builder(mainActivity).create();
			alertDialog.setTitle("Incoming Video Call");
			alertDialog.setMessage(comingCallTips);
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Audio",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//Answer Audio call
							answerSessionCall(curSession,false);
						}
					});
			alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Video",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//Answer Video call
							answerSessionCall(curSession,true);
						}
					});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Reject",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//Reject call
							if(curSession.getSessionId() != PortSipErrorcode.INVALID_SESSION_ID) {
								sdk.rejectCall(curSession.getSessionId(), 486);
							}
							curSession.reset();
							showTipMessage("Rejected call");
						}
					});
			// Showing Alert Message
			alertDialog.show();
		}
		else {//Audio call
			final Line curSession = tempSession;
			String incomingnumber;
			incomingnumber = caller;
			String mycaller = incomingnumber.toString();
			String s = mycaller;
			final String requiredString = s.substring(s.indexOf(":") + 1, s.indexOf("@"));
			Log.d(TAG, "=====>The incoming number is:" + requiredString);
			try {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				r.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
			;
			//AlertDialog alertDialog;

			try {
			//startActivity(new Intent(this,MainActivity.class));
			alertDialog2 = new AlertDialog.Builder(mainActivity).create();
			alertDialog2.setTitle("Incoming Call");
			Log.d(TAG, "Incomming caller id" + comingCallTips.toString());
			alertDialog2.setMessage(comingCallTips);
			alertDialog2.show();
			Window win = alertDialog2.getWindow();
			win.setContentView(R.layout.incoming_call);
			//Answer
			TextView txtView = (TextView) win.findViewById(R.id.txtName);
			txtView.setText(comingCallTips);
			Button accept_btn = (Button) win.findViewById(R.id.answer);
			accept_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					answerSessionCall(curSession, false);
					//insertPlaceholderCall(getContentResolver(), requiredString, calleeDisplayName);
					beginConnected(requiredString,callerDisplayName,"Secure Connected");
					dialogFlag=true;
					r.stop();
					alertDialog2.dismiss();
				}
			});
			//Reject
			Button reject_btn = (Button) win.findViewById(R.id.reject);
			reject_btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					sdk.rejectCall(curSession.getSessionId(), 486);
					curSession.reset();
					insertPlaceholderCallMissed(getContentResolver(), requiredString, callerDisplayName);
					showTipMessage("Rejected call");
					r.stop();
					alertDialog2.dismiss();
				}
			});
			// You should write your own code to play the wav file here for alert
			// the incoming call(incoming tone);
			} catch (Exception e) {
				if(hometabActivity!=null) {
					final AlertDialog alertDialog2 = new AlertDialog.Builder(hometabActivity).create();
					alertDialog2.setTitle("Incoming Call");
					Log.d(TAG, "Incomming caller id" + comingCallTips.toString());
					alertDialog2.setMessage(comingCallTips);
					alertDialog2.show();
					Window win = alertDialog2.getWindow();
					win.setContentView(R.layout.incoming_call);
					//Answer
					TextView txtView = (TextView) win.findViewById(R.id.txtName);
					txtView.setText(comingCallTips);
					Button accept_btn = (Button) win.findViewById(R.id.answer);
					accept_btn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							answerSessionCall(curSession, false);
							//	insertPlaceholderCall(getContentResolver(), requiredString, calleeDisplayName);
							beginConnected(requiredString, calleeDisplayName, "Secure Connected");
							dialogFlag = true;
							r.stop();
							alertDialog2.dismiss();
						}
					});
					//Reject
					Button reject_btn = (Button) win.findViewById(R.id.reject);
					reject_btn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							sdk.rejectCall(curSession.getSessionId(), 486);
							curSession.reset();
							insertPlaceholderCallMissed(getContentResolver(), requiredString, calleeDisplayName);
							showTipMessage("Rejected call");
							r.stop();
							alertDialog2.dismiss();
						}
					});
				}else{
					try {
						Intent startActivity = new Intent();
						startActivity.setClass(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
						pi.send(this,0,null);
					} catch (CanceledException e2) {
						e2.printStackTrace();
					}

				}
			}
			Log.d(TAG, "-----------Incomgin call number is---------------" + comingCallTips);
			//bringToFront(requiredString,calleeDisplayName,"Secure Connected");
			//Toast.makeText(this, "Incoming Call"+comingCallTips, Toast.LENGTH_LONG).show();
		}
		try {
			alertDialog2.show();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public static void insertPlaceholderCallMissed(ContentResolver contentResolver, String number,String displayName){
		ContentValues values = new ContentValues();
		values.put(CallLog.Calls.NUMBER, number);
		values.put(CallLog.Calls.DATE, System.currentTimeMillis());
		values.put(CallLog.Calls.DURATION, 0);
		values.put(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
		values.put(CallLog.Calls.NEW, 1);
		values.put(CallLog.Calls.CACHED_NAME, displayName);
		values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
		values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
		Log.d(TAG, "Inserting call log placeholder for " + number);
		contentResolver.insert(CallLog.Calls.CONTENT_URI, values);
	}
	//@Override

	@Override
	public void onInviteTrying(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("Call is trying...");
		sendSessionChangeMessage("Call is trying...", SESSION_CHANG);
		//Toast.makeText(getApplicationContext(),"Call is trying",Toast.LENGTH_LONG).show();
		Utils.playTone(this, ToneGenerator.TONE_SUP_DIAL);

	}
	@Override
	public void onInviteSessionProgress(long sessionId, String audioCodecs,
										String videoCodecs, boolean existsEarlyMedia, boolean existsAudio,
										boolean existsVideo) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		if (existsVideo) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		if (existsAudio) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		tempSession.setSessionState(true);
		tempSession.setDescriptionString("Call session progress.");
		sendSessionChangeMessage("Call session progress.", SESSION_CHANG);
		tempSession.setEarlyMeida(existsEarlyMedia);
		Toast.makeText(getApplicationContext(),"Call on progress",Toast.LENGTH_LONG).show();
	}
	@Override
	public void onInviteRinging(long sessionId, String statusText,
								int statusCode) {
		Line tempSession = findLineBySessionId(sessionId);
		Log.d(TAG, "Ringing.....");
		Toast.makeText(this,"Ringing...",Toast.LENGTH_SHORT).show();
		if (tempSession == null) {
			return;
		}
		if (!tempSession.hasEarlyMeida()) {
			// Hasn't the early media, you must play the local WAVE file for
			// ringing tone
		}
		tempSession.setDescriptionString("Ringing...");
		sendSessionChangeMessage("Ringing...", SESSION_CHANG);
		//Toast.makeText(getApplicationContext(),"Ringing...",Toast.LENGTH_SHORT).show();
		Log.d(TAG, "_________________________?????????????? Ringing.....");
		//call it like this from your activity' any method
		//beginConnected("","","Ringing..");

	}
	public void playAlertTone(final Context context,String Stop){

		Thread t = new Thread(){
			public void run(){
				MediaPlayer player = null;
				int countBeep = 0;
				while(countBeep<2){
					player = MediaPlayer.create(context,R.raw.outring);
					player.start();
					countBeep+=1;
					try {
						// 100 milisecond is duration gap between two beep
						Thread.sleep(player.getDuration()+100);
						player.release();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}
		};
		t.start();
		if(Stop=="stop"){
			t.stop();
		}
	}
	@Override
	public void onInviteAnswered(long sessionId, String callerDisplayName, String caller,
								 String calleeDisplayName, String callee,
								 String audioCodecs, String videoCodecs, boolean existsAudio,
								 boolean existsVideo) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		if (existsVideo) {
			//Do nothing for the time being on version 1.0.1 SecureCall
			sdk.sendVideo(tempSession.getSessionId(), true);
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		if (existsAudio) {
			// sdk.addAudioCodec("g.729#GSM#AMR#H264#H263");
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		tempSession.setVideoState(existsVideo);
		tempSession.setSessionState(true);
		tempSession.setDescriptionString("Call established");
		setStatusFlag(true);
		sendSessionChangeMessage("Call established", SESSION_CHANG);
		if (isConference()) {
			sdk.joinToConference(tempSession.getSessionId());
			tempSession.setHoldState(false);
		}
		// If this is the refer call then need set it to normal
		if (tempSession.isReferCall()) {
			tempSession.setReferCall(false, 0);
		}
		Log.d(TAG, "---------------------My Application Connected" + caller + "\n"
				+ callerDisplayName + "\n"
				+ callee + "\n"
				+ calleeDisplayName);
		Log.d(TAG, "===================Call is connected:======================");

		//String caller, String displayName,String status
		//beginConnected(callee, calleeDisplayName, "Secure Connected");
	}
	@Override
	public void onInviteFailure(long sessionId, String reason, int code) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("call failure" + reason);
		sendSessionChangeMessage("call failure" + reason, SESSION_CHANG);
		if (tempSession.isReferCall()) {
			// Take off the origin call from HOLD if the refer call is failure
			Line originSession = findLineBySessionId(tempSession
					.getOriginCallSessionId());
			if (originSession != null) {
				sdk.unHold(originSession.getSessionId());
				originSession.setHoldState(false);
				// Switch the currently line to origin call line
				setCurrentLine(originSession);
				tempSession.setDescriptionString("refer failure:" + reason
						+ "resume orignal call");
				sendSessionChangeMessage("call failure" + reason, SESSION_CHANG);
			}
		}
		tempSession.reset();
		try {
			Intent startActivity = new Intent();
			startActivity.setClass(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
			pi.send(this,0,null);
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInviteUpdated(long sessionId, String audioCodecs,
								String videoCodecs, boolean existsAudio, boolean existsVideo) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}

		if (existsVideo) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		if (existsAudio) {
			// If more than one codecs using, then they are separated with "#",
			// for example: "g.729#GSM#AMR", "H264#H263", you have to parse them
			// by yourself.
		}
		tempSession.setDescriptionString("Call on progress");
		Log.d(TAG, "Updated");
	}

	@Override
	public void onInviteConnected(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);

		if (tempSession == null) {
			return;
		}

		tempSession.setDescriptionString("Connected");
		sendSessionChangeMessage("Connected", SESSION_CHANG);
			setStatusFlag(true);
		updateSessionVideo();
		Log.d(TAG, "!!!!!!!!!!!!!!!Connected!!!!!!!!!!!!!!! MY APPLICATION CLASS");
		//beginConnected("Default","Default","Secure Connected");
	}

	@Override
	public void onInviteBeginingForward(String forwardTo) {
		sendSessionChangeMessage("An incoming call was forwarded to: "
				+ forwardTo, SESSION_CHANG);
	}

	@Override
	public void onInviteClosed(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.reset();
		updateSessionVideo();
		tempSession.setDescriptionString(": Call closed.");
		sendSessionChangeMessage(": Call closed.", SESSION_CHANG);
		try {
			r.stop();
			alertDialog2.dismiss();
			}catch(Exception e){
			System.out.println("Error-------------------");
		}
		Intent startActivity = new Intent();
		Log.d(TAG, "Call CLOSED--------------------");
		startActivity.setClass(this,HomeTabActivity.class).
				setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_CLEAR_TOP |
						Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
		try {
			pi.send(this, 0, null);
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		//String caller, String displayName,String status
		//beginConnected("Caller ID","Caller Name","Call Ended");
		//bringToFront();
	}
	@Override
	public void onRemoteHold(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("Placed on hold by remote.");
		sendSessionChangeMessage("Placed on hold by remote.", SESSION_CHANG);
		//String caller, String displayName,String status
		//beginConnected();
	}
	@Override
	public void onRemoteUnHold(long sessionId, String audioCodecs,
							   String videoCodecs, boolean existsAudio, boolean existsVideo) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("Take off hold by remote.");
		sendSessionChangeMessage("Take off hold by remote.", SESSION_CHANG);
	}
	@Override
	public void onRecvDtmfTone(long sessionId, int tone) {
		// TODO Auto-generated method stub
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
			r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void onReceivedRefer(long sessionId, final long referId, String to,
								String from, final String referSipMessage) {
		final Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			sdk.rejectRefer(referId);
			return;
		}
		final Line referSession = findIdleLine();
		if (referSession == null)// all sessions busy
		{
			sdk.rejectRefer(referId);
			return;
		} else {
			referSession.setSessionState(true);
		}
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_NEGATIVE: {

						sdk.rejectRefer(referId);
						referSession.reset();
					}
					break;
					case DialogInterface.BUTTON_POSITIVE: {

						sdk.hold(tempSession.getSessionId());// hold current session
						tempSession.setHoldState(true);

						tempSession
								.setDescriptionString("Place currently call on hold on line: ");
						long referSessionId = sdk.acceptRefer(referId,
								referSipMessage);
						if (referSessionId <= 0) {
							referSession
									.setDescriptionString("Failed to accept REFER on line");

							referSession.reset();
							// Take off hold
							sdk.unHold(tempSession.getSessionId());
							tempSession.setHoldState(false);
						} else {
							referSession.setSessionId(referSessionId);
							referSession.setSessionState(true);
							referSession.setReferCall(true,
									tempSession.getSessionId());

							referSession
									.setDescriptionString("Accepted the refer, new call is trying on line ");

							_CurrentlyLine = referSession;

							tempSession
									.setDescriptionString("Now current line is set to: "
											+ _CurrentlyLine.getLineName());
							updateSessionVideo();
						}
					}
				}

			}
		};
		showGloableDialog("Received REFER", "accept", listener, "reject",
				listener);
	}

	@Override
	public void onReferAccepted(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("the REFER was accepted.");
		sendSessionChangeMessage("the REFER was accepted.", SESSION_CHANG);
	}

	@Override
	public void onReferRejected(long sessionId, String reason, int code) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
Log.d(TAG,"---------------_______--------------Refer Rjected");
		tempSession.setDescriptionString("the REFER was rejected.");
		sendSessionChangeMessage("the REFER was rejected.", SESSION_CHANG);
	}

	@Override
	public void onTransferTrying(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}

		tempSession.setDescriptionString("Transfer Trying.");
		sendSessionChangeMessage("Transfer Trying.", SESSION_CHANG);
	}

	@Override
	public void onTransferRinging(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}

		tempSession.setDescriptionString("Transfer Ringing.");
		sendSessionChangeMessage("Transfer Ringing.", SESSION_CHANG);
	}

	@Override
	public void onACTVTransferSuccess(long sessionId) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}
		tempSession.setDescriptionString("Transfer succeeded.");
	}

	@Override
	public void onACTVTransferFailure(long sessionId, String reason, int code) {
		Line tempSession = findLineBySessionId(sessionId);
		if (tempSession == null) {
			return;
		}

		tempSession.setDescriptionString("Transfer failure");

		// reason is error reason
		// code is error code

	}

	public Line findLineBySessionId(long sessionId) {
		for (int i = Line.LINE_BASE; i < Line.MAX_LINES; ++i) {
			if (_CallSessions[i].getSessionId() == sessionId) {
				return _CallSessions[i];
			}
		}

		return null;
	}

	public Line findSessionByIndex(int index) {

		if (Line.LINE_BASE <= index && index < Line.MAX_LINES) {
			return _CallSessions[index];
		}

		return null;
	}

	static Line findIdleLine() {

		for (int i = Line.LINE_BASE; i < Line.MAX_LINES; ++i)// get idle session
		{
			if (!_CallSessions[i].getSessionState()
					&& !_CallSessions[i].getRecvCallState()) {
				return _CallSessions[i];
			}
		}

		return null;
	}
	public void setCurrentLine(Line line) {
		if (line == null) {
			_CurrentlyLine = _CallSessions[Line.LINE_BASE];
		} else {
			_CurrentlyLine = line;
		}
	}

	public Session getCurrentSession() {
		return _CurrentlyLine;
	}

	@Override
	public void onReceivedSignaling(long sessionId, String message) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onSendingSignaling(long sessionId, String message) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onWaitingVoiceMessage(String messageAccount,
									  int urgentNewMessageCount, int urgentOldMessageCount,
									  int newMessageCount, int oldMessageCount) {
		String text = messageAccount;
		text += " has voice message.";

		showMessage(text);
		// You can use these parameters to check the voice message count

		// urgentNewMessageCount;
		// urgentOldMessageCount;
		// newMessageCount;
		// oldMessageCount;

	}

	@Override
	public void onWaitingFaxMessage(String messageAccount,
									int urgentNewMessageCount, int urgentOldMessageCount,
									int newMessageCount, int oldMessageCount) {
		String text = messageAccount;
		text += " has FAX message.";

		showMessage(text);
		// You can use these parameters to check the FAX message count

		// urgentNewMessageCount;
		// urgentOldMessageCount;
		// newMessageCount;
		// oldMessageCount;

	}

	@Override
	public void onPresenceRecvSubscribe(long subscribeId,
										String fromDisplayName, String from, String subject) {

		String fromSipUri = "sip:" + from;

		final long tempId = subscribeId;
		DialogInterface.OnClickListener onClick;
		SipContact contactRefrence = null;
		boolean contactExist = false;

		for (int i = 0; i < contacts.size(); ++i) {
			contactRefrence = contacts.get(i);
			String SipUri = contactRefrence.getSipAddr();

			if (SipUri.equals(fromSipUri)) {
				contactExist = true;
				if (contactRefrence.isAccept()) {
					long nOldSubscribID = contactRefrence.getSubId();
					sdk.presenceAcceptSubscribe(tempId);
					String status = "Available";
					sdk.presenceOnline(tempId, status);

					if (contactRefrence.isSubscribed() && nOldSubscribID >= 0) {
						sdk.presenceSubscribeContact(fromSipUri, subject);
					}
					return;
				} else {
					break;
				}
			}
		}
		//
		if (!contactExist) {
			contactRefrence = new SipContact();
			contacts.add(contactRefrence);
			contactRefrence.setSipAddr(fromSipUri);
		}
		final SipContact contact = contactRefrence;
		onClick = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						sdk.presenceAcceptSubscribe(tempId);
						contact.setSubId(tempId);
						contact.setAccept(true);
						String status = "Available";
						sdk.presenceOnline(tempId, status);
						contact.setSubstatus(true);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						contact.setAccept(false);// reject subscribe
						contact.setSubId(0);
						contact.setSubstatus(false);// offline

						sdk.presenceRejectSubscribe(tempId);
						break;
					default:
						break;
				}
				dialog.dismiss();
			}
		};
		showGloableDialog(from, "Accept", onClick, "Reject", onClick);
	}
	@Override
	public void onPresenceOnline(String fromDisplayName, String from,
								 String stateText) {

		String fromSipUri = "sip:" + from;
		SipContact contactRefernce;
		for (int i = 0; i < contacts.size(); ++i) {
			contactRefernce = contacts.get(i);
			String SipUri = contactRefernce.getSipAddr();
			if (SipUri.endsWith(fromSipUri)) {
				contactRefernce.setSubDescription(stateText);
				contactRefernce.setSubstatus(true);// online
			}
		}
		sendSessionChangeMessage("contact status change.", CONTACT_CHANG);
	}

	@Override
	public void onPresenceOffline(String fromDisplayName, String from) {

		String fromSipUri = "sip:" + from;
		SipContact contactRefernce;
		for (int i = 0; i < contacts.size(); ++i) {
			contactRefernce = contacts.get(i);
			String SipUri = contactRefernce.getSipAddr();
			if (SipUri.endsWith(fromSipUri)) {
				contactRefernce.setSubstatus(false);// "Offline";
				contactRefernce.setSubId(0);
			}
		}
		sendSessionChangeMessage("contact status change.", CONTACT_CHANG);
	}

	@Override
	public void onRecvOptions(String optionsMessage) {
		// String text = "Received an OPTIONS message: ";
		// text += optionsMessage.toString();
		// showTips(text);
	}

	@Override
	public void onRecvInfo(String infoMessage) {

		// String text = "Received a INFO message: ";
		// text += infoMessage.toString();
		// showTips(text);
	}

	@Override
	public void onRecvMessage(long sessionId, String mimeType,
							  String subMimeType, byte[] messageData, int messageDataLength) {
	}
	@Override
	public void onRecvOutOfDialogMessage(String fromDisplayName, String from,
										 String toDisplayName, String to, String mimeType,
										 String subMimeType, byte[] messageData, int messageDataLength) {
		String text = "Received a " + mimeType + "message from ";
		String msg= messageData.toString();
		Log.d(TAG,":::: sjekke meldingen her"+msg);
		String s=from;
		final String requiredString =s.substring(s.indexOf(":") + 1, s.indexOf("@"));
		// This is text messaging
		String receivedMsg = new String(messageData);
		text += requiredString;
		SessionManager session= new SessionManager(this);
		// Generate for the message ID
		int min = 10;
		int max = 99;
		Random r = new Random();
		int i1 = r.nextInt(max - min + 1) + min;
		session.setSmsInf(fromDisplayName + " " + requiredString, mimeType, receivedMsg, String.valueOf(i1), true);
		if (mimeType.equals("text") && subMimeType.equals("plain")) {
			Intent startActivity = new Intent();
			startActivity.setClass(this,SendSms.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//putExtra("putSmsWindow",true);
			PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
			try {
				pi.send(getApplicationContext(), 0,null);
			} catch (CanceledException e) {
				e.printStackTrace();
			}
			//showMessage(text + "  \n" + receivedMsg);
		} else if (mimeType.equals("application")
				&& subMimeType.equals("vnd.3gpp.sms")) {
			// The messageData is binary data
			showMessage(text);
		} else if (mimeType.equals("application")
				&& subMimeType.equals("vnd.3gpp2.sms")) {
			// The messageData is binary data
			showMessage(text);
			// Test 
		}

	}
	@Override
	public void onPlayAudioFileFinished(long sessionId, String fileName) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onPlayVideoFileFinished(long sessionId) {
		// TODO Auto-generated method stub
	}

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	public void setConnetedCAll(ConnectedCall connectedCall) {
		this.connectedCall = connectedCall;
	}
	public void setHometabActivity(HomeTabActivity hometabActivity){
		this.hometabActivity=hometabActivity;
	}
	public void setSmsActivity(SendSms sendSms){
		this.sendSms=sendSms;
	}
	@Override
	public void onSendMessageSuccess(long sessionId, long messageId) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onSendMessageFailure(long sessionId, long messageId,
									 String reason, int code) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onSendOutOfDialogMessageSuccess(long messageId,
												String fromDisplayName, String from, String toDisplayName, String to) {
	}

	@Override
	public void onSendOutOfDialogMessageFailure(long messageId,
												String fromDisplayName, String from, String toDisplayName,
												String to, String reason, int code) {
	}

	@Override
	public void onReceivedRTPPacket(long sessionId, boolean isAudio,
									byte[] RTPPacket, int packetSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSendingRTPPacket(long sessionId, boolean isAudio,
								   byte[] RTPPacket, int packetSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAudioRawCallback(long sessionId, int enum_audioCallbackMode,
								   byte[] data, int dataLength, int samplingFreqHz) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onVideoRawCallback(long sessionId, int enum_videoCallbackMode,
								   int width, int height, byte[] data, int dataLength) {
		// TODO Auto-generated method stub

	}
	void showMessage(String message) {
		OnClickListener listener = null;
		final String msg= message;
		showGloableDialog(message, "", listener, "Cancel",
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "Breakpoint");
						dialog.dismiss();
					}
				});

	}
	void showGloableDialog(String message, String strPositive,
						   OnClickListener positiveListener, String strNegative,
						   OnClickListener negativeListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		if (positiveListener != null) {
			builder.setPositiveButton(strPositive, positiveListener);
			Log.d(TAG,"The message is "+message);
		}
		if (negativeListener != null) {
			builder.setNegativeButton(strNegative, negativeListener);
			Log.d(TAG, "We got the message here" + message);
		}
		//Intent intent= new Intent(this,MessageFragment.class);
		//getApplicationContext().StartActivity().class;
		// Asset By Itman
		AlertDialog ad = builder.create();
		ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ad.setCanceledOnTouchOutside(false);
		ad.show();
	}
	public void myRejectcall(){
		/*Intent intent= new Intent(this,chooseservice.class);
		startActivity(intent);*/
	}
	public void beginConnected(String caller, String displayName,String status){
		//String whatyouaresearching = myString.subString(0,myString.lastIndexOf("/"))
		//SessionManager sessionManager= new SessionManager(this);
		//sessionManager.setDestName(displayName);
		try {
			Intent startActivity = new Intent();
			startActivity.setClass(this, ConnectedCall.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.putExtra("value", caller)
					.putExtra("nameValue", displayName)
					.putExtra("incallFlag", true)
					.putExtra("status", status);
			PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
			pi.send(this,0,null);
		} catch (CanceledException e) {
			e.printStackTrace();
		}
		//bringToFront();

	}
	public void bringToFront(String caller, String displayName,String status){
		//dialogFlag=true;
		setGName(displayName);
		setGPhone(caller);
		setInCallFlag(true);
		setDialogFlag(dialogFlag);
		try {
			Intent startActivity = new Intent();
			startActivity.setClass(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
					.putExtra("mybool",dialogFlag);
			PendingIntent pi = PendingIntent.getActivity(this,0,startActivity,0);
			pi.send(this,0,null);
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}



	public static synchronized MyApplication getInstance() {
		return mInstance;
	}
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}
	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}
	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}
	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
	public ArrayList<String> getPhone_list() {
		return phone_list;
	}
	public void setPhone_list(ArrayList<String> someVariable) {
		//addValues.add(someVariable);
		this.phone_list = someVariable;
	}
	public static ArrayList<String> getUserDetail() {
		return userDetail;
	}
	public void setUserDetail(ArrayList<String> someVariable) {
		userDetail=new ArrayList<String>();
		//addValues.add(someVariable);
		this.userDetail = someVariable;
	}
	public String getGName(){
		return gName;
	}
	public void setGName(String name){
		this.gName=name;
	}

	public String getGPhone(){
		return gPhone;
	}
	public void setGPhone(String phone){
		this.gPhone=phone;
	}
	public String getGPassword(){
		return gPassword;
	}
	public void setGPassword(String password){
		this.gPassword=password;
	}
	public String getGEmail(){
		return gEmail;
	}
	public void setGEmail(String email){
		this.gEmail=email;
	}
	private void setInCallFlag(boolean flag) {
		this.gFlag=flag;
	}
	public boolean getInCallFlag(){
		return gFlag;
	}

	private void setDialogFlag(boolean flag) {
		this.gDialogFlag=flag;
	}
	public boolean getDialogFlag(){
		return gDialogFlag;
	}
}
