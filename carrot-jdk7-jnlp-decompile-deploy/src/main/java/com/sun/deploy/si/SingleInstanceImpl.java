package com.sun.deploy.si;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.services.Service;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;

public class SingleInstanceImpl
{
  public static final String SI_FILEDIR = Config.getTempDirectory() + File.separator + "si" + File.separator;
  public static final String SI_MAGICWORD = "javaws.singleinstance.init";
  public static final String SI_MAGICWORD_OPENPRINT = "javaws.singleinstance.init.openprint";
  public static final String SI_ACK = "javaws.singleinstance.ack";
  public static final String SI_STOP = "javaws.singleinstance.stop";
  public static final String SI_EOF = "EOF";
  private ArrayList _sil_list = new ArrayList();
  private static boolean _serverStarted = false;
  private Object _lock = new Object();
  private SingleInstanceServer _sis;
  private static int DEFAULT_FILESIZE = 2147483647;
  private static SecureRandom random = null;
  private static int _randomNumber;

  public static String getSingleInstanceFilePrefix(String paramString)
  {
    String str = paramString.replace('/', '_');
    str = str.replace(':', '_');
    return str;
  }

  public void addSingleInstanceListener(DeploySIListener paramDeploySIListener, String paramString)
  {
    if (paramDeploySIListener == null)
      return;
    synchronized (this._lock)
    {
      if (!_serverStarted)
      {
        Trace.println("unique id: " + paramString, TraceLevel.BASIC);
        try
        {
          String str = paramString + Platform.get().getSessionSpecificString();
          this._sis = new SingleInstanceServer(str, this);
        }
        catch (Exception localException)
        {
          Trace.println("addSingleInstanceListener failed", TraceLevel.BASIC);
          Trace.ignoredException(localException);
          return;
        }
        _serverStarted = true;
      }
    }
    synchronized (this._sil_list)
    {
      if (!this._sil_list.contains(paramDeploySIListener))
        this._sil_list.add(paramDeploySIListener);
    }
  }

  public boolean isSame(String paramString1, String paramString2)
  {
    return true;
  }

  public String[] getArguments(String paramString1, String paramString2)
  {
    String[] arrayOfString = new String[1];
    arrayOfString[0] = paramString1;
    return arrayOfString;
  }

  private static SecureRandom getSecureRandom()
  {
    if (random == null)
    {
      random = ServiceManager.getService().getSecureRandom();
      random.nextInt();
    }
    return random;
  }

  public void removeSingleInstanceListener(DeploySIListener paramDeploySIListener)
  {
    synchronized (this._sil_list)
    {
      Object localObject1 = paramDeploySIListener.getSingleInstanceListener();
      Object localObject2 = null;
      int i = -1;
      for (int j = 0; j < this._sil_list.size(); j++)
      {
        localObject2 = ((DeploySIListener)this._sil_list.get(j)).getSingleInstanceListener();
        if (!localObject2.equals(localObject1))
          continue;
        i = j;
        break;
      }
      if ((i < 0) || (i >= this._sil_list.size()))
        return;
      this._sil_list.remove(i);
      if (this._sil_list.isEmpty())
        AccessController.doPrivileged(new PrivilegedAction()
        {
          public Object run()
          {
            try
            {
              Socket localSocket = new Socket("127.0.0.1", SingleInstanceImpl.this._sis.getPort());
              PrintStream localPrintStream = new PrintStream(localSocket.getOutputStream());
              localPrintStream.println(SingleInstanceImpl._randomNumber);
              localPrintStream.println("javaws.singleinstance.stop");
              localPrintStream.flush();
              localSocket.close();
              SingleInstanceImpl.access$302(false);
            }
            catch (IOException localIOException)
            {
              Trace.ignoredException(localIOException);
            }
            return null;
          }
        });
    }
  }

  class SingleInstanceServer extends Thread
  {
    ServerSocket _ss;
    int _port;
    String _idString;
    String[] _arguments;
    SingleInstanceImpl _impl;

