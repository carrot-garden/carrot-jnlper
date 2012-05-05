package com.sun.deploy.panel;

import com.sun.deploy.config.Config;
import com.sun.deploy.resources.ResourceManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeRenderers
{
  private static final DefaultTreeCellRenderer _renderer = new DefaultTreeCellRenderer()
  {
    private JCheckBox _checkBox = new JCheckBox()
    {
      public void paintBorder(Graphics paramGraphics)
      {
        getBorder().paintBorder(this, paramGraphics, 0, 0, getWidth(), getHeight());
      }

      public void paint(Graphics paramGraphics)
      {
        super.paint(paramGraphics);
      }
    };
    private JPanel _cbPanel = new JPanel(new BorderLayout());
    private final JRadioButton _radio = new JRadioButton()
    {
      public void paintBorder(Graphics paramGraphics)
      {
        getBorder().paintBorder(this, paramGraphics, 0, 0, getWidth(), getHeight());
      }
    };
    private JPanel _radioPanel = new JPanel(new BorderLayout());
    private JPanel _textPanel = new JPanel(new BorderLayout());
    private JButton _browse = new JButton(ResourceManager.getMessage("deploy.advanced.browse.browse_btn"));
    private JTextField _path = new JTextField("");
    private final JLabel _label = new JLabel();
    private JPanel _labelPanel = new JPanel(new BorderLayout());
    private Border _emptyBorder = BorderFactory.createEmptyBorder();
    private Border _invisBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    public Component getTreeCellRendererComponent(JTree paramJTree, Object paramObject, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, int paramInt, boolean paramBoolean4)
    {
      Object localObject;
      if ((paramObject instanceof ToggleProperty))
      {
        localObject = (ToggleProperty)paramObject;
        this._cbPanel.removeAll();
        if (paramBoolean1)
        {
          this._checkBox.setForeground(getTextSelectionColor());
          this._checkBox.setBackground(getBackgroundSelectionColor());
          this._cbPanel.setBackground(getBackgroundSelectionColor());
          this._checkBox.setBorder(BorderFactory.createLineBorder(getBorderSelectionColor()));
        }
        else
        {
          this._checkBox.setForeground(getTextNonSelectionColor());
          this._checkBox.setBackground(getBackgroundNonSelectionColor());
          this._cbPanel.setBackground(getBackgroundNonSelectionColor());
          this._checkBox.setBorder(this._invisBorder);
        }
        this._checkBox.setSelected(((ToggleProperty)localObject).isSelected());
        this._checkBox.setText(((ToggleProperty)localObject).getDescription());
        this._checkBox.setFont(paramJTree.getFont());
        this._checkBox.setEnabled(!Config.get().isPropertyLocked(((ToggleProperty)localObject).getPropertyName()));
        this._checkBox.setRequestFocusEnabled(paramBoolean4);
        this._checkBox.setOpaque(false);
        this._cbPanel.setOpaque(true);
        this._cbPanel.setToolTipText(((ToggleProperty)localObject).getTooltip());
        this._cbPanel.add(this._checkBox);
        return this._cbPanel;
      }
      if ((paramObject instanceof RadioProperty))
      {
        localObject = (RadioProperty)paramObject;
        this._radioPanel.removeAll();
        if (paramBoolean1)
        {
          this._radio.setForeground(getTextSelectionColor());
          this._radio.setBackground(getBackgroundSelectionColor());
          this._radioPanel.setBackground(getBackgroundSelectionColor());
          this._radio.setBorder(BorderFactory.createLineBorder(getBorderSelectionColor()));
        }
        else
        {
          this._radio.setForeground(getTextNonSelectionColor());
          this._radio.setBackground(getBackgroundNonSelectionColor());
          this._radioPanel.setBackground(getBackgroundNonSelectionColor());
          this._radio.setBorder(this._invisBorder);
        }
        this._radio.setText(((RadioProperty)localObject).getDescription());
        this._radio.setFont(paramJTree.getFont());
        this._radio.setSelected(((RadioProperty)localObject).isSelected());
        this._radio.setEnabled(!Config.get().isPropertyLocked(((RadioProperty)localObject).getGroupName()));
        this._radio.setRequestFocusEnabled(paramBoolean4);
        this._radio.setOpaque(false);
        this._radioPanel.setOpaque(true);
        this._radioPanel.setToolTipText(((RadioProperty)localObject).getTooltip());
        this._radioPanel.add(this._radio);
        return this._radioPanel;
      }
      if ((paramObject instanceof TextFieldProperty))
      {
        localObject = (TextFieldProperty)paramObject;
        this._textPanel.removeAll();
        if (paramBoolean1)
        {
          this._textPanel.setBackground(getBackgroundSelectionColor());
          this._textPanel.setBorder(BorderFactory.createLineBorder(getBorderSelectionColor()));
        }
        else
        {
          this._textPanel.setBackground(getBackgroundNonSelectionColor());
          this._textPanel.setBorder(this._invisBorder);
        }
        this._path.setColumns(22);
        this._textPanel.add(this._path, "Center");
        this._textPanel.add(this._browse, "East");
        this._browse.setMnemonic(ResourceManager.getVKCode("deploy.advanced.browse.browse_btn.mnemonic"));
        this._textPanel.setOpaque(true);
        boolean bool = !Config.get().isPropertyLocked(((TextFieldProperty)localObject).getPropertyName());
        this._path.setEnabled(bool);
        this._browse.setEnabled(bool);
        this._path.setText(((TextFieldProperty)localObject).getValue());
        this._path.setFont(paramJTree.getFont());
        return this._textPanel;
      }
      this._labelPanel.removeAll();
      if (paramBoolean1)
      {
        this._label.setForeground(getTextSelectionColor());
        this._label.setBackground(getBackgroundSelectionColor());
        this._label.setBorder(BorderFactory.createLineBorder(getBorderSelectionColor()));
      }
      else
      {
        this._label.setForeground(getTextNonSelectionColor());
        this._label.setBackground(getBackgroundNonSelectionColor());
        this._label.setBorder(this._invisBorder);
      }
      this._label.setFont(paramJTree.getFont());
      this._label.setText(String.valueOf(paramObject));
      this._label.setOpaque(true);
      this._labelPanel.add(this._label);
      return (Component)this._labelPanel;
    }
  };

  public static DefaultTreeCellRenderer getRenderer()
  {
    return _renderer;
  }
}

/* Location:           /opt/sun/java32/jdk1.7.0_04/jre/lib/deploy.jar
 * Qualified Name:     com.sun.deploy.panel.TreeRenderers
 * JD-Core Version:    0.6.0
 */