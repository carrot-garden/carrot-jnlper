package com.sun.javaws.ui;

import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.net.DownloadEngine;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.ui.ImageLoader;
import com.sun.deploy.ui.ImageLoaderCallback;
import com.sun.deploy.util.URLUtil;
import com.sun.javaws.LaunchDownload;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

class CacheObject
{
  private static final DateFormat df = DateFormat.getDateInstance();
  private static final String[] JNLP_COLUMN_KEYS = { "jnlp.viewer.app.column", "jnlp.viewer.vendor.column", "jnlp.viewer.type.column", "jnlp.viewer.date.column", "jnlp.viewer.size.column", "jnlp.viewer.status.column" };
  private static final String[] RES_COLUMN_KEYS = { "res.viewer.name.column", "res.viewer.url.column", "res.viewer.modified.column", "res.viewer.expired.column", "res.viewer.version.column", "res.viewer.size.column" };
  private static final String[] DEL_COLUMN_KEYS = { "del.viewer.app.column", "del.viewer.url.column" };
  private static String[][] keys = { JNLP_COLUMN_KEYS, RES_COLUMN_KEYS, DEL_COLUMN_KEYS };
  private static TLabel titleLabel;
  private static TLabel vendorLabel;
  private static TLabel typeLabel;
  private static TLabel[] dateLabel = new TLabel[3];
  private static TLabel sizeLabel;
  private static TLabel statusLabel;
  private static TLabel versionLabel;
  private static TLabel urlLabel;
  private static ImageIcon onlineIcon;
  private static ImageIcon offlineIcon;
  private static ImageIcon noLaunchIcon;
  private static ImageIcon java32;
  private static ImageIcon jnlp24;
  private static ImageIcon jar24;
  private static ImageIcon class24;
  private static ImageIcon image24;
  private static ImageIcon other24;
  private final int tableType;
  private final CacheEntry cacheEntry;
  private final AbstractTableModel tableModel;
  private int objectType = -1;
  public static final int TYPE_DELETED = 0;
  public static final int TYPE_JNLP = 1;
  public static final int TYPE_JAR = 2;
  public static final int TYPE_CLASS = 3;
  public static final int TYPE_IMAGE = 4;
  public static final int TYPE_OTHER = 5;
  public static final int ONLINE_ALLOWED_STATUS = 1;
  public static final int OFFLINE_ALLOWED_STATUS = 2;
  private static HashMap imageMap = new HashMap();
  private String deletedTitleString;
  private String deletedUrlString;
  private final int VIEWER_ICON_SIZE = 24;
  private String titleString = null;
  private ImageIcon icon = null;
  private String nameString = null;
  private String vendorString = null;
  private String typeString = null;
  private Date[] date = new Date[3];
  private String[] dateString = new String[3];
  private long theSize = 0L;
  private String sizeString = null;
  static Font plainFont;
  static Font boldFont;
  private int statusInt = -1;
  private ImageIcon statusIcon = null;
  private String statusText = "";
  private static final float[] dash = { 1.0F, 2.0F };
  private static final BasicStroke _dashed = new BasicStroke(1.0F, 2, 0, 10.0F, dash, 0.0F);
  LaunchDesc _ld = null;
  LocalApplicationProperties _lap = null;
  private String versionString = null;

  public CacheObject(CacheEntry paramCacheEntry, AbstractTableModel paramAbstractTableModel, int paramInt)
  {
    this.tableType = paramInt;
    this.cacheEntry = paramCacheEntry;
    this.tableModel = paramAbstractTableModel;
    if (titleLabel == null)
    {
      titleLabel = new TLabel(2);
      vendorLabel = new TLabel(2);
      typeLabel = new TLabel(0);
      for (int i = 0; i < 3; i++)
        dateLabel[i] = new TLabel(4);
      sizeLabel = new TLabel(4);
      statusLabel = new TLabel(0);
      versionLabel = new TLabel(0);
      urlLabel = new TLabel(2);
      java32 = new ViewerIcon(0, ResourceManager.class.getResource("image/java32.png"));
      jnlp24 = new ViewerIcon(0, ResourceManager.class.getResource("image/jnlp24.png"));
      jar24 = new ViewerIcon(0, ResourceManager.class.getResource("image/jar24.png"));
      class24 = new ViewerIcon(0, ResourceManager.class.getResource("image/class24.png"));
      image24 = new ViewerIcon(0, ResourceManager.class.getResource("image/image24.png"));
      other24 = new ViewerIcon(0, ResourceManager.class.getResource("image/other24.png"));
      onlineIcon = new ViewerIcon(0, ResourceManager.class.getResource("image/connect24.png"));
      offlineIcon = new ViewerIcon(0, ResourceManager.class.getResource("image/disconnect24.png"));
      noLaunchIcon = null;
    }
  }

