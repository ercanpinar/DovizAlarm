package com.doviz.alarm;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ercanpinar on 4/8/15.
 */
public class GaUtil {

    private static final String PROPERTY_ID = "UA-61674656-1";
    private static Tracker tracker;

    public static void init(Context context) {
        tracker = getTracker(context);
    }

    synchronized static Tracker getTracker(Context context) {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            //   analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            // When dry run is set, hits will not be dispatched, but will still be logged as though they were dispatched.
            //  analytics.setDryRun(true);
            tracker = analytics.newTracker(PROPERTY_ID);
        }
        return tracker;
    }

    public static synchronized void sendEvent(String category, String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    /*   public static synchronized void sendEventAdmob(String action,String value) {
           tracker.send(new HitBuilders.EventBuilder()
                   .setCategory("Show ad")
                   .setAction(action)
                   .set("ad action",value)
                   .build());

       }*/
    public static synchronized void sendEventFacebook(String label, String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Facebook Ad")
                .setLabel(label)
                .setAction(action)
                .build());
    }

    public static void sendView(String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }
}