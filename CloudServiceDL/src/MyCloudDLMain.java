import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyCloudDLMain
{
  public static void main(final String[] args)
  {
    final ConcurrentLinkedQueue<String> bQueue = new ConcurrentLinkedQueue<>();
    List<MyCloudTLServerDL> myCloudTLServersDL = new ArrayList<>();

    int nDevices = (args.length - 1) / 2;
    String ipAddr = null;
    int easPort;
    int wsmpPort;

    // System.out.println("Working Directory = " + System.getProperty("user.dir"));
    final MyCloudHttpServer myCloudHttpServer = new MyCloudHttpServer(args[0], bQueue);

    myCloudHttpServer.Run();

    for (int i = 0; i < nDevices; i++)
    {
      ipAddr = args[(2 * i) + 1];
      easPort = Integer.parseInt(args[(2 * i) + 2]);
      wsmpPort = 12;

      try
      {
        // if (InetAddress.getByName(ipAddr).isReachable(1000))
        // if (true)
        // {
        myCloudTLServersDL.add(new MyCloudTLServerDL(bQueue, ipAddr, easPort, wsmpPort));
        // }
        // else
        // {
        // (new MyLogger()).log("IP: " + ipAddr + " is not reachable!", "ERROR");
        // }
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    for (MyCloudTLServerDL myCloudTLServerDL : myCloudTLServersDL)
    {
      new Thread(myCloudTLServerDL).start();
    }

  }

}
