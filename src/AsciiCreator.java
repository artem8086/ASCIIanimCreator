

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Артём Святоха
 */
public class AsciiCreator {

    
    static final ArrayList<ByteArrayOutputStream> operations = new ArrayList<>();
    
    static DataOutputStream out;

    public static void parseOperations(File operation) throws IOException {
        List<String> lines = Files.readAllLines(operation.toPath(), StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64536);
        operations.add(baos);
        DataOutputStream dos = new DataOutputStream(baos);
        //
        int color = 0xF;
        for (String line : lines) {
            String[] words = line.trim().split("\\s+");
            if (words.length != 0) {
                switch (words[0]) {
                case "": break;
                case "#": break; // Комментарий
                case "setColor": // Установка цвета
                    color = Integer.parseInt(words[1], 16);
                    break;
                case "block": // Повторять блок n раз
                    int time = Integer.parseInt(words[1]);
                    dos.writeByte(((time & 0xF00) >> 4) | 0x0);
                    dos.writeByte(time);
                    break;
                case "setScale":
                    int scale = Integer.parseInt(words[1]) - 1;
                    dos.writeByte(((scale & 0xF00) >> 4) | 0x1);
                    dos.writeByte(scale);
                    break;
                case "setPos": // Установка позици курсора
                    dos.writeByte(0x2);
                    dos.writeShort(Integer.parseInt(words[1]));
                    dos.writeShort(Integer.parseInt(words[2]));
                    break;
                case "moveCursor": // Сместить курсор на x/y символов
                    dos.writeByte(0x3);
                    dos.writeByte(Integer.parseInt(words[1]));
                    dos.writeByte(Integer.parseInt(words[2]));
                    break;
                case "drawChar": // Вывести символ
                    dos.writeByte((color << 4) | 0x4);
                    dos.writeByte(words[1].charAt(0));
                    break;
                case "drawCode": // Вывести символ
                    dos.writeByte((color << 4) | 0x4);
                    dos.writeByte(Integer.parseInt(words[1], 16));
                    break;
                case "fillRect": // Нарисовать прямоугольник
                    dos.writeByte((color << 4) | 0x5);
                    dos.writeShort(Integer.parseInt(words[1]));
                    dos.writeShort(Integer.parseInt(words[2]));
                    break;
                case "fillChar": // Залить символами область
                    dos.writeByte((color << 4) | 0x6);
                    dos.writeByte(Integer.parseInt(words[2]));
                    dos.writeByte(Integer.parseInt(words[3]));
                    dos.writeByte(words[1].charAt(0));
                    break;
                case "fillCode": // Залить символами область
                    dos.writeByte((color << 4) | 0x6);
                    dos.writeByte(Integer.parseInt(words[2]));
                    dos.writeByte(Integer.parseInt(words[3]));
                    dos.writeByte(Integer.parseInt(words[1], 16));
                    break;
                case "fillData": // Залить данными область
                    dos.writeByte((color << 4) | 0x7);
                    dos.writeByte(Integer.parseInt(words[1]));
                    dos.writeByte(Integer.parseInt(words[2]));
                    dos.writeByte(Integer.parseInt(words[3]));
                    dos.writeByte(Integer.parseInt(words[4]));
                    break;
                case "str": // Вывести строку
                    dos.writeByte((color << 4) | 0x8);
                    dos.writeByte(Integer.parseInt(words[1]));
                    break;
                case "substr": // Вывести подстроку
                    dos.writeByte((color << 4) | 0x9);
                    dos.writeByte(Integer.parseInt(words[1]));
                    dos.writeByte(Integer.parseInt(words[2]));
                    dos.writeByte(Integer.parseInt(words[3]));
                    break;
                default:
                    System.out.println("Unknow command - " + words[0] + "at line: " + line);
                }
            }
        }
        dos.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        out = new DataOutputStream(new FileOutputStream(args.length != 0 ? args[0] : "out.ascii"));
        int i = 0;
        File file;
        while ((file = new File("operation" + i + ".op")).exists()) {
            parseOperations(file);
            i++;
        }
        out.writeByte(operations.size());
        for (ByteArrayOutputStream operation : operations) {
            out.writeShort(operation.size());
            out.write(operation.toByteArray());
        }
        i = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
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