    int getPort()
    {
      return this._port;
    }

    SingleInstanceServer(String paramSingleInstanceImpl, SingleInstanceImpl arg3)
      throws IOException
    {
      this._idString = paramSingleInstanceImpl;
      Object localObject;
      this._impl = localObject;
      this._ss = null;
      this._ss = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"));
      this._port = this._ss.getLocalPort();
      Trace.println("server port at: " + this._port, TraceLevel.BASIC);
      setDaemon(true);
      start();
      createSingleInstanceFile(this._idString, this._port);
    }

    private String getSingleInstanceFilename(String paramString, int paramInt)
    {
      String str = SingleInstanceImpl.SI_FILEDIR + SingleInstanceImpl.getSingleInstanceFilePrefix(paramString) + "_" + paramInt;
      Trace.println("getSingleInstanceFilename: " + str, TraceLevel.BASIC);
      return str;
    }

    private void removeSingleInstanceFile(String paramString, int paramInt)
    {
      new File(getSingleInstanceFilename(paramString, paramInt)).delete();
      Trace.println("removed SingleInstanceFile: " + getSingleInstanceFilename(paramString, paramInt), TraceLevel.BASIC);
    }

    private void createSingleInstanceFile(String paramString, int paramInt)
    {
      String str = getSingleInstanceFilename(paramString, paramInt);
      File localFile1 = new File(str);
      File localFile2 = new File(SingleInstanceImpl.SI_FILEDIR);
      AccessController.doPrivileged(new PrivilegedAction(localFile2, paramString, localFile1)
      {
        private final File val$siDir;
        private final String val$id;
        private final File val$siFile;

        public Object run()
        {
          this.val$siDir.mkdirs();
          String[] arrayOfString = this.val$siDir.list();
          if (arrayOfString != null)
            for (int i = 0; i < arrayOfString.length; i++)
            {
              if (!arrayOfString[i].startsWith(SingleInstanceImpl.getSingleInstanceFilePrefix(this.val$id)))
                continue;
              Trace.println("file should be removed: " + SingleInstanceImpl.SI_FILEDIR + arrayOfString[i], TraceLevel.BASIC);
              new File(SingleInstanceImpl.SI_FILEDIR + arrayOfString[i]).delete();
            }
          try
          {
            this.val$siFile.createNewFile();
            this.val$siFile.deleteOnExit();
            PrintStream localPrintStream = new PrintStream(new FileOutputStream(this.val$siFile));
            SingleInstanceImpl.access$002(SingleInstanceImpl.access$100().nextInt());
            localPrintStream.print(SingleInstanceImpl._randomNumber);
            localPrintStream.close();
          }
          catch (IOException localIOException)
          {
            Trace.ignoredException(localIOException);
          }
          return null;
        }
      });
    }

    boolean isSameInstance(String paramString)
    {
      if (this._impl.isSame(paramString, this._idString))
      {
        this._arguments = this._impl.getArguments(paramString, this._idString);
        return true;
      }
      return false;
    }

