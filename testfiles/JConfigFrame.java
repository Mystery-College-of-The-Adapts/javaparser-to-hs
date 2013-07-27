/*
 * Copyright (c) 2000-2005 CyberFOX Software, Inc. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the
 *  Free Software Foundation, Inc.
 *  59 Temple Place
 *  Suite 330
 *  Boston, MA 02111-1307
 *  USA
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;

/**
 * Implements the "Configure" frames. This holds all the configuration options.
 *
 * @version $Revision: 1.38 $
 */
public class JConfigFrame implements ActionListener {
  private JFrame mainFrame;
  private boolean buttonPressed = false;
  private List allTabs;
  private static int cfgCount = 1;

  public void spinWait() {
    while(!buttonPressed) {
      try { //noinspection MultiplyOrDivideByPowerOfTwo,BusyWait
        Thread.sleep(Constants.ONE_SECOND/2);
      } catch(InterruptedException ignored) {
        //  We don't care that we caught an exception, just that we woke up.
      }
    }
  }

  public JConfigFrame() {
    mainFrame = createConfigFrame();
    Rectangle rec = OptionUI.findCenterBounds(mainFrame.getPreferredSize());
    mainFrame.setLocation(rec.x, rec.y);
    show();
  }

  public final void show() {
    for(int i = 0; i<allTabs.size(); i++) {
      JConfigTab jct = (JConfigTab) allTabs.get(i);

      jct.updateValues();
    }
    mainFrame.setState(Frame.NORMAL);
    mainFrame.setVisible(true);
  }

  private void applyAll() {
    for(int i = 0; i<allTabs.size(); i++) {
      JConfigTab jct = (JConfigTab) allTabs.get(i);

      jct.apply();
    }
  }

  private void cancelAll() {
    for(int i = 0; i<allTabs.size(); i++) {
      JConfigTab jct = (JConfigTab) allTabs.get(i);

      jct.cancel();
    }
  }

  public void actionPerformed(ActionEvent ae) {
    String actionString = ae.getActionCommand();

    if(actionString.equals("Save")) {
      applyAll();
      JConfig.updateComplete();
      JConfig.saveConfiguration();
    } else if(actionString.equals("Cancel")) {
      cancelAll();
    }

    mainFrame.setVisible(false);
    buttonPressed = true;
  }

  public static JPanel buildButtonPane(ActionListener al) {
    JPanel tp = new JPanel();

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setToolTipText("Cancel any changes made.");
    JButton saveButton = new JButton("Save");
    saveButton.setToolTipText("Apply changes and save settings.");

    tp.add(cancelButton, BorderLayout.WEST);
    tp.add(  saveButton, BorderLayout.CENTER);

    cancelButton.addActionListener(al);
    saveButton.addActionListener(al);

    return(tp);
  }

  private static void anotherConfig() {
    cfgCount++;
  }

  private JFrame createConfigFrame() {
    JTabbedPane jtpAllTabs = new JTabbedPane();
    final JFrame w;

    if(cfgCount == 2) {
      w = new JFrame("Configuration Manager (2)");
    } else {
      anotherConfig();
      w = new JFrame("Configuration Manager");
    }
    Platform.setMacFrameMenu(w);

    Container contentPane = w.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(jtpAllTabs, BorderLayout.CENTER);

    allTabs = new ArrayList();

    //  Add all non-server-specific tabs here.
    allTabs.add(new JConfigGeneralTab());

    List serverTabs = AuctionServerManager.getInstance().getServerConfigurationTabs();
    JConfigTab jct;
    for(Iterator it = serverTabs.iterator(); it.hasNext();) {
      jct = (JConfigTab)it.next();
      allTabs.add(jct);
    }

    //  Stub the browser tab under MacOSX, so they don't try to use it.
    if(Platform.isMac()) {
      allTabs.add(new JConfigMacBrowserTab());
    } else {
      allTabs.add(new JConfigBrowserTab());
    }
    allTabs.add(new JConfigFirewallTab());
    allTabs.add(new JConfigSnipeTab());
    allTabs.add(new JConfigFilePathTab());
    allTabs.add(new JConfigWebserverTab());

    //  Don't add the tabs that don't work yet.  -- BUGBUG (need to write the tabs)
    allTabs.add(new JConfigSecurityTab());
    allTabs.add(new JConfigAdvancedTab());

    //  HACKHACK -- Presently all tabs created need to have 3 rows of
    //  GridLayout.  In general, all tabs have to have the same number
    //  of rows.  This is likely to suck, in the long run.  For now,
    //  it's the requirement.  If you have more or less, the display
    //  either for that tab (if it has less), or for all the others
    //  (if it has more) will look somewhat wonky.

    //  Loop over all tabs, and add them to the display.
    for(int i = 0; i<allTabs.size(); i++) {
      jct = (JConfigTab) allTabs.get(i);
      jct.setOpaque(true);
      jtpAllTabs.addTab(jct.getTabName(), jct);
    }

    jtpAllTabs.setSelectedIndex(0);
    contentPane.add(buildButtonPane(this), BorderLayout.SOUTH);

    w.addWindowListener(new IconifyingWindowAdapter(w));
    w.pack();
    w.setResizable(false);
    return w;
  }

  private class JConfigSecurityTab extends JConfigStubTab {
    public String getTabName() { return("Security"); }
  }

  private final class JConfigMacBrowserTab extends JConfigStubTab {
    public String getTabName() { return("Browser"); }

    JConfigMacBrowserTab() {
      JLabel newLabel = new JLabel("Under MacOSX, the browser does not need to be configured.");
      setLayout(new BorderLayout());
      add(newLabel, BorderLayout.CENTER);
    }
  }

  public static class IconifyingWindowAdapter extends WindowAdapter {
    private final JFrame _window;

    IconifyingWindowAdapter(JFrame window) {
      _window = window;
    }

    public void windowIconified(WindowEvent we) {
      super.windowIconified(we);
      if(Platform.isWindows() && Platform.isTrayEnabled()) {
        if(JConfig.queryConfiguration("windows.tray", "true").equals("true") &&
           JConfig.queryConfiguration("windows.minimize", "true").equals("true")) {
          _window.setVisible(false);
        }
      }
    }

    public void windowDeiconified(WindowEvent we) {
      super.windowDeiconified(we);
      if(Platform.isWindows() && Platform.isTrayEnabled()) {
        if(JConfig.queryConfiguration("windows.tray", "true").equals("true") &&
           JConfig.queryConfiguration("windows.minimize", "true").equals("true")) {
          _window.setState(Frame.NORMAL);
          _window.setVisible(true);
        }
      }
    }
  }
}
