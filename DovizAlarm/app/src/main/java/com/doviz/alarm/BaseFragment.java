package com.doviz.alarm;

import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by ercanpinar on 3/14/15.
 */
public class BaseFragment extends Fragment {
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
