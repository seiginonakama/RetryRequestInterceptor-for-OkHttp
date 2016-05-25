package me.touko.core.storage;

import com.google.gson.Gson;

import me.touko.core.utils.GsonFactory;

/**
 * author: zhou date: 2015/12/23.
 */
public class GsonObjStorage<T> extends ObjStorage<T> {
  private static final Gson gson = GsonFactory.getGson();

  public GsonObjStorage(Class<T> tClass, Storage storage) {
    super(tClass, storage);
  }

  @Override
  protected String convertToString(T t) {
    return gson.toJson(t);
  }

  @Override
  protected T convertFromString(String str, Class<T> tClass) {
    return gson.fromJson(str, tClass);
  }
}

