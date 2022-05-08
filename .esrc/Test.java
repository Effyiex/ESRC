import esrc.lang.*;;
import de.prplx.jwa.*;;
import de.prplx.jwa.rendering.*;;
import de.prplx.jwa.utilities.*;;
import java.awt.*;;
import java.io.*;;
import javax.imageio.*;;
import java.awt.image.*;;
@SuppressWarnings("unchecked") public class Test {
static  String[] programArgs;

public static  void main(String... args) {

  programArgs = args;

  new Test();

};
static class ConsoleHandler {

  String prefix = "";

  void log(String... text) {

    System.out.print(prefix);

    for(String part : text) System.out.print(part);

    System.out.print('\n');

};
  String input(String... text) {

    System.out.print(prefix);

    for(String part : text) System.out.print(part);

    return System.console().readLine();

};
};
static  ConsoleHandler console = new ConsoleHandler();

static class Atomic<Type> {

  private boolean sealed = false;

  private Type value;

  Atomic(Type initial) {

    this.value = initial;

};
  Atomic() {

    this(null);

};
  void set(Type value) {

    this.value = value;

};
  Type get() {

    return value;

};
  Atomic<Type> seal() {

    this.sealed = true;

    return this;

};
};
static class Vector<Type> {

  Object[] buffer;

  Vector(Type... args) {

    this.buffer = args;

};
  void add(Type item) {

    Object[] replace = new Object[buffer.length + 1];

    for(int i = 0; i < buffer.length; i++) {

      replace[i] = buffer[i];

};
    replace[buffer.length] = item;

    this.buffer = replace;

};
  void remove(int index) {

    Object[] replace = new Object[buffer.length - 1];

    for(int i = 0; i < buffer.length; i++) {

      if(i < index) replace[i] = buffer[i];

      else if(i > index) replace[i - 1] = buffer[i];

};
    this.buffer = replace;

};
  int indexOf(Type item) {

    for(int i = 0; i < buffer.length; i++) {

      if(buffer[i] == item) return i;

};
    return -1;

};
  void remove(Type item) {

    this.remove(indexOf(item));

};
  Type get(int index) {

    return (Type) buffer[index];

};
  int dimension() {

    return buffer.length;

};
};
static class Reflect {

  static  Object invoke(String header, String method, Object... args) {

    try {

      Class<?> javaClass = Class.forName(header);

      java.lang.reflect.Method invokable = null;

      for(java.lang.reflect.Method m : javaClass.getMethods()) {

        if(m.getName().equals(method) && m.getParameterCount() == args.length) {

          invokable = m;

          break;

};
};
      invokable.setAccessible(true);

      return invokable.invoke(null, args);


    } catch(Exception e) {

      e.printStackTrace();

};
    return null;

};
  static class Instance {

    Class<?> javaClass;

    Object javaInstance;

    enum Type {

      CONSTRUCT,

      GIVEN;

};
    final  String header;

    Instance(Type type, String header, Object... args) {

      this.header = header;

      try {

        this.javaClass = Class.forName(header);

        if(type == Type.GIVEN) this.javaInstance = args[0];

        else {

          for(java.lang.reflect.Constructor c : javaClass.getConstructors()) {

            if(c.getParameterCount() == args.length) {

              this.javaInstance = c.newInstance(args);

              break;

};
};
};

      } catch(Exception e) {

        e.printStackTrace();

};
};
    Instance(String header, Object... args) {

      this(Type.CONSTRUCT, header, args);

};
    Object invoke(String method, Object... args) {

      try {

        java.lang.reflect.Method invokable = null;

        Class<?> from = javaClass;

        LOOKUP : while(invokable == null) {

          if(from != null) {

            for(java.lang.reflect.Method m : from.getDeclaredMethods()) {

              if(m.getName().equals(method) && m.getParameterCount() == args.length) {

                invokable = m;

                break LOOKUP;

};
};
}
          else break;

          from = from.getSuperclass();

};
        invokable.setAccessible(true);

        return invokable.invoke(javaInstance, args);


      } catch(Exception e) {

        e.printStackTrace();

};
      return null;

};
};
};
static class Window {

  final static  int TITLEBAR_HEIGHT = 32;

  static  java.awt.image.BufferedImage defaultIcon;

