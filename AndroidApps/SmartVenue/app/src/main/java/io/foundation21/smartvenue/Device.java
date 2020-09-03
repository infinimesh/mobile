package io.foundation21.smartvenue;

public class Device {
    long id;
    String description;
    int counter;

    public Device(long id, String description){
        this.id = id;
        this.description=description;
        counter=0;
    }

}
