package com.example.castlesdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import CTOS.CtCL;

public class MainActivity extends AppCompatActivity {
    CtCL cl = new CtCL();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.buttonCL).setOnClickListener(view -> {
            Log.d(TAG, "CtCL");
            int x = clpp_test();
        });
        findViewById(R.id.buttonC).setOnClickListener(view -> {
            Log.d(TAG, "CtCS");
            Intent i = new Intent(this, MainActivity2.class);
            startActivity(i);
        });
    }

    public void CL_Delay() {
        long mCreationTime;
        mCreationTime = SystemClock.elapsedRealtime();
        while (SystemClock.elapsedRealtime() < (mCreationTime + 10)) ;
    }

    public int CL_ResetRF() {
        int ret;
        ret = cl.powerOff();
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.powerOff] PowerOff Fail."));
            Log.d(TAG, String.format("[cl.powerOff] ret = 0x%04X.", ret));
            return ret;
        }
        CL_Delay();
        ret = cl.powerOn();
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.powerOn] PowerOn Fail."));
            Log.d(TAG, String.format("[cl.powerOn] ret = 0x%04X.", ret));
            return ret;
        }
        return 0;
    }

    public int CL_FindCard(byte[] baCSN, int TimeOut) {
        long mCreationTime;
        int ret = 0;
        mCreationTime = SystemClock.elapsedRealtime();
        while (SystemClock.elapsedRealtime() < (mCreationTime + TimeOut)) {
            ret = cl.ppPolling(baCSN, 0);
            if (ret == 0) {
                break;
            }
        }
        if (ret != 0) {
            return ret;
        }
        return 0;
    }

    public int clpp_test() {
        int ret = 0;
        byte baCSN[] = new byte[10];
        byte baTxBuf[] = {(byte) 0x00, (byte) 0xA4, (byte) 0x02, (byte) 0x00,
                (byte) 0x02, (byte) 0x00, (byte) 0xB0};
        byte baRxBuf[] = new byte[256];
        int RxLen = 0;
        int baCSNLen = 0;
        int i = 0;
        // Reset contactless card sensing area.
        ret = CL_ResetRF();
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.powerOn] Rest Fail."));
            Log.d(TAG, String.format("[cl.powerOn] ret = 0x%04X.", ret));
            return ret;
        }
        // Start search contactless card, timeout is 1 second.
        ret = CL_FindCard(baCSN, 1000);
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.ppPolling] Find Card Fail"));
            Log.d(TAG, String.format("[cl.ppPolling] ret = 0x%04X.", ret));
            return ret;
        }
        baCSNLen = cl.getPPPollingCSNLen();
        Log.d(TAG, String.format("[cl.ppPolling] baCSNLen : %d", baCSNLen));
        for (i = 0; i < baCSNLen; i++) {
            Log.d(TAG, String.format("[cl.ppPolling] baCSN : %d", baCSN[i]));
        }
        // Activate contactless
        ret = cl.ppActivation(baCSN);
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.ppActivation] Active Card Fail"));
            Log.d(TAG, String.format("[cl.ppActivation] ret = 0x%04X.", ret));
            return ret;
        }
        // Send APDU command.
        ret = cl.ppAPDU(baTxBuf, baRxBuf, 256);
        if (ret != 0) {
            Log.d(TAG, String.format("[cl.ppAPDU] APDU Fail"));
            Log.d(TAG, String.format("[cl.ppAPDU] ret = 0x%04X.", ret));
            return ret;
        }
        RxLen = cl.getPPAPDURxLen();
        Log.d(TAG, String.format("[cl.ppAPDU] RxLen : %d", RxLen));
        for (i = 0; i < RxLen; i++) {
            Log.d(TAG, String.format("[cl.ppAPDU] RxBuf : %d", baRxBuf[i]));
        }
        ret = cl.ppRemoval(0);
        if (ret == 0x8208) {
            Log.d(TAG, String.format("[cl.ppRemoval] Card Still on the Operating Field "));
            Log.d(TAG, String.format("[cl.ppRemoval] ret = 0x%04X.", ret));
            return ret;
        }
        if (ret == 0x8207) {
            Log.d(TAG, String.format("[cl.ppRemoval] Card not ready. "));
            Log.d(TAG, String.format("[cl.ppRemoval] ret = 0x%04X.", ret));
            return ret;
        }
        Log.d(TAG, String.format("[cl.ppRemoval] Card has beed removed from the Operating Field "));
        Log.d(TAG, String.format("[cl.ppRemoval] ret = 0x%04X.", ret));
        Log.d(TAG, String.format("[cl] Test success!"));
        return ret;
    }
}