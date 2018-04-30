package com.portsip.main;
/**
 * Property of IT Man AS, Bryne Norway It is strictly forbidden to copy or modify the authors file,
 * (C) 2015 IT Man AS
 * Created by Nigussie on 03.03.2015.
 */
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.portsip.R;
import com.portsip.util.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nigussie on 27.03.2015.
 */
public class InviteFriend extends Activity {
    private static final String TAG = InviteFriend.class.getSimpleName();
    private static final int REQUEST_INVITE = 0;
    private ArrayList<Map<String, String>> mPeopleList;
    private SimpleAdapter mAdapter;
    private MultiAutoCompleteTextView multiAutoComplete;
    private AutoCompleteTextView mTxtPhoneNo;
    Map<String, String> resultValue;
    ArrayList<String> phoneLists;
    ArrayList<Person> listItems;
    ArrayList<String> phoneList, nameslist;
    Person aContact;
    int selectedItem;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_invitation);
        Bundle extras=getIntent().getExtras();
        String phone_number;
        if(extras!=null) {
        //Intent intent = getIntent();
            phone_number = extras.getString("number");
        }
        mPeopleList = new ArrayList<Map<String, String>>();
        PopulatePeopleList();
        mTxtPhoneNo = (AutoCompleteTextView) findViewById(R.id.mmWhoNo);

        mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.actionbar_search ,new String[] { "Name", "Phone" , "Type" }, new int[] { R.id.ccontName, R.id.ccontNo, R.id.ccontType });
        mTxtPhoneNo.setAdapter(mAdapter);

        //Log.d(TAG,"The result is_: "+listItems);
        mTxtPhoneNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
                String name  = map.get("Name");
                String number = map.get("Phone");
                mTxtPhoneNo.setText(""+name+"<"+number+">");
            }
        });
        //final String reciever=mTxtPhoneNo.getText().toString();
        //Log.d(TAG,"Reciever Number is: "+reciever.toString());
         Button sendbtn=(Button)findViewById(R.id.sendinvite);
        sendbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                invite();

            }
        });
    }
    public void invite(){
        UserInfo info = new UserInfo();
        String address=info.getUserName();
        // send Notification through sms();

        // Google cloud messaging service will be implemeted here
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", address);
        smsIntent.putExtra("sms_body", "An invitation from a friend to use securecall for better communication" +
                "To download click on the googleplay link");
    }


    public void PopulatePeopleList()
    {
        mPeopleList.clear();
        phoneLists= new ArrayList<>();
        listItems= new ArrayList<>();
        Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        nameslist = new ArrayList<String>();
        phoneList = new ArrayList<String>();
        while (people.moveToNext())
        {

            String contactName = people.getString(people.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME));

            String contactId = people.getString(people.getColumnIndex(
                    ContactsContract.Contacts._ID));
            String hasPhone = people.getString(people.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if ((Integer.parseInt(hasPhone) > 0))
            {

                // You know have the number so now query it like this
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                        null, null);
                while (phones.moveToNext()) {

                    //store numbers and display a dialog letting the user select which.
                    String phoneNumber = phones.getString(
                            phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                    String numberType = phones.getString(phones.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.TYPE));

                    Map<String, String> NamePhoneType = new HashMap<String, String>();
                    NamePhoneType.put("Name", contactName);
                    NamePhoneType.put("Phone", phoneNumber);
//                    NamePhoneType.put("Type",  "Mobile");
                 try {
                     if (numberType.equals("0"))
                         NamePhoneType.put("Type", "Work");
                     else if (numberType.equals("1"))
                         NamePhoneType.put("Type", "Home");
                     else if (numberType.equals("2"))
                         NamePhoneType.put("Type", "Mobile");
                     else
                         NamePhoneType.put("Type", "Other");
                 }
                 catch (Exception e){
                     Log.d(TAG,e.getMessage());
                 }
                    //Then add this map to the list.
                    mPeopleList.add(NamePhoneType);
                }
                phones.close();
            }
        }
        people.close();

        startManagingCursor(people);
    }

}
