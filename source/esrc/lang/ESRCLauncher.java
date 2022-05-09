
package esrc.lang;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ESRCLauncher implements ESRCCore {

  public static final long VM_TICK = 1000L;

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

      String classPath = new String();
      char sep = System.getProperty("os.name").contains("Windows") ? ';' : ':';
      for(String jarImport : jarImports)
      if(!jarImport.isEmpty())
      classPath += String.valueOf(sep) + System.getProperty("user.dir") + '/' + jarImport + ".jar";
      classPath = "\"." + sep + getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1) + classPath + '\"';

      List<String> compileCommand = new ArrayList<String>();
      compileCommand.add("javac");
      compileCommand.add("-nowarn");
      compileCommand.add("-source");
      compileCommand.add("8");
      compileCommand.add("-target");
      compileCommand.add("8");
      compileCommand.add("-cp");
      compileCommand.add(classPath);
      compileCommand.add(javaFile.getName());

      ProcessBuilder compileProcess = new ProcessBuilder(compileCommand);
      compileProcess.directory(WORKSPACE);
      compileProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
      compileProcess.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      compileProcess.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process compiler = compileProcess.start();
      compiler.waitFor();

      javaFile.delete();

      List<String> scriptCommand = new ArrayList<String>();
      scriptCommand.add("java");
      scriptCommand.add("-cp");
      scriptCommand.add("\"." + sep + "/" + WORKSPACE.getAbsolutePath() + classPath.substring(2));
      scriptCommand.add(classFile.getName().substring(0, classFile.getName().lastIndexOf('.')));
      for(String vmArg : VM_ARGS.get()) scriptCommand.add(vmArg);

      ProcessBuilder scriptProcess = new ProcessBuilder(scriptCommand);
      scriptProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
      scriptProcess.redirectOutput(ProcessBuilder.Redirect.INHERIT);
      scriptProcess.redirectError(ProcessBuilder.Redirect.INHERIT);
      this.scriptInstance = scriptProcess.start();

      File vmActivityFile = new File(WORKSPACE.getAbsolutePath() + "/.vm-activity");
      Thread vmActivityThread = new Thread(() -> {
        int vmActivity = Byte.MIN_VALUE;
        while(scriptInstance.isAlive()) {
          try {
            OutputStream vmActivityStream = new FileOutputStream(vmActivityFile);
            vmActivityStream.write(new byte[] { (byte) vmActivity });
            vmActivityStream.close();
            vmActivity = vmActivity >= Byte.MAX_VALUE ? Byte.MIN_VALUE : vmActivity + 1;
            Thread.sleep(VM_TICK);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
      vmActivityThread.start();

      Thread endingThread = new Thread(() -> {
        classFile.delete();
        for(File tempFile : WORKSPACE.listFiles()) {
          String tempName = tempFile.getName();
          if(tempName.startsWith(className + '$') && tempName.endsWith(".class"))
          tempFile.delete();
        }
        if(vmActivityFile.exists()) vmActivityFile.delete();
        if(WORKSPACE.exists()) WORKSPACE.delete();
        if(scriptInstance.isAlive()) scriptInstance.destroyForcibly();
      });

      Runtime.getRuntime().addShutdownHook(endingThread);

      scriptInstance.waitFor();
      endingThread.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