  public int getObjectType()
  {
    if (this.objectType < 0)
    {
      this.objectType = 5;
      if (this.tableType == 0)
      {
        this.objectType = 1;
      }
      else if (this.tableType == 2)
      {
        this.objectType = 0;
      }
      else
      {
        getNameString();
        if (this.nameString.endsWith("jnlp"))
          this.objectType = 1;
        else if ((this.nameString.endsWith("jar")) || (this.nameString.endsWith("zip")))
          this.objectType = 2;
        else if (this.nameString.endsWith("class"))
          this.objectType = 3;
        else if ((this.nameString.endsWith("jpg")) || (this.nameString.endsWith("gif")) || (this.nameString.endsWith("png")) || (this.nameString.endsWith("ico")))
          this.objectType = 4;
      }
    }
    return this.objectType;
  }

  public CacheObject(String paramString1, String paramString2, AbstractTableModel paramAbstractTableModel)
  {
    this(null, paramAbstractTableModel, 2);
    this.deletedTitleString = paramString1;
    this.deletedUrlString = paramString2;
  }

  public String getDeletedTitle()
  {
    return this.deletedTitleString;
  }

  public String getDeletedUrl()
  {
    return this.deletedUrlString;
  }

  public static String getColumnName(int paramInt1, int paramInt2)
  {
    return ResourceManager.getMessage(keys[paramInt2][paramInt1]);
  }

  public static int getColumnCount(int paramInt)
  {
    return keys[paramInt].length;
  }

  public static String getHeaderToolTipText(int paramInt1, int paramInt2)
  {
    String str = "";
    str = keys[paramInt2][paramInt1];
    return ResourceManager.getString(str + ".tooltip");
  }

  public static int getPreferredWidth(int paramInt1, int paramInt2)
  {
    if (paramInt2 == 0)
    {
      switch (paramInt1)
      {
      case 0:
        return 200;
      case 1:
        return 140;
      case 2:
        return 76;
      case 3:
        return 76;
      case 4:
        return 64;
      case 5:
        return 64;
      }
    }
    else if (paramInt2 == 1)
    {
      switch (paramInt1)
      {
      case 0:
        return 120;
      case 1:
        return 220;
      case 2:
        return 76;
      case 3:
        return 76;
      case 4:
        return 64;
      case 5:
        return 64;
      }
    }
    else
    {
      if (paramInt1 == 0)
        return 200;
      return 420;
    }
    return 600;
  }

  public static Class getClass(int paramInt1, int paramInt2)
  {
    return JLabel.class;
  }

  public Object getObject(int paramInt)
  {
    if (this.tableType == 0)
      switch (paramInt)
      {
      case 0:
        return getTitleLabel();
      case 1:
        return getVendorLabel();
      case 2:
        return getTypeLabel();
      case 3:
        return getDateLabel(0);
      case 4:
        return getSizeLabel();
      case 5:
        return getStatusLabel();
      }
    else if (this.tableType == 1)
      switch (paramInt)
      {
      case 0:
        return getNameLabel();
      case 1:
        return getUrlLabel();
      case 2:
        return getDateLabel(1);
      case 3:
        return getDateLabel(2);
      case 4:
        return getVersionLabel();
      case 5:
        return getSizeLabel();
      }
    if (paramInt == 0)
      return getDeletedTitleLabel();
    return getDeletedUrlLabel();
  }

  public boolean isEditable(int paramInt)
  {
    return false;
  }

  public void setValue(int paramInt, Object paramObject)
  {
  }

  public String getTitleString()
  {
    if (this.titleString == null)
      this.titleString = getTitle();
    return this.titleString;
  }

