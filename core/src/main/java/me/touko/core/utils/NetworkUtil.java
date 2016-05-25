package me.touko.core.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author zhoulei@shandianshua.com (Zhou Lei)
 */
public final class NetworkUtil {

  private NetworkUtil() {}

  public static boolean isNetworkConnected(Context context) {
    ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = null;
    try {
      activeNetworkInfo = connManager.getActiveNetworkInfo();
    } catch (Exception e) {
      // in some roms, here maybe throw a exception(like nullpoint).
      e.printStackTrace();
    }
    return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
  }

}
