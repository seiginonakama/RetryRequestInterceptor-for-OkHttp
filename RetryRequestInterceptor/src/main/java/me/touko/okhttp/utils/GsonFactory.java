package me.touko.okhttp.utils;

import com.google.gson.Gson;

/**
 * author: zhoulei date: 15/12/4.
 */
public class GsonFactory {
  private static Gson gson;

  public static synchronized Gson getGson() {
    if (gson == null) {
      gson = new Gson();
    }
    return gson;
  }
}
