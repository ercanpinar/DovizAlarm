package com.doviz.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class MainFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        Crouton.cancelAllCroutons();
        super.onDestroyView();
    }
}
