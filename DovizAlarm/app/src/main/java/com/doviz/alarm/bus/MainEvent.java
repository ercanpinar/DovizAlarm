package com.doviz.alarm.bus;

/**
 * Created by ercanpinar on 2/25/15.
 */
public class MainEvent {

    String response;

    public MainEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
