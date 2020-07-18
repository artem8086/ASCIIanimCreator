package art.soft;

import art.soft.scripter.core.BaseFunction;
import art.soft.scripter.core.Namespace;
import art.soft.scripter.core.ScriptCore;
import art.soft.scripter.libs.SystemLib;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Артём Святоха
 */
public class AsciiCreator {

    
    static ByteArrayOutputStream baos;

    static DataOutputStream out, dos;

    static int color;

    public static void initCore(ScriptCore core) {
        Namespace global = core.getGlobalNamespace();
        global.set("FONT_WIDTH", 8);
        global.set("FONT_HEIGHT", 12);
        global.set("setColor", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                color = (Integer) args[0];
                return null;
            }
        });
        global.set("getColor", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                return color;
            }
        });
        global.set("block", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    int time = (Integer) args[0];
                    dos.writeByte(((time & 0xF00) >> 4) | 0x0);
                    dos.writeByte(time);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("setScale", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    int scale = (Integer) args[0] - 1;
                    dos.writeByte(((scale & 0xF00) >> 4) | 0x1);
                    dos.writeByte(scale);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("setPos", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte(0x2);
                    dos.writeShort((Integer) args[0]);
                    dos.writeShort((Integer) args[1]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("moveCursor", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte(0x3);
                    dos.writeByte((Integer) args[0]);
                    dos.writeByte((Integer) args[1]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("drawChar", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x4);
                    dos.writeByte((Integer) args[0]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("fillRect", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x5);
                    dos.writeShort((Integer) args[0]);
                    dos.writeShort((Integer) args[1]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("fillChar", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x6);
                    dos.writeByte((Integer) args[1]);
                    dos.writeByte((Integer) args[2]);
                    dos.writeByte((Integer) args[0]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("fillData", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x7);
                    dos.writeByte((Integer) args[0]);
                    dos.writeByte((Integer) args[1]);
                    dos.writeByte((Integer) args[2]);
                    dos.writeByte((Integer) args[3]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("str", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x8);
                    dos.writeByte((Integer) args[0]);
                } catch (IOException ex) {}
                return null;
            }
        });
        global.set("substr", new BaseFunction() {
            @Override
            public Object exec(Object[] args, ScriptCore core, Object thisPtr) {
                try {
                    dos.writeByte((color << 4) | 0x9);
                    dos.writeByte((Integer) args[0]);
                    dos.writeByte((Integer) args[1]);
                    dos.writeByte((Integer) args[2]);
                } catch (IOException ex) {}
                return null;
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        ScriptCore.Init();
        //
        ScriptCore core = new ScriptCore();
        core.setOutStream(System.out);
        core.setErrorStream(System.err);
        // Loading libs ...
        core.loadLibrary(new SystemLib());
        initCore(core);
        core.loadScriptFromFile("animation.ass");
        core.exec(null);
        //
        Namespace global = core.getGlobalNamespace();
        String path = (String) global.get("path");
        out = new DataOutputStream(new FileOutputStream(path != null ? path : "out.ascii"));
        Namespace operations = (Namespace) global.get("operations");
        int i = 0;
        File file;
        out.writeByte(operations.size());
        for (; i < operations.size(); i++) {
            baos = new ByteArrayOutputStream(65536);
            dos = new DataOutputStream(baos);
            //
            color = 0xF;
            core.execFunction((BaseFunction) operations.get(Integer.toString(i)), new Object[0], global);
            //
            out.writeShort(baos.size());
            out.write(baos.toByteArray());
            baos.close();
        }
        i = 0;
        baos = new ByteArrayOutputStream(256);
        while ((file = new File("data" + i + ".bin")).exists()) {
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available() & 0xFF;
            byte[] bytes = new byte[size];
            fis.read(bytes);
            baos.write(size);
            baos.write(bytes, 0, size);
            i++;
        }
        out.writeByte(i);
        out.write(baos.toByteArray());
        baos.close();
        out.close();
        System.out.println("Operations count: " + operations.size());
        System.out.println("Data count: " + i);
        System.out.println("Writing complete!");
    }
}
