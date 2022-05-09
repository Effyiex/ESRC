
package esrc.lang;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

public interface ESRCCore {

  String FILE_EXTENSION = ".esrc";
  File WORKSPACE = new File(System.getProperty("user.dir") + '/' + FILE_EXTENSION);

  static boolean contains(Object[] array, Object obj) {
    for(Object item : array)
    if(item != null && item.equals(obj))
    return true;
    return false;
  }

  Atomic<String[]> VM_ARGS = new Atomic<String[]>().seal();

  static void main(String... args) {
    for(int i = 0; i < args.length; i++) args[i] = args[i].replace("\"", new String());
    VM_ARGS.set(args);
    if(contains(args, "-IDE")) ESRCEditor.INSTANCE.launch();
    else if(args.length > 0) {
      String fileName = args[0];
      if(!fileName.endsWith(FILE_EXTENSION)) fileName += FILE_EXTENSION;
      new ESRCLauncher(fileName).execute();
    }
  }

  Atomic<String> EXECUTED_PATH = new Atomic<String>(System.getProperty("user.dir"));

  Vector<Runnable> VM_TICK_LISTENER = new Vector<Runnable>();

  Runnable HOST_LISTENER = () -> {
    int prevActivityState = Byte.MIN_VALUE;
    while(true) {
      try {
        Thread.sleep(ESRCLauncher.VM_TICK);
        InputStream vmActivityStream = new FileInputStream(new File(EXECUTED_PATH.get() + "/.vm-activity"));
        byte[] vmActivityBuffer = new byte[vmActivityStream.available()];
        vmActivityStream.read(vmActivityBuffer);
        int vmActivityState = (int) vmActivityBuffer[0];
        if(prevActivityState == vmActivityState) break;
        else {
          prevActivityState = vmActivityState;
          VM_TICK_LISTENER.forEach(listener -> listener.run());
        }
        vmActivityStream.close();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    System.exit(0);
  };

}
