package art.soft.scripter;

import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.libs.SystemLib;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Артём Святоха
 */
public class scripter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnsupportedEncodingException, IOException {
        ScriptCore.Init();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "Cp866"));
        //
        ScriptCore core = new ScriptCore();
        core.setOutStream(System.out);
        core.setErrorStream(System.err);
        // Loading libs ...
        core.loadLibrary(new SystemLib());
        //
        System.out.println("        ╔══════════════════════════════════════════════════════════╗");
        System.out.println("        ║     ___    ____  ______    _____  ____   ______ ______   ║");
        System.out.println("        ║    /   |  / __ \\/_  __/   / ___/ / __ \\ / ____//_  __/   ║");
        System.out.println("        ║   / /| | / /_/ / / /      \\__ \\ / / / // /__    / /      ║");
        System.out.println("        ║  / ___ |/ _, _/ / /      ___/ // /_/ // ___/   / /       ║");
        System.out.println("        ║ /_/  |_/_/ |_| /_/      /____/ \\____//_/      /_/  inc.  ║");
        System.out.println("        ║                                                          ║");
        System.out.println("        ╠══════════════════════════════════════════════════════════╣");
        System.out.println("        ║                 ArtSoft Scripter start...                ║");
        System.out.println("        ╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        //
        for (String arg : args) {
            core.loadScriptFromFile(arg);
            core.exec(null);
        }
        //
        System.out.println();
        while (true) {
            System.out.print("> ");
            String script = br.readLine();
            //System.out.println("Build and run script...");
            core.loadScript(script);
            core.execWithEcho(null);
            //System.out.println(intr.start(br.readLine()));
        }
    }
}
