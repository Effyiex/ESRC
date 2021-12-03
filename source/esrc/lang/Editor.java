
package esrc.lang;

import java.awt.*;
import javax.swing.*;

import java.io.InputStream;

import javax.imageio.ImageIO;

public class Editor {

  public static final Editor INSTANCE = new Editor();

  public final JFrame frame = new JFrame("IDE for Effyiex-Source.");

  public Editor() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 480);
    try {
      InputStream icon = this.getClass().getResourceAsStream("Icon.png");
      frame.setIconImage(ImageIO.read(icon));
      icon.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void launch() {
    frame.setVisible(true);
  }

}
