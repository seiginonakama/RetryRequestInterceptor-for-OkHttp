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
