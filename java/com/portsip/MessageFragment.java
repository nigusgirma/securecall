package com.portsip;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.portsip.main.bubbles.BubbleCreater;
import com.portsip.main.bubbles.DiscussArrayAdapter;
import com.portsip.main.chat.ChatArrayAdapter;
import com.portsip.main.chat.ChatMessage;
import com.portsip.util.SipContact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import helper.SessionManager;

public class MessageFragment extends Fragment implements  OnItemClickListener, AdapterView.OnItemSelectedListener {
	public final String TAG="MessageFragment";
	EditText etContact, etStatus, etmsgdest,tempsendto, etMessage;
	private ChatArrayAdapter chatArrayAdapter;
	private ListView listView;
	private Button buttonSend;
	private boolean side = false;
	//ImageButton btSendmessage;
	ImageButton btSendmessage;

	String toNumberValue="";
	int selectedItem;
	// Sender Info
	String msgSenderId,msgType,msgData,msgId;
	SessionManager sessionManager;
	int selectItem;
	OnClickListener clickListener;
	MyApplication myApplication;
	PortSipSdk mSipSdk;
	Context context = null;
	BaseListAdapter mAdapter;
	List<SipContact> contacts = null;
	private ArrayList<Map<String, String>> mPeopleList;
	private ArrayAdapter<String> phoneAdapter;
	String contact;
	private MultiAutoCompleteTextView multiAutoComplete;
	private AutoCompleteTextView mTxtPhoneNo;
	public static ArrayList<String> phoneValueArr = new ArrayList<String>();
	public static ArrayList<String> nameValueArr = new ArrayList<String>();
	///// For bubble the messages
	private DiscussArrayAdapter adapter;
	private ListView lv;
	private BubbleCreater ipsum;
	private EditText editText1;
	private static Random random;
	String sendNumber;
	String InputMsgType;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		try {
			InputMsgType = getArguments().getString("type");// Decides whether the msg is incomming or outgoing
		}catch(Exception e){e.getMessage();}
		context = getActivity();
		myApplication = (MyApplication) context.getApplicationContext();
		mSipSdk = myApplication.getPortSIPSDK();
		clickListener = new BtOnclickListen();
		contacts = myApplication.getSipContacts();
		View view = inflater.inflate(R.layout.messageview, null);
		//v=inflater.inflate(R.layout.temp,null);
		/**/
		//Context context=getApplicationcontext();
		String message=null;
		String address=null; //
		Bundle extras=getActivity().getIntent().getExtras();
		if(extras!=null) {
			//Intent intent = getIntent();
			message = extras.getString("value");//the message content will be here
			address = extras.getString("value2");
			//phoneValueArr=extras.getStringArrayList("phoneList");
			//nameValueArr=extras.getStringArrayList("nameList");
			//address = extras.getString("value2"); // the caller it will be here
		}else{
			Log.d(TAG,"We dont't have a message in this class");
		}
		Log.d(TAG,"=================MESSAGE IS :================== \n"+message+address);
		mTxtPhoneNo = (AutoCompleteTextView) view.findViewById(R.id.etmsgdest);
  	    /// Old versjon
		/*phoneAdapter = new SimpleAdapter(this.getActivity(), mPeopleList, R.layout.actionbar_search ,
				new String[] { "Name", "Phone" , "Type" },
				new int[] { R.id.ccontName, R.id.ccontNo, R.id.ccontType });
		//ArrayAdapter<String> phoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, ;
		mTxtPhoneNo.setThreshold(1);
		mTxtPhoneNo.setAdapter(phoneAdapter);*/
		// ,msgType,msgData,msgId
		sessionManager=new SessionManager(getActivity().getApplicationContext());
		msgSenderId=sessionManager.getMsgSender();
		msgType=sessionManager.getMsgType();
		msgData=sessionManager.getMsg();
		msgId=sessionManager.getMsgId();
		//Nye versjon
		//Create phoneAdapter
		phoneAdapter = new ArrayAdapter<String>
				(this.context, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		mTxtPhoneNo.setAdapter(phoneAdapter);
		mTxtPhoneNo.setThreshold(1);
		readContactData();
		mTxtPhoneNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
									   long arg3) {
				// TODO Auto-generated method stub
				contact = arg0.getItemAtPosition(position).toString();
				Log.d(TAG, "onItemSelected() position " + position + contact);
				String autotext = mTxtPhoneNo.getText().toString();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						getActivity().INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
			}
		});

		mTxtPhoneNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				String result2 = arg0.getItemAtPosition(0).toString();
				Log.d(TAG,"Nothing selected take edit value "+arg0.toString()+arg1+arg2+arg3);
				selectedItem = arg2;
				contact = arg0.getItemAtPosition(arg2).toString();

				String addsign=null;
				boolean plusTrue=false;
				if(contact.contains("+"))
				{
					addsign="+";
					plusTrue=true;
				}
				final String contactName = contact.replaceAll("[^A-Za-z] ", "");
				final String contactNumber = contact.replaceAll("[^0-9.]", "");
				Log.d(TAG, "AutoComptext View is" + mTxtPhoneNo.getText().toString());
				String result=mTxtPhoneNo.getText().toString();
				Log.d(TAG, "result selected " + result);
				final String selectedNr = result.replaceAll("[^0-9.]", "");
				if(contact==null){
					sendNumber=selectedNr;//;mTxtPhoneNo.getText().toString();
				}else{
					sendNumber = contactNumber;
				}
              	System.out.println(contact);
				/*if(addsign=="+"||plusTrue==true){
					sendNumber= new String("+"+sendNumber);
				}*/
				Log.d(TAG, "selected Item" + sendNumber + "Name: " + contactName + "Number: " + contactNumber);
			}
		});
		//////////////////
		//etMessage.getWindow().setShowSoftInputOnFocus(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		etMessage = (EditText) view.findViewById(R.id.etmessage);
		//EditText yourEditText= (EditText) findViewById(R.id.yourEditText);
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
		//tx=(TextView) view.findViewById(R.id.message_viewer);
		btSendmessage = (ImageButton) view.findViewById(R.id.btsendmsg);
		//btSendmessage.setOnClickListener(clickListener);
		//*
		/// Nye forslag
		listView = (ListView) view.findViewById(R.id.listView1);
		chatArrayAdapter = new ChatArrayAdapter(context.getApplicationContext(), R.layout.zactivity_chat_singlemessage);
		listView.setAdapter(chatArrayAdapter);
		Log.i(TAG, "Incomming message Datas are: " + msgData + msgSenderId);
		if(sessionManager.isInSmsFlag()){
			Log.d(TAG,"We are Going to insert values: ");
			mTxtPhoneNo.setText(msgSenderId);
			//phoneAdapter.add(msgSenderId);
			mTxtPhoneNo.setOnItemSelectedListener(this);
			sendChatMessage(true, msgData);

		}
		//sessionManager.setSmsFlag(false);
		etMessage.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if(!myApplication.isOnline()){
					return false;
				}
				String temp=null;//etMessage.getText().toString();
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					temp=etMessage.getText().toString();
					BtnSend_Click(sendNumber, etMessage.getText().toString());
					return sendChatMessage(false, temp);
				}
				return false;
			}
		});
		btSendmessage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//adapter.add(new OneComment(false, etMessage.getText().toString()));
				if(!myApplication.isOnline()){
					return;
				}
				sendChatMessage(false, etMessage.getText().toString());
				if(sendNumber==null){
					sendNumber=msgSenderId;
				}
				BtnSend_Click(sendNumber, etMessage.getText().toString());
