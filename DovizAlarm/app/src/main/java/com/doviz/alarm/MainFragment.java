package com.doviz.alarm;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.doviz.alarm.bus.MainEvent;
import com.doviz.alarm.response.DovizResponse;
import com.gc.materialdesign.views.LayoutRipple;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
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
    @InjectView(R.id.chck_alarm)
    CheckBox mChckAlarm;
    @InjectView(R.id.chck_alarm_time_five_min)
    CheckBox mChckAlarm5dk;
    @InjectView(R.id.chck_alarm_time_fifty_min)
    CheckBox mChckAlarm15dk;
    @InjectView(R.id.chck_alarm_time_thirty_min)
    CheckBox mChckAlarm30dk;
    @InjectView(R.id.lr_guncelle_button)
    LayoutRipple mLrGuncelle;
    @InjectView(R.id.sp_alarm_time)
    Spinner mSpAlarmMinuteList;
    @InjectView(R.id.rl_loading)
    RelativeLayout mRlLoading;

    MyTimerTask yourTask;
    Timer t;
    SharedPref shrpT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);
        EventBus.getDefault().register(this);
        rq = Volley.newRequestQueue(getActivity());
        shrpT = new SharedPref(getActivity());

        if (shrpT.getKaynak().equals("1"))
            new ParseUrlAsyncTask().execute(new String[]{"http://kur.doviz.com/serbest-piyasa/"});
        else
            refreshRequest();
        yourTask = new MyTimerTask();
        t = new Timer();
        t.scheduleAtFixedRate(yourTask, 0, 10000);

        if (servisCalisiyorMu())
            mChckAlarm.setChecked(true);
        else
            mChckAlarm.setChecked(false);

        mChckAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serviceStart();
                } else {
                    serviceStop();
                }
            }
        });
        createSpinner();

        /**
         * Analytic
         * */

        GaUtil.init(getActivity());
        GaUtil.sendView("Döviz Kurları");
        return rootView;
    }

    private void createSpinner() {
        List<String> list = new ArrayList<>();
        String dk = " dk.";
        for (int i = 1; i < 61; i++) {
            list.add(i + dk);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpAlarmMinuteList.setAdapter(dataAdapter);
        mSpAlarmMinuteList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                shrpT.updateAlarmTime(String.valueOf((pos + 1) * 60000));
                DovizAlarmService.LOOP_TIME = (pos + 1) * 60000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        lastSelectSpinner();
    }

    private void lastSelectSpinner() {
        mSpAlarmMinuteList.setSelection(((Integer.parseInt(String.valueOf(shrpT.getAlarmTime())) / 60000) - 1));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        t.cancel();
        Crouton.cancelAllCroutons();
        EventBus.getDefault().unregister(this);
        ButterKnife.reset(this);
        this.rq.cancelAll(this);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Analytic
         * */
        GaUtil.sendView("Döviz Kurları");
    }

    /**
     * Eventbus
     */
    public void onEvent(MainEvent event) {
        mRlLoading.setVisibility(View.GONE);

        String[] deger = event.getResponse().split(":");
        if (deger.length == 4) {
            mTvDolarAlis.setText(addZero(deger[0]) + " TL");
            mTvDolarSatis.setText(addZero(deger[1]) + " TL");
            mTvEuroAlis.setText(addZero(deger[2]) + " TL");
            mTvEuroSatis.setText(addZero(deger[3]) + " TL");
        } else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Bir sorun oluştu.",
                    Style.ALERT).show();
        }
    }

    /**
     * OnClick
     */
    @OnClick(R.id.lr_guncelle_button)
    public void guncelleBtnClick(View view) {
        mRlLoading.setVisibility(View.VISIBLE);
        Crouton.cancelAllCroutons();

        if (shrpT.getKaynak().equals("1"))
            new ParseUrlAsyncTask().execute(new String[]{"http://kur.doviz.com/serbest-piyasa/"});
        else
            refreshRequest();
    }

    @OnClick(R.id.lr_alarm)
    public void alarmBtnClick(View view) {
        alarmDialog();
    }

    private void alarmDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_alarm);
        final EditText dolarAlisAlrm = (EditText) dialog.findViewById(R.id.edt_dolar_alis_alarm);
        final EditText dolarSatisAlrm = (EditText) dialog.findViewById(R.id.edt_dolar_satis_alarm);
        final EditText euroAlisAlrm = (EditText) dialog.findViewById(R.id.edt_euro_alis_alarm);
        final EditText euroSatisAlrm = (EditText) dialog.findViewById(R.id.edt_euro_satis_alarm);
        LayoutRipple dialogButton = (LayoutRipple) dialog.findViewById(R.id.lr_dialogButton);
        SharedPref shrpT = new SharedPref(getActivity());

        dolarAlisAlrm.setText(shrpT.getAlarmValue(shrpT.DOLAR_ALIS));
        dolarSatisAlrm.setText(shrpT.getAlarmValue(shrpT.DOLAR_SATIS));
        euroAlisAlrm.setText(shrpT.getAlarmValue(shrpT.EURO_ALIS));
        euroSatisAlrm.setText(shrpT.getAlarmValue(shrpT.EURO_SATIS));

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPref shrp = new SharedPref(getActivity());
                boolean serviceActv = false;
                if (stringIsNotEmptyAndZero(dolarAlisAlrm)) {
                    shrp.updateAlarm(shrp.DOLAR_ALIS, addZero(dolarAlisAlrm.getText().toString()));
                    if (!addZero(dolarAlisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(dolarSatisAlrm)) {
                    shrp.updateAlarm(shrp.DOLAR_SATIS, addZero(dolarSatisAlrm.getText().toString()));
                    if (!addZero(dolarSatisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(euroAlisAlrm)) {
                    shrp.updateAlarm(shrp.EURO_ALIS, addZero(euroAlisAlrm.getText().toString()));
                    if (!addZero(euroAlisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }
                if (stringIsNotEmptyAndZero(euroSatisAlrm)) {
                    shrp.updateAlarm(shrp.EURO_SATIS, addZero(euroSatisAlrm.getText().toString()));
                    if (!addZero(euroSatisAlrm.getText().toString()).equals("0.0000"))
                        serviceActv = true;
                }

                if (serviceActv) {
                    serviceStart();
                    mChckAlarm.setChecked(true);
                } else {
                    mChckAlarm.setChecked(false);
                    serviceStop();
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean stringIsNotEmptyAndZero(EditText editText) {

        if (!editText.getText().toString().trim().equals("") && !editText.getText().toString().trim().equals("0"))
            return true;
        else
            return false;
    }

    private String addZero(String str) {

        String tmp = str.replace(".", "");
        int leng = tmp.length();

        if (leng < 5) {
            leng = 5 - leng;
            for (int i = 0; i < leng; i++) {
                tmp = tmp + "0";
            }
        } else {
            tmp = tmp.substring(0, 5);
        }
        int len = tmp.length();
        Character[] array = new Character[len];
        for (int i = 0; i < len; i++) {
            array[i] = new Character(tmp.charAt(i));
        }

        String ret = array[0].toString() + ".";
        for (int i = 1; i < array.length; i++) {
            ret = ret + array[i].toString();
        }

        return ret;
    }

    /**
     * Request
     */
    public void refreshRequest() {
        if (((MainActivity) getActivity()).internetConnectionCheck()) {
            jReq = new JsonObjectRequest(DOVIZ_URL, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            if (response != null) {
                                mRlLoading.setVisibility(View.GONE);

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
                            MainFragment.this.getActivity(), "Bir hata oluştu.",
                            Style.ALERT).show();
                    mRlLoading.setVisibility(View.GONE);
                }
            }
            );
            rq.add(jReq);
        } else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "İnternet bağlantınızı kontrol ediniz.",
                    Style.ALERT).show();
            mRlLoading.setVisibility(View.GONE);
        }
    }

    private class MyTimerTask extends TimerTask {
        public void run() {
            if (shrpT.getKaynak().equals("1"))
                new ParseUrlAsyncTask().execute(new String[]{"http://kur.doviz.com/serbest-piyasa/"});
            else
                refreshRequest();
        }
    }

    private void serviceStart() {
        if (!servisCalisiyorMu()) {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm aktif edildi.",
                    Style.INFO).show();
            getActivity().startService(new Intent(getActivity(), DovizAlarmService.class));
        }
    }

    private boolean servisCalisiyorMu() {
        ActivityManager servisYoneticisi = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo servis : servisYoneticisi.getRunningServices(Integer.MAX_VALUE)) {
            if (getActivity().getPackageName().equals(servis.service.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Service start-stop
     */
    public void serviceStartStop() {
        if (servisCalisiyorMu()) {
            getActivity().stopService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm durduruldu.",
                    Style.INFO).show();
        } else

        {
            getActivity().startService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm aktif edildi.",
                    Style.INFO).show();
        }
    }

    public void serviceStop() {
        if (servisCalisiyorMu()) {
            getActivity().stopService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    MainFragment.this.getActivity(), "Alarm durduruldu.",
                    Style.INFO).show();
        }
    }
}
