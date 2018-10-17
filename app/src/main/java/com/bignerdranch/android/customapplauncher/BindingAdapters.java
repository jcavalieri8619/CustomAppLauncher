package com.bignerdranch.android.customapplauncher;

import android.content.pm.ResolveInfo;
import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

public class BindingAdapters {

    @BindingAdapter("showHide")
    public static void showHide(View v, boolean flag) {

        v.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("submitAdapterItems")
    public static void submitItems(Spinner v, List<String> items) {
        ((ArrayAdapter<String>) v.getAdapter()).addAll(items);
    }
}
