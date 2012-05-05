package com.sun.deploy.cache;

import com.sun.deploy.config.Platform;
import java.util.Properties;

final class UpgradeStatus
{
  protected static final String BEGIN_TIMESTAMP_PROP_SUFFIX = "BeginTimestamp";
  protected static final String ATTEMPT_COUNT_PROP_SUFFIX = "AttemptCount";
  private static final int MAX_UPGRADE_ATTEMPTS = 5;
  private static final long RECENT_LIMIT = 1800000L;
  private final String upgradeBeginPropName;
  private final String upgradeCountPropName;
  private long lastUpgradeBeginTimestamp;
  private int upgradeAttemptCount;

  private UpgradeStatus(String paramString1, long paramLong, String paramString2, int paramInt)
  {
    this.upgradeBeginPropName = paramString1;
    this.upgradeCountPropName = paramString2;
    this.lastUpgradeBeginTimestamp = paramLong;
    this.upgradeAttemptCount = paramInt;
  }

  static boolean beenInitialized(String paramString)
  {
    String[] arrayOfString = getUpgraderStatusPropNames(paramString);
    Properties localProperties = Platform.get().getCacheUpgradeInfo(arrayOfString);
    return localProperties.size() > 0;
  }

  static void initialized(String paramString)
  {
    String str1 = paramString + "BeginTimestamp";
    String str2 = paramString + "AttemptCount";
    Properties localProperties = new Properties();
    localProperties.setProperty(str1, String.valueOf(0));
    localProperties.setProperty(str2, String.valueOf(0));
    Platform.get().storeCacheUpgradeInfo(localProperties);
  }

  static UpgradeStatus getUpgradeStatus(String paramString1, long paramLong, String paramString2, int paramInt)
  {
    return new UpgradeStatus(paramString1, paramLong, paramString2, paramInt);
  }

  static String[] getUpgraderStatusPropNames(String paramString)
  {
    String str1 = paramString + "BeginTimestamp";
    String str2 = paramString + "AttemptCount";
    return new String[] { str1, str2 };
  }

  static UpgradeStatus getUpgradeStatus(String paramString)
  {
    long l = 0L;
    int i = 0;
    String str1 = paramString + "BeginTimestamp";
    String str2 = paramString + "AttemptCount";
    Properties localProperties = Platform.get().getCacheUpgradeInfo(new String[] { str1, str2 });
    String str3 = localProperties.getProperty(str1);
    if (str3 != null)
      try
      {
        l = Long.parseLong(str3);
      }
      catch (NumberFormatException localNumberFormatException1)
      {
      }
    str3 = localProperties.getProperty(str2);
    if (str3 != null)
      try
      {
        i = Integer.parseInt(str3);
      }
      catch (NumberFormatException localNumberFormatException2)
      {
      }
    UpgradeStatus localUpgradeStatus = new UpgradeStatus(str1, l, str2, i);
    return localUpgradeStatus;
  }

  boolean isCompleted()
  {
    return this.upgradeAttemptCount == 2147483647;
  }

  void setCompleted()
  {
    this.upgradeAttemptCount = 2147483647;
    save();
  }

  boolean incrementUpgradeAttempt(int paramInt)
  {
    if (this.upgradeAttemptCount != paramInt)
      return false;
    this.upgradeAttemptCount += 1;
    save();
    return true;
  }

  private void save()
  {
    Properties localProperties = new Properties();
    localProperties.put(this.upgradeBeginPropName, String.valueOf(System.currentTimeMillis()));
    localProperties.put(this.upgradeCountPropName, String.valueOf(this.upgradeAttemptCount));
    Platform.get().storeCacheUpgradeInfo(localProperties);
  }

  boolean isDone()
  {
    return this.upgradeAttemptCount >= 5;
  }

  boolean wasStartedRecently()
  {
    return (this.lastUpgradeBeginTimestamp > 0L) && (System.currentTimeMillis() - this.lastUpgradeBeginTimestamp < 1800000L);
  }

  int getUpgradeAttempts()
  {
    return this.upgradeAttemptCount;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.cache.UpgradeStatus
 * JD-Core Version:    0.6.0
 */