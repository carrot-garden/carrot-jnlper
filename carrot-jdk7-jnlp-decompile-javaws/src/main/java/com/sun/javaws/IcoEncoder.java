package com.sun.javaws;

import com.sun.deploy.config.Platform;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.ImageLoader;
import com.sun.deploy.util.PerfLogger;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class IcoEncoder
{
  private static final boolean DEBUG = false;
  private Image _awtImage;
  private byte _size;
  private byte[] _andData;
  private byte[] _xorData;
  static final int[] DEFAULT_SIZES = { 32, 16, 48, 64 };

  public IcoEncoder(Image paramImage, byte paramByte)
  {
    this._size = paramByte;
    this._awtImage = paramImage;
  }

  public static String getIconPath(LaunchDesc paramLaunchDesc, boolean paramBoolean)
  {
    return getIconPath(paramLaunchDesc);
  }

  public static String getIconPath(LaunchDesc paramLaunchDesc)
  {
    ArrayList localArrayList = new ArrayList();
    Integer localInteger1 = new Integer(Platform.get().getSystemShortcutIconSize(true));
    Integer localInteger2 = new Integer(Platform.get().getSystemShortcutIconSize(false));
    for (int i = 0; i < DEFAULT_SIZES.length; i++)
    {
      localObject1 = new Integer(DEFAULT_SIZES[i]);
      if (localArrayList.contains(localObject1))
        continue;
      localArrayList.add(localObject1);
    }
    if (!localArrayList.contains(localInteger1))
      localArrayList.add(localInteger1);
    if (!localArrayList.contains(localInteger2))
      localArrayList.add(localInteger2);
    Iterator localIterator = localArrayList.iterator();
    Object localObject1 = new IconDesc[6];
    byte[] arrayOfByte = new byte[6];
    int j = 0;
    while (localIterator.hasNext())
    {
      int k = ((Integer)localIterator.next()).intValue();
      if (k < 16)
        k = 16;
      if (k > 64)
        k = 64;
      localObject3 = paramLaunchDesc.getInformation().getIconLocation(k, 5);
      if (localObject3 == null)
        localObject3 = paramLaunchDesc.getInformation().getIconLocation(k, 0);
      if (localObject3 != null)
      {
        m = 0;
        for (int n = 0; (n < j) && (m == 0); n++)
        {
          if (!((IconDesc)localObject3).equals(localObject1[n]))
            continue;
          m = 1;
        }
        if (m == 0)
        {
          localObject1[j] = localObject3;
          n = ((IconDesc)localObject3).getWidth();
          int i1 = ((IconDesc)localObject3).getHeight();
          if ((n == i1) && (n >= 16) && (n <= 64))
            arrayOfByte[j] = (byte)n;
          else
            arrayOfByte[j] = (byte)k;
          j++;
        }
      }
    }
    Object localObject2 = null;
    Object localObject3 = new IcoEncoder[6];
    Object localObject4;
    for (int m = 0; m < j; m++)
    {
      localObject4 = null;
      Object localObject5 = null;
      try
      {
        File localFile = DownloadEngine.getUpdatedShortcutImage(localObject1[m].getLocation(), localObject1[m].getVersion());
        if (localFile != null)
        {
          if (Platform.get().isPlatformIconType(localObject1[m].getLocation().toString()))
          {
            localObject4 = localFile.toString();
            localObject5 = localFile;
          }
          else
          {
            localObject4 = localFile.getPath() + ".ico";
            localObject5 = new File((String)localObject4);
          }
          if (((File)localObject5).exists())
          {
            localObject2 = localObject4;
            return localObject2;
          }
          if (localObject2 == null)
            localObject2 = localObject4;
          PerfLogger.setTime("before ico creation for " + (String)localObject4);
          Image localImage = ImageLoader.getInstance().loadImage(localFile.getPath());
          localObject3[m] = new IcoEncoder(localImage, arrayOfByte[m]);
          localObject3[m].createBitmaps();
          PerfLogger.setTime("after ico creation for " + (String)localObject4);
        }
      }
      catch (IOException localIOException2)
      {
        Trace.ignored(localIOException2);
      }
    }
    BufferedOutputStream localBufferedOutputStream = null;
    if ((localObject2 != null) && (j > 0))
    {
      localObject4 = new int[6];
      localObject4[0] = (6 + 16 * j);
      for (int i2 = 1; i2 < j; i2++)
        localObject4[i2] = (localObject4[(i2 - 1)] + 40 + localObject3[(i2 - 1)].getXorData().length + localObject3[(i2 - 1)].getAndData().length);
      try
      {
        localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(localObject2)));
        IcoStreamWriter localIcoStreamWriter = localObject3[0].getIcoStreamWriter(localBufferedOutputStream);
        localIcoStreamWriter.writeIcoHeader(j);
        for (int i3 = 0; i3 < j; i3++)
          localObject3[i3].writeIconDirEntry(localIcoStreamWriter, localObject4[i3]);
        for (i3 = 0; i3 < j; i3++)
        {
          localObject3[i3].writeInfoHeader(localIcoStreamWriter);
          localBufferedOutputStream.write(localObject3[i3].getXorData());
          localBufferedOutputStream.write(localObject3[i3].getAndData());
        }
        localBufferedOutputStream.flush();
      }
      catch (IOException localException2)
      {
        Trace.ignoredException(localIOException1);
      }
      finally
      {
        if (localBufferedOutputStream != null)
          try
          {
            localBufferedOutputStream.close();
          }
          catch (Exception localException3)
          {
          }
      }
    }
    return (String)(String)(String)(String)localObject2;
  }

  public static String getIconPath(URL paramURL, String paramString)
  {
    String str = null;
    IcoEncoder localIcoEncoder = null;
    try
    {
      File localFile2 = DownloadEngine.getUpdatedShortcutImage(paramURL, paramString);
      if (localFile2 != null)
      {
        File localFile1;
        if (Platform.get().isPlatformIconType(paramURL.toString()))
        {
          str = localFile2.toString();
          localFile1 = localFile2;
        }
        else
        {
          str = localFile2.getPath() + ".ico";
          localFile1 = new File(str);
        }
        if (localFile1.exists())
          return str;
        Image localImage = ImageLoader.getInstance().loadImage(localFile2.getPath());
        localIcoEncoder = new IcoEncoder(localImage, 32);
        localIcoEncoder.createBitmaps();
      }
    }
    catch (IOException localIOException1)
    {
      Trace.ignored(localIOException1);
    }
    if ((str != null) && (localIcoEncoder != null))
    {
      BufferedOutputStream localBufferedOutputStream = null;
      int i = 22;
      try
      {
        localBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(str)));
        IcoStreamWriter localIcoStreamWriter = localIcoEncoder.getIcoStreamWriter(localBufferedOutputStream);
        localIcoStreamWriter.writeIcoHeader(1);
        localIcoEncoder.writeIconDirEntry(localIcoStreamWriter, i);
        localIcoEncoder.writeInfoHeader(localIcoStreamWriter);
        localBufferedOutputStream.write(localIcoEncoder.getXorData());
        localBufferedOutputStream.write(localIcoEncoder.getAndData());
        localBufferedOutputStream.flush();
      }
      catch (IOException localException2)
      {
        Trace.ignoredException(localIOException2);
      }
      finally
      {
        if (localBufferedOutputStream != null)
          try
          {
            localBufferedOutputStream.close();
          }
          catch (Exception localException3)
          {
          }
      }
    }
    return str;
  }

  private void createBitmaps()
    throws IOException
  {
    int i = this._size;
    int j = this._size;
    int k = getXorScanSize(this._size);
    int m = getAndScanSize(this._size);
    int n = 0;
    int[] arrayOfInt = new int[i * j];
    byte[] arrayOfByte1 = new byte[j * k];
    this._xorData = new byte[j * k];
    byte[] arrayOfByte2 = new byte[j * m];
    this._andData = new byte[j * m];
    Image localImage = this._awtImage.getScaledInstance(i, j, 4);
    PixelGrabber localPixelGrabber = new PixelGrabber(localImage, 0, 0, i, j, arrayOfInt, 0, i);
    try
    {
      localPixelGrabber.grabPixels();
    }
    catch (InterruptedException localInterruptedException)
    {
      localInterruptedException.printStackTrace();
    }
    int i2;
    int i4;
    for (int i1 = 0; i1 < j; i1++)
    {
      i2 = i1 * m;
      i3 = 0;
      i4 = i1 * k;
      int i5 = 0;
      int i6 = 0;
      int i7 = 0;
      for (int i8 = 0; i8 < i; i8++)
      {
        int i9 = i1 * i + i8;
        int i10 = arrayOfInt[i9] >> 24 & 0xFF;
        int i11 = arrayOfInt[i9] >> 16 & 0xFF;
        int i12 = arrayOfInt[i9] >> 8 & 0xFF;
        int i13 = arrayOfInt[i9] & 0xFF;
        if (i10 == 0)
          i6 = (byte)(i6 | 128 >> i7);
        i7++;
        if ((i7 == 8) || (i8 == i - 1))
        {
          arrayOfByte2[(i2 + i3++)] = i6;
          i6 = 0;
          i7 = 0;
        }
        arrayOfByte1[(i4 + i5++)] = (byte)i13;
        arrayOfByte1[(i4 + i5++)] = (byte)i12;
        arrayOfByte1[(i4 + i5++)] = (byte)i11;
      }
      while (i3 < m)
        arrayOfByte2[(i2 + i3++)] = 0;
      while (i5 < k)
        arrayOfByte1[(i4 + i5++)] = 0;
    }
    for (int i3 = 0; i3 < j; i3++)
    {
      i2 = i3 * k;
      i1 = (j - i3 - 1) * k;
      for (i4 = 0; i4 < k; i4++)
        this._xorData[(i2 + i4)] = arrayOfByte1[(i1 + i4)];
      i2 = i3 * m;
      i1 = (j - i3 - 1) * m;
      for (i4 = 0; i4 < m; i4++)
        this._andData[(i2 + i4)] = arrayOfByte2[(i1 + i4)];
    }
  }

  private byte[] getXorData()
  {
    return this._xorData;
  }

  private byte[] getAndData()
  {
    return this._andData;
  }

  private void writeInfoHeader(IcoStreamWriter paramIcoStreamWriter)
    throws IOException
  {
    paramIcoStreamWriter.writeDWord(40);
    paramIcoStreamWriter.writeDWord(this._size);
    paramIcoStreamWriter.writeDWord(2 * this._size);
    paramIcoStreamWriter.writeWord(1);
    paramIcoStreamWriter.writeWord(24);
    paramIcoStreamWriter.writeDWord(0);
    paramIcoStreamWriter.writeDWord(0);
    paramIcoStreamWriter.writeDWord(0);
    paramIcoStreamWriter.writeDWord(0);
    paramIcoStreamWriter.writeDWord(0);
    paramIcoStreamWriter.writeDWord(0);
  }

  private void writeIconDirEntry(IcoStreamWriter paramIcoStreamWriter, int paramInt)
    throws IOException
  {
    int i = getAndScanSize(this._size);
    int j = getXorScanSize(this._size);
    try
    {
      paramIcoStreamWriter.write(this._size);
      paramIcoStreamWriter.write(this._size);
      paramIcoStreamWriter.write(0);
      paramIcoStreamWriter.write(0);
      paramIcoStreamWriter.writeWord(1);
      paramIcoStreamWriter.writeWord(24);
      int k = this._size * j + this._size * i + 40;
      paramIcoStreamWriter.writeDWord(k);
      paramIcoStreamWriter.writeDWord(paramInt);
    }
    catch (IOException localIOException)
    {
      localIOException.printStackTrace();
    }
  }

  private IcoStreamWriter getIcoStreamWriter(BufferedOutputStream paramBufferedOutputStream)
  {
    return new IcoStreamWriter(paramBufferedOutputStream, null);
  }

  private static int getAndScanSize(int paramInt)
  {
    int i = (paramInt + 7) / 8;
    int j = 4 * ((i + 3) / 4);
    return j;
  }

  private static int getXorScanSize(int paramInt)
  {
    int i = paramInt * 3;
    int j = 4 * ((i + 3) / 4);
    return j;
  }

  public static void showIconFile(File paramFile)
  {
    Trace.println("Icon: " + paramFile);
    try
    {
      FileInputStream localFileInputStream = new FileInputStream(paramFile);
      byte[] arrayOfByte1 = new byte[16];
      byte[] arrayOfByte2 = new byte[6];
      localFileInputStream.read(arrayOfByte2);
      Trace.println("header: " + arrayOfByte2[0] + ", " + arrayOfByte2[1] + ", " + arrayOfByte2[2] + ", " + arrayOfByte2[3] + ", " + arrayOfByte2[4] + ", " + arrayOfByte2[5]);
      int j = arrayOfByte2[4];
      for (int k = 0; k < j; k++)
      {
        localFileInputStream.read(arrayOfByte1);
        Trace.println("Dir entry " + k + ": " + arrayOfByte1[0] + ", " + arrayOfByte1[1] + ", " + arrayOfByte1[2] + ", " + arrayOfByte1[3] + ", " + arrayOfByte1[4] + ", " + arrayOfByte1[5] + ", " + arrayOfByte1[6] + ", " + arrayOfByte1[7] + ", " + arrayOfByte1[8] + ", " + arrayOfByte1[9] + ", " + arrayOfByte1[10] + ", " + arrayOfByte1[11] + ", " + arrayOfByte1[12] + ", " + arrayOfByte1[13] + ", " + arrayOfByte1[14] + ", " + arrayOfByte1[15]);
      }
      k = 0;
      Trace.println("InfoHeader: ");
      byte[] arrayOfByte3 = new byte[40];
      localFileInputStream.read(arrayOfByte3);
      for (k = 0; k < 40; k++)
        Trace.print(arrayOfByte3[k] + ",");
      Trace.println("\n");
      Trace.println("the rest: ");
      int i;
      while ((i = localFileInputStream.read(arrayOfByte1)) > 0)
        Trace.println(" line " + k++ + " : " + arrayOfByte1[0] + ", " + arrayOfByte1[1] + ", " + arrayOfByte1[2] + ", " + arrayOfByte1[3] + ", " + arrayOfByte1[4] + ", " + arrayOfByte1[5] + ", " + arrayOfByte1[6] + ", " + arrayOfByte1[7] + ", " + arrayOfByte1[8] + ", " + arrayOfByte1[9] + ", " + arrayOfByte1[10] + ", " + arrayOfByte1[11] + ", " + arrayOfByte1[12] + ", " + arrayOfByte1[13] + ", " + arrayOfByte1[14] + ", " + arrayOfByte1[15]);
    }
    catch (Exception localException)
    {
    }
  }

  private class IcoStreamWriter
  {
    BufferedOutputStream _bos;
    private final IcoEncoder this$0;

    private IcoStreamWriter(BufferedOutputStream arg2)
    {
      this.this$0 = this$1;
      Object localObject;
      this._bos = localObject;
    }

    private void writeIcoHeader(int paramInt)
      throws IOException
    {
      try
      {
        writeWord(0);
        writeWord(1);
        writeWord(paramInt);
      }
      catch (IOException localIOException)
      {
        localIOException.printStackTrace();
      }
    }

    public void write(byte paramByte)
      throws IOException
    {
      this._bos.write(paramByte);
    }

    public void writeWord(int paramInt)
      throws IOException
    {
      this._bos.write(paramInt & 0xFF);
      this._bos.write((paramInt & 0xFF00) >> 8);
    }

    public void writeDWord(int paramInt)
      throws IOException
    {
      this._bos.write(paramInt & 0xFF);
      this._bos.write((paramInt & 0xFF00) >> 8);
      this._bos.write((paramInt & 0xFF0000) >> 16);
      this._bos.write((paramInt & 0xFF000000) >> 24);
    }

    IcoStreamWriter(BufferedOutputStream param1, IcoEncoder.1 arg3)
    {
      this(param1);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.IcoEncoder
 * JD-Core Version:    0.6.0
 */