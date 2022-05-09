
package esrc.lang;

public class Reflect {

  public static Object invoke(String header, String method, Object... args) {
    try {
      Class<?> javaClass = Class.forName(header);
      java.lang.reflect.Method invokable = null;
      for(java.lang.reflect.Method m : javaClass.getMethods()) {
        if(m.getName().equals(method) && m.getParameterCount() == args.length) {
          invokable = m;
          break;
        }
      }
      invokable.setAccessible(true);
      return invokable.invoke(null, args);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static class Instance {

    private Class<?> javaClass;
    private Object javaInstance;

    public static enum Type {
      CONSTRUCT,
      GIVEN
    }

    public final String header;

    public Instance(Type type, String header, Object... args) {
      this.header = header;
      try {
        this.javaClass = Class.forName(header);
        if(type == Type.GIVEN) this.javaInstance = args[0];
        else {
          for(java.lang.reflect.Constructor c : javaClass.getConstructors()) {
            if(c.getParameterCount() == args.length) {
              this.javaInstance = c.newInstance(args);
              break;
            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    public Instance(String header, Object... args) {
      this(Type.CONSTRUCT, header, args);
    }

    public Object invoke(String method, Object... args) {
      try {
        java.lang.reflect.Method invokable = null;
        Class<?> from = javaClass;
        LOOKUP : while(invokable == null) {
          if(from != null) {
            for(java.lang.reflect.Method m : from.getDeclaredMethods()) {
              if(m.getName().equals(method) && m.getParameterCount() == args.length) {
                invokable = m;
                break;
              }
            }
          } else break;
          from = from.getSuperclass();
        }
        invokable.setAccessible(true);
        return invokable.invoke(javaInstance, args);
      } catch(Exception e) {
        e.printStackTrace();
      }
      return null;
    }

  }

}
