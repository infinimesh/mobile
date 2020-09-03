package io.foundation21.smartvenue;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class SmartVenueResultReceiver extends ResultReceiver {

        private Receiver mReceiver;

        public SmartVenueResultReceiver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        public interface Receiver {
            public void onReceiveResult(int resultCode, Bundle resultData);

        }

        public void setReceiver(Receiver receiver) {
            mReceiver = receiver;
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (mReceiver != null) {
                mReceiver.onReceiveResult(resultCode, resultData);
            }
        }


}