//				etMessage.setText("");
				//sendNumber
			}
		});

		listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		listView.setAdapter(chatArrayAdapter);
		//to scroll the list view to bottom on data change
		chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				listView.setSelection(chatArrayAdapter.getCount() - 1);
			}
		});

		//addItems();
		return view;
	}
	private boolean sendChatMessage(boolean side,String msg){
		//boolean left = getRandomInteger(0, 1) == 0 ? true : false;
		chatArrayAdapter.add(new ChatMessage(side, msg));
		//etMessage.setText("");
		//side = !side;
		return true;
	}

	private void readContactData() {
		try {
			/*********** Reading Contacts Name And Number **********/
			String phoneNumber = "";
			ContentResolver cr = myApplication.getBaseContext().getContentResolver();
			//Query to get contact name
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
					null,
					null,
					null,
					null);
			// If data data found in contacts
			if (cur.getCount() > 0) {
				Log.i("AutocompleteContacts", "Reading   contacts........");
				int k=0;
				String name = "";
				while (cur.moveToNext())
				{
					String id = cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts._ID));
					name = cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					//Check contact have phone number
					if (Integer.parseInt(cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
					{
						//Create query to get phone number by contact id
						Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = ?",
								new String[] { id },null);
						int j=0;
						while (pCur.moveToNext())
						{
							// Sometimes get multiple data
							if(j==0)
							{
								// Get Phone number
								phoneNumber =""+pCur.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
								// Add contacts names to adapter
								phoneAdapter.add(name.toString() + " " + phoneNumber.toString());
								// Add ArrayList names to adapter
								phoneValueArr.add(phoneNumber.toString());
								nameValueArr.add(name.toString());
								j++;
								k++;
							}
						}  // End while loop
						pCur.close();
					} // End if
				}  // End while loop
			} // End Cursor value check
			cur.close();
		} catch (Exception e) {
			Log.i("AutocompleteContacts", "Exception : " + e);
		}
	}
	// The message inflator
	class BtOnclickListen implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btsendmsg:
					//BtnSend_Click();
					break;
				/*case R.id.btsendstatus:
					BtnSetStatus_Click();
					break;*/
				case R.id.btaddcontact:
					BtnAddContact_Click();
					break;
				/*case R.id.btdelcontact:
					BtnDelContact_Click();
					break;
				case R.id.btclear:
					BtnClearContact_Click();
					break;
				case R.id.btupdate:
					BtnUpdateContact_Click();
					break;*/
				default:
					break;
			}
		}
	}
	private void BtnAddContact_Click() {
		if (!myApplication.isOnline()) {
			return;
		}
		String sendTo = etContact.getText().toString();
		if (sendTo == null || sendTo.length() <= 0) {
			return;
		}
		String subject = "";
		mSipSdk.presenceSubscribeContact(sendTo, subject);
		for (int i = 0; i < contacts.size(); i++)// already added
		{
			SipContact tempReference = contacts.get(i);
			String SipUri = tempReference.getSipAddr();
			if (SipUri.equals(sendTo)) {
				tempReference.setSubscribed(true);
				updateLV();
				return;
			}
		}
		SipContact newContact = new SipContact();
		newContact.setSipAddr(sendTo);
		newContact.setSubstatus(false);// off line
		newContact.setSubscribed(true);// weigher send my status to remote
		// subscribe
		newContact.setAccept(false);   // weigher rev remote subscribe
		newContact.setSubId(0);
		contacts.add(newContact);
		updateLV();
	}
	private void BtnDelContact_Click() {
		if (contacts.size() > 0) {
			contacts.remove(selectItem);
		}
		updateLV();
	}
	private void BtnUpdateContact_Click() {
		for (int i = 0; i < contacts.size(); ++i) {
			SipContact tempReference = contacts.get(i);
			String SipUri = tempReference.getSipAddr();
			String subject = "Hello";
			long subscribeId = tempReference.getSubId();
			if (tempReference.isSubscribed()) {
				mSipSdk.presenceSubscribeContact(SipUri, subject);
			}
			String statusText = etStatus.getText().toString();
			if (tempReference.isAccept() && subscribeId != -1) {
				mSipSdk.presenceOnline(subscribeId, statusText);
			}
		}
	}
	private void BtnClearContact_Click() {
		contacts.clear();
		updateLV();
	}
	private void updateLV() {
		mAdapter.notifyDataSetChanged();
	}
	private void BtnSetStatus_Click() {
		if (!myApplication.isOnline()) {
			return;
		}
		String content = etStatus.getText().toString();
		if (content == null || content.length() <= 0) {
			showTips("please input status description string");
			return;
		}
		for (int i = 0; i < contacts.size(); ++i) {
			SipContact tempReferece = contacts.get(i);
			long subscribeId = tempReferece.getSubId();
			String statusText = etStatus.getText().toString();
			if (tempReferece.isAccept() && subscribeId != -1) {
				mSipSdk.presenceOnline(subscribeId, statusText);
			}
		}
		//String sendTo="sip:"+send+"@callman.cloudapp.net:5060";
	}
	private void BtnSend_Click(String send,String content) {
		if (!myApplication.isOnline()) {
			Log.d(TAG,"not online need to be registed");
			return;
		}
		//SIP ADDRESS sip:55443322@securecall.cloudapp.net:5070
		//String mymessage
		//send to = 410 xx xxx
		//tv.setText(content.toString());
		//tv.setGravity(View.SCROLLBAR_POSITION_LEFT);
		////////////// Need to tilbake
		String sendTo="sip:"+send+"@callman.cloudapp.net:5060";
		//String sendTo="sip:"+send+"@10.14.4.26:5060";
		Log.d(TAG,"SIP ADDRESS "+sendTo+"....."+send);
		if (send == null || sendTo.length() <= 0) {
			Toast.makeText(context, "Please input send to target",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (content == null || content.length() <= 0) {
			Toast.makeText(context, "Please input message content",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// If TextView is empty - copy string from EditText
		//if (tx.getText().toString().equals("")) {
		/*tx.setText(" \n");
		tx.setText(etMessage.getText().toString());
		tx.setGravity(View.SCROLLBAR_POSITION_LEFT);*/
		etMessage.setText("");
		//} else { // Otherwise, clear the TextView.
		//tx.setText("");
		//}
		mSipSdk.sendOutOfDialogMessage(sendTo, "text", "plain",
				content.getBytes(), content.length());
		//myview.setText(content);
	}
	void showTips(String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	private class BaseListAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		public BaseListAdapter(Context mContext) {
			inflater = LayoutInflater.from(mContext);
		}
		@Override
		public int getCount() {
			return contacts.size();
		}
		@Override
		public Object getItem(int position) {
			return null;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tvRefrence;
			ImageButton btDelContact;
			SipContact contactRefrence;
			contactRefrence = contacts.get(position);
			convertView = inflater.inflate(R.layout.viewlistitem, null);
			tvRefrence = (TextView) convertView.findViewById(R.id.tvsipaddr);
			tvRefrence.setText(contactRefrence.getSipAddr());
			tvRefrence = (TextView) convertView
					.findViewById(R.id.tvsubdescription);
			tvRefrence.setText(contactRefrence.currentStatusToString());
			btDelContact = (ImageButton) convertView
					.findViewById(R.id.btdelcontact);
			btDelContact.setOnClickListener(clickListener);
			return convertView;
		}

	}
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateLV();
			// The message will come hre
			// If TextView is empty - copy string from EditText

		}
	};

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
							   long arg3) {
		// TODO Auto-generated method stub
		//Log.d("AutocompleteContacts", "onItemSelected() position " + position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
				getActivity().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		// Get Array index value for selected name
		int i = nameValueArr.indexOf(""+arg0.getItemAtPosition(arg2));

		// If name exist in name ArrayList
		if (i >= 0) {
			// Get Phone Number
			toNumberValue = phoneValueArr.get(i);
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
					getActivity().INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
			// Show Alert
			Toast.makeText(getActivity().getBaseContext(),
					"Position:"+arg2+" Name:"+arg0.getItemAtPosition(arg2)+" Number:"+toNumberValue,
					Toast.LENGTH_LONG).show();

			Log.d("AutocompleteContacts",
					"Position:"+arg2+" Name:"+arg0.getItemAtPosition(arg2)+" Number:"+toNumberValue);
		}

	}


	@Override
	public void onResume() {
		super.onResume();
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(MyApplication.CONTACT_CHANG);
		context.registerReceiver(mReceiver, mIntentFilter);
	}
	@Override
	public void onPause() {
		super.onPause();
		context.unregisterReceiver(mReceiver);
	}
}
