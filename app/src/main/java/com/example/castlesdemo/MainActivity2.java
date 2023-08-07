package com.example.castlesdemo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import CTOS.CtSC;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";
    CtSC sc = new CtSC();
    int socket = 0, status = 0, ATRLen = 0, ret = 0, i = 0;
    byte baATR[] = new byte[128];
    byte baRBuf[] = new byte[128];
    byte baSBuf[] = new byte[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        findViewById(R.id.buttonTestSocket).setOnClickListener(view -> {
            Log.d(TAG, "testSocket");
            testSocket();
        });


    }
    public void testSocket (){
        for (socket = 0; socket < 1; socket++) {
            Log.d(TAG, String.format("test socket:%d", socket));
            ret = sc.status(socket);
            status = sc.getStatus();
            Log.d(TAG, String.format("getStatus return code:0x%02x, stat us:0 x%02x", ret, status));
            if (status == 0) {
                Log.d(TAG, String.format("card no exist"));
            } else {
                ret = sc.resetISO(socket, 1, baATR);
                Log.d(TAG, String.format("resetISO return code:0x%02x", ret));
                ATRLen = sc.getATRLen();
                Log.d(TAG, String.format("ATRLen = %d", ATRLen));
                Log.d(TAG, String.format("CardType = %d", sc.getCardType()));
                if (ret == 0) {
                    Log.d(TAG, "ATR :");
                    for (i = 0; i < ATRLen; i++) {
                        Log.d(TAG, String.format("0x%02x, ", baATR[i]));
                        baSBuf[0] = (byte) 0x00;
                        baSBuf[1] = (byte) 0x86;
                        baSBuf[2] = (byte) 0x00;
                        baSBuf[3] = (byte) 0x00;
                        baSBuf[4] = (byte) 0x08;
                        ret = sc.sendAPDU(socket, baSBuf, baRBuf);
                        Log.d(TAG, String.format("sendAPDU return code:0x%02x", ret));
                        if (ret == 0) {
                            Log.d(TAG, "RX :");
                            for (i = 0; i < sc.getRLen(); i++) {
                                Log.d(TAG, String.format("0x%02x, ", baRBuf[i]));
                            }
                        }
                    }
                }
            }
        }
    }
}

