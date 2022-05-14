
package esrc.lang;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

public interface ESRCCore {

  String FILE_EXTENSION = ".esrc";
  File WORKSPACE = new File(System.getProperty("user.dir") + '/' + FILE_EXTENSION);

  static boolean contains(Object[] array, Object obj) {
    for(Object item : array)
    if(item != null && item.equals(obj))
    return true;
    return false;
  }

  Atomic<Process> SCRIPT_INSTANCE = new Atomic<Process>().seal();
  Atomic<String> CLASS_NAME = new Atomic<String>().seal();
  Atomic<File> JAVA_FILE = new Atomic<File>().seal();
  Atomic<File> CLASS_FILE = new Atomic<File>().seal();
  Atomic<File> SCRIPT_FILE = new Atomic<File>().seal();
  Atomic<File> VM_ACTIVITY_FILE = new Atomic<File>().seal();

  long VM_TICK = 256L;

  Thread VM_ACTIVITY_THREAD = new Thread(() -> {
    int vmActivity = Byte.MIN_VALUE;
    VM_ACTIVITY_FILE.set(new File(WORKSPACE.getAbsolutePath() + "/.vm-activity"));
    while(SCRIPT_INSTANCE.get().isAlive()) {
      try {
        OutputStream vmActivityStream = new FileOutputStream(VM_ACTIVITY_FILE.get());
        vmActivityStream.write(new byte[] { (byte) vmActivity });
        vmActivityStream.close();
        vmActivity = vmActivity >= Byte.MAX_VALUE ? Byte.MIN_VALUE : vmActivity + 1;
        Thread.sleep(VM_TICK);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  });

  Thread VM_SHUTDOWN_HOOK = new Thread(() -> {
    CLASS_FILE.get().delete();
    for(File tempFile : WORKSPACE.listFiles()) {
      String tempName = tempFile.getName();
      if(tempName.startsWith(CLASS_NAME.get() + '$') && tempName.endsWith(".class"))
      tempFile.delete();
    }
    if(VM_ACTIVITY_FILE.get() != null && VM_ACTIVITY_FILE.get().exists()) VM_ACTIVITY_FILE.get().delete();
    if(WORKSPACE.exists()) WORKSPACE.delete();
    if(SCRIPT_INSTANCE.get() != null && SCRIPT_INSTANCE.get().isAlive()) SCRIPT_INSTANCE.get().destroyForcibly();
  });

  Atomic<String[]> VM_ARGS = new Atomic<String[]>().seal();
  Atomic<Boolean> JUST_COMPILE = new Atomic<Boolean>().seal();

  static void main(String... args) {
    for(int i = 0; i < args.length; i++) args[i] = args[i].replace("\"", new String());
    VM_ARGS.set(args);
    if(contains(args, "-IDE")) ESRCEditor.INSTANCE.launch();
    else if(args.length > 0) {
      JUST_COMPILE.set(contains(args, "-compile"));
      String fileName = args[0];
      if(!fileName.endsWith(FILE_EXTENSION)) fileName += FILE_EXTENSION;
      ESRCCore.launch(fileName);
    }
  }

  char CP_SEPERATOR = System.getProperty("os.name").contains("Windows") ? ';' : ':';

  static String getClasspath(String[] jarImports) throws Exception {
    String classPath = new String();
    for(String jarImport : jarImports)
      if(!jarImport.isEmpty())
        classPath += String.valueOf(CP_SEPERATOR) + System.getProperty("user.dir") + '/' + jarImport + ".jar";
    classPath = "\"." + CP_SEPERATOR + ESRCCore.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1) + classPath + '\"';
    return classPath;
  }

  static String getDefaultScript() throws Exception {
    InputStream defaultInput = ESRCCore.class.getResourceAsStream("Default.esrc");
    byte[] defaultBuffer = new byte[defaultInput.available()];
    defaultInput.read(defaultBuffer);
    defaultInput.close();
    return new String(defaultBuffer).replace("%ESRC_INSTANCE%", CLASS_NAME.get());
  }

  static String getUserScript() throws Exception {
    InputStream scriptInput = new FileInputStream(SCRIPT_FILE.get());
    byte[] scriptBuffer = new byte[scriptInput.available()];
    scriptInput.read(scriptBuffer);
    scriptInput.close();
    return new String(scriptBuffer);
  }

  static void createJavaFile(String code) throws Exception {
    OutputStream javaOutput = new FileOutputStream(JAVA_FILE.get());
    javaOutput.write(code.getBytes());
    javaOutput.close();
  }

  static void compileJavaFile(String classPath) throws Exception {
    Vector<String> compileCommand = new Vector<String>();
    compileCommand.add("javac");
    compileCommand.add("-nowarn");
    compileCommand.add("-source");
    compileCommand.add("8");
    compileCommand.add("-target");
    compileCommand.add("8");
    compileCommand.add("-cp");
    compileCommand.add(classPath);
    compileCommand.add(JAVA_FILE.get().getName());
    ProcessBuilder compileProcess = new ProcessBuilder(compileCommand.cast(new String[compileCommand.dimension()]));
    compileProcess.directory(WORKSPACE);
    compileProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
    compileProcess.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    compileProcess.redirectError(ProcessBuilder.Redirect.INHERIT);
    compileProcess.start().waitFor();
  }

  static void executeScript(String classPath) throws Exception {
    Vector<String> scriptCommand = new Vector<String>();
    scriptCommand.add("java");
    scriptCommand.add("-cp");
    scriptCommand.add("\"." + CP_SEPERATOR + "/" + WORKSPACE.getAbsolutePath() + classPath.substring(2));
    scriptCommand.add(CLASS_FILE.get().getName().substring(0, CLASS_FILE.get().getName().lastIndexOf('.')));
    for(String vmArg : VM_ARGS.get()) scriptCommand.add(vmArg);
    ProcessBuilder scriptProcess = new ProcessBuilder(scriptCommand.cast(new String[scriptCommand.dimension()]));
    scriptProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
    scriptProcess.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    scriptProcess.redirectError(ProcessBuilder.Redirect.INHERIT);
    SCRIPT_INSTANCE.set(scriptProcess.start());
  }

  static void makeArchive() throws Exception {
    FileOutputStream fileOutput = new FileOutputStream(System.getProperty("user.dir") + '/' + CLASS_NAME.get().replace('_', ' ') + ".jar");
    java.util.zip.ZipOutputStream archiveOutput = new java.util.zip.ZipOutputStream(fileOutput);
    Vector<File> archiveFiles = new Vector<File>();
    for(File file : WORKSPACE.listFiles()) archiveFiles.add(file);
    for(int i = 0; i < archiveFiles.dimension(); i++) {
      File file = archiveFiles.get(i);
      if(file.isDirectory()) {
        for(File sub : file.listFiles()) archiveFiles.add(sub);
      } else {
        String entry = (file.getParent().substring(WORKSPACE.getAbsolutePath().length()) + '/' + file.getName()).replace('\\', '/');
        if(entry.startsWith("/")) entry = entry.substring(1);
        archiveOutput.putNextEntry(new java.util.zip.ZipEntry(entry));
        InputStream fileInput = new FileInputStream(file);
        byte[] fileBuffer = new byte[fileInput.available()];
        fileInput.read(fileBuffer);
        fileInput.close();
        archiveOutput.write(fileBuffer);
        archiveOutput.closeEntry();
      }
    }
    File jarFile = new File(ESRCCore.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().substring(1));
    java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(jarFile.getAbsolutePath());
    FileInputStream fileInput = new FileInputStream(jarFile);
    java.util.zip.ZipInputStream archiveInput = new java.util.zip.ZipInputStream(fileInput);
    java.util.zip.ZipEntry entry;
    while((entry = archiveInput.getNextEntry()) != null) {
      archiveOutput.putNextEntry(new java.util.zip.ZipEntry(entry.getName()));
      InputStream entryStream = zipFile.getInputStream(entry);
      byte[] entryBuffer = new byte[entryStream.available()];
      entryStream.read(entryBuffer);
      if(entry.getName().replace('\\', '/').equalsIgnoreCase("META-INF/MANIFEST.MF")) {
        String[] manifest = new String(entryBuffer).split("\n");
        for(int i = 0; i < manifest.length; i++)
          if(manifest[i].startsWith("Main-Class: "))
            manifest[i] = "Main-Class: " + CLASS_NAME.get();
        for(String line : manifest)
          archiveOutput.write((line + '\n').getBytes());
      } else archiveOutput.write(entryBuffer);
      entryStream.close();
      archiveOutput.closeEntry();
    }
    archiveInput.close();
    fileInput.close();
    archiveOutput.flush();
    archiveOutput.close();
    fileOutput.close();
  }

  static void launch(String scriptFile) {
    if(SCRIPT_FILE.get() != null) return;
    String[] filePath = scriptFile.replace("\\", "/").split("/");
    String fileName = filePath[filePath.length - 1];
    CLASS_NAME.set(fileName.substring(0, fileName.lastIndexOf(".")).replace(' ', '_'));
    SCRIPT_FILE.set(new File(scriptFile));
    JAVA_FILE.set(new File(WORKSPACE.getAbsolutePath() + '/' + CLASS_NAME.get() + ".java"));
    CLASS_FILE.set(new File(WORKSPACE.getAbsolutePath() + '/' + CLASS_NAME.get() + ".class"));
    try {
      if(!WORKSPACE.exists()) WORKSPACE.mkdir();
      String userScript = getUserScript();
      String[] jarImports = ESRCParser.getJarImports(userScript);
      String javaCode = ESRCParser.convertCode(CLASS_NAME.get(), getDefaultScript() + userScript);
      String classPath = getClasspath(jarImports);
      ESRCCore.createJavaFile(javaCode);
      ESRCCore.compileJavaFile(classPath);
      JAVA_FILE.get().delete();
      if(!JUST_COMPILE.get()) {
        ESRCCore.executeScript(classPath);
        VM_ACTIVITY_THREAD.start();
        Runtime.getRuntime().addShutdownHook(VM_SHUTDOWN_HOOK);
        SCRIPT_INSTANCE.get().waitFor();
      } else makeArchive();
      VM_SHUTDOWN_HOOK.start();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  Atomic<String> EXECUTED_PATH = new Atomic<String>(System.getProperty("user.dir"));

  Vector<Runnable> VM_TICK_LISTENER = new Vector<Runnable>();

  Runnable VM_ACTIVITY_CHECK = () -> {
    int prevActivityState = Byte.MIN_VALUE;
    File vmActivityFile = new File(EXECUTED_PATH.get() + "/.vm-activity");
    while(true) {
      try {
        Thread.sleep(VM_TICK);
        if(vmActivityFile.exists()) {
          InputStream vmActivityStream = new FileInputStream(vmActivityFile);
          byte[] vmActivityBuffer = new byte[vmActivityStream.available()];
          vmActivityStream.read(vmActivityBuffer);
          int vmActivityState = (int) vmActivityBuffer[0];
          if(prevActivityState == vmActivityState) break;
          else {
            prevActivityState = vmActivityState;
            VM_TICK_LISTENER.forEach(listener -> listener.run());
          }
          vmActivityStream.close();
        } else VM_TICK_LISTENER.forEach(listener -> listener.run());
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    System.exit(0);
  };

}
