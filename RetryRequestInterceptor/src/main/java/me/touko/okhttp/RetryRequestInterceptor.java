package me.touko.okhttp;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import me.touko.okhttp.storage.FileStorage;
import me.touko.okhttp.storage.GsonObjStorage;
import me.touko.okhttp.storage.Storage;
import me.touko.okhttp.utils.CollectionUtils;
import me.touko.okhttp.utils.GsonFactory;
import me.touko.okhttp.utils.MD5Utils;
import me.touko.okhttp.utils.MainThreadPostUtils;
import me.touko.okhttp.utils.NetworkUtil;
import me.touko.okhttp.utils.RequestBodyUtil;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * a interceptor for okHttp, can retry request until {@link okhttp3.Response#isSuccessful()} return true
 * <p/>
 * author: zhou date: 2016/4/11.
 */
public class RetryRequestInterceptor implements Interceptor {
  private static final RetryRequestInterceptor singleton = new RetryRequestInterceptor();
  private RetryConfig retryConfig;
  private Context appContext;

  private GsonObjStorage<RequestWrapper> requestStorage;
  private FutureTask<Void> initFuture;
  private OkHttpClient okHttpClient;

  private static final String KEY_SUFFIX_SENDING_QUEST = ".sending";
  private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

  private long preRetryTime;

  private final Set<RetryResultListener> retryResultListeners = new HashSet<>();

  private RetryRequestInterceptor() {
  }

  /**
   * get RetryRequestInterceptor singleton
   *
   * @return the singleton
   */
  public static RetryRequestInterceptor getInstance() {
    return singleton;
  }

  /**
   * init method for RetryRequestInterceptor, must called before use
   *
   * @param context     RetryRequestInterceptor only cache {@link Context#getApplicationContext()}
   * @param retryConfig the RetryConfig for RetryRequestInterceptor {@link RetryConfig}
   */
  public void init(Context context, RetryConfig retryConfig) {
    if (retryConfig == null) {
      throw new IllegalStateException("retry config can't be null");
    }
    this.retryConfig = retryConfig;

    if (context == null) {
      throw new IllegalArgumentException("context can't be null");
    }
    appContext = context.getApplicationContext();

    final Storage storage = retryConfig.storage(context);
    if (storage == null) {
      throw new IllegalArgumentException("retryConfig.storage() can not return null");
    }
    this.requestStorage = new GsonObjStorage<>(RequestWrapper.class, storage);

    okHttpClient = retryConfig.okHttpClient();
    if (okHttpClient == null) {
      throw new IllegalArgumentException("okHttpClient can not be null");
    }

    initFuture = new FutureTask<>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        recoverSendingRequest();
        clearDirtyData();
        return null;
      }
    });
    threadPool.execute(initFuture);
  }

  /**
   * the listener to listen retry result
   */
  public interface RetryResultListener {
    /**
     * callback for request retry success
     *
     * @param request  the retry request
     * @param response the success response
     */
    void onRetrySuccess(Request request, Response response);

    /**
     * callback for occur exception when retry request, like {@link java.net.SocketTimeoutException} etc...
     *
     * @param request   the retry request
     * @param exception the occurred exception
     */
    void onRetryError(Request request, IOException exception);

    /**
     * callback for retry request failed, {@link RetryConfig#isSuccess(Request, Response)}
     *
     * @param request  the retry request
     * @param response the failed response
     */
    void onRetryFailed(Request request, Response response);

    /**
     * callback for RetryInterceptor abort this retry because retry times > {@link RetryConfig#maxRetryTimes()}
     * or request life > {@link RetryConfig#life()}
     *
     * @param request    the retry request
     * @param deadLine   the deadLine of this retry request
     * @param retryTimes the retry times for this retry request
     */
    void onAbortRetry(Request request, long deadLine, int retryTimes);
  }

  public static abstract class RetryConfig {

    /**
     * min duration for retry request
     *
     * @return duration in unix times
     */
    public abstract long minRetryDuration();

    /**
     * the life of retry request, if life < 0, request will live forever
     *
     * @return life in unix time
     */
    public abstract long life();

    /**
     * max retry times of request, if maxRetryTimes < 0, will retry unLimit times
     *
     * @return max retry times
     */
    public abstract int maxRetryTimes();

    /**
     * judge whether to retry request when {@link #isSuccess(Request, Response)} return false
     *
     * @return whether to retry request
     */
    public abstract boolean isRetryRequest(Request request);

    /**
     * the storage to store retry requests, you can override this method to provide customize storage,
     * like encrypt storage {@link me.touko.okhttp.storage.EncryptFileStorage} etc..
     *
     * @return storage {@link Storage} {@link FileStorage}
     */
    protected Storage storage(Context context) {
      return new FileStorage(new File(context.getFilesDir(), "retryInterceptor").getAbsolutePath(), 0);
    }

    /**
     * the okHttpClient to send retry requests, you can override this method to provide your customize OkHttpClient
     *
     * @return okHttpClient {@link OkHttpClient}
     */
    protected OkHttpClient okHttpClient() {
      return new OkHttpClient();
    }

    /**
     * the default method to judge whether should retry request, you can override this method to judge by your logic
     *
     * @param request  the retry request
     * @param response the retry response
     * @return if return true, means the request retry succeed, else should continue retry
     */
    protected boolean isSuccess(Request request, Response response) {
      return response != null && response.isSuccessful();
    }
  }

  /**
   * the trigger to invoke retry action, you had better invoke this method in Activity.onResume or Activity.onPause,
   * or the time point when you receive system broadcast event like {@link android.content.Intent#ACTION_BATTERY_CHANGED}
   * etc...
   */
  public void retryTrigger() {
    if (System.currentTimeMillis() - preRetryTime > retryConfig.minRetryDuration()
        && NetworkUtil.isNetworkConnected(appContext)) {
      threadPool.execute(new Runnable() {
        @Override
        public void run() {
          preRetryTime = System.currentTimeMillis();
          tryRetryRequest();
        }
      });
    }
  }

  /**
   * add {@link RetryResultListener} to RetryRequestInterceptor
   *
   * @param retryResultListener the listener to add
   */
  public void addRetryResultListener(RetryResultListener retryResultListener) {
    synchronized (retryResultListeners) {
      retryResultListeners.add(retryResultListener);
    }
  }

  /**
   * remove {@link RetryResultListener} from RetryRequestInterceptor
   *
   * @param retryResultListener the listener to remove
   */
  public void removeRetryResultListener(RetryResultListener retryResultListener) {
    synchronized (retryResultListeners) {
      retryResultListeners.remove(retryResultListener);
    }
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    try {
      initFuture.get();
      Request request = chain.request();
      if (!retryConfig.isRetryRequest(request)) {
        return chain.proceed(request);
      }
      try {
        Response response = chain.proceed(request);
        if (!retryConfig.isSuccess(request, response)) {
          saveToStorage(new RequestWrapper(request, retryConfig.life(), retryConfig.maxRetryTimes()));
        }
        return response;
      } catch (IOException e) {
        saveToStorage(new RequestWrapper(request, retryConfig.life(), retryConfig.maxRetryTimes()));
        throw new IOException(e);
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return chain.proceed(chain.request());
    }
  }

  private void saveToStorage(RequestWrapper requestWrapper) {
    if (requestWrapper == null) {
      return;
    }
    synchronized (requestStorage) {
      requestStorage.put(requestWrapper.getMd5(), requestWrapper);
    }
  }

  private void tryRetryRequest() {
    List<RequestWrapper> requestWrappers = outRetryRequests();
    if (CollectionUtils.isEmpty(requestWrappers)) {
      return;
    }
    for (RequestWrapper requestWrapper : requestWrappers) {
      Response response;
      Request request = requestWrapper.toRequest();
      try {
        response = executeRequest(request);
      } catch (IOException e) {
        restoreSendingRequest(requestWrapper);
        notifyRetryError(request, e);
        continue;
      }
      if (retryConfig.isSuccess(request, response)) {
        finishSendRequest(requestWrapper);
        notifyRetrySuccess(request, response);
      } else {
        restoreSendingRequest(requestWrapper);
        notifyRetryFailed(request, response);
      }
    }
  }

  private Response executeRequest(Request request) throws IOException {
    return okHttpClient.newCall(request).execute();
  }

  private List<RequestWrapper> outRetryRequests() {
    List<RequestWrapper> requestWrappers = new ArrayList<>();
    synchronized (requestStorage) {
      Set<String> keyset = requestStorage.getKeys();
      if (keyset.isEmpty()) {
        return requestWrappers;
      }
      for (String key : keyset) {
        if (isSendingRequest(key)) {
          continue;
        }
        RequestWrapper requestWrapper = requestStorage.getFirst(key);
        if (deleteIfDirty(key, requestWrapper)) {
          continue;
        }
        moveToSendingRequest(key, requestWrapper);
        requestWrappers.add(requestWrapper);
      }
    }
    return requestWrappers;
  }

  private void moveToSendingRequest(String key, RequestWrapper requestWrapper) {
    String sendingKey = key + KEY_SUFFIX_SENDING_QUEST;
    requestWrapper.currentRetryTimes += 1;
    synchronized (requestStorage) {
      requestStorage.delete(key);
      requestStorage.put(sendingKey, requestWrapper);
    }
  }

  private void finishSendRequest(RequestWrapper requestWrapper) {
    String sendingKey = requestWrapper.getMd5() + KEY_SUFFIX_SENDING_QUEST;
    synchronized (requestStorage) {
      requestStorage.delete(sendingKey);
    }
  }

  private void restoreSendingRequest(RequestWrapper requestWrapper) {
    String md5Key = requestWrapper.getMd5();
    String sendingKey = md5Key + KEY_SUFFIX_SENDING_QUEST;
    synchronized (requestStorage) {
      requestStorage.rename(sendingKey, md5Key);
    }
  }

  private void recoverSendingRequest() {
    synchronized (requestStorage) {
      Set<String> keySet = requestStorage.getKeys();
      if (keySet.isEmpty()) {
        return;
      }
      for (String key : keySet) {
        if (key.endsWith(KEY_SUFFIX_SENDING_QUEST)) {
          requestStorage.put(convertSendingKeyToMd5Key(key),
              requestStorage.getFirst(key));
          requestStorage.delete(key);
        }
      }
    }
  }

  private void clearDirtyData() {
    synchronized (requestStorage) {
      Set<String> md5KeySet = requestStorage.getKeys();
      if (md5KeySet.isEmpty()) {
        return;
      }
      for (String key : md5KeySet) {
        RequestWrapper requestWrapper = requestStorage.getFirst(key);
        deleteIfDirty(key, requestWrapper);
      }
    }
  }

  private boolean isSendingRequest(String key) {
    return key != null && key.endsWith(KEY_SUFFIX_SENDING_QUEST);
  }

  private boolean isDirtyData(String key, RequestWrapper requestWrapper) {
    if (requestWrapper == null || requestWrapper.isDead()) {
      return true;
    }
    String md5Key;
    if (isSendingRequest(key)) {
      md5Key = convertSendingKeyToMd5Key(key);
    } else {
      md5Key = key;
    }
    return !requestWrapper.getMd5().equals(md5Key);
  }

  private boolean deleteIfDirty(String key, RequestWrapper requestWrapper) {
    if (isDirtyData(key, requestWrapper)) {
      requestStorage.delete(key);
      if (requestWrapper != null) {
        notifyRetryAbort(requestWrapper.toRequest(), requestWrapper.deadLine, requestWrapper.currentRetryTimes - 1);
      }
      return true;
    }
    return false;
  }

  private String convertSendingKeyToMd5Key(String sendingKey) {
    return sendingKey.substring(0, sendingKey.length() - KEY_SUFFIX_SENDING_QUEST.length());
  }

  private void notifyRetryListener(ListenerRunnable runnable) {
    if (retryResultListeners.isEmpty()) {
      return;
    }
    for (final RetryResultListener retryResultListener : retryResultListeners) {
      runnable.setListener(retryResultListener);
      MainThreadPostUtils.post(runnable);
    }
  }

  private void notifyRetrySuccess(final Request request, final Response response) {
    notifyRetryListener(new ListenerRunnable() {
      @Override
      void doNotify(RetryResultListener retryResultListener) {
        retryResultListener.onRetrySuccess(request, response);
      }
    });
  }

  private void notifyRetryError(final Request request, final IOException exception) {
    notifyRetryListener(new ListenerRunnable() {
      @Override
      void doNotify(RetryResultListener retryResultListener) {
        retryResultListener.onRetryError(request, exception);
      }
    });
  }

  private void notifyRetryFailed(final Request request, final Response response) {
    notifyRetryListener(new ListenerRunnable() {
      @Override
      void doNotify(RetryResultListener retryResultListener) {
        retryResultListener.onRetryFailed(request, response);
      }
    });
  }

  private void notifyRetryAbort(final Request request, final long deadLine, final int retryTimes) {
    notifyRetryListener(new ListenerRunnable() {
      @Override
      void doNotify(RetryResultListener retryResultListener) {
        retryResultListener.onAbortRetry(request, deadLine, retryTimes);
      }
    });
  }

  private static class RequestWrapper implements Serializable {
    private String url;
    private String method;
    private Map<String, List<String>> headers;
    private String mediaType;
    private byte[] body;
    private long deadLine;
    private int currentRetryTimes;
    private int maxRetryTimes;

    public RequestWrapper(Request request, long life, int maxRetryTimes) throws IOException {
      url = request.url().toString();
      method = request.method();
      headers = request.headers().toMultimap();
      if (RequestBodyUtil.hasRequestBody(request)) {
        try {
          mediaType = request.body().contentType().toString();
          body = RequestBodyUtil.readBody(request);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (life < 0) {
        deadLine = -1;
      } else {
        deadLine = System.currentTimeMillis() + life;
      }
      this.maxRetryTimes = maxRetryTimes;
    }

    public Request toRequest() {
      Request.Builder builder = new Request.Builder();
      builder.url(url)
          .method(method, body == null ? null : RequestBody.create(MediaType.parse(mediaType), body));
      if (headers.size() > 0) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
          for (String value : entry.getValue()) {
            builder.addHeader(entry.getKey(), value);
          }
        }
      }
      return builder.build();
    }

    public String getMd5() {
      StringBuilder srcBuilder = new StringBuilder();
      srcBuilder.append("url:").append(url)
          .append("method:").append(method)
          .append("headers:").append(GsonFactory.getGson().toJson(headers))
          .append("deadLine:").append(deadLine);
      if (body != null) {
        srcBuilder.append("mediaType:").append(mediaType)
            .append("body:").append(Arrays.toString(body));
      }
      return MD5Utils.MD5(srcBuilder.toString());
    }

    public boolean isDead() {
      return (deadLine >= 0 && System.currentTimeMillis() > deadLine)
          || (maxRetryTimes >= 0 && currentRetryTimes > maxRetryTimes);
    }
  }

  public abstract class ListenerRunnable implements Runnable {
    public RetryResultListener listener;

    public ListenerRunnable() {
    }

    public void setListener(RetryResultListener listener) {
      this.listener = listener;
    }

    abstract void doNotify(RetryResultListener retryResultListener);

    @Override
    public final void run() {
      doNotify(listener);
    }
  }
}
