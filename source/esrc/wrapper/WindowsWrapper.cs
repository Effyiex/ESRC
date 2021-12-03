
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace Effyiex.Source {

   public class WinWrapper {

    public readonly byte[] Bytecode; /*{INJECTION}*/

    public readonly string[] ProgramArgs;
    public readonly string JarFile, ExeFile;

    public static void Main(string[] args) {
      new WinWrapper(args);
    }

    public WinWrapper(string[] args) {
      this.ProgramArgs = args;
      this.ExeFile = Process.GetCurrentProcess().MainModule.FileName;
      this.JarFile = Path.GetTempPath() + '\\' + ExeFile.Substring(ExeFile.LastIndexOf('\\') + 1).Replace(".exe", ".jar");
      this.createProcess();
    }

    public void createProcess() {
      byte[] bytes = new byte[Bytecode.Length];
      for(int i = 0; i < bytes.Length; i++) {
        byte b = Bytecode[i];
        b = (byte) (b + 128);
        if(b > 255)
          b = (byte) (b - 256);
        bytes[i] = b;
      }
      ProcessStartInfo info = new ProcessStartInfo();
      info.Arguments = string.Empty;
      foreach(string arg in ProgramArgs) info.Arguments += ' ' + arg;
      using(FileStream stream = File.OpenWrite(JarFile)) {
        stream.Write(bytes, 0, bytes.Length);
        stream.Close();
      }
      info.FileName = JarFile;
      info.WorkingDirectory = Environment.CurrentDirectory;
      Process.Start(info).WaitForExit();
      File.Delete(JarFile);
    }

   }

}
