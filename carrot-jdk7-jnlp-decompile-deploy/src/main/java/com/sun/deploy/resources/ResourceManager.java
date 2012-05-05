package com.sun.deploy.resources;

import com.sun.deploy.trace.Trace;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ResourceManager
{
  private static ResourceBundle rb;
  private static ResourceBundle rbMnemonics;
  private static NumberFormat _numberFormat;
  static Class _keyEventClazz;

  static void reset()
  {
    rb = ResourceBundle.getBundle("com.sun.deploy.resources.Deployment");
    rbMnemonics = ResourceBundle.getBundle("com.sun.deploy.resources.DeploymentMnemonics");
    _numberFormat = NumberFormat.getInstance();
  }

  public static String getMessage(String paramString)
  {
    try
    {
      return rb.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
    }
    return paramString;
  }

  public static String getFormattedMessage(String paramString, Object[] paramArrayOfObject)
  {
    try
    {
      MessageFormat localMessageFormat = new MessageFormat(rb.getString(paramString));
      return localMessageFormat.format(paramArrayOfObject);
    }
    catch (MissingResourceException localMissingResourceException)
    {
    }
    return paramString;
  }

  public static String[] getMessageArray(String paramString)
  {
    // Byte code:
    //   0: getstatic 195	com/sun/deploy/resources/ResourceManager:rb	Ljava/util/ResourceBundle;
    //   3: aload_0
    //   4: invokevirtual 231	java/util/ResourceBundle:getStringArray	(Ljava/lang/String;)[Ljava/lang/String;
    //   7: areturn
    //   8: astore_1
    //   9: iconst_1
    //   10: anewarray 105	java/lang/String
    //   13: dup
    //   14: iconst_0
    //   15: aload_0
    //   16: aastore
    //   17: areturn
    //
    // Exception table:
    //   from	to	target	type
    //   0	7	8	java/util/MissingResourceException
  }

  public static int getAcceleratorKey(String paramString)
  {
    Integer localInteger = (Integer)rbMnemonics.getObject(paramString + ".acceleratorKey");
    return localInteger.intValue();
  }

  public static String getString(String paramString)
  {
    try
    {
      return rb.getString(paramString);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      Trace.ignoredException(localMissingResourceException);
    }
    return null;
  }

  public static int getInteger(String paramString)
  {
    try
    {
      return Integer.parseInt(getString(paramString), 16);
    }
    catch (MissingResourceException localMissingResourceException)
    {
      Trace.ignoredException(localMissingResourceException);
    }
    return -1;
  }

  public static String getString(String paramString1, String paramString2)
  {
    Object[] arrayOfObject = { paramString2 };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static String getString(String paramString1, String paramString2, String paramString3)
  {
    Object[] arrayOfObject = { paramString2, paramString3 };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static String getString(String paramString, Long paramLong1, Long paramLong2)
  {
    Object[] arrayOfObject = { paramLong1, paramLong2 };
    return applyPattern(paramString, arrayOfObject);
  }

  public static String getString(String paramString1, String paramString2, String paramString3, String paramString4)
  {
    Object[] arrayOfObject = { paramString2, paramString3, paramString4 };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static String getString(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
  {
    Object[] arrayOfObject = { paramString2, paramString3, paramString4, paramString5 };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static String getString(String paramString, int paramInt)
  {
    Object[] arrayOfObject = { new Integer(paramInt) };
    return applyPattern(paramString, arrayOfObject);
  }

  public static String getString(String paramString, int paramInt1, int paramInt2, int paramInt3)
  {
    Object[] arrayOfObject = { new Integer(paramInt1), new Integer(paramInt2), new Integer(paramInt3) };
    return applyPattern(paramString, arrayOfObject);
  }

  public static String getString(String paramString1, String paramString2, int paramInt, String paramString3)
  {
    Object[] arrayOfObject = { paramString2, new Integer(paramInt), paramString3 };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static String getString(String paramString1, String paramString2, int paramInt)
  {
    Object[] arrayOfObject = { paramString2, new Integer(paramInt) };
    return applyPattern(paramString1, arrayOfObject);
  }

  public static synchronized String formatDouble(double paramDouble, int paramInt)
  {
    _numberFormat.setGroupingUsed(true);
    _numberFormat.setMaximumFractionDigits(paramInt);
    _numberFormat.setMinimumFractionDigits(paramInt);
    return _numberFormat.format(paramDouble);
  }

  public static ImageIcon getIcon(String paramString)
  {
    String str = getString(paramString);
    return new ImageIcon(ResourceManager.class.getResource(str));
  }

  public static ImageIcon[] getIcons(String paramString)
  {
    ImageIcon[] arrayOfImageIcon = new ImageIcon[4];
    String str1 = getString(paramString);
    arrayOfImageIcon[0] = new ImageIcon(ResourceManager.class.getResource(str1));
    int i = str1.lastIndexOf(".");
    String str2 = str1;
    String str3 = "";
    if (i > 0)
    {
      str2 = str1.substring(0, i);
      str3 = str1.substring(i);
    }
    arrayOfImageIcon[1] = new ImageIcon(ResourceManager.class.getResource(str2 + "-p" + str3));
    arrayOfImageIcon[2] = new ImageIcon(ResourceManager.class.getResource(str2 + "-d" + str3));
    arrayOfImageIcon[3] = new ImageIcon(ResourceManager.class.getResource(str2 + "-o" + str3));
    return arrayOfImageIcon;
  }

  private static String applyPattern(String paramString, Object[] paramArrayOfObject)
  {
    String str1 = getString(paramString);
    String str2 = MessageFormat.format(str1, paramArrayOfObject);
    return str2;
  }

  public static Color getColor(String paramString)
  {
    int i = getInteger(paramString);
    return new Color(i);
  }

  public static Font getUIFont()
  {
    return new JLabel().getFont();
  }

  public static int getMinFontSize()
  {
    int i = 0;
    try
    {
      i = ((Integer)rb.getObject("ui.min.font.size")).intValue();
    }
    catch (MissingResourceException localMissingResourceException)
    {
    }
    return i;
  }

  public static int getVKCode(String paramString)
  {
    String str = getString(paramString);
    if ((str != null) && (str.startsWith("VK_")))
      try
      {
        if (_keyEventClazz == null)
          _keyEventClazz = Class.forName("java.awt.event.KeyEvent");
        Field localField = _keyEventClazz.getDeclaredField(str);
        int i = localField.getInt(null);
        return i;
      }
      catch (ClassNotFoundException localClassNotFoundException)
      {
        Trace.ignoredException(localClassNotFoundException);
      }
      catch (NoSuchFieldException localNoSuchFieldException)
      {
        Trace.ignoredException(localNoSuchFieldException);
      }
      catch (SecurityException localSecurityException)
      {
        Trace.ignoredException(localSecurityException);
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    return 0;
  }

  static
  {
    reset();
    _keyEventClazz = null;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.resources.ResourceManager
 * JD-Core Version:    0.6.0
 */