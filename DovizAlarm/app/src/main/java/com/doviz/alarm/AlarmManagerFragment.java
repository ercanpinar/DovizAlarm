package com.doviz.alarm;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gc.materialdesign.views.LayoutRipple;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by ercanpinar on 3/28/15.
 */
public class AlarmManagerFragment extends BaseFragment {

    @InjectView(R.id.tv_dolar_alis_alarm_degeri_al)
    TextView tvDolarAlisAlarmDegeri;
    @InjectView(R.id.tv_dolar_satis_alarm_degeri_al)
    TextView tvDolarSatisAlarmDegeri;
    @InjectView(R.id.tv_euro_alis_alarm_degeri_al)
    TextView tvEuroAlisAlarmDegeri;
    @InjectView(R.id.tv_euro_satis_alarm_degeri_al)
    TextView tvEuroSatisAlarmDegeri;
    @InjectView(R.id.rd_dovizcom)
    RadioButton mRdDovizComKaynak1;
    @InjectView(R.id.rd_dovizgentr)
    RadioButton mRdDovizGenTrKaynak2;
    SharedPref shrpT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_manager, container, false);
        ButterKnife.inject(this, rootView);
        shrpT = new SharedPref(getActivity());
        tvDolarAlisAlarmDegeri.setText(shrpT.getAlarmValue(shrpT.DOLAR_ALIS) + " TL");
        tvDolarSatisAlarmDegeri.setText(shrpT.getAlarmValue(shrpT.DOLAR_SATIS) + " TL");
        tvEuroAlisAlarmDegeri.setText(shrpT.getAlarmValue(shrpT.EURO_ALIS) + " TL");
        tvEuroSatisAlarmDegeri.setText(shrpT.getAlarmValue(shrpT.EURO_SATIS) + " TL");
        mRdDovizComKaynak1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRdDovizComKaynak1.setChecked(true);
                mRdDovizGenTrKaynak2.setChecked(false);
                shrpT.updateKaynak("1");
            }
        });
        mRdDovizGenTrKaynak2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRdDovizComKaynak1.setChecked(false);
                mRdDovizGenTrKaynak2.setChecked(true);
                shrpT.updateKaynak("2");
            }
        });
        setRadioButton();
        return rootView;
    }

    private void setRadioButton() {
        if (shrpT.getKaynak().equals("1")) {
            mRdDovizComKaynak1.setChecked(true);
            mRdDovizGenTrKaynak2.setChecked(false);
        } else {
            mRdDovizComKaynak1.setChecked(false);
            mRdDovizGenTrKaynak2.setChecked(true);
        }
    }

    /**
     * OnClick
     */
    @OnClick(R.id.lr_alarm_al)
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
                } else {
                    serviceStop();
                }

                tvDolarAlisAlarmDegeri.setText(shrp.getAlarmValue(shrp.DOLAR_ALIS) + " TL");
                tvDolarSatisAlarmDegeri.setText(shrp.getAlarmValue(shrp.DOLAR_SATIS) + " TL");
                tvEuroAlisAlarmDegeri.setText(shrp.getAlarmValue(shrp.EURO_ALIS) + " TL");
                tvEuroSatisAlarmDegeri.setText(shrp.getAlarmValue(shrp.EURO_SATIS) + " TL");

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

    private void serviceStart() {
        if (!servisCalisiyorMu()) {
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    AlarmManagerFragment.this.getActivity(), "Alarm aktif edildi.",
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

    public void serviceStop() {
        if (servisCalisiyorMu()) {
            getActivity().stopService(new Intent(getActivity(), DovizAlarmService.class));
            Crouton.cancelAllCroutons();
            Crouton.makeText(
                    AlarmManagerFragment.this.getActivity(), "Alarm durduruldu.",
                    Style.INFO).show();
        }
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
}
