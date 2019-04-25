
// 4GIT
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

public class MyCloudHttpClient extends Thread
{
  MyLogger myLogger;
  // HttpClient httpClient;
  // HttpPost httpPost;
  // HttpResponse clientSrvResponce = null;
  String            clientRespStr = new String();
  final String      clientURL     = "";
  String            ulDataStr    = "";
  JSONObject        ulJson;
  private SharedRes sharedRes;
  // boolean isSent2client = false;

  public MyCloudHttpClient(SharedRes sharedRes)
  {
    myLogger = new MyLogger();
    this.sharedRes = sharedRes;
  }

  @Override
  public void run()
  {
    // Connect2client();

    while (true)
    {
      if (sharedRes.bQueue.peek() != null)
      {
        boolean res;

        myLogger.log("Before sending new message to client!", "INFO");
        res = (Send2client());

        if (res)
        {
          myLogger.log("Sent To client", "Info");
          sharedRes.bQueue.remove();
        }

      }

      try
      {
        TimeUnit.MILLISECONDS.sleep(500);
        // TimeUnit.MILLISECONDS
      }
      catch (final InterruptedException e)
      {
        myLogger.log("Can't sleep!", "ERROR");
        myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
      }
    }

  }

  private boolean Send2client()
  {
    int timeout = 15;
    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
        .setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();

    CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    HttpPost httpPost = new HttpPost(clientURL);
    httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:55.0) Gecko/20100101 Firefox/55.0");
    httpPost.addHeader("Accept", "*/*");
    httpPost.addHeader("Accept-Language", "en-US,en;q=0.5");
    myLogger.log("UL: HTTP: Connected to client!", "INFO");

    CloseableHttpResponse clientSrvResponce = null;

    try
    {
      ulDataStr = URLEncoder.encode(sharedRes.bQueue.peek(), "UTF-8");

      final StringEntity myEntity = new StringEntity(ulDataStr,
          ContentType.create("application/x-www-form-urlencoded"));
      httpPost.setEntity(myEntity);

      clientSrvResponce = httpClient.execute(httpPost);

      clientSrvResponce.close();
      httpClient.close();

      return true;
    }
    catch (Exception ex)
    {
      if (clientSrvResponce != null) // FIXME: Why != null, what if it is null?
      {
        try
        {
          clientSrvResponce.close();
          httpClient.close();
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      return false;
    }
  }

}