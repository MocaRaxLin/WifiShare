package com.sharerececive.wifishare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PaymentService extends Service {
    private Handler handler = new Handler();
    private static PaymentService paymentService = null;
    private Context mcontext;

    public PaymentService() {
    }

    public static PaymentService getInstance(Context context){
        if(paymentService == null)
            paymentService = new PaymentService();
        return paymentService;
    }

     @Override
     public void onCreate() {
     }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PaymentService", "Start payment service.");
        Toast.makeText(this, "Start payment service.", Toast.LENGTH_LONG).show();
        handler.postDelayed(sendPayment, 5000);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("PaymentService", "Stop payment service.");
        Toast.makeText(this, "Stop payment service.", Toast.LENGTH_LONG).show();
        handler.removeCallbacks(sendPayment);
        super.onDestroy();
    }

    private Runnable sendPayment = new Runnable() {
        public void run() {
            //Log.i("%%%%%SLAVE print SELLER ID%%%%%:", MainActivity.MasterID);
            IoWithServer.getInstance().sendPayment(PaymentService.this, MainActivity.MasterID);
            handler.postDelayed(this, 7000);
        }
    };
}
