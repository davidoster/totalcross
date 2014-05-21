/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2014 SuperWaba Ltda.                                      *
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

package tc.samples.api;

import totalcross.res.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.font.*;
import totalcross.ui.gfx.*;
import totalcross.ui.image.*;
import totalcross.util.*;

public class BaseContainer extends Container
{
   public static final int BKGCOLOR = 0x0A246A;
   public static final int SELCOLOR = 0x829CE2; // Color.brighter(BKGCOLOR,120);
   protected Bar headerBar,footerBar;
   private static Vector containerStack = new Vector(5);
   private static Image infoImg;
   public static String defaultTitle = "TotalCross API";
   protected int gap;
   public boolean isSingleCall;
   public String info;

   protected String getHelpMessage()
   {
      return null;
   }
   
   public void initUI()
   {
      try
      {
         gap = fmH/2;
         boolean isMainMenu = containerStack.size() == 1;
         if (isMainMenu) 
         {
            backgroundStyle = BACKGROUND_CYLINDRIC_SHADED;
            setForeColor(0xBFCFFF);
         }
         
         if (infoImg == null)
            infoImg = new Image("ui/images/ic_dialog_info.png");
         int c1 = 0x0A246A;
         Font f = font.adjustedBy(2,true);
         headerBar = new Bar(defaultTitle);
         headerBar.setFont(f);
         headerBar.setBackForeColors(c1,Color.WHITE);
         headerBar.addButton(isMainMenu ? infoImg : Resources.back);
         add(headerBar, LEFT,0,FILL,PREFERRED);
         
         footerBar = new Bar("");
         footerBar.uiAdjustmentsBasedOnFontHeightIsSupported = false;
         footerBar.setFont(f);
         footerBar.titleAlign = CENTER;
         footerBar.backgroundStyle = BACKGROUND_SOLID;
         footerBar.setBackForeColors(c1,Color.WHITE);
         setInsets(0,0,headerBar.getHeight(),footerBar.getPreferredHeight());
         add(footerBar, LEFT,BOTTOM+insets.bottom,FILL,PREFERRED);
         // we use a PressListener so that the subclasses don't need to call super.onEvent
         headerBar.addPressListener(new PressListener()
         {
            public void controlPressed(ControlEvent e)
            {
               e.consumed = true;
               try
               {
                  switch (((Bar)e.target).getSelectedIndex())
                  {
                     case 1:
                     {
                        boolean isMainMenu = containerStack.size() == 1;
                        if (isMainMenu)
                        {
                           String helpMessage = getHelpMessage();
                           if (helpMessage == null) 
                              return;
                           MessageBox mb = new MessageBox("Help",helpMessage,new String[]{"Close"});
                           mb.footerColor = mb.headerColor = UIColors.messageboxBack;
                           mb.setIcon(infoImg);
                           mb.popup();
                        }
                        else
                        {
                           back();
                        }
                        break;
                     }
                  }
               }
               catch (Exception ee)
               {
                  MessageBox.showException(ee,true);
               }
            }
         });
         
         Window w = getParentWindow();
         w.callListenersOnAllTargets = true; // fixes problem if focus goes to another control
         w.addKeyListener(new KeyListener() 
         {
            public void keyPressed(KeyEvent e)
            {
            }

            public void actionkeyPressed(KeyEvent e)
            {
            }

            public void specialkeyPressed(KeyEvent e)
            {
               if (e.key == SpecialKeys.ESCAPE)
               {
                  e.consumed = true;
                  back();
               }
            }
         });
         
         String name = getClass().getName();
         setTitle(name.endsWith("Sample") ? name.substring(name.lastIndexOf('.')+1,name.length()-6) : defaultTitle);
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   
   public void setTitle(String s)
   {
      headerBar.setTitle(s);
   }
   
   public String getTitle()
   {
      return headerBar.getTitle();
   }
   
   public void setInfo(String s)
   {
      if (footerBar != null)
         footerBar.setTitle(s);
   }
   
   public void show()
   {
      containerStack.push(this); // push ourself
      Window.getTopMost().swap(this);
   }
   
   public void back()
   {
      if (!(this instanceof MainMenu))
         try
         {
            setInfo(MainMenu.DEFAULT_INFO);
            containerStack.pop(); // pop ourself
            Window.getTopMost().swap((Container)containerStack.peek());
         }
         catch (ElementNotFoundException enfe)
         {
            //MainWindow.exit(0); // we're the last screen, so just exit the application
         }
   }
   
   public boolean ask(String question)
   {
      MessageBox mb = new MessageBox("Question", question, new String[]{"Yes","No"});
      mb.popup();
      return mb.getPressedButtonIndex() == 0;
   }
   
   // single place to add and log messages
   protected static ListBox lblog;
   public void addLog(int x, int y, int w, int h, Control rel)
   {
      add(lblog = new ListBox(),x,y,w,h,rel);
   }

   // a log method that runs safely on threads
   public static void log(Object s)
   {
      log(s,true);
   }
   public static void log(Object s, boolean selLast)
   {
      if (s == null) return;
      final Object _s = s;
      final boolean _selLast = selLast;
      if (MainWindow.isMainThread())
      {
         if (s instanceof String)
            lblog.addWrapping((String)s);
         else
            lblog.add(s);
         if (selLast) lblog.selectLast();
      }
      else
      MainWindow.getMainWindow().runOnMainThread(new Runnable()
      {
         public void run()
         {
            if (_s instanceof String)
               lblog.addWrapping((String)_s);
            else
               lblog.add(_s);
            if (_selLast) lblog.selectLast();
         }
      });
   }
}
