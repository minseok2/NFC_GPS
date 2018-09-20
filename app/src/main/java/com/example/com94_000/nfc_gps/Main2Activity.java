package com.example.com94_000.nfc_gps;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class Main2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        if(action == MotionEvent.ACTION_DOWN){
            finish();
        }

        return super.onTouchEvent(event);
    }//화면 터치시 창 종료
}
