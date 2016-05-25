## RetryRequestInterceptor for OkHttp2， OkHttp3
</br>
a RetryRequestInterceptor for okHttp2, OkHttp3, has those features:

* will retry request until success or retry times over limit , or request live time over limit; 
* you can listen retry result by provide a RetryResultListener;
* you can provide a encrypt storage to keep retry request safe;
* invoke retry action by trigger, no loop thread, no waste cpu.

---
### Usage

####Step 1 : init RetryRequestInterceptor before use
you can simple init RetryRequestInterceptor in Application.onCreate() method

```java
  @Override
  public void onCreate() {
    super.onCreate();
    RetryRequestInterceptor.getInstance().init(this, new RetryConfig() {
      /**
       * min duration for retry request
       *
       * @return duration in unix times
       */
      @Override
      public long minRetryDuration() {
        return 1000L * 10;
      }

      /**
       * the life of retry request, if life < 0, request will live forever
       *
       * @return life in unix time
       */
      @Override
      public long life() {
        return -1; //the life of retry request, if life < 0, request will live forever
      }

      /**
       * max retry times of request, if maxRetryTimes < 0, will retry unLimit times
       *
       * @return max retry times
       */
      @Override
      public int maxRetryTimes() {
        return 10;
      }

      /**
       * judge whether to retry request when {@link #isSuccess(Request, Response)} return false
       *
       * @return whether to retry request
       */
      @Override
      public boolean isRetryRequest(Request request) {
        return false; //judge whether to retry request
      }
    }));
  }
```
####Step 2 : add RetryRequestInterceptor to your OkHttpClient

```java
    mOkHttpClient.interceptors().add(RetryRequestInterceptor.getInstance());
```

####Step 3 : invoke retry action by random time point

you can simple invoke retry action in Activity.onPause() and Activity.onResume() method

```java
@Override
public void onPause() {
  super.onPause();
  RetryRequestInterceptor.getInstance().retryTrigger();
}

@Override
public void onResume() {
  super.onResume();
  RetryRequestInterceptor.getInstance().retryTrigger();
}
```

####(Optional)Step 4 : add RetryResultListener

```java
    RetryRequestInterceptor.getInstance().addRetryResultListener(new RetryResultListener() {
      @Override
      public void onRetrySuccess(Request request, Response response) {
        
      }

      @Override
      public void onRetryError(Request request, IOException exception) {

      }

      @Override
      public void onRetryFailed(Request request, Response response) {

      }

      @Override
      public void onAbortRetry(Request request, long deadLine, int retryTimes) {

      }
    });
```

####(Optional)Step 5 : Override more default RetryConfig method in Step 1 to make retry more customizable
```java
RetryRequestInterceptor.getInstance().init(this, new RetryConfig() {
    
    ......
    
    /**
     * the storage to store retry requests, you can override this method to provide customize storage,
     * like encrypt storage {@link me.touko.okhttp.retryinterceptor.storage.EncryptFileStorage} etc..
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
```