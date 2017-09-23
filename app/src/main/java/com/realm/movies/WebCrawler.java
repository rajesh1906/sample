package com.realm.movies;

/**
 * Created by Rajesh Kumar on 23-09-2017.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

public class WebCrawler {

    /**
     * Interface for crawling callback
     */
    interface CrawlingCallback {
        void onPageCrawlingCompleted();

        void onPageCrawlingFailed(String Url, int errorCode);

        void onCrawlingCompleted();
    }

    private Context mContext;
    // SQLiteOpenHelper object for handling crawling database
    private CrawlerDB mCrawlerDB;
    // Set containing already visited URls
    private HashSet<String> crawledURL;
    // Queue for unvisited URL
    BlockingQueue<String> uncrawledURL;
    // For parallel crawling execution using ThreadPoolExecuter
    RunnableManager mManager;
    // Callback interface object to notify UI
    CrawlingCallback callback;
    // For sync of crawled and yet to crawl url lists
    Object lock;

    public WebCrawler(Context ctx, CrawlingCallback callback) {
        this.mContext = ctx;
        this.callback = callback;
        mCrawlerDB = new CrawlerDB(mContext);
        crawledURL = new HashSet<>();
        uncrawledURL = new LinkedBlockingQueue<>();
        lock = new Object();
    }

    /**
     * API to add crawler runnable in ThreadPoolExecutor workQueue
     *
     * @param Url
     *            - Url to crawl
     * @param isRootUrl
     */
    public void startCrawlerTask(String Url, boolean isRootUrl) {
        // If it's root URl, we clear previous lists and DB table content
        if (isRootUrl) {
            crawledURL.clear();
            uncrawledURL.clear();
            clearDB();
            mManager = new RunnableManager();
        }
        // If ThreadPoolExecuter is not shutting down, add wunable to workQueue
        if (!mManager.isShuttingDown()) {
            CrawlerRunnable mTask = new CrawlerRunnable(callback, Url);
            mManager.addToCrawlingQueue(mTask);
        }
    }

    /**
     * API to shutdown ThreadPoolExecuter
     */
    public void stopCrawlerTasks() {
        mManager.cancelAllRunnable();
    }

    /**
     * Runnable task which performs task of crawling and adding encountered URls
     * to crawling list
     *
     * @author CLARION
     *
     */
    private class CrawlerRunnable implements Runnable {

        CrawlingCallback mCallback;
        String mUrl;

        public CrawlerRunnable(CrawlingCallback callback, String Url) {
            this.mCallback = callback;
            this.mUrl = Url;
        }

        @Override
        public void run() {
            String pageContent = retreiveHtmlContent(mUrl);



            if (!TextUtils.isEmpty(pageContent.toString())) {
                insertIntoCrawlerDB(mUrl, pageContent);
//                Log.e("page content is ","<><>"+getHtmlCode(mUrl));
                synchronized (lock) {
                    crawledURL.add(mUrl);
                }
                mCallback.onPageCrawlingCompleted();
            } else {
                mCallback.onPageCrawlingFailed(mUrl, -1);
            }

            if (!TextUtils.isEmpty(pageContent.toString())) {
                // START
                // JSoup Library used to filter urls from html body
                Document doc = Jsoup.parse(pageContent.toString());
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String extractedLink = link.attr("href");
                    if (!TextUtils.isEmpty(extractedLink)) {
//                        synchronized (lock) {

//                        }

                    }
                }

                Elements media = doc.select("[src]");
                Elements title = doc.getElementsByTag("[title]");

//                print("\nMedia: (%d)", media.size());
                for (Element src : media) {
                    if (src.tagName().equals("img"))

//                       Log.e("images is ","<><>"+src.attr("abs:src"));
                    if (!crawledURL.contains(src.attr("abs:src"))) {
                        uncrawledURL.add(src.attr("abs:src"));
                    }
                    else
                        Log.e("images is ","<><>"+src.tagName());
//                        print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
                }

               // print("\nImports: (%d)", imports.size());
                for (Element link : title) {
//                    print(" * %s <%s> (%s)", link.tagName(),link.attr("abs:href"), link.attr("rel"));
                    Log.e("imports is ","<><>"+link.attr("abs:title"));

                }

//                print("\nLinks: (%d)", links.size());
                /*for (Element link : links) {
                    Log.e("links is ","<><>"+link.tagName());
                    print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
                }*/
                // End JSoup
            }
            // Send msg to handler that crawling for this url is finished
            // start more crawling tasks if queue is not empty
            mHandler.sendEmptyMessage(0);

        }


        private String getHtmlCode(String url){
            String html = "";
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);


                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
                html = str.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return html;
        }

        private String retreiveHtmlContent(String Url) {
            URL httpUrl = null;
            try {
                httpUrl = new URL(Url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            int responseCode = HttpStatus.SC_OK;
            StringBuilder pageContent = new StringBuilder();
            try {
                if (httpUrl != null) {
                    HttpURLConnection conn = (HttpURLConnection) httpUrl
                            .openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    responseCode = conn.getResponseCode();
                    if (responseCode != HttpStatus.SC_OK) {
                        throw new IllegalAccessException(
                                " http connection failed");
                    }
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        pageContent.append(line);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                mCallback.onPageCrawlingFailed(Url, -1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                mCallback.onPageCrawlingFailed(Url, responseCode);
            }

            return pageContent.toString();
        }

    }

    /**
     * API to clear previous content of crawler DB table
     */
    public void clearDB() {
        try {
            SQLiteDatabase db = mCrawlerDB.getWritableDatabase();
            db.delete(CrawlerDB.TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * API to insert crawled url info in database
     *
     * @param mUrl
     *            - crawled url
     * @param result
     *            - html body content of url
     */
    public void insertIntoCrawlerDB(String mUrl, String result) {

        if (TextUtils.isEmpty(result))
            return;

        SQLiteDatabase db = mCrawlerDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CrawlerDB.COLUMNS_NAME.CRAWLED_URL, mUrl);
        values.put(CrawlerDB.COLUMNS_NAME.CRAWLED_PAGE_CONTENT, result);

        db.insert(CrawlerDB.TABLE_NAME, null, values);
    }

    /**
     * To manage Messages in a Thread
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {

            synchronized (lock) {
                if (uncrawledURL != null && uncrawledURL.size() > 0) {
                    int availableTasks = mManager.getUnusedPoolSize();
                    while (availableTasks > 0 && !uncrawledURL.isEmpty()) {
//                           if(uncrawledURL.contains("telugu-latest")) {
                               startCrawlerTask(uncrawledURL.remove(), false);
//                           }
                        availableTasks--;
                    }
                }
            }

        };
    };

    /**
     * Helper class to interact with ThreadPoolExecutor for adding and removing
     * runnable in workQueue
     *
     * @author CLARION
     *
     */
    private class RunnableManager {

        // Sets the amount of time an idle thread will wait for a task before
        // terminating
        private static final int KEEP_ALIVE_TIME = 1;

        // Sets the Time Unit to seconds
        private final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Sets the initial threadpool size to 5
        private static final int CORE_POOL_SIZE = 5;

        // Sets the maximum threadpool size to 8
        private static final int MAXIMUM_POOL_SIZE = 8;

        // A queue of Runnables for crawling url
        private final BlockingQueue<Runnable> mCrawlingQueue;

        // A managed pool of background crawling threads
        private final ThreadPoolExecutor mCrawlingThreadPool;

        public RunnableManager() {
            mCrawlingQueue = new LinkedBlockingQueue<>();
            mCrawlingThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
                    MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
                    mCrawlingQueue);
        }

        private void addToCrawlingQueue(Runnable runnable) {
            mCrawlingThreadPool.execute(runnable);
        }

        private void cancelAllRunnable() {
            mCrawlingThreadPool.shutdownNow();
        }

        private int getUnusedPoolSize() {
            return MAXIMUM_POOL_SIZE - mCrawlingThreadPool.getActiveCount();
        }

        private boolean isShuttingDown() {
            return mCrawlingThreadPool.isShutdown()|| mCrawlingThreadPool.isTerminating();
        }
    }
}
