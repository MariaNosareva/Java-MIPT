package ru.mipt.java2016.homework.g596.gerasimov.task3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.NotDirectoryException;

/**
 * Created by geras-artem on 17.11.16.
 */
public class StorageFileIO {
    private File file;

    private File tmpFile;

    private RandomAccessFile randomAccessFile;

    private BufferedInputStream inputStream;

    private BufferedOutputStream outputStream;

    private ByteBuffer intBuffer = ByteBuffer.allocate(4);

    public StorageFileIO(String directoryPath, String fileName) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            throw new NotDirectoryException("Directory not found");
        }
        file = new File(directoryPath, fileName);
        tmpFile = new File(directoryPath, "tmp.db");
        file.createNewFile();
        outputStream = new BufferedOutputStream(new FileOutputStream(file, true));
        randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public int readSize(long offset) throws IOException {
        randomAccessFile.seek(offset);
        return randomAccessFile.readInt();
    }

    public ByteBuffer readField(int size) throws IOException {
        ByteBuffer result = ByteBuffer.allocate(size);
        randomAccessFile.readFully(result.array());
        return result;
    }

    public void writeSize(int size) throws IOException {
        intBuffer.putInt(0, size);
        outputStream.write(intBuffer.array());
    }

    public void writeField(ByteBuffer toWrite) throws IOException {
        outputStream.write(toWrite.array());
    }

    public void enterCopyMode() throws IOException {
        randomAccessFile.close();
        outputStream.close();
        tmpFile.createNewFile();
        inputStream = new BufferedInputStream(new FileInputStream(file));
        outputStream = new BufferedOutputStream((new FileOutputStream(tmpFile)));
    }

    public void exitCopyMode() throws IOException {
        inputStream.close();
        outputStream.close();
        tmpFile.renameTo(file);
        randomAccessFile = new RandomAccessFile(file, "rw");
        outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
    }

    public int copyReadSize() throws IOException {
        if (inputStream.read(intBuffer.array()) != 4) {
            throw new IOException("Read error");
        }
        return intBuffer.getInt(0);
    }

    public ByteBuffer copyReadField(int size) throws IOException {
        byte[] array = new byte[size];
        if (inputStream.read(array) != size) {
            throw new IOException("Read error");
        }
        return ByteBuffer.wrap(array);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public long fileLength() throws IOException {
        return randomAccessFile.length();
    }

    public void close() throws IOException {
        intBuffer.putInt(0, -1);
        outputStream.write(intBuffer.array());
        outputStream.close();
        randomAccessFile.close();
    }
}