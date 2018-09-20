package com.example.com94_000.nfc_gps;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView tagInfo; //tag 정보
    private TextView chkConn; //tag 연결 확인
    private boolean mess = true; //tag 연결 해제 시 화면 출력 판별
    private float minD = (float) 0.01; //측정 될 이동 거리
    private int minT = 2; //측정 간 최소 시간간격
    private Tag tag;
    private Handler handler;
    public static final String CHARS = "0123456789ABCDEF";
    Button button;
    LocationManager lm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }//위치 권한

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //stateCheck(); //기능이 켜져 있는지 상태 체크



        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); //NFC 센싱을 위해 사용됨
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        tagInfo = (TextView)findViewById(R.id.textView);
        chkConn = (TextView) findViewById(R.id.textView2);
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent); //Background로 넘어감
            }
        });
    }//End of onCreate

    public void stateCheck(){

        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "GPS를 설정해주세요.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }//GPS 상태 확인 ( 기능을 켜지 않았을 경우 호출 )


        ConnectivityManager conMan = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
        NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();

        if((wifi == NetworkInfo.State.DISCONNECTED) && (mobile == NetworkInfo.State.DISCONNECTED)) {
            Toast.makeText(getApplicationContext(),"인터넷 연결 여부를 확인해주세요.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }//LTE, Wifi 연결 상태를 확인 ( 기능을 켜지 않았을 경우 호출 )


        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplication());

        if (!nfcAdapter.isEnabled())
        {
            Toast.makeText(getApplicationContext(), "NFC 연결 여부를 확인해주세요.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        } //NFC 연결 연결 상태를 확인 ( 기능을 켜지 않았을 경우 호출 )

    }

    public void checkNFCAndLocation(){

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }//위치 권한 처리


            if(tag != null) {

                Ndef ndefTag = Ndef.get(tag);
                ndefTag.connect(); // NFC connect

                if (ndefTag.isConnected()) {

                    Log.d("network", "NFC connected");
                    chkConn.setText("NFC connected");

                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            minT, minD, mLocationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            minT, minD, mLocationListener);
                    //NFC 연결 시, GPS와 NETWORK의 위치 정보를 호출 ( 위치 제공자, 최소시간, 최소거리, 리스너 )

                    if(!mess){
                        Toast.makeText(getApplication(),"연결됨",Toast.LENGTH_LONG).show();
                    }

                    mess= true;

                } else {
                    Log.d("network", "NFC disconnected");
                    chkConn.setText("NFC disconnected");

                    lm.removeUpdates(mLocationListener); //위치관리자 제거
                    callActivity();//Connect가 끊길 시 Activity 호출
                }
                ndefTag.close();
            }

        } catch (IOException e) {
            //e.printStackTrace();
            Log.d("network", "NFC disconnected");
            chkConn.setText("NFC disconnected");

            lm.removeUpdates(mLocationListener);
            callActivity();//Connect가 끊길 시 Activity 호출
        }//Excepiton 시 에도 Disconnected와 같이 동작
    }//NFC 연결 여부와 GPS 정보를 취득

    public void callActivity(){

        if(mess) {

            Intent intent2 = new Intent(this, Main2Activity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); //Task 분담
            startActivity(intent2);

            Toast.makeText(getApplication(),"연결이 끊김", Toast.LENGTH_LONG).show();
            mess = false;
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            Log.d("LocState", "onLocationChanged, location : " + location);

            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude(); //위도
            double altitude = location.getAltitude(); //고도
            float accuracy = location.getAccuracy(); //정확도
            String provider = location.getProvider(); //위치

            Log.d("LocInfo","위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);
        }

        public void onProviderDisabled(String provider) {
            Log.d("LocState", "onProviderDisabled, provider : " + provider);
        }

        public void onProviderEnabled(String provider) {
            Log.d("LocState", "onProviderEnabled, provider : " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("LocState", "onStatusChanged, provider : " + provider + ", status : " + status + " ,Bundle : " + extras);
        }

    };//Location 정보 취득 할 수 있는 Listener

    @Override
    protected void onNewIntent(Intent intent) {

        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){

            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] tagId = tag.getId();
            tagInfo.setText("TagId: " + toHexString(tagId)); //태그 id 표시

            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkNFCAndLocation(); //NFC Connect 확인
                    handler.postDelayed(this,1000); //1초 delay
                }
            }, 1000);
            //핸들러를 통해 Background 내 에서도 동작하게 끔 설정
        }
    }

    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; ++i) {
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(CHARS.charAt(data[i] & 0x0F));
        }
        return sb.toString();

    } //byte -> String( 16진수 전환 후 저장 )

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

}//End of class
