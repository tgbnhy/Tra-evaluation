/*
 * Decompiled with CFR 0_114.
 */
package IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class FileRead {
    public static String readFile(String fileName) throws IOException {
        //FileInputStream stream = new FileInputStream(new File(fileName));
        FileInputStream stream = new FileInputStream(new File("EDwP-data.txt"));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            String string = Charset.defaultCharset().decode(bb).toString();
            return string;
        }
        finally {
            stream.close();
        }
    }
}

