
package esrc.lang;

@SuppressWarnings("unchecked")
public class Vector<Type> {

  private Object[] buffer;

  private boolean sealed = false;

  public Vector(Type... initial) {
    this.buffer = initial;
  }

  public Vector<Type> seal() {
    this.sealed = true;
    return this;
  }

  public void add(Type item) {
    if(sealed) return;
    Object[] replace = new Object[buffer.length + 1];
    for(int i = 0; i < buffer.length; i++) replace[i] = buffer[i];
    replace[buffer.length] = item;
    this.buffer = replace;
  }

  public void remove(int index) {
    if(sealed) return;
    Object[] replace = new Object[buffer.length - 1];
    for(int i = 0; i < buffer.length; i++)
      if(i < index) replace[i] = buffer[i];
      else if(i > index) replace[i - 1] = buffer[i];
    this.buffer = replace;
  }

  public int indexOf(Type item) {
    for(int i = 0; i < buffer.length; i++)
      if(buffer[i] == item) return i;
    return -1;
  }

  public void remove(Type item) {
    if(sealed) return;
    this.remove(indexOf(item));
  }

  public Type get(int index) {
    return (Type) buffer[index];
  }

  public void set(int index, Type value) {
    this.buffer[index] = value;
  }

  public int dimension() {
    return buffer.length;
  }

  public void forEach(SingleConsumer<Type> handler) {
    for(Object o : buffer) handler.accept((Type) o);
  }

  public void forEach(DualConsumer<Type, Integer> handler) {
    for(int i = 0; i < buffer.length; i++) handler.accept((Type) buffer[i], i);
  }

  public void clear() {
    this.buffer = new Object[0];
  }

  public Type[] cast(Type[] buffer) {
    for(int i = 0; i < buffer.length; i++) buffer[i] = (Type) this.buffer[i];
    return buffer;
  }

  public Object cast(Class<?> clazz) {
    Object instance = null;
    for(java.lang.reflect.Constructor con : clazz.getConstructors()) {
      if(con.getParameterTypes()[0] != buffer[0].getClass()) continue;
      if(con.getParameterCount() == buffer.length) {
        try {
          instance = con.newInstance(buffer);
        } catch(Exception e) {
          e.printStackTrace();
        }
        break;
      }
    }
    return instance;
  }

}
