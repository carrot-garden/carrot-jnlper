package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.config.JREInfo;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.ui.UIFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FileChooserUI;

public class JreFindDialog extends JDialog
  implements ActionListener
{
  private final JButton _cancelButton = new JButton(ResourceManager.getMessage("find.cancelButton"));
  private final JButton _prevButton = new JButton(ResourceManager.getMessage("find.prevButton"));
  private final JButton _nextButton = new JButton(ResourceManager.getMessage("find.nextButton"));
  private JLabel _titleLabel;
  private JComponent _mainComponent = null;
  private JPanel _buttonPanel;
  private File _directory;
  private int _state;
  private GridBagConstraints _constraints;
  private JTextArea _ta;
  private PathChooser _pc;
  private Dimension _panelPref;
  private JREInfo[] _jres;

  public JreFindDialog(JDialog paramJDialog)
  {
    super(paramJDialog, ResourceManager.getMessage("find.dialog.title"), true);
    initComponents();
  }

  private void initComponents()
  {
    setJREs(null);
    this._titleLabel = new JLabel(ResourceManager.getMessage("find.title"));
    setFont(this._titleLabel, 2, 18);
    this._cancelButton.addActionListener(this);
    this._nextButton.addActionListener(this);
    this._prevButton.addActionListener(this);
    this._cancelButton.setMnemonic(ResourceManager.getVKCode("find.cancelButtonMnemonic"));
    this._prevButton.setMnemonic(ResourceManager.getVKCode("find.prevButtonMnemonic"));
    this._nextButton.setMnemonic(ResourceManager.getVKCode("find.nextButtonMnemonic"));
    this._buttonPanel = new JPanel(new BorderLayout());
    JPanel localJPanel = new JPanel();
    localJPanel.add(this._cancelButton);
    localJPanel.add(this._prevButton);
    localJPanel.add(this._nextButton);
    this._buttonPanel.add(localJPanel, "East");
    String str = ResourceManager.getMessage("find.intro");
    this._ta = new JSmartTextArea(str);
    this._pc = new PathChooser();
    this._panelPref = this._pc.getUI().getPreferredSize(this._pc);
    this._directory = null;
    1 local1 = new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        JreFindDialog.this.setVisible(false);
        JreFindDialog.this.dispose();
      }
    };
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancel");
    getRootPane().getActionMap().put("cancel", local1);
    setState(0);
  }

  public static JREInfo[] search(JDialog paramJDialog)
  {
    JreFindDialog localJreFindDialog = new JreFindDialog(paramJDialog);
    localJreFindDialog.show();
    return localJreFindDialog._jres;
  }

  private static void setFont(JComponent paramJComponent, int paramInt1, int paramInt2)
  {
    int i = paramJComponent.getFont().getSize();
    paramInt2 = Math.max(i, paramInt2);
    paramJComponent.setFont(paramJComponent.getFont().deriveFont(paramInt1, paramInt2));
  }

  public void show()
  {
    getContentPane().setLayout(new GridBagLayout());
    GridBagConstraints localGridBagConstraints = new GridBagConstraints();
    localGridBagConstraints.gridx = (localGridBagConstraints.gridy = 0);
    localGridBagConstraints.weightx = (localGridBagConstraints.weighty = 0.0D);
    localGridBagConstraints.fill = 0;
    localGridBagConstraints.anchor = 10;
    localGridBagConstraints.insets = new Insets(5, 5, 5, 5);
    getContentPane().add(this._titleLabel, localGridBagConstraints);
    localGridBagConstraints.gridy = 2;
    localGridBagConstraints.weightx = (localGridBagConstraints.weighty = 0.0D);
    localGridBagConstraints.fill = 0;
    localGridBagConstraints.anchor = 13;
    getContentPane().add(this._buttonPanel, localGridBagConstraints);
    localGridBagConstraints.gridy = 1;
    localGridBagConstraints.weightx = (localGridBagConstraints.weighty = 1.0D);
    localGridBagConstraints.fill = 1;
    localGridBagConstraints.anchor = 17;
    if (this._mainComponent != null)
      getContentPane().add(this._mainComponent, localGridBagConstraints);
    this._constraints = localGridBagConstraints;
    pack();
    Dimension localDimension1 = getSize();
    localDimension1.width = Math.max(localDimension1.width, 500);
    localDimension1.height = Math.max(localDimension1.height, 420);
    Dimension localDimension2 = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds((localDimension2.width - localDimension1.width) / 2, (localDimension2.height - localDimension1.height) / 2, localDimension1.width, localDimension1.height);
    UIFactory.placeWindow(this);
    addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent paramKeyEvent)
      {
        int i = ResourceManager.getVKCode("find.cancelButtonMnemonic");
        if (paramKeyEvent.getKeyCode() == i)
          JreFindDialog.this._cancelButton.doClick();
      }
    });
    super.show();
    dispose();
    setState(-1);
  }

  protected void cancel()
  {
    setJREs(null);
    setVisible(false);
  }

  protected void next()
  {
    int i = getState();
    i++;
    if (i == 3)
      setVisible(false);
    else
      setState(i);
  }

  protected void previous()
  {
    int i = getState();
    i--;
    setState(i);
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
    Object localObject = paramActionEvent.getSource();
    if (localObject.equals(this._cancelButton))
      cancel();
    else if (localObject.equals(this._prevButton))
      previous();
    else if (localObject.equals(this._nextButton))
      next();
  }

  private void setDirectory(File paramFile)
  {
    this._directory = paramFile;
    this._nextButton.setEnabled(paramFile != null);
  }

  private File getDirectory()
  {
    return this._directory;
  }

  private void setJREs(JREInfo[] paramArrayOfJREInfo)
  {
    this._jres = paramArrayOfJREInfo;
  }

  private void setState(int paramInt)
  {
    this._cancelButton.setEnabled(true);
    this._prevButton.setEnabled(false);
    this._nextButton.setEnabled(true);
    this._state = paramInt;
    if (this._mainComponent != null)
    {
      getContentPane().remove(this._mainComponent);
      this._mainComponent = null;
    }
    Object localObject;
    switch (this._state)
    {
    case 0:
      this._mainComponent = this._ta;
      this._nextButton.setText(ResourceManager.getMessage("find.nextButton"));
      break;
    case 1:
      this._mainComponent = this._pc;
      this._pc.setCurrentDirectory(this._directory);
      if (this._directory == null)
        this._pc.updateDirectory();
      this._prevButton.setEnabled(true);
      this._nextButton.setText(ResourceManager.getMessage("find.nextButton"));
      break;
    case 2:
      localObject = new SearchPanel();
      this._mainComponent = ((JComponent)localObject);
      ((SearchPanel)localObject).start();
      this._nextButton.setText(ResourceManager.getMessage("find.finishButton"));
      break;
    }
    if (this._mainComponent != null)
    {
      this._mainComponent.setPreferredSize(this._panelPref);
      getContentPane().add(this._mainComponent, this._constraints);
      this._mainComponent.revalidate();
      localObject = getSize();
      Dimension localDimension = getPreferredSize();
      ((Dimension)localObject).width = Math.max(((Dimension)localObject).width, localDimension.width);
      ((Dimension)localObject).height = Math.max(((Dimension)localObject).height, localDimension.height);
      setSize(((Dimension)localObject).width, ((Dimension)localObject).height);
    }
    repaint();
  }

  private int getState()
  {
    return this._state;
  }

  private class PathChooser extends JFileChooser
  {
    public PathChooser()
    {
      setFileSelectionMode(1);
      addActionListener(new ActionListener(JreFindDialog.this)
      {
        private final JreFindDialog val$this$0;

        public void actionPerformed(ActionEvent paramActionEvent)
        {
          if ("ApproveSelection".equals(paramActionEvent.getActionCommand()))
          {
            if ((paramActionEvent.getSource() instanceof JFileChooser))
            {
              JFileChooser localJFileChooser = (JFileChooser)paramActionEvent.getSource();
              File localFile = localJFileChooser.getSelectedFile();
              if ((localFile != null) && (localFile.isDirectory()))
              {
                localJFileChooser.setCurrentDirectory(localFile);
                JreFindDialog.this.setDirectory(localFile);
              }
            }
            JreFindDialog.PathChooser.this.updateDirectory();
          }
          else if ("CancelSelection".equals(paramActionEvent.getActionCommand()))
          {
            JreFindDialog.this.cancel();
          }
        }
      });
      addPropertyChangeListener(new PropertyChangeListener(JreFindDialog.this)
      {
        private final JreFindDialog val$this$0;

        public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent)
        {
          if (("SelectedFileChangedProperty".equals(paramPropertyChangeEvent.getPropertyName())) || ("directoryChanged".equals(paramPropertyChangeEvent.getPropertyName())))
            JreFindDialog.PathChooser.this.updateDirectory();
        }
      });
      try
      {
        Class[] arrayOfClass = { Boolean.TYPE };
        Method localMethod = JFileChooser.class.getMethod("setControlButtonsAreShown", arrayOfClass);
        if (localMethod != null)
        {
          Object[] arrayOfObject = { Boolean.FALSE };
          localMethod.invoke(this, arrayOfObject);
        }
      }
      catch (NoSuchMethodException localNoSuchMethodException)
      {
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
      }
      setDialogType(0);
    }

    private void updateDirectory()
    {
      File localFile = getSelectedFile();
      if (localFile == null)
        localFile = getCurrentDirectory();
      JreFindDialog.this.setDirectory(localFile);
    }
  }

  private class SearchPanel extends JPanel
    implements ActionListener
  {
    private DefaultListModel model = new DefaultListModel();
    private JList list = new JList(this.model);
    private JLabel searchLabel;
    private String searchPrefix = ResourceManager.getMessage("find.searching.prefix");
    private JLabel titleLabel;
    private Searcher searcher;
    private Timer timer;
    private boolean active;

    public SearchPanel()
    {
      this.list.setCellRenderer(new DefaultListCellRenderer(JreFindDialog.this)
      {
        private final JreFindDialog val$this$0;

        public Component getListCellRendererComponent(JList paramJList, Object paramObject, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
        {
          if ((paramObject instanceof JREInfo))
            paramObject = ((JREInfo)paramObject).getPath();
          return super.getListCellRendererComponent(paramJList, paramObject, paramInt, paramBoolean1, paramBoolean2);
        }
      });
      this.list.addListSelectionListener(new ListSelectionListener(JreFindDialog.this)
      {
        private final JreFindDialog val$this$0;

        public void valueChanged(ListSelectionEvent paramListSelectionEvent)
        {
          JreFindDialog.SearchPanel.this.updateJREs();
        }
      });
      this.searchLabel = new JLabel(" ");
      JreFindDialog.access$500(this.searchLabel, 0, 12);
      this.titleLabel = new JLabel(ResourceManager.getMessage("find.searching.title"));
      JreFindDialog.access$500(this.titleLabel, 0, 12);
      setLayout(new GridBagLayout());
      GridBagConstraints localGridBagConstraints = new GridBagConstraints();
      localGridBagConstraints.gridx = (localGridBagConstraints.gridy = 0);
      localGridBagConstraints.weightx = 1.0D;
      localGridBagConstraints.weighty = 0.0D;
      localGridBagConstraints.fill = 2;
      localGridBagConstraints.insets = new Insets(5, 2, 0, 2);
      localGridBagConstraints.gridy = 0;
      add(this.titleLabel, localGridBagConstraints);
      localGridBagConstraints.gridy = 2;
      add(this.searchLabel, localGridBagConstraints);
      localGridBagConstraints.gridy = 1;
      localGridBagConstraints.weighty = 1.0D;
      localGridBagConstraints.fill = 1;
      localGridBagConstraints.insets.bottom = 0;
      add(new JScrollPane(this.list), localGridBagConstraints);
    }

    public void start()
    {
      this.active = true;
      JreFindDialog.this._nextButton.setEnabled(false);
      JreFindDialog.this._prevButton.setEnabled(true);
      if (this.model != null)
        this.model.removeAllElements();
      this.timer = new Timer(100, this);
      this.timer.setRepeats(true);
      this.timer.start();
      this.searcher = new Searcher(null);
      this.searcher.start(JreFindDialog.this.getDirectory());
    }

    public void stop()
    {
      this.active = false;
      stopSearching();
    }

    private void stopSearching()
    {
      if (this.searcher != null)
      {
        this.searcher.stop();
        this.searcher = null;
        this.timer.stop();
        this.timer = null;
      }
    }

    public void actionPerformed(ActionEvent paramActionEvent)
    {
      if (!this.searcher.isFinished())
      {
        File localFile = this.searcher.getCurrentFile();
        if (localFile != null)
          this.searchLabel.setText(this.searchPrefix + localFile.getPath());
        else
          this.searchLabel.setText(this.searchPrefix);
      }
      else
      {
        if (this.model.getSize() > 0)
        {
          this.titleLabel.setText(ResourceManager.getMessage("find.foundJREs.title"));
          JreFindDialog.this._nextButton.setEnabled(true);
        }
        else
        {
          this.titleLabel.setText(ResourceManager.getMessage("find.noJREs.title"));
          JreFindDialog.this._nextButton.setEnabled(false);
        }
        this.searchLabel.setText(" ");
        stopSearching();
      }
    }

    private void updateJREs()
    {
      if (!this.active)
        return;
      if (this.model != null)
      {
        int i = this.model.getSize();
        if (i > 0)
        {
          JreFindDialog.this._nextButton.setEnabled(true);
          int[] arrayOfInt = this.list.getSelectedIndices();
          JREInfo[] arrayOfJREInfo;
          if ((arrayOfInt != null) && (arrayOfInt.length > 0))
          {
            arrayOfJREInfo = new JREInfo[arrayOfInt.length];
            for (int j = 0; j < arrayOfInt.length; j++)
              arrayOfJREInfo[j] = ((JREInfo)this.model.getElementAt(arrayOfInt[j]));
            JreFindDialog.this.setJREs(arrayOfJREInfo);
          }
          else
          {
            arrayOfJREInfo = new JREInfo[i];
            this.model.copyInto(arrayOfJREInfo);
            JreFindDialog.this.setJREs(arrayOfJREInfo);
          }
        }
        else
        {
          JreFindDialog.this.setJREs(null);
          JreFindDialog.this._nextButton.setEnabled(false);
        }
      }
      else
      {
        JreFindDialog.this.setJREs(null);
      }
    }

    private boolean alreadyFound(JREInfo paramJREInfo)
    {
      for (int i = 0; i < this.model.getSize(); i++)
      {
        Object localObject = this.model.get(i);
        if ((localObject != null) && ((localObject instanceof JREInfo)) && (paramJREInfo.getPath().equals(((JREInfo)localObject).getPath())))
          return true;
      }
      return false;
    }

    private void add(Searcher paramSearcher, JREInfo paramJREInfo)
    {
      SwingUtilities.invokeLater(new Runnable(paramSearcher, paramJREInfo)
      {
        private final JreFindDialog.SearchPanel.Searcher val$searcher;
        private final JREInfo val$jre;

        public void run()
        {
          if ((JreFindDialog.SearchPanel.this.searcher == this.val$searcher) && (!JreFindDialog.SearchPanel.this.alreadyFound(this.val$jre)))
          {
            JreFindDialog.SearchPanel.this.model.addElement(this.val$jre);
            JreFindDialog.SearchPanel.this.updateJREs();
          }
        }
      });
    }

    private class Searcher
      implements Runnable
    {
      private File file;
      private boolean stop;
      private File currentFile;
      private boolean finished;
      private final JreFindDialog.SearchPanel this$1;

      private Searcher()
      {
        this.this$1 = this$1;
      }

      void start(File paramFile)
      {
        this.file = paramFile;
        new Thread(this).start();
        this.this$1.updateJREs();
      }

      public File getCurrentFile()
      {
        return this.currentFile;
      }

      public void stop()
      {
        this.stop = true;
      }

      public void run()
      {
        check(this.file, false);
        this.finished = true;
      }

      public boolean isFinished()
      {
        return this.finished;
      }

      private boolean check(File paramFile, boolean paramBoolean)
      {
        this.currentFile = paramFile;
        String str = paramFile.getName();
        Object localObject;
        if ((paramFile.isFile()) && ((str.equals("java")) || (str.equals("javaw.exe"))))
        {
          localObject = getVersion(paramFile, paramBoolean);
          if (localObject != null)
          {
            this.this$1.add(this, (JREInfo)localObject);
            return true;
          }
        }
        else if (!paramFile.isFile())
        {
          localObject = paramFile.list();
          if (localObject != null)
          {
            Arrays.sort(localObject);
            int i = 0;
            int j = localObject.length;
            while ((i < j) && (!this.stop))
            {
              paramBoolean = check(new File(paramFile, localObject[i]), paramBoolean);
              i++;
            }
          }
        }
        return paramBoolean;
      }

      private JREInfo getVersion(File paramFile, boolean paramBoolean)
      {
        if (!isValidJavaPath(paramFile, paramBoolean))
          return null;
        try
        {
          File localFile = paramFile.getCanonicalFile();
          JREInfo localJREInfo = JreLocator.getVersion(localFile);
          if (localJREInfo != null)
          {
            if (localJREInfo.getPlatform().compareTo("1.3") < 0)
              return null;
            localJREInfo.setOSName(Config.getOSName());
            localJREInfo.setOSArch(Config.getOSArch());
          }
          return localJREInfo;
        }
        catch (IOException localIOException)
        {
        }
        return null;
      }

      private boolean isValidJavaPath(File paramFile, boolean paramBoolean)
      {
        String str1 = paramFile.getParent();
        if ((str1.endsWith(File.separator + "native_threads")) || (str1.endsWith(File.separator + "green_threads")))
          return false;
        String str2 = File.separator + "jre" + File.separator + "bin";
        if ((str1.endsWith(str2)) && (str1.length() > str2.length()) && (paramBoolean))
        {
          String str3 = str1.substring(0, str1.length() - str2.length() + 1) + "bin" + File.separator;
          File localFile = new File(str3 + "java");
          if ((localFile.exists()) && (localFile.isFile()))
            return false;
          localFile = new File(str3 + "javaw.exe");
          if ((localFile.exists()) && (localFile.isFile()))
            return false;
        }
        return !str1.endsWith("sparcv9");
      }

      Searcher(JreFindDialog.1 arg2)
      {
        this();
      }
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.JreFindDialog
 * JD-Core Version:    0.6.0
 */