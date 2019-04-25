// 4GIT
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class MyLogger
{
  public void log(final String logMsg, final String logType)
  {
    Path path = Paths.get("./" + logType + ".log");
    final String logStr = ((new Date()).toString() + " [" + logType.toUpperCase() + "] " + logMsg
        + System.getProperty("line.separator")
        + "--------------------------------------------------------------------------------"
        + System.getProperty("line.separator"));
    try
    {
      if (Files.exists(path))
      {
        Files.write(path, logStr.getBytes(), StandardOpenOption.APPEND);
      }
      else
      {
        Files.write(path, logStr.getBytes());
      }
    }
    catch (final IOException fe)
    {
      System.err.println("Can't write to " + logType + ".log file!");
    }

    if (logType.equals("ERROR"))
    {
      System.err.print(logStr);
    }
    else
    {
      System.out.print(logStr);
    }

  }

  public static String exceptionStacktraceToString(Exception e)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    e.printStackTrace(ps);
    ps.close();
    return baos.toString();
  }

}