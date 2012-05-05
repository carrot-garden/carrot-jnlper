package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.Platform;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.ui.DialogTemplate;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.deploy.uitoolkit.ui.UIFactory;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

public class CacheSettingsDialog extends JDialog
{
  private final int DIALOG_WIDTH_UNIX = 510;
  private final int DIALOG_WIDTH_WIN = 470;
  private final int VERTICAL_STRUT_UNIX = 12;
  private final int VERTICAL_STRUT_WIN = 6;
  private final int CACHE_MIN_SIZE = 1;
  private final int CACHE_MAX_SIZE = 32768;
  private JTextField location;
  private JLabel locLbl;
  private JLabel diskSpaceLbl;
  private JLabel compressionLbl;
  private JLabel unitsLbl;
  private JButton chooseBtn;
  private JButton okBtn;
  private JButton cancelBtn;
  private JButton deleteFilesBtn;
  private JButton restoreDefaultsBtn;
  private JComboBox compression;
  private JSlider cacheSizeSlider;
  private JSpinner cacheSizeSpinner;
  private JCheckBox cacheEnabled;
  private int dialogWidth = 510;
  private int verticalStrut = 12;

  public CacheSettingsDialog(Frame paramFrame, boolean paramBoolean)
  {
    super(paramFrame, paramBoolean);
    if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
    {
      this.dialogWidth = 470;
      this.verticalStrut = 6;
    }
    initComponents();
  }

