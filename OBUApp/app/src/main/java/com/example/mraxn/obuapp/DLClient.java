// 4GIT
package com.example.mraxn.obuapp;

import android.util.Log;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.commsignia.v2x.client.ITSApplication;
import com.commsignia.v2x.client.ITSEventAdapter;
import com.commsignia.v2x.client.exception.ClientException;
import com.commsignia.v2x.client.model.WsmpNotification;
import com.commsignia.v2x.client.model.WsmpSendData;
import com.commsignia.v2x.client.model.dev.FacilityModule;
import com.commsignia.v2x.client.msgset.d.MessageSetD;

class DLClient
{

  private ITSApplication itsApplication = null;

  private final String hostAddr;
  boolean isConnected = false;
  private int wsmpPort = 12;
  private ConcurrentLinkedQueue<String> cQueue;
  public int prevVid = 0;
  public int vid = 0;

  DLClient(String hostAddr, ConcurrentLinkedQueue<String> bQueue)
  {
    this.hostAddr = hostAddr;
    this.cQueue = bQueue;

    itsApplication = new ITSApplication(6, hostAddr, 7942, new MessageSetD());

    itsApplication.addEventListener(new ITSEventAdapter()
    {
      @Override
      public void onWsmpNotification(WsmpNotification notification)
      {
        ByteBuffer rcvBytes = ByteBuffer.wrap(notification.getData());
        String rxJsonStr = new String(rcvBytes.array());

        try
        {
          String rxJsonStrProc = ProcessAlert(rxJsonStr);

          if (!cQueue.contains(rxJsonStrProc) && (prevVid != vid))
          {
            cQueue.add(rxJsonStrProc);
          }
        }
        catch (Exception e)
        {
          Log.e("DL", "BQUEUE concurrency error!");
        }
      }
    });
  }

  void Connect()
  {
    try
    {
      Log.i("DL", "Connecting to: " + hostAddr + ", port: " + 7942 + "...");
      itsApplication.connect();
      Log.i("DL", "Connected to: " + hostAddr + ", port: " + 7942 + "...");
    }
    catch (Exception e)
    {
      isConnected = false;
      Log.e("DL", "Can't connect to: " + hostAddr + ", port: " + 7942 + "...");
//      e.printStackTrace();
    }
    try
     {
       Log.i("DL", "Registering on port port: " + wsmpPort + "...");
       itsApplication.commands().registerBlocking();
       itsApplication.commands().setFacilityModuleStatus(FacilityModule.CAM, false);
       itsApplication.commands().wsmpBindBlocking(wsmpPort);
       isConnected = true;

       Log.i("DL", "WSMP: Registered connection on port " + wsmpPort + "...");
     }
     catch (final ClientException e)
     {
       isConnected = false;

       Log.e("DL", "Can't register / bind blocking!");
//       ex.printStackTrace();
       Close();
       Connect();
     }
  }

//  public String RcvFromSrv()
//  {
//    return rxStr;
//  }

  private String ProcessAlert(String rxStr2Json)
  {
    String jsonStr = null;

    try
    {
      jsonStr = URLDecoder.decode(rxStr2Json, "UTF-8");
    }
    catch (Exception e)
    {
      Log.e("DL", "Can't URL decode alert!");
    }

    try
    {
      JSONObject rcvdJson = new JSONObject(jsonStr);
//      String amikaAlert = rcvdJson.getJSONObject("AlertDetails").getString("FullMessage");
      String amikaAlert = rcvdJson.getJSONObject("AlertDetails").getString("Subject");
      String timeStamp = rcvdJson.getJSONObject("AlertDetails").getString("Timestamp");
      java.util.Date dateTime = new java.util.Date(Long.parseLong(timeStamp));
//      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a z", Locale.CANADA);
      SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.CANADA);
      vid = rcvdJson.getJSONObject("TargetingDetails").getInt("VIDs");

      if (prevVid != vid)
      {
        String tstamp = dateFormat.format(dateTime);
        jsonStr = "[" + tstamp + "]   Vehicle " + vid + " " + amikaAlert;
        prevVid = vid;
      }
    }
    catch (Exception e)
    {
      Log.e("DL", "Can't convert to JSON object: " + jsonStr);
      jsonStr = null;
    }

    return jsonStr;

  }

  void SendRsp2Srv()
  {
      String s = cQueue.peek();
      s.substring(s.indexOf("[")+1, s.indexOf("]"));

    final ByteBuffer str2send = ByteBuffer.wrap(("Received:" + s).getBytes());

    try
    {
      itsApplication.commands().wsmpSendBlocking(new WsmpSendData.Builder().withLLAddress("ff:ff:ff:ff:ff:ff")
          .withPortNumber(wsmpPort).withData(str2send.array()).withTxPowerDbm(20).build());
      Log.i("DL", "WSMP: Msg sent!");
    }
    catch (final ClientException e)
    {
      Log.e("DL", "WSMP: Can't sent!");
      Close();
      Connect();
    }
  }

  void Close()
  {
    try
    {
      Log.i("DL", "WSMP: Closing...");
      itsApplication.commands().wsmpCloseBlocking(wsmpPort);
      itsApplication.commands().deregisterBlocking();
      itsApplication.shutdown();
    }
    catch (final ClientException e)
    {
      Log.e("DL", "WSMP: Can't close /deregister !");
    }
  }

}
