package com.portsip;

import com.portsip.util.PreferenceFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SettingFragment extends PreferenceFragment {
	PortSipSdk mSipSdk;
	PreferenceManager mprefmamager;
	SharedPreferences mpreferences;
	boolean changed = true;
	Context context = null;

	enum Profile {
		UNKOWN,
		ALWAYS,
		WIFI,
		NEVER
	}
	private CheckBox globIntegrate;
	private RadioButton globProfileAlways;
	private RadioButton globProfileWifi;
	private RadioButton globProfileNever;
	private CheckBox globGsm;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.media_set);
		context = getActivity();
		mSipSdk = ((MyApplication) getActivity().getApplicationContext())
				.getPortSIPSDK();
		//mprefmamager.setSharedPreferencesMode();
		mprefmamager = getPreferenceManager();
		mpreferences = mprefmamager.getSharedPreferences();
		MyOnChangeListen changeListen = new MyOnChangeListen();
		//findPreference(getString(R.string.str_bitrate)).setOnPreferenceChangeListener(changeListen);
		findPreference(getString(R.string.str_bitrate)).setOnPreferenceChangeListener(changeListen);
		findPreference(getString(R.string.str_resolution))
				.setOnPreferenceChangeListener(changeListen);
		findPreference(getString(R.string.str_fwtokey))
				.setOnPreferenceChangeListener(changeListen);
		mpreferences
				.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(
							SharedPreferences sharedPreferences, String key) {
						changed = true;
					}
				});

	}
	@Override
	public void onPause() {
		super.onPause();
		if (!changed)
			return;
		 //mSipSdk.enable3GppTags(true);
		// audio codecs
		if (mpreferences.getBoolean(getString(R.string.network1), false)) {
			//mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G722);
			toggleWiFi(true);
		}else if(mpreferences.getBoolean(getString(R.string.network1), true)){
			toggleWiFi(false);
		}
		if (mpreferences.getBoolean(getString(R.string.network2), false)) {
			//mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G722);
			toggleumt_3g(true);
		}else if(mpreferences.getBoolean(getString(R.string.network2), true)){
			toggleumt_3g(false);
		}
		mSipSdk.clearAudioCodec();
		if (mpreferences.getBoolean(getString(R.string.MEDIA_G722), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G722);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_G729), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_G729);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_AMR), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_AMR);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_AMRWB), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_AMRWB);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_GSM), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_GSM);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_PCMA), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMA);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_PCMU), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_PCMU);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_SPEEX), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEX);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_SPEEXWB), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_SPEEXWB);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_ILBC), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ILBC);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_ISACWB), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACWB);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_ISACSWB), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_ISACSWB);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_OPUS), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_OPUS);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_DTMF), false)) {
			mSipSdk.addAudioCodec(PortSipEnumDefine.ENUM_AUDIOCODEC_DTMF);
		}
		mSipSdk.enableVAD(mpreferences.getBoolean(
				getString(R.string.MEDIA_VAD), true));
		mSipSdk.enableAEC(mpreferences.getBoolean(
				getString(R.string.MEDIA_AEC), true)?PortSipEnumDefine.ENUM_EC_DEFAULT:PortSipEnumDefine.ENUM_EC_NONE);
		mSipSdk.enableANS(mpreferences.getBoolean(
				getString(R.string.MEDIA_ANS), false)?PortSipEnumDefine.ENUM_NS_DEFAULT:PortSipEnumDefine.ENUM_NS_NONE);
		mSipSdk.enableAGC(mpreferences.getBoolean(
				getString(R.string.MEDIA_AGC), true)?PortSipEnumDefine.ENUM_AGC_DEFAULT:PortSipEnumDefine.ENUM_AGC_NONE);
		mSipSdk.enableCNG(mpreferences.getBoolean(
				getString(R.string.MEDIA_CNG), true));
		// audio codecs
		mSipSdk.clearVideoCodec();
		if (mpreferences.getBoolean(getString(R.string.MEDIA_H263), false)) {
			mSipSdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_H263);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_H26398), false)) {
			mSipSdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_H263_1998);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_H264), false)) {
			mSipSdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_H264);
		}
		if (mpreferences.getBoolean(getString(R.string.MEDIA_VP8), false)) {
			mSipSdk.addVideoCodec(PortSipEnumDefine.ENUM_VIDEOCODEC_VP8);
		}
		// sdk.setAudioSamples(20,0);
		mSipSdk.setRtpPortRange(2000, 3000, 3002, 4000);
		setForward();
        mSipSdk.enableReliableProvisional(mpreferences.getBoolean(
                getString(R.string.str_pracktitle), false));
	}
	public void toggleWiFi(boolean status) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (status == true && !wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		} else if (status == false && wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}
	}
	public void toggleumt_3g(boolean status){
		boolean state = isMobileDataEnable();
		// toggle the state
		if(state==true&&status==false){toggleMobileDataConnection(false);}
		else if(state==false&&status==true){toggleMobileDataConnection(true);}
	}
	public boolean isMobileDataEnable() {
		boolean mobileDataEnabled = false; // Assume disabled
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class cmClass = Class.forName(cm.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true); // Make the method callable
			// get the setting for "mobile data"
			mobileDataEnabled = (Boolean)method.invoke(cm);
		} catch (Exception e) {
			// Some problem accessible private API and do whatever error handling you want here
		}
		return mobileDataEnabled;
	}

	public boolean toggleMobileDataConnection(boolean ON)
	{
		try {
			//create instance of connectivity manager and get system connectivity service
			final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			//create instance of class and get name of connectivity manager system service class
			final Class conmanClass  = Class.forName(conman.getClass().getName());
			//create instance of field and get mService Declared field
			final Field iConnectivityManagerField= conmanClass.getDeclaredField("mService");
			//Attempt to set the value of the accessible flag to true
			iConnectivityManagerField.setAccessible(true);
			//create instance of object and get the value of field conman
			final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			//create instance of class and get the name of iConnectivityManager field
			final Class iConnectivityManagerClass=  Class.forName(iConnectivityManager.getClass().getName());
			//create instance of method and get declared method and type
			final Method setMobileDataEnabledMethod= iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled",Boolean.TYPE);
			//Attempt to set the value of the accessible flag to true
			setMobileDataEnabledMethod.setAccessible(true);
			//dynamically invoke the iConnectivityManager object according to your need (true/false)
			setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
		} catch (Exception e){
		}
		return true;
	}
	class MyOnChangeListen implements OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			if (arg0.getKey().equals(getString(R.string.str_bitrate))) {
				mSipSdk.setVideoBitrate((Integer) arg1);
			} else if (arg0.getKey().equals(getString(R.string.str_resolution))) {
				mSipSdk.setVideoResolution(Integer.valueOf((String) arg1));

			} else if (arg0.getKey().equals(getString(R.string.str_fwtokey))) {
				String forwardto = (String) arg1;
				if (!forwardto.matches(MyApplication.SIP_ADDRRE_PATTERN)) {
					Toast.makeText(
							context,
							"The forward address must likes sip:xxxx@sip.portsip.com.",
							Toast.LENGTH_LONG).show();
				}
			}
			return true;
		}
	}

	private int setForward() {
		int ret = PortSipErrorcode.ECoreArgumentNull;
		boolean forwardopen = mpreferences.getBoolean(
				getString(R.string.str_fwopenkey), false);

		if (!forwardopen) {
			mSipSdk.disableCallForward();
			return ret;
		}

		String forwardTo = mpreferences.getString(
				getString(R.string.str_fwtokey), "");
		boolean forwardonbusy = mpreferences.getBoolean(
				getString(R.string.str_fwbusykey), true);

		if (forwardTo.length() <= 0
				|| !forwardTo.matches(MyApplication.SIP_ADDRRE_PATTERN)) {
			// Toast.makeText(context,"The forward address must likes sip:xxxx@sip.portsip.com.",
			// Toast.LENGTH_LONG).show();
			mSipSdk.disableCallForward();
			return ret;
		}

		if (forwardonbusy) {
			ret = mSipSdk.enableCallForward(true, forwardTo);
		} else {
			ret = mSipSdk.enableCallForward(false, forwardTo);
		}
		return ret;
	}
}
