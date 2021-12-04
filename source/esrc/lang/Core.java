
package esrc.lang;

import java.io.File;

public interface Core {

  File WORKSPACE = new File(System.getProperty("user.dir") + "/workspace");

  static boolean contains(Object[] array, Object obj) {
    for(Object item : array)
    if(item != null && item.equals(obj))
    return true;
    return false;
  }

  static void main(String... args) {
    if(!WORKSPACE.exists()) WORKSPACE.mkdir();
    if(contains(args, "-IDE")) Editor.INSTANCE.launch();
    else {
      String fileName = new String();
      for(String arg : args) fileName += arg + ' ';
      if(fileName.isEmpty()) return;
      fileName = fileName.substring(0, fileName.length() - 1);
      new Launcher(fileName).execute();
    }
  }

}
