package com.hpe.application.automation.tfs;

/**
 * Created by ybobrik on 1/22/2017.
 */

import javax.crypto.NullCipher;
import java.lang.String;
import java.util.Arrays;

enum RunKind {
    lep,
    le;
}

public class MainClass {
    private static final Integer ExitCode_Error_MissingArgumetnt = 1;
    private static final Integer ExitCode_Error_IllegalRunKind = 2;
    private static final Integer ExitCode_Error_MissingRunKind = 3;
    private static final Integer ExitCode_Error_TaskExecutionInnerException = 4;
    private static final Integer ExitCode_Error_FailedParseArgs = 5;
    private static final Integer ExitCode_Error_FailedExecute = 6;

    public static void main (String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument");
            System.exit(ExitCode_Error_MissingArgumetnt);
        }

        RunKind rk = getRunKind(args[0]);
        switch (rk) {
            case lep: {
                try {
                    System.out.println("ALM Lab Management Environment preparation task recognized");
                    LabEnvPrepTask t = new LabEnvPrepTask();
                    try {
                        t.parseArgs(Arrays.copyOfRange(args, 1, args.length));
                    }
                    catch(Throwable th) {
                        System.out.println("Parameter parsing FAILED with error: " + th.getMessage());
                        System.exit(ExitCode_Error_FailedParseArgs);
                    }
                    try {
                        t.execute();
                    }
                    catch(Throwable th) {
                        System.out.println("Execution FAILED with error: " + th.getMessage());
                        System.exit(ExitCode_Error_FailedExecute);
                    }
                }
                catch (Throwable th) {
                    System.out.println("Failed run with error: " + th.toString());
                    System.exit(ExitCode_Error_TaskExecutionInnerException);
                }
                break;
            }
            case le: {
                break;
            }
        }
        System.exit(0);
    }

    private static RunKind getRunKind(String arg) {
        try {
            return RunKind.valueOf(arg.toLowerCase());
        }
        catch (IllegalArgumentException iae) {
            System.out.println("Illegal Run Kind argument value");
            System.exit(ExitCode_Error_IllegalRunKind);
        }
        catch (NullPointerException npe) {
            System.out.println("Missing Run Kind argument");
            System.exit(ExitCode_Error_MissingRunKind);
        }
        return null;
    }
}
