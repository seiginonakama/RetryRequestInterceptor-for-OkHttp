package me.touko.okhttp2.util;

import com.squareup.okhttp.Request;

import java.io.IOException;

import okio.Buffer;

/**
 * author: zhoulei date: 15/11/23.
 */
public class RequestBodyUtil {

  public static byte[] readBody(Request request) throws IOException {
    if (!hasRequestBody(request)) {
      return new byte[0];
    } else {
      final Request copy = request.newBuilder().build();
      final Buffer buffer = new Buffer();
      copy.body().writeTo(buffer);
      return buffer.readByteArray();
    }
  }

  public static boolean hasRequestBody(Request request) {
    try {
      return request.body() != null && request.body().contentLength() > 0;
    } catch (IOException e) {
      return false;
    }
  }
}
