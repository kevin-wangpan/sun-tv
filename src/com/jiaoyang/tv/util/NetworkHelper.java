package com.jiaoyang.tv.util;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.jiaoyang.video.tv.R;

public class NetworkHelper {

    public static boolean isWifiAvailable(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifiInfo.isConnectedOrConnecting();
    }

    public static boolean isMobileNetwork(Context context) {
        boolean result = false;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                result = networkInfo.isConnectedOrConnecting();
            }
        }

        return result;
    }
    public static boolean hasAvailableNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork == null ? false : true;
    }


    public static void showNotWifiNotic(final Context context) {
        AlertDialog mSetNetDialog = new AlertDialog.Builder(context).setTitle(R.string.tip)
                .setMessage(R.string.tops_no_network)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                            context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).create();
        mSetNetDialog.show();
    }

}
