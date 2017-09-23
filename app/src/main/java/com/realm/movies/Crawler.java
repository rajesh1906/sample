package com.realm.movies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Rajesh Kumar on 23-09-2017.
 */

public class Crawler extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout crawlingInfo;
    private Button startButton;
    private EditText urlInputView;
    private TextView progressText;
    int crawledUrlCount;
    // state variable to check crawling status
    boolean crawlingRunning;
    // For sending message to Handler in order to stop crawling after 60000 ms
    private static final int MSG_STOP_CRAWLING = 111;
    private static final int CRAWLING_RUNNING_TIME = 60000;
    private WebCrawler crawler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crawled);
        crawlingInfo = (LinearLayout) findViewById(R.id.crawlingInfo);
        startButton = (Button) findViewById(R.id.start);
        urlInputView = (EditText) findViewById(R.id.webUrl);
        progressText = (TextView) findViewById(R.id.progressText);
        crawler = new WebCrawler(this, mCallback);
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            stopCrawling();
        };
    };

    private void stopCrawling() {
        if (crawlingRunning) {
            crawler.stopCrawlerTasks();
            crawlingRunning = false;
            if (crawledUrlCount > 0)
                Toast.makeText(getApplicationContext(),printCrawledEntriesFromDb() + "pages crawled",Toast.LENGTH_SHORT).show();

            crawledUrlCount = 0;

        }
    }

    protected int printCrawledEntriesFromDb() {

        int count = 0;
        CrawlerDB mCrawlerDB = new CrawlerDB(this);
        SQLiteDatabase db = mCrawlerDB.getReadableDatabase();

        Cursor mCursor = db.query(CrawlerDB.TABLE_NAME, null, null, null, null,
                null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            count = mCursor.getCount();
            mCursor.moveToFirst();
            int columnIndex = mCursor
                    .getColumnIndex(CrawlerDB.COLUMNS_NAME.CRAWLED_URL);
            String format=".jpg";
            String format1="movie";
            for (int i = 0; i < count; i++) {
                if (mCursor.getString(columnIndex).contains(format)) {
                        Log.e("AndroidSRC_Crawler",
                                "Crawled Url " + mCursor.getString(columnIndex));


                }else{
//                    Log.e("not a image ","<><>"+mCursor.getString(columnIndex));
                }
                mCursor.moveToNext();
            }
        }

        return count;
    }

//    public static boolean isImageFile(String path) {
//        Image image = BitmapFactory.read(new URL(path));
//        if(image != null){
//            System.out.println("IMAGE");
//        }else{
//            System.out.println("NOT IMAGE");
//        }
//    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.start:
                String webUrl = "http://123freemovies.net/genres/action.html";
                if (TextUtils.isEmpty(webUrl)) {
                    Toast.makeText(getApplicationContext(), "Please input web Url",
                            Toast.LENGTH_SHORT).show();
                } else {
                    crawlingRunning = true;
                    crawler.startCrawlerTask("http://123freemovies.net/genres/action.html", true);
                    startButton.setEnabled(false);
                    crawlingInfo.setVisibility(View.VISIBLE);
                    // Send delayed message to handler for stopping crawling
                    handler.sendEmptyMessageDelayed(MSG_STOP_CRAWLING,
                            CRAWLING_RUNNING_TIME);
                }
                break;
            case R.id.stop:
                // remove any scheduled messages if user stopped crawling by
                // clicking stop button
                handler.removeMessages(MSG_STOP_CRAWLING);
                stopCrawling();
                break;
        }
    }


    private WebCrawler.CrawlingCallback mCallback = new WebCrawler.CrawlingCallback() {

        @Override
        public void onPageCrawlingCompleted() {
            crawledUrlCount++;
            progressText.post(new Runnable() {

                @Override
                public void run() {
                    progressText.setText(crawledUrlCount
                            + " pages crawled so far!!");

                }
            });
        }

        @Override
        public void onPageCrawlingFailed(String Url, int errorCode) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onCrawlingCompleted() {
            stopCrawling();
        }
    };



}


