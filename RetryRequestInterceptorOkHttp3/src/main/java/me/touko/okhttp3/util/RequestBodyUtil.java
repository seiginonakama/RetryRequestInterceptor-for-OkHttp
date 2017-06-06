/*
 *  Copyright (C) 2017 seiginonakama (https://github.com/seiginonakama).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.touko.okhttp3.util;


import java.io.IOException;

import okhttp3.Request;
import okio.Buffer;

/**
 * author: zhoulei date: 15/11/23.
 */
public class RequestBodyUtil {

  /**
   * read body form request
   */
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

  /**
   * whether request has body
   */
  public static boolean hasRequestBody(Request request) {
    try {
      return request.body() != null && request.body().contentLength() > 0;
    } catch (IOException e) {
      return false;
    }
  }
}
