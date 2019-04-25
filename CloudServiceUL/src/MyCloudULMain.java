
// 4GIT
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: RF - MyCloudUL - Separate HTTP & TCP to different classes

public class MyCloudULMain
{

  public static void main(final String[] args) throws IOException
  {
    int nDevices = args.length / 2;
    SharedRes sharedRes = new SharedRes();
    String ipAddr = null;
    int easPort;
    int wsmpPort;

    final MyCloudHttpClient myCloudHttpClient = new MyCloudHttpClient(sharedRes);
    List<MyCloudUL> myCloudServersUL = new ArrayList<>();

    myCloudHttpClient.start();

    for (int i = 0; i < nDevices; i++)
    {

      ipAddr = args[2 * i];
      easPort = Integer.parseInt(args[(2 * i) + 1]);
      // wsmpPort = i + 11;
      wsmpPort = 11;

      try
      {
        myCloudServersUL.add(new MyCloudUL(ipAddr, easPort, wsmpPort, sharedRes));
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    for (MyCloudUL myCloudUL : myCloudServersUL)
    {
      new Thread(myCloudUL).start();
    }
  }
}

class SharedRes
{
  private int                          currentRsu      = 0;
  private boolean                      reconnectSignal = false;
  public ConcurrentLinkedQueue<String> bQueue;

  public SharedRes()
  {
    bQueue = new ConcurrentLinkedQueue<>();
  }

  public synchronized int GetCurrentRsu()
  {
    return currentRsu;
  }

  public synchronized void SetCurrentRsu(int currentRsu)
  {
    this.currentRsu = currentRsu;
  }

  public synchronized boolean GetReconnect()
  {
    return reconnectSignal;
  }

  public synchronized void SetReconnect(boolean reconnect)
  {
    this.reconnectSignal = reconnect;
  }
}
