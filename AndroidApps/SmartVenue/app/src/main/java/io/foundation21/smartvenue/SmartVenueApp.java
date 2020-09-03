package io.foundation21.smartvenue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.View.VISIBLE;

public class SmartVenueApp extends AppCompatActivity
        implements SmartVenueResultReceiver.Receiver, View.OnTouchListener{

    private static final String TAG = "SmartVenueApp";
    private GestureDetector gestureDetector;

    public SmartVenueResultReceiver smartVenueReceiver;
    private float THRESHOLD_WARNING = 0.7f;
    private float THRESHOLD_ALARM = 0.95f;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_event_building);

        smartVenueReceiver = new SmartVenueResultReceiver(new Handler());
        smartVenueReceiver.setReceiver(this);

        gestureDetector = new GestureDetector(this, new GestureListener());


        Intent intent = new Intent(this, SmartVenueService.class);
        intent.putExtra(SmartVenueService.RECEIVER_APP, smartVenueReceiver);
        this.startService(intent);

    }

    protected void onResume() {
        super.onResume();
        hideSysUI();
    }

    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, SmartVenueService.class);
        intent.putExtra(SmartVenueService.PARAM_SHUTDOWN, "SHUTDOWN");
        this.startService(intent);
    }

    public void onReceiveResult(int cmd, Bundle resultData) {
        Log.v(TAG,"RECEIVED MSG "+cmd);

        switch (cmd) {
            case SmartVenueService.MSG_NEWDATA:
                if (resultData.containsKey(VenueLocation.PARAM_TIME)){
                    Date resultDate = new Date(resultData.getLong(VenueLocation.PARAM_TIME));
                    int counter = resultData.getInt(VenueLocation.PARAM_COUNTER);
                    int capacity = resultData.getInt(VenueLocation.PARAM_CAPACITY);
                    String eventDescription = resultData.getString(VenueLocation.PARAM_EVENTLABEL);


                    TextView eventStatus = findViewById(R.id.event_status);
                    TextView eventCounter = findViewById(R.id.event_counter);
                    TextView eventLabel = findViewById(R.id.event_label);
                    ImageView background = (ImageView)findViewById(R.id.background);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    eventStatus.setText(sdf.format(resultDate));
                    eventCounter.setText(counter+" / "+capacity);
                    eventLabel.setText(eventDescription);

                    if ((float)counter/(float)capacity>THRESHOLD_ALARM) {
                        background.setBackgroundColor(0xffff0000);
                        eventCounter.setTextColor(Color.WHITE);
                    }
                    else if ((float)counter/(float)capacity>THRESHOLD_WARNING) {
                        background.setBackgroundColor(0xffffcc00);
                        eventCounter.setTextColor(Color.BLUE);
                    }
                    else {
                        background.setBackgroundColor(0x00ffffff);
                        eventCounter.setTextColor(Color.BLACK);
                    }

                    // Screen doors
                    if (this.findViewById(R.id.door_labels)!=null) {
                        TextView doorLabels = findViewById(R.id.door_labels);
                        TextView doorCounters = findViewById(R.id.door_counters);
                        int[] doorCountersArr = resultData.getIntArray(VenueLocation.PARAM_DOOR_COUNTS);
                        String[] doorLabelsArr = resultData.getStringArray(VenueLocation.PARAM_DOOR_LABELS);
                        String doorLabelList = "";
                        String doorCounterList = "";
                        for (int i=0;i<doorCountersArr.length;i++){
                            doorLabelList+=(doorLabelsArr[i]+"<br>");
                            doorCounterList+=(String.valueOf(doorCountersArr[i])+"<br>");
                        }
                        doorLabels.setText(Html.fromHtml(doorLabelList));
                        doorCounters.setText(Html.fromHtml(doorCounterList));

                        if (resultData.containsKey(VenueLocation.PARAM_GRAPH_COUNTS)) {
                            long times[] = resultData.getLongArray(VenueLocation.PARAM_GRAPH_TIMES);
                            int counters[] = resultData.getIntArray(VenueLocation.PARAM_GRAPH_COUNTS);

                            ChartDoors graph = new ChartDoors(times, counters);
                            ((ImageView) findViewById(R.id.chart_image)).setImageDrawable(graph);
                        }
                    }
                }
                else
                    Log.w(TAG, "NEW DATA without TIME");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cmd);
        }
    }

    public void goto_doors(View view) {
        setContentView(R.layout.screen_event_doors);
        hideSysUI();
    }

    public void goto_overview(View view) {
        setContentView(R.layout.screen_event_building);
        hideSysUI();
    }

    private void hideSysUI(){
        View screen;
        screen = findViewById(R.id.layout);
        screen.setOnTouchListener(this);
        screen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    // --------- Gestures -----------------
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {onSwipeRight();} else {onSwipeLeft();}
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {onSwipeBottom();} else {onSwipeTop();}
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        public void onSwipeRight() {
            goto_overview(null);
        }

        public void onSwipeLeft() {
            goto_doors(null);
        }

        public void onSwipeTop() {}
        public void onSwipeBottom() {}
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

}