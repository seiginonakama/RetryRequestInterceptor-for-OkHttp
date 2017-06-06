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

import android.text.TextUtils;

/**
 * author: zhoulei date: 15/4/18.
 */
public class NumberParseUtils {
  public static boolean canParseLong(String string, int radix) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Long.parseLong(string, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean canParseLong(String string) {
    return canParseLong(string, 10);
  }

  public static long parseLong(String string, long cannotParseValue) {
    return parseLong(string, cannotParseValue, 10);
  }

  public static long parseLong(String string, long cannotParseValue, int radix) {
    try {
      return Long.parseLong(string, radix);
    } catch (NumberFormatException e) {
      return cannotParseValue;
    }
  }

  public static boolean canParseInt(String string, int radix) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Integer.parseInt(string, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean canParseInt(String string) {
    return canParseInt(string, 10);
  }

  public static int parseInt(String string, int cannotParseValue) {
    return parseInt(string, cannotParseValue, 10);
  }

  public static int parseInt(String string, int cannotParseValue, int radix) {
    try {
      return Integer.parseInt(string, radix);
    } catch (NumberFormatException e) {
      return cannotParseValue;
    }
  }

  public static float parseFloat(String string, float cannotParseValue) {
    try {
      return Float.parseFloat(string);
    } catch (NumberFormatException e) {
      return cannotParseValue;
    }
  }

  public static boolean canParseShort(String string, int radix) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Short.parseShort(string, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean canParseShort(String string) {
    return canParseShort(string, 10);
  }

  public static boolean canParseByte(String string, int radix) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Byte.parseByte(string, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean canParseByte(String string) {
    return canParseByte(string, 10);
  }

  public static boolean canParseFloat(String string) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Float.parseFloat(string);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static boolean canParseDouble(String string) {
    if (TextUtils.isEmpty(string)) {
      return false;
    }
    try {
      Double.parseDouble(string);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
