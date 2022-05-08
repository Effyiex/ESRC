
package esrc.lang;

import java.awt.*;
import javax.swing.*;
import java.io.*;

public class ESRCEditor {

  public static final ESRCEditor INSTANCE = new ESRCEditor();

  public final JFrame frame = new JFrame("IDE for Effyiex-Source.");

  public ESRCEditor() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 480);
    try {
      InputStream icon = this.getClass().getResourceAsStream("Icon.png");
      frame.setIconImage(javax.imageio.ImageIO.read(icon));
      icon.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void launch() {
    frame.setVisible(true);
  }

}