  private void initComponents()
  {
    setTitle(getMessage("cache.settings.dialog.title"));
    JPanel localJPanel1 = new JPanel(new FlowLayout(3, 0, 12));
    this.cacheEnabled = new JCheckBox(getMessage("cache.settings.dialog.cacheEnabled"));
    this.cacheEnabled.setMnemonic(ResourceManager.getVKCode("cache.settings.dialog.cacheEnabled.mnemonic"));
    this.cacheEnabled.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent paramItemEvent)
      {
        CacheSettingsDialog.this.checkboxStateChanged(paramItemEvent);
      }
    });
    localJPanel1.add(Box.createHorizontalStrut(12));
    localJPanel1.add(this.cacheEnabled);
    JPanel localJPanel2 = new JPanel(new BorderLayout());
    localJPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 12, this.verticalStrut, 12), BorderFactory.createTitledBorder(getMessage("cache.settings.dialog.cache_location"))));
    this.locLbl = new JLabel(getMessage("cache.settings.dialog.location_label"));
    JPanel localJPanel3 = new JPanel(new FlowLayout(3, 0, 5));
    localJPanel3.add(Box.createHorizontalStrut(24));
    localJPanel3.add(this.locLbl);
    JPanel localJPanel4 = new JPanel();
    localJPanel4.setLayout(new BoxLayout(localJPanel4, 0));
    this.location = new JTextField();
    this.location.setPreferredSize(new Dimension(10, this.location.getPreferredSize().height));
    this.chooseBtn = makeButton("cache.settings.dialog.change_btn");
    this.chooseBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheSettingsDialog.this.chooseButtonActionPerformed(paramActionEvent);
      }
    });
    this.chooseBtn.setToolTipText(getMessage("cache.settings.dialog.change_btn.tooltip"));
    localJPanel4.add(Box.createHorizontalStrut(24));
    localJPanel4.add(this.location);
    localJPanel4.add(Box.createHorizontalStrut(12));
    localJPanel4.add(this.chooseBtn);
    localJPanel4.add(Box.createHorizontalStrut(12));
    localJPanel2.add(localJPanel3, "North");
    localJPanel2.add(localJPanel4, "Center");
    localJPanel2.add(Box.createVerticalStrut(this.verticalStrut), "South");
    JPanel localJPanel5 = new JPanel();
    localJPanel5.setLayout(new BorderLayout());
    localJPanel5.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 12, this.verticalStrut, 12), BorderFactory.createTitledBorder(getMessage("cache.settings.dialog.disk_space"))));
    this.compressionLbl = new JLabel(getMessage("cache.settings.dialog.compression"));
    String[] arrayOfString = { getMessage("cache.settings.dialog.none"), getMessage("cache.settings.dialog.low"), getMessage("cache.settings.dialog.medium"), getMessage("cache.settings.dialog.high") };
    this.compression = new JComboBox(arrayOfString);
    JPanel localJPanel6 = new JPanel();
    localJPanel6.setLayout(new BoxLayout(localJPanel6, 0));
    localJPanel6.setBorder(BorderFactory.createEmptyBorder(0, 0, this.verticalStrut, 0));
    localJPanel6.add(Box.createHorizontalStrut(24));
    localJPanel6.add(this.compressionLbl);
    localJPanel6.add(Box.createHorizontalStrut(12));
    localJPanel6.add(Box.createHorizontalGlue());
    localJPanel6.add(this.compression);
    this.diskSpaceLbl = new JLabel(getMessage("cache.settings.dialog.diskSpaceLbl"));
    JPanel localJPanel7 = new JPanel();
    localJPanel7.setLayout(new BoxLayout(localJPanel7, 0));
    localJPanel7.setBorder(BorderFactory.createEmptyBorder(0, 0, this.verticalStrut, 0));
    localJPanel7.add(Box.createHorizontalStrut(24));
    localJPanel7.add(this.diskSpaceLbl);
    localJPanel7.add(Box.createHorizontalGlue());
    localJPanel7.add(Box.createHorizontalStrut(12));
    this.cacheSizeSlider = new JSlider(1, 32768);
    this.cacheSizeSlider.setMinorTickSpacing(1638);
    this.cacheSizeSlider.setPaintTicks(true);
    this.cacheSizeSlider.setPaintLabels(false);
    this.cacheSizeSlider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent paramChangeEvent)
      {
        CacheSettingsDialog.this.sliderStateChanged(paramChangeEvent);
      }
    });
    this.cacheSizeSpinner = new JSpinner(new SpinnerNumberModel(32768, 1, 32768, 1));
    JSpinner.NumberEditor localNumberEditor = new JSpinner.NumberEditor(this.cacheSizeSpinner, "######");
    localNumberEditor.getTextField().setColumns(8);
    localNumberEditor.getTextField().setHorizontalAlignment(11);
    ((NumberFormatter)localNumberEditor.getTextField().getFormatter()).setCommitsOnValidEdit(true);
    ((NumberFormatter)localNumberEditor.getTextField().getFormatter()).setAllowsInvalid(false);
    this.cacheSizeSpinner.setEditor(localNumberEditor);
    this.cacheSizeSpinner.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent paramChangeEvent)
      {
        CacheSettingsDialog.this.updateSlider();
      }
    });
    this.unitsLbl = new JLabel("MB");
    int i = this.unitsLbl.getPreferredSize().width + 12 + 12;
    localJPanel6.add(Box.createHorizontalStrut(i));
    JPanel localJPanel8 = new JPanel();
    localJPanel8.setLayout(new BoxLayout(localJPanel8, 0));
    localJPanel8.setBorder(BorderFactory.createEmptyBorder(0, 0, this.verticalStrut, 0));
    JPanel localJPanel9 = new JPanel();
    localJPanel9.setLayout(new FlowLayout(4, 0, 5));
    localJPanel9.add(this.cacheSizeSpinner);
    localJPanel8.add(Box.createHorizontalStrut(24));
    localJPanel8.add(this.cacheSizeSlider);
    localJPanel8.add(Box.createHorizontalStrut(12));
    localJPanel8.add(localJPanel9);
    localJPanel8.add(Box.createHorizontalStrut(12));
    localJPanel8.add(this.unitsLbl);
    localJPanel8.add(Box.createHorizontalStrut(12));
    localJPanel5.add(localJPanel6, "North");
    localJPanel5.add(localJPanel7, "Center");
    localJPanel5.add(localJPanel8, "South");
    JPanel localJPanel10 = new JPanel();
    localJPanel10.setLayout(new FlowLayout(4, 6, 5));
    localJPanel10.setBorder(BorderFactory.createEmptyBorder(0, 12, this.verticalStrut, 12));
    this.deleteFilesBtn = makeButton("cache.settings.dialog.delete_btn");
    this.deleteFilesBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheSettingsDialog.this.deleteFilesBtnActionPerformed(paramActionEvent);
      }
    });
    this.deleteFilesBtn.setToolTipText(getMessage("temp.files.delete.btn.tooltip"));
    this.restoreDefaultsBtn = makeButton("cache.settings.dialog.restore_btn");
    this.restoreDefaultsBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheSettingsDialog.this.restoreDefaultsBtnActionPerformed(paramActionEvent);
      }
    });
    this.restoreDefaultsBtn.setToolTipText(getMessage("cache.settings.dialog.restore_btn.tooltip"));
    DialogTemplate.resizeButtons(new JButton[] { this.deleteFilesBtn, this.restoreDefaultsBtn });
    localJPanel10.add(this.deleteFilesBtn);
    localJPanel10.add(Box.createHorizontalGlue());
    localJPanel10.add(Box.createHorizontalGlue());
    localJPanel10.add(this.restoreDefaultsBtn);
    JPanel localJPanel11 = new JPanel();
    localJPanel11.setLayout(new BorderLayout());
    localJPanel11.add(localJPanel2, "North");
    localJPanel11.add(localJPanel5, "Center");
    localJPanel11.add(localJPanel10, "South");
    this.okBtn = new JButton(getMessage("common.ok_btn"));
    this.okBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheSettingsDialog.this.okBtnActionPerformed(paramActionEvent);
      }
    });
    8 local8 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheSettingsDialog.this.cancelBtnActionPerformed(paramActionEvent);
      }
    };
    this.cancelBtn = new JButton(getMessage("common.cancel_btn"));
    this.cancelBtn.addActionListener(local8);
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", local8);
    DialogTemplate.resizeButtons(new JButton[] { this.okBtn, this.cancelBtn });
    JPanel localJPanel12 = new JPanel();
    localJPanel12.setBorder(BorderFactory.createEmptyBorder(this.verticalStrut, 12, this.verticalStrut, 12));
    localJPanel12.setLayout(new FlowLayout(2, 0, 0));
    localJPanel12.add(Box.createHorizontalGlue());
    localJPanel12.add(this.okBtn);
    localJPanel12.add(Box.createHorizontalStrut(6));
    localJPanel12.add(this.cancelBtn);
    JPanel localJPanel13 = new JPanel();
    localJPanel13.setLayout(new BorderLayout());
    JSeparator localJSeparator = new JSeparator();
    localJSeparator.setPreferredSize(new Dimension(this.dialogWidth, 1));
    localJPanel13.add(localJSeparator, "North");
    localJPanel13.add(localJPanel12, "Center");
    getContentPane().add(localJPanel1, "North");
    getContentPane().add(localJPanel11, "Center");
    getContentPane().add(localJPanel13, "South");
    getRootPane().setDefaultButton(this.okBtn);
    pack();
    setResizable(false);
    initValues();
  }

  public void initValues()
  {
    this.location.setText(Config.getCacheDirectory());
    this.location.setToolTipText(this.location.getText());
    this.location.setHorizontalAlignment(2);
    boolean bool1 = (!Config.get().isPropertyLocked("deployment.user.cachedir")) && (!Platform.get().isPlatformWindowsVista());
    this.location.setEnabled(bool1);
    this.location.setEditable(false);
    this.chooseBtn.setEnabled(bool1);
    int i = Config.getIntProperty("deployment.cache.max.size");
    bool1 = !Config.get().isPropertyLocked("deployment.cache.max.size");
    if (i == -1)
    {
      ((JSpinner.NumberEditor)this.cacheSizeSpinner.getEditor()).getTextField().setValue(Integer.valueOf(32768));
      this.cacheSizeSlider.setValue(32768);
    }
    else
    {
      ((JSpinner.NumberEditor)this.cacheSizeSpinner.getEditor()).getTextField().setValue(Integer.valueOf(i));
      this.cacheSizeSlider.setValue(i);
    }
    if (!bool1)
    {
      this.cacheSizeSlider.setEnabled(bool1);
      this.cacheSizeSpinner.setEnabled(bool1);
    }
    bool1 = !Config.get().isPropertyLocked("deployment.cache.jarcompression");
    int j = Config.getIntProperty("deployment.cache.jarcompression");
    if (j / 3 < this.compression.getItemCount())
      this.compression.setSelectedIndex(j / 3);
    else
      this.compression.setSelectedIndex(0);
    this.compression.setEnabled(bool1);
    bool1 = !Config.get().isPropertyLocked("deployment.cache.enabled");
    boolean bool2 = Config.getBooleanProperty("deployment.cache.enabled");
    this.cacheEnabled.setSelected(bool2);
    this.cacheEnabled.setEnabled(bool1);
    setAllEnabled(bool2);
  }

  private void restoreDefaultsBtnActionPerformed(ActionEvent paramActionEvent)
  {
    if (!Config.get().isPropertyLocked("deployment.user.cachedir"))
    {
      Config.get();
      this.location.setText(Config.getDefaultCacheDirectory());
      this.location.setToolTipText(this.location.getText());
    }
    if (!Config.get().isPropertyLocked("deployment.cache.max.size"))
    {
      ((JSpinner.NumberEditor)this.cacheSizeSpinner.getEditor()).getTextField().setValue(Integer.valueOf(32768));
      this.cacheSizeSlider.setValue(32768);
    }
    if (!Config.get().isPropertyLocked("deployment.cache.jarcompression"))
    {
      int i = 0;
      if (i / 3 < this.compression.getItemCount())
        this.compression.setSelectedIndex(i / 3);
      else
        this.compression.setSelectedIndex(0);
    }
    if (!Config.get().isPropertyLocked("deployment.cache.enabled"))
    {
      this.cacheEnabled.setSelected(true);
      setAllEnabled(this.cacheEnabled.isSelected());
    }
  }

  private void chooseButtonActionPerformed(ActionEvent paramActionEvent)
  {
    JFileChooser localJFileChooser = new JFileChooser();
    localJFileChooser.setFileSelectionMode(1);
    localJFileChooser.setDialogTitle(getMessage("cache.settings.dialog.chooser_title"));
    localJFileChooser.setApproveButtonText(getMessage("cache.settings.dialog.select"));
    String str1 = getMessage("cache.settings.dialog.select_tooltip");
    localJFileChooser.setApproveButtonToolTipText(str1);
    int i = ResourceManager.getVKCode("cache.settings.dialog.select_mnemonic");
    localJFileChooser.setApproveButtonMnemonic(i);
    File localFile = new File(this.location.getText());
    localJFileChooser.setCurrentDirectory(localFile);
    if (localJFileChooser.showDialog(this, null) == 0)
    {
      String str2;
      try
      {
        str2 = localJFileChooser.getSelectedFile().getCanonicalPath();
      }
      catch (IOException localIOException)
      {
        str2 = localJFileChooser.getSelectedFile().getPath();
      }
      if (new File(str2).isDirectory())
      {
        this.location.setText(str2);
        this.location.setToolTipText(str2);
      }
      else
      {
        String str3 = getMessage("cache.settings.dialog.chooser_title");
        String str4 = getMessage("cache.settings.dialog.directory_masthead");
        String str5 = getMessage("cache.settings.dialog.directory_body");
        String str6 = ResourceManager.getString("common.ok_btn");
        String str7 = ResourceManager.getString("common.detail.button");
        ToolkitStore.getUI();
        ToolkitStore.getUI().showMessageDialog(this, new AppInfo(), 0, str3, str4, str5, null, str6, str7, null);
      }
    }
  }

  private void deleteFilesBtnActionPerformed(ActionEvent paramActionEvent)
  {
    new DeleteFilesDialog(this);
  }

  private void okBtnActionPerformed(ActionEvent paramActionEvent)
  {
    Config.setCacheDirectory(this.location.getText());
    if (this.cacheSizeSlider.getValue() == 32768)
      Config.setIntProperty("deployment.cache.max.size", -1);
    else
      Config.setIntProperty("deployment.cache.max.size", this.cacheSizeSlider.getValue());
    Config.setIntProperty("deployment.cache.jarcompression", this.compression.getSelectedIndex() * 3);
    Config.setBooleanProperty("deployment.cache.enabled", this.cacheEnabled.isSelected());
    setVisible(false);
    dispose();
  }

  private void cancelBtnActionPerformed(ActionEvent paramActionEvent)
  {
    setVisible(false);
    dispose();
  }

  private void closeDialog(WindowEvent paramWindowEvent)
  {
    setVisible(false);
    dispose();
  }

  private String getMessage(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  public JButton makeButton(String paramString)
  {
    JButton localJButton = new JButton(getMessage(paramString));
    localJButton.setMnemonic(ResourceManager.getVKCode(paramString + ".mnemonic"));
    return localJButton;
  }

  private void sliderStateChanged(ChangeEvent paramChangeEvent)
  {
    this.cacheSizeSpinner.setValue(Integer.valueOf(this.cacheSizeSlider.getValue()));
  }

  private void updateSlider()
  {
    this.cacheSizeSlider.setValue(Integer.valueOf(this.cacheSizeSpinner.getModel().getValue().toString()).intValue());
  }

  private void checkboxStateChanged(ItemEvent paramItemEvent)
  {
    if (paramItemEvent.getStateChange() == 2)
      setAllEnabled(false);
    if (paramItemEvent.getStateChange() == 1)
      setAllEnabled(true);
  }

  private void setAllEnabled(boolean paramBoolean)
  {
    this.location.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.user.cachedir")));
    this.locLbl.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.user.cachedir")));
    this.chooseBtn.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.user.cachedir")) && (!Platform.get().isPlatformWindowsVista()));
    this.compression.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.jarcompression")));
    this.compressionLbl.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.jarcompression")));
    this.diskSpaceLbl.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.max.size")));
    this.cacheSizeSlider.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.max.size")));
    this.cacheSizeSpinner.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.max.size")));
    this.unitsLbl.setEnabled((paramBoolean) && (!Config.get().isPropertyLocked("deployment.cache.max.size")));
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.CacheSettingsDialog
 * JD-Core Version:    0.6.0
 */