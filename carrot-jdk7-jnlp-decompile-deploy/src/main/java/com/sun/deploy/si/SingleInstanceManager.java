package com.sun.deploy.si;

import com.sun.deploy.config.Platform;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class SingleInstanceManager
{
  private static final boolean DEBUG = false;
  private static String _idString;
  private static int _currPort;
  private static String _randomNumberString = null;
  private static String _openPrintFilePath = null;
  private static String _actionName = null;

  public static void setActionName(String paramString)
  {
    if ((paramString == null) || (paramString.equals("-open")) || (paramString.equals("-print")))
      _actionName = paramString;
  }

  public static String getActionName()
  {
    return _actionName;
  }

  public static void setOpenPrintFilePath(String paramString)
  {
    _openPrintFilePath = paramString;
  }

  public static String getOpenPrintFilePath()
  {
    return _openPrintFilePath;
  }

  public static boolean isServerRunning(String paramString)
  {
    File localFile1 = new File(SingleInstanceImpl.SI_FILEDIR);
    String[] arrayOfString = localFile1.list();
    if (arrayOfString != null)
      for (int i = 0; i < arrayOfString.length; i++)
      {
        if (!arrayOfString[i].startsWith(SingleInstanceImpl.getSingleInstanceFilePrefix(paramString + Platform.get().getSessionSpecificString())))
          continue;
        try
        {
          _currPort = Integer.parseInt(arrayOfString[i].substring(arrayOfString[i].lastIndexOf('_') + 1));
        }
        catch (NumberFormatException localNumberFormatException)
        {
          Trace.ignoredException(localNumberFormatException);
          return false;
        }
        Trace.println("server running at port: " + _currPort);
        File localFile2 = new File(SingleInstanceImpl.SI_FILEDIR, arrayOfString[i]);
        BufferedReader localBufferedReader = null;
        try
        {
          localBufferedReader = new BufferedReader(new FileReader(localFile2));
          _randomNumberString = localBufferedReader.readLine();
        }
        catch (IOException localIOException1)
        {
          Trace.ignoredException(localIOException1);
        }
        finally
        {
          try
          {
            if (localBufferedReader != null)
              localBufferedReader.close();
          }
          catch (IOException localIOException2)
          {
            Trace.ignoredException(localIOException2);
          }
        }
        _idString = paramString;
        return true;
      }
    return false;
  }

  public static boolean connectToServer(String paramString)
  {
    Trace.println("connect to: " + _idString + " " + _currPort, TraceLevel.TEMP);
    if (_randomNumberString == null)
    {
      Trace.println("MAGIC number is null, bail out", TraceLevel.TEMP);
      return false;
    }
    try
    {
      Socket localSocket = new Socket("127.0.0.1", _currPort);
      PrintStream localPrintStream = new PrintStream(localSocket.getOutputStream());
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
      localPrintStream.println(_randomNumberString);
      String str1 = getOpenPrintFilePath();
      String str2 = getActionName();
      if ((str1 != null) && (str2 != null))
      {
        localPrintStream.println("javaws.singleinstance.init.openprint");
        localPrintStream.println(str2);
        localPrintStream.println(str1);
      }
      else
      {
        localPrintStream.println("javaws.singleinstance.init");
        localPrintStream.println(paramString);
      }
      localPrintStream.println("EOF");
      localPrintStream.flush();
      Trace.println("waiting for ack", TraceLevel.TEMP);
      int i = 5;
      for (int j = 0; j < i; j++)
      {
        String str3 = localBufferedReader.readLine();
        if ((str3 == null) || (!str3.equals("javaws.singleinstance.ack")))
          continue;
        Trace.println("GOT ACK", TraceLevel.TEMP);
        localSocket.close();
        return true;
      }
      localSocket.close();
    }
    catch (ConnectException localConnectException)
    {
      Trace.println("no server is running - continue launch!", TraceLevel.TEMP);
      return false;
    }
    catch (SocketException localSocketException)
    {
      Trace.println("no server is running - continue launch!", TraceLevel.TEMP);
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    Trace.println("no ACK from server, bail out", TraceLevel.TEMP);
    return false;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.si.SingleInstanceManager
 * JD-Core Version:    0.6.0
 */