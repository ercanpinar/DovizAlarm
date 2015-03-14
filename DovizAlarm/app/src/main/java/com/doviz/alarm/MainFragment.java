package com.doviz.alarm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.doviz.alarm.response.DovizResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class MainFragment extends BaseFragment {
    String DOVIZ_URL = "http://www.doviz.gen.tr/doviz_json.asp?version=1.2";
    private RequestQueue rq;
    JsonObjectRequest jReq;
    //_______
    @InjectView(R.id.tv_dolar_alis)
    TextView mTvDolarAlis;
    @InjectView(R.id.tv_dolar_satis)
    TextView mTvDolarSatis;
    @InjectView(R.id.tv_euro_alis)
    TextView mTvEuroAlis;
    @InjectView(R.id.tv_euro_satis)
    TextView mTvEuroSatis;
    @InjectView(R.id.tv_altin_alis)
    TextView mTvAltinAlis;
    @InjectView(R.id.tv_altin_satis)
    TextView mTvAltinSatis;
    @InjectView(R.id.tv_guncelleme)
    TextView mTvGuncelleme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        rq = Volley.newRequestQueue(getActivity());
        refreshRequest();

        MyTimerTask yourTask = new MyTimerTask();
        Timer t = new Timer();
        t.scheduleAtFixedRate(yourTask, 0, 5000);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Crouton.cancelAllCroutons();
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    /**
     * OnClick
     */
    @OnClick(R.id.lr_alarm)
    public void alarmBtnClick(View view) {
//        refreshRequest();
    }

    /**
     * Request
     */
    private void refreshRequest() {
        jReq = new JsonObjectRequest(Request.Method.POST, DOVIZ_URL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<DovizResponse>() {
                            }.getType();
                            DovizResponse data = null;
                            String tempStr = response.toString();
                            try {
                                URLEncoder.encode(tempStr, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            data = gson.fromJson(tempStr, type);
                            if (data != null) {
                                mTvDolarAlis.setText(data.dolar + " TL");
                                mTvDolarSatis.setText(data.dolar2 + " TL");
                                mTvEuroAlis.setText(data.euro + " TL");
                                mTvEuroSatis.setText(data.euro2 + " TL");
                                mTvGuncelleme.setText(data.guncelleme);
                            } else {
                                Crouton.cancelAllCroutons();
                                Crouton.makeText(
                                        MainFragment.this.getActivity(), "Bir sorun oluştu.",
                                        Style.ALERT).show();
                            }
                        }
                    }
                }

                , new Response.ErrorListener()

        {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley hatasi ", error.toString());
                Crouton.cancelAllCroutons();
                Crouton.makeText(
                        MainFragment.this.getActivity(), "Bir sorun oluştu.2",
                        Style.ALERT).show();
            }
        }
        );
        rq.add(jReq);
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            refreshRequest();
        }
    }
}
