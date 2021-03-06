package tw.imonkey.fu2go;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class BossActivity extends AppCompatActivity {
    public static final String service="Boss"; //主機 deviceType

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss);

    }

    public void onClickLogout(View v) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(BossActivity.this, LoginActivity.class));
                        finish();
                    }
                });

    }

    public void onClickAddDevicePLC(View v) {
        Intent intent = new Intent(BossActivity.this, AddThingsDeviceActivity.class);
        intent.putExtra("deviceType","PLC監控機");
        startActivity(intent);
        finish();

    }

    public void onClickAddDeviceRPI3IO(View v) {
        Intent intent = new Intent(BossActivity.this, AddThingsDeviceActivity.class);
        intent.putExtra("deviceType","GPIO智慧機");
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
