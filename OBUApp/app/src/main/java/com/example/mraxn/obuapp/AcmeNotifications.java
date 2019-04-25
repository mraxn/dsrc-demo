package com.example.mraxn.obuapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mraxn on 7/1/2018.
 */

public class AcmeNotifications
{
  static int alertId = 0;

  static public void SendToast(MainActivity mActivity, String alert)
  {
    LayoutInflater inflater = LayoutInflater.from(mActivity.getApplicationContext());
    View layout = inflater.inflate(R.layout.amika_alert_toast, mActivity.findViewById(R.id.amika_alert_toast));

    TextView text = layout.findViewById(R.id.toast_alert_tv);
    text.setText(alert);

    Toast toast = new Toast(mActivity.getApplicationContext());
    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    toast.setDuration(Toast.LENGTH_LONG);
    toast.setView(layout);
    toast.show();
  }

  static public void SendNotification(MainActivity mActivity, String alert)
{
  NotificationCompat.Builder mBuilder =
     new NotificationCompat.Builder(mActivity, Integer.toString(alertId));

 Intent intent = new Intent(mActivity, mActivity.getClass());
 PendingIntent pendingIntent = PendingIntent.getActivity(mActivity, 0, intent, 0);
 mBuilder.setContentIntent(pendingIntent);

 mBuilder.setSmallIcon(R.drawable.ic_info_outline_white_36dp);
 mBuilder.setContentTitle("Alert");
 mBuilder.setContentText(alert);

 NotificationManager mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
 mNotificationManager.notify(alertId, mBuilder.build());

  alertId++;

}


}
