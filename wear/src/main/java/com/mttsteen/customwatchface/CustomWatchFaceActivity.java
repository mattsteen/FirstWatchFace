package com.mttsteen.customwatchface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.Date;

public class CustomWatchFaceActivity extends Activity {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private WatchAssetService watchAssetService;
    private Intent watchServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    @Override
    public void onResume() {

        super.onResume();

        // Watch Stub ************

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                setup();

            }

        });


    }

    @Override
    public void onDestroy() {

        unregisterReceiver(wearableMessageReceiver);
        stopWatchService();

        super.onDestroy();

    }

    private void setup() {

        // Setup *****************

        registerMessageReceivers();

        stopWatchService();
        startWatchService();

    }

    private void startWatchService() {

        watchServiceIntent = new Intent(this, WatchAssetService.class);
        startService(watchServiceIntent);

    }

    private void stopWatchService() {

        if (watchServiceIntent != null) {
            stopService(watchServiceIntent);
        }

    }

    public void setAssetImage() {

        // Asset sent over from the service...

        Bundle ex = getIntent().getExtras();

        if (ex != null) {

            ((TextView)findViewById(R.id.asset_text)).setText("Assests Found!");

            Bitmap assetBitmap = ex.getParcelable("watchAsset");

            ImageView imageView = (ImageView) findViewById(R.id.asset_image);
            imageView.setImageBitmap(assetBitmap);

        } else {

            ((TextView)findViewById(R.id.asset_text)).setText("No Assests Found...");

        }

    }

    private void setAssetText(String message) {

        ((TextView)findViewById(R.id.asset_text)).setText(message);

    }

    private void setPeerStatusMessage(String message) {

        ((TextView)findViewById(R.id.peer_status_message)).setText(message);

    }

    private void lastUpdated() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"+ cal.get(Calendar.SECOND);

        ((TextView) findViewById(R.id.last_updated)).setText(time);

    }

    private void registerMessageReceivers() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WearableBroadcastMessages.MOBILE_DATA_RECEIVED);
        intentFilter.addAction(WearableBroadcastMessages.PEER_CONNECTED);
        intentFilter.addAction(WearableBroadcastMessages.PEER_DISCONNECTED);

        registerReceiver(wearableMessageReceiver, intentFilter);

    }

    private final BroadcastReceiver wearableMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            lastUpdated();

            if (WearableBroadcastMessages.MOBILE_DATA_RECEIVED.equals(action)) {

                String message = intent.getStringExtra("STRING_MESSAGE");
                setAssetText(message);

            } else if (WearableBroadcastMessages.PEER_CONNECTED.equals(action)) {

                setPeerStatusMessage("Connected!");

            } else if (WearableBroadcastMessages.PEER_DISCONNECTED.equals(action)) {

                setPeerStatusMessage("Disconnected");

            }

        }
    };
}
