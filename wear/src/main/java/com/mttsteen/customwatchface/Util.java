package com.mttsteen.customwatchface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by mattst on 8/8/14.
 */
public class Util {

    public static String TAG = "Matt Log";
    public static String MAP_REQUEST_PATH = "/watchface";
    public static String WATCH_FACE_ON = "watchFaceOn";
    public static String WATCH_FACE_OFF = "watchFaceOff";
    public static String WATCH_FACE_TITLE = "watchFaceTitle";

    public static byte[] bitmapToByteArray(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();

    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

    }
}
