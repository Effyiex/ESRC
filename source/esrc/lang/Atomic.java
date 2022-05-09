
package esrc.lang;

public class Atomic<Type> {

  private boolean sealed = false;
  private Type value;

  public Atomic(Type initial) {
    this.value = initial;
  }

  public Atomic() {
    this(null);
  }

  public void set(Type value) {
    if(this.value == null || !sealed)
    this.value = value;
  }

  public Type get() {
    return value;
  }

  public Atomic<Type> seal() {
    this.sealed = true;
    return this;
  }

}