  static  {

    try {

      java.io.InputStream iconStream = Class.forName("esrc.lang.Core").getResourceAsStream("Icon.png");

      defaultIcon = javax.imageio.ImageIO.read(iconStream);

      iconStream.close();


    } catch(Exception e) {

      e.printStackTrace();

};
};
  javax.swing.JPanel canvas = new javax.swing.JPanel() {

    protected void paintComponent(java.awt.Graphics g3d) {

      java.awt.Graphics2D gl = ((java.awt.Graphics2D) g3d);

      gl.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

      gl.setColor(new java.awt.Color(33, 33, 36));

      gl.fillRect(0, 0, getWidth(), getHeight());

      if(titlebar) {

        gl.setColor(new java.awt.Color(22, 22, 24));

        gl.fillRect(0, 0, getWidth(), TITLEBAR_HEIGHT);

        gl.setColor(new java.awt.Color(200, 200, 222));

        gl.drawString(Window.this.title, TITLEBAR_HEIGHT, TITLEBAR_HEIGHT / 3 * 2);

        gl.drawImage(defaultIcon, TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 2, TITLEBAR_HEIGHT / 2, null);

        gl.setColor(new java.awt.Color(222, 55, 111));

        gl.fillOval(getWidth() - TITLEBAR_HEIGHT * 1 + TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 2, TITLEBAR_HEIGHT / 2);

        gl.setColor(new java.awt.Color(222, 222, 55));

        gl.fillOval(getWidth() - TITLEBAR_HEIGHT * 2 + TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 2, TITLEBAR_HEIGHT / 2);

        gl.setColor(new java.awt.Color(55, 222, 111));

        gl.fillOval(getWidth() - TITLEBAR_HEIGHT * 3 + TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 4, TITLEBAR_HEIGHT / 2, TITLEBAR_HEIGHT / 2);

        gl.translate(0, TITLEBAR_HEIGHT);

};
};
};
  boolean titlebar = true;

  Reflect.Instance base;

  void onControlButtonsClick(int button) {

    java.awt.Point cursor = canvas.getMousePosition();

    if(cursor.y < TITLEBAR_HEIGHT) {

      int control = (canvas.getWidth() - cursor.x) / TITLEBAR_HEIGHT;

      if(control == 0) this.base.invoke("setVisible", false);

      else if(control == 1) System.out.println("ERROR: Not supported, yet.");

      else if(control == 2) this.base.invoke("setState", 1);

};
};
  Window(boolean floating) {

    if(floating) this.base = new Reflect.Instance("javax.swing.JWindow");

    else {

      this.base = new Reflect.Instance("javax.swing.JFrame");

      this.base.invoke("setUndecorated", true);

      this.base.invoke("setIconImage", defaultIcon);

};
    this.base.invoke("add", canvas);

    this.base.invoke("addMouseListener", mouseListener);

    this.base.invoke("addKeyListener", keyListener);

    this.updateThread.start();

    this.onMouseDown.add(this::onControlButtonsClick);

};
  Window() {

    this(false);

};
  String title = "";

  Runnable closeOperation = () -> System.exit(0);

  int framerate = 60;

  boolean hiddenByFunction = true;

  boolean closeOperationRan = false;

  Runnable updateRoutine = () -> {

    while(true) { if(false) break; ;

      long tickDelay = Math.round(1000.0D / framerate);

      try {

        Thread.sleep(tickDelay);


      } catch(Exception e) {

        break;

};
      if(Window.this.isVisible()) {

        if(base.javaInstance instanceof javax.swing.JFrame) {

          String currentTitle = (String) base.invoke("getTitle");

          if(currentTitle != title) base.invoke("setTitle", title);

};
        canvas.repaint();

        closeOperationRan = false;

}
      else if(!hiddenByFunction && closeOperation != null && !closeOperationRan) {

        closeOperation.run();

        closeOperationRan = true;

};
};
};
  Vector<java.util.function.Consumer<Integer>> onKeyDown = new Vector();

  Vector<java.util.function.Consumer<Integer>> onKeyUp = new Vector();

  Vector<Integer> holdKeys = new Vector();

