package com.portsip.main;
/*
 * Property of IT Man AS, Bryne Norway It is strictly forbidden to copy or modify the authors file
 * (C) 2015 IT Man AS
 * Created by Nigussie on 03.03.2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.portsip.MessageFragment;
import com.portsip.MyApplication;
import com.portsip.R;

import java.util.ArrayList;

public class SendSms extends FragmentActivity{
    MessageFragment messageFragment = null;
    Fragment frontFragment;
    public ViewGroup layout=null;
    View v;
    ArrayList<String> mylist;
    public static ArrayList<String> phoneValueArr = new ArrayList<String>();
    public static ArrayList<String> nameValueArr = new ArrayList<String>();
    String msgType;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super .onCreate(savedInstanceState);
        setContentView(R.layout.mainview);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            msgType=extras.getString("type");
        }
        loadMessageFragment();
       /*Intent intent= new Intent(this,ChatBubbleActivity.class);
        startActivity(intent);*/
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    // this is the place that calls the message fragment..
    // The fragment manager will be called here
    private void loadMessageFragment() {
        Bundle args = new Bundle();
        if (messageFragment == null) {
            messageFragment = new MessageFragment();
        }
        args.putString("type", msgType);
       /*
        args.putStringArrayList("phoneList",phoneValueArr);
        args.putStringArrayList("nameList",nameValueArr);*/
        frontFragment = messageFragment;
           getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, messageFragment).commit();
    }
    @Override
    protected void onResume() {
        try {
            ((MyApplication) getApplicationContext()).setSmsActivity(this);
        }catch(Exception e){}
        super.onResume();
    }
    @Override
    protected void onPause() {
        try {
            ((MyApplication) getApplicationContext()).setSmsActivity(this);
        }catch(Exception e){}
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.setting) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
