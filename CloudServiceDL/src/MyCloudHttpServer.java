import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.sun.net.httpserver.Headers;
// import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// @SuppressWarnings("restriction")
public class MyCloudHttpServer
{

  HttpServer    httpServer    = null;
  String        rcvDataString = null;
  String        msgUuid       = null;
  String        responseMsg   = null;
  int           responceCode  = 0;
  JsonSchema    jsonSchema    = null;
  String        clientRxSchema = null;
  JsonValidator jsonValidator = null;
  MyLogger    myLogger    = null;
  String        portNumber    = null;
  boolean       isValid       = true;

  private final ConcurrentLinkedQueue<String> bQueue;

  // Constructor:
  public MyCloudHttpServer(final String portNumber, final ConcurrentLinkedQueue<String> bQueue)
  {
    this.portNumber = portNumber;
    jsonValidator = new JsonValidator();
    myLogger = new MyLogger();
    this.bQueue = bQueue;

    try
    {
      clientRxSchema = new String(Files.readAllBytes(Paths.get("client_json_rx_schema.json")));
    }
    catch (final IOException e)
    {

      System.err.println("Can't read RX Schema file!");
      myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
      System.exit(-1);
    }

    try
    {
      jsonSchema = jsonValidator.getJsonSchemaFromStringContent(clientRxSchema);
    }
    catch (final Exception e)
    {
      System.err.println("Can't convert Client JSON RX Schema to an JSON Object!");
      myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
      System.exit(-1);
    }

    Create();
  }

  public void Run()
  {
    httpServer.createContext("/", new MyCloudHttpHandler());
    httpServer.start();
  }

  private void Create()
  {
    try
    {
      httpServer = HttpServer.create(new InetSocketAddress(Integer.parseInt(portNumber)), 0);
      myLogger.log("DL: HTTP Server started... HTTP: " + portNumber, "INFO");
    }
    catch (final IOException e)
    {
      System.err.println("Can't create HTTP socket!");
      myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
      System.exit(-1);

    }

  }

  private boolean ValidateJson(final String rcvDataString)
  {
    boolean isValid = false;

    JsonNode jsonNode = null;

    try
    {
      jsonNode = jsonValidator.getJsonNodeFromStringContent(rcvDataString);
    }
    catch (final Exception e)
    {
      myLogger.log("Received msg is not valid JSON!" + System.getProperty("line.separator") + rcvDataString, "INFO");
      isValid = false;
      return isValid;
    }

    final Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);

    if (errors.size() == 0)
    {
      isValid = true;
      // myLogger.log("Received JSON is valid!" +
      // System.getProperty("line.separator") + rcvDataString, "INFO");
      myLogger.log("Received JSON is valid!", "INFO");
    }
    else
    {
      myLogger.log("Received JSON is invalid against schema!" + System.getProperty("line.separator") + rcvDataString,
          "INFO");
      isValid = false;
    }

    return isValid;
  }

  // Handler for "/" context
  class MyCloudHttpHandler implements HttpHandler
  {

    @Override
    public void handle(final HttpExchange httpExchange)
    {
      // Serve for POST requests only
      if (httpExchange.getRequestMethod().equalsIgnoreCase("POST"))
      {
        try
        {
          myLogger.log(
              "Received message from: " + httpExchange.getRemoteAddress().getAddress().toString().substring(1), "INFO");

          // REQUEST Headers
          final Headers requestHeaders = httpExchange.getRequestHeaders();

          final int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

          // REQUEST Body
          final InputStream inputStream = httpExchange.getRequestBody();

          final byte[] rcvBytes = new byte[contentLength];
          final int length = inputStream.read(rcvBytes);

          if (length > 0)
          {
            rcvDataString = new String(rcvBytes, 0, length);
            isValid = ValidateJson(rcvDataString);
          }
          else
          {
            isValid = false;
          }

          msgUuid = UUID.randomUUID().toString();

          if (isValid)
          {
            responceCode = HttpURLConnection.HTTP_OK;

            responseMsg = "<html><head><title>MyCloud</title></head><body>OK<br>UUID: " + msgUuid + "</body></html>";
          }
          else
          {
            responceCode = HttpURLConnection.HTTP_BAD_REQUEST;

            responseMsg = "<html><head><title>MyCloud</title></head><body>Bad Request<br>UUID: " + msgUuid
                + "</body></html>";

          }

          // RESPONSE Headers
          // final Headers responseHeaders = httpExchange.getResponseHeaders();

          // Send RESPONSE Headers

          final int rspLength = responseMsg.getBytes().length;

          httpExchange.sendResponseHeaders(responceCode, rspLength);

          // RESPONSE Body
          final OutputStream outputStream = httpExchange.getResponseBody();

          outputStream.write(responseMsg.getBytes());

          if (isValid)
          {
            bQueue.add(rcvDataString);
            myLogger.log("Added to BQUEUE", "INFO");
            myLogger.log("BQUEUE size: " + bQueue.size(), "INFO");

          }

          httpExchange.close();

        }
        catch (final Exception e)
        {

          myLogger.log("HTTP server exception occured! Restarting...", "ERROR");
          myLogger.log(MyLogger.exceptionStacktraceToString(e), "ERROR");
          httpServer.stop(1);
          Run();

        }
      }

    }
  }
}
