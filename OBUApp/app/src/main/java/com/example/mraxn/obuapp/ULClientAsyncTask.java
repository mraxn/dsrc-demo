// 4GIT
package com.example.mraxn.obuapp;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.TimeUnit;

class ULClientAsyncTask extends AsyncTask<String, Void, String>
 {
   private MainActivity mActivity;
   private ULClient ulClient;

   public ULClientAsyncTask(MainActivity mainActivity, String hostAddr)
   {
     mActivity = mainActivity;
     ulClient = new ULClient(hostAddr);
   }


   @Override
   protected String doInBackground(final String... params)
   {
     while (!this.isCancelled())
     {
       if(!ulClient.isConnected)
       {
         ulClient.Connect();
       }
       else
       {
         ulClient.Send2Srv(mActivity.txJson.toString());
         //       ulClient.RcvFromSrv();
         try
         {
           TimeUnit.SECONDS.sleep(1);
           // TimeUnit.MILLISECONDS
         }
         catch (final InterruptedException e)
         {
           Log.e("UL", "Can't sleep!");
         }
       }
     }
     return "";
   }

   protected void onCancelled()
   {
     this.cancel(false);
     ulClient.Close();
     Log.i("UL", "Closed WSMP connection!");
   }
 }
