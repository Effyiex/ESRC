
package esrc.lang;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

public class Launcher implements Core {

  public final File scriptFile;
  public final File javaFile;
  public final File classFile;
  public final String className;

  public Launcher(String scriptFile) {
    String[] filePath = scriptFile.replace("\\", "/").split("/");
    String fileName = filePath[filePath.length - 1];
    this.className = fileName.substring(0, fileName.lastIndexOf("."));
    this.scriptFile = new File(scriptFile);
    this.javaFile = new File(WORKSPACE.getAbsolutePath() + '/' + className + ".java");
    this.classFile = new File(WORKSPACE.getAbsolutePath() + '/' + className + ".class");
  }

  private Object scriptInstance;

  public void execute() {
    try {
      if(!WORKSPACE.exists()) WORKSPACE.mkdir();
      InputStream scriptInput = new FileInputStream(scriptFile);
      byte[] scriptBuffer = new byte[scriptInput.available()];
      scriptInput.read(scriptBuffer);
      scriptInput.close();
      InputStream defaultInput = this.getClass().getResourceAsStream("Default.esrc");
      byte[] defaultBuffer = new byte[defaultInput.available()];
      defaultInput.read(defaultBuffer);
      defaultInput.close();
      String script = Parser.convertCode(className, new String(defaultBuffer) + new String(scriptBuffer));
      OutputStream javaOutput = new FileOutputStream(javaFile);
      javaOutput.write(script.getBytes());
      javaOutput.close();
      Process compileProcess = Runtime.getRuntime().exec("cmd /c \"cd /d \"" + WORKSPACE.getAbsolutePath() + "\" & javac -nowarn -source 8 -target 8 " + javaFile.getName() + '\"');
      BufferedReader compileLog = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
      String output;
      while(compileProcess.isAlive() && (output = compileLog.readLine()) != null)
      System.out.print(output + '\n');
      ClassLoader classLoader = new URLClassLoader(new URL[] { classFile.getParentFile().toURI().toURL() }, ClassLoader.getSystemClassLoader());
      Class<?> scriptClass = classLoader.loadClass(classFile.getName().substring(0, classFile.getName().lastIndexOf('.')));
      this.scriptInstance = scriptClass.getConstructors()[0].newInstance();
      javaFile.delete();
      classFile.delete();
      for(File tempFile : WORKSPACE.listFiles()) {
        String tempName = tempFile.getName();
        if(tempName.startsWith(className + '$') && tempName.endsWith(".class"))
        tempFile.delete();
      }
      if(WORKSPACE.exists()) WORKSPACE.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
