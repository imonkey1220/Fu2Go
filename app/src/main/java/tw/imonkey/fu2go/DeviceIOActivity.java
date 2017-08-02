package tw.imonkey.fu2go;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    DatabaseReference mUsers, mDevice,mLog,mXINPUT,mYOUTPUT,mSETTINGS;
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

}
