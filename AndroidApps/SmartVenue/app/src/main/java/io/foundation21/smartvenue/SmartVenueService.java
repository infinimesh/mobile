package io.foundation21.smartvenue;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;


public class SmartVenueService extends Service {
    static final String TAG = "SmartVenueService";

    static final int MSG_NEWDATA = 100;

    static final String PARAM_MESSAGE = "DATA_BUILDING" ;
    static final String PARAM_SHUTDOWN = "SHUTDOWN";
    static final String RECEIVER_APP = "SMART_VENUE_APP_RECEIVER" ;


    private long CONFIG_POLL_CYCLE = 2000;

    private ResultReceiver SmartVenueAppReceiver = null;
    private AtomicBoolean doPoll = new AtomicBoolean(true);
    private long lastTime = 0;
    private int lastCounter = 0;
    private VenueLocation venueLocation;

    private Runnable pollData = new Runnable() {
        public void run() {
            while(doPoll.get()) {
                if (SmartVenueAppReceiver !=null) {
                    DeviceData data = null;
                    if (venueLocation.id == VenueLocation.DEMO_LOCATION_ID)
                        data = venueLocation.createDemoDeviceData();
                    else {
                        //-------------------  please overwrite
                        // get Devicedata from Cloud and store it to data;
                        // ------------------- end overwrite
                    }
                    venueLocation.addData(data);
                    SmartVenueAppReceiver.send(MSG_NEWDATA,venueLocation.getBundle());
                }
                try {
                    sleep(CONFIG_POLL_CYCLE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public IBinder onBind(Intent arg0) {
        Log.v(TAG,"ON BIND");
        return null;
    }

    public void onCreate() {
        long now=System.currentTimeMillis();
        Log.i(TAG,"onCreate Background service ...");

        // Demo Mode
        venueLocation = new VenueLocation();
        // ----------------

        new Thread(pollData).start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "startet Background service: " + startId);

        if (intent.hasExtra(PARAM_SHUTDOWN)){
            stopSelf();
        }

        if (SmartVenueAppReceiver == null && intent!=null && intent.hasExtra(RECEIVER_APP)) {
            SmartVenueAppReceiver = intent.getParcelableExtra(RECEIVER_APP);
        }

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        doPoll.set(false);
    }


    public void onStatusChanged(String s, int i, Bundle bundle) {}
    public void onProviderEnabled(String s) { }
    public void onProviderDisabled(String s) { }
}
