package com.doviz.alarm;

import android.os.AsyncTask;

import com.doviz.alarm.bus.MainEvent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.greenrobot.event.EventBus;

/**
 * Created by ercanpinar on 3/28/15.
 */
public class ParseUrlAsyncTask extends AsyncTask<String, Void, String> {
    String dolarAl, dolarSat;
    String euroAl, euroSat;
    //    String altinAl, altinSat;

    @Override
    protected String doInBackground(String... strings) {
        StringBuffer buffer = new StringBuffer();
        try {
            Document doc = Jsoup.connect(strings[0]).get();
            /**
             * Dolar-Euro
             * */
            Elements divs = doc.select("div");

            for (Element div : divs) {
                if (div.attr("class").equals("cevirici-select")) {
                    Elements lis = div.select("ul").select("li").select("ul");
                    for (Element li : lis) {

                        if (li.attr("data-code").equals("USD")) {
                            dolarAl = li.attr("data-buying");
                            buffer.append(dolarAl + ":");

                            dolarSat = li.attr("data-selling");
                            buffer.append(dolarSat + ":");
                        } else if (li.attr("data-code").equals("EUR")) {
                            euroAl = li.attr("data-buying");
                            buffer.append(euroAl + ":");

                            euroSat = li.attr("data-selling");
                            buffer.append(euroSat);
                        }
                    }
                }
            }
//            Document docAltn = Jsoup.connect("http://altin.doviz.com/gram-altin").get();
            /**
             * Altın
             * */
//            Elements divAs = docAltn.select("div");
//            for (Element div : divAs) {
//                Log.i("**altin div ***", div.text());
//                if (div.attr("class").equals("doviz-column btgold")) {
//                    Elements lis = div.select("ul").select("li");
//
//                    for (Element li : lis) {
//                        Log.i("**altin ***", li.select("div").text());
//                        if (li.select("div").select("h1").text().equals("Gram Altın")) {
//                            Elements dvs = li.select("div");
//                            altinAl = dvs.get(4).text();
//                            buffer.append(altinAl + ":");
//                            Log.i("**altin al***", altinAl);
//
//                            altinSat = dvs.get(5).text();
//                            buffer.append(altinSat + "-");
//                            Log.i("**altin sat***", altinSat);
//                        }
//                    }
//                }
//            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return buffer.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        EventBus.getDefault().post(new MainEvent(s));
    }
}
