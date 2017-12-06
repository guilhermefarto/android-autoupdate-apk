package autoupdateapk.android.guilherme.cleva.farto.autoupdateapk.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public abstract class ActivityUtil {

    public static String getVersionName(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    }

}
