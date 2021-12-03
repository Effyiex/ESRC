
package esrc.lang;

public interface Core {

  static boolean contains(Object[] array, Object obj) {
    for(Object item : array)
    if(item != null && item.equals(obj))
    return true;
    return false;
  }

  static void main(String... args) {
    if(contains(args, "-IDE")) Editor.INSTANCE.launch();
  }

}
