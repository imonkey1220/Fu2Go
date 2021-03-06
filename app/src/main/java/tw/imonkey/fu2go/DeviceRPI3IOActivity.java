package tw.imonkey.fu2go;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceRPI3IOActivity extends AppCompatActivity {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="RPI3IO"; //IO智慧機 deviceType
    CharSequence[] items = {"EMAIL","PUSH","SMS"};//notify_user type
    boolean[] checkedValues = new boolean[items.length];//notify_user flag
    String deviceId, memberEmail;
    boolean master;
    ArrayList<String> users = new ArrayList<>();
    Map<String, Object> cmd = new HashMap<>();
    Map<String, Object> log = new HashMap<>();
    DatabaseReference mUsers, mDevice,mState,mAlert,mLog,mSETTINGS;
    FirebaseListAdapter mAdapter;
    ListView userView ,logView;
    Switch Y00,Y01,Y02,Y03,Y04,Y05,Y06,Y07;
    TextView X00,X01,X02,X03,X04,X05,X06,X07;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_rpi3_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        X00=(TextView) findViewById(R.id.textViewX00);
        X01=(TextView) findViewById(R.id.textViewX01);
        X02=(TextView) findViewById(R.id.textViewX02);
        X03=(TextView) findViewById(R.id.textViewX03);
        X04=(TextView) findViewById(R.id.textViewX04);
        X05=(TextView) findViewById(R.id.textViewX05);
        X06=(TextView) findViewById(R.id.textViewX06);
        X07=(TextView) findViewById(R.id.textViewX07);

        Y00=(Switch) findViewById(R.id.switchY00);
        Y01=(Switch) findViewById(R.id.switchY01);
        Y02=(Switch) findViewById(R.id.switchY02);
        Y03=(Switch) findViewById(R.id.switchY03);
        Y04=(Switch) findViewById(R.id.switchY04);
        Y05=(Switch) findViewById(R.id.switchY05);
        Y06=(Switch) findViewById(R.id.switchY06);
        Y07=(Switch) findViewById(R.id.switchY07);
        init();
        SETTINGS();
        logView();
        mXINPUTListener();
        mYOUTPUTListener();

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (master) {
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_friend:
                AlertDialog.Builder dialog_add = new AlertDialog.Builder(DeviceRPI3IOActivity.this);
                LayoutInflater inflater = LayoutInflater.from(DeviceRPI3IOActivity.this);
                final View v = inflater.inflate(R.layout.add_friend, userView, false);
                dialog_add.setTitle("邀請朋友加入智慧機服務");
                dialog_add.setView(v);
                dialog_add.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText editTextAddFriendEmail = (EditText) (v.findViewById(R.id.editTextAddFriendEmail));
                        if (!editTextAddFriendEmail.getText().toString().isEmpty()) {
                            DatabaseReference mAddfriend = FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId);
                            mAddfriend.child("/users/"+editTextAddFriendEmail.getText().toString().replace(".", "_")).setValue(editTextAddFriendEmail.getText().toString());
                            Toast.makeText(DeviceRPI3IOActivity.this, "已寄出邀請函(有效時間10分鐘)", Toast.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    }
                });
                dialog_add.show();
                return true;

            case R.id.action_del_friend:
                users.remove(users.indexOf(memberEmail));//remove boss
                AlertDialog.Builder dialog_del = new AlertDialog.Builder(DeviceRPI3IOActivity.this);
                dialog_del.setTitle("選擇要刪除的朋友");
                dialog_del.setItems(users.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DeviceRPI3IOActivity.this, "你要刪除是" + users.get(which), Toast.LENGTH_SHORT).show();
                        mUsers.orderByValue().equalTo(users.get(which)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();
                                    users.add(memberEmail);//add boss
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        users.remove(which);
                    }
                });
                dialog_del.show();
                return true;
            case R.id.action_SETTINGS:

                // arraylist to keep the selected items
                final ArrayList<Integer> seletedItems=new ArrayList<>();
                AlertDialog dialog_settings = new AlertDialog.Builder(this)
                                    .setTitle("訊息通知設定")
                                    .setMultiChoiceItems(items,checkedValues, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    seletedItems.add(indexSelected);
                                    checkedValues[indexSelected]=true;
                                } else if (seletedItems.contains(indexSelected)) {
                                    // Else, if the item is already in the array, remove it
                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                    checkedValues[indexSelected]=false;
                                }
                            }
                        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //  Your code when user clicked on OK
                                //  You can write the code  to save the selected item here
                                for(int i=0;i<items.length;i++) {
                                    if (checkedValues[i]) {
                                        mSETTINGS.child("/notify/"+items[i]).setValue(true);
                                    }else{
                                        mSETTINGS.child("/notify/"+items[i]).removeValue();
                                    }
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //  Your code when user clicked on Cancel

                            }
                        }).create();
                dialog_settings.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    private void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Taipei"));
        Bundle extras = getIntent().getExtras();
        deviceId = extras.getString("deviceId");
        memberEmail = extras.getString("memberEmail");
        master = extras.getBoolean("master");
        mDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId);
        mSETTINGS = FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId + "/SETTINGS");
        mLog=FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/LOG/");
        mAlert= FirebaseDatabase.getInstance().getReference("/DEVICE/"+ deviceId + "/alert");
        mState=FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/state/");
        mDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.child("connection").getValue() != null) {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "上線");
                    } else {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "離線");
                        Toast.makeText(DeviceRPI3IOActivity.this, "GPIO智慧機離線", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //Device's Users
        mUsers= FirebaseDatabase.getInstance().getReference("/DEVICE/"+deviceId+"/users/");
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    users.add(childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
    private void log(String message) {
        log.clear();
        log.put("message", message);
        log.put("memberEmail", memberEmail);
        log.put("timeStamp", ServerValue.TIMESTAMP);
        mLog.push().setValue(log);
    }
    private void logView(){
        Query refDevice = FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/LOG/").limitToLast(25);
        logView = (ListView) findViewById(R.id.listViewLog);
        mAdapter= new FirebaseListAdapter<Message>(this, Message.class, android.R.layout.two_line_list_item, refDevice) {
            @Override
            public Message getItem(int position) {
                return super.getItem(getCount() - (position + 1)); //反轉排序
            }

            @Override
            protected void populateView(View view, Message message, int position) {
                Calendar timeStamp= Calendar.getInstance();
                timeStamp.setTimeInMillis(message.getTimeStamp());
                SimpleDateFormat df = new SimpleDateFormat(" HH:mm:ss MM/dd", Locale.TAIWAN);
                if (position%2==0) {
                    ((TextView) view.findViewById(android.R.id.text1)).setText(message.getMessage());
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.BLUE);
                }else{
                    ((TextView) view.findViewById(android.R.id.text1)).setText(message.getMessage());
                    ((TextView) view.findViewById(android.R.id.text1)).setTextColor(Color.RED);
                }
                ((TextView)view.findViewById(android.R.id.text2)).setText((df.format(timeStamp.getTime())));

            }
        };
        logView.setAdapter(mAdapter);

    }
    public void buttonSendMessageOnClick(View view){
        EditText editTextTalk=(EditText)findViewById(R.id.editTextTalk);
        //   DatabaseReference mTalk=FirebaseDatabase.getInstance().getReference("/LOG/GPIO/" + deviceId+"/LOG/");
        DatabaseReference mTalk=FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/LOG/");
        if(TextUtils.isEmpty(editTextTalk.getText().toString().trim())){
            Map<String, Object> addMessage = new HashMap<>();
            addMessage.put("memberEmail",memberEmail);
            addMessage.put("message","Gotcha:"+memberEmail);
            addMessage.put("timeStamp", ServerValue.TIMESTAMP);
            mTalk.push().setValue(addMessage);
            Toast.makeText(DeviceRPI3IOActivity.this, "Gotcha!", Toast.LENGTH_LONG).show();
        }else{
            Map<String, Object> addMessage = new HashMap<>();
            addMessage.put("memberEmail",memberEmail);
            addMessage.put("message","Gotcha:"+memberEmail+"->"+editTextTalk.getText().toString().trim());
            addMessage.put("timeStamp", ServerValue.TIMESTAMP);
            mTalk.push().setValue(addMessage);
            Toast.makeText(DeviceRPI3IOActivity.this,editTextTalk.getText().toString().trim(), Toast.LENGTH_LONG).show();
            editTextTalk.setText("");
        }
    }
    private void mXINPUTListener(){
        mState.child("X00/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X00.setBackgroundColor(Color.RED);
                    }else{
                        X00.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("X01/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X01.setBackgroundColor(Color.RED);
                    }else{
                        X01.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X02/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X02.setBackgroundColor(Color.RED);
                    }else{
                        X02.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X03/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X03.setBackgroundColor(Color.RED);
                    }else{
                        X03.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X04/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X04.setBackgroundColor(Color.RED);
                    }else{
                        X04.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X05/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X05.setBackgroundColor(Color.RED);
                    }else{
                        X05.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X06/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X06.setBackgroundColor(Color.RED);
                    }else{
                        X06.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("X07/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if(snapshot.getValue().equals(true)) {
                        X07.setBackgroundColor(Color.RED);
                    }else{
                        X07.setBackgroundColor(Color.BLUE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private void mYOUTPUTListener(){
        Y00.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y00.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp", ServerValue.TIMESTAMP);
                    mState.child("Y00").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y00=true");
                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y00").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y00=false");
                }
            }
        });
        Y01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y01.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y01").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y01=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y01").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y01=false");

                }
            }
        });

        Y02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y02.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y02").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y02=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y02").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y02=false");

                }
            }
        });

        Y03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y03.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y03").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y03=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y03").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y03=false");
                }
            }
        });

        Y04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y04.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y04").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y04=true");
                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y04").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y04=false");

                }
            }
        });
        Y05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y05.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y05").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y05=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y05").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y05=false");

                }
            }
        });

        Y06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y06.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y06").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y06=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y06").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y06=false");

                }
            }
        });

        Y07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Y07.isChecked()) {
                    cmd.clear();
                    cmd.put("pinState",true);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y07").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y07=true");

                }else{
                    cmd.clear();
                    cmd.put("pinState",false);
                    cmd.put("memberEmail",memberEmail);
                    cmd.put("timeStamp",ServerValue.TIMESTAMP);
                    mState.child("Y07").updateChildren(cmd);
                    log("Y_input:"+memberEmail+":Y07=false");
                }
            }
        });

        mState.child("Y00/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y00.setChecked(true);
                    } else {
                        Y00.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y01/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y01.setChecked(true);
                    } else {
                        Y01.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y02/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y02.setChecked(true);
                    } else {
                        Y02.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        mState.child("Y03/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y03.setChecked(true);
                    } else {
                        Y03.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y04/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                if (snapshot.getValue().equals(true)) {
                    Y04.setChecked(true);
                    } else {
                        Y04.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y05/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y05.setChecked(true);
                    } else {
                        Y05.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y06/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y06.setChecked(true);
                    } else {
                        Y06.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mState.child("Y07/pinState/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    if (snapshot.getValue().equals(true)) {
                        Y07.setChecked(true);
                    } else {
                        Y07.setChecked(false);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void SETTINGS() {
        mSETTINGS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (master) {
                    if (snapshot.child("/notify/EMAIL").getValue()!= null) {
                        checkedValues[0]=true;
                    } else {
                        checkedValues[0]=false;
                    }
                    if (snapshot.child("/notify/PUSH").getValue() != null) {
                        checkedValues[1]=true;
                    } else {
                        checkedValues[1]=false;
                    }
                    if (snapshot.child("/notify/SMS").getValue() != null) {
                        checkedValues[2]=true;
                    } else {
                        checkedValues[2]=false;
                    }
                }
                if (snapshot.child("X00").getValue() != null) {
                    X00.setText(snapshot.child("X00").getValue().toString());
                }
                if (snapshot.child("X01").getValue() != null) {
                    X01.setText(snapshot.child("X01").getValue().toString());
                }
                if (snapshot.child("X02").getValue() != null) {
                    X02.setText(snapshot.child("X02").getValue().toString());
                }
                if (snapshot.child("X03").getValue() != null) {
                    X03.setText(snapshot.child("X03").getValue().toString());
                }
                if (snapshot.child("X04").getValue() != null) {
                    X04.setText(snapshot.child("X04").getValue().toString());
                }
                if (snapshot.child("X05").getValue() != null) {
                    X05.setText(snapshot.child("X05").getValue().toString());
                }
                if (snapshot.child("X06").getValue() != null) {
                    X06.setText(snapshot.child("X06").getValue().toString());
                }
                if (snapshot.child("X07").getValue() != null) {
                    X07.setText(snapshot.child("X07").getValue().toString());
                }
                if (snapshot.child("Y00").getValue() != null) {
                    Y00.setText(snapshot.child("Y00").getValue().toString());
                }
                if (snapshot.child("Y01").getValue() != null) {
                    Y01.setText(snapshot.child("Y01").getValue().toString());
                }
                if (snapshot.child("Y02").getValue() != null) {
                    Y02.setText(snapshot.child("Y02").getValue().toString());
                }
                if (snapshot.child("Y03").getValue() != null) {
                    Y03.setText(snapshot.child("Y03").getValue().toString());
                }
                if (snapshot.child("Y04").getValue() != null) {
                    Y04.setText(snapshot.child("Y04").getValue().toString());
                }
                if (snapshot.child("Y05").getValue() != null) {
                    Y05.setText(snapshot.child("Y05").getValue().toString());
                }
                if (snapshot.child("Y06").getValue() != null) {
                    Y06.setText(snapshot.child("Y06").getValue().toString());
                }
                if (snapshot.child("Y07").getValue() != null) {
                    Y07.setText(snapshot.child("Y07").getValue().toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if (master) {
            X00.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X00");
                    return true;
                }
            });
            X01.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X01");
                    return true;
                }
            });
            X02.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X02");
                    return true;
                }
            });
            X03.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X03");
                    return true;
                }
            });
            X04.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X04");
                    return true;
                }
            });
            X05.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X05");
                    return true;
                }
            });
            X06.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X06");
                    return true;
                }
            });
            X07.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("X07");
                    return true;
                }
            });

            Y00.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y00");
                    return true;
                }
            });
            Y01.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y01");
                    return true;
                }
            });
            Y02.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y02");
                    return true;
                }
            });
            Y03.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y03");
                    return true;
                }
            });
            Y04.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y04");
                    return true;
                }
            });
            Y05.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y05");
                    return true;
                }
            });
            Y06.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y06");
                    return true;
                }
            });
            Y07.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    showDialog("Y07");
                    return true;
                }
            });
        }
    }
    private void showDialog(final String pin) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(pin)
                .setMessage("請輸入"+pin+"功能")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mSETTINGS.child(pin).setValue(input.getText().toString());
                        mState.child(pin+"/name/").setValue(input.getText().toString());
                        mState.child(pin+"/pinId/").setValue(mState.push().getKey());
                    }
                })
                .show();
    }
}

