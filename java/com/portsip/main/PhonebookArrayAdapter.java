package com.portsip.main;
/**
 * Created by Admin on 29.09.2015.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.portsip.Calling;
import com.portsip.MainActivity;
import com.portsip.MyApplication;
import com.portsip.R;
import com.portsip.bulk.InviteFriend;

import org.json.JSONException;
import org.json.JSONObject;

import helper.SQLiteHandler;
import helper.SessionManager;

public class PhonebookArrayAdapter extends BaseAdapter implements ListAdapter {
    private static final String TAG = PhonebookArrayAdapter.class.getSimpleName();
    private ArrayList<Person> list = new ArrayList<Person>();
    public String name;
    public String number,resnumber;
    public SessionManager session;
    public SQLiteHandler db;
    private boolean checkFlag;
    private Context context;
    int count=0;
    String result;
    TextView listItemText;
    TextView listItemText2;
    String [] myres;// = new String[0];
    private ArrayList<String> phoneArray,tempPhoneArray;//
    ViewHolder mHolder;
    LayoutInflater inflater;
    MyApplication myapp;;
    public PhonebookArrayAdapter(ArrayList<Person> list, Context context) {
        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
        this.context = context;
        this.phoneArray = new ArrayList<String>();
        this.tempPhoneArray=new ArrayList<>();
        this.myapp = new MyApplication();
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }
    @Override
    public long getItemId(int pos) {
        return list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        count+=1;
                if (convertView == null) {
                convertView = inflater.inflate(R.layout.phonebook_list_items_alter, null);
                mHolder = new ViewHolder();
                Log.d(TAG, String.valueOf(position));
                mHolder.eText1 = (TextView) convertView.findViewById(R.id.text1);
                mHolder.eText2 = (TextView) convertView.findViewById(R.id.text2);
                mHolder.invitebtn = (Button) convertView.findViewById(R.id.btnInvite);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            mHolder.eText1.setText(list.get(position).getName());
            mHolder.eText2.setText(list.get(position).getPhoneNum());
        //for (int i = 0; i < list.size(); i++) {
            String num = list.get(position).getPhoneNum();
            num = num.replaceAll("[^\\d]", "");
            //result = check_db(num);
            if (result != null) {
                tempPhoneArray.add(result);//
            }else{
                Log.d(TAG,"Result is null");
            }
        //}
            //Not good approach sync is better
            Log.d(TAG, "position " + position);
            Log.d(TAG, "Limits" + phoneArray);
        if(list.get(position).getName()=="Nigussie"||list.get(position).getName()=="Atnasia"||list.get(position).getName()=="Egil Byberg"
                ){
            mHolder.invitebtn.setVisibility(View.VISIBLE);
            mHolder.invitebtn.setText("Call");
            mHolder.invitebtn.setBackgroundColor(Color.GREEN);
        }

            //}
            //Check mot db
            //MyApplication appen = new MyApplication();
            //phoneArray=appen.getPhone_list();
            Log.d(TAG, "Nå er Det" + phoneArray + tempPhoneArray);

        ArrayList<String> results = new ArrayList<>();
        ArrayList<String> temp=new ArrayList<>();
        for(int i=0;i<list.size();i++){
            temp.add((list.get(i).getPhoneNum()).replaceAll("[^\\d]", ""));
        }
//num = num.replaceAll("[^\\d]", "");
// ArrayList<Custom>
        Set<String> subtitles = new HashSet<String>();
        for (Iterator<String> it = tempPhoneArray.iterator(); it.hasNext(); ) {
            if (!subtitles.add(it.next())) {
                it.remove();
            }
        }
        System.out.println("tempPhoneArray: "+tempPhoneArray);
        for (String person2 : tempPhoneArray) {
            // Loop arrayList1 items
            boolean found = false;
            for (String person1 : temp) {
                if (person2 == person1) {
                    found = true;
                    mHolder.invitebtn.setVisibility(View.VISIBLE);
                    mHolder.invitebtn.setText("Call");
                    mHolder.invitebtn.setBackgroundColor(Color.GREEN);
                }
            }
            if (!found) {
                //results.add(person2);
            }
        }

        mHolder.invitebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //do something
                    //list.remove(position);//
                    if (mHolder.invitebtn.getText() != "Invite")
                    {
                        mHolder.invitebtn.setVisibility(View.VISIBLE);
                        mHolder.invitebtn.setText("Call");
                        mHolder.invitebtn.setBackgroundColor(Color.GREEN);
                        ringer(number);
                        notifyDataSetChanged();
                    } else {
                        mHolder.invitebtn.setVisibility(View.VISIBLE);
                        mHolder.invitebtn.setText("Invite");
                        mHolder.invitebtn.setBackgroundColor(Color.BLUE);
                        String dest_number= list.get(position).getPhoneNum();
                        //contact = contact.replace("<>", "");
                        //Log.d("InvitMain", "Contact" + contact);
                        Uri smsUri = Uri.parse("tel:" + dest_number);
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + dest_number));
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("address", dest_number);
                        smsIntent.putExtra("sms_body", "You are invited to use SecureCall -  app for encrypted voice and SMS (encrypted VoIP)\n" +
                            "        Download from Google Play for Android:    «link»\n" +
                            "        Download from App Store for IOS (Iphone):  Not launched yet - comming soon");
                    smsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(smsIntent);
                        /*Intent call_intet = new Intent(context.getApplicationContext(), InviteFriend.class);// Calling
                        call_intet.putExtra("nummer", list.get(position).getPhoneNum());
                        context.startActivity(call_intet);
                        notifyDataSetChanged();*/
                    }
                }
            });
            Log.d(TAG, "MYAPPPPPPPPPPPP" + myapp.getPhone_list());
            return convertView;
    }
    public void ringer(String phone){
        try {
            Intent startActivity = new Intent();
            startActivity.setClass(context.getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra("number",phone)
                    .putExtra("flag",true);
            PendingIntent pi = PendingIntent.getActivity(this.context.getApplicationContext(),0,startActivity,0);
            pi.send(this.context.getApplicationContext(),0,null);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        //context.getApplicationContext();
        //this.finish();
    }
    private class ViewHolder {
        private TextView eText1;
        private TextView eText2;
        private Button invitebtn;
    }
    public String check_db(final String myphone){
        // Let me call the backend class here
        // Tag used to cancel the request
        String tag_string_req = "req_checkin";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                    Log.d(TAG, "Fetching Response: " + response);
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            //User successfully stored in MySQL
                            //Now store the user in sqlite
                            //            String uid = jObj.getString("uid");
                            JSONObject user = jObj.getJSONObject("user");
                            Log.d(TAG, "users are: " + user);
                            String name = user.getString("name");
                            String phone = user.getString("phone");
                            checkFlag = true;
                            phoneArray.add(phone);
                            mHolder.invitebtn.setText("Call");
                            mHolder.invitebtn.setBackgroundResource(R.drawable.call_style);
                                    myapp.setPhone_list(phoneArray);
                            Log.d(TAG, "myapp.setPhone now is: " + myapp.getPhone_list());
                            Log.d(TAG, "name  and phone from db are" + phone);
                        } else {
                            // Error occurred in registration. Get the error
                            // message
                            checkFlag = false;
                            String errorMsg = jObj.getString("error_msg");
                            //Toast.makeText(context.getApplicationContext(),
                            //           errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Fetching Error: " + error.getMessage());
                Toast.makeText(context.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "checkin");
                params.put("phone",myphone);
                return params;
            }
        };



        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
        //return phone;
        if(checkFlag){
            return myphone;}
        else{return null;}
    }
}

/*
*
       //Tag used to cancel the request
        String tag_string_req = "req_login";
       //pDialog.setMessage("Logging in ...");
        intet_email=email.toString();
        Log.d(TAG, "intet_email=email" + intet_email);
        showDialog();

    }
**/