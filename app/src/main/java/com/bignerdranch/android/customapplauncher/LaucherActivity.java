package com.bignerdranch.android.customapplauncher;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LaucherActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        String param1 = "";
        String param2 = "";
        return LaucherFragment.newInstance(param1, param2);
    }

}
