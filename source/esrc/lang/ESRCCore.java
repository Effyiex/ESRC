
package esrc.lang;

import java.io.File;

public interface ESRCCore {

  String FILE_EXTENSION = ".esrc";
  File WORKSPACE = new File(System.getProperty("user.dir") + '/' + FILE_EXTENSION);

  static boolean contains(Object[] array, Object obj) {
    for(Object item : array)
    if(item != null && item.equals(obj))
    return true;
    return false;
  }

  static void main(String... args) {
    if(contains(args, "-IDE")) ESRCEditor.INSTANCE.launch();
    else {
      String fileName = new String();
      for(String arg : args) fileName += arg + ' ';
      if(fileName.trim().isEmpty()) return;
      fileName = fileName.substring(0, fileName.length() - 1);
      if(!fileName.endsWith(FILE_EXTENSION)) fileName += FILE_EXTENSION;
      new ESRCLauncher(fileName).execute();
    }
  }

}
