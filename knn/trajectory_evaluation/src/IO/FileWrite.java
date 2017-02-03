/*
 * Decompiled with CFR 0_114.
 */
package IO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileWrite {
    public static void write(String path, String contents) throws IOException {
        FileWriter fstream = new FileWriter(path);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(contents);
        out.close();
    }
}

