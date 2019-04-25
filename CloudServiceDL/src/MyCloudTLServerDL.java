
// 4GIT
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.commsignia.v2x.client.ITSApplication;
import com.commsignia.v2x.client.ITSConnectionStateListener;
import com.commsignia.v2x.client.ITSEventAdapter;
import com.commsignia.v2x.client.exception.ClientException;
import com.commsignia.v2x.client.model.WsmpNotification;
import com.commsignia.v2x.client.model.WsmpSendData;
import com.commsignia.v2x.client.model.dev.FacilityModule;
import com.commsignia.v2x.client.msgset.d.MessageSetD;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyCloudTLServerDL extends Thread
{

  String ulDataStr = "";
  // String clientAlertStr = null;
  private final ConcurrentLinkedQueue<String> bQueue;
  boolean                                     canSend        = false;
  ITSApplication                              itsApplication = null;
  MyLogger                                  myLogger;
  // boolean isReceivedFromClient = false;
  String  ipAddr = null;
  int     wsmpPort;
  int     easPort;
  boolean isRegistered;
  boolean isConnected;

  public MyCloudTLServerDL(final ConcurrentLinkedQueue<String> bQueue, String ipAddr, int easPort, int wsmpPort)
  {
    this.bQueue = bQueue;
    this.ipAddr = ipAddr;
    // clientAlertStr = null;
    this.wsmpPort = wsmpPort;
    this.easPort = easPort;
    isConnected = false;
    isRegistered = false;

    itsApplication = new ITSApplication(wsmpPort, ipAddr, easPort, new MessageSetD());

    // itsApplication = new ITSApplication(
    // new ITSConnectionParameters.Builder<>(wsmpPort, easIpAddr, wsmpPort, new
    // MessageSetD())
    // .withConnectionTimeout(10000).build());

    itsApplication.addEventListener(new ITSEventAdapter()
    {
      @Override
      public void onWsmpNotification(WsmpNotification notification)
      {
        ByteBuffer buffer = ByteBuffer.wrap(notification.getData());
        ulDataStr = new String(buffer.array(), 0, buffer.array().length);

        if (ulDataStr.startsWith("Received"))
        {
          bQueue.remove();

          myLogger.log("Removed from BQUEUE", "INFO");
          myLogger.log("BQUEUE size: " + bQueue.size(), "INFO");
          myLogger.log("RX: " + ipAddr + ":" + easPort + ":" + wsmpPort + " - Alert received!", "INFO");
        }
      }
    });

    itsApplication.addConnectionStateListener(new ITSConnectionStateListener()
    {

      @Override
      public void onPreConnect()
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void onFrameworkReady()
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void onConnected()
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void onDisconnected()
      {
        myLogger.log("Disconnected from RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort + "!", "ERROR");
        isConnected = false;
        Close();
      }

      @Override
      public void onFrameworkError(Throwable cause)
      {
        // isConnected = false;
        // myLogger.log("Framework error in RSU: " + easIpAddr + ":" + wsmpPort + "!",
        // "ERROR");
      }

    });

    myLogger = new MyLogger();

  }

  @Override
  public void run()
  {
    while (true)
    {
      if (!isConnected)
      {
        Connect();
        Register();
      }
      else
      {

        try
        {
          TimeUnit.SECONDS.sleep(3);
          // TimeUnit.MILLISECONDS
        }
        catch (final InterruptedException e)
        {
          myLogger.log("Can't sleep!", "ERROR");
        }

        if (!bQueue.isEmpty())
        {

          if (!ReachedTimeout(bQueue.peek()))
          {
            SendAlert2OBUApp();
          }
          else
          {
            try
            {
              bQueue.remove();
            }
            catch (Exception e)
            {

            }
          }
        }
      }
    }

  }

  public void Connect()
  {
    try
    {
      myLogger.log("Connecting to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");

      itsApplication.connect();

      myLogger.log("Connected to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
    }
    catch (final TimeoutException | InterruptedException ex)
    {
      isConnected = false;
      myLogger.log("Can't connect to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort + "!", "ERROR");
    }

  }

  private void Register()
  {
    try
    {
      myLogger.log("Registering to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
      itsApplication.commands().registerBlocking();
      itsApplication.commands().setFacilityModuleStatus(FacilityModule.CAM, false);
      itsApplication.commands().wsmpBindBlocking(wsmpPort);
      myLogger.log("Registered to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
      isConnected = true;
    }
    catch (final ClientException ex)
    {
      isConnected = false;
      myLogger.log("Can't register to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort + "!", "ERROR");
      Close();
    }
  }

  private boolean ReachedTimeout(String jsonStr)
  {

    boolean res = false;
    JsonNode rcvdJson = null;

    try
    {
      rcvdJson = (new ObjectMapper()).readTree(jsonStr);
    }
    catch (JsonProcessingException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    long timeStamp = Long.parseLong(rcvdJson.get("AlertDetails").get("Timestamp").textValue());
    long currTime = System.currentTimeMillis();
    long tsDiff = currTime - timeStamp;

    Date date = new Date(timeStamp);
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    String formatted = format.format(date);
    myLogger.log("Client timestamp: " + formatted, "TRACE");

    if (tsDiff > 300000)
    {
      res = true;
      bQueue.remove();
      myLogger.log("Received message timout!", "INFO");
      myLogger.log("Removed from BQUEUE without sending...", "INFO");
      myLogger.log("BQUEUE size: " + bQueue.size(), "INFO");
    }
    else
    {
      res = false;
    }

    return res;

  }

  private void Close()
  {
    try
    {
      myLogger.log("Closing connection to RSU: " + ipAddr + ":" + wsmpPort, "INFO");
      itsApplication.commands().wsmpCloseBlocking(wsmpPort);
      itsApplication.commands().deregisterBlocking();
      itsApplication.shutdown();
    }
    catch (final ClientException e)
    {
      myLogger.log("Can't close!", "ERROR");
    }
  }

  private void SendAlert2OBUApp()
  {
    try
    {
      String clientAlertStr = bQueue.peek();

      if ((clientAlertStr != null) && !clientAlertStr.equals(""))
      {
        final ByteBuffer alert2OBUAppData = ByteBuffer.wrap(clientAlertStr.getBytes());

        myLogger.log("TX: Trying to send alert to OBU...", "INFO");
        itsApplication.commands().wsmpSendBlocking(new WsmpSendData.Builder().withLLAddress("70:B3:D5:8C:D7:20")
            .withPortNumber(12).withData(alert2OBUAppData.array()).withTxPowerDbm(20).build());

      }
    }
    catch (final ClientException e)
    {
      myLogger.log("Can't write WSMP data!", "ERROR");
      myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
    }

  }

}
