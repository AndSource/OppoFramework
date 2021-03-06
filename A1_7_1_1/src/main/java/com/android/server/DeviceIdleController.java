package com.android.server;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.INetworkPolicyManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IDeviceIdleController.Stub;
import android.os.IMaintenanceActivityListener;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.format.Formatter;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.MutableLong;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.Display;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.AtomicFile;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
import com.android.server.oppo.IElsaManager;
import com.mediatek.datashaping.IDataShapingManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DeviceIdleController extends SystemService implements DeviceIdleCallback {
    private static final boolean COMPRESS_TIME = false;
    private static final String CONFIG_AUTO_POWER = "persist.config.AutoPowerModes";
    private static boolean DEBUG = false;
    private static boolean DEBUG_OPPO = false;
    static final long DURATION_TRAFFIC_CHECK = 300000;
    private static final int ENABLE_DEEP_DOZE = 2;
    private static final int ENABLE_LIGHT_AND_DEEP_DOZE = 3;
    private static final int ENABLE_LIGHT_DOZE = 1;
    private static final int EVENT_BUFFER_SIZE = 100;
    private static final int EVENT_DEEP_IDLE = 4;
    private static final int EVENT_DEEP_MAINTENANCE = 5;
    private static final int EVENT_LIGHT_IDLE = 2;
    private static final int EVENT_LIGHT_MAINTENANCE = 3;
    private static final int EVENT_NORMAL = 1;
    private static final int EVENT_NULL = 0;
    private static final int LIGHT_STATE_ACTIVE = 0;
    private static final int LIGHT_STATE_IDLE = 4;
    private static final int LIGHT_STATE_IDLE_MAINTENANCE = 6;
    private static final int LIGHT_STATE_INACTIVE = 1;
    private static final int LIGHT_STATE_OVERRIDE = 7;
    private static final int LIGHT_STATE_PRE_IDLE = 3;
    private static final int LIGHT_STATE_WAITING_FOR_NETWORK = 5;
    static final int MSG_FINISH_IDLE_OP = 8;
    static final int MSG_REPORT_ACTIVE = 5;
    static final int MSG_REPORT_IDLE_OFF = 4;
    static final int MSG_REPORT_IDLE_ON = 2;
    static final int MSG_REPORT_IDLE_ON_LIGHT = 3;
    static final int MSG_REPORT_MAINTENANCE_ACTIVITY = 7;
    static final int MSG_TEMP_APP_WHITELIST_TIMEOUT = 6;
    static final int MSG_WRITE_CONFIG = 1;
    private static final int STATE_ACTIVE = 0;
    private static final int STATE_IDLE = 5;
    private static final int STATE_IDLE_MAINTENANCE = 6;
    private static final int STATE_IDLE_PENDING = 2;
    private static final int STATE_INACTIVE = 1;
    private static final int STATE_LOCATING = 4;
    private static final int STATE_SENSING = 3;
    private static final String TAG = "DeviceIdleController";
    static final long THRESH_SPEED = 10;
    private int mActiveIdleOpCount;
    private WakeLock mActiveIdleWakeLock;
    private AlarmManager mAlarmManager;
    private boolean mAlarmsActive;
    private AnyMotionDetector mAnyMotionDetector;
    private IBatteryStats mBatteryStats;
    BinderService mBinderService;
    private boolean mCharging;
    public final AtomicFile mConfigFile;
    private ConnectivityService mConnectivityService;
    private Constants mConstants;
    private Display mCurDisplay;
    private long mCurIdleBudget;
    private IDataShapingManager mDataShapingManager;
    private final OnAlarmListener mDeepAlarmListener;
    private boolean mDeepEnabled;
    private boolean mDeepIdleAccordingToRus;
    ColorOSDeviceIdleHelper mDeviceIdleHelper;
    private final DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private final int[] mEventCmds;
    private final long[] mEventTimes;
    private boolean mForceIdle;
    private final LocationListener mGenericLocationListener;
    private final LocationListener mGpsLocationListener;
    final MyHandler mHandler;
    private boolean mHasGps;
    private boolean mHasNetworkLocation;
    private Intent mIdleIntent;
    private final BroadcastReceiver mIdleStartedDoneReceiver;
    private long mInactiveTimeout;
    private boolean mJobsActive;
    TrafficRecord mLastDeepTrafficRecord;
    private Location mLastGenericLocation;
    private Location mLastGpsLocation;
    TrafficRecord mLastLightTrafficRecord;
    private final OnAlarmListener mLightAlarmListener;
    private boolean mLightEnabled;
    private boolean mLightIdleAccordingToRus;
    private Intent mLightIdleIntent;
    private int mLightState;
    private com.android.server.AlarmManagerService.LocalService mLocalAlarmManager;
    private PowerManagerInternal mLocalPowerManager;
    private boolean mLocated;
    private boolean mLocating;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private final RemoteCallbackList<IMaintenanceActivityListener> mMaintenanceActivityListeners;
    private long mMaintenanceStartTime;
    private final MotionListener mMotionListener;
    private Sensor mMotionSensor;
    private boolean mNetworkConnected;
    private INetworkPolicyManager mNetworkPolicyManager;
    Runnable mNetworkPolicyTempWhitelistCallback;
    private long mNextAlarmTime;
    private long mNextIdleDelay;
    private long mNextIdlePendingDelay;
    private long mNextLightAlarmTime;
    private long mNextLightIdleDelay;
    private long mNextSensingTimeoutAlarmTime;
    private boolean mNotMoving;
    private PowerManager mPowerManager;
    private int[] mPowerSaveWhitelistAllAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistAllAppIds;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistApps;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistAppsExceptIdle;
    private int[] mPowerSaveWhitelistExceptIdleAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistSystemAppIdsExceptIdle;
    private int[] mPowerSaveWhitelistUserAppIdArray;
    private final SparseBooleanArray mPowerSaveWhitelistUserAppIds;
    private final ArrayMap<String, Integer> mPowerSaveWhitelistUserApps;
    private final BroadcastReceiver mReceiver;
    private boolean mReportedMaintenanceActivity;
    private boolean mScreenOn;
    private final OnAlarmListener mSensingTimeoutAlarmListener;
    private SensorManager mSensorManager;
    private int mState;
    private int[] mTempWhitelistAppIdArray;
    private final SparseArray<Pair<MutableLong, String>> mTempWhitelistAppIdEndTimes;

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(DeviceIdleController this$0, BinderService binderService) {
            this();
        }

        private BinderService() {
        }

        public void addPowerSaveWhitelistApp(String name) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.addPowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void removePowerSaveWhitelistApp(String name) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.removePowerSaveWhitelistAppInternal(name);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public String[] getSystemPowerWhitelistExceptIdle() {
            return DeviceIdleController.this.getSystemPowerWhitelistExceptIdleInternal();
        }

        public String[] getSystemPowerWhitelist() {
            return DeviceIdleController.this.getSystemPowerWhitelistInternal();
        }

        public String[] getUserPowerWhitelist() {
            return DeviceIdleController.this.getUserPowerWhitelistInternal();
        }

        public String[] getFullPowerWhitelistExceptIdle() {
            return DeviceIdleController.this.getFullPowerWhitelistExceptIdleInternal();
        }

        public String[] getFullPowerWhitelist() {
            return DeviceIdleController.this.getFullPowerWhitelistInternal();
        }

        public int[] getAppIdWhitelistExceptIdle() {
            return DeviceIdleController.this.getAppIdWhitelistExceptIdleInternal();
        }

        public int[] getAppIdWhitelist() {
            return DeviceIdleController.this.getAppIdWhitelistInternal();
        }

        public int[] getAppIdUserWhitelist() {
            return DeviceIdleController.this.getAppIdUserWhitelistInternal();
        }

        public int[] getAppIdTempWhitelist() {
            return DeviceIdleController.this.getAppIdTempWhitelistInternal();
        }

        public boolean isPowerSaveWhitelistExceptIdleApp(String name) {
            return DeviceIdleController.this.isPowerSaveWhitelistExceptIdleAppInternal(name);
        }

        public boolean isPowerSaveWhitelistApp(String name) {
            return DeviceIdleController.this.isPowerSaveWhitelistAppInternal(name);
        }

        public void addPowerSaveTempWhitelistApp(String packageName, long duration, int userId, String reason) throws RemoteException {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
        }

        public long addPowerSaveTempWhitelistAppForMms(String packageName, int userId, String reason) throws RemoteException {
            long duration = DeviceIdleController.this.mConstants.MMS_TEMP_APP_WHITELIST_DURATION;
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
            return duration;
        }

        public long addPowerSaveTempWhitelistAppForSms(String packageName, int userId, String reason) throws RemoteException {
            long duration = DeviceIdleController.this.mConstants.SMS_TEMP_APP_WHITELIST_DURATION;
            DeviceIdleController.this.addPowerSaveTempWhitelistAppChecked(packageName, duration, userId, reason);
            return duration;
        }

        public void exitIdle(String reason) {
            DeviceIdleController.this.getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                DeviceIdleController.this.exitIdleInternal(reason);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) {
            return DeviceIdleController.this.registerMaintenanceActivityListener(listener);
        }

        public void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) {
            DeviceIdleController.this.unregisterMaintenanceActivityListener(listener);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            DeviceIdleController.this.dump(fd, pw, args);
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
            new Shell().exec(this, in, out, err, args, resultReceiver);
        }
    }

    private final class Constants extends ContentObserver {
        private static final String KEY_IDLE_AFTER_INACTIVE_TIMEOUT = "idle_after_inactive_to";
        private static final String KEY_IDLE_FACTOR = "idle_factor";
        private static final String KEY_IDLE_PENDING_FACTOR = "idle_pending_factor";
        private static final String KEY_IDLE_PENDING_TIMEOUT = "idle_pending_to";
        private static final String KEY_IDLE_TIMEOUT = "idle_to";
        private static final String KEY_INACTIVE_TIMEOUT = "inactive_to";
        private static final String KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = "light_after_inactive_to";
        private static final String KEY_LIGHT_IDLE_FACTOR = "light_idle_factor";
        private static final String KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = "light_idle_maintenance_max_budget";
        private static final String KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = "light_idle_maintenance_min_budget";
        private static final String KEY_LIGHT_IDLE_TIMEOUT = "light_idle_to";
        private static final String KEY_LIGHT_MAX_IDLE_TIMEOUT = "light_max_idle_to";
        private static final String KEY_LIGHT_PRE_IDLE_TIMEOUT = "light_pre_idle_to";
        private static final String KEY_LOCATING_TIMEOUT = "locating_to";
        private static final String KEY_LOCATION_ACCURACY = "location_accuracy";
        private static final String KEY_MAX_IDLE_PENDING_TIMEOUT = "max_idle_pending_to";
        private static final String KEY_MAX_IDLE_TIMEOUT = "max_idle_to";
        private static final String KEY_MAX_TEMP_APP_WHITELIST_DURATION = "max_temp_app_whitelist_duration";
        private static final String KEY_MIN_DEEP_MAINTENANCE_TIME = "min_deep_maintenance_time";
        private static final String KEY_MIN_LIGHT_MAINTENANCE_TIME = "min_light_maintenance_time";
        private static final String KEY_MIN_TIME_TO_ALARM = "min_time_to_alarm";
        private static final String KEY_MMS_TEMP_APP_WHITELIST_DURATION = "mms_temp_app_whitelist_duration";
        private static final String KEY_MOTION_INACTIVE_TIMEOUT = "motion_inactive_to";
        private static final String KEY_NOTIFICATION_WHITELIST_DURATION = "notification_whitelist_duration";
        private static final String KEY_SENSING_TIMEOUT = "sensing_to";
        private static final String KEY_SMS_TEMP_APP_WHITELIST_DURATION = "sms_temp_app_whitelist_duration";
        private static final long MIN_TIMEOUT = 300000;
        public long IDLE_AFTER_INACTIVE_TIMEOUT;
        public float IDLE_FACTOR;
        public float IDLE_PENDING_FACTOR;
        public long IDLE_PENDING_TIMEOUT;
        public long IDLE_TIMEOUT;
        public long INACTIVE_TIMEOUT;
        public long LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT;
        public float LIGHT_IDLE_FACTOR;
        public long LIGHT_IDLE_MAINTENANCE_MAX_BUDGET;
        public long LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
        public long LIGHT_IDLE_TIMEOUT;
        public long LIGHT_MAX_IDLE_TIMEOUT;
        public long LIGHT_PRE_IDLE_TIMEOUT;
        public long LOCATING_TIMEOUT;
        public float LOCATION_ACCURACY;
        public long MAX_IDLE_PENDING_TIMEOUT;
        public long MAX_IDLE_TIMEOUT;
        public long MAX_TEMP_APP_WHITELIST_DURATION;
        public long MIN_DEEP_MAINTENANCE_TIME;
        public long MIN_LIGHT_MAINTENANCE_TIME;
        public long MIN_TIME_TO_ALARM;
        public long MMS_TEMP_APP_WHITELIST_DURATION;
        public long MOTION_INACTIVE_TIMEOUT;
        public long NOTIFICATION_WHITELIST_DURATION;
        public long SENSING_TIMEOUT;
        public long SMS_TEMP_APP_WHITELIST_DURATION;
        private final boolean mHasWatch;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private final ContentResolver mResolver;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void, dex:  in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        void dump(java.io.PrintWriter r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void, dex:  in method: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.DeviceIdleController.Constants.dump(java.io.PrintWriter):void");
        }

        public Constants(Handler handler, ContentResolver resolver) {
            String str;
            super(handler);
            this.mResolver = resolver;
            this.mHasWatch = DeviceIdleController.this.getContext().getPackageManager().hasSystemFeature("android.hardware.type.watch");
            ContentResolver contentResolver = this.mResolver;
            if (this.mHasWatch) {
                str = "device_idle_constants_watch";
            } else {
                str = "device_idle_constants";
            }
            contentResolver.registerContentObserver(Global.getUriFor(str), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (DeviceIdleController.this) {
                try {
                    String str;
                    KeyValueListParser keyValueListParser = this.mParser;
                    ContentResolver contentResolver = this.mResolver;
                    if (this.mHasWatch) {
                        str = "device_idle_constants_watch";
                    } else {
                        str = "device_idle_constants";
                    }
                    keyValueListParser.setString(Global.getString(contentResolver, str));
                } catch (IllegalArgumentException e) {
                    Slog.e(DeviceIdleController.TAG, "Bad device idle settings", e);
                }
                this.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT, 300000);
                this.LIGHT_PRE_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_PRE_IDLE_TIMEOUT, LocationFudger.FASTEST_INTERVAL_MS);
                this.LIGHT_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_IDLE_TIMEOUT, 300000);
                this.LIGHT_IDLE_FACTOR = this.mParser.getFloat(KEY_LIGHT_IDLE_FACTOR, 2.0f);
                this.LIGHT_MAX_IDLE_TIMEOUT = this.mParser.getLong(KEY_LIGHT_MAX_IDLE_TIMEOUT, 900000);
                this.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET = this.mParser.getLong(KEY_LIGHT_IDLE_MAINTENANCE_MIN_BUDGET, 60000);
                this.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET = this.mParser.getLong(KEY_LIGHT_IDLE_MAINTENANCE_MAX_BUDGET, 300000);
                this.MIN_LIGHT_MAINTENANCE_TIME = this.mParser.getLong(KEY_MIN_LIGHT_MAINTENANCE_TIME, 5000);
                this.MIN_DEEP_MAINTENANCE_TIME = this.mParser.getLong(KEY_MIN_DEEP_MAINTENANCE_TIME, 30000);
                long totalInterval2Idle = ColorOSDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE;
                if (DeviceIdleController.this.mDeviceIdleHelper != null) {
                    totalInterval2Idle = DeviceIdleController.this.mDeviceIdleHelper.getTotalIntervalToIdle();
                }
                this.INACTIVE_TIMEOUT = (totalInterval2Idle - (this.SENSING_TIMEOUT + this.LOCATING_TIMEOUT)) / 2;
                if (this.INACTIVE_TIMEOUT < 300000) {
                    this.INACTIVE_TIMEOUT = 300000;
                }
                this.SENSING_TIMEOUT = JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
                this.LOCATING_TIMEOUT = 30000;
                this.LOCATION_ACCURACY = this.mParser.getFloat(KEY_LOCATION_ACCURACY, 20.0f);
                this.MOTION_INACTIVE_TIMEOUT = this.INACTIVE_TIMEOUT;
                this.IDLE_AFTER_INACTIVE_TIMEOUT = this.INACTIVE_TIMEOUT;
                this.IDLE_PENDING_TIMEOUT = 300000;
                this.MAX_IDLE_PENDING_TIMEOUT = LocationFudger.FASTEST_INTERVAL_MS;
                this.IDLE_PENDING_FACTOR = 2.0f;
                this.IDLE_TIMEOUT = 3600000;
                this.MAX_IDLE_TIMEOUT = 21600000;
                this.IDLE_FACTOR = 2.0f;
                this.MIN_TIME_TO_ALARM = 3600000;
                this.MAX_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_MAX_TEMP_APP_WHITELIST_DURATION, 300000);
                this.MMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_MMS_TEMP_APP_WHITELIST_DURATION, 60000);
                this.SMS_TEMP_APP_WHITELIST_DURATION = this.mParser.getLong(KEY_SMS_TEMP_APP_WHITELIST_DURATION, 20000);
                this.NOTIFICATION_WHITELIST_DURATION = this.mParser.getLong(KEY_NOTIFICATION_WHITELIST_DURATION, 30000);
            }
            return;
        }
    }

    public final class LocalService {
        public void addPowerSaveTempWhitelistAppDirect(int appId, long duration, boolean sync, String reason) {
            DeviceIdleController.this.addPowerSaveTempWhitelistAppDirectInternal(0, appId, duration, sync, reason);
        }

        public long getNotificationWhitelistDuration() {
            return DeviceIdleController.this.mConstants.NOTIFICATION_WHITELIST_DURATION;
        }

        public void setNetworkPolicyTempWhitelistCallback(Runnable callback) {
            DeviceIdleController.this.setNetworkPolicyTempWhitelistCallbackInternal(callback);
        }

        public void setJobsActive(boolean active) {
            DeviceIdleController.this.setJobsActive(active);
        }

        public void setAlarmsActive(boolean active) {
            DeviceIdleController.this.setAlarmsActive(active);
        }

        public int[] getPowerSaveWhitelistUserAppIds() {
            return DeviceIdleController.this.getPowerSaveWhitelistUserAppIds();
        }
    }

    private final class MotionListener extends TriggerEventListener implements SensorEventListener {
        boolean active;

        /* synthetic */ MotionListener(DeviceIdleController this$0, MotionListener motionListener) {
            this();
        }

        private MotionListener() {
            this.active = false;
        }

        public void onTrigger(TriggerEvent event) {
            synchronized (DeviceIdleController.this) {
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onSensorChanged(SensorEvent event) {
            synchronized (DeviceIdleController.this) {
                DeviceIdleController.this.mSensorManager.unregisterListener(this, DeviceIdleController.this.mMotionSensor);
                this.active = false;
                DeviceIdleController.this.motionLocked();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public boolean registerLocked() {
            boolean success;
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == 2) {
                success = DeviceIdleController.this.mSensorManager.requestTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                success = DeviceIdleController.this.mSensorManager.registerListener(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor, 3);
            }
            if (success) {
                this.active = true;
            } else {
                Slog.e(DeviceIdleController.TAG, "Unable to register for " + DeviceIdleController.this.mMotionSensor);
            }
            return success;
        }

        public void unregisterLocked() {
            if (DeviceIdleController.this.mMotionSensor.getReportingMode() == 2) {
                DeviceIdleController.this.mSensorManager.cancelTriggerSensor(DeviceIdleController.this.mMotionListener, DeviceIdleController.this.mMotionSensor);
            } else {
                DeviceIdleController.this.mSensorManager.unregisterListener(DeviceIdleController.this.mMotionListener);
            }
            this.active = false;
        }
    }

    final class MyHandler extends Handler {
        MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (DeviceIdleController.DEBUG) {
                Slog.d(DeviceIdleController.TAG, "handleMessage(" + msg.what + ")");
            }
            boolean deepChanged;
            boolean lightChanged;
            int i;
            switch (msg.what) {
                case 1:
                    DeviceIdleController.this.handleWriteConfigFile();
                    return;
                case 2:
                case 3:
                    EventLogTags.writeDeviceIdleOnStart();
                    if (msg.what == 2) {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(true);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    } else {
                        deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                        lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(true);
                    }
                    try {
                        int i2;
                        if (DeviceIdleController.this.getDataShapingService() != null) {
                            DeviceIdleController.this.mDataShapingManager.setDeviceIdleMode(true);
                        }
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(true);
                        IBatteryStats -get1 = DeviceIdleController.this.mBatteryStats;
                        if (msg.what == 2) {
                            i2 = 2;
                        } else {
                            i2 = 1;
                        }
                        -get1.noteDeviceIdleMode(i2, null, Process.myUid());
                    } catch (RemoteException e) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOnComplete();
                    ArrayList<String> listPowerSaveUser = new ArrayList();
                    synchronized (this) {
                        for (i = 0; i < DeviceIdleController.this.mPowerSaveWhitelistUserApps.size(); i++) {
                            listPowerSaveUser.add((String) DeviceIdleController.this.mPowerSaveWhitelistUserApps.keyAt(i));
                        }
                    }
                    if (msg.what == 2) {
                        DeviceIdleController.this.mDeviceIdleHelper.onDeepIdleOn(listPowerSaveUser);
                        return;
                    } else {
                        DeviceIdleController.this.mDeviceIdleHelper.onLightIdleOn(listPowerSaveUser);
                        return;
                    }
                case 4:
                    EventLogTags.writeDeviceIdleOffStart("unknown");
                    deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                    lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    try {
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(false);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(0, null, Process.myUid());
                    } catch (RemoteException e2) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, 0, null, null);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.incActiveIdleOps();
                        DeviceIdleController.this.getContext().sendOrderedBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL, null, DeviceIdleController.this.mIdleStartedDoneReceiver, null, 0, null, null);
                    }
                    DeviceIdleController.this.decActiveIdleOps();
                    EventLogTags.writeDeviceIdleOffComplete();
                    return;
                case 5:
                    String activeReason = msg.obj;
                    int activeUid = msg.arg1;
                    EventLogTags.writeDeviceIdleOffStart(activeReason != null ? activeReason : "unknown");
                    deepChanged = DeviceIdleController.this.mLocalPowerManager.setDeviceIdleMode(false);
                    lightChanged = DeviceIdleController.this.mLocalPowerManager.setLightDeviceIdleMode(false);
                    try {
                        if (DeviceIdleController.this.getDataShapingService() != null) {
                            DeviceIdleController.this.mDataShapingManager.setDeviceIdleMode(false);
                        }
                        DeviceIdleController.this.mNetworkPolicyManager.setDeviceIdleMode(false);
                        DeviceIdleController.this.mBatteryStats.noteDeviceIdleMode(0, activeReason, activeUid);
                    } catch (RemoteException e3) {
                    }
                    if (deepChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mIdleIntent, UserHandle.ALL);
                    }
                    if (lightChanged) {
                        DeviceIdleController.this.getContext().sendBroadcastAsUser(DeviceIdleController.this.mLightIdleIntent, UserHandle.ALL);
                    }
                    EventLogTags.writeDeviceIdleOffComplete();
                    DeviceIdleController.this.mDeviceIdleHelper.onIdleExit();
                    return;
                case 6:
                    DeviceIdleController.this.checkTempAppWhitelistTimeout(msg.arg1);
                    return;
                case 7:
                    boolean active = msg.arg1 == 1;
                    int size = DeviceIdleController.this.mMaintenanceActivityListeners.beginBroadcast();
                    for (i = 0; i < size; i++) {
                        try {
                            ((IMaintenanceActivityListener) DeviceIdleController.this.mMaintenanceActivityListeners.getBroadcastItem(i)).onMaintenanceActivityChanged(active);
                        } catch (RemoteException e4) {
                        } catch (Throwable th) {
                            DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                        }
                    }
                    DeviceIdleController.this.mMaintenanceActivityListeners.finishBroadcast();
                    return;
                case 8:
                    DeviceIdleController.this.decActiveIdleOps();
                    return;
                default:
                    return;
            }
        }
    }

    class Shell extends ShellCommand {
        int userId = 0;

        Shell() {
        }

        public int onCommand(String cmd) {
            return DeviceIdleController.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            DeviceIdleController.dumpHelp(getOutPrintWriter());
        }
    }

    class TrafficRecord {
        long mTimeStamp;
        long mTotalTraffic;

        /* synthetic */ TrafficRecord(DeviceIdleController this$0, long traffic, long timeStamp, TrafficRecord trafficRecord) {
            this(traffic, timeStamp);
        }

        private TrafficRecord(long traffic, long timeStamp) {
            this.mTotalTraffic = traffic;
            this.mTimeStamp = timeStamp;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.DeviceIdleController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.DeviceIdleController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.DeviceIdleController.<clinit>():void");
    }

    private static String stateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 2:
                return "IDLE_PENDING";
            case 3:
                return "SENSING";
            case 4:
                return "LOCATING";
            case 5:
                return "IDLE";
            case 6:
                return "IDLE_MAINTENANCE";
            default:
                return Integer.toString(state);
        }
    }

    private static String lightStateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 3:
                return "PRE_IDLE";
            case 4:
                return "IDLE";
            case 5:
                return "WAITING_FOR_NETWORK";
            case 6:
                return "IDLE_MAINTENANCE";
            case 7:
                return "OVERRIDE";
            default:
                return Integer.toString(state);
        }
    }

    private void addEvent(int cmd) {
        if (this.mEventCmds[0] != cmd) {
            System.arraycopy(this.mEventCmds, 0, this.mEventCmds, 1, 99);
            System.arraycopy(this.mEventTimes, 0, this.mEventTimes, 1, 99);
            this.mEventCmds[0] = cmd;
            this.mEventTimes[0] = SystemClock.elapsedRealtime();
        }
    }

    public void onAnyMotionResult(int result) {
        if (DEBUG) {
            Slog.d(TAG, "onAnyMotionResult(" + result + ")");
        }
        if (result != -1) {
            synchronized (this) {
                cancelSensingTimeoutAlarmLocked();
            }
        }
        if (result == 1 || result == -1) {
            synchronized (this) {
                handleMotionDetectedLocked(this.mConstants.INACTIVE_TIMEOUT, "non_stationary");
                motionDetected(2);
            }
        } else if (result != 0) {
            return;
        } else {
            if (this.mState == 3) {
                synchronized (this) {
                    this.mNotMoving = true;
                    stepIdleStateLocked("s:stationary");
                }
            } else if (this.mState == 4) {
                synchronized (this) {
                    this.mNotMoving = true;
                    if (this.mLocated) {
                        stepIdleStateLocked("s:stationary");
                    }
                }
            } else {
                return;
            }
        }
    }

    public DeviceIdleController(Context context) {
        super(context);
        this.mMaintenanceActivityListeners = new RemoteCallbackList();
        this.mPowerSaveWhitelistAppsExceptIdle = new ArrayMap();
        this.mPowerSaveWhitelistApps = new ArrayMap();
        this.mPowerSaveWhitelistUserApps = new ArrayMap();
        this.mPowerSaveWhitelistSystemAppIdsExceptIdle = new SparseBooleanArray();
        this.mPowerSaveWhitelistSystemAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIdArray = new int[0];
        this.mPowerSaveWhitelistAllAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAllAppIdArray = new int[0];
        this.mPowerSaveWhitelistUserAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistUserAppIdArray = new int[0];
        this.mTempWhitelistAppIdEndTimes = new SparseArray();
        this.mTempWhitelistAppIdArray = new int[0];
        this.mEventCmds = new int[100];
        this.mEventTimes = new long[100];
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                String action = intent.getAction();
                if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    DeviceIdleController.this.updateConnectivityState(intent);
                } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    synchronized (DeviceIdleController.this) {
                        int plugged = intent.getIntExtra("plugged", 0);
                        DeviceIdleController deviceIdleController = DeviceIdleController.this;
                        if (plugged != 0) {
                            z = true;
                        }
                        deviceIdleController.updateChargingLocked(z);
                    }
                } else if (action.equals("android.intent.action.PACKAGE_REMOVED") && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    Uri data = intent.getData();
                    if (data != null) {
                        String ssp = data.getSchemeSpecificPart();
                        if (ssp != null) {
                            DeviceIdleController.this.removePowerSaveWhitelistAppInternal(ssp);
                        }
                    }
                }
            }
        };
        this.mLightAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepLightIdleStateLocked("s:alarm");
                }
            }
        };
        this.mSensingTimeoutAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                if (DeviceIdleController.this.mState == 3) {
                    synchronized (DeviceIdleController.this) {
                        DeviceIdleController.this.becomeInactiveIfAppropriateLocked();
                    }
                }
            }
        };
        this.mDeepAlarmListener = new OnAlarmListener() {
            public void onAlarm() {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.stepIdleStateLocked("s:alarm");
                }
            }
        };
        this.mIdleStartedDoneReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_DEEP_MAINTENANCE_TIME);
                } else {
                    DeviceIdleController.this.mHandler.sendEmptyMessageDelayed(8, DeviceIdleController.this.mConstants.MIN_LIGHT_MAINTENANCE_TIME);
                }
            }
        };
        this.mDisplayListener = new DisplayListener() {
            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    synchronized (DeviceIdleController.this) {
                        DeviceIdleController.this.updateDisplayLocked();
                    }
                }
            }
        };
        this.mMotionListener = new MotionListener(this, null);
        this.mGenericLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.receivedGenericLocationLocked(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        this.mGpsLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                synchronized (DeviceIdleController.this) {
                    DeviceIdleController.this.receivedGpsLocationLocked(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        this.mDeepIdleAccordingToRus = true;
        this.mLightIdleAccordingToRus = true;
        this.mConfigFile = new AtomicFile(new File(getSystemDir(), "deviceidle.xml"));
        this.mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
    }

    int[] getPowerSaveWhitelistUserAppIds() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistUserAppIdArray;
        }
        return iArr;
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public void onStart() {
        PackageManager pm = getContext().getPackageManager();
        synchronized (this) {
            int i;
            ApplicationInfo ai;
            int appid;
            boolean z = getContext().getResources().getBoolean(17956883);
            this.mDeepEnabled = z;
            this.mLightEnabled = z;
            if (SystemProperties.get(CONFIG_AUTO_POWER, "0").equals(Integer.valueOf(1))) {
                this.mDeepEnabled = false;
                this.mLightEnabled = true;
            } else if (SystemProperties.get(CONFIG_AUTO_POWER, "0").equals(Integer.valueOf(2))) {
                this.mDeepEnabled = true;
                this.mLightEnabled = false;
            } else if (SystemProperties.get(CONFIG_AUTO_POWER, "0").equals(Integer.valueOf(3))) {
                this.mDeepEnabled = true;
                this.mLightEnabled = true;
            }
            SystemConfig sysConfig = SystemConfig.getInstance();
            ArraySet<String> allowPowerExceptIdle = sysConfig.getAllowInPowerSaveExceptIdle();
            for (i = 0; i < allowPowerExceptIdle.size(); i++) {
                try {
                    ai = pm.getApplicationInfo((String) allowPowerExceptIdle.valueAt(i), DumpState.DUMP_DEXOPT);
                    appid = UserHandle.getAppId(ai.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid, true);
                } catch (NameNotFoundException e) {
                }
            }
            ArraySet<String> allowPower = sysConfig.getAllowInPowerSave();
            for (i = 0; i < allowPower.size(); i++) {
                try {
                    ai = pm.getApplicationInfo((String) allowPower.valueAt(i), DumpState.DUMP_DEXOPT);
                    appid = UserHandle.getAppId(ai.uid);
                    this.mPowerSaveWhitelistAppsExceptIdle.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIdsExceptIdle.put(appid, true);
                    this.mPowerSaveWhitelistApps.put(ai.packageName, Integer.valueOf(appid));
                    this.mPowerSaveWhitelistSystemAppIds.put(appid, true);
                } catch (NameNotFoundException e2) {
                }
            }
            this.mConstants = new Constants(this.mHandler, getContext().getContentResolver());
            readConfigFileLocked();
            updateWhitelistAppIdsLocked();
            this.mNetworkConnected = true;
            this.mScreenOn = true;
            this.mCharging = true;
            this.mState = 0;
            this.mLightState = 0;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
        }
        this.mBinderService = new BinderService(this, null);
        publishBinderService("deviceidle", this.mBinderService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this) {
                this.mAlarmManager = (AlarmManager) getContext().getSystemService("alarm");
                this.mBatteryStats = BatteryStatsService.getService();
                this.mLocalPowerManager = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
                this.mPowerManager = (PowerManager) getContext().getSystemService(PowerManager.class);
                this.mActiveIdleWakeLock = this.mPowerManager.newWakeLock(1, "deviceidle_maint");
                this.mActiveIdleWakeLock.setReferenceCounted(false);
                this.mConnectivityService = (ConnectivityService) ServiceManager.getService("connectivity");
                this.mLocalAlarmManager = (com.android.server.AlarmManagerService.LocalService) getLocalService(com.android.server.AlarmManagerService.LocalService.class);
                this.mNetworkPolicyManager = INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy"));
                this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
                this.mSensorManager = (SensorManager) getContext().getSystemService("sensor");
                int sigMotionSensorId = getContext().getResources().getInteger(17694733);
                if (sigMotionSensorId > 0) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(sigMotionSensorId, true);
                }
                if (this.mMotionSensor == null && getContext().getResources().getBoolean(17956884)) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(26, true);
                }
                if (this.mMotionSensor == null) {
                    this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
                }
                if (getContext().getResources().getBoolean(17956885)) {
                    this.mLocationManager = (LocationManager) getContext().getSystemService("location");
                    this.mLocationRequest = new LocationRequest().setQuality(100).setInterval(0).setFastestInterval(0).setNumUpdates(1);
                }
                this.mAnyMotionDetector = new AnyMotionDetector((PowerManager) getContext().getSystemService("power"), this.mHandler, this.mSensorManager, this, ((float) getContext().getResources().getInteger(17694732)) / 100.0f);
                this.mIdleIntent = new Intent("android.os.action.DEVICE_IDLE_MODE_CHANGED");
                this.mIdleIntent.addFlags(1342177280);
                this.mLightIdleIntent = new Intent("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
                this.mLightIdleIntent.addFlags(1342177280);
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.BATTERY_CHANGED");
                getContext().registerReceiver(this.mReceiver, filter);
                filter = new IntentFilter();
                filter.addAction("android.intent.action.PACKAGE_REMOVED");
                filter.addDataScheme("package");
                getContext().registerReceiver(this.mReceiver, filter);
                filter = new IntentFilter();
                filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                getContext().registerReceiver(this.mReceiver, filter);
                this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
                this.mLocalAlarmManager.setDeviceIdleUserWhitelist(this.mPowerSaveWhitelistUserAppIdArray);
                this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
                updateDisplayLocked();
                this.mDeviceIdleHelper = new ColorOSDeviceIdleHelper(getContext(), this, this.mConstants);
            }
            updateConnectivityState(null);
        }
    }

    public boolean addPowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            try {
                if (this.mPowerSaveWhitelistUserApps.put(name, Integer.valueOf(UserHandle.getAppId(getContext().getPackageManager().getApplicationInfo(name, DumpState.DUMP_PREFERRED_XML).uid))) == null) {
                    reportPowerSaveWhitelistChangedLocked();
                    updateWhitelistAppIdsLocked();
                    writeConfigFileLocked();
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    public boolean removePowerSaveWhitelistAppInternal(String name) {
        synchronized (this) {
            if (this.mPowerSaveWhitelistUserApps.remove(name) != null) {
                reportPowerSaveWhitelistChangedLocked();
                updateWhitelistAppIdsLocked();
                writeConfigFileLocked();
                return true;
            }
            return false;
        }
    }

    public boolean getPowerSaveWhitelistAppInternal(String name) {
        boolean containsKey;
        synchronized (this) {
            containsKey = this.mPowerSaveWhitelistUserApps.containsKey(name);
        }
        return containsKey;
    }

    public String[] getSystemPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
            apps = new String[size];
            for (int i = 0; i < size; i++) {
                apps[i] = (String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getSystemPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            int size = this.mPowerSaveWhitelistApps.size();
            apps = new String[size];
            for (int i = 0; i < size; i++) {
                apps[i] = (String) this.mPowerSaveWhitelistApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getUserPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            apps = new String[this.mPowerSaveWhitelistUserApps.size()];
            for (int i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
                apps[i] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistExceptIdleInternal() {
        String[] apps;
        synchronized (this) {
            int i;
            apps = new String[(this.mPowerSaveWhitelistAppsExceptIdle.size() + this.mPowerSaveWhitelistUserApps.size())];
            int cur = 0;
            for (i = 0; i < this.mPowerSaveWhitelistAppsExceptIdle.size(); i++) {
                apps[cur] = (String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i);
                cur++;
            }
            for (i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
                apps[cur] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
                cur++;
            }
        }
        return apps;
    }

    public String[] getFullPowerWhitelistInternal() {
        String[] apps;
        synchronized (this) {
            int i;
            apps = new String[(this.mPowerSaveWhitelistApps.size() + this.mPowerSaveWhitelistUserApps.size())];
            int cur = 0;
            for (i = 0; i < this.mPowerSaveWhitelistApps.size(); i++) {
                apps[cur] = (String) this.mPowerSaveWhitelistApps.keyAt(i);
                cur++;
            }
            for (i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
                apps[cur] = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
                cur++;
            }
        }
        return apps;
    }

    public boolean isPowerSaveWhitelistExceptIdleAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (this.mPowerSaveWhitelistAppsExceptIdle.containsKey(packageName)) {
                z = true;
            } else {
                z = this.mPowerSaveWhitelistUserApps.containsKey(packageName);
            }
        }
        return z;
    }

    public boolean isPowerSaveWhitelistAppInternal(String packageName) {
        boolean z;
        synchronized (this) {
            if (this.mPowerSaveWhitelistApps.containsKey(packageName)) {
                z = true;
            } else {
                z = this.mPowerSaveWhitelistUserApps.containsKey(packageName);
            }
        }
        return z;
    }

    public int[] getAppIdWhitelistExceptIdleInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistExceptIdleAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistAllAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdUserWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mPowerSaveWhitelistUserAppIdArray;
        }
        return iArr;
    }

    public int[] getAppIdTempWhitelistInternal() {
        int[] iArr;
        synchronized (this) {
            iArr = this.mTempWhitelistAppIdArray;
        }
        return iArr;
    }

    void addPowerSaveTempWhitelistAppChecked(String packageName, long duration, int userId, String reason) throws RemoteException {
        getContext().enforceCallingPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", "No permission to change device idle whitelist");
        int callingUid = Binder.getCallingUid();
        userId = ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, false, "addPowerSaveTempWhitelistApp", null);
        long token = Binder.clearCallingIdentity();
        try {
            addPowerSaveTempWhitelistAppInternal(callingUid, packageName, duration, userId, true, reason);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void addPowerSaveTempWhitelistAppInternal(int callingUid, String packageName, long duration, int userId, boolean sync, String reason) {
        try {
            addPowerSaveTempWhitelistAppDirectInternal(callingUid, UserHandle.getAppId(getContext().getPackageManager().getPackageUidAsUser(packageName, userId)), duration, sync, reason);
        } catch (NameNotFoundException e) {
        }
    }

    void addPowerSaveTempWhitelistAppDirectInternal(int callingUid, int appId, long duration, boolean sync, String reason) {
        long timeNow = SystemClock.elapsedRealtime();
        Runnable networkPolicyTempWhitelistCallback = null;
        synchronized (this) {
            int callingAppId = UserHandle.getAppId(callingUid);
            if (callingAppId < 10000 || this.mPowerSaveWhitelistSystemAppIds.get(callingAppId)) {
                duration = Math.min(duration, this.mConstants.MAX_TEMP_APP_WHITELIST_DURATION);
                Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.get(appId);
                boolean newEntry = entry == null;
                if (newEntry) {
                    entry = new Pair(new MutableLong(0), reason);
                    this.mTempWhitelistAppIdEndTimes.put(appId, entry);
                }
                ((MutableLong) entry.first).value = timeNow + duration;
                if (DEBUG) {
                    Slog.d(TAG, "Adding AppId " + appId + " to temp whitelist. New entry: " + newEntry);
                }
                if (newEntry) {
                    try {
                        this.mBatteryStats.noteEvent(32785, reason, appId);
                    } catch (RemoteException e) {
                    }
                    postTempActiveTimeoutMessage(appId, duration);
                    updateTempWhitelistAppIdsLocked();
                    if (this.mNetworkPolicyTempWhitelistCallback != null) {
                        if (sync) {
                            networkPolicyTempWhitelistCallback = this.mNetworkPolicyTempWhitelistCallback;
                        } else {
                            this.mHandler.post(this.mNetworkPolicyTempWhitelistCallback);
                        }
                    }
                    reportTempWhitelistChangedLocked();
                }
            } else {
                throw new SecurityException("Calling app " + UserHandle.formatUid(callingUid) + " is not on whitelist");
            }
        }
        if (networkPolicyTempWhitelistCallback != null) {
            networkPolicyTempWhitelistCallback.run();
        }
    }

    public void setNetworkPolicyTempWhitelistCallbackInternal(Runnable callback) {
        synchronized (this) {
            this.mNetworkPolicyTempWhitelistCallback = callback;
        }
    }

    private void postTempActiveTimeoutMessage(int uid, long delay) {
        if (DEBUG) {
            Slog.d(TAG, "postTempActiveTimeoutMessage: uid=" + uid + ", delay=" + delay);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6, uid, 0), delay);
    }

    void checkTempAppWhitelistTimeout(int uid) {
        long timeNow = SystemClock.elapsedRealtime();
        if (DEBUG) {
            Slog.d(TAG, "checkTempAppWhitelistTimeout: uid=" + uid + ", timeNow=" + timeNow);
        }
        synchronized (this) {
            Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.get(uid);
            if (entry == null) {
                return;
            } else if (timeNow >= ((MutableLong) entry.first).value) {
                this.mTempWhitelistAppIdEndTimes.delete(uid);
                if (DEBUG) {
                    Slog.d(TAG, "Removing UID " + uid + " from temp whitelist");
                }
                updateTempWhitelistAppIdsLocked();
                if (this.mNetworkPolicyTempWhitelistCallback != null) {
                    this.mHandler.post(this.mNetworkPolicyTempWhitelistCallback);
                }
                reportTempWhitelistChangedLocked();
                try {
                    this.mBatteryStats.noteEvent(16401, (String) entry.second, uid);
                } catch (RemoteException e) {
                }
            } else {
                if (DEBUG) {
                    Slog.d(TAG, "Time to remove UID " + uid + ": " + ((MutableLong) entry.first).value);
                }
                postTempActiveTimeoutMessage(uid, ((MutableLong) entry.first).value - timeNow);
            }
        }
    }

    public void exitIdleInternal(String reason) {
        synchronized (this) {
            becomeActiveLocked(reason, Binder.getCallingUid());
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0026, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updateConnectivityState(Intent connIntent) {
        ConnectivityService cm;
        synchronized (this) {
            cm = this.mConnectivityService;
        }
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            synchronized (this) {
                boolean conn;
                if (ni == null) {
                    conn = false;
                } else if (connIntent == null) {
                    conn = ni.isConnected();
                } else {
                    if (ni.getType() != connIntent.getIntExtra("networkType", -1)) {
                        return;
                    }
                    conn = !connIntent.getBooleanExtra("noConnectivity", false);
                }
                if (conn != this.mNetworkConnected) {
                    this.mNetworkConnected = conn;
                    if (conn && this.mLightState == 5) {
                        stepLightIdleStateLocked("network");
                    }
                }
            }
        }
    }

    void updateDisplayLocked() {
        this.mCurDisplay = this.mDisplayManager.getDisplay(0);
        boolean screenOn = this.mCurDisplay.getState() == 2;
        if (DEBUG) {
            Slog.d(TAG, "updateDisplayLocked: screenOn=" + screenOn);
        }
        if (!screenOn && this.mScreenOn) {
            this.mScreenOn = false;
            if (!this.mForceIdle) {
                becomeInactiveIfAppropriateLocked();
            }
            if (this.mDeviceIdleHelper != null) {
                if (this.mLightIdleAccordingToRus) {
                    this.mLightEnabled = this.mDeviceIdleHelper.isAutoPowerModesEnabled();
                }
                if (this.mDeepIdleAccordingToRus) {
                    this.mDeepEnabled = this.mDeviceIdleHelper.isAutoPowerModesEnabled();
                }
                this.mDeviceIdleHelper.onScreenOff();
            }
        } else if (screenOn) {
            this.mScreenOn = true;
            if (!this.mForceIdle) {
                becomeActiveLocked("screen", Process.myUid());
                if (this.mDeviceIdleHelper != null) {
                    this.mDeviceIdleHelper.onScreenOn();
                }
            }
        }
    }

    void updateChargingLocked(boolean charging) {
        if (DEBUG) {
            Slog.i(TAG, "updateChargingLocked: charging=" + charging);
        }
        if (!charging && this.mCharging) {
            this.mCharging = false;
            if (!this.mForceIdle) {
                becomeInactiveIfAppropriateLocked();
            }
        } else if (charging) {
            this.mCharging = charging;
            if (!this.mForceIdle) {
                becomeActiveLocked("charging", Process.myUid());
            }
        }
    }

    void scheduleReportActiveLocked(String activeReason, int activeUid) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, activeUid, 0, activeReason));
    }

    void becomeActiveLocked(String activeReason, int activeUid) {
        if (DEBUG) {
            Slog.i(TAG, "becomeActiveLocked, reason = " + activeReason);
        }
        if (this.mState != 0 || this.mLightState != 0) {
            EventLogTags.writeDeviceIdle(0, activeReason);
            EventLogTags.writeDeviceIdleLight(0, activeReason);
            scheduleReportActiveLocked(activeReason, activeUid);
            this.mState = 0;
            this.mLightState = 0;
            this.mInactiveTimeout = this.mConstants.INACTIVE_TIMEOUT;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            resetIdleManagementLocked();
            resetLightIdleManagementLocked();
            addEvent(1);
        }
    }

    void becomeInactiveIfAppropriateLocked() {
        if (DEBUG) {
            Slog.d(TAG, "becomeInactiveIfAppropriateLocked()");
        }
        if ((!this.mScreenOn && !this.mCharging) || this.mForceIdle) {
            if (this.mState == 0 && this.mDeepEnabled) {
                this.mState = 1;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from STATE_ACTIVE to STATE_INACTIVE");
                }
                resetIdleManagementLocked();
                scheduleAlarmLocked(this.mInactiveTimeout, false);
                EventLogTags.writeDeviceIdle(this.mState, "no activity");
            }
            if (this.mLightState == 0 && this.mLightEnabled) {
                this.mLightState = 1;
                if (DEBUG) {
                    Slog.d(TAG, "Moved from LIGHT_STATE_ACTIVE to LIGHT_STATE_INACTIVE");
                }
                resetLightIdleManagementLocked();
                scheduleLightAlarmLocked(this.mConstants.LIGHT_IDLE_AFTER_INACTIVE_TIMEOUT);
                this.mLastLightTrafficRecord = new TrafficRecord(this, getTotalTraffic(), SystemClock.elapsedRealtime(), null);
                EventLogTags.writeDeviceIdleLight(this.mLightState, "no activity");
            }
        }
    }

    void resetIdleManagementLocked() {
        this.mNextIdlePendingDelay = 0;
        this.mNextIdleDelay = 0;
        this.mNextLightIdleDelay = 0;
        cancelAlarmLocked();
        cancelSensingTimeoutAlarmLocked();
        cancelLocatingLocked();
        stopMonitoringMotionLocked();
        this.mAnyMotionDetector.stop();
    }

    void resetLightIdleManagementLocked() {
        cancelLightAlarmLocked();
    }

    void exitForceIdleLocked() {
        if (this.mForceIdle) {
            this.mForceIdle = false;
            if (this.mScreenOn || this.mCharging) {
                becomeActiveLocked("exit-force", Process.myUid());
            }
        }
    }

    void stepLightIdleStateLocked(String reason) {
        if (this.mLightState != 7) {
            if (DEBUG) {
                Slog.d(TAG, "stepLightIdleStateLocked: mLightState=" + this.mLightState);
            }
            EventLogTags.writeDeviceIdleLightStep();
            switch (this.mLightState) {
                case 1:
                    this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                    this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
                    this.mMaintenanceStartTime = 0;
                    if (!isOpsInactiveLocked()) {
                        this.mLightState = 3;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        scheduleLightAlarmLocked(this.mConstants.LIGHT_PRE_IDLE_TIMEOUT);
                        break;
                    }
                case 3:
                case 6:
                    if ((this.mLightState != 1 && this.mLightState != 3) || !isLightInTraffic()) {
                        if (this.mMaintenanceStartTime != 0) {
                            long duration = SystemClock.elapsedRealtime() - this.mMaintenanceStartTime;
                            if (duration < this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET) {
                                this.mCurIdleBudget += this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET - duration;
                            } else {
                                this.mCurIdleBudget -= duration - this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                            }
                        }
                        this.mMaintenanceStartTime = 0;
                        scheduleLightAlarmLocked(this.mNextLightIdleDelay);
                        this.mNextLightIdleDelay = Math.min(this.mConstants.LIGHT_MAX_IDLE_TIMEOUT, (long) (((float) this.mNextLightIdleDelay) * this.mConstants.LIGHT_IDLE_FACTOR));
                        if (this.mNextLightIdleDelay < this.mConstants.LIGHT_IDLE_TIMEOUT) {
                            this.mNextLightIdleDelay = this.mConstants.LIGHT_IDLE_TIMEOUT;
                        }
                        if (DEBUG) {
                            Slog.d(TAG, "Moved to LIGHT_STATE_IDLE.");
                        }
                        this.mLightState = 4;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        addEvent(2);
                        this.mHandler.sendEmptyMessage(3);
                        break;
                    }
                    scheduleLightAlarmLocked(300000);
                    if (DEBUG_OPPO) {
                        Slog.d(TAG, "stepLightIdleStateLocked: is in traffic.");
                        break;
                    }
                    break;
                case 4:
                case 5:
                    if (!this.mNetworkConnected && this.mLightState != 5) {
                        scheduleLightAlarmLocked(this.mNextLightIdleDelay);
                        if (DEBUG) {
                            Slog.d(TAG, "Moved to LIGHT_WAITING_FOR_NETWORK.");
                        }
                        this.mLightState = 5;
                        EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                        break;
                    }
                    this.mActiveIdleOpCount = 1;
                    this.mActiveIdleWakeLock.acquire();
                    this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                    if (this.mCurIdleBudget < this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET) {
                        this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MIN_BUDGET;
                    } else if (this.mCurIdleBudget > this.mConstants.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET) {
                        this.mCurIdleBudget = this.mConstants.LIGHT_IDLE_MAINTENANCE_MAX_BUDGET;
                    }
                    scheduleLightAlarmLocked(this.mCurIdleBudget);
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from LIGHT_STATE_IDLE to LIGHT_STATE_IDLE_MAINTENANCE.");
                    }
                    this.mLightState = 6;
                    EventLogTags.writeDeviceIdleLight(this.mLightState, reason);
                    addEvent(3);
                    this.mHandler.sendEmptyMessage(4);
                    break;
                    break;
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0052, code:
            return;
     */
    /* JADX WARNING: Missing block: B:39:0x013d, code:
            if (r12.mLocating == false) goto L_0x013f;
     */
    /* JADX WARNING: Missing block: B:40:0x013f, code:
            cancelAlarmLocked();
            cancelLocatingLocked();
            r12.mAnyMotionDetector.stop();
     */
    /* JADX WARNING: Missing block: B:41:0x014a, code:
            scheduleAlarmLocked(r12.mNextIdleDelay, true);
     */
    /* JADX WARNING: Missing block: B:42:0x0151, code:
            if (DEBUG == false) goto L_0x0176;
     */
    /* JADX WARNING: Missing block: B:43:0x0153, code:
            android.util.Slog.d(TAG, "Moved to STATE_IDLE. Next alarm in " + r12.mNextIdleDelay + " ms.");
     */
    /* JADX WARNING: Missing block: B:44:0x0176, code:
            r12.mNextIdleDelay = (long) (((float) r12.mNextIdleDelay) * r12.mConstants.IDLE_FACTOR);
     */
    /* JADX WARNING: Missing block: B:45:0x0183, code:
            if (DEBUG == false) goto L_0x01a1;
     */
    /* JADX WARNING: Missing block: B:46:0x0185, code:
            android.util.Slog.d(TAG, "Setting mNextIdleDelay = " + r12.mNextIdleDelay);
     */
    /* JADX WARNING: Missing block: B:47:0x01a1, code:
            r12.mNextIdleDelay = java.lang.Math.min(r12.mNextIdleDelay, r12.mConstants.MAX_IDLE_TIMEOUT);
     */
    /* JADX WARNING: Missing block: B:48:0x01b5, code:
            if (r12.mNextIdleDelay >= r12.mConstants.IDLE_TIMEOUT) goto L_0x01bd;
     */
    /* JADX WARNING: Missing block: B:49:0x01b7, code:
            r12.mNextIdleDelay = r12.mConstants.IDLE_TIMEOUT;
     */
    /* JADX WARNING: Missing block: B:50:0x01bd, code:
            r12.mState = 5;
     */
    /* JADX WARNING: Missing block: B:51:0x01c3, code:
            if (r12.mLightState == 7) goto L_0x01cb;
     */
    /* JADX WARNING: Missing block: B:52:0x01c5, code:
            r12.mLightState = 7;
            cancelLightAlarmLocked();
     */
    /* JADX WARNING: Missing block: B:53:0x01cb, code:
            com.android.server.EventLogTags.writeDeviceIdle(r12.mState, r13);
            addEvent(4);
            r12.mHandler.sendEmptyMessage(2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void stepIdleStateLocked(String reason) {
        if (DEBUG) {
            Slog.d(TAG, "stepIdleStateLocked: mState=" + this.mState);
        }
        EventLogTags.writeDeviceIdleStep();
        if (this.mConstants.MIN_TIME_TO_ALARM + SystemClock.elapsedRealtime() <= this.mAlarmManager.getNextWakeFromIdleTime()) {
            switch (this.mState) {
                case 1:
                    startMonitoringMotionLocked();
                    scheduleAlarmLocked(this.mConstants.IDLE_AFTER_INACTIVE_TIMEOUT, false);
                    this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    this.mNextIdleDelay = this.mConstants.IDLE_TIMEOUT;
                    this.mState = 2;
                    this.mLastDeepTrafficRecord = new TrafficRecord(this, getTotalTraffic(), SystemClock.elapsedRealtime(), null);
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_INACTIVE to STATE_IDLE_PENDING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    break;
                case 2:
                    if (!isDeepInTraffic()) {
                        this.mState = 3;
                        if (DEBUG) {
                            Slog.d(TAG, "Moved from STATE_IDLE_PENDING to STATE_SENSING.");
                        }
                        EventLogTags.writeDeviceIdle(this.mState, reason);
                        scheduleSensingTimeoutAlarmLocked(this.mConstants.SENSING_TIMEOUT);
                        cancelLocatingLocked();
                        this.mNotMoving = false;
                        this.mLocated = false;
                        this.mLastGenericLocation = null;
                        this.mLastGpsLocation = null;
                        this.mAnyMotionDetector.checkForAnyMotion();
                        break;
                    }
                    scheduleAlarmLocked(300000, false);
                    if (DEBUG_OPPO) {
                        Slog.d(TAG, "stepIdleStateLocked: is in traffic.");
                        break;
                    }
                    break;
                case 3:
                    cancelSensingTimeoutAlarmLocked();
                    this.mState = 4;
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_SENSING to STATE_LOCATING.");
                    }
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    scheduleAlarmLocked(this.mConstants.LOCATING_TIMEOUT, false);
                    if (this.mLocationManager == null || this.mLocationManager.getProvider("network") == null) {
                        this.mHasNetworkLocation = false;
                    } else {
                        this.mLocationManager.requestLocationUpdates(this.mLocationRequest, this.mGenericLocationListener, this.mHandler.getLooper());
                        this.mLocating = true;
                    }
                    if (this.mLocationManager == null || this.mLocationManager.getProvider("gps") == null) {
                        this.mHasGps = false;
                    } else {
                        this.mHasGps = true;
                        this.mLocationManager.requestLocationUpdates("gps", 1000, 5.0f, this.mGpsLocationListener, this.mHandler.getLooper());
                        this.mLocating = true;
                    }
                    break;
                case 4:
                    break;
                case 5:
                    this.mActiveIdleOpCount = 1;
                    this.mActiveIdleWakeLock.acquire();
                    scheduleAlarmLocked(this.mNextIdlePendingDelay, false);
                    if (DEBUG) {
                        Slog.d(TAG, "Moved from STATE_IDLE to STATE_IDLE_MAINTENANCE. Next alarm in " + this.mNextIdlePendingDelay + " ms.");
                    }
                    this.mMaintenanceStartTime = SystemClock.elapsedRealtime();
                    this.mNextIdlePendingDelay = Math.min(this.mConstants.MAX_IDLE_PENDING_TIMEOUT, (long) (((float) this.mNextIdlePendingDelay) * this.mConstants.IDLE_PENDING_FACTOR));
                    if (this.mNextIdlePendingDelay < this.mConstants.IDLE_PENDING_TIMEOUT) {
                        this.mNextIdlePendingDelay = this.mConstants.IDLE_PENDING_TIMEOUT;
                    }
                    this.mState = 6;
                    EventLogTags.writeDeviceIdle(this.mState, reason);
                    addEvent(5);
                    this.mHandler.sendEmptyMessage(4);
                    break;
                case 6:
                    break;
            }
        }
        if (this.mState != 0) {
            becomeActiveLocked("alarm", Process.myUid());
            becomeInactiveIfAppropriateLocked();
        }
    }

    void incActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount++;
        }
    }

    void decActiveIdleOps() {
        synchronized (this) {
            this.mActiveIdleOpCount--;
            if (this.mActiveIdleOpCount <= 0) {
                exitMaintenanceEarlyIfNeededLocked();
                this.mActiveIdleWakeLock.release();
            }
        }
    }

    void setJobsActive(boolean active) {
        synchronized (this) {
            this.mJobsActive = active;
            reportMaintenanceActivityIfNeededLocked();
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    void setAlarmsActive(boolean active) {
        synchronized (this) {
            this.mAlarmsActive = active;
            if (!active) {
                exitMaintenanceEarlyIfNeededLocked();
            }
        }
    }

    boolean registerMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        boolean z;
        synchronized (this) {
            this.mMaintenanceActivityListeners.register(listener);
            z = this.mReportedMaintenanceActivity;
        }
        return z;
    }

    void unregisterMaintenanceActivityListener(IMaintenanceActivityListener listener) {
        synchronized (this) {
            this.mMaintenanceActivityListeners.unregister(listener);
        }
    }

    void reportMaintenanceActivityIfNeededLocked() {
        boolean active = this.mJobsActive;
        if (active != this.mReportedMaintenanceActivity) {
            int i;
            this.mReportedMaintenanceActivity = active;
            MyHandler myHandler = this.mHandler;
            if (this.mReportedMaintenanceActivity) {
                i = 1;
            } else {
                i = 0;
            }
            this.mHandler.sendMessage(myHandler.obtainMessage(7, i, 0));
        }
    }

    boolean isOpsInactiveLocked() {
        return (this.mActiveIdleOpCount > 0 || this.mJobsActive || this.mAlarmsActive) ? false : true;
    }

    void exitMaintenanceEarlyIfNeededLocked() {
        if ((this.mState == 6 || this.mLightState == 6 || this.mLightState == 3) && isOpsInactiveLocked()) {
            long now = SystemClock.elapsedRealtime();
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Exit: start=");
                TimeUtils.formatDuration(this.mMaintenanceStartTime, sb);
                sb.append(" now=");
                TimeUtils.formatDuration(now, sb);
                Slog.d(TAG, sb.toString());
            }
            if (this.mState == 6) {
                stepIdleStateLocked("s:early");
            } else if (this.mLightState == 3) {
                stepLightIdleStateLocked("s:predone");
            } else {
                stepLightIdleStateLocked("s:early");
            }
        }
    }

    void motionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "motionLocked()");
        }
        motionDetected(1);
        handleMotionDetectedLocked(this.mConstants.MOTION_INACTIVE_TIMEOUT, "motion");
    }

    void handleMotionDetectedLocked(long timeout, String type) {
        boolean becomeInactive = false;
        if (this.mState != 0) {
            scheduleReportActiveLocked(type, Process.myUid());
            this.mState = 0;
            this.mInactiveTimeout = timeout;
            this.mCurIdleBudget = 0;
            this.mMaintenanceStartTime = 0;
            EventLogTags.writeDeviceIdle(this.mState, type);
            addEvent(1);
            becomeInactive = true;
        }
        if (this.mLightState == 7) {
            this.mLightState = 0;
            EventLogTags.writeDeviceIdleLight(this.mLightState, type);
            becomeInactive = true;
        }
        if (becomeInactive) {
            becomeInactiveIfAppropriateLocked();
        }
    }

    void receivedGenericLocationLocked(Location location) {
        if (this.mState != 4) {
            cancelLocatingLocked();
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "Generic location: " + location);
        }
        this.mLastGenericLocation = new Location(location);
        if (location.getAccuracy() <= this.mConstants.LOCATION_ACCURACY || !this.mHasGps) {
            this.mLocated = true;
            if (this.mNotMoving) {
                stepIdleStateLocked("s:location");
            }
        }
    }

    void receivedGpsLocationLocked(Location location) {
        if (this.mState != 4) {
            cancelLocatingLocked();
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "GPS location: " + location);
        }
        this.mLastGpsLocation = new Location(location);
        if (location.getAccuracy() <= this.mConstants.LOCATION_ACCURACY) {
            this.mLocated = true;
            if (this.mNotMoving) {
                stepIdleStateLocked("s:gps");
            }
        }
    }

    void startMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "startMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && !this.mMotionListener.active) {
            this.mMotionListener.registerLocked();
        }
    }

    void stopMonitoringMotionLocked() {
        if (DEBUG) {
            Slog.d(TAG, "stopMonitoringMotionLocked()");
        }
        if (this.mMotionSensor != null && this.mMotionListener.active) {
            this.mMotionListener.unregisterLocked();
        }
    }

    void cancelAlarmLocked() {
        if (this.mNextAlarmTime != 0) {
            this.mNextAlarmTime = 0;
            this.mAlarmManager.cancel(this.mDeepAlarmListener);
        }
    }

    void cancelLightAlarmLocked() {
        if (this.mNextLightAlarmTime != 0) {
            this.mNextLightAlarmTime = 0;
            this.mAlarmManager.cancel(this.mLightAlarmListener);
        }
    }

    void cancelLocatingLocked() {
        if (this.mLocating) {
            this.mLocationManager.removeUpdates(this.mGenericLocationListener);
            this.mLocationManager.removeUpdates(this.mGpsLocationListener);
            this.mLocating = false;
        }
    }

    void cancelSensingTimeoutAlarmLocked() {
        if (this.mNextSensingTimeoutAlarmTime != 0) {
            this.mNextSensingTimeoutAlarmTime = 0;
            this.mAlarmManager.cancel(this.mSensingTimeoutAlarmListener);
        }
    }

    void scheduleAlarmLocked(long delay, boolean idleUntil) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleAlarmLocked(" + delay + ", " + idleUntil + ")");
        }
        if (this.mMotionSensor != null) {
            this.mNextAlarmTime = SystemClock.elapsedRealtime() + delay;
            if (idleUntil) {
                this.mAlarmManager.setIdleUntil(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            } else {
                this.mAlarmManager.setExact(2, this.mNextAlarmTime, "DeviceIdleController.deep", this.mDeepAlarmListener, this.mHandler);
            }
        }
    }

    void scheduleLightAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleLightAlarmLocked(" + delay + ")");
        }
        this.mNextLightAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.setExact(2, this.mNextLightAlarmTime, "DeviceIdleController.light", this.mLightAlarmListener, this.mHandler);
    }

    void scheduleSensingTimeoutAlarmLocked(long delay) {
        if (DEBUG) {
            Slog.d(TAG, "scheduleSensingAlarmLocked(" + delay + ")");
        }
        this.mNextSensingTimeoutAlarmTime = SystemClock.elapsedRealtime() + delay;
        this.mAlarmManager.setExact(2, this.mNextSensingTimeoutAlarmTime, "DeviceIdleController.sensing", this.mSensingTimeoutAlarmListener, this.mHandler);
    }

    private static int[] buildAppIdArray(ArrayMap<String, Integer> systemApps, ArrayMap<String, Integer> userApps, SparseBooleanArray outAppIds) {
        int i;
        outAppIds.clear();
        if (systemApps != null) {
            for (i = 0; i < systemApps.size(); i++) {
                outAppIds.put(((Integer) systemApps.valueAt(i)).intValue(), true);
            }
        }
        if (userApps != null) {
            for (i = 0; i < userApps.size(); i++) {
                outAppIds.put(((Integer) userApps.valueAt(i)).intValue(), true);
            }
        }
        int size = outAppIds.size();
        int[] appids = new int[size];
        for (i = 0; i < size; i++) {
            appids[i] = outAppIds.keyAt(i);
        }
        return appids;
    }

    private void updateWhitelistAppIdsLocked() {
        this.mPowerSaveWhitelistExceptIdleAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistAppsExceptIdle, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistExceptIdleAppIds);
        this.mPowerSaveWhitelistAllAppIdArray = buildAppIdArray(this.mPowerSaveWhitelistApps, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistAllAppIds);
        this.mPowerSaveWhitelistUserAppIdArray = buildAppIdArray(null, this.mPowerSaveWhitelistUserApps, this.mPowerSaveWhitelistUserAppIds);
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock whitelist to " + Arrays.toString(this.mPowerSaveWhitelistAllAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleWhitelist(this.mPowerSaveWhitelistAllAppIdArray);
        }
        if (this.mLocalAlarmManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting alarm whitelist to " + Arrays.toString(this.mPowerSaveWhitelistUserAppIdArray));
            }
            this.mLocalAlarmManager.setDeviceIdleUserWhitelist(this.mPowerSaveWhitelistUserAppIdArray);
        }
    }

    private void updateTempWhitelistAppIdsLocked() {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (this.mTempWhitelistAppIdArray.length != size) {
            this.mTempWhitelistAppIdArray = new int[size];
        }
        for (int i = 0; i < size; i++) {
            this.mTempWhitelistAppIdArray[i] = this.mTempWhitelistAppIdEndTimes.keyAt(i);
        }
        if (this.mLocalPowerManager != null) {
            if (DEBUG) {
                Slog.d(TAG, "Setting wakelock temp whitelist to " + Arrays.toString(this.mTempWhitelistAppIdArray));
            }
            this.mLocalPowerManager.setDeviceIdleTempWhitelist(this.mTempWhitelistAppIdArray);
        }
    }

    private void reportPowerSaveWhitelistChangedLocked() {
        Intent intent = new Intent("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        intent.addFlags(1073741824);
        getContext().sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    private void reportTempWhitelistChangedLocked() {
        Intent intent = new Intent("android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED");
        intent.addFlags(1073741824);
        getContext().sendBroadcastAsUser(intent, UserHandle.SYSTEM);
    }

    void readConfigFileLocked() {
        if (DEBUG) {
            Slog.d(TAG, "Reading config from " + this.mConfigFile.getBaseFile());
        }
        this.mPowerSaveWhitelistUserApps.clear();
        try {
            FileInputStream stream = this.mConfigFile.openRead();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, StandardCharsets.UTF_8.name());
                readConfigFileLocked(parser);
                try {
                    stream.close();
                } catch (IOException e) {
                }
            } catch (XmlPullParserException e2) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
        }
    }

    private void readConfigFileLocked(XmlPullParser parser) {
        int type;
        PackageManager pm = getContext().getPackageManager();
        do {
            try {
                type = parser.next();
                if (type == 2) {
                    break;
                }
            } catch (IllegalStateException e) {
                Slog.w(TAG, "Failed parsing config " + e);
                return;
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Failed parsing config " + e2);
                return;
            } catch (NumberFormatException e3) {
                Slog.w(TAG, "Failed parsing config " + e3);
                return;
            } catch (XmlPullParserException e4) {
                Slog.w(TAG, "Failed parsing config " + e4);
                return;
            } catch (IOException e5) {
                Slog.w(TAG, "Failed parsing config " + e5);
                return;
            } catch (IndexOutOfBoundsException e6) {
                Slog.w(TAG, "Failed parsing config " + e6);
                return;
            }
        } while (type != 1);
        if (type != 2) {
            throw new IllegalStateException("no start tag found");
        }
        int outerDepth = parser.getDepth();
        while (true) {
            type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("wl")) {
                    String name = parser.getAttributeValue(null, OppoCrashClearManager.CRASH_COUNT);
                    if (name != null) {
                        try {
                            ApplicationInfo ai = pm.getApplicationInfo(name, DumpState.DUMP_PREFERRED_XML);
                            this.mPowerSaveWhitelistUserApps.put(ai.packageName, Integer.valueOf(UserHandle.getAppId(ai.uid)));
                        } catch (NameNotFoundException e7) {
                        }
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <config>: " + parser.getName());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
    }

    void writeConfigFileLocked() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    void handleWriteConfigFile() {
        ByteArrayOutputStream memStream = new ByteArrayOutputStream();
        try {
            synchronized (this) {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(memStream, StandardCharsets.UTF_8.name());
                writeConfigFileLocked(out);
            }
        } catch (IOException e) {
        }
        synchronized (this.mConfigFile) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = this.mConfigFile.startWrite();
                memStream.writeTo(fileOutputStream);
                fileOutputStream.flush();
                FileUtils.sync(fileOutputStream);
                fileOutputStream.close();
                this.mConfigFile.finishWrite(fileOutputStream);
            } catch (IOException e2) {
                Slog.w(TAG, "Error writing config file", e2);
                this.mConfigFile.failWrite(fileOutputStream);
            }
        }
        return;
    }

    void writeConfigFileLocked(XmlSerializer out) throws IOException {
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, "config");
        for (int i = 0; i < this.mPowerSaveWhitelistUserApps.size(); i++) {
            String name = (String) this.mPowerSaveWhitelistUserApps.keyAt(i);
            out.startTag(null, "wl");
            out.attribute(null, OppoCrashClearManager.CRASH_COUNT, name);
            out.endTag(null, "wl");
        }
        out.endTag(null, "config");
        out.endDocument();
    }

    static void dumpHelp(PrintWriter pw) {
        pw.println("Device idle controller (deviceidle) commands:");
        pw.println("  help");
        pw.println("    Print this help text.");
        pw.println("  step [light|deep]");
        pw.println("    Immediately step to next state, without waiting for alarm.");
        pw.println("  force-idle [light|deep]");
        pw.println("    Force directly into idle mode, regardless of other device state.");
        pw.println("  force-inactive");
        pw.println("    Force to be inactive, ready to freely step idle states.");
        pw.println("  unforce");
        pw.println("    Resume normal functioning after force-idle or force-inactive.");
        pw.println("  get [light|deep|force|screen|charging|network]");
        pw.println("    Retrieve the current given state.");
        pw.println("  disable [light|deep|all]");
        pw.println("    Completely disable device idle mode.");
        pw.println("  enable [light|deep|all]");
        pw.println("    Re-enable device idle mode after it had previously been disabled.");
        pw.println("  enabled [light|deep|all]");
        pw.println("    Print 1 if device idle mode is currently enabled, else 0.");
        pw.println("  whitelist");
        pw.println("    Print currently whitelisted apps.");
        pw.println("  whitelist [package ...]");
        pw.println("    Add (prefix with +) or remove (prefix with -) packages.");
        pw.println("  tempwhitelist");
        pw.println("    Print packages that are temporarily whitelisted.");
        pw.println("  tempwhitelist [-u] [package ..]");
        pw.println("    Temporarily place packages in whitelist for 10 seconds.");
        pw.println("  motiondetectlog [enable/disable]");
        pw.println("    enable:AnyMotionDetector log on;disable:AnyMotionDetector log off");
        pw.println("  networkpolicylog [enable/disable]");
        pw.println("    enable:NetworkPolicyManagerService log on;disable:NetworkPolicyManagerService log off");
        pw.println("  deviceidlelog [enable/disable]");
        pw.println("    enable:Deviceidle log on;disable:Deviceidle log off");
        pw.println("  deviceidleoppolog [enable/disable]");
        pw.println("    enable:Deviceidle oppo log on;disable:Deviceidle oppo log off");
    }

    /* JADX WARNING: Missing block: B:179:0x0348, code:
            if ("all".equals(r3) == false) goto L_0x0365;
     */
    /* JADX WARNING: Missing block: B:220:0x0416, code:
            if ("all".equals(r3) == false) goto L_0x0433;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int onShellCommand(Shell shell, String cmd) {
        PrintWriter pw = shell.getOutPrintWriter();
        long token;
        String arg;
        boolean valid;
        if ("step".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    arg = shell.getNextArg();
                    if (arg != null) {
                        if (!"deep".equals(arg)) {
                            if ("light".equals(arg)) {
                                stepLightIdleStateLocked("s:shell");
                                pw.print("Stepped to light: ");
                                pw.println(lightStateToString(this.mLightState));
                            } else {
                                pw.println("Unknown idle mode: " + arg);
                            }
                            Binder.restoreCallingIdentity(token);
                        }
                    }
                    stepIdleStateLocked("s:shell");
                    pw.print("Stepped to deep: ");
                    pw.println(stateToString(this.mState));
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    throw th;
                }
            }
            return 0;
        } else if ("force-idle".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    arg = shell.getNextArg();
                    if (arg != null) {
                        if (!"deep".equals(arg)) {
                            if ("light".equals(arg)) {
                                this.mForceIdle = true;
                                becomeInactiveIfAppropriateLocked();
                                int curLightState = this.mLightState;
                                while (curLightState != 4) {
                                    stepIdleStateLocked("s:shell");
                                    if (curLightState == this.mLightState) {
                                        pw.print("Unable to go light idle; stopped at ");
                                        pw.println(lightStateToString(this.mLightState));
                                        exitForceIdleLocked();
                                        Binder.restoreCallingIdentity(token);
                                        return -1;
                                    }
                                    curLightState = this.mLightState;
                                }
                                pw.println("Now forced in to light idle mode");
                                Binder.restoreCallingIdentity(token);
                            } else {
                                pw.println("Unknown idle mode: " + arg);
                                Binder.restoreCallingIdentity(token);
                            }
                        }
                    }
                    if (this.mDeepEnabled) {
                        this.mForceIdle = true;
                        becomeInactiveIfAppropriateLocked();
                        int curState = this.mState;
                        while (curState != 5) {
                            stepIdleStateLocked("s:shell");
                            if (curState == this.mState) {
                                pw.print("Unable to go deep idle; stopped at ");
                                pw.println(stateToString(this.mState));
                                exitForceIdleLocked();
                                Binder.restoreCallingIdentity(token);
                                return -1;
                            }
                            curState = this.mState;
                        }
                        pw.println("Now forced in to deep idle mode");
                        Binder.restoreCallingIdentity(token);
                    } else {
                        pw.println("Unable to go deep idle; not enabled");
                        Binder.restoreCallingIdentity(token);
                        return -1;
                    }
                } catch (Throwable th2) {
                    throw th2;
                }
            }
        } else if ("force-inactive".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    this.mForceIdle = true;
                    becomeInactiveIfAppropriateLocked();
                    pw.print("Light state: ");
                    pw.print(lightStateToString(this.mLightState));
                    pw.print(", deep state: ");
                    pw.println(stateToString(this.mState));
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th22) {
                    throw th22;
                }
            }
            return 0;
        } else if ("unforce".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    exitForceIdleLocked();
                    pw.print("Light state: ");
                    pw.print(lightStateToString(this.mLightState));
                    pw.print(", deep state: ");
                    pw.println(stateToString(this.mState));
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th222) {
                    throw th222;
                }
            }
            return 0;
        } else if ("get".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    arg = shell.getNextArg();
                    if (arg != null) {
                        token = Binder.clearCallingIdentity();
                        if (arg.equals("light")) {
                            pw.println(lightStateToString(this.mLightState));
                        } else if (arg.equals("deep")) {
                            pw.println(stateToString(this.mState));
                        } else if (arg.equals("force")) {
                            pw.println(this.mForceIdle);
                        } else if (arg.equals("screen")) {
                            pw.println(this.mScreenOn);
                        } else if (arg.equals("charging")) {
                            pw.println(this.mCharging);
                        } else if (arg.equals("network")) {
                            pw.println(this.mNetworkConnected);
                        } else {
                            pw.println("Unknown get option: " + arg);
                        }
                        Binder.restoreCallingIdentity(token);
                    } else {
                        pw.println("Argument required");
                    }
                } catch (Throwable th2222) {
                    throw th2222;
                }
            }
            return 0;
        } else if ("disable".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    arg = shell.getNextArg();
                    boolean becomeActive = false;
                    valid = false;
                    if (arg != null) {
                        if (!"deep".equals(arg)) {
                        }
                    }
                    valid = true;
                    if (this.mDeepEnabled) {
                        this.mDeepEnabled = false;
                        becomeActive = true;
                        this.mDeepIdleAccordingToRus = false;
                        pw.println("Deep idle mode disabled");
                    }
                    if (arg == null || "light".equals(arg) || "all".equals(arg)) {
                        valid = true;
                        if (this.mLightEnabled) {
                            this.mLightEnabled = false;
                            becomeActive = true;
                            this.mLightIdleAccordingToRus = false;
                            pw.println("Light idle mode disabled");
                        }
                    }
                    if (becomeActive) {
                        String str;
                        StringBuilder stringBuilder = new StringBuilder();
                        if (arg == null) {
                            str = "all";
                        } else {
                            str = arg;
                        }
                        becomeActiveLocked(stringBuilder.append(str).append("-disabled").toString(), Process.myUid());
                    }
                    if (!valid) {
                        pw.println("Unknown idle mode: " + arg);
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th22222) {
                    throw th22222;
                }
            }
            return 0;
        } else if ("enable".equals(cmd)) {
            getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (this) {
                try {
                    token = Binder.clearCallingIdentity();
                    arg = shell.getNextArg();
                    boolean becomeInactive = false;
                    valid = false;
                    if (arg != null) {
                        if (!"deep".equals(arg)) {
                        }
                    }
                    valid = true;
                    if (!this.mDeepEnabled) {
                        this.mDeepEnabled = true;
                        becomeInactive = true;
                        this.mDeepIdleAccordingToRus = false;
                        pw.println("Deep idle mode enabled");
                    }
                    if (arg == null || "light".equals(arg) || "all".equals(arg)) {
                        valid = true;
                        if (!this.mLightEnabled) {
                            this.mLightEnabled = true;
                            becomeInactive = true;
                            this.mLightIdleAccordingToRus = false;
                            pw.println("Light idle mode enable");
                        }
                    }
                    if (becomeInactive) {
                        becomeInactiveIfAppropriateLocked();
                    }
                    if (!valid) {
                        pw.println("Unknown idle mode: " + arg);
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th222222) {
                    throw th222222;
                }
            }
            return 0;
        } else if ("enabled".equals(cmd)) {
            synchronized (this) {
                try {
                    arg = shell.getNextArg();
                    if (arg == null || "all".equals(arg)) {
                        Object valueOf = (this.mDeepEnabled && this.mLightEnabled) ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : Integer.valueOf(0);
                        pw.println(valueOf);
                    } else if ("deep".equals(arg)) {
                        pw.println(this.mDeepEnabled ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : Integer.valueOf(0));
                    } else if ("light".equals(arg)) {
                        pw.println(this.mLightEnabled ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : Integer.valueOf(0));
                    } else {
                        pw.println("Unknown idle mode: " + arg);
                    }
                } catch (Throwable th2222222) {
                    throw th2222222;
                }
            }
            return 0;
        } else {
            if ("whitelist".equals(cmd)) {
                arg = shell.getNextArg();
                if (arg != null) {
                    getContext().enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                    token = Binder.clearCallingIdentity();
                    do {
                        try {
                            if (arg.length() < 1 || !(arg.charAt(0) == '-' || arg.charAt(0) == '+' || arg.charAt(0) == '=')) {
                                pw.println("Package must be prefixed with +, -, or =: " + arg);
                                Binder.restoreCallingIdentity(token);
                                return -1;
                            }
                            char op = arg.charAt(0);
                            String pkg = arg.substring(1);
                            if (op == '+') {
                                if (addPowerSaveWhitelistAppInternal(pkg)) {
                                    pw.println("Added: " + pkg);
                                } else {
                                    pw.println("Unknown package: " + pkg);
                                }
                            } else if (op != '-') {
                                pw.println(getPowerSaveWhitelistAppInternal(pkg));
                            } else if (removePowerSaveWhitelistAppInternal(pkg)) {
                                pw.println("Removed: " + pkg);
                            }
                            arg = shell.getNextArg();
                        } catch (Throwable th22222222) {
                            Binder.restoreCallingIdentity(token);
                            throw th22222222;
                        }
                    } while (arg != null);
                    Binder.restoreCallingIdentity(token);
                } else {
                    synchronized (this) {
                        int j = 0;
                        while (j < this.mPowerSaveWhitelistAppsExceptIdle.size()) {
                            try {
                                pw.print("system-excidle,");
                                pw.print((String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(j));
                                pw.print(",");
                                pw.println(this.mPowerSaveWhitelistAppsExceptIdle.valueAt(j));
                                j++;
                            } catch (Throwable th222222222) {
                                throw th222222222;
                            }
                        }
                        for (j = 0; j < this.mPowerSaveWhitelistApps.size(); j++) {
                            pw.print("system,");
                            pw.print((String) this.mPowerSaveWhitelistApps.keyAt(j));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistApps.valueAt(j));
                        }
                        for (j = 0; j < this.mPowerSaveWhitelistUserApps.size(); j++) {
                            pw.print("user,");
                            pw.print((String) this.mPowerSaveWhitelistUserApps.keyAt(j));
                            pw.print(",");
                            pw.println(this.mPowerSaveWhitelistUserApps.valueAt(j));
                        }
                    }
                }
            } else if ("tempwhitelist".equals(cmd)) {
                while (true) {
                    String opt = shell.getNextOption();
                    if (opt == null) {
                        arg = shell.getNextArg();
                        if (arg != null) {
                            try {
                                addPowerSaveTempWhitelistAppChecked(arg, 10000, shell.userId, "shell");
                            } catch (RemoteException re) {
                                pw.println("Failed: " + re);
                            }
                        } else {
                            dumpTempWhitelistSchedule(pw, false);
                        }
                    } else if ("-u".equals(opt)) {
                        opt = shell.getNextArg();
                        if (opt == null) {
                            pw.println("-u requires a user number");
                            return -1;
                        }
                        shell.userId = Integer.parseInt(opt);
                    }
                }
            } else if ("motiondetectlog".equals(cmd)) {
                arg = shell.getNextArg();
                if (arg == null || arg.length() < 1) {
                    pw.println("At leat enable/disable must be specified");
                    dumpHelp(pw);
                    return -1;
                }
                token = Binder.clearCallingIdentity();
                try {
                    if ("enable".equals(arg)) {
                        AnyMotionDetector.DEBUG = true;
                        pw.println("motiondetect log enabled");
                    } else if ("disable".equals(arg)) {
                        AnyMotionDetector.DEBUG = false;
                        pw.println("motiondetect log disableed");
                    }
                    Binder.restoreCallingIdentity(token);
                    Slog.d(TAG, "motiondetectlog DEBUG=" + AnyMotionDetector.DEBUG);
                } catch (Throwable th2222222222) {
                    Binder.restoreCallingIdentity(token);
                    throw th2222222222;
                }
            } else if ("networkpolicylog".equals(cmd)) {
                arg = shell.getNextArg();
                if (arg == null || arg.length() < 1) {
                    pw.println("At leat enable/disable must be specified");
                    dumpHelp(pw);
                    return -1;
                }
                token = Binder.clearCallingIdentity();
                try {
                    if ("enable".equals(arg)) {
                        try {
                            this.mNetworkPolicyManager.setDebugModeEnabled();
                            pw.println("network policy log enabled");
                        } catch (RemoteException e) {
                        }
                    } else if ("disable".equals(arg)) {
                        try {
                            this.mNetworkPolicyManager.setDebugModeDisabled();
                            pw.println("network policy log disableed");
                        } catch (RemoteException e2) {
                        }
                    }
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th22222222222) {
                    Binder.restoreCallingIdentity(token);
                    throw th22222222222;
                }
            } else if ("deviceidlelog".equals(cmd)) {
                arg = shell.getNextArg();
                if (arg == null || arg.length() < 1) {
                    pw.println("At leat enable/disable must be specified");
                    dumpHelp(pw);
                    return -1;
                }
                token = Binder.clearCallingIdentity();
                try {
                    if ("enable".equals(arg)) {
                        DEBUG = true;
                        pw.println("device idle log enabled");
                    } else if ("disable".equals(arg)) {
                        DEBUG = false;
                        pw.println("device idle log disableed");
                    }
                    Binder.restoreCallingIdentity(token);
                    Slog.d(TAG, "deviceidlelog DEBUG=" + DEBUG);
                } catch (Throwable th222222222222) {
                    Binder.restoreCallingIdentity(token);
                    throw th222222222222;
                }
            } else if (!"deviceidleoppolog".equals(cmd)) {
                return shell.handleDefaultCommands(cmd);
            } else {
                arg = shell.getNextArg();
                if (arg == null || arg.length() < 1) {
                    pw.println("At leat enable/disable must be specified");
                    dumpHelp(pw);
                    return -1;
                }
                token = Binder.clearCallingIdentity();
                try {
                    if ("enable".equals(arg)) {
                        DEBUG_OPPO = true;
                        pw.println("device idle log enabled");
                    } else if ("disable".equals(arg)) {
                        DEBUG_OPPO = SystemProperties.getBoolean("persist.sys.assert.panic", false);
                        pw.println("device idle log reset");
                    }
                    Binder.restoreCallingIdentity(token);
                    Slog.d(TAG, "deviceidleoppolog DEBUG=" + DEBUG_OPPO);
                } catch (Throwable th2222222222222) {
                    Binder.restoreCallingIdentity(token);
                    throw th2222222222222;
                }
            }
            return 0;
        }
        return 0;
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump DeviceIdleController from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        int i;
        if (args != null) {
            int userId = 0;
            i = 0;
            while (i < args.length) {
                String arg = args[i];
                if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                }
                if ("-u".equals(arg)) {
                    i++;
                    if (i < args.length) {
                        userId = Integer.parseInt(args[i]);
                    }
                } else if (!"-a".equals(arg)) {
                    if (arg.length() > 0 && arg.charAt(0) == '-') {
                        pw.println("Unknown option: " + arg);
                        return;
                    } else if ("oppo-whitelist".equals(arg)) {
                        if (this.mDeviceIdleHelper != null) {
                            this.mDeviceIdleHelper.dump(pw);
                        }
                        return;
                    } else {
                        Shell shell = new Shell();
                        shell.userId = userId;
                        String[] newArgs = new String[(args.length - i)];
                        System.arraycopy(args, i, newArgs, 0, args.length - i);
                        shell.exec(this.mBinderService, null, fd, null, newArgs, new ResultReceiver(null));
                        return;
                    }
                }
                i++;
            }
        }
        synchronized (this) {
            this.mConstants.dump(pw);
            if (this.mEventCmds[0] != 0) {
                pw.println("  Idling history:");
                long now = SystemClock.elapsedRealtime();
                for (i = 99; i >= 0; i--) {
                    if (this.mEventCmds[i] != 0) {
                        String label;
                        switch (this.mEventCmds[i]) {
                            case 1:
                                label = "     normal";
                                break;
                            case 2:
                                label = " light-idle";
                                break;
                            case 3:
                                label = "light-maint";
                                break;
                            case 4:
                                label = "  deep-idle";
                                break;
                            case 5:
                                label = " deep-maint";
                                break;
                            default:
                                label = "         ??";
                                break;
                        }
                        pw.print("    ");
                        pw.print(label);
                        pw.print(": ");
                        TimeUtils.formatDuration(this.mEventTimes[i], now, pw);
                        pw.println();
                    }
                }
            }
            int size = this.mPowerSaveWhitelistAppsExceptIdle.size();
            if (size > 0) {
                pw.println("  Whitelist (except idle) system apps:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistAppsExceptIdle.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistApps.size();
            if (size > 0) {
                pw.println("  Whitelist system apps:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistApps.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistUserApps.size();
            if (size > 0) {
                pw.println("  Whitelist user apps:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.println((String) this.mPowerSaveWhitelistUserApps.keyAt(i));
                }
            }
            size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist (except idle) all app ids:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i));
                    pw.println();
                }
            }
            size = this.mPowerSaveWhitelistUserAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist user app ids:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistUserAppIds.keyAt(i));
                    pw.println();
                }
            }
            size = this.mPowerSaveWhitelistAllAppIds.size();
            if (size > 0) {
                pw.println("  Whitelist all app ids:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.print(this.mPowerSaveWhitelistAllAppIds.keyAt(i));
                    pw.println();
                }
            }
            dumpTempWhitelistSchedule(pw, true);
            size = this.mTempWhitelistAppIdArray != null ? this.mTempWhitelistAppIdArray.length : 0;
            if (size > 0) {
                pw.println("  Temp whitelist app ids:");
                for (i = 0; i < size; i++) {
                    pw.print("    ");
                    pw.print(this.mTempWhitelistAppIdArray[i]);
                    pw.println();
                }
            }
            if (this.mDeviceIdleHelper != null) {
                if (this.mLightIdleAccordingToRus) {
                    this.mLightEnabled = this.mDeviceIdleHelper.isAutoPowerModesEnabled();
                }
                if (this.mDeepIdleAccordingToRus) {
                    this.mDeepEnabled = this.mDeviceIdleHelper.isAutoPowerModesEnabled();
                }
            }
            pw.print("  mLightEnabled=");
            pw.print(this.mLightEnabled);
            pw.print("  mDeepEnabled=");
            pw.println(this.mDeepEnabled);
            pw.print("  mForceIdle=");
            pw.println(this.mForceIdle);
            pw.print("  mMotionSensor=");
            pw.println(this.mMotionSensor);
            pw.print("  mCurDisplay=");
            pw.println(this.mCurDisplay);
            pw.print("  mScreenOn=");
            pw.println(this.mScreenOn);
            pw.print("  mNetworkConnected=");
            pw.println(this.mNetworkConnected);
            pw.print("  mCharging=");
            pw.println(this.mCharging);
            pw.print("  mMotionActive=");
            pw.println(this.mMotionListener.active);
            pw.print("  mNotMoving=");
            pw.println(this.mNotMoving);
            pw.print("  mLocating=");
            pw.print(this.mLocating);
            pw.print(" mHasGps=");
            pw.print(this.mHasGps);
            pw.print(" mHasNetwork=");
            pw.print(this.mHasNetworkLocation);
            pw.print(" mLocated=");
            pw.println(this.mLocated);
            if (this.mLastGenericLocation != null) {
                pw.print("  mLastGenericLocation=");
                pw.println(this.mLastGenericLocation);
            }
            if (this.mLastGpsLocation != null) {
                pw.print("  mLastGpsLocation=");
                pw.println(this.mLastGpsLocation);
            }
            pw.print("  mState=");
            pw.print(stateToString(this.mState));
            pw.print(" mLightState=");
            pw.println(lightStateToString(this.mLightState));
            pw.print("  mInactiveTimeout=");
            TimeUtils.formatDuration(this.mInactiveTimeout, pw);
            pw.println();
            if (this.mActiveIdleOpCount != 0) {
                pw.print("  mActiveIdleOpCount=");
                pw.println(this.mActiveIdleOpCount);
            }
            if (this.mNextAlarmTime != 0) {
                pw.print("  mNextAlarmTime=");
                TimeUtils.formatDuration(this.mNextAlarmTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mNextIdlePendingDelay != 0) {
                pw.print("  mNextIdlePendingDelay=");
                TimeUtils.formatDuration(this.mNextIdlePendingDelay, pw);
                pw.println();
            }
            if (this.mNextIdleDelay != 0) {
                pw.print("  mNextIdleDelay=");
                TimeUtils.formatDuration(this.mNextIdleDelay, pw);
                pw.println();
            }
            if (this.mNextLightIdleDelay != 0) {
                pw.print("  mNextIdleDelay=");
                TimeUtils.formatDuration(this.mNextLightIdleDelay, pw);
                pw.println();
            }
            if (this.mNextLightAlarmTime != 0) {
                pw.print("  mNextLightAlarmTime=");
                TimeUtils.formatDuration(this.mNextLightAlarmTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mCurIdleBudget != 0) {
                pw.print("  mCurIdleBudget=");
                TimeUtils.formatDuration(this.mCurIdleBudget, pw);
                pw.println();
            }
            if (this.mMaintenanceStartTime != 0) {
                pw.print("  mMaintenanceStartTime=");
                TimeUtils.formatDuration(this.mMaintenanceStartTime, SystemClock.elapsedRealtime(), pw);
                pw.println();
            }
            if (this.mJobsActive) {
                pw.print("  mJobsActive=");
                pw.println(this.mJobsActive);
            }
            if (this.mAlarmsActive) {
                pw.print("  mAlarmsActive=");
                pw.println(this.mAlarmsActive);
            }
        }
    }

    void dumpTempWhitelistSchedule(PrintWriter pw, boolean printTitle) {
        int size = this.mTempWhitelistAppIdEndTimes.size();
        if (size > 0) {
            String prefix = IElsaManager.EMPTY_PACKAGE;
            if (printTitle) {
                pw.println("  Temp whitelist schedule:");
                prefix = "    ";
            }
            long timeNow = SystemClock.elapsedRealtime();
            for (int i = 0; i < size; i++) {
                pw.print(prefix);
                pw.print("UID=");
                pw.print(this.mTempWhitelistAppIdEndTimes.keyAt(i));
                pw.print(": ");
                Pair<MutableLong, String> entry = (Pair) this.mTempWhitelistAppIdEndTimes.valueAt(i);
                TimeUtils.formatDuration(((MutableLong) entry.first).value, timeNow, pw);
                pw.print(" - ");
                pw.println((String) entry.second);
            }
        }
    }

    private IDataShapingManager getDataShapingService() {
        if (this.mDataShapingManager != null) {
            return this.mDataShapingManager;
        }
        IDataShapingManager iDataShapingManager = (IDataShapingManager) ServiceManager.getService("data_shaping");
        this.mDataShapingManager = iDataShapingManager;
        return iDataShapingManager;
    }

    void motionDetected(int type) {
        int state = 0;
        if (this.mState == 2) {
            state = 2;
        } else if (this.mState == 3) {
            state = 3;
        } else if (this.mState == 4) {
            state = 4;
        } else if (this.mState == 5) {
            state = 5;
        } else if (this.mState == 6) {
            state = 6;
        }
        if (!(state == 0 || this.mDeviceIdleHelper == null)) {
            this.mDeviceIdleHelper.onMotionDetected(state, type);
        }
    }

    private boolean isDeepInTraffic() {
        if (this.mLastDeepTrafficRecord == null) {
            Slog.d(TAG, "isDeepInTraffic: mLastDeepTrafficRecord is null!");
            return false;
        }
        long nowTraffic = getTotalTraffic();
        long nowElapsed = SystemClock.elapsedRealtime();
        long deltaTraffic = nowTraffic - this.mLastDeepTrafficRecord.mTotalTraffic;
        long deltaTime = (nowElapsed - this.mLastDeepTrafficRecord.mTimeStamp) / 1000;
        if (deltaTime == 0) {
            if (DEBUG_OPPO) {
                Slog.d(TAG, "isDeepInTraffic: deltaTime is zero!");
            }
            return false;
        }
        long speed = (deltaTraffic / 1024) / deltaTime;
        if (DEBUG_OPPO) {
            Slog.d(TAG, "isDeepInTraffic: speed=" + speed + "KB/s. deltaTraffic=" + Formatter.formatFileSize(getContext(), deltaTraffic) + ", deltaTime=" + deltaTime + "s");
        }
        if (speed < THRESH_SPEED) {
            return false;
        }
        this.mLastDeepTrafficRecord = new TrafficRecord(this, nowTraffic, nowElapsed, null);
        return true;
    }

    private boolean isLightInTraffic() {
        if (this.mLastLightTrafficRecord == null) {
            Slog.d(TAG, "isLightInTraffic: mLastLightTrafficRecord is null!");
            return false;
        }
        long nowTraffic = getTotalTraffic();
        long nowElapsed = SystemClock.elapsedRealtime();
        long deltaTraffic = nowTraffic - this.mLastLightTrafficRecord.mTotalTraffic;
        long deltaTime = (nowElapsed - this.mLastLightTrafficRecord.mTimeStamp) / 1000;
        if (deltaTime == 0) {
            if (DEBUG_OPPO) {
                Slog.d(TAG, "isLightInTraffic: deltaTime is zero!");
            }
            return false;
        }
        long speed = (deltaTraffic / 1024) / deltaTime;
        if (DEBUG_OPPO) {
            Slog.d(TAG, "isLightInTraffic: speed=" + speed + "KB/s. deltaTraffic=" + Formatter.formatFileSize(getContext(), deltaTraffic) + ", deltaTime=" + deltaTime + "s");
        }
        if (speed < THRESH_SPEED) {
            return false;
        }
        this.mLastLightTrafficRecord = new TrafficRecord(this, nowTraffic, nowElapsed, null);
        return true;
    }

    private long getTotalTraffic() {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        if (DEBUG_OPPO) {
            Slog.d(TAG, "getTotalTrafficKB: rx=" + Formatter.formatFileSize(getContext(), rx) + ", tx=" + Formatter.formatFileSize(getContext(), tx) + ", total=" + Formatter.formatFileSize(getContext(), rx + tx));
        }
        return rx + tx;
    }
}
