/**
 * The Shell class represents a simple shell program that allows users to enter commands for execution.
 * It extends the Thread class and implements a basic command execution mechanism.
 * The shell continuously accepts user commands, parses them, and executes them accordingly.
 * Commands can be executed sequentially using ';' as a delimiter or concurrently using '&' as a delimiter.
 * The shell terminates when the user enters the command "exit".
 *
 * @author Ayleen Piteo-Tarpy
 */

import java.lang.StringBuffer;

class Shell extends Thread {

  /**
   * Primary execution method of the Shell class. Responsible for taking user commands from the command line, parsing
   * them, and executing them accordingly.
   *
   * PRECONDITIONS:   None
   * POSTCONDITIONS:  The method continuously accepts and executes user commands until the command "exit" is entered
   */
  public void run() {
    StringBuffer stringbuffer = new StringBuffer();
    int shellCount = 1;

    while (true) {
      stringbuffer.setLength(0);
      SysLib.cout("shell[" + shellCount + "]% ");
      SysLib.cin(stringbuffer);

      String[] args = SysLib.stringToArgs(stringbuffer.toString());

      if (args.length > 0) {
        if (processCommandLine(args) == -1 ) {
          SysLib.exit();
          return;
        }
      }

      if (!stringbuffer.isEmpty()) {
        shellCount++;
      }
    }
  }

  /**
   * Processes the command line arguments, parsing them to build individual commands,
   * and executes them accordingly.
   *
   * @param args    an array of strings representing the command line arguments
   * @return -1 if the command is "exit", indicating the termination of the program; otherwise, returns 0
   *
   * PRECONDITIONS:   None
   * POSTCONDITIONS:  The method parses the command line arguments, executes commands,
   *                 and returns control to the main execution loop.
   */
  private int processCommandLine(String[] args) {
    StringBuilder command = new StringBuilder();
    char delim = '\u0000';
    char tempDelim = '\u0000';
    int cid = 0;
    int j;

    // Separates and executes the full command line input as individual commands separated by delimiters
    for (int i = 0; i < args.length; i++) {
      for (j = i; j < args.length; j++) {
        String arg = args[j];

        if (arg.equals("&") || arg.equals(";")) {
          tempDelim = arg.charAt(0);
          break;
        } else if (j < args.length - 1 && (arg.equals("&") || arg.equals(";"))) {
          command.append(arg);
          tempDelim = arg.charAt(0);
          break;
        } else if (j == args.length - 1) {
          command.append(arg);
          break;
        } else {
          command.append(arg).append(" ");
        }
      }

      String[] execute = SysLib.stringToArgs(command.toString());
      i = j;

      if (execute.length > 0) {
        cid = executeCommand(execute, delim, cid);
        if (cid == -1) {
          if (delim == ';') {
            waitForThreadToTerminate(cid);
          }
          return -1;
        }
        delim = tempDelim;
      }
      command.setLength(0);
    }

    // completes all threads in current command line before getting new user input
    if (cid != 0 && (delim != '&' || !args[args.length - 1].equals("&"))) {
      waitForThreadToTerminate(cid);
    }
    return 0;
  }

  /**
   * Executes a command with the given delimiter and returns the thread ID of the executed command.
   *
   * @param execute an array of strings representing the command to execute
   * @param delim   the delimiter character indicating the type of execution ('&' or ';')
   * @param cid     the thread ID of the previous command, used for synchronization when using ';'
   * @return the thread ID of the executed command
   *
   * PRECONDITIONS:   None
   * POSTCONDITIONS:  The command is executed, and its thread ID is returned.
   *                  If the delimiter is ';', the method waits for the previous command to finish.
   */
  private int executeCommand(String[] execute, char delim, int cid) {
    int tempCid = 0;
    try {
      if (cid != 0 && delim == ';') {
        waitForThreadToTerminate(cid);
      }
      SysLib.cout(execute[0] + "\n");
      if (execute[0].equals("exit")) {
        return -1;
      }
      if (delim == '&' || delim == '\u0000') {
        tempCid = SysLib.exec(execute);
      } else {
        tempCid = SysLib.exec(execute);
      }
      if (tempCid != -1) {
        cid = tempCid;
      }
      return cid;
    } catch (Exception e) {
      SysLib.cout(e + "\n");
      return 0;
    } catch (Error e) {
      SysLib.cout(e + "\n");
      return 0;
    }
  }

  /**
   * Waits for the termination of a specific thread identified by its ID.
   *
   * @param cid the thread ID of the thread to wait for termination
   *
   * PRECONDITIONS:   None
   * POSTCONDITIONS:  The method runs until the specified thread has terminated.
   */
  private void waitForThreadToTerminate(int cid) {
    if (cid != -1) {
      while (SysLib.join() != cid);
    }
  }
}