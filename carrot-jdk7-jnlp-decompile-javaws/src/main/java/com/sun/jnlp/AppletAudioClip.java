package com.sun.jnlp;

import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import java.applet.AudioClip;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

final class AppletAudioClip
  implements AudioClip
{
  private static Constructor acConstructor = null;
  private URL url = null;
  private AudioClip audioClip = null;
  private static Map audioClips = new HashMap();

  public AppletAudioClip()
  {
  }

  public AppletAudioClip(URL paramURL)
  {
    this.url = paramURL;
    try
    {
      InputStream localInputStream = paramURL.openStream();
      createAppletAudioClip(localInputStream);
    }
    catch (IOException localIOException)
    {
      Trace.println("IOException creating AppletAudioClip" + localIOException, TraceLevel.BASIC);
    }
  }

  public static synchronized AudioClip get(URL paramURL)
  {
    checkConnect(paramURL);
    Object localObject = (AudioClip)audioClips.get(paramURL);
    if (localObject == null)
    {
      localObject = new AppletAudioClip(paramURL);
      audioClips.put(paramURL, localObject);
    }
    return (AudioClip)localObject;
  }

  void createAppletAudioClip(InputStream paramInputStream)
    throws IOException
  {
    if (acConstructor == null)
    {
      Trace.println("Initializing AudioClip constructor.", TraceLevel.BASIC);
      try
      {
        acConstructor = (Constructor)AccessController.doPrivileged(new PrivilegedExceptionAction()
        {
          public Object run()
            throws NoSuchMethodException, SecurityException, ClassNotFoundException
          {
            Class localClass = null;
            try
            {
              localClass = Class.forName("com.sun.media.sound.JavaSoundAudioClip", true, ClassLoader.getSystemClassLoader());
              Trace.println("Loaded JavaSoundAudioClip", TraceLevel.BASIC);
            }
            catch (ClassNotFoundException localClassNotFoundException)
            {
              localClass = Class.forName("sun.audio.SunAudioClip", true, null);
              Trace.println("Loaded SunAudioClip", TraceLevel.BASIC);
            }
            Class[] arrayOfClass = new Class[1];
            arrayOfClass[0] = Class.forName("java.io.InputStream");
            return localClass.getConstructor(arrayOfClass);
          }
        });
      }
      catch (PrivilegedActionException localPrivilegedActionException)
      {
        Trace.println("Got a PrivilegedActionException: " + localPrivilegedActionException.getException(), TraceLevel.BASIC);
        throw new IOException("Failed to get AudioClip constructor: " + localPrivilegedActionException.getException());
      }
    }
    try
    {
      Object[] arrayOfObject = { paramInputStream };
      this.audioClip = ((AudioClip)acConstructor.newInstance(arrayOfObject));
    }
    catch (Exception localException)
    {
      throw new IOException("Failed to construct the AudioClip: " + localException);
    }
  }

  private static void checkConnect(URL paramURL)
  {
    SecurityManager localSecurityManager = System.getSecurityManager();
    if (localSecurityManager != null)
      try
      {
        Permission localPermission = paramURL.openConnection().getPermission();
        if (localPermission != null)
          localSecurityManager.checkPermission(localPermission);
        else
          localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
      catch (IOException localIOException)
      {
        localSecurityManager.checkConnect(paramURL.getHost(), paramURL.getPort());
      }
  }

  public synchronized void play()
  {
    if (this.audioClip != null)
      this.audioClip.play();
  }

  public synchronized void loop()
  {
    if (this.audioClip != null)
      this.audioClip.loop();
  }

  public synchronized void stop()
  {
    if (this.audioClip != null)
      this.audioClip.stop();
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.jnlp.AppletAudioClip
 * JD-Core Version:    0.6.0
 */