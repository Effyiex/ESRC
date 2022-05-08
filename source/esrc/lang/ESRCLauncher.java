
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
import java.util.ArrayList;
import java.util.List;

public class ESRCLauncher implements ESRCCore {

  public final File scriptFile;
  public final File javaFile;
  public final File classFile;
  public final String className;

  public ESRCLauncher(String scriptFile) {
    String[] filePath = scriptFile.replace("\\", "/").split("/");
    String fileName = filePath[filePath.length - 1];
    this.className = fileName.substring(0, fileName.lastIndexOf("."));
    this.scriptFile = new File(scriptFile);
    this.javaFile = new File(WORKSPACE.getAbsolutePath() + '/' + className + ".java");
    this.classFile = new File(WORKSPACE.getAbsolutePath() + '/' + className + ".class");
  }

  private Process scriptInstance;

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
      String[] jarImports = ESRCParser.getJarImports(new String(scriptBuffer));
      String script = ESRCParser.convertCode(className, new String(defaultBuffer).replace("%ESRC_INSTANCE%", className) + new String(scriptBuffer));
      OutputStream javaOutput = new FileOutputStream(javaFile);
      javaOutput.write(script.getBytes());
      javaOutput.close();
      List<String> compileCommand = new ArrayList<String>();
      compileCommand.add("javac");
      compileCommand.add("-nowarn");
      compileCommand.add("-source");
      compileCommand.add("8");
      compileCommand.add("-target");
      compileCommand.add("8");
      compileCommand.add("-cp");
      String classPath = new String();
      char sep = System.getProperty("os.name").contains("Windows") ? ';' : ':';
      for(String jarImport : jarImports)
      if(!jarImport.isEmpty())
      classPath += String.valueOf(sep) + System.getProperty("user.dir") + '/' + jarImport + ".jar";
      classPath = "\"." + sep + getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1) + classPath + '\"';
      compileCommand.add(classPath);
      compileCommand.add(javaFile.getName());
      /*for(String cmdPart : compileCommand) System.out.print(cmdPart + ' ');
      System.out.print("\n");*/
      ProcessBuilder compileProcess = new ProcessBuilder(compileCommand);
      compileProcess.directory(WORKSPACE);
      Process compiler = compileProcess.start();
      BufferedReader compileLog = new BufferedReader(new InputStreamReader(compiler.getErrorStream()));
      String output;
      while(compiler.isAlive() && (output = compileLog.readLine()) != null)
      System.out.print(output + '\n');
      List<String> scriptCommand = new ArrayList<String>();
      scriptCommand.add("java");
      scriptCommand.add("-cp");
      scriptCommand.add("\"." + sep + "/" + WORKSPACE.getAbsolutePath() + classPath.substring(2));
      scriptCommand.add(classFile.getName().substring(0, classFile.getName().lastIndexOf('.')));
      /*for(String cmdPart : scriptCommand) System.out.print(cmdPart + ' ');
      System.out.print("\n");*/
      ProcessBuilder scriptProcess = new ProcessBuilder(scriptCommand);
      this.scriptInstance = scriptProcess.start();
      BufferedReader scriptLog = new BufferedReader(new InputStreamReader(scriptInstance.getErrorStream()));
      while(scriptInstance.isAlive() && (output = scriptLog.readLine()) != null)
      System.out.print(output + '\n');
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
