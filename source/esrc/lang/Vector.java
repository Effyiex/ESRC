
package esrc.lang;

@SuppressWarnings("unchecked")
public class Vector<Type> {

  private Object[] buffer;

  public Vector(Type... initial) {
    this.buffer = initial;
  }

  public void add(Type item) {
    Object[] replace = new Object[buffer.length + 1];
    for(int i = 0; i < buffer.length; i++) replace[i] = buffer[i];
    replace[buffer.length] = item;
    this.buffer = replace;
  }

  public void remove(int index) {
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
    this.remove(indexOf(item));
  }

  public Type get(int index) {
    return (Type) buffer[index];
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

}
