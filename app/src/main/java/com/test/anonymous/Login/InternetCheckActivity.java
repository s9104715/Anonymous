package  com.test.anonymous.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.test.anonymous.R;

public class InternetCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private Button reconnectBtn;
    private ProgressDialog reconnectPD;

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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reconnectPD.dismiss();
                        if(internetCheck()){
                            finish();
                            startActivity(new Intent(getApplicationContext() , LoginActivity.class));
                        }else {
                            onRestart();
                        }
                    }
                } , 1500);
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
        reconnectPD = new ProgressDialog(this);
        reconnectPD.setCancelable(false);
        reconnectPD.setCanceledOnTouchOutside(false);
        reconnectPD.setTitle("連線");
        reconnectPD.setMessage("連線中.....");
        reconnectPD.show();
    }
}
