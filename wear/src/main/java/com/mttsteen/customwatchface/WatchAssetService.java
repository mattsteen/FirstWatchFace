package com.mttsteen.customwatchface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mattst on 8/8/14.
 */
public class WatchAssetService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;
    private Handler mStatusUpdateHandler;
    private final int STATUS_UPDATE_TIME = 3000;
    private boolean peerConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();

        connectGoogleApiClient();
        startStatusUpdates();
        broadcastStringUpdate(WearableBroadcastMessages.MOBILE_DATA_RECEIVED, "Testing the Receiver...");
        Log.v(Util.TAG, "Starting, testing the Receiver...");
    }

    @Override
    public void onDestroy() {

        stopStatusUpdates();
        disconnectGoogleApiClient();
        super.onDestroy();

    }

    @Override
    public void onPeerConnected(Node peer) {

        super.onPeerConnected(peer);
        Log.v(Util.TAG, "Peer Connected!");

        peerConnected = true;

    }

    @Override
    public void onPeerDisconnected(Node peer) {

        super.onPeerDisconnected(peer);

        peerConnected = false;

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {


        Log.v(Util.TAG, "onDataChanged Triggered!");

        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(Util.TAG, "Failed to connect to GoogleApiClient.");
            return;
        }


        for (DataEvent event : events) {

            if (event.getType() == DataEvent.TYPE_CHANGED) {

                String path = event.getDataItem().getUri().getPath();

                if (Util.MAP_REQUEST_PATH.equals(path)) {

                    // Get the data out of the event
                    DataMapItem dataMapItem =
                            DataMapItem.fromDataItem(event.getDataItem());
                    final String title = dataMapItem.getDataMap().getString(Util.WATCH_FACE_TITLE);
                    //Asset asset = dataMapItem.getDataMap().getAsset(Util.WATCH_FACE_ON);

                    broadcastStringUpdate(WearableBroadcastMessages.MOBILE_DATA_RECEIVED, title);

                    //sendAssetToActivity(asset);

                    // Build the intent to display our custom notification
                    /*Intent notificationIntent =
                            new Intent(this, NotificationActivity.class);
                    notificationIntent.putExtra(
                            NotificationActivity.EXTRA_TITLE, title);
                    notificationIntent.putExtra(
                            NotificationActivity.EXTRA_IMAGE, asset);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create the ongoing notification
                    Notification.Builder notificationBuilder =
                            new Notification.Builder(this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(
                                            getResources(), R.drawable.ic_launcher))
                                    .setOngoing(true)
                                    .extend(new Notification.WearableExtender()
                                            .setDisplayIntent(notificationPendingIntent));

                    // Build the notification and show it
                    NotificationManager notificationManager =
                            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(
                            NOTIFICATION_ID, notificationBuilder.build());
                    */

                } else {
                    Log.d(Util.TAG, "Unrecognized path: " + path);
                }
            }
        }
    }

    private void connectGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();


    }

    private void disconnectGoogleApiClient() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    private void sendAssetToActivity(Asset asset) {

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {

            Log.v(Util.TAG, "Requested an unknown Asset.");

        } else {

            Log.v(Util.TAG, "Sending Asset to Activity!");

            Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);

            Intent intent = new Intent(this, CustomWatchFaceActivity.class);
            intent.putExtra("watchAsset", Util.bitmapToByteArray(bitmap));

            startActivity(intent);

        }

    }

    private void startStatusUpdates() {

        mStatusUpdateHandler = new Handler();
        mStatusUpdateHandler.postDelayed(new Runnable() {

            @Override
            public void run() {

               // Log.v(Util.TAG, "Status update...");
                updateStatus();
                mStatusUpdateHandler.postDelayed(this, STATUS_UPDATE_TIME);

            }

        }, STATUS_UPDATE_TIME);

    }

    private void stopStatusUpdates() {

        mStatusUpdateHandler.removeCallbacksAndMessages(null);

    }

    private void updateStatus() {

        if (peerConnected) {
            broadcastUpdate(WearableBroadcastMessages.PEER_CONNECTED);
        } else {
            broadcastUpdate(WearableBroadcastMessages.PEER_DISCONNECTED);
        }

    }

    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    private void broadcastStringUpdate(final String action, String stringMessage) {

        final Intent intent = new Intent(action);
        intent.putExtra("STRING_MESSAGE", stringMessage);

        sendBroadcast(intent);

    }

}
