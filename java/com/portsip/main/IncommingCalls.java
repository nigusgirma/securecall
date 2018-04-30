package com.portsip.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.portsip.R;
import com.portsip.util.Line;

/**
 * Created by Nigussie on 21.10.2015.
 */


public class IncommingCalls extends AlertDialog {
    String incomingNumber;
    Line curSession;
    Context context;

    protected IncommingCalls(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    //myShowDialog(comingCallTips, curSession, requiredString);
    public IncommingCalls(Context context,Line curSession,String incomingNumber) {
        super(context);//,curSession,incomingNumber);
        this.context=context;
        this.curSession=curSession;
        this.incomingNumber=incomingNumber;
    }
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highlight_sms_call);

    }*/

}
