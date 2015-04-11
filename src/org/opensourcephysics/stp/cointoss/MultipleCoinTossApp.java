/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.stp.cointoss;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.frames.HistogramFrame;

/**
 *  <b>Input Parameters</b> <br>
 *  <code>probability</code> probability of heads coming up, which should be
 *  between zero and unity, inclusive. <br>
 *  <code>coins to flip</code> number of coins to flip.
 *
 * @author     jgould
 * @created    January 23, 2002
 * Modified    Natali  Jan 22, 2003
 */
public class MultipleCoinTossApp extends AbstractSimulation {
  Random random;
  int totalFlips;
  double sumHeads = 0, sum2Heads = 0, probability, coinsToFlip, time, dt;
  HistogramFrame histogramFrame = new HistogramFrame("Heads", "Occurences", "Histogram");
  NumberFormat nf;

  /**
   * Constructor MultipleCoinTossApp
   */
  public MultipleCoinTossApp() {
    random = new Random();
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
  }

  public void initialize() {
    probability = control.getDouble("probability");
    coinsToFlip = control.getInt("coins to flip");
    random.setSeed(System.currentTimeMillis());
    histogramFrame.setPreferredMinMaxX(0, coinsToFlip);
    histogramFrame.setPreferredMinMaxY(0, 10);
    histogramFrame.setAutoscaleY(true);
    histogramFrame.clearData();
    histogramFrame.repaint();
    totalFlips = 0;
    sumHeads = 0;
    sum2Heads = 0;
    stopRunning();
  }

  public void start() {
    probability = control.getDouble("probability");
  }

  public void reset() {
    control.setValue("probability", 0.5);
    control.setValue("coins to flip", 100);
    control.clearMessages();
    initialize();
    enableStepsPerDisplay(true);
  }

  protected void doStep() {
    probability = control.getDouble("probability");
    int nHeads = 0;
    for(int i = 0; i<coinsToFlip; i++) {
      if(random.nextDouble()<probability) {
        nHeads++;
      }
    }
    totalFlips++;
    histogramFrame.append(nHeads);
    sumHeads += nHeads;
    sum2Heads += nHeads*nHeads;
  }

  public void stopRunning() {
    control.clearMessages();
    double avg = (totalFlips>0)
                 ? sumHeads/totalFlips
                 : 0;
    double avg2 = (totalFlips>0)
                  ? sum2Heads/totalFlips
                  : 0;
    control.println("<H> = "+nf.format(avg));
    control.println("<H*H> = "+nf.format(avg2));
    double sigma = Math.sqrt(avg2-avg*avg);
    if(totalFlips>0) {
      control.println("sigma = "+nf.format(sigma));
    } else {
      control.println("sigma = undefined");
    }
    control.println("number of trials= "+totalFlips);
  }

  /**
   * Switch to the WRApp user interface.
   */
  public void switchGUI() {
	stopSimulation();
    Runnable runner = new Runnable() {
      public synchronized void run() {
        OSPRuntime.disableAllDrawing = true;
        OSPFrame mainFrame = getMainFrame();
        XMLControlElement xml = new XMLControlElement(getOSPApp());
        WindowListener[] listeners = mainFrame.getWindowListeners();
        int closeOperation = mainFrame.getDefaultCloseOperation();
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setKeepHidden(true);
        mainFrame.dispose();
        MultipleCoinTossWRApp app = new MultipleCoinTossWRApp();
        MultipleCoinTossWRAppControl c = new MultipleCoinTossWRAppControl(app, app.histogramFrame, null);
        c.getMainFrame().setDefaultCloseOperation(closeOperation);
        for(int i = 0, n = listeners.length; i<n; i++) {
          if(listeners[i].getClass().getName().equals("org.opensourcephysics.tools.Launcher$FrameCloser")) {
            c.getMainFrame().addWindowListener(listeners[i]);
          }
        }
        c.loadXML(xml, true);
        app.customize();
        c.resetSimulation();
        System.gc();
        OSPRuntime.disableAllDrawing = false;
        GUIUtils.showDrawingAndTableFrames();
      }

    };
    Thread t = new Thread(runner);
    t.start();
  }

  void customize() {
    OSPFrame f = getMainFrame();
    if((f==null)||!f.isDisplayable()) {
      return;
    }
    JMenu menu = f.getMenu("Display");
    JMenuItem item = new JMenuItem("Switch GUI");
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        switchGUI();
      }

    });
    menu.add(item);
    addChildFrame(histogramFrame);
  }

  public static void main(String[] args) {
    MultipleCoinTossApp app = new MultipleCoinTossApp();
    SimulationControl.createApp(app, args);
    app.customize();
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
