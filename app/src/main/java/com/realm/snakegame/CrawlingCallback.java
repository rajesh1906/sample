package com.realm.snakegame;

/**
 * Created by Rajesh Kumar on 23-09-2017.
 */

public interface CrawlingCallback {

    void onPageCrawlingCompleted();

    void onPageCrawlingFailed(String Url, int errorCode);

    void onCrawlingCompleted();
}
