package com.sun.javaws.ui;

import com.sun.deploy.Environment;
import com.sun.deploy.cache.Cache;
import com.sun.deploy.cache.CacheEntry;
import com.sun.deploy.cache.LocalApplicationProperties;
import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import com.sun.deploy.trace.Trace;
import com.sun.deploy.trace.TraceLevel;
import com.sun.deploy.ui.AppInfo;
import com.sun.deploy.uitoolkit.ToolkitStore;
import com.sun.javaws.BrowserSupport;
import com.sun.javaws.CacheUtil;
import com.sun.javaws.LocalInstallHandler;
import com.sun.javaws.Main;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.ShortcutDesc;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box.Filler;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.SeparatorUI;
import javax.swing.table.TableModel;

public class CacheViewer extends JDialog
  implements ListSelectionListener, PopupMenuListener
{
  private final JPanel tablePanel = new JPanel(new BorderLayout());
  private CacheTable jnlpTable = null;
  private CacheTable resourceTable = null;
  private CacheTable importTable = null;
  private CacheTable sysJnlpTable = null;
  private CacheTable sysResourceTable = null;
  private CacheTable activeTable;
  private boolean noSeparator = false;
  private boolean wasDirty = false;
  private int leadinSpace;
  private int minSpace;
  private JComboBox viewComboBox;
  private JLabel viewLabel;
  private JToolBar toolbar;
  private JLabel sizeLabel = new JLabel("");
  private JButton runButton;
  private JButton removeButton;
  private JButton installButton;
  private JButton showButton;
  private JButton showResourceButton;
  private JButton homeButton;
  private JButton removeResourceButton;
  private JButton removeRemovedButton;
  private JButton importButton;
  private JButton closeButton;
  private JPopupMenu runPopup;
  private JMenuItem onlineMI;
  private JMenuItem offlineMI;
  private JPopupMenu popup;
  private JMenuItem runOnlineMI;
  private JMenuItem runOfflineMI;
  private JMenuItem installMI;
  private JMenuItem removeMI;
  private JMenuItem showMI;
  private JMenuItem showResourceMI;
  private JMenuItem homeMI;
  private JMenuItem importMI;
  private static final int WAIT_REMOVE = 0;
  private static final int WAIT_IMPORT = 1;
  private ImageIcon dummy = null;
  private static final String BOUNDS_PROPERTY_KEY = "deployment.javaws.viewer.bounds";
  private final int SLEEP_DELAY = 2000;

  private CacheViewer(JFrame paramJFrame)
  {
    super(paramJFrame);
    Cache.setDoIPLookup(false);
    setTitle(getResource("viewer.title"));
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent paramWindowEvent)
      {
        CacheViewer.this.exitViewer();
      }
    });
    initComponents();
    this.wasDirty = Config.get().isConfigDirty();
  }

  private void refresh()
  {
    int i = this.viewComboBox.getSelectedIndex();
    Dimension localDimension1 = new Dimension(this.minSpace, 0);
    Dimension localDimension2 = new Dimension(8, 0);
    Dimension localDimension3 = new Dimension(this.leadinSpace, 0);
    Dimension localDimension4 = new Dimension(4, 0);
    SwingUtilities.invokeLater(new Runnable(localDimension2, localDimension4, localDimension3, i, localDimension1)
    {
      private final Dimension val$d1;
      private final Dimension val$d4;
      private final Dimension val$d2;
      private final int val$index;
      private final Dimension val$d0;

      public void run()
      {
        Component[] arrayOfComponent = CacheViewer.this.toolbar.getComponents();
        if (arrayOfComponent.length == 0)
        {
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d1, this.val$d1, this.val$d1));
          CacheViewer.this.toolbar.add(CacheViewer.this.viewLabel);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d4, this.val$d4, this.val$d4));
          CacheViewer.this.toolbar.add(CacheViewer.this.viewComboBox);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d2, this.val$d2, this.val$d2));
        }
        else
        {
          for (int i = arrayOfComponent.length - 1; i > 4; i--)
            CacheViewer.this.toolbar.remove(i);
        }
        JButton localJButton1;
        JButton localJButton2;
        switch (this.val$index)
        {
        case 0:
        default:
          if (CacheViewer.this.jnlpTable == null)
            CacheViewer.access$402(CacheViewer.this, new CacheTable(CacheViewer.this, 0, false));
          CacheViewer.this.sizeLabel.setText(CacheViewer.this.jnlpTable.getSizeLabelText());
          CacheViewer.access$602(CacheViewer.this, CacheViewer.this.jnlpTable);
          CacheViewer.this.toolbar.add(CacheViewer.this.runButton);
          localJButton1 = CacheViewer.this.runButton;
          if (CacheViewer.this.noSeparator)
          {
            CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          }
          else
          {
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
            CacheViewer.this.toolbar.add(new CacheViewer.VSeparator(CacheViewer.this));
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
          }
          CacheViewer.this.toolbar.add(CacheViewer.this.showButton);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.installButton);
          if (CacheViewer.this.noSeparator)
          {
            CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          }
          else
          {
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
            CacheViewer.this.toolbar.add(new CacheViewer.VSeparator(CacheViewer.this));
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
          }
          CacheViewer.this.toolbar.add(CacheViewer.this.removeButton);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.homeButton);
          CacheViewer.this.runButton.setNextFocusableComponent(CacheViewer.this.showButton);
          CacheViewer.this.showButton.setNextFocusableComponent(CacheViewer.this.installButton);
          CacheViewer.this.installButton.setNextFocusableComponent(CacheViewer.this.removeButton);
          CacheViewer.this.removeButton.setNextFocusableComponent(CacheViewer.this.homeButton);
          localJButton2 = CacheViewer.this.homeButton;
          break;
        case 1:
          if (CacheViewer.this.resourceTable == null)
            CacheViewer.access$1302(CacheViewer.this, new CacheTable(CacheViewer.this, 1, false));
          CacheViewer.this.sizeLabel.setText(CacheViewer.this.resourceTable.getSizeLabelText());
          CacheViewer.access$602(CacheViewer.this, CacheViewer.this.resourceTable);
          CacheViewer.this.toolbar.add(CacheViewer.this.showResourceButton);
          localJButton1 = CacheViewer.this.showResourceButton;
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.removeResourceButton);
          CacheViewer.this.showResourceButton.setNextFocusableComponent(CacheViewer.this.removeResourceButton);
          localJButton2 = CacheViewer.this.removeResourceButton;
          break;
        case 2:
          if (CacheViewer.this.importTable == null)
            CacheViewer.access$1602(CacheViewer.this, new CacheTable(CacheViewer.this, 2, false));
          CacheViewer.this.sizeLabel.setText(CacheViewer.this.importTable.getSizeLabelText());
          CacheViewer.access$602(CacheViewer.this, CacheViewer.this.importTable);
          CacheViewer.this.toolbar.add(CacheViewer.this.importButton);
          localJButton1 = CacheViewer.this.importButton;
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.removeRemovedButton);
          CacheViewer.this.importButton.setNextFocusableComponent(CacheViewer.this.removeRemovedButton);
          localJButton2 = CacheViewer.this.removeRemovedButton;
          break;
        case 3:
          if (CacheViewer.this.sysJnlpTable == null)
            CacheViewer.access$1902(CacheViewer.this, new CacheTable(CacheViewer.this, 0, true));
          CacheViewer.this.sizeLabel.setText(CacheViewer.this.sysJnlpTable.getSizeLabelText());
          CacheViewer.access$602(CacheViewer.this, CacheViewer.this.sysJnlpTable);
          CacheViewer.this.toolbar.add(CacheViewer.this.runButton);
          localJButton1 = CacheViewer.this.runButton;
          if (CacheViewer.this.noSeparator)
          {
            CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          }
          else
          {
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
            CacheViewer.this.toolbar.add(new CacheViewer.VSeparator(CacheViewer.this));
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
          }
          CacheViewer.this.toolbar.add(CacheViewer.this.showButton);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.installButton);
          if (CacheViewer.this.noSeparator)
          {
            CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          }
          else
          {
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
            CacheViewer.this.toolbar.add(new CacheViewer.VSeparator(CacheViewer.this));
            CacheViewer.this.toolbar.addSeparator(this.val$d1);
          }
          CacheViewer.this.toolbar.add(CacheViewer.this.removeButton);
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.homeButton);
          CacheViewer.this.runButton.setNextFocusableComponent(CacheViewer.this.showButton);
          CacheViewer.this.showButton.setNextFocusableComponent(CacheViewer.this.installButton);
          CacheViewer.this.installButton.setNextFocusableComponent(CacheViewer.this.removeButton);
          CacheViewer.this.removeButton.setNextFocusableComponent(CacheViewer.this.homeButton);
          localJButton2 = CacheViewer.this.homeButton;
          break;
        case 4:
          if (CacheViewer.this.sysResourceTable == null)
            CacheViewer.access$2002(CacheViewer.this, new CacheTable(CacheViewer.this, 1, true));
          CacheViewer.this.sizeLabel.setText(CacheViewer.this.sysResourceTable.getSizeLabelText());
          CacheViewer.access$602(CacheViewer.this, CacheViewer.this.sysResourceTable);
          CacheViewer.this.toolbar.add(CacheViewer.this.showResourceButton);
          localJButton1 = CacheViewer.this.showResourceButton;
          CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
          CacheViewer.this.toolbar.add(CacheViewer.this.removeResourceButton);
          CacheViewer.this.showResourceButton.setNextFocusableComponent(CacheViewer.this.removeResourceButton);
          localJButton2 = CacheViewer.this.removeResourceButton;
        }
        CacheViewer.access$2102(CacheViewer.this, new JPopupMenu());
        CacheViewer.this.popup.addPopupMenuListener(CacheViewer.this);
        switch (this.val$index)
        {
        case 0:
        case 3:
          CacheViewer.this.popup.add(CacheViewer.this.runOnlineMI);
          CacheViewer.this.popup.add(CacheViewer.this.runOfflineMI);
          CacheViewer.this.popup.addSeparator();
          CacheViewer.this.popup.add(CacheViewer.this.installMI);
          CacheViewer.this.popup.add(CacheViewer.this.removeMI);
          CacheViewer.this.popup.addSeparator();
          CacheViewer.this.popup.add(CacheViewer.this.showMI);
          CacheViewer.this.popup.add(CacheViewer.this.homeMI);
          break;
        case 1:
        case 4:
          CacheViewer.this.popup.add(CacheViewer.this.showResourceMI);
          CacheViewer.this.popup.add(CacheViewer.this.removeMI);
          break;
        case 2:
          CacheViewer.this.popup.add(CacheViewer.this.importMI);
          CacheViewer.this.popup.add(CacheViewer.this.removeMI);
        }
        JScrollPane localJScrollPane = new JScrollPane(CacheViewer.this.activeTable);
        CacheViewer.this.toolbar.add(new Box.Filler(this.val$d0, this.val$d0, this.val$d0));
        CacheViewer.this.toolbar.add(new CacheViewer.remainingSpacer(CacheViewer.this, null));
        CacheViewer.this.toolbar.add(CacheViewer.this.sizeLabel);
        CacheViewer.this.toolbar.add(new Box.Filler(this.val$d1, this.val$d1, this.val$d1));
        CacheViewer.this.tablePanel.removeAll();
        CacheViewer.this.tablePanel.add(localJScrollPane);
        CacheViewer.this.enableButtons();
        CacheViewer.this.closeButton.setNextFocusableComponent(CacheViewer.this.viewComboBox);
        CacheViewer.this.viewComboBox.setNextFocusableComponent(localJButton1);
        localJButton2.setNextFocusableComponent(CacheViewer.this.activeTable);
        CacheViewer.this.validate();
        CacheViewer.this.repaint();
      }
    });
  }

  private void initComponents()
  {
    JPanel localJPanel1 = new JPanel(new BorderLayout());
    JPanel localJPanel2 = new JPanel();
    this.toolbar = new JToolBar();
    this.toolbar.setBorderPainted(false);
    this.toolbar.setFloatable(false);
    this.toolbar.setMargin(new Insets(2, 2, 0, 0));
    this.toolbar.setRollover(true);
    String str = UIManager.getLookAndFeel().getID();
    if (str.startsWith("Windows"))
    {
      this.leadinSpace = 27;
      this.minSpace = 4;
      this.noSeparator = false;
    }
    else
    {
      this.leadinSpace = 30;
      this.noSeparator = true;
      if (str.startsWith("GTK"))
        this.minSpace = 2;
      else
        this.minSpace = 10;
    }
    3 local3 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.runApplication(true);
      }
    };
    4 local4 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.runApplication(false);
      }
    };
    5 local5 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.installApplication();
      }
    };
    6 local6 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.importApplication();
      }
    };
    7 local7 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.delete();
      }
    };
    8 local8 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        if ((CacheViewer.this.activeTable == CacheViewer.this.jnlpTable) || (CacheViewer.this.activeTable == CacheViewer.this.sysJnlpTable))
          CacheViewer.this.showApplication();
        else if ((CacheViewer.this.activeTable == CacheViewer.this.resourceTable) || (CacheViewer.this.activeTable == CacheViewer.this.sysResourceTable))
          CacheViewer.this.showInformation();
      }
    };
    9 local9 = new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.browseApplication();
      }
    };
    this.runOnlineMI = createMI("viewer.run.online.menuitem", local3);
    this.runOfflineMI = createMI("viewer.run.offline.menuitem", local4);
    this.installMI = createMI("viewer.install.menuitem", local5);
    this.removeMI = createMI("viewer.remove.menuitem", local7);
    this.showMI = createMI("viewer.show.menuitem", local8);
    this.showResourceMI = createMI("viewer.show.resource.menuitem", local8);
    this.homeMI = createMI("viewer.home.menuitem", local9);
    this.importMI = createMI("viewer.import.menuitem", local6);
    int i = (Config.getSystemCacheDirectory() != null) && (!Environment.isSystemCacheMode()) ? 1 : 0;
    String[] arrayOfString = i != 0 ? new String[5] : new String[3];
    arrayOfString[0] = getResource("viewer.view.jnlp");
    arrayOfString[1] = getResource("viewer.view.res");
    arrayOfString[2] = getResource("viewer.view.import");
    if (i != 0)
    {
      arrayOfString[3] = getResource("viewer.sys.view.jnlp");
      arrayOfString[4] = getResource("viewer.sys.view.res");
    }
    this.viewComboBox = new JComboBox(arrayOfString)
    {
      public Dimension getMinimumSize()
      {
        return getPreferredSize();
      }

      public Dimension getMaximumSize()
      {
        return getPreferredSize();
      }
    };
    this.viewComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.refresh();
      }
    });
    this.viewLabel = new JLabel(getResource("viewer.view.label"));
    this.runButton = createRunButton("viewer.run.online");
    this.importButton = createImageButton("viewer.import", 0);
    this.importButton.addActionListener(local6);
    this.removeButton = createImageButton("viewer.remove", 0);
    this.removeButton.addActionListener(local7);
    this.removeResourceButton = createImageButton("viewer.remove.res", 0);
    this.removeResourceButton.addActionListener(local7);
    this.removeRemovedButton = createImageButton("viewer.remove.removed", 0);
    this.removeRemovedButton.addActionListener(local7);
    this.installButton = createImageButton("viewer.install", 0);
    this.installButton.addActionListener(local5);
    this.showButton = createImageButton("viewer.show", 0);
    this.showButton.addActionListener(local8);
    this.showResourceButton = createImageButton("viewer.info", 0);
    this.showResourceButton.addActionListener(local8);
    this.homeButton = createImageButton("viewer.home", 0);
    this.homeButton.addActionListener(local9);
    this.tablePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
    this.closeButton = new JButton(getResource("viewer.close"));
    this.closeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.exitViewer();
      }
    });
    addCancelAction();
    JButton localJButton = new JButton(getResource("viewer.help"));
    localJButton.setMnemonic(ResourceManager.getVKCode("viewer.help.mnemonic"));
    localJButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.help();
      }
    });
    localJPanel2.add(this.closeButton);
    localJPanel1.add(localJPanel2, "East");
    localJPanel1.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
    JPanel localJPanel3 = new JPanel(new BorderLayout());
    localJPanel3.add(this.toolbar, "Center");
    localJPanel3.add(new JSeparator(), "South");
    JPanel localJPanel4 = new JPanel(new BorderLayout());
    localJPanel4.add(this.tablePanel, "Center");
    localJPanel4.add(localJPanel1, "South");
    getContentPane().add(localJPanel3, "North");
    getContentPane().add(localJPanel4, "Center");
    this.runPopup = new JPopupMenu();
    this.onlineMI = this.runPopup.add(getResource("viewer.run.online.mi"));
    this.onlineMI.setEnabled(false);
    this.onlineMI.setIcon(getIcon("viewer.run.online.mi.icon"));
    this.onlineMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.runApplication(true);
      }
    });
    this.offlineMI = this.runPopup.add(getResource("viewer.run.offline.mi"));
    this.offlineMI.setEnabled(false);
    this.offlineMI.setIcon(getIcon("viewer.run.offline.mi.icon"));
    this.offlineMI.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.runApplication(false);
      }
    });
    this.runPopup.addPopupMenuListener(this);
    this.runPopup.addMenuKeyListener(new MenuKeyListener()
    {
      public void menuKeyPressed(MenuKeyEvent paramMenuKeyEvent)
      {
        int i = paramMenuKeyEvent.getKeyCode();
        if ((i == 9) || (i == 39) || (i == 37))
          CacheViewer.this.runPopup.setVisible(false);
      }

      public void menuKeyReleased(MenuKeyEvent paramMenuKeyEvent)
      {
      }

      public void menuKeyTyped(MenuKeyEvent paramMenuKeyEvent)
      {
      }
    });
    this.jnlpTable = new CacheTable(this, 0, false);
    if (this.jnlpTable.getModel().getRowCount() != 0)
    {
      this.viewComboBox.setSelectedIndex(0);
      focusLater(this.jnlpTable);
      return;
    }
    this.resourceTable = new CacheTable(this, 1, false);
    if (this.resourceTable.getModel().getRowCount() != 0)
    {
      this.viewComboBox.setSelectedIndex(1);
      focusLater(this.resourceTable);
      return;
    }
    this.importTable = new CacheTable(this, 2, false);
    if (this.importTable.getModel().getRowCount() != 0)
    {
      this.viewComboBox.setSelectedIndex(2);
      focusLater(this.importTable);
      return;
    }
    focusLater(this.closeButton);
    refresh();
  }

  private void focusLater(Component paramComponent)
  {
    SwingUtilities.invokeLater(new Runnable(paramComponent)
    {
      private final Component val$component;

      public void run()
      {
        this.val$component.requestFocus();
      }
    });
  }

  public void addCancelAction()
  {
    getRootPane().getInputMap(2).put(KeyStroke.getKeyStroke(27, 0), "cancelViewer");
    getRootPane().getActionMap().put("cancelViewer", new AbstractAction()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        CacheViewer.this.exitViewer();
      }
    });
  }

  public void removeCancelAction()
  {
    InputMap localInputMap = getRootPane().getInputMap(2);
    KeyStroke localKeyStroke = KeyStroke.getKeyStroke(27, 0);
    if (localInputMap != null)
    {
      localInputMap.remove(localKeyStroke);
      getRootPane().setInputMap(2, localInputMap);
    }
  }

  public void popupMenuCanceled(PopupMenuEvent paramPopupMenuEvent)
  {
  }

  public void popupMenuWillBecomeInvisible(PopupMenuEvent paramPopupMenuEvent)
  {
    addCancelAction();
  }

  public void popupMenuWillBecomeVisible(PopupMenuEvent paramPopupMenuEvent)
  {
    removeCancelAction();
  }

  void runApplication()
  {
    runApplication(true);
  }

  void delete()
  {
    if ((this.activeTable == this.jnlpTable) || (this.activeTable == this.sysJnlpTable))
      removeApplications();
    else if ((this.activeTable == this.resourceTable) || (this.activeTable == this.sysResourceTable))
      removeResources();
    else if (this.activeTable == this.importTable)
      removeRemoved();
  }

  void runApplication(boolean paramBoolean)
  {
    try
    {
      CacheObject localCacheObject = getSelectedCacheObject();
      if (localCacheObject != null)
      {
        LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
        if ((localLaunchDesc != null) && (localLaunchDesc.isApplicationDescriptor()) && ((paramBoolean) || (localLaunchDesc.getInformation().supportsOfflineOperation())))
        {
          String[] arrayOfString = new String[4];
          arrayOfString[0] = Environment.getJavawsCommand();
          arrayOfString[1] = (paramBoolean ? "-online" : "-offline");
          arrayOfString[2] = "-localfile";
          arrayOfString[3] = localCacheObject.getJnlpFile().getPath();
          Process localProcess = Runtime.getRuntime().exec(arrayOfString);
          traceStream(localProcess.getInputStream());
          traceStream(localProcess.getErrorStream());
        }
      }
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  void removeApplications()
  {
    removeResources();
  }

  void importApplication()
  {
    if (this.activeTable == this.importTable)
      try
      {
        CacheObject[] arrayOfCacheObject = getSelectedCacheObjects();
        if (arrayOfCacheObject.length > 0)
          showWaitDialog(arrayOfCacheObject, 1);
      }
      catch (Throwable localThrowable)
      {
        Trace.ignored(localThrowable);
      }
  }

  void installApplication()
  {
    LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
    CacheObject localCacheObject = getSelectedCacheObject();
    if ((localCacheObject != null) && (localLocalInstallHandler.isLocalInstallSupported()))
    {
      LocalApplicationProperties localLocalApplicationProperties = localCacheObject.getLocalApplicationProperties();
      localLocalApplicationProperties.refreshIfNecessary();
      if (this.activeTable == this.sysJnlpTable)
      {
        if (localLocalApplicationProperties.isShortcutInstalledSystem());
      }
      else if ((!localLocalApplicationProperties.isShortcutInstalled()) || (!localLocalInstallHandler.isShortcutExists(localLocalApplicationProperties)))
      {
        LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
        localLocalInstallHandler.uninstallShortcuts(localLaunchDesc, localLocalApplicationProperties);
        localLocalInstallHandler.installShortcuts(localLaunchDesc, localLocalApplicationProperties);
        enableButtons();
      }
    }
  }

  private void browseApplication()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
    {
      LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
      if (localLaunchDesc != null)
      {
        URL localURL = localLaunchDesc.getInformation().getHome();
        showDocument(localURL);
      }
    }
  }

  private void showApplication()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
    {
      LaunchDesc localLaunchDesc = localCacheObject.getLaunchDesc();
      if (localLaunchDesc != null)
      {
        String str1 = localLaunchDesc.toString();
        String str2 = getResource("common.ok_btn");
        String str3 = getResource("common.cancel_btn");
        ToolkitStore.getUI();
        ToolkitStore.getUI().showMessageDialog(this, new AppInfo(), -1, getResource("viewer.show.title"), null, str1, null, str2, str3, null);
      }
    }
  }

  void showInformation()
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
      switch (localCacheObject.getObjectType())
      {
      case 1:
        showApplication();
        break;
      case 2:
      case 3:
        showResource(localCacheObject);
        break;
      case 4:
        showImage(localCacheObject);
        break;
      }
  }

  void showResource(CacheObject paramCacheObject)
  {
  }

  void showImage(CacheObject paramCacheObject)
  {
  }

  void removeRemoved()
  {
    CacheObject[] arrayOfCacheObject = getSelectedCacheObjects();
    for (int i = 0; i < arrayOfCacheObject.length; i++)
      Cache.removeRemovedApp(arrayOfCacheObject[i].getDeletedUrl(), arrayOfCacheObject[i].getDeletedTitle());
  }

  void removeResources()
  {
    try
    {
      CacheObject[] arrayOfCacheObject = getSelectedCacheObjects();
      showWaitDialog(arrayOfCacheObject, 0);
    }
    catch (Throwable localThrowable)
    {
      Trace.ignored(localThrowable);
    }
  }

  public void showWaitDialog(CacheObject[] paramArrayOfCacheObject, int paramInt)
  {
    boolean bool;
    String str1;
    String str2;
    if (paramInt == 0)
    {
      bool = true;
      str1 = paramArrayOfCacheObject.length > 1 ? "viewer.wait.remove" : "viewer.wait.remove.single";
      str2 = getResource("viewer.wait.remove.title");
    }
    else
    {
      bool = false;
      str1 = paramArrayOfCacheObject.length > 1 ? "viewer.wait.import" : "viewer.wait.import.single";
      str2 = getResource("viewer.wait.import.title");
    }
    String str3 = getResource(str1);
    CacheViewDialog localCacheViewDialog = new CacheViewDialog();
    localCacheViewDialog.initialize(this, bool);
    localCacheViewDialog.setHeading(str3, false);
    localCacheViewDialog.setTitle(str2);
    localCacheViewDialog.setProgressBarVisible(true);
    localCacheViewDialog.setIndeterminate(paramArrayOfCacheObject.length == 1);
    localCacheViewDialog.setProgressBarValue(0);
    localCacheViewDialog.setVisible(true);
    Thread localThread = new Thread(new Runnable(paramArrayOfCacheObject, paramInt, localCacheViewDialog)
    {
      private final CacheObject[] val$co;
      private final int val$type;
      private final CacheViewDialog val$dw;

      public void run()
      {
        for (int i = 0; i < this.val$co.length; i++)
          try
          {
            Object localObject1;
            Object localObject2;
            Object localObject3;
            if (this.val$type == 0)
            {
              int j = (CacheViewer.this.activeTable == CacheViewer.this.sysJnlpTable) || (CacheViewer.this.activeTable == CacheViewer.this.sysResourceTable) ? 1 : 0;
              if (j != 0)
              {
                Environment.setSystemCacheMode(true);
                Cache.reset();
              }
              localObject1 = this.val$co[i].getLaunchDesc();
              localObject2 = this.val$co[i].getCE();
              localObject3 = this.val$co[i].getNameString();
              String str2 = this.val$co[i].getUrlString();
              if (localObject1 == null)
              {
                this.val$dw.setApplication((String)localObject3, null, str2);
                Cache.removeAllCacheEntries((CacheEntry)localObject2);
              }
              else
              {
                localObject3 = ((LaunchDesc)localObject1).getInformation().getTitle();
                String str3 = ((LaunchDesc)localObject1).getInformation().getVendor();
                this.val$dw.setApplication((String)localObject3, str3, str2);
                CacheUtil.remove((CacheEntry)localObject2, (LaunchDesc)localObject1);
              }
              if (j != 0)
              {
                Environment.setSystemCacheMode(false);
                Cache.reset();
              }
            }
            else
            {
              String str1 = this.val$co[i].getDeletedTitle();
              localObject1 = this.val$co[i].getDeletedUrl();
              this.val$dw.setApplication(str1, null, (String)localObject1);
              localObject2 = new String[5];
              localObject2[0] = Environment.getJavawsCommand();
              localObject2[1] = "-wait";
              localObject2[2] = "-quiet";
              localObject2[3] = "-import";
              localObject2[4] = localObject1;
              localObject3 = Runtime.getRuntime().exec(localObject2);
              CacheViewer.this.traceStream(((Process)localObject3).getInputStream());
              CacheViewer.this.traceStream(((Process)localObject3).getErrorStream());
              int m = ((Process)localObject3).waitFor();
            }
            if (!this.val$dw.isVisible())
              break;
            int k = (i + 1) * 100 / this.val$co.length;
            this.val$dw.setProgressBarValue(k);
          }
          catch (Throwable localThrowable)
          {
            Trace.ignored(localThrowable);
          }
        this.val$dw.setVisible(false);
        CacheViewer.this.enableButtons();
      }
    });
    localThread.start();
  }

  private void traceStream(InputStream paramInputStream)
  {
    new Thread(new Runnable(paramInputStream)
    {
      private final InputStream val$is;

      public void run()
      {
        byte[] arrayOfByte = new byte[1024];
        try
        {
          int i = 0;
          while (i != -1)
          {
            i = this.val$is.read(arrayOfByte);
            if ((i > 0) || (i != 0))
              continue;
            try
            {
              Thread.sleep(200L);
            }
            catch (Exception localException2)
            {
            }
          }
        }
        catch (Exception localException1)
        {
        }
      }
    }).start();
  }

  void help()
  {
  }

  private CacheObject getSelectedCacheObject()
  {
    int[] arrayOfInt = this.activeTable.getSelectedRows();
    if (arrayOfInt.length == 1)
      return this.activeTable.getCacheObject(arrayOfInt[0]);
    return null;
  }

  private CacheObject[] getSelectedCacheObjects()
  {
    int[] arrayOfInt = this.activeTable.getSelectedRows();
    int i = this.activeTable.getRowCount();
    for (int j = 0; j < arrayOfInt.length; j++)
    {
      if (arrayOfInt[j] < i)
        continue;
      Trace.println("Bug in JTable ?, getRowCount() = " + i + " , but getSelectedRows() contains: " + arrayOfInt[j], TraceLevel.BASIC);
      return new CacheObject[0];
    }
    CacheObject[] arrayOfCacheObject = new CacheObject[arrayOfInt.length];
    for (int k = 0; k < arrayOfInt.length; k++)
      arrayOfCacheObject[k] = this.activeTable.getCacheObject(arrayOfInt[k]);
    return arrayOfCacheObject;
  }

  private void showDocument(URL paramURL)
  {
    new Thread(new Runnable(paramURL)
    {
      private final URL val$page;

      public void run()
      {
        BrowserSupport.showDocument(this.val$page);
      }
    }).start();
  }

  private String getResource(String paramString)
  {
    return ResourceManager.getMessage(paramString);
  }

  private Icon getDummyIcon()
  {
    if (this.dummy == null)
    {
      try
      {
        this.dummy = ResourceManager.getIcon("java32.image");
      }
      catch (Throwable localThrowable)
      {
      }
      if (this.dummy == null)
        this.dummy = new ImageIcon();
      Image localImage = this.dummy.getImage().getScaledInstance(20, 20, 1);
      this.dummy.setImage(localImage);
    }
    return this.dummy;
  }

  private Icon getIcon(String paramString)
  {
    try
    {
      return ResourceManager.getIcon(paramString);
    }
    catch (Throwable localThrowable)
    {
    }
    return getDummyIcon();
  }

  public JButton createRunButton(String paramString)
  {
    JButton localJButton = createImageButton(paramString, 8);
    localJButton.addMouseListener(new MouseAdapter(localJButton)
    {
      boolean clicked;
      Timer t;
      private final JButton val$b;

      public void mouseClicked(MouseEvent paramMouseEvent)
      {
        if (!CacheViewer.this.runPopup.isVisible())
          this.clicked = true;
      }

      public void mousePressed(MouseEvent paramMouseEvent)
      {
        this.clicked = false;
        this.t = new Timer(500, new ActionListener()
        {
          public void actionPerformed(ActionEvent paramActionEvent)
          {
            if ((!CacheViewer.22.this.clicked) && (CacheViewer.this.runButton.isEnabled()))
            {
              CacheViewer.this.runPopup.show(CacheViewer.22.this.val$b, 0, CacheViewer.22.this.val$b.getHeight());
              CacheViewer.22.this.val$b.getModel().setPressed(false);
            }
          }
        });
        this.t.setRepeats(false);
        this.t.start();
      }
    });
    localJButton.addKeyListener(new KeyAdapter(localJButton)
    {
      private final JButton val$b;

      public void keyPressed(KeyEvent paramKeyEvent)
      {
        if ((paramKeyEvent.getKeyCode() == 40) || (paramKeyEvent.getKeyCode() == 225))
        {
          Timer localTimer = new Timer(50, new ActionListener()
          {
            public void actionPerformed(ActionEvent paramActionEvent)
            {
              if ((!CacheViewer.this.runPopup.isVisible()) && (CacheViewer.this.runButton.isEnabled()))
                CacheViewer.this.runPopup.show(CacheViewer.23.this.val$b, 0, CacheViewer.23.this.val$b.getHeight());
            }
          });
          localTimer.setRepeats(false);
          localTimer.start();
        }
      }
    });
    localJButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent paramActionEvent)
      {
        if (!CacheViewer.this.runPopup.isVisible())
          CacheViewer.this.runApplication();
      }
    });
    return localJButton;
  }

  private JButton createImageButton(String paramString, int paramInt)
  {
    int i = 32 + paramInt;
    25 local25 = new JButton(i)
    {
      private final int val$w;

      public Dimension getPreferredSize()
      {
        return new Dimension(this.val$w, 32);
      }

      public Dimension getMinimumSize()
      {
        return new Dimension(this.val$w, 32);
      }

      public Dimension getMaximumSize()
      {
        return new Dimension(this.val$w, 32);
      }
    };
    setButtonIcons(local25, paramString + ".icon");
    local25.setToolTipText(getResource(paramString + ".tooltip"));
    return local25;
  }

  private void setButtonIcons(JButton paramJButton, String paramString)
  {
    ImageIcon[] arrayOfImageIcon = ResourceManager.getIcons(paramString);
    paramJButton.setIcon(arrayOfImageIcon[0] != null ? arrayOfImageIcon[0] : getDummyIcon());
    if (arrayOfImageIcon[1] != null)
      paramJButton.setPressedIcon(arrayOfImageIcon[1]);
    if (arrayOfImageIcon[2] != null)
      paramJButton.setDisabledIcon(arrayOfImageIcon[2]);
    if (arrayOfImageIcon[3] != null)
      paramJButton.setRolloverIcon(arrayOfImageIcon[3]);
  }

  private JMenuItem createMI(String paramString, ActionListener paramActionListener)
  {
    JMenuItem localJMenuItem = new JMenuItem(ResourceManager.getMessage(paramString));
    String str1 = paramString + ".mnemonic";
    String str2 = ResourceManager.getMessage(str1);
    if (!str2.equals(str1))
      localJMenuItem.setMnemonic(ResourceManager.getVKCode(str1));
    localJMenuItem.addActionListener(paramActionListener);
    return localJMenuItem;
  }

  private void exitViewer()
  {
    Rectangle localRectangle = getBounds();
    Config.setStringProperty("deployment.javaws.viewer.bounds", "" + localRectangle.x + "," + localRectangle.y + "," + localRectangle.width + "," + localRectangle.height);
    if (!this.wasDirty)
      Config.get().storeIfNeeded();
    setVisible(false);
    dispose();
  }

  public void valueChanged(ListSelectionEvent paramListSelectionEvent)
  {
    enableButtons();
  }

  public void popupApplicationMenu(Component paramComponent, int paramInt1, int paramInt2)
  {
    CacheObject localCacheObject = getSelectedCacheObject();
    if (localCacheObject != null)
      this.popup.show(paramComponent, paramInt1, paramInt2);
  }

  private boolean canWriteSys()
  {
    String str = Config.getSystemCacheDirectory();
    if (str != null)
      try
      {
        File localFile = new File(str);
        return localFile.canWrite();
      }
      catch (Exception localException)
      {
      }
    return false;
  }

  public void enableButtons()
  {
    CacheObject[] arrayOfCacheObject = getSelectedCacheObjects();
    boolean bool1 = arrayOfCacheObject.length == 1;
    boolean bool2 = arrayOfCacheObject.length > 0;
    int i = (this.activeTable != this.sysResourceTable) && (this.activeTable != this.sysJnlpTable) ? 1 : 0;
    this.showButton.setEnabled(bool1);
    this.showMI.setEnabled(bool1);
    this.removeRemovedButton.setEnabled((bool2) && (i != 0));
    this.importButton.setEnabled(bool2);
    this.importMI.setEnabled(bool2);
    this.removeButton.setEnabled((bool2) && ((i != 0) || (canWriteSys())));
    this.removeMI.setEnabled((bool2) && (i != 0));
    this.removeResourceButton.setEnabled((bool2) && ((i != 0) || (canWriteSys())));
    this.runButton.setEnabled(false);
    this.runOnlineMI.setEnabled(false);
    this.runOfflineMI.setEnabled(false);
    this.installButton.setEnabled(false);
    this.installMI.setEnabled(false);
    this.homeButton.setEnabled(false);
    this.homeMI.setEnabled(false);
    this.showResourceButton.setEnabled(false);
    this.showResourceMI.setEnabled(false);
    this.onlineMI.setEnabled(false);
    this.offlineMI.setEnabled(false);
    if (bool1)
    {
      LaunchDesc localLaunchDesc = arrayOfCacheObject[0].getLaunchDesc();
      if (localLaunchDesc != null)
      {
        if (localLaunchDesc.isApplicationDescriptor())
        {
          this.runButton.setEnabled(true);
          this.onlineMI.setEnabled(true);
          this.runOnlineMI.setEnabled(true);
          setButtonIcons(this.runButton, "viewer.run.online.icon");
          this.runButton.setToolTipText(getResource("viewer.run.online.tooltip"));
          if (localLaunchDesc.getInformation().supportsOfflineOperation())
          {
            this.runButton.setEnabled(true);
            this.offlineMI.setEnabled(true);
            this.runOfflineMI.setEnabled(true);
            if (localLaunchDesc.getLocation() == null)
            {
              setButtonIcons(this.runButton, "viewer.run.offline.icon");
              this.runButton.setToolTipText(getResource("viewer.run.offline.tooltip"));
            }
          }
          LocalInstallHandler localLocalInstallHandler = LocalInstallHandler.getInstance();
          if (localLocalInstallHandler.isLocalInstallSupported())
          {
            LocalApplicationProperties localLocalApplicationProperties = arrayOfCacheObject[0].getLocalApplicationProperties();
            localLocalApplicationProperties.refreshIfNecessary();
            if ((!localLocalInstallHandler.isShortcutExists(localLocalApplicationProperties)) && (this.activeTable == this.jnlpTable))
            {
              ShortcutDesc localShortcutDesc = localLaunchDesc.getInformation().getShortcut();
              if ((localShortcutDesc == null) || (localShortcutDesc.getMenu()) || (localShortcutDesc.getDesktop()))
              {
                this.installButton.setEnabled(true);
                this.installMI.setEnabled(true);
              }
            }
          }
        }
        if (localLaunchDesc.getInformation().getHome() != null)
        {
          this.homeButton.setEnabled(true);
          this.homeMI.setEnabled(true);
        }
        this.showResourceButton.setEnabled(true);
        this.showResourceMI.setEnabled(true);
      }
    }
    this.activeTable.setEnabled(this.activeTable.getRowCount() > 0);
  }

  private void startWatchers()
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        long l1 = Cache.getLastAccessed(false);
        long l2 = Cache.getLastAccessed(true);
        long l3 = 0L;
        String str = Cache.getRemovePath();
        File localFile = new File(str);
        if ((localFile != null) && (localFile.exists()))
          l3 = localFile.lastModified();
        do
        {
          try
          {
            Thread.sleep(2000L);
          }
          catch (InterruptedException localInterruptedException)
          {
            Trace.ignored(localInterruptedException);
            continue;
          }
          long l4 = Cache.getLastAccessed(false);
          long l5 = Cache.getLastAccessed(true);
          long l6 = 0L;
          if ((localFile != null) && (localFile.exists()))
            l6 = localFile.lastModified();
          if (l4 != l1)
          {
            l1 = l4;
            SwingUtilities.invokeLater(new Runnable()
            {
              public void run()
              {
                if (CacheViewer.this.jnlpTable != null)
                  CacheViewer.this.jnlpTable.reset();
                if (CacheViewer.this.resourceTable != null)
                  CacheViewer.this.resourceTable.reset();
                CacheViewer.this.enableButtons();
                CacheViewer.this.sizeLabel.setText(CacheViewer.this.activeTable.getSizeLabelText());
              }
            });
          }
          if (l5 != l2)
          {
            l2 = l5;
            SwingUtilities.invokeLater(new Runnable()
            {
              public void run()
              {
                if (CacheViewer.this.sysJnlpTable != null)
                  CacheViewer.this.sysJnlpTable.reset();
                if (CacheViewer.this.sysResourceTable != null)
                  CacheViewer.this.sysResourceTable.reset();
                CacheViewer.this.enableButtons();
                CacheViewer.this.sizeLabel.setText(CacheViewer.this.activeTable.getSizeLabelText());
              }
            });
          }
          if (l6 == l3)
            continue;
          l3 = l6;
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              if (CacheViewer.this.importTable != null)
                CacheViewer.this.importTable.reset();
              CacheViewer.this.enableButtons();
            }
          });
        }
        while (CacheViewer.this.isShowing());
      }
    }).start();
  }

  public static void showCacheViewer(JFrame paramJFrame)
  {
    Cache.reset();
    Main.initializeExecutionEnvironment();
    CacheViewer localCacheViewer = new CacheViewer(paramJFrame);
    localCacheViewer.setModal(true);
    String str1 = Config.getStringProperty("deployment.javaws.viewer.bounds");
    if (str1 != null)
    {
      StringTokenizer localStringTokenizer = new StringTokenizer(str1, ",");
      int[] arrayOfInt = new int[4];
      for (int i = 0; i < 4; i++)
      {
        if (!localStringTokenizer.hasMoreTokens())
          continue;
        String str2 = localStringTokenizer.nextToken();
        try
        {
          arrayOfInt[i] = Integer.parseInt(str2);
        }
        catch (NumberFormatException localNumberFormatException)
        {
        }
      }
      if (i == 4)
        localCacheViewer.setBounds(arrayOfInt[0], arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
    }
    else
    {
      localCacheViewer.setBounds(100, 100, 720, 360);
      com.sun.deploy.ui.UIFactory.placeWindow(localCacheViewer);
    }
    localCacheViewer.startWatchers();
    localCacheViewer.setVisible(true);
  }

  class VSeparator extends JSeparator
  {
    public VSeparator()
    {
      super();
    }

    public Dimension getPreferredSize()
    {
      Dimension localDimension = getUI().getPreferredSize(this);
      localDimension.height = 20;
      return localDimension;
    }

    public Dimension getMaximumSize()
    {
      return getPreferredSize();
    }
  }

  private class remainingSpacer extends JComponent
  {
    private final CacheViewer this$0;

    private remainingSpacer()
    {
      this.this$0 = this$1;
    }

    public Dimension getPreferredSize()
    {
      Dimension localDimension1 = super.getPreferredSize();
      Container localContainer = getParent();
      int i = localContainer.getWidth();
      int j = 0;
      Component[] arrayOfComponent = localContainer.getComponents();
      for (int k = 0; k < arrayOfComponent.length; k++)
      {
        if (equals(arrayOfComponent[k]))
          continue;
        Dimension localDimension2 = arrayOfComponent[k].getPreferredSize();
        j += localDimension2.width;
      }
      if (i > j)
        localDimension1.width = (i - j);
      return localDimension1;
    }

    remainingSpacer(CacheViewer.1 arg2)
    {
      this();
    }
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/javaws.jar
 * Qualified Name:     com.sun.javaws.ui.CacheViewer
 * JD-Core Version:    0.6.0
 */