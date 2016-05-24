package me.touko.okhttp.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * @author zhoulei@shandianshua.com (Zhou Lei)
 */
public class MainThreadPostUtils {
  private static Handler handler;

  private static final byte[] handlerLock = new byte[0];

  public static Handler getHandler() {
    synchronized (handlerLock) {
      if (handler == null) {
        handler = new Handler(Looper.getMainLooper());
      }
    }
    return handler;
  }

  public static void post(Runnable run) {
    if(isMainThread()) {
      run.run();
    } else {
      getHandler().post(run);
    }
  }

  public static boolean isMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }
}
