
package esrc.lang;

public class ConsoleHandler {

  public static class AwaitingConsoleHandler extends ConsoleHandler {

    private final Vector<String[]> buffer = new Vector<String[]>();

    public AwaitingConsoleHandler() {
      ESRCCore.VM_TICK_LISTENER.add(() -> {
        buffer.forEach(text -> AwaitingConsoleHandler.super.append(text));
        buffer.clear();
      });
    }

    protected void append(String... text) {
      this.buffer.add(text);
    }

  }

  private String prefix = new String();

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getPrefix() {
    return prefix;
  }

  public void log(String... text) {
    if(text.length > 0) text[text.length - 1] += '\n';
    this.append(text);
  }

  protected void append(String... text) {
    System.out.print(prefix);
    for(String part : text) System.out.print(part);
  }

  public String input(String... text) {
    this.append(text);
    String consoleInput = new String();
    try {
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      consoleInput = reader.readLine();
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return consoleInput;
  }

}
