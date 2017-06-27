package szalai.hu.videoplayertest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import android.os.Process;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

import static szalai.hu.videoplayertest.R.id.videoView;

public class MainActivity extends Activity {

    private static final String CHANEL1 = "http://5.226.137.176:8080/SKYSPORTS4/mpegts";
    private static final String CHANEL2 = "http://5.226.137.176:8080/ANIMALPLANET/mpegts";
    private static final String CHANEL3 = "http://5.226.137.176:8080/BOOMERANG/mpegts";
    private VideoView videoView = null;
    private TelephonyManager telephonyManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableRuntimePermission();
    }

    public void sendIMEI(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String IMEI = telephonyManager.getDeviceId();
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams("IMEI",IMEI);
        RequestHandle handle = asyncHttpClient.post("http://szemelyek.szalaimihaly.hu/imei.php", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Log.d("DEBUG","SUCCES");
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.d("DEBUG","FAILURE");
                throwable.printStackTrace();
            }
        });
    }

    private void setChanel(String chanel){
        Uri uri = Uri.parse(chanel);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

    }

    private void enableRuntimePermission() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED))  {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.INTERNET}
                            , 10);
                } else {
                    sendIMEI();
                    setView();
                }
            } else {
                sendIMEI();
                setView();
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10:
                if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    sendIMEI();
                    setView();
                } else {
                    Toast.makeText(this, "This app requieres location permissions to be granted", Toast.LENGTH_LONG).show();
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"helloka",Toast.LENGTH_LONG).show();
                            //finish();
                        }
                    },3000);
                }
                break;
        }
    }

    private void setView(){
        setContentView(R.layout.activity_main);
        videoView = (VideoView) findViewById(R.id.videoView);
        //MediaController mediaController = new MediaController(this);
        //mediaController.setAnchorView(videoView);
        setChanel(CHANEL1);
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Válasszon csatornát!");
                String[] options = {"Chanel1", "Chanel2", "Chanel3"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            setChanel(CHANEL1);
                        }
                        if (which == 1) {
                            setChanel(CHANEL2);
                        }
                        if (which == 2) {
                            setChanel(CHANEL3);
                        }
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
                final Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        timer.cancel();
                    }
                }, 3000);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

}
