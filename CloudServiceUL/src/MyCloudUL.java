
// 4GIT
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.commsignia.v2x.client.ITSApplication;
import com.commsignia.v2x.client.ITSConnectionStateListener;
import com.commsignia.v2x.client.ITSEventAdapter;
import com.commsignia.v2x.client.model.WsmpNotification;
import com.commsignia.v2x.client.model.dev.FacilityModule;
import com.commsignia.v2x.client.msgset.d.MessageSetD;

public class MyCloudUL extends Thread
{

  MyLogger myLogger;
  JSONObject ulJson;
  Polygon2D  d1;
  Polygon2D  d2;
  Polygon2D  d3;
  boolean[]  isSent =
  { false, false, false };

  List<ITSApplication> ITSApplications = null;
  ITSApplication       itsApplication  = null;
  int                  wsmpPort;
  int                  iRsu            = 0;
  String               ipAddr;
  int                  easPort;
  boolean              isConnected;

  private SharedRes sharedRes;

  public MyCloudUL(String ipAddr, int easPort, int wsmpPort, SharedRes sharedRes)
  {
    this.ipAddr = ipAddr;
    this.sharedRes = sharedRes;
    this.wsmpPort = wsmpPort;
    this.easPort = easPort;
    isConnected = false;

    d1 = new Polygon2D();
    d2 = new Polygon2D();
    d3 = new Polygon2D();

    d1 = Polygon2D.SetPolygon("cdata1.txt");
    d2 = Polygon2D.SetPolygon("cdata2.txt");
    d3 = Polygon2D.SetPolygon("cdata3.txt");

    myLogger = new MyLogger();

    Init();
  }

  public void Init()
  {
    itsApplication = new ITSApplication(5, ipAddr, easPort, new MessageSetD());
    itsApplication.addEventListener(new ITSEventAdapter()
    {
      @Override
      public void onWsmpNotification(WsmpNotification notification)
      {

        System.out.println(ipAddr + ":" + ", RSSI:" + notification.getRssi());
        ByteBuffer rcvBytes = ByteBuffer.wrap(notification.getData());

        try
        {
          ProcessMessage(new String(rcvBytes.array()));
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          myLogger.log("Process Message Error", "ERROR");
        }

      }
    });

    itsApplication.addConnectionStateListener(new ITSConnectionStateListener()
    {
      @Override
      public void onPreConnect()
      {
      }

      @Override
      public void onConnected()
      {
      }

      @Override
      public void onDisconnected()
      {
        myLogger.log("Disconnected from RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort + "!", "ERROR");
        isConnected = false;
        Close();
      }

      @Override
      public void onFrameworkReady()
      {
      }

      @Override
      public void onFrameworkError(Throwable cause)
      {
      }
    });
  }

  @Override
  public void run()
  {
    // while (true)
    // {
    Connect();
    Register();
    RunServer();

  }

  public void Connect()
  {
    try
    {
      myLogger.log("Connecting to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
      itsApplication.connect();
      myLogger.log("Connected to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
    }
    catch (final Exception ex)
    {
      isConnected = false;
      myLogger.log("Can't connect to " + ipAddr + ":" + easPort + ":" + wsmpPort, "ERROR");
    }

  }

  private void Close()
  {
    try
    {
      myLogger.log("Closing connection to RSU: " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
      itsApplication.commands().wsmpCloseBlocking(wsmpPort);
      itsApplication.commands().deregisterBlocking();
      itsApplication.shutdown();
    }
    catch (final Exception e)
    {
      myLogger.log("Can't close!", "ERROR");
      isConnected = false;
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
      myLogger.log("WSMP: Registered connection on " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
      isConnected = true;
    }
    catch (final Exception ex)
    {
      isConnected = false;
      myLogger.log("WSMP: Can't register / bind blocking on " + ipAddr + ":" + easPort + ":" + wsmpPort, "ERROR");
      Close();
    }
  }

  private void ProcessMessage(String ulDataStr) throws Exception
  {
    // Convert to JSON object:
    ulJson = new JSONObject(ulDataStr);

    // Add RSU event timestamp:
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss", Locale.CANADA);
    final String tstamp = dateFormat.format(new Date());
    ulJson.getJSONObject("EventDetails").put("Timestamp", tstamp);

    final double lat = Double.parseDouble(ulJson.getJSONObject("EventDetails").getString("RSU_LAT"));
    final double lon = Double.parseDouble(ulJson.getJSONObject("EventDetails").getString("RSU_LON"));

    myLogger.log(
        "Received message via " + ipAddr + ":" + easPort + ":" + wsmpPort + ", with LON: " + lon + ", LAT: " + lat,
        "TRACE");

    // Check if OBU is inside the polygon:
    if (d3.contains(lon, lat) && (sharedRes.GetCurrentRsu() != 3))
    {
      ulJson.getJSONObject("EventDetails").put("RSUID", 3);
      sharedRes.bQueue.add(ulJson.toString());
      sharedRes.SetCurrentRsu(3);

      myLogger.log("Connected to RSU #3 via " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
    }
    else if (d2.contains(lon, lat) && (sharedRes.GetCurrentRsu() != 2))
    {
      ulJson.getJSONObject("EventDetails").put("RSUID", 2);
      sharedRes.bQueue.add(ulJson.toString());
      sharedRes.SetCurrentRsu(2);

      myLogger.log("Connected to RSU #2 via " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
    }
    else if (d1.contains(lon, lat) && (sharedRes.GetCurrentRsu() != 1))
    {
      ulJson.getJSONObject("EventDetails").put("RSUID", 1);
      sharedRes.bQueue.add(ulJson.toString());
      sharedRes.SetCurrentRsu(1);

      myLogger.log("Connected to RSU #1 via " + ipAddr + ":" + easPort + ":" + wsmpPort, "INFO");
    }
    else
    {
      // myLogger.log("Already sent or connected to same RSU via port " + wsmpPort,
      // "INFO");
    }

  }

  private void RunServer()
  {
    while (true)
    {

      if (!isConnected)
      {
        Connect();
        Register();
      }

      try
      {
        TimeUnit.SECONDS.sleep(1);
        // TimeUnit.MILLISECONDS
      }
      catch (final InterruptedException e)
      {
        myLogger.log("Can't sleep!", "ERROR");
      }

    }
  }

}
