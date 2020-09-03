package io.foundation21.smartvenue;

import android.os.Bundle;
import android.util.Log;

import java.util.Vector;


public class VenueLocation {

    static int DEMO_LOCATION_ID = 1000;
    static int DEMO_CAPACITY = 150;
    static String DEMO_LABEL = "Infinimesh Club Demo";

    static final String PARAM_TIME = "LONG_TIME";
    static final String PARAM_CAPACITY = "INT_CAPACITY";
    static final String PARAM_COUNTER = "INT_COUNTER";
    static final String PARAM_EVENTLABEL = "STR_LABEL";
    static final String PARAM_DOOR_LABELS = "STR_DOOR_LABEL";
    static final String PARAM_DOOR_COUNTS = "INT_DOOR_COUNT";
    static final String PARAM_GRAPH_TIMES = "LONG_GRAPH_TIME";
    static final String PARAM_GRAPH_COUNTS = "INT_GRAPH_COUNT";

    private final static String TAG = "VenueLocation";

    private static int MAX_DATA_CAPACITY = 240;
    long id;
    String description;
    int capacity;
    Vector <Device>  devices;
    Vector <DeviceData> data;

    public VenueLocation(long id, String description, int capacity){
        this.id=id;
        this.capacity=capacity;
        this.description=description;
        devices = new Vector();
        data = new Vector();
    }

    public boolean addData(DeviceData newData){
        for (Device device : devices) {
            if (newData.id == device.id){
                device.counter=newData.counter;
                data.add(newData);
                data.lastElement().totalCounter= totalCount();
                if (data.size()> MAX_DATA_CAPACITY) data.remove(0);
                return true;
            }
        }
        return false;
    }

    public int totalCount(){
        int counter=0;
        for (Device device : devices) counter+=device.counter;
        return counter;
    }

    public Bundle getBundle(){
        if (data.size()==0) return null;
        Bundle bundle = new Bundle();
        DeviceData deviceData = data.lastElement();
        bundle.putLong(PARAM_TIME, deviceData.timestamp);
        bundle.putInt(PARAM_COUNTER, totalCount());
        bundle.putInt(PARAM_CAPACITY,capacity);
        bundle.putString(PARAM_EVENTLABEL, description);

        // Door Data
        String[] doorLabels = new String[devices.size()];
        int[] doorCounters = new int[devices.size()];

        for (int i=0; i<doorCounters.length;i++){
            doorLabels[i]=devices.elementAt(i).description;
            doorCounters[i]=devices.elementAt(i).counter;
        }

        bundle.putStringArray(PARAM_DOOR_LABELS,doorLabels);
        bundle.putIntArray(PARAM_DOOR_COUNTS,doorCounters);

        long lastData = data.size()==0 ? System.currentTimeMillis() : data.lastElement().timestamp;
        int dataCounter=0;
        for (DeviceData devData : data)
            if (devData.timestamp+ChartDoors.MIN_TIMERANGE>=lastData) dataCounter++;

        if (dataCounter>0) {
            long timestamps[] = new long[dataCounter];
            int counters[] = new int[dataCounter];
            dataCounter = 0;
            for (DeviceData devData : data)
                if (devData.timestamp + ChartDoors.MIN_TIMERANGE >= lastData) {
                    timestamps[dataCounter] = devData.timestamp;
                    counters[dataCounter] = devData.totalCounter;
                    dataCounter++;
                }
            bundle.putLongArray(PARAM_GRAPH_TIMES,timestamps);
            bundle.putIntArray(PARAM_GRAPH_COUNTS,counters);
        }

        return bundle;
    }

    // Demo Mode Begin ----------
    public VenueLocation(){
        this (DEMO_LOCATION_ID, DEMO_LABEL,DEMO_CAPACITY);
        devices.add(new Device(100,"Eingang Südtor"));
        devices.add(new Device(101,"Eingang Westtor"));
        devices.add(new Device(102,"Hintereingang"));
    }

    public DeviceData createDemoDeviceData(){
        DeviceData deviceData = new DeviceData();
        deviceData.timestamp = System.currentTimeMillis();
        if (Math.random()>0.65) deviceData.id=100;
        else if (Math.random()>0.4) deviceData.id=101;
        else if (Math.random()>0.3) deviceData.id=102;

        for (Device device : devices)
            if (device.id == deviceData.id) {
                deviceData.counter=device.counter;
                break;
            }

        float delta = 1;
        float ratio = (float) totalCount()/(float)capacity;
        if (ratio>0.0f && Math.random()>0.9f) delta = -1;
        if (ratio>0.7f && Math.random()>0.4f) delta = -1;
        if (ratio>0.95f && Math.random()>0.95f) delta = -1;
        if (ratio<0.7f && Math.random()>0.2f) delta = 1;
        if (ratio<0.95f && Math.random()>0.7f) delta = 1;
        if (ratio>=0.95f && Math.random()>0.95f) delta = 1;
        if (ratio>1.1f) delta=-1;
        if (ratio==0f && delta==-1) delta=1;
        delta = Math.round(8f*Math.random())*delta;
        deviceData.counter+=delta;
        Log.v(TAG,"demo: "+ratio+" / "+delta + " --> "+deviceData.counter);
        return deviceData;
    }
    // Demo Mode End ---------

}
