package  com.test.anonymous.Login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.anonymous.R;
import com.test.anonymous.Tools.Task;

import java.util.Timer;
import java.util.TimerTask;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class InternetCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private Button reconnectBtn;
    private Task reconnectTask;
    private ACProgressFlower reconnectPD;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_internet_check);

        reconnectBtn = findViewById(R.id.reconnect_btn);

        reconnectBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.reconnect_btn:
                showRestartDialog();
                buildRestartTask();
                reconnectTask.activateTask(1500 , 1000);
                break;
        }
    }

    private boolean internetCheck(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getActiveNetworkInfo()!=null){
            return connectivityManager.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    public void showRestartDialog(){
        reconnectPD = new ACProgressFlower.Builder(this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Loading......")
                .fadeColor(Color.DKGRAY).build();
        reconnectPD.setCancelable(false);
        reconnectPD.setCanceledOnTouchOutside(false);
        reconnectPD.show();
    }

    public void buildRestartTask(){
        reconnectTask = new Task(new Timer(), new TimerTask() {
            @Override
            public void run() {
                if(reconnectPD !=null){
                    Log.e("RestartTask" , "isRunning!");
                    reconnectPD.dismiss();
                    reconnectTask.disableTask();
                    if(internetCheck()){
                        finish();
                        startActivity(new Intent(getApplicationContext() , LoginActivity.class));
                    }else {
                        onRestart();
                    }
                }
            }
        });
    }
}