  private TLabel getTitleLabel()
  {
    if (this.icon == null)
    {
      IconDesc localIconDesc = null;
      LaunchDesc localLaunchDesc = getLaunchDesc();
      if (localLaunchDesc != null)
      {
        InformationDesc localInformationDesc = localLaunchDesc.getInformation();
        if (localInformationDesc != null)
          localIconDesc = localInformationDesc.getIconLocation(24, 0);
      }
      if (localIconDesc != null)
        this.icon = new ViewerIcon(24, localIconDesc.getLocation(), localIconDesc.getVersion());
      if (this.icon == null)
        this.icon = java32;
    }
    if ((this.icon != null) && (this.icon.getIconWidth() > 0) && (this.icon.getIconHeight() > 0))
      titleLabel.setIcon(this.icon);
    else
      titleLabel.setIcon(java32);
    titleLabel.setText(getTitleString());
    return titleLabel;
  }

  private TLabel getDeletedTitleLabel()
  {
    titleLabel.setIcon(null);
    titleLabel.setText(this.deletedTitleString);
    return titleLabel;
  }

  private TLabel getDeletedUrlLabel()
  {
    urlLabel.setText(this.deletedUrlString);
    return urlLabel;
  }

  public String getNameString()
  {
    if (this.nameString == null)
    {
      String str = this.cacheEntry.getURL();
      int i = str.lastIndexOf("/");
      int j = (i < 0) || (i >= str.length() - 1) ? 0 : i + 1;
      this.nameString = str.substring(j);
      i = this.nameString.lastIndexOf(".jarjnlp");
      if (i > 0)
        this.nameString = (this.nameString.substring(0, i) + ".jnlp");
    }
    return this.nameString;
  }

  private ImageIcon getJNLPIcon()
  {
    if (this.icon == null)
    {
      IconDesc localIconDesc = null;
      LaunchDesc localLaunchDesc = getLaunchDesc();
      if (localLaunchDesc != null)
      {
        InformationDesc localInformationDesc = localLaunchDesc.getInformation();
        if (localInformationDesc != null)
          localIconDesc = localInformationDesc.getIconLocation(24, 0);
      }
      if (localIconDesc != null)
        this.icon = new ViewerIcon(24, localIconDesc.getLocation(), localIconDesc.getVersion());
      if (this.icon == null)
        this.icon = jnlp24;
    }
    return this.icon;
  }

  private ImageIcon getJarIcon()
  {
    return jar24;
  }

  private ImageIcon getClassIcon()
  {
    return class24;
  }

  private ImageIcon getImageIcon()
  {
    return image24;
  }

  private ImageIcon getOtherIcon()
  {
    return other24;
  }

  private ImageIcon getTypeIcon()
  {
    if (this.icon == null)
      switch (getObjectType())
      {
      case 1:
        this.icon = getJNLPIcon();
        break;
      case 2:
        this.icon = getJarIcon();
        break;
      case 3:
        this.icon = getClassIcon();
        break;
      case 4:
        this.icon = getImageIcon();
        break;
      default:
        this.icon = getOtherIcon();
      }
    return this.icon;
  }

  private TLabel getNameLabel()
  {
    titleLabel.setText(getNameString());
    titleLabel.setIcon(getTypeIcon());
    return titleLabel;
  }

  public String getVendorString()
  {
    if (this.vendorString == null)
      this.vendorString = getVendor();
    return this.vendorString;
  }

  private TLabel getVendorLabel()
  {
    vendorLabel.setText(getVendorString());
    return vendorLabel;
  }

  public static String getLaunchTypeString(int paramInt)
  {
    switch (paramInt)
    {
    case 1:
      return ResourceManager.getMessage("viewer.application");
    case 2:
      return ResourceManager.getMessage("viewer.applet");
    case 3:
      return ResourceManager.getMessage("viewer.extension");
    case 4:
      return ResourceManager.getMessage("viewer.installer");
    }
    return "";
  }

  public String getTypeString()
  {
    if (this.typeString == null)
      this.typeString = getLaunchTypeString(getLaunchDesc().getLaunchType());
    return this.typeString;
  }

  private TLabel getTypeLabel()
  {
    typeLabel.setText(getTypeString());
    return typeLabel;
  }

