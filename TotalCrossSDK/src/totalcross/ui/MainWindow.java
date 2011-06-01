/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.ui;

import totalcross.sys.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.unit.*;

/**
 * MainWindow is the main window of a UI-based application.
 * <p>
 * All TotalCross programs with an user-interface must have <b>one and only one</b> main window.
 * <p>
 * Here is an example showing a basic application:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 *    Edit edName;
 *    public void initUI()
 *    {
 *       ... initialization code ...
 *       add(new Label("Name:"), LEFT,TOP+2);
 *       add(edName = new Edit(""), AFTER,SAME-2);
 *    }
 * }
 * </pre>
 */

public class MainWindow extends Window implements totalcross.MainClass
{
   protected TimerEvent firstTimer;
   protected TimerEvent lastTimer;
   
   private TimerEvent startTimer;
   static MainWindow mainWindowInstance;
   private static int lastMinInterval;
   private boolean restoreRegistry;
   private static int timeAvailable;

   static Font defaultFont = Font.getFont(Font.DEFAULT, false, Font.NORMAL_SIZE);

   /** Constructs a main window with no title and no border. */
   public MainWindow()
   {
      this(null,NO_BORDER);
   }

   /** Constructs a main window with the given title and border style.
    * @see #NO_BORDER
    * @see #RECT_BORDER
    * @see #ROUND_BORDER
    * @see #TAB_BORDER
    * @see #TAB_ONLY_BORDER
    * @since SuperWaba 2.0b4
    */
   public MainWindow(String title, byte style) // guich@112
   {
      super(title,style);
      setX = 0; setY = 0; setW = Settings.screenWidth; setH = Settings.screenHeight; setFont = this.font;

      boolean isAndroid = Settings.platform.equals(Settings.ANDROID);
      Settings.useSIPBox = isAndroid;
      Settings.enableWindowTransitionEffects = !isAndroid; // guich@tc126_30
      if (Settings.fingerTouch) // guich@tc120_48
         Settings.touchTolerance = 10;
      
      if ((""+Settings.deviceId).indexOf("Treo") >= 0) // guich@tc113_36: disable virtual keyboard for all Treo family
         Settings.virtualKeyboard = false;

      // update some settings
      highResPrepared = true; // main window is always prepared
      setBackColor(UIColors.controlsBack = 0xA0D8EC); // guich@200b4_39 - guich@tc100: set the controlsBack to this color

      uitip = new ToolTip(null,"");

      if (mainWindowInstance == null)
      {
         mainWindowInstance = this;
         mainWindowCreate();
         zStack.push(this); // guich
         topMost = this;
      }

      canDrag = false; // we can't drag the mainwindow.
      
      if (Settings.isWindowsDevice() && Settings.romVersion >= 500) // guich@tc115_90
         try
         {
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\System\\ErrorReporting\\DumpSettings", "DumpEnabled", 1);
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\System\\ErrorReporting\\DumpSettings", "UploadClient", "\\Windows\\Dw.exe1");
            restoreRegistry = true;
         }
         catch (Exception e) {e.printStackTrace();} 
   }

   void mainWindowCreate()
   {
      totalcross.Launcher.instance.registerMainWindow(this);
   }
   void mainWindowCreate4D() {} // not needed at device

   /** Sets the default font used in all controls created. To change the default font, assign it to this member in the MainWindow constructor,
    * making it the FIRST LINE in the constructor; you'll not be able to use super(title,border): change by setBorderStyle and setTitle, after
    * the defaultFont assignment. Example:
    * <pre>
    * public MyApp()
    * {
    *    MainWindow.setDefaultFont(Font.getFont(false, Font.NORMAL_SIZE+2));
    *    setBorderStyle(TAB_ONLY_BORDER);
    *    setTitle("My application");
    * }
    * </pre>
    * @since TotalCross 1.0 beta3
    */
   public static void setDefaultFont(Font newFont)
   {
      defaultFont = newFont;
      mainWindowInstance.setFont(newFont);
      uitip.setFont(newFont); // guich@tc100b5_58
      mainWindowInstance.setTitleFont(newFont.asBold()); // guich@tc125_4
   }

   /** Returns the default font.
    * @since TotalCross 1.0 beta3
    */
   public static Font getDefaultFont()
   {
      return defaultFont;
   }

   /** Changes the user interface style to the given one.
    * This method must be called in the MainWindow's constructor, and only once. E.g.:
    * <pre>
    * public class Foo extends MainWindow
    * {
    *    public Foo()
    *    {
    *       super("Hi bar",TAB_ONLY_BORDER);
    *       setUIStyle(totalcross.sys.Settings.FLAT);
    * </pre>
    * @see totalcross.sys.Settings#PalmOS
    * @see totalcross.sys.Settings#WinCE
    * @see totalcross.sys.Settings#Flat
    * @see totalcross.sys.Settings#Vista
    * @since SuperWaba 5.05
    */
   public void setUIStyle(byte style)
   {
      if (style == Settings.PalmOS)
         setBackColor(Color.WHITE);
      Settings.uiStyle = style;
      Control.uiStyleChanged();
   }

