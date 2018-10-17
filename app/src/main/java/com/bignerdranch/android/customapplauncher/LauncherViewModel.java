package com.bignerdranch.android.customapplauncher;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LauncherViewModel extends AndroidViewModel {

    private static final String TAG = "LauncherViewModel";
    private List<ResolveInfo> mResolveInfos = new ArrayList<>();

    MutableLiveData<List<ResolveInfo>> ObsResInfoList = new MutableLiveData<>();


    private ObservableBoolean mIsLoading = new ObservableBoolean(true);


    private Map<String, List<String>> mAppPermissionsMap = new HashMap<>();

    private Map<String, Drawable> mIconForApp = new HashMap<>();

    public LauncherViewModel(final Application app) {
        super(app);


        Completable.fromAction(() -> LoadLaucherData(app))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(final Disposable d) {

                    }

                    @Override
                    public void onComplete() {

                        Log.d(TAG, "onComplete: JPC finished loading viewmodel data");

                        ObsResInfoList.setValue(mResolveInfos);
                        mIsLoading.set(false);

                    }

                    @Override
                    public void onError(final Throwable e) {

                        Log.d(TAG, "onError: JPC error loading viewmodel data", e);
                    }
                });





    }

    public ObservableBoolean isStillLoading() {
        return mIsLoading;
    }

    private void LoadLaucherData(final Application app) {
        Intent intent = new Intent(Intent.ACTION_MAIN);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);


        mResolveInfos = app.getPackageManager().queryIntentActivities(intent, 0);


        Collections.sort(mResolveInfos, new Comparator<ResolveInfo>() {
            @Override
            public int compare(final ResolveInfo o1, final ResolveInfo o2) {
                return String.CASE_INSENSITIVE_ORDER
                        .compare(o1.loadLabel(app.getPackageManager()).toString(),

                                o2.loadLabel(app.getPackageManager()).toString());
            }

        });

        Log.d(TAG, "LauncherViewModel: JPC found activities: " + mResolveInfos.size());


        for (ResolveInfo resolveInfo :
                mResolveInfos) {

            Log.d(TAG, "LoadLaucherData: JPC activityInfo");

            ActivityInfo activity_info = resolveInfo.activityInfo;

            String PackageName = activity_info.packageName;
            Log.d(TAG, "LauncherViewModel: JPC packageName: " + PackageName);

            String labelName = activity_info.loadLabel(app.getPackageManager()).toString();

            Log.d(TAG, "LauncherViewModel: JPC labelName: " + labelName);

            mAppPermissionsMap.put(labelName, permissionsListForPackage(PackageName));

            Drawable icon=null;
            try {

//                icon = resolveInfo.loadIcon(app.getPackageManager());

                icon = app.getPackageManager().getApplicationIcon(PackageName);

            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "LoadLaucherData: couldn't find package to load icon", e);
            }


            mIconForApp.put(labelName,icon );


        }
        Log.d(TAG, "LoadLaucherData: JPC finished resolveInfo iteration");

    }

    private List<String> permissionsListForPackage(String packageName) {


        Log.d(TAG, "permissionsListForPackage: JPC getting permissions for package");

        PackageInfo packageInfo;
        try {
            packageInfo = getApplication()
                    .getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);






        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "permissionsListForPackage: JPC unable to get permissions for package: " + packageName, e);

            return  null;
        }

        if (packageInfo.requestedPermissions == null) {
            return null;
        }

        List<String> permissionList = new ArrayList<>();

        for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
            if (packageInfo.requestedPermissionsFlags != null &&
                    (packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {


                String perm = packageInfo.requestedPermissions[i];

                Log.d(TAG, "permissionsListForPackage: JPC permission :" + perm);

                // extracts just permission name
                perm = perm.substring(perm.lastIndexOf('.') + 1);

                permissionList.add(perm);

            }
        }

        return permissionList;


    }

    public LiveData<List<ResolveInfo>> getListResolveInfos() {
        return ObsResInfoList;
    }

    public List<String> getPermissionsForApp(String appName) {
        List<String> permissions = mAppPermissionsMap.get(appName);
        if (permissions == null) {
            return Collections.singletonList("No Permissions");
        }
        return permissions;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public Drawable getIconForApp(final String appLabel) {
        return mIconForApp.get(appLabel);
    }
}
