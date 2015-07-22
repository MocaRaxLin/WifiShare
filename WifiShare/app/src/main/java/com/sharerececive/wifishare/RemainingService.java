package com.sharerececive.wifishare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class RemainingService extends Service {
    private Handler handler = new Handler();
    private static RemainingService remainingService = null;
    private Context mcontext;

    public RemainingService() {
    }


    public static RemainingService getInstance(Context context){
        if(remainingService == null)
            remainingService = new RemainingService();
        return remainingService;
    }

   /* public void startPayService(Intent intent){
        startService(intent);
    }

    public void stopPayService(Intent intent){
        stopService(intent);
    }*/

 //-------------------------------------------------------------------------
     @Override
     public void onCreate() {
     }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("RemainingService", "Start GetRemaining service.");
        Toast.makeText(this, "Start GetRemaining service.", Toast.LENGTH_LONG).show();
        handler.postDelayed(sendPayment, 5000);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("RemainingService", "Stop GetRemaining service.");
        Toast.makeText(this, "Stop GetRemaining service.", Toast.LENGTH_LONG).show();
        handler.removeCallbacks(sendPayment);
        super.onDestroy();
    }

    private Runnable sendPayment = new Runnable() {
        public void run() {
            //Log.i("time:", new Date().toString());
            //Log.i("%%%%%%%%%%:", MainActivity.Name);
            IoWithServer.getInstance().getRemaining(RemainingService.this);
            handler.postDelayed(this, 7000);
        }
    };
}