  public Date getDate(int paramInt)
  {
    if (this.dateString[paramInt] == null)
    {
      switch (paramInt)
      {
      case 0:
        this.date[0] = getLastAccesed();
        break;
      case 1:
        this.date[1] = getLastModified();
        break;
      case 2:
        this.date[2] = getExpired();
      }
      if (this.date[paramInt] != null)
        this.dateString[paramInt] = df.format(this.date[paramInt]);
      else
        this.dateString[paramInt] = "";
    }
    return this.date[paramInt];
  }

  private TLabel getDateLabel(int paramInt)
  {
    getDate(paramInt);
    dateLabel[paramInt].setText(this.dateString[paramInt]);
    return dateLabel[paramInt];
  }

  public long getSize()
  {
    if (this.sizeString == null)
    {
      this.theSize = getResourceSize();
      if (this.theSize > 10240L)
        this.sizeString = (" " + this.theSize / 1024L + " KB");
      else
        this.sizeString = (" " + this.theSize / 1024L + "." + this.theSize % 1024L / 102L + " KB");
    }
    return this.theSize;
  }

  private TLabel getSizeLabel()
  {
    getSize();
    sizeLabel.setText(this.sizeString);
    if (this.tableType == 0)
    {
      if (boldFont == null)
      {
        plainFont = sizeLabel.getFont();
        boldFont = plainFont.deriveFont(1);
      }
      getLocalApplicationProperties();
      if ((this._lap != null) && (this._lap.isJnlpInstalled()))
        sizeLabel.setFont(boldFont);
      else
        sizeLabel.setFont(plainFont);
    }
    return sizeLabel;
  }

  public int getStatus()
  {
    if (this.statusInt < 0)
    {
      this.statusInt = 1;
      if (canLaunchOffline())
        this.statusInt = 3;
      switch (this.statusInt)
      {
      case 0:
        this.statusIcon = noLaunchIcon;
        if (getLaunchDesc().isApplicationDescriptor())
          this.statusText = ResourceManager.getString("viewer.norun1.tooltip", getTypeString());
        else
          this.statusText = ResourceManager.getString("viewer.norun2.tooltip");
        break;
      case 1:
        this.statusIcon = onlineIcon;
        this.statusText = ResourceManager.getString("viewer.online.tooltip", getTypeString());
        break;
      case 2:
        this.statusIcon = offlineIcon;
        this.statusText = ResourceManager.getString("viewer.offline.tooltip", getTypeString());
        break;
      case 3:
        this.statusIcon = offlineIcon;
        this.statusText = ResourceManager.getString("viewer.onlineoffline.tooltip", getTypeString());
      }
    }
    return this.statusInt;
  }

  private TLabel getStatusLabel()
  {
    getStatus();
    if ((this.statusIcon == null) || ((this.statusIcon.getIconWidth() > 0) && (this.statusIcon.getIconHeight() > 0)))
    {
      statusLabel.setIcon(this.statusIcon);
      statusLabel.setToolTipText(this.statusText);
    }
    return statusLabel;
  }

  public static void hasFocus(Component paramComponent, boolean paramBoolean)
  {
    if ((paramComponent instanceof TLabel))
      ((TLabel)paramComponent).hasFocus(paramBoolean);
  }

  public int compareColumns(CacheObject paramCacheObject, int paramInt)
  {
    if (this.tableType == 1)
    {
      switch (paramInt)
      {
      case 0:
        return compareStrings(getNameString(), paramCacheObject.getNameString());
      case 1:
        return compareStrings(getUrlString(), paramCacheObject.getUrlString());
      case 2:
        return compareDates(getDate(1), paramCacheObject.getDate(1));
      case 3:
        return compareDates(getDate(2), paramCacheObject.getDate(2));
      case 4:
        return compareStrings(getVersionString(), paramCacheObject.getVersionString());
      case 5:
      }
      return compareLong(getSize(), paramCacheObject.getSize());
    }
    if (this.tableType == 0)
    {
      switch (paramInt)
      {
      case 0:
        return compareStrings(getTitleString(), paramCacheObject.getTitleString());
      case 1:
        return compareStrings(getVendorString(), paramCacheObject.getVendorString());
      case 2:
        return compareStrings(getTypeString(), paramCacheObject.getTypeString());
      case 3:
        return compareDates(getDate(0), paramCacheObject.getDate(0));
      case 4:
        return compareLong(getSize(), paramCacheObject.getSize());
      case 5:
      }
      return compareInt(getStatus(), paramCacheObject.getStatus());
    }
    if (this.tableType == 2)
    {
      switch (paramInt)
      {
      case 0:
        return compareStrings(getDeletedTitle(), paramCacheObject.getDeletedTitle());
      case 1:
      }
      return compareStrings(getDeletedUrl(), paramCacheObject.getDeletedUrl());
    }
    return 0;
  }

