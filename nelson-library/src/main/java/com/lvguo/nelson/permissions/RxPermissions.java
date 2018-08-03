//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.lvguo.nelson.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

public class RxPermissions {
    public static final String TAG = "RxPermissions";
    public static final Object TRIGGER = new Object();
    public RxPermissionsFragment mRxPermissionsFragment;

    public RxPermissions(@NonNull Activity activity) {
        this.mRxPermissionsFragment = this.getRxPermissionsFragment(activity);
    }

    private RxPermissionsFragment getRxPermissionsFragment(Activity activity) {
        RxPermissionsFragment rxPermissionsFragment = null;

        try {
            rxPermissionsFragment = this.findRxPermissionsFragment(activity);
            boolean isNewInstance = rxPermissionsFragment == null;
            if (isNewInstance) {
                rxPermissionsFragment = new RxPermissionsFragment();
                FragmentManager fragmentManager = activity.getFragmentManager();
                fragmentManager.beginTransaction().add(rxPermissionsFragment, "RxPermissions").commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return rxPermissionsFragment;
    }

    private RxPermissionsFragment findRxPermissionsFragment(Activity activity) {
        return (RxPermissionsFragment)activity.getFragmentManager().findFragmentByTag("RxPermissions");
    }

    public void setLogging(boolean logging) {
        this.mRxPermissionsFragment.setLogging(logging);
    }

    public <T> ObservableTransformer<T, Boolean> ensure(final String... permissions) {
        return new ObservableTransformer<T, Boolean>() {
            public ObservableSource<Boolean> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions).buffer(permissions.length).flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() {
                    public ObservableSource<Boolean> apply(List<Permission> permissionsx) throws Exception {
                        if (permissionsx.isEmpty()) {
                            return Observable.empty();
                        } else {
                            Iterator var2 = permissionsx.iterator();

                            Permission p;
                            do {
                                if (!var2.hasNext()) {
                                    return Observable.just(true);
                                }

                                p = (Permission)var2.next();
                            } while(p.granted);

                            return Observable.just(false);
                        }
                    }
                });
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEach(final String... permissions) {
        return new ObservableTransformer<T, Permission>() {
            public ObservableSource<Permission> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions);
            }
        };
    }

    public Observable<Boolean> request(String... permissions) {
        return Observable.just(TRIGGER).compose(this.ensure(permissions));
    }

    public Observable<Permission> requestEach(String... permissions) {
        return Observable.just(TRIGGER).compose(this.ensureEach(permissions));
    }

    private Observable<Permission> request(Observable<?> trigger, final String... permissions) {
        if (permissions != null && permissions.length != 0) {
            return this.oneOf(trigger, this.pending(permissions)).flatMap(new Function<Object, Observable<Permission>>() {
                public Observable<Permission> apply(Object o) throws Exception {
                    return RxPermissions.this.requestImplementation(permissions);
                }
            });
        } else {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
    }

    private Observable<?> pending(String... permissions) {
        String[] var2 = permissions;
        int var3 = permissions.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String p = var2[var4];
            if (!this.mRxPermissionsFragment.containsByPermission(p)) {
                return Observable.empty();
            }
        }

        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        return trigger == null ? Observable.just(TRIGGER) : Observable.merge(trigger, pending);
    }

    @TargetApi(23)
    private Observable<Permission> requestImplementation(String... permissions) {
        List<Observable<Permission>> list = new ArrayList(permissions.length);
        List<String> unrequestedPermissions = new ArrayList();
        String[] unrequestedPermissionsArray = permissions;
        int var5 = permissions.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String permission = unrequestedPermissionsArray[var6];
            this.mRxPermissionsFragment.log("Requesting permission " + permission);
            if (this.isGranted(permission)) {
                list.add(Observable.just(new Permission(permission, true, false)));
            } else if (this.isRevoked(permission)) {
                list.add(Observable.just(new Permission(permission, false, false)));
            } else {
                PublishSubject<Permission> subject = this.mRxPermissionsFragment.getSubjectByPermission(permission);
                if (subject == null) {
                    unrequestedPermissions.add(permission);
                    subject = PublishSubject.create();
                    this.mRxPermissionsFragment.setSubjectForPermission(permission, subject);
                }

                list.add(subject);
            }
        }

        if (!unrequestedPermissions.isEmpty()) {
            unrequestedPermissionsArray = (String[])unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]);
            this.requestPermissionsFromFragment(unrequestedPermissionsArray);
        }

        return Observable.concat(Observable.fromIterable(list));
    }

    public Observable<Boolean> shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        return !this.isMarshmallow() ? Observable.just(false) : Observable.just(this.shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(23)
    private boolean shouldShowRequestPermissionRationaleImplementation(Activity activity, String... permissions) {
        String[] var3 = permissions;
        int var4 = permissions.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String p = var3[var5];
            if (!this.isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }

        return true;
    }

    @TargetApi(23)
    void requestPermissionsFromFragment(String[] permissions) {
        this.mRxPermissionsFragment.log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        this.mRxPermissionsFragment.requestPermissions(permissions);
    }

    public boolean isGranted(String permission) {
        return !this.isMarshmallow() || this.mRxPermissionsFragment.isGranted(permission);
    }

    public boolean isRevoked(String permission) {
        return this.isMarshmallow() && this.mRxPermissionsFragment.isRevoked(permission);
    }

    boolean isMarshmallow() {
        return VERSION.SDK_INT >= 23;
    }

    void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        this.mRxPermissionsFragment.onRequestPermissionsResult(permissions, grantResults, new boolean[permissions.length]);
    }
}
