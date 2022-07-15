
using System;
using System.Diagnostics;
using System.Windows.Forms;
using System.Threading;
using System.IO;

namespace ESRC {

  public static class ProcessExtension {

    public static bool IsRunning(this Process sub) {
      if(sub == null) throw new ArgumentNullException("process");
      try {
        Process.GetProcessById(sub.Id);
      } catch (ArgumentException) {
        return false;
      }
      return true;
    }

    public static void BeginOutputRead(this Process sub) {
      new Thread(() => {
        while(!sub.StandardOutput.EndOfStream)
        Console.WriteLine(sub.StandardOutput.ReadLine());
      }).Start();
    }

    public static void BeginErrorRead(this Process sub) {
      new Thread(() => {
        while(!sub.StandardError.EndOfStream)
        Console.WriteLine(sub.StandardError.ReadLine());
      }).Start();
    }

    public static void BeginInputTransfer(this Process sub) {
      new Thread(() => {
        while(sub.IsRunning())
        sub.StandardInput.Write(Console.Read());
      }).Start();
    }

  }

  public class WinExe {

    public static readonly string ScriptFile = Application.StartupPath + "/esrc.bat";

    public static void Main(string[] args) {
      using(var sub = new Process()) {
        var info = new ProcessStartInfo();
        info.FileName = ScriptFile;
        info.Arguments = String.Join(" ", args);
        info.RedirectStandardInput = true;
        info.RedirectStandardOutput = true;
        info.RedirectStandardError = true;
        info.UseShellExecute = false;
        sub.StartInfo = info;
        sub.Start();
        sub.BeginOutputRead();
        sub.BeginErrorRead();
        // TODO: Fix Console-Input
        //sub.BeginInputTransfer();
        sub.WaitForExit();
      }
    }

  }

}