   /**
   * Notifies the application that it should stop executing and exit. It will
   * exit after executing any pending events. If the underlying system supports
   * it, the exitCode passed is returned to the program that started the app.
   * Note: in PalmOS and in AppletViewer/Browser the exitCode is useless.
   * <p>If you want your code to be called when the VM exits, extend the onExit method.
   * @see #onExit
   */
   public static final void exit(int exitCode)
   {
      totalcross.Launcher.instance.exit(exitCode);
   }
   native public static final void exit4D(int exitCode);

   /**
    * Notifies the application that it should be minimized, that is, transfered
    * to the background. Currently, this is only supported on Java, BlackBerry, WinCE, Win32
    * and Android. Whenever the application is minimized, the following call back function
    * will be called: {@link #onMinimize()}. Note: On Android, calling {@link #minimize()} will
    * pause the application execution and it can only be restored manually by the user.
    * @see #onMinimize
    * @see #onRestore
    * @since TotalCross 1.10
    */
   public final void minimize() // bruno@tc110_89
   {
      totalcross.Launcher.instance.minimize();
   }
   native public final void minimize4D();

   /**
    * Notifies the application that it should be restored, that is, transfered
    * to the foreground. Currently, this is only supported on Java, BlackBerry, WinCE and Win32.
    * Whenever the application is restored, the following call back function will be called:
    * {@link #onRestore()}. Note: This method is supported on Android; the user must restore
    * the application manually.
    * @since TotalCross 1.10
    */
   public final void restore() // bruno@tc110_89
   {
      totalcross.Launcher.instance.restore();
   }
   native public final void restore4D();

   /** Returns the MainWindow of the current application. */
   public static MainWindow getMainWindow()
   {
      return mainWindowInstance;
   }

   /**
   * Adds a timer to a control. This method is protected, the public
   * method to add a timer to a control is the addTimer() method in
   * the Control class.
   */
   protected TimerEvent addTimer(Control target, int millis)
   {
      TimerEvent t = new TimerEvent();
      addTimer(t,target,millis);
      return t;
   }

   /**
    * Adds the timer t to the target control. This method is protected, the public
    * method to add a timer to a control is the addTimer() method in
    * the Control class.
    */
    protected void addTimer(TimerEvent t, Control target, int millis)
    {
       addTimer(t, target, millis, true);
    }

    /**
     * Adds the timer t to the target control. This method is protected, the public
     * method to add a timer to a control is the addTimer() method in
     * the Control class.
     */
    protected void addTimer(TimerEvent t, Control target, int millis, boolean append)
    {
       t.target = target;
       t.millis = millis;
       t.lastTick = Vm.getTimeStamp();
       if (firstTimer == null) // first timer to be added
       {
          t.next = null;
          firstTimer = lastTimer = t;
       }
       else if (append) // appending timer to the end of the list
       {
          lastTimer.next = t;
          t.next = null;
          lastTimer = t;
       }
       else // inserting timer to the beginning of the list
       {
          t.next = firstTimer;
          firstTimer = t;
       }
       setTimerInterval(1); // forces a call to _onTimerTick inside the TC Event Thread
    }


   /**
   * Removes a timer. This method returns true if the timer was found
   * and removed and false if the given timer could not be found.
   * The <code>target</code> member is set to null.
   */
   public boolean removeTimer(TimerEvent timer)
   {
      if (timer == null)
         return false;
      TimerEvent t = firstTimer;
      TimerEvent prev = null;
      while (t != timer)
      {
         if (t == null)
            return false;
         prev = t;
         t = t.next;
      }
      if (prev == null)
         firstTimer = t.next;
      else
         prev.next = t.next;
      if (t.next == null) // this was the last timer, so now "prev" is the last one
         lastTimer = prev;
      if (timer.target != null) // not already removed?
         setTimerInterval(1); // forces a call to _onTimerTick inside the TC Event Thread
      timer.target = null; // guich@tc120_46
      return true;
   }

   /** Removes any timers that belongs to this window or whose paren't is null */
   void removeTimers(Window win)
   {
      boolean changed;
      do
      {
         changed = false;
         TimerEvent t = firstTimer;
         while (t != null)
         {
            Control c = (Control)t.target;
            Window w = c.getParentWindow();
            if (w == null || w == win)
            {
               changed = true;
               removeTimer(t);
               break;
            }
            t = t.next;
         }
      } while (changed);
   }

