package tw.imonkey.fu2go;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceIOActivity extends AppCompatActivity {
    public static final String devicePrefs = "devicePrefs";
    public static final String service="RPI3IO"; //GPIO智慧機 deviceType
    CharSequence[] items = {"EMAIL","PUSH","SMS"};
    boolean[] checkedValues = new boolean[items.length];
    String deviceId, memberEmail;
    boolean master;
    ArrayList<String> users = new ArrayList<>();
    Map<String, Object> cmd = new HashMap<>();
    Map<String, Object> log = new HashMap<>();
    DatabaseReference mUsers, mDevice,mState,mAlert,mLog,mXINPUT,mYOUTPUT,mSETTINGS;
    ListView userView;
    FirebaseRecyclerAdapter mPinoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPinoutAdapter.cleanup();
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
                AlertDialog.Builder dialog_add = new AlertDialog.Builder(DeviceIOActivity.this);
                LayoutInflater inflater = LayoutInflater.from(DeviceIOActivity.this);
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
                            Toast.makeText(DeviceIOActivity.this, "已寄出邀請函(有效時間10分鐘)", Toast.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    }
                });
                dialog_add.show();
                return true;

            case R.id.action_del_friend:
                users.remove(users.indexOf(memberEmail));//remove boss
                AlertDialog.Builder dialog_del = new AlertDialog.Builder(DeviceIOActivity.this);
                dialog_del.setTitle("選擇要刪除的朋友");
                dialog_del.setItems(users.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DeviceIOActivity.this, "你要刪除是" + users.get(which), Toast.LENGTH_SHORT).show();
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
        mXINPUT= FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/X/");
        mYOUTPUT=FirebaseDatabase.getInstance().getReference("/DEVICE/" + deviceId+"/Y/");
        mDevice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.child("connection").getValue() != null) {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "上線");
                    } else {
                        setTitle(snapshot.child("companyId").getValue().toString() + "." + snapshot.child("device").getValue().toString() + "." + "離線");
                        Toast.makeText(DeviceIOActivity.this, "GPIO智慧機離線", Toast.LENGTH_LONG).show();
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

    private void getPinState() {
        RecyclerView RV4 = (RecyclerView) findViewById(R.id.RVPinOut);
        RV4.setLayoutManager(new LinearLayoutManager(this));
        Query refDevice = FirebaseDatabase.getInstance().getReference("DEVICE/"+deviceId+"/state/");
        mPinoutAdapter = new FirebaseRecyclerAdapter<rvDevice, rvDeviceHolder>(
                rvDevice.class,
                R.layout.rv_device,
                rvDeviceHolder.class,
                refDevice) {

            @Override
            public void populateViewHolder( rvDeviceHolder holder, rvDevice device, final int position) {
            //todo
                holder.setPin(device.getPin());
                holder.setPinState(device.getpinState());
                Calendar timeStamp = Calendar.getInstance();
                timeStamp.setTimeInMillis(Long.parseLong(device.getTimeStamp().toString()));
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd", Locale.TAIWAN);
                holder.setTimeStamp(df.format(timeStamp.getTime()));
                holder.setPhoto(device.getPinId());
            }
        };
        RV4.setAdapter(mPinoutAdapter);
        RV4.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), RV4, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                //todo
            }

            @Override
            public void onLongClick(View view, int position) {
                //todo
            }
        }));
    }
}


