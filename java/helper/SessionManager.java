package helper;

/**
 * Created by Nigussie on 26.03.2015.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

public class SessionManager{
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared Preferences
    SharedPreferences pref;
    SharedPreferences preferences;
    Editor editor;
    Context _context;
    public ArrayList<String> globalList;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "Login";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_NAME="Name";
    private static final String KEY_PHONE="Phone";
    private static final String KEY_EMAIL="Email";
    private static final String KEY_PASSWORD="Password";
    private static final String KEY_DEST="destination";
    private static final String KEY_FLAG_call="flag";
    private static final String KEY_DEST_NAME="destinationNumber";
    private static final String KEY_MSG_SENDER="sender";
    private static final String KEY_MSG_TYPE="msgtype";
    private static final String KEY_MSG="msg";
    private static final String KEY_MSG_ID="msgId";// This one i need it to save the msg forever
    private static final String KEY_MSG_FLAG="msgFlag";

    private static boolean gFlag;
    private static boolean gSmsFlag;
    private static String gName;
    private static String gPhone;
    private static String gPassword;
    private static String gEmail;
    private static String gDestNumber;
    private static String gDestName;
    private static String gSender;
    private static String gMsgType;
    private static String gMsg;
    private static String gMsgId;
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn,String name,String phone,String password,String email) {
        gName=name;
        gEmail=email;
        gPhone=phone;
        gPassword=password;
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putString(KEY_NAME,name);
        editor.putString(KEY_EMAIL,email);
        editor.putString(KEY_PASSWORD,password);
        editor.putString(KEY_PHONE,phone);
        // commit changes
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }
    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }
    public String getName(){
        return pref.getString(KEY_NAME, gName);
    }
    public String getPhone(){
        return pref.getString(KEY_PHONE,gPhone);
    }
    public String getPassword(){
        return pref.getString(KEY_PASSWORD,gPassword);
    }
    public String getEmail(){
        return pref.getString(KEY_EMAIL,gEmail);
    }
    public void setDestNumber(String destPhone){
        gDestNumber=destPhone;
        editor.putString(KEY_DEST,destPhone);
        editor.commit();
    }
    public String getDestValue(){
        return pref.getString(KEY_DEST,gDestNumber);
    }
    public void setDestName(String destName){
        gDestName=destName;
        editor.putString(KEY_DEST_NAME,destName);
        editor.commit();
    }
    public String getDestName(){
        return pref.getString(KEY_DEST_NAME,gDestName);
    }
    public void setCallingFlag(boolean flag){
        gFlag=flag;
        editor.putBoolean(KEY_FLAG_call,flag);
        editor.commit();
    }
    public boolean getCallingFlag(){
        return pref.getBoolean(KEY_FLAG_call,gFlag);
    }
    public void setSmsInf(String sender, String msgType,String msg,String msgId,boolean smsFlag){
//    private static String gSender;private static String gMsgType;private static String gMsg;private static String gMsgId;
    gSender=sender;
    gMsgType=msgType;
    gMsg=msg;
    gMsgId=msgId;
    editor.putString(KEY_MSG_SENDER,sender);
    editor.putString(KEY_MSG_TYPE,msgType);
    editor.putString(KEY_MSG,msg);
    editor.putString(KEY_MSG_ID, msgId);
    editor.putBoolean(KEY_MSG_FLAG, smsFlag);
    editor.commit();
    }
    public String getMsgSender(){
        return pref.getString(KEY_MSG_SENDER, gSender);
    }
    public String getMsgType(){
        return pref.getString(KEY_MSG_TYPE, gMsgType);
    }
    public String getMsg(){
        return pref.getString(KEY_MSG, gMsg);
    }
    public String getMsgId(){
        return pref.getString(KEY_MSG_ID, gMsgId);
    }
    public boolean isInSmsFlag(){
        return pref.getBoolean(KEY_MSG_FLAG, false);
    }
    /**
     *
     * private static final String KEY_MSG_SENDER="sender";
     private static final String KEY_MSG_TYPE="msgtype";
     private static final String KEY_MSG="msg";
     private static final String KEY_MSG_ID="msgId";//
     // Sender = requiredString
     // Message type = mimeType
     // Message = recievedMsg
     // Message id = Give Tag number
     *
     */

}
