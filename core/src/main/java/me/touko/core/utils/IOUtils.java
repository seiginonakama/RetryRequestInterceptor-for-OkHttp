package me.touko.core.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Utility class to handle I/O operations.
 *
 * @author zhoulei@shandianshua.com (Zhou Lei)
 */
public class IOUtils {

  private static final int BUFFER_SIZE = 8 * 1024;

  public static final String DEFAULT_ENCODING = "utf-8";

  private IOUtils() {
  }

  /**
   * Read the Stream content as a string.
   *
   * @param is The stream to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(InputStream is, String encoding) throws IOException {
    StringWriter sw = new StringWriter();
    try {
      copy(is, sw, encoding);
      return sw.toString();
    } finally {
      close(is);
      close(sw);
    }
  }

  private static void copy(InputStream input, Writer output, String encoding)
      throws IOException {
    InputStreamReader in =
        new InputStreamReader(input, encoding == null ? DEFAULT_ENCODING : encoding);
    char[] buffer = new char[BUFFER_SIZE];
    int n = 0;
    while (-1 != (n = in.read(buffer))) {
      output.write(buffer, 0, n);
    }
  }

  /**
   * Read file content to a String (always use utf-8).
   *
   * @param file The file to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(File file) throws IOException {
    return readString(file, DEFAULT_ENCODING);
  }

  /**
   * Read file content to a String.
   *
   * @param file The file to read
   * @return The String content
   * @throws IOException IOException
   */
  public static String readString(File file, String encoding) throws IOException {
    return readString(new FileInputStream(file), encoding);
  }

  /**
   * Write String content to a file (always use utf-8).
   *
   * @param content The content to read
   * @param file    The file to write
   * @throws IOException IOException
   */
  public static void writeString(String content, File file) throws IOException {
    writeString(content, new FileOutputStream(file), DEFAULT_ENCODING);
  }

  /**
   * Write String content to a file (always use utf-8).
   *
   * @param content The content to read
   * @param file    The file to write
   * @throws IOException IOException
   */
  public static void writeString(String content, File file, boolean append) throws IOException {
    writeString(content, new FileOutputStream(file, append), DEFAULT_ENCODING);
  }

  /**
   * Write String content to a stream (always use utf-8).
   *
   * @param content The content to read
   * @param os      The stream to write
   * @throws IOException IOException
   */
  public static void writeString(String content, OutputStream os, String encoding)
      throws IOException {
    try {
      PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, encoding));
      printWriter.write(content);
      printWriter.flush();
      os.flush();
    } finally {
      close(os);
    }
  }

  /**
   * Close stream.
   *
   * @param is The stream to close
   */
  public static void close(Closeable is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