   /** Called by the VM when the application is starting. Setups a
     * timer that will call initUI after the event loop is started.
     * Never call this method directly; this method is not private
     * to prevent the compiler from removing it during optimization.
     * The timeAvail parameter is passed by the vm to show how much
     * time the user have to keep testing the demo vm. Even if this
     * value is not shown to the user, it is internally computed and 
     * the vm will exit when the counter reaches 0.
     */
   final public void appStarting(int timeAvail) // guich@200b4_107 - guich@tc126_46: added timeAvail parameter to show MessageBox from inside here.
   {
      timeAvailable = timeAvail;
      gfx = new Graphics(this); // revalidate the pixels
      startTimer = addTimer(1); // guich@567_17
   }

   /** Called by the system so we can finish things correctly.
     * Never call this method directly; this method is not private
     * to prevent the compiler from removing it during optimization.
    */
   final public void appEnding() // guich@200final_11: fixed when switching apps not calling killThreads.
   {
      // guich@tc100: do this at device side - if (resetSipToBottom) setStatePosition(0, Window.VK_BOTTOM); // fixes a problem of the window of the sip not correctly being returned to the bottom
      if (timeAvailable != 0) // guich@tc126_46: don't call app's onExit if time expired, since initUI was not called.
         onExit(); // guich@200b4_85
      if (restoreRegistry) // guich@tc115_90
         try
         {
            Registry.set(Registry.HKEY_LOCAL_MACHINE, "\\System\\ErrorReporting\\DumpSettings", "UploadClient", "\\Windows\\Dw.exe");
         }
         catch (Exception e) {} 
   }

   /**
   * Called just before an application exits.
   * When this is called, all threads are already killed.
   */
   public void onExit()
   {
   }

   /**
    * Called just after the application is minimized. Currently, this is only supported on
    * Java, BlackBerry, WinCE, Win32 and Android.
    * <br><br>The following applies to Android, only.
    * <br><br>The Android on TotalCross has a predefined life cycle:
    * <ol>
    * <li>onStart: called when the application starts
    * <li>onMinimize: called when the user press the home key, or when a call is received, or when the screen turns off
    * <li>onRestore: called after the call ends or the screen is turned on again
    * <li>onExit: called when the system decides that is time to finish the application. If the home key was pressed, this method
    * is called when another application is invoked.
    * </ol>
    * If the user press the home key and then forces the application to stop (by going to the Settings / Applications), then
    * all Litebase tables may be corrupted (actually, no data is lost, but a TableNotClosedException will be issued). So, its a good
    * thing to call LitebaseConnection.closeAll in your litebase instances and recover them in the onRestore method.
    * <br><br>
    * When the onMinimize is called, the screen will only be able to be updated after it resumes (in other words,
    * calling repaint or repaintNow from the onMinimize method has no effect).
    * @see #minimize()
    * @since TotalCross 1.10
    */
   public void onMinimize() // bruno@tc110_89 - bruno@tc122_31: now supported on wince and win32
   {
   }

   /**
    * Called just after the application is restored. Currently, this is only supported on
    * Java, BlackBerry, WinCE, Win32 and Android.
    * <br><br>The following applies to Android, only.
    * <br><br>The Android on TotalCross has a predefined life cycle:
    * <ol>
    * <li>onStart: called when the application starts
    * <li>onMinimize: called when the user press the home key, or when a call is received, or when the screen turns off
    * <li>onRestore: called after the call ends or the screen is turned on again
    * <li>onExit: called when the system decides that is time to finish the application. If the home key was pressed, this method
    * is called when another application is invoked.
    * </ol>
    * If the user press the home key and then forces the application to stop (by going to the Settings / Applications), then
    * all Litebase tables may be corrupted (actually, no data is lost, but a TableNotClosedException will be issued). So, its a good
    * thing to call LitebaseConnection.closeAll in your litebase instances and recover them in the onRestore method.
    * <br><br>
    * When the onMinimize is called, the screen will only be able to be updated after it resumes (in other words,
    * calling repaint or repaintNow from the onMinimize method has no effect).
    * @see #onRestore()
    * @since TotalCross 1.10
    */
   public void onRestore() // bruno@tc110_89 - bruno@tc122_31: now supported on wince and win32
   {
   }
   
   private static class DemoBox extends MessageBox
   {
      private static String tit,msg;
      DemoBox()
      {
         super(tit = "TotalCross Virtual Machine "+Settings.versionStr,
               msg = "Copyright (c) 2008-2011\nSuperWaba Ltda\n\nDEMO VERSION\n\nTime available: "+(timeAvailable == 0 ? "EXPIRED!" : (timeAvailable/100)+"h"+(timeAvailable%100)+"m"),
               new String[]{"   Ok   "});
         Vm.debug(tit);
         Vm.debug(msg);
      }
      public void initUI()
      {
         setBackForeColors(timeAvailable == 0 ? Color.RED : Color.BLUE,Color.WHITE);
         setFont(font.asBold());
      }
      