  private int compareStrings(String paramString1, String paramString2)
  {
    if (paramString1 == paramString2)
      return 0;
    if (paramString1 == null)
      return -1;
    if (paramString2 == null)
      return 1;
    return paramString1.compareTo(paramString2);
  }

  private int compareDates(Date paramDate1, Date paramDate2)
  {
    if (paramDate1 == paramDate2)
      return 0;
    if (paramDate1 == null)
      return -1;
    if (paramDate2 == null)
      return 1;
    return compareLong(paramDate1.getTime(), paramDate2.getTime());
  }

  private int compareLong(long paramLong1, long paramLong2)
  {
    if (paramLong1 == paramLong2)
      return 0;
    return paramLong1 < paramLong2 ? -1 : 1;
  }

  private int compareInt(int paramInt1, int paramInt2)
  {
    if (paramInt1 == paramInt2)
      return 0;
    return paramInt1 < paramInt2 ? -1 : 1;
  }

  public CacheEntry getCE()
  {
    return this.cacheEntry;
  }

  public LaunchDesc getLaunchDesc()
  {
    if ((this.cacheEntry == null) || ((this.tableType == 1) && (!this.cacheEntry.getURL().endsWith("jnlp"))))
      return null;
    if (this._ld == null)
      try
      {
        this._ld = LaunchDescFactory.buildDescriptor(this.cacheEntry.getDataFile(), null != this.cacheEntry ? URLUtil.getBase(new URL(this.cacheEntry.getURL())) : null, null, new URL(this.cacheEntry.getURL()));
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    return this._ld;
  }

  public LocalApplicationProperties getLocalApplicationProperties()
  {
    if (this._lap == null)
      this._lap = Cache.getLocalApplicationProperties(this.cacheEntry);
    return this._lap;
  }

  public File getJnlpFile()
  {
    return this.cacheEntry.getDataFile();
  }

  public String getTitle()
  {
    try
    {
      return getLaunchDesc().getInformation().getTitle();
    }
    catch (Exception localException)
    {
    }
    return "";
  }

  public String getVendor()
  {
    try
    {
      return getLaunchDesc().getInformation().getVendor();
    }
    catch (Exception localException)
    {
    }
    return "";
  }

  public String getHref()
  {
    URL localURL = getLaunchDesc().getLocation();
    if (localURL != null)
      return localURL.toString();
    return null;
  }

  public File getIconFile()
  {
    try
    {
      IconDesc localIconDesc = getLaunchDesc().getInformation().getIconLocation(24, 0);
      File localFile = DownloadEngine.getCachedFile(localIconDesc.getLocation(), localIconDesc.getVersion());
      if (localFile != null)
        return localFile;
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public Date getLastAccesed()
  {
    return getLocalApplicationProperties().getLastAccessed();
  }

  public Date getLastModified()
  {
    long l = this.cacheEntry.getLastModified();
    return l > 0L ? new Date(l) : null;
  }

  public Date getExpired()
  {
    long l = this.cacheEntry.getExpirationDate();
    return l > 0L ? new Date(l) : null;
  }

  public long getResourceSize()
  {
    long l = 0L;
    if (this.tableType == 0)
    {
      l += LaunchDownload.getCachedSize(getLaunchDesc());
      l += this.cacheEntry.getSize();
    }
    else if (this.tableType == 1)
    {
      l += this.cacheEntry.getSize();
    }
    return l;
  }

  public String getVersionString()
  {
    if (this.versionString == null)
    {
      this.versionString = this.cacheEntry.getVersion();
      if (this.versionString == null)
        this.versionString = "";
    }
    return this.versionString;
  }

  public TLabel getVersionLabel()
  {
    versionLabel.setText(getVersionString());
    return versionLabel;
  }

  public String getUrlString()
  {
    return this.cacheEntry.getURL();
  }

  public String getCodebaseIP()
  {
    return this.cacheEntry.getCodebaseIP();
  }

  public TLabel getUrlLabel()
  {
    urlLabel.setText(this.cacheEntry.getURL());
    return urlLabel;
  }

  public boolean inFilter(int paramInt)
  {
    return (paramInt == 0) || (paramInt == getLaunchDesc().getLaunchType());
  }

  public boolean hasHref()
  {
    if (getLaunchDesc().isApplicationDescriptor())
      return this._ld.getLocation() != null;
    return false;
  }

  public boolean canLaunchOffline()
  {
    if (getLaunchDesc().isApplicationDescriptor())
      return this._ld.getInformation().supportsOfflineOperation();
    return false;
  }

  private class TLabel extends JLabel
  {
    boolean _focus = false;

    public TLabel(int arg2)
    {
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
      int i;
      setHorizontalAlignment(i);
    }

    public void paint(Graphics paramGraphics)
    {
      super.paint(paramGraphics);
      if ((this._focus) && ((paramGraphics instanceof Graphics2D)))
      {
        Stroke localStroke = ((Graphics2D)paramGraphics).getStroke();
        ((Graphics2D)paramGraphics).setStroke(CacheObject._dashed);
        paramGraphics.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        ((Graphics2D)paramGraphics).setStroke(localStroke);
      }
    }

    public void hasFocus(boolean paramBoolean)
    {
      this._focus = paramBoolean;
    }
  }

  private class ViewerIcon extends ImageIcon
    implements ImageLoaderCallback
  {
    private int _width;
    private int _height;

    public ViewerIcon(int paramString, String arg3)
    {
      this._width = paramString;
      this._height = paramString;
      try
      {
        String str;
        URL localURL = URLUtil.fileToURL(new File(str));
        if ((localURL != null) && (!isCached(localURL, null)))
          ImageLoader.getInstance().loadImage(localURL, this, true);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
    }

    public ViewerIcon(int paramURL, URL paramString, String arg4)
    {
      this._width = paramURL;
      this._height = paramURL;
      try
      {
        String str;
        if (!isCached(paramString, str))
          ImageLoader.getInstance().loadImage(paramString, str, this, true);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
    }

    public ViewerIcon(int paramURL, URL arg3)
    {
      this._width = paramURL;
      this._height = paramURL;
      URL localURL;
      if (localURL == null)
      {
        localURL = ResourceManager.class.getResource("image/java32.png");
        this._width = 24;
        this._height = 24;
      }
      try
      {
        if ((localURL != null) && (!isCached(localURL, null)))
          ImageLoader.getInstance().loadImage(localURL, this, true);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
    }

    private boolean isCached(URL paramURL, String paramString)
    {
      Image localImage = (Image)CacheObject.imageMap.get(getKey(paramURL, paramString));
      if (localImage != null)
      {
        setImage(localImage);
        return true;
      }
      return false;
    }

    private String getKey(URL paramURL, String paramString)
    {
      String str = "" + paramURL + "-w=" + this._width + "-h=" + this._height;
      if (paramString != null)
        str = str + "-version=" + paramString;
      return str;
    }

    public void imageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
    {
      int i = paramImage.getWidth(null);
      int j = paramImage.getHeight(null);
      Image localImage = paramImage;
      new Thread(new Runnable(localImage, i, j, paramURL, paramString)
      {
        private final Image val$imageIn;
        private final int val$w;
        private final int val$h;
        private final URL val$url;
        private final String val$version;

        public void run()
        {
          Image localImage = this.val$imageIn;
          if ((CacheObject.ViewerIcon.this._width > 0) && (CacheObject.ViewerIcon.this._height > 0) && ((CacheObject.ViewerIcon.this._width != this.val$w) || (CacheObject.ViewerIcon.this._height != this.val$h)))
            localImage = this.val$imageIn.getScaledInstance(CacheObject.ViewerIcon.this._width, CacheObject.ViewerIcon.this._height, 1);
          CacheObject.imageMap.put(CacheObject.ViewerIcon.this.getKey(this.val$url, this.val$version), localImage);
          CacheObject.ViewerIcon.this.setImage(localImage);
          CacheObject.this.tableModel.fireTableDataChanged();
        }
      }).start();
    }

    public void finalImageAvailable(URL paramURL, String paramString, Image paramImage, File paramFile)
    {
      imageAvailable(paramURL, paramString, paramImage, paramFile);
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.CacheObject
 * JD-Core Version:    0.6.0
 */