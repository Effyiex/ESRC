
package esrc.lang;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Window {

  public static final int TITLEBAR_HEIGHT = 32;

  public static final int SHOW_IN_TASKBAR = 0x9237597;
  public static final int HIDE_IN_TASKBAR = 0x8432753;

  public final Vector<Float> ankerPoint = new Vector(0.0F, 0.0F).seal();

  private final Thread thread;

  private Reflect.Instance base;
  private int type;

  public String title = new String(), prevTitle;
  public int tps = 30;
  public Image icon, prevIcon;

  public Runnable exitOperation = () -> {
    System.exit(0);
  };

  private boolean ranExitOperation = false;
  private boolean wantedHide = true;

  private final Runnable routine = () -> {
    while(true) {
      long tickInterval = 1000L / tps;
      try {
        Thread.sleep(tickInterval);
      } catch(Exception e) {
        e.printStackTrace();
      }
      if(!((boolean) base.invoke("isVisible")) && !wantedHide && !ranExitOperation) {
        exitOperation.run();
        ranExitOperation = true;
      }
      if(prevTitle != title) {
        this.base.invoke("setTitle", title);
        prevTitle = title;
      }
      if(type == SHOW_IN_TASKBAR && prevIcon != icon) {
        this.base.invoke("setIconImage", icon);
        prevIcon = icon;
      }
      this.panel.repaint();
    }
  };

  private final JPanel panel = new JPanel() {

    protected void paintComponent(Graphics gl) {
      Window.this.render((Graphics2D) gl);
    }

  };

  public Window() {
    this(SHOW_IN_TASKBAR);
  }

  public Window(int type) {
    this.type = type;
    String baseHeader;
    if(type == HIDE_IN_TASKBAR) baseHeader = "javax.swing.JWindow";
    else if(type == SHOW_IN_TASKBAR) baseHeader = "javax.swing.JFrame";
    else {
      baseHeader = null;
      System.err.println("ERROR: Not a valid Window-Type.");
      System.exit(0);
    }
    this.base = new Reflect.Instance(Reflect.Instance.Type.CONSTRUCT, baseHeader);
    this.base.invoke("setDefaultCloseOperation", JFrame.HIDE_ON_CLOSE);
    this.base.invoke("add", panel);
    try {
      InputStream iconStream = this.getClass().getResourceAsStream("Icon.png");
      this.icon = javax.imageio.ImageIO.read(iconStream);
      iconStream.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
    if(type == SHOW_IN_TASKBAR) {
      this.base.invoke("setUndecorated", true);
      this.base.invoke("setIconImage", icon);
    }
    this.thread = new Thread(routine);
    this.thread.start();
  }

  private Color background = new Color(0, 0, 0);

  public void background(int r, int g, int b) {
    this.background = new Color(r, g, b);
  }

  public SingleConsumer<Graphics2D> renderRoutine = (gl) -> {
    gl.setColor(Color.WHITE);
    gl.drawString("EMPTY RENDER-ROUTINE.", 16, 16);
  };

  private void render(Graphics2D gl) {
    gl.setColor(background);
    gl.fillRect(0, 0, panel.getWidth(), panel.getHeight());
    gl.translate(0, TITLEBAR_HEIGHT);
    renderRoutine.accept(gl);
    gl.translate(0, -TITLEBAR_HEIGHT);
  }

  public void anker(float x, float y) {
    this.ankerPoint.set(0, x);
    this.ankerPoint.set(1, y);
  }

  public void scale(float x, float y) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    this.scale((int) (screen.getWidth() * x), (int) (screen.getHeight() * y));
  }

  public void scale(int x, int y) {
    this.base.invoke("setSize", x, y);
  }

  public void position(float x, float y) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    this.position((int) (screen.getWidth() * x), (int) (screen.getHeight() * y));
  }

  public void position(int x, int y) {
    Dimension size = (Dimension) base.invoke("getSize");
    this.base.invoke("setLocation", x - (int) (size.getWidth() * ankerPoint.get(0)), y - (int) (size.getHeight() * ankerPoint.get(1)));
  }

  public void show() {
    this.base.invoke("setVisible", true);
    this.ranExitOperation = false;
    this.wantedHide = false;
  }

  public void hide() {
    this.wantedHide = true;
    this.base.invoke("setVisible", false);
  }

}
