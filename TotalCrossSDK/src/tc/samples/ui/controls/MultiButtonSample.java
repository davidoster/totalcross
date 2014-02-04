package tc.samples.ui.controls;

import totalcross.ui.*;
import totalcross.ui.gfx.*;

public class MultiButtonSample extends BaseContainer
{
   public void initUI()
   {
      super.initUI();
      setTitle("MultiButton");
      ScrollContainer sc = new ScrollContainer(false, true);
      sc.borderColor = headerBar.getBackColor();
      sc.setBorderStyle(BORDER_ROUNDED); // sample of the new rounded border
      sc.setInsets(gap, gap, gap, gap);

      sc.add(new Label("Normal"),LEFT,TOP+fmH/2);
      MultiButton b = new MultiButton(new String[]{"+","-"});
      b.setBackColor(Color.ORANGE);
      b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED,fmH*3/2);

      sc.add(new Label("Sticky, 3d text, center disabled"),LEFT,AFTER+fmH);
      b = new MultiButton(new String[]{"Left","Center","Right"});
      b.setBackColor(Color.GREEN);
      b.isSticky = b.is3dText = true;
      sc.add(b,SAME,AFTER,PREFERRED+fmH*2,fmH*2);
      
      b.setEnabled(1,false);

      add(sc, LEFT+fmH, TOP+fmH, FILL-fmH, FILL-fmH);
   }
}