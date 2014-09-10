package com.mttsteen.customwatchface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private Handler statusTimeoutHandler;
    private PendingResult<DataApi.DataItemResult> pendingResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectGoogleApiClient();

    }

    @Override
    public void onDestroy() {

        disconnectGoogleApiClient();

        super.onDestroy();

    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.v(Util.TAG, "onConnected in Activity...");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void sendButtonImageToWatch(View view) {

        sendDrawableToWatch(R.drawable.pac_man_face_on);

    }

    private void sendDrawableToWatch(int drawableResId) {

        sendAssetToWatch(
                createAssetFromBitmap(
                        createBitmapFromDrawable(drawableResId))
        );

    }

    private Bitmap createBitmapFromDrawable(int drawableResId) {

        return BitmapFactory.decodeResource(getResources(), drawableResId);

    }

    private Asset createAssetFromBitmap(Bitmap bitmap) {

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());

    }

    private void sendAssetToWatch(Asset asset) {

        loadBitmapFromAsset(this, asset);

        /*PutDataMapRequest dataMap = PutDataMapRequest.create(Util.MAP_REQUEST_PATH);
        dataMap.getDataMap().putAsset(Util.WATCH_FACE_ON, asset);
        dataMap.getDataMap().putString(Util.WATCH_FACE_TITLE, "Watch Face On");

        PutDataRequest request = dataMap.asPutDataRequest();
        pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);

        ((TextView)findViewById(R.id.watch_comm_status)).setText("Sending Asset...");

        statusTimeoutHandler = new Handler();
        statusTimeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.watch_comm_status)).setText("--");

            }
        }, 3000);*/

    }

    private void connectGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(Util.TAG, "onConnected: " + connectionHint);
                        // Now you can use the data layer API

                        ((TextView)findViewById(R.id.watch_connection_status)).setText("Connected!");
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(Util.TAG, "onConnectionSuspended: " + cause);

                        ((TextView)findViewById(R.id.watch_connection_status)).setText("Suspended");
                    }

                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(Util.TAG, "onConnectionFailed: " + result);

                        ((TextView)findViewById(R.id.watch_connection_status)).setText("Failed");
                    }

                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

    }

    private void disconnectGoogleApiClient() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    public static void loadBitmapFromAsset(final Context context, final Asset asset) {

        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        new AsyncTask<Asset, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Asset... assets) {

                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(Wearable.API)
                        .build();

                ConnectionResult result =
                        googleApiClient.blockingConnect(
                                1000, TimeUnit.MILLISECONDS);

                if (!result.isSuccess()) {
                    return null;
                }

                // convert asset into a file descriptor and block until it's ready
                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        googleApiClient, assets[0]).await().getInputStream();
                googleApiClient.disconnect();

                if (assetInputStream == null) {
                    Log.w(Util.TAG, "Requested an unknown Asset.");
                    return null;
                }

                // decode the stream into a bitmap
                return BitmapFactory.decodeStream(assetInputStream);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    //target.setImageBitmap(bitmap);
                }
            }

        }.execute(asset);
    }


}
