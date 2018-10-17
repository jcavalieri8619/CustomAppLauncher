package com.bignerdranch.android.customapplauncher;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bignerdranch.android.customapplauncher.databinding.LaucherListItemBinding;
import com.bignerdranch.android.customapplauncher.databinding.FragmentLaucherBinding;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LaucherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LaucherFragment extends Fragment {

    private static final String TAG = "LaucherFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentLaucherBinding mBinding;
    private LauncherViewModel mViewModel;


    public LaucherFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LaucherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LaucherFragment newInstance(String param1, String param2) {
        LaucherFragment fragment = new LaucherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: lifecycle");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: lifecycle");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: lifecycle");

        mBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_laucher, container, false);

        mViewModel = ViewModelProviders.of(getActivity()).get(LauncherViewModel.class);


        mBinding.setIsLoading(mViewModel.isStillLoading());

        setupRecyclerView();

        mViewModel.getListResolveInfos().observe(this, new Observer<List<ResolveInfo>>() {
            @Override
            public void onChanged(@Nullable final List<ResolveInfo> resolveInfos) {
                ((LaucherAdapter) mBinding.recyclerView.getAdapter()).submitItem(resolveInfos);



            }
        });


        return mBinding.getRoot();

    }

    private void setupRecyclerView() {


        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        mBinding.recyclerView.setAdapter(new LaucherAdapter());

    }


    private AppIconClickHandler onIconClick = new AppIconClickHandler() {
        @Override
        public void onClick(final ActivityInfo activityInfo) {
            ComponentName name=new ComponentName(activityInfo.applicationInfo.packageName,
                    activityInfo.name);
            Intent i=new Intent(Intent.ACTION_MAIN);

            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);

            startActivity(i);
        }
    };


    private class LauncherViewHolder extends RecyclerView.ViewHolder {


        LaucherListItemBinding mListItemBinding;

        private String mAppLabel;
        private Drawable mAppIcon;
        private List<String> mPermList;

        LauncherViewHolder(final LaucherListItemBinding binding) {
            super(binding.getRoot());
            Log.d(TAG, "LauncherViewHolder: ");
            mListItemBinding = binding;
        }

        void bindResolveInfo(ResolveInfo resolveInfo) {
            Log.d(TAG, "bindResolveInfo: ");


            Log.d(TAG, "bindResolveInfo: binding for mAppLabel: " + mAppLabel);

            mAppLabel = resolveInfo.loadLabel(getActivity().getPackageManager()).toString();

            mAppIcon = mViewModel.getIconForApp(mAppLabel);
            mPermList = mViewModel.getPermissionsForApp(mAppLabel);

            mListItemBinding.setAppLabel(mAppLabel);
            mListItemBinding.setPermList(mPermList);

            mListItemBinding.setAppIcon(mAppIcon);

            mListItemBinding.permissionsList
                    .setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item));

            ((ArrayAdapter<String>) mListItemBinding.permissionsList.getAdapter())
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mListItemBinding.setActivityInfo(resolveInfo.activityInfo);
            mListItemBinding.setOnClickIcon(onIconClick);

        }
    }


    private class LaucherAdapter extends RecyclerView.Adapter<LauncherViewHolder> {

        List<ResolveInfo> mItems = null;

        LaucherAdapter() {
            Log.d(TAG, "LaucherAdapter: ");
        }

        /**
         * Called when RecyclerView needs a new {link ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * {link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * ee #onBindViewHolder(ViewHolder, int)
         */
        @NonNull
        @Override
        public LauncherViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            LaucherListItemBinding binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.laucher_list_item, parent, false);

            Log.d(TAG, "onCreateViewHolder: ");

            return new LauncherViewHolder(binding);
        }

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p>
         * Note that unlike {@link ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Override {link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull final LauncherViewHolder holder, final int position) {

            Log.d(TAG, "onBindViewHolder: ");
            holder.bindResolveInfo(mItems.get(position));
            holder.mListItemBinding.executePendingBindings();

        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {

            return mItems == null ? 0 : mItems.size();
        }

        public void submitItem(final List<ResolveInfo> items) {
            Log.d(TAG, "submitItem: size " + items.size());
            mItems = items;
            notifyDataSetChanged();

        }
    }


}
