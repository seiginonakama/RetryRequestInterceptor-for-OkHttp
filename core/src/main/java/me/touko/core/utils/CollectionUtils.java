package me.touko.core.utils;

import java.util.Collection;

/**
 * Usage: Util for collections.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CollectionUtils {

  public static <T> boolean isEmpty(Collection<T> list) {
    return list == null || list.isEmpty();
  }
}
