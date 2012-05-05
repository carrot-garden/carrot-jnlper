package com.sun.deploy.panel;

import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class UpdatePanel extends JPanel
  implements ActionListener
{
  private int update_notify = 0;
  private int update_frequency = 1;
  private int update_day = 1;
  private int update_schedule = 0;
  private String update_lastrun;
  private static final String update_image = "com/sun/deploy/resources/image/JavaUpdateIcon-48.png";
  private JButton updateNowBtn;
  private JButton advancedUpdateBtn;
  private JComboBox notifyComboBox;
  private JCheckBox autoUpdateChBox;
  private JSmartTextArea updateScheduleTextArea;
  private JSmartTextArea lastUpdatedTextArea;
  private String textDesc;
  private DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz", Locale.US);
  private String sysTrayIconText = getMessage("update.desc_systrayicon.text");
  private String[] weekDays;
  private String[] atComboBoxText;
  private boolean javaUpdateEnabled = false;
  private boolean javaUpdateEditPrefs = false;
  private boolean autoUpdateCheck = false;
  private boolean schedulerPlatform = true;
  private boolean manualUpdateEnabled = true;
  private boolean corporateOverride = false;

  UpdatePanel()
  {
    Platform.get().onLoad(this);
    setLayout(new BorderLayout());
    EmptyBorder localEmptyBorder = new EmptyBorder(new Insets(5, 5, 5, 5));
    TitledBorder localTitledBorder = new TitledBorder(new TitledBorder(new EtchedBorder()), getMessage("update.notify.border.text"), 0, 0);
    setBorder(BorderFactory.createCompoundBorder(localEmptyBorder, localTitledBorder));
    JPanel localJPanel1 = new JPanel();
    localJPanel1.setLayout(new BorderLayout());
    JPanel localJPanel2 = new JPanel();
    localJPanel2.setLayout(new BorderLayout());
    ImageIcon localImageIcon = new ImageIcon(ClassLoader.getSystemResource("com/sun/deploy/resources/image/JavaUpdateIcon-48.png"));
    JLabel localJLabel1 = new JLabel();
    localJLabel1.setIcon(localImageIcon);
    localJPanel2.add(localJLabel1, "North");
    localJPanel1.add(localJPanel2, "West");
    JPanel localJPanel3 = new JPanel();
    localJPanel3.setLayout(new BorderLayout());
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BorderLayout());
    localJPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    DateFormatSymbols localDateFormatSymbols = new DateFormatSymbols();
    DateFormat localDateFormat = DateFormat.getTimeInstance(3);
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("H:mm");
    this.weekDays = localDateFormatSymbols.getWeekdays();
    this.atComboBoxText = new String[24];
    for (int i = 0; i < 24; i++)
      try
      {
        Date localDate = localSimpleDateFormat.parse(i + ":00");
        this.atComboBoxText[i] = localDateFormat.format(localDate);
      }
      catch (Exception localException)
      {
        this.atComboBoxText[i] = (i + ":00");
      }
    JSmartTextArea localJSmartTextArea = new JSmartTextArea(getMessage("update.desc.text"));
    localJPanel4.add(localJSmartTextArea, "Center");
    JPanel localJPanel5 = new JPanel();
    localJPanel5.setLayout(new BorderLayout());
    localJPanel5.add(localJPanel4, "North");
    JPanel localJPanel6 = new JPanel();
    localJPanel6.setLayout(new MyBoxLayout(localJPanel6, 0));
    localJPanel6.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 8));
    JLabel localJLabel2 = new JLabel(getMessage("update.notify.text"));
    this.notifyComboBox = new UComboBox(null);
    this.notifyComboBox.addItem(getMessage("update.notify_download.text"));
    this.notifyComboBox.addItem(getMessage("update.notify_install.text"));
    this.notifyComboBox.addActionListener(this);
    this.notifyComboBox.setToolTipText(getMessage("update.notify_combo.tooltip"));
    localJPanel6.add(localJLabel2);
    localJPanel6.add(Box.createGlue());
    localJPanel6.add(this.notifyComboBox);
    localJPanel5.add(localJPanel6, "Center");
    JPanel localJPanel7 = new JPanel();
    localJPanel7.setLayout(new MyBoxLayout(localJPanel7, 0));
    localJPanel7.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 8));
    this.autoUpdateChBox = new JCheckBox(getMessage("update.autoupdate.text"));
    this.autoUpdateChBox.addActionListener(this);
    this.autoUpdateChBox.setEnabled(!this.corporateOverride);
    this.advancedUpdateBtn = new JButton(getMessage("update.advanced.button.text"));
    this.advancedUpdateBtn.setMnemonic(ResourceManager.getVKCode("update.advanced.button.mnemonic"));
    this.advancedUpdateBtn.setToolTipText(getMessage("update.advanced_btn.tooltip"));
    this.advancedUpdateBtn.addActionListener(this);
    localJPanel7.add(this.autoUpdateChBox);
    localJPanel7.add(Box.createGlue());
    localJPanel7.add(this.advancedUpdateBtn);
    localJPanel5.add(localJPanel7, "South");
    localJPanel3.add(localJPanel5, "North");
    JPanel localJPanel8 = new JPanel();
    localJPanel8.setLayout(new GridLayout(1, 1));
    localJPanel8.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    this.updateScheduleTextArea = new JSmartTextArea(" ");
    localJPanel8.add(this.updateScheduleTextArea);
    JPanel localJPanel9 = new JPanel();
    localJPanel9.setLayout(new GridLayout(1, 1));
    localJPanel9.add(localJPanel8);
    localJPanel3.add(localJPanel9, "Center");
    BorderLayout localBorderLayout = new BorderLayout();
    localBorderLayout.setHgap(8);
    JPanel localJPanel10 = new JPanel(localBorderLayout);
    localJPanel10.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    this.lastUpdatedTextArea = new JSmartTextArea(" ");
    this.updateNowBtn = new JButton(getMessage("update.updatenow.button.text"));
    this.updateNowBtn.setMnemonic(ResourceManager.getVKCode("update.updatenow.button.mnemonic"));
    this.updateNowBtn.setToolTipText(getMessage("update.now_btn.tooltip"));
    this.updateNowBtn.addActionListener(this);
    this.updateNowBtn.setEnabled(this.manualUpdateEnabled);
    localJPanel10.add(this.lastUpdatedTextArea, "Center");
    localJPanel10.add(this.updateNowBtn, "East");
    localJPanel3.add(localJPanel10, "South");
    localJPanel1.add(localJPanel3, "Center");
    add(localJPanel1, "Center");
    reset();
  }

  public void reset()
  {
    if (isSchedulerPlatform())
      this.autoUpdateChBox.setSelected(isAutoUpdateChecked());
    else
      this.autoUpdateChBox.setEnabled(false);
    this.notifyComboBox.setSelectedIndex(getUpdateNotify());
    setText();
    String str1 = getUpdateLastRun();
    GregorianCalendar localGregorianCalendar = new GregorianCalendar();
    try
    {
      localGregorianCalendar.setTime(this.df.parse(str1));
    }
    catch (Exception localException)
    {
      str1 = null;
    }
    if (str1 == null)
    {
      str1 = new String("    ");
    }
    else
    {
      DateFormat localDateFormat1 = DateFormat.getTimeInstance(3);
      DateFormat localDateFormat2 = DateFormat.getDateInstance(3);
      MessageFormat localMessageFormat = new MessageFormat(getMessage("update.lastrun.text"));
      String str2 = localDateFormat1.format(localGregorianCalendar.getTime());
      String str3 = localDateFormat2.format(localGregorianCalendar.getTime());
      str1 = localMessageFormat.format(new String[] { str2, str3 });
    }
    this.lastUpdatedTextArea.setText(str1);
  }

  private void setText()
  {
    if (!this.autoUpdateChBox.isSelected())
    {
      this.advancedUpdateBtn.setEnabled(false);
      this.textDesc = getMessage("update.desc_autooff.text");
      this.updateScheduleTextArea.setText(this.textDesc);
    }
    else
    {
      this.advancedUpdateBtn.setEnabled(true);
      int i = getUpdateFrequency();
      String str1 = this.atComboBoxText[getUpdateSchedule()];
      String str2 = null;
      MessageFormat localMessageFormat;
      if (i == 0)
      {
        this.textDesc = getMessage("update.desc_check_daily.text");
        localMessageFormat = new MessageFormat(this.textDesc);
        this.textDesc = localMessageFormat.format(new String[] { str1 });
      }
      else if (i == 2)
      {
        this.textDesc = getMessage("update.desc_check_monthly.text");
        str2 = String.valueOf(this.weekDays[getUpdateDay()]);
      }
      else
      {
        this.textDesc = getMessage("update.desc_check_weekly.text");
        str2 = String.valueOf(this.weekDays[getUpdateDay()]);
      }
      if (str2 != null)
      {
        localMessageFormat = new MessageFormat(this.textDesc);
        this.textDesc = localMessageFormat.format(new String[] { str2, str1 });
      }
      this.textDesc += this.sysTrayIconText;
      i = this.notifyComboBox.getSelectedIndex();
      if (i == 1)
        this.textDesc += getMessage("update.desc_notify_install.text");
      else
        this.textDesc += getMessage("update.desc_notify_download.text");
      if (getUpdateFrequency() == 2)
        this.textDesc = (this.textDesc + "\n\n" + getMessage("update.desc_check_monthly_2.text"));
      this.updateScheduleTextArea.setText(this.textDesc);
    }
    if (!this.javaUpdateEditPrefs)
    {
      this.autoUpdateChBox.setEnabled(false);
      this.advancedUpdateBtn.setEnabled(false);
      this.notifyComboBox.setEnabled(false);
    }
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    try
    {
      if (paramActionEvent.getSource() == this.updateNowBtn)
      {
        String str = Platform.get().getJucheckPath();
        if (str == null)
          throw new RuntimeException("Can not launch update check.  Path to jucheck.exe returned null.");
        if (!new File(str).exists())
          throw new RuntimeException("Can not launch update check.  jucheck.exe dosn't exist at: " + str);
        try
        {
          Runtime.getRuntime().exec(str);
        }
        catch (IOException localIOException)
        {
          Trace.ignored(localIOException);
          String[] arrayOfString = new String[3];
          arrayOfString[0] = "cmd";
          arrayOfString[1] = "/c";
          arrayOfString[2] = str;
          Runtime.getRuntime().exec(arrayOfString);
        }
      }
      else if (paramActionEvent.getSource() == this.advancedUpdateBtn)
      {
        new AdvancedDialog(this);
      }
      else if (paramActionEvent.getSource() == this.notifyComboBox)
      {
        int i = this.notifyComboBox.getSelectedIndex();
        setUpdateNotify(i);
        setText();
        ControlPanel.propertyHasChanged();
      }
      else if (paramActionEvent.getSource() == this.autoUpdateChBox)
      {
        boolean bool = this.autoUpdateChBox.isSelected();
        setAutoUpdateCheck(bool);
        setText();
        ControlPanel.propertyHasChanged();
      }
    }
    catch (Exception localException)
    {
      ToolkitStore.getUI().showExceptionDialog(null, null, localException, null, null, null, null);
    }
  }

  public String[] getAtComboBoxText()
  {
    return this.atComboBoxText;
  }

  public String[] getWeekDays()
  {
    return this.weekDays;
  }

  public void setManualUpdate(boolean paramBoolean)
  {
    this.manualUpdateEnabled = paramBoolean;
  }

  public void setCorporateOverride(boolean paramBoolean)
  {
    this.corporateOverride = paramBoolean;
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public void saveUpdateSettingsInReg()
  {
    Platform.get().onSave(this);
  }

  public void setUpdateNotify(int paramInt)
  {
    this.update_notify = paramInt;
  }

  public int getUpdateNotify()
  {
    return this.update_notify;
  }

  public void setUpdateFrequency(int paramInt)
  {
    this.update_frequency = paramInt;
  }

  public void setUpdateDay(int paramInt)
  {
    if ((paramInt >= 0) && (paramInt <= 7))
      this.update_day = paramInt;
  }

  public int getUpdateFrequency()
  {
    return this.update_frequency;
  }

  public int getUpdateDay()
  {
    return this.update_day;
  }

  public void setUpdateSchedule(int paramInt)
  {
    this.update_schedule = paramInt;
  }

  public int getUpdateSchedule()
  {
    return this.update_schedule;
  }

  public void setUpdateLastRun(String paramString)
  {
    this.update_lastrun = paramString;
  }

  public String getUpdateLastRun()
  {
    return this.update_lastrun;
  }

  public void enableJavaUpdate(boolean paramBoolean)
  {
    this.javaUpdateEnabled = paramBoolean;
  }

  public boolean isJavaUpdateEnabled()
  {
    return this.javaUpdateEnabled;
  }

  public void enableEditPrefs(boolean paramBoolean)
  {
    this.javaUpdateEditPrefs = paramBoolean;
  }

  public void setAutoUpdateCheck(boolean paramBoolean)
  {
    ToolkitStore.getUI();
    int i = 1;
    if ((!paramBoolean) && (paramBoolean != this.autoUpdateCheck))
    {
      String str1 = ResourceManager.getMessage("update.autoupdate.disable.neverCheck");
      String str2 = ResourceManager.getMessage("update.warning");
      String str3 = ResourceManager.getMessage("update.autoupdate.disable.message");
      String str4 = ResourceManager.getMessage("update.autoupdate.disable.info");
      String str5 = ResourceManager.getMessage("update.autoupdate.disable.monthlyCheck");
      if (getUpdateFrequency() == 0)
        str5 = ResourceManager.getMessage("update.autoupdate.disable.dailyCheck");
      else if (getUpdateFrequency() == 1)
        str5 = ResourceManager.getMessage("update.autoupdate.disable.weeklyCheck");
      ToolkitStore.getUI();
      i = ToolkitStore.getUI().showMessageDialog(null, null, 2, str2, str3, str4, null, str5, str1, null);
    }
    ToolkitStore.getUI();
    if (i == 0)
      this.autoUpdateChBox.setSelected(true);
    else
      this.autoUpdateCheck = paramBoolean;
  }

  public boolean isAutoUpdateChecked()
  {
    return this.autoUpdateCheck;
  }

  public void setSchedulerPlatform(boolean paramBoolean)
  {
    this.schedulerPlatform = paramBoolean;
  }

  public boolean isSchedulerPlatform()
  {
    return this.schedulerPlatform;
  }

  private class AdvancedDialog
    implements ActionListener
  {
    ChangeListener changeListener = new ChangeListener()
    {
      public void stateChanged(ChangeEvent paramChangeEvent)
      {
      }
    };
    private UpdatePanel updPanel;
    private JRadioButton RButton1;
    private JRadioButton RButton2;
    private JRadioButton RButton3;
    private JRadioButton RButtonLast = null;
    private ButtonGroup FreqGroup;
    private String[] weekDays;
    private String[] atComboBoxText;
    private JComboBox at;
    private JComboBox every;
    private String atLabelText;
    private String everyLabelText;
    private String dayLabelText;
    private JLabel whenLabel1;
    private JLabel whenLabel2;
    private JLabel dummyLabel;
    private JSmartTextArea descTextArea;
    JPanel whenPanel1;
    JPanel whenPanel2;
    JPanel whenPanel;
    GridBagConstraints c;

    public AdvancedDialog(UpdatePanel arg2)
    {
      UpdatePanel localUpdatePanel;
      this.updPanel = localUpdatePanel;
      JPanel localJPanel1 = new JPanel();
      localJPanel1.setLayout(new GridBagLayout());
      this.c = new GridBagConstraints();
      this.c.anchor = 17;
      this.c.fill = 0;
      this.c.insets = new Insets(5, 0, 0, 0);
      this.c.weighty = 1.0D;
      this.c.weightx = 0.0D;
      this.c.gridwidth = 0;
      localJPanel1.add(new JLabel(localUpdatePanel.getMessage("update.advanced_title1.text")), this.c);
      JPanel localJPanel2 = new JPanel();
      localJPanel2.setLayout(new BoxLayout(localJPanel2, 1));
      Border localBorder = BorderFactory.createEtchedBorder();
      localJPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createTitledBorder(localBorder, localUpdatePanel.getMessage("update.advanced_title2.text"))));
      this.RButton1 = new JRadioButton(localUpdatePanel.getMessage("update.check_daily.text"));
      this.RButton2 = new JRadioButton(localUpdatePanel.getMessage("update.check_weekly.text"));
      this.RButton3 = new JRadioButton(localUpdatePanel.getMessage("update.check_monthly.text"));
      this.FreqGroup = new ButtonGroup();
      this.FreqGroup.add(this.RButton1);
      this.FreqGroup.add(this.RButton2);
      this.FreqGroup.add(this.RButton3);
      this.RButton1.addActionListener(this);
      this.RButton2.addActionListener(this);
      this.RButton3.addActionListener(this);
      localJPanel2.add(this.RButton1);
      localJPanel2.add(this.RButton2);
      localJPanel2.add(this.RButton3);
      this.c.weighty = 3.0D;
      this.c.weightx = 0.0D;
      this.c.gridwidth = 2;
      this.c.insets = new Insets(0, 0, 0, 0);
      localJPanel1.add(localJPanel2, this.c);
      this.whenPanel = new JPanel();
      this.whenPanel.setLayout(new GridBagLayout());
      this.whenPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createTitledBorder(localBorder, localUpdatePanel.getMessage("update.advanced_title3.text"))));
      this.atComboBoxText = localUpdatePanel.getAtComboBoxText();
      this.weekDays = localUpdatePanel.getWeekDays();
      this.dummyLabel = new JLabel("    ");
      this.everyLabelText = localUpdatePanel.getMessage("update.check_day.text");
      this.atLabelText = localUpdatePanel.getMessage("update.check_time.text");
      this.dayLabelText = localUpdatePanel.getMessage("update.check_date.text");
      this.whenLabel1 = new JLabel();
      this.whenLabel2 = new JLabel();
      this.every = new UpdatePanel.UComboBox(UpdatePanel.this, null);
      this.every.setMaximumRowCount(7);
      this.every.addItem(this.weekDays[1]);
      this.every.addItem(this.weekDays[2]);
      this.every.addItem(this.weekDays[3]);
      this.every.addItem(this.weekDays[4]);
      this.every.addItem(this.weekDays[5]);
      this.every.addItem(this.weekDays[6]);
      this.every.addItem(this.weekDays[7]);
      this.every.addActionListener(this);
      this.at = new UpdatePanel.UComboBox(UpdatePanel.this, null);
      this.at.setMaximumRowCount(this.atComboBoxText.length);
      for (int i = 0; i < this.atComboBoxText.length; i++)
        this.at.addItem(this.atComboBoxText[i]);
      this.at.addActionListener(this);
      this.c.anchor = 17;
      this.whenPanel1 = new JPanel();
      this.whenPanel2 = new JPanel();
      this.c.insets = new Insets(0, 10, 0, 5);
      this.c.gridwidth = 2;
      this.c.gridheight = 2;
      this.c.weighty = 0.0D;
      this.c.weightx = 0.0D;
      this.whenPanel.add(this.whenLabel1, this.c);
      this.c.gridwidth = 0;
      this.c.weightx = 2.0D;
      this.c.fill = 2;
      this.whenPanel.add(this.whenPanel1, this.c);
      this.c.gridwidth = 2;
      this.c.fill = 0;
      this.c.weightx = 0.0D;
      this.c.gridheight = 0;
      this.whenPanel.add(this.whenLabel2, this.c);
      this.c.weightx = 1.0D;
      this.c.fill = 2;
      this.c.gridwidth = 0;
      this.whenPanel.add(this.whenPanel2, this.c);
      this.c.weightx = 2.0D;
      this.c.weighty = 3.0D;
      this.c.fill = 1;
      this.c.gridheight = -1;
      this.c.insets = new Insets(0, 5, 0, 0);
      this.c.gridwidth = 0;
      localJPanel1.add(this.whenPanel, this.c);
      if (localUpdatePanel.getUpdateFrequency() == 0)
      {
        this.RButton1.setSelected(true);
      }
      else if (localUpdatePanel.getUpdateFrequency() == 2)
      {
        this.RButton3.setSelected(true);
        this.every.setSelectedIndex(localUpdatePanel.getUpdateDay() - 1);
      }
      else
      {
        this.RButton2.setSelected(true);
        this.every.setSelectedIndex(localUpdatePanel.getUpdateDay() - 1);
      }
      this.at.setSelectedIndex(localUpdatePanel.getUpdateSchedule());
      this.descTextArea = new JSmartTextArea("  ");
      this.descTextArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createTitledBorder(localBorder, "")));
      this.c.fill = 2;
      this.c.weightx = 1.0D;
      this.c.weighty = 1.0D;
      this.c.insets = new Insets(0, 0, 0, 0);
      this.c.gridwidth = 0;
      localJPanel1.add(this.descTextArea, this.c);
      setPanelOptions();
      JOptionPane localJOptionPane = new JOptionPane(localJPanel1, -1, 2);
      JDialog localJDialog = localJOptionPane.createDialog(localUpdatePanel, localUpdatePanel.getMessage("update.advanced_title.text"));
      localJDialog.setModal(true);
      localJDialog.setDefaultCloseOperation(2);
      localJDialog.setVisible(true);
      Integer localInteger = (Integer)localJOptionPane.getValue();
      if ((localInteger != null) && (localInteger.intValue() == 0))
      {
        localUpdatePanel.setUpdateSchedule(this.at.getSelectedIndex());
        if (this.RButton1.isSelected())
        {
          localUpdatePanel.setUpdateFrequency(0);
          localUpdatePanel.setUpdateDay(0);
        }
        else if (this.RButton3.isSelected())
        {
          localUpdatePanel.setUpdateFrequency(2);
          localUpdatePanel.setUpdateDay(this.every.getSelectedIndex() + 1);
        }
        else
        {
          localUpdatePanel.setUpdateFrequency(1);
          localUpdatePanel.setUpdateDay(this.every.getSelectedIndex() + 1);
        }
        ControlPanel.propertyHasChanged();
      }
      localUpdatePanel.setText();
    }

    void setPanelOptions()
    {
      String str1 = null;
      String str2 = null;
      MessageFormat localMessageFormat;
      if (this.RButton1.isSelected())
      {
        if (!this.RButton1.equals(this.RButtonLast))
        {
          this.every.setVisible(false);
          this.whenLabel1.setText("  ");
          this.whenLabel2.setText(this.atLabelText);
          this.whenPanel1.add(this.dummyLabel);
          this.whenPanel2.add(this.at);
          this.at.setVisible(true);
          this.dummyLabel.setVisible(true);
          this.RButtonLast = this.RButton1;
        }
        str1 = this.updPanel.getMessage("update.advanced_desc1.text");
        localMessageFormat = new MessageFormat(str1);
        str1 = localMessageFormat.format(new String[] { (String)this.at.getItemAt(this.at.getSelectedIndex()) });
      }
      else if (this.RButton2.isSelected())
      {
        if (!this.RButton2.equals(this.RButtonLast))
        {
          this.dummyLabel.setVisible(false);
          this.whenLabel1.setText(this.everyLabelText);
          this.whenLabel2.setText(this.atLabelText);
          this.whenPanel1.add(this.every);
          this.whenPanel2.add(this.at);
          this.every.setVisible(true);
          this.at.setVisible(true);
          this.RButtonLast = this.RButton2;
        }
        str2 = (String)this.every.getItemAt(this.every.getSelectedIndex());
        str1 = this.updPanel.getMessage("update.advanced_desc2.text");
      }
      else if (this.RButton3.isSelected())
      {
        if (!this.RButton3.equals(this.RButtonLast))
        {
          this.dummyLabel.setVisible(false);
          this.whenLabel1.setText(this.everyLabelText);
          this.whenLabel2.setText(this.atLabelText);
          this.whenPanel1.add(this.every);
          this.whenPanel2.add(this.at);
          this.every.setVisible(true);
          this.at.setVisible(true);
          this.RButtonLast = this.RButton3;
        }
        str2 = (String)this.every.getItemAt(this.every.getSelectedIndex());
        str1 = this.updPanel.getMessage("update.advanced_desc3.text");
      }
      if (str2 != null)
      {
        localMessageFormat = new MessageFormat(str1);
        str1 = localMessageFormat.format(new String[] { str2, (String)this.at.getItemAt(this.at.getSelectedIndex()) });
      }
      this.descTextArea.setText(str1);
    }

    public void actionPerformed(ActionEvent paramActionEvent)
    {
      try
      {
        if ((paramActionEvent.getSource() == this.RButton1) || (paramActionEvent.getSource() == this.RButton2) || (paramActionEvent.getSource() == this.RButton3) || (paramActionEvent.getSource() == this.every) || (paramActionEvent.getSource() == this.at))
          setPanelOptions();
      }
      catch (Exception localException)
      {
      }
    }
  }

  private class MyBoxLayout extends BoxLayout
  {
    public MyBoxLayout(Container paramInt, int arg3)
    {
      super(i);
    }

    public Dimension preferredLayoutSize(Container paramContainer)
    {
      int i = paramContainer.getWidth();
      Dimension localDimension = super.preferredLayoutSize(paramContainer);
      if ((i > 0) && (localDimension.width > i))
      {
        Component[] arrayOfComponent = paramContainer.getComponents();
        paramContainer.removeAll();
        paramContainer.setLayout(new BorderLayout());
        if (arrayOfComponent.length > 0)
          paramContainer.add(arrayOfComponent[0], "North");
        if (arrayOfComponent.length > 2)
          paramContainer.add(arrayOfComponent[2], "East");
        return paramContainer.getLayout().preferredLayoutSize(paramContainer);
      }
      return localDimension;
    }
  }

  private class UComboBox extends JComboBox
  {
    private final UpdatePanel this$0;

    private UComboBox()
    {
      this.this$0 = this$1;
    }

    public Dimension getPreferredSize()
    {
      Dimension localDimension = super.getPreferredSize();
      localDimension.width += 8;
      return localDimension;
    }

    UComboBox(UpdatePanel.1 arg2)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.UpdatePanel
 * JD-Core Version:    0.6.0
 */