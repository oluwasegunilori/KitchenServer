package com.example.changeme.kitchenserver.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.changeme.kitchenserver.Model.Request;
import com.example.changeme.kitchenserver.Model.User;
import com.example.changeme.kitchenserver.Remote.APIService;
import com.example.changeme.kitchenserver.Remote.FcmRetrofitClient;
import com.example.changeme.kitchenserver.Remote.IGeoCoordinates;
import com.example.changeme.kitchenserver.Remote.RetrofitClient;

/**
 * Created by SHEGZ on 1/3/2018.
 */
public class Common {
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String ADDTOBANNER = "Add to Banner";
    public static final int PICK_IMAGE_REQUEST = 71;
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";
    private static final String BASE_URL = "https://maps.googleapis.com";
    private static final String FCM_URL = "https://fcm.googleapis.com/";
    public static User currentuser;
    public static Request currentRequest;
    public static String topic_name = "News";

    public static String convertCodeToStatus(String code) {
        if (code.equals("0")) {
            return "placed";
        } else if (code.equals("1")) {
            return "on my way";
        } else {
            return "delivered";
        }
    }

    public static APIService getFCMClient() {
        return FcmRetrofitClient.getClient(FCM_URL).create(APIService.class);

    }

    public static IGeoCoordinates getGeoCodeService() {

        return RetrofitClient.getClient(BASE_URL).create(IGeoCoordinates.class);

    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_4444);
        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