      public void postPressedEvent()
      {
         // do nothing
      }
   }
   
   /**
   * Called by the VM to process timer interrupts. This method is not private
   * to prevent the compiler from removing it during optimization.
   */
   final public void _onTimerTick(boolean canUpdate)
   {
      if (startTimer != null) // guich@567_17
      {
         TimerEvent t = startTimer;
         startTimer = null; // removeTimer calls again onTimerTick, so we have to null out this before calling it
         removeTimer(t);
         if (timeAvailable != -1) // guich@tc126_46
         {
            new DemoBox().popup();
            if (timeAvailable == 0)
            {
               exit(0);
               return;
            }
         }
         Window.needsPaint = true;
         initUI();
         Window.needsPaint = Graphics.needsUpdate = true; // required by device
         started = true; // guich@567_17: moved this from appStarting to here to let popup check if the screen was already painted
         repaintActiveWindows();
         // start a robot if one is passed as parameter
         String cmd = getCommandLine();
         if (cmd != null && cmd.endsWith(".robot") && !Vm.isKeyDown(SpecialKeys.SHIFT))
            try
            {
               new UIRobot(cmd+" (cmdline)");
            }
            catch (Exception e)
            {
               MessageBox.showException(e,true);
            }
      }
      int minInterval = 0;
      TimerEvent timer = firstTimer;
      while (timer != null)
      {
         if (timer.target == null) // aleady removed but still in the queue?
         {
            TimerEvent t = timer.next;
            removeTimer(timer);
            timer = t != null ? t.next : null;
            continue;
         }
         int now = Vm.getTimeStamp(); // guich@tc100b4
         int diff = now - timer.lastTick;
         if (diff < 0)
            diff += (1 << 30); // wrap around - max stamp is (1 << 30)
         int interval;
         if (diff >= timer.millis)
         {
            // post TIMER event
            timer.triggered = true; // guich@220_39
            ((Control)timer.target).postEvent(timer);
            timer.triggered = false;
            timer.lastTick = now;
            interval = timer.millis;
         }
         else
            interval = timer.millis - diff;
         if (interval < minInterval || minInterval == 0)
            minInterval = interval;
         timer = timer.next;
      }
      if (minInterval > 0 || lastMinInterval > 0) // guich@tc100: call only if there's a timer to run
         setTimerInterval(lastMinInterval = minInterval);
      if (Window.needsPaint) // guich@200b4_1: corrected the infinit repaint on popup windows
         topMost._doPaint();
      if (canUpdate && Graphics.needsUpdate) // guich@tc100: make sure that any pending screen update is committed. - if not called from addTimer/removeTimer (otherwise, an open combobox will flicker)
         updateScreen();
   }

   void setTimerInterval(int n)
   {
      totalcross.Launcher.instance.setTimerInterval(n);
   }
   native void setTimerInterval4D(int n);

   /** Returns the command line passed by the application that called this application in the Vm.exec method.
    * 
    * In Android, you can start an application using adb:
    * <pre>
    * adb shell am start -a android.intent.action.MAIN -n totalcross.app.uigadgets/.UIGadgets -e cmdline "Hello world"
    * </pre>
    * In the sample above, we're starting UIGadgets. Your app should be: totalcross.app.yourMainWindowClass/.yourMainWindowClass
    */
   final public static String getCommandLine() // guich@tc120_8: now is static
   {
      return totalcross.Launcher.instance.commandLine;
   }
   native public static String getCommandLine4D();

   /** This method can't be called for a MainWindow */
   public void setRect(int x, int y, int width, int height, Control relative, boolean screenChanged) // guich@567_19
   {
      // no messages, please. just ignore
   }

   /** Takes a screen shot of the current screen. 
    * Here's a sample:
    * <pre>
    * Image img = MainWindow.getScreenShot();
    * File f = new File(Settings.onJavaSE ? "screen.png" : "/sdcard/screen.png",File.CREATE_EMPTY);
    * img.createPng(f);
    * f.close();
    * </pre>
    * Note that the font varies from device to device and even to desktop. So, if you want to compare a device's
    * screen shot with one taken at desktop, be sure to set the default font in both to the same, like using
    * <code>setDefaultFont(Font.getFont(false,20))</code>.
    * 
    * @since TotalCross 1.3
    */
   public static Image getScreenShot() throws ImageException
   {
      Graphics gscr = mainWindowInstance.getGraphics();
      int w = Settings.screenWidth;
      int h = Settings.screenHeight;
      Image img = new Image(w,h);
      img.transparentColor = -1;
      Graphics gimg = img.getGraphics();
      int buf[] = new int[w];
      for (int y = 0; y < h; y++)
      {
         gscr.getRGB(buf, 0,0,y,w,1);
         gimg.setRGB(buf, 0,0,y,w,1);
      }      
      return img;
   }
}