  java.awt.event.KeyListener keyListener = new java.awt.event.KeyListener() {

    public void keyPressed(java.awt.event.KeyEvent event) {

      if(holdKeys.indexOf(event.getKeyCode()) < 0) {

        for(Object obj : onKeyDown.buffer) ((java.util.function.Consumer) obj).accept(event.getKeyCode());

        holdKeys.add(event.getKeyCode());

};
};
    public void keyReleased(java.awt.event.KeyEvent event) {

      if(0 <= holdKeys.indexOf(event.getKeyCode())) {

        for(Object obj : onKeyUp.buffer) ((java.util.function.Consumer) obj).accept(event.getKeyCode());

        holdKeys.remove((Integer) event.getKeyCode());

};
};
    public void keyTyped(java.awt.event.KeyEvent event) {

      return;

};
};
  Vector<java.util.function.Consumer<Integer>> onMouseDown = new Vector();

  Vector<java.util.function.Consumer<Integer>> onMouseUp = new Vector();

  Vector<Integer> holdMouseButtons = new Vector();

  java.awt.event.MouseListener mouseListener = new java.awt.event.MouseListener() {

    java.awt.Point dragging;

    public void mouseExited(java.awt.event.MouseEvent event) {

      return;

};
    public void mouseEntered(java.awt.event.MouseEvent event) {

      return;

};
    public void mouseClicked(java.awt.event.MouseEvent event) {

      return;

};
    public void mousePressed(java.awt.event.MouseEvent event) {

      if(event.getButton() == 1 && event.getY() < TITLEBAR_HEIGHT && titlebar) {

        dragging = event.getPoint();

        if(event.getX() > canvas.getWidth() - TITLEBAR_HEIGHT * 3) dragging = null;

        Runnable draggingRoutine = () -> {

          while(dragging != null) {

            java.awt.Point global = java.awt.MouseInfo.getPointerInfo().getLocation();

            global.x -= dragging.x;

            global.y -= dragging.y;

            base.invoke("setLocation", global.x, global.y);

            try {

              Thread.sleep(1L);


            } catch(Exception e) {

              break;

};
};
};
        new Thread(draggingRoutine).start();

};
      if(holdMouseButtons.indexOf(event.getButton()) < 0) {

        for(Object obj : onMouseDown.buffer) ((java.util.function.Consumer) obj).accept(event.getButton());

        holdMouseButtons.add(event.getButton());

};
};
    public void mouseReleased(java.awt.event.MouseEvent event) {

      if(event.getButton() == 1) dragging = null;

      if(0 <= holdMouseButtons.indexOf(event.getButton())) {

        for(Object obj : onMouseUp.buffer) ((java.util.function.Consumer) obj).accept(event.getButton());

        holdMouseButtons.remove((Integer) event.getButton());

};
};
};
  Thread updateThread = new Thread(updateRoutine);

  void show() {

    this.base.invoke("setVisible", true);

    this.hiddenByFunction = false;

};
  void hide() {

    this.base.invoke("setVisible", false);

    this.hiddenByFunction = true;

};
  boolean isVisible() {

    return (boolean) this.base.invoke("isVisible");

};
  Vector<Float> anker = new Vector(0, 0);

  Vector<Integer> getScale() {

    java.awt.Dimension size = (java.awt.Dimension) base.invoke("getSize");

    if(titlebar) size.height -= TITLEBAR_HEIGHT;

    return new Vector(size.width, size.height);

};
  void position(int x, int y) {

    Vector<Integer> scale = this.getScale();

    x -= Math.round(anker.get(0) * scale.get(0));

    y -= Math.round(anker.get(1) * scale.get(1));

    this.base.invoke("setLocation", x, y);

};
  void position(float x, float y) {

    java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

    int iX = Math.round(screen.width * x);

    int iY = Math.round(screen.height * y);

    this.position(iX, iY);

};
  void scale(int width, int height) {

    if(titlebar) height += TITLEBAR_HEIGHT;

    this.base.invoke("setSize", width, height);

};
  void scale(float width, float height) {

    java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

    int iWidth = Math.round(screen.width * width);

    int iHeight = Math.round(screen.height * height);

    this.scale(iWidth, iHeight);

};
};
public static Test SCRIPT; public Test() { SCRIPT = this; ;
  JWALogger.setState(false);
  JWAScene scene = new JWAScene("Test");
  scene.setBackground(Color.BLACK);
  try {
    InputStream iconStream = ESRCCore.class.getResourceAsStream("Icon.png");
    BufferedImage iconImage = ImageIO.read(iconStream);
    iconStream.close();
    JWebApplet applet = new JWebApplet(80);
    applet.setDefaultScene(scene);
    applet.setFavicon(iconImage);
    applet.start();

  } catch(Exception e) {
    e.printStackTrace();

};
};

};