    public void run()
    {
      AccessController.doPrivileged(new PrivilegedAction()
      {
        public Object run()
        {
          while (true)
          {
            InputStream localInputStream = null;
            Socket localSocket = null;
            String str1 = null;
            String str2 = "";
            int i = 0;
            int j = -1;
            try
            {
              Trace.println("waiting connection", TraceLevel.BASIC);
              localSocket = SingleInstanceImpl.SingleInstanceServer.this._ss.accept();
              localInputStream = localSocket.getInputStream();
              BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));
              str1 = localBufferedReader.readLine();
              Object localObject1;
              if (!str1.equals(String.valueOf(SingleInstanceImpl._randomNumber)))
              {
                SingleInstanceImpl.SingleInstanceServer.this.removeSingleInstanceFile(SingleInstanceImpl.SingleInstanceServer.this._idString, SingleInstanceImpl.SingleInstanceServer.this._port);
                SingleInstanceImpl.SingleInstanceServer.this._ss.close();
                SingleInstanceImpl.access$302(false);
                Trace.println("Unexpected Error, SingleInstanceService disabled", TraceLevel.BASIC);
                localObject1 = null;
                jsr 337;
              }
              str1 = localBufferedReader.readLine();
              Trace.println("recv: " + str1, TraceLevel.BASIC);
              if (str1.equals("javaws.singleinstance.init"))
              {
                Trace.println("got magic word!!!", TraceLevel.BASIC);
                while (true)
                  try
                  {
                    str1 = localBufferedReader.readLine();
                    if ((str1 != null) && (str1.equals("EOF")))
                      break;
                    str2 = str2 + str1;
                    continue;
                  }
                  catch (IOException localIOException2)
                  {
                    Trace.ignoredException(localIOException2);
                  }
                Trace.println(str2, TraceLevel.BASIC);
                if (SingleInstanceImpl.SingleInstanceServer.this.isSameInstance(str2))
                  i = 1;
              }
              else
              {
                if (str1.equals("javaws.singleinstance.stop"))
                {
                  SingleInstanceImpl.SingleInstanceServer.this.removeSingleInstanceFile(SingleInstanceImpl.SingleInstanceServer.this._idString, SingleInstanceImpl.SingleInstanceServer.this._port);
                  jsr 171;
                }
                if (str1.equals("javaws.singleinstance.init.openprint"))
                {
                  int k = 0;
                  SingleInstanceImpl.SingleInstanceServer.this._arguments = new String[2];
                  Trace.println("GOT OPENPRINT MAGICWORD", TraceLevel.BASIC);
                  for (int m = 0; m < 3; m++)
                    try
                    {
                      str1 = localBufferedReader.readLine();
                      if ((str1 != null) && (str1.equals("EOF")))
                        break;
                      Trace.println(str1, TraceLevel.BASIC);
                      if (k < 2)
                      {
                        SingleInstanceImpl.SingleInstanceServer.this._arguments[k] = str1;
                        k++;
                      }
                    }
                    catch (IOException localIOException3)
                    {
                      Trace.ignoredException(localIOException3);
                    }
                  if (k == 2)
                  {
                    SingleInstanceManager.setActionName(SingleInstanceImpl.SingleInstanceServer.this._arguments[0]);
                    SingleInstanceManager.setOpenPrintFilePath(SingleInstanceImpl.SingleInstanceServer.this._arguments[1]);
                  }
                  i = 1;
                }
              }
            }
            catch (IOException localIOException1)
            {
              Trace.ignoredException(localIOException1);
            }
            finally
            {
              try
              {
                if (i != 0)
                {
                  for (int n = 0; n < SingleInstanceImpl.SingleInstanceServer.this._arguments.length; n++)
                    Trace.println("Starting new instance with arguments: " + SingleInstanceImpl.SingleInstanceServer.this._arguments[n], TraceLevel.BASIC);
                  ArrayList localArrayList = (ArrayList)SingleInstanceImpl.this._sil_list.clone();
                  Iterator localIterator = localArrayList.iterator();
                  while (localIterator.hasNext())
                  {
                    DeploySIListener localDeploySIListener = (DeploySIListener)localIterator.next();
                    localDeploySIListener.newActivation(SingleInstanceImpl.SingleInstanceServer.this._arguments);
                  }
                  Trace.println("sending out ACK..", TraceLevel.BASIC);
                  PrintStream localPrintStream = new PrintStream(localSocket.getOutputStream());
                  localPrintStream.println("javaws.singleinstance.ack");
                  localPrintStream.flush();
                }
                if (localSocket != null)
                  localSocket.close();
              }
              catch (IOException localIOException4)
              {
                Trace.ignoredException(localIOException4);
              }
            }
          }
          return null;
        }
      });
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.si.SingleInstanceImpl
 * JD-Core Version:    0.6.0
 */