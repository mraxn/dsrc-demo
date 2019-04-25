package com.example.mraxn.obuapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


class DLClientAsyncTask extends AsyncTask<String, String, String>
 {
   private MainActivity mActivity;
   private DLClient dlClient;
   private ConcurrentLinkedQueue<String> bQueue  = null;


   DLClientAsyncTask(MainActivity mainActivity, String hostAddr)
   {
     mActivity = mainActivity;
     bQueue = new ConcurrentLinkedQueue<>();
     dlClient = new DLClient(hostAddr, bQueue);
   }

   @Override
   protected String doInBackground(final String... params)
   {
     while (!this.isCancelled())
     {
       if(!dlClient.isConnected)
       {
         dlClient.Connect();
       }
       else
       {

         if (!bQueue.isEmpty())
         {
           dlClient.SendRsp2Srv();
           publishProgress(bQueue.remove());
         }

         try
         {
           TimeUnit.SECONDS.sleep(1);
           // TimeUnit.MILLISECONDS
         }
         catch (final InterruptedException e)
         {
           Log.e("DL", "Can't sleep!");
         }
       }
     }
     return "";
   }

   @Override
   protected void onCancelled()
   {
     this.cancel(false);
     dlClient.Close();
     Log.i("DL", "Closed WSMP connection!");
   }

   @Override
   protected void onProgressUpdate(String... params)
   {
     LinearLayout linearLayout = mActivity.findViewById(R.id.info_main_ll);
     LinearLayout pd_ll = mActivity.findViewById(R.id.pd_main_linear_layout);
     LayoutInflater inflater = LayoutInflater.from(mActivity.getApplicationContext());
     View inflatedLayout = inflater.inflate(R.layout.info_card, linearLayout, false);
     ((TextView) inflatedLayout.findViewById(R.id.infoText)).setText(params[0]);
     ((TextView) inflatedLayout.findViewById(R.id.infoText)).setTextColor(Color.BLACK);
     (inflatedLayout.findViewById(R.id.info_card)).setBackground(pd_ll.getChildAt(0).getBackground());
     linearLayout.addView(inflatedLayout, 0);
     AcmeNotifications.SendNotification(mActivity, params[0]);
     AcmeNotifications.SendToast(mActivity, params[0]);
   }
 }
