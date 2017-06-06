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

import java.security.MessageDigest;

/**
 * @author yaoyi@shandianshua.com (Yi Yao)
 */
public class MD5Utils {


  public static String MD5(String str)
  {
    MessageDigest md5 = null;
    try
    {
      md5 = MessageDigest.getInstance("MD5");
    } catch (Exception e)
    {
      e.printStackTrace();
      return "";
    }

    char[] charArray = str.toCharArray();
    byte[] byteArray = new byte[charArray.length];

    for (int i = 0; i < charArray.length; i++)
    {
      byteArray[i] = (byte) charArray[i];
    }
    byte[] md5Bytes = md5.digest(byteArray);

    StringBuffer hexValue = new StringBuffer();
    for (int i = 0; i < md5Bytes.length; i++)
    {
      int val = ((int) md5Bytes[i]) & 0xff;
      if (val < 16)
      {
        hexValue.append("0");
      }
      hexValue.append(Integer.toHexString(val));
    }
    return hexValue.toString();
  }

}
