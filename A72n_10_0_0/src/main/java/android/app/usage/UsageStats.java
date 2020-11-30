package android.app.usage;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseIntArray;

public final class UsageStats implements Parcelable {
    public static final Parcelable.Creator<UsageStats> CREATOR = new Parcelable.Creator<UsageStats>() {
        /* class android.app.usage.UsageStats.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UsageStats createFromParcel(Parcel in) {
            UsageStats stats = new UsageStats();
            stats.mPackageName = in.readString();
            stats.mBeginTimeStamp = in.readLong();
            stats.mEndTimeStamp = in.readLong();
            stats.mLastTimeUsed = in.readLong();
            stats.mLastTimeVisible = in.readLong();
            stats.mLastTimeForegroundServiceUsed = in.readLong();
            stats.mTotalTimeInForeground = in.readLong();
            stats.mTotalTimeVisible = in.readLong();
            stats.mTotalTimeForegroundServiceUsed = in.readLong();
            stats.mLaunchCount = in.readInt();
            stats.mAppLaunchCount = in.readInt();
            stats.mErrorCount = in.readInt();
            stats.mLastEvent = in.readInt();
            Bundle allCounts = in.readBundle();
            if (allCounts != null) {
                stats.mChooserCounts = new ArrayMap<>();
                for (String action : allCounts.keySet()) {
                    if (!stats.mChooserCounts.containsKey(action)) {
                        stats.mChooserCounts.put(action, new ArrayMap<>());
                    }
                    Bundle currentCounts = allCounts.getBundle(action);
                    if (currentCounts != null) {
                        for (String key : currentCounts.keySet()) {
                            int value = currentCounts.getInt(key);
                            if (value > 0) {
                                stats.mChooserCounts.get(action).put(key, Integer.valueOf(value));
                            }
                        }
                    }
                }
            }
            readSparseIntArray(in, stats.mActivities);
            readBundleToEventMap(in.readBundle(), stats.mForegroundServices);
            return stats;
        }

        private void readSparseIntArray(Parcel in, SparseIntArray arr) {
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                arr.put(in.readInt(), in.readInt());
            }
        }

        private void readBundleToEventMap(Bundle bundle, ArrayMap<String, Integer> eventMap) {
            if (bundle != null) {
                for (String className : bundle.keySet()) {
                    eventMap.put(className, Integer.valueOf(bundle.getInt(className)));
                }
            }
        }

        @Override // android.os.Parcelable.Creator
        public UsageStats[] newArray(int size) {
            return new UsageStats[size];
        }
    };
    public SparseIntArray mActivities = new SparseIntArray();
    public int mAppLaunchCount;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mBeginTimeStamp;
    public ArrayMap<String, ArrayMap<String, Integer>> mChooserCounts = new ArrayMap<>();
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mEndTimeStamp;
    public int mErrorCount;
    public ArrayMap<String, Integer> mForegroundServices = new ArrayMap<>();
    @UnsupportedAppUsage
    @Deprecated
    public int mLastEvent;
    public long mLastTimeForegroundServiceUsed;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public long mLastTimeUsed;
    public long mLastTimeVisible;
    @UnsupportedAppUsage
    public int mLaunchCount;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public String mPackageName;
    public long mTotalTimeForegroundServiceUsed;
    @UnsupportedAppUsage
    public long mTotalTimeInForeground;
    public long mTotalTimeVisible;

    public UsageStats() {
    }

    public UsageStats(UsageStats stats) {
        this.mPackageName = stats.mPackageName;
        this.mBeginTimeStamp = stats.mBeginTimeStamp;
        this.mEndTimeStamp = stats.mEndTimeStamp;
        this.mLastTimeUsed = stats.mLastTimeUsed;
        this.mLastTimeVisible = stats.mLastTimeVisible;
        this.mLastTimeForegroundServiceUsed = stats.mLastTimeForegroundServiceUsed;
        this.mTotalTimeInForeground = stats.mTotalTimeInForeground;
        this.mTotalTimeVisible = stats.mTotalTimeVisible;
        this.mTotalTimeForegroundServiceUsed = stats.mTotalTimeForegroundServiceUsed;
        this.mLaunchCount = stats.mLaunchCount;
        this.mAppLaunchCount = stats.mAppLaunchCount;
        this.mLastEvent = stats.mLastEvent;
        this.mActivities = stats.mActivities;
        this.mForegroundServices = stats.mForegroundServices;
        this.mChooserCounts = stats.mChooserCounts;
        this.mErrorCount = stats.mErrorCount;
    }

    public UsageStats getObfuscatedForInstantApp() {
        UsageStats ret = new UsageStats(this);
        ret.mPackageName = UsageEvents.INSTANT_APP_PACKAGE_NAME;
        return ret;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getFirstTimeStamp() {
        return this.mBeginTimeStamp;
    }

    public long getLastTimeStamp() {
        return this.mEndTimeStamp;
    }

    public long getLastTimeUsed() {
        return this.mLastTimeUsed;
    }

    public long getLastTimeVisible() {
        return this.mLastTimeVisible;
    }

    public long getTotalTimeInForeground() {
        return this.mTotalTimeInForeground;
    }

    public long getTotalTimeVisible() {
        return this.mTotalTimeVisible;
    }

    public long getLastTimeForegroundServiceUsed() {
        return this.mLastTimeForegroundServiceUsed;
    }

    public long getErrorCount() {
        return (long) this.mErrorCount;
    }

    public long getTotalTimeForegroundServiceUsed() {
        return this.mTotalTimeForegroundServiceUsed;
    }

    @SystemApi
    public int getAppLaunchCount() {
        return this.mAppLaunchCount;
    }

    private void mergeEventMap(SparseIntArray left, SparseIntArray right) {
        int size = right.size();
        for (int i = 0; i < size; i++) {
            int instanceId = right.keyAt(i);
            int event = right.valueAt(i);
            int index = left.indexOfKey(instanceId);
            if (index >= 0) {
                left.put(instanceId, Math.max(left.valueAt(index), event));
            } else {
                left.put(instanceId, event);
            }
        }
    }

    private void mergeEventMap(ArrayMap<String, Integer> left, ArrayMap<String, Integer> right) {
        int size = right.size();
        for (int i = 0; i < size; i++) {
            String className = right.keyAt(i);
            Integer event = right.valueAt(i);
            if (left.containsKey(className)) {
                left.put(className, Integer.valueOf(Math.max(left.get(className).intValue(), event.intValue())));
            } else {
                left.put(className, event);
            }
        }
    }

    public void add(UsageStats right) {
        if (this.mPackageName.equals(right.mPackageName)) {
            if (right.mBeginTimeStamp > this.mBeginTimeStamp) {
                mergeEventMap(this.mActivities, right.mActivities);
                mergeEventMap(this.mForegroundServices, right.mForegroundServices);
                this.mLastTimeUsed = Math.max(this.mLastTimeUsed, right.mLastTimeUsed);
                this.mLastTimeVisible = Math.max(this.mLastTimeVisible, right.mLastTimeVisible);
                this.mLastTimeForegroundServiceUsed = Math.max(this.mLastTimeForegroundServiceUsed, right.mLastTimeForegroundServiceUsed);
            }
            this.mBeginTimeStamp = Math.min(this.mBeginTimeStamp, right.mBeginTimeStamp);
            this.mEndTimeStamp = Math.max(this.mEndTimeStamp, right.mEndTimeStamp);
            this.mTotalTimeInForeground += right.mTotalTimeInForeground;
            this.mTotalTimeVisible += right.mTotalTimeVisible;
            this.mTotalTimeForegroundServiceUsed += right.mTotalTimeForegroundServiceUsed;
            this.mLaunchCount += right.mLaunchCount;
            this.mAppLaunchCount += right.mAppLaunchCount;
            this.mErrorCount += right.mErrorCount;
            if (this.mChooserCounts == null) {
                this.mChooserCounts = right.mChooserCounts;
                return;
            }
            ArrayMap<String, ArrayMap<String, Integer>> arrayMap = right.mChooserCounts;
            if (arrayMap != null) {
                int chooserCountsSize = arrayMap.size();
                for (int i = 0; i < chooserCountsSize; i++) {
                    String action = right.mChooserCounts.keyAt(i);
                    ArrayMap<String, Integer> counts = right.mChooserCounts.valueAt(i);
                    if (!this.mChooserCounts.containsKey(action) || this.mChooserCounts.get(action) == null) {
                        this.mChooserCounts.put(action, counts);
                    } else {
                        int annotationSize = counts.size();
                        for (int j = 0; j < annotationSize; j++) {
                            String key = counts.keyAt(j);
                            this.mChooserCounts.get(action).put(key, Integer.valueOf(this.mChooserCounts.get(action).getOrDefault(key, 0).intValue() + counts.valueAt(j).intValue()));
                        }
                    }
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Can't merge UsageStats for package '" + this.mPackageName + "' with UsageStats for package '" + right.mPackageName + "'.");
    }

    private boolean hasForegroundActivity() {
        int size = this.mActivities.size();
        for (int i = 0; i < size; i++) {
            if (this.mActivities.valueAt(i) == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisibleActivity() {
        int size = this.mActivities.size();
        for (int i = 0; i < size; i++) {
            int type = this.mActivities.valueAt(i);
            if (type == 1 || type == 2) {
                return true;
            }
        }
        return false;
    }

    private boolean anyForegroundServiceStarted() {
        return !this.mForegroundServices.isEmpty();
    }

    private void incrementTimeUsed(long timeStamp) {
        long j = this.mLastTimeUsed;
        if (timeStamp > j) {
            this.mTotalTimeInForeground += timeStamp - j;
            this.mLastTimeUsed = timeStamp;
        }
    }

    private void incrementTimeVisible(long timeStamp) {
        long j = this.mLastTimeVisible;
        if (timeStamp > j) {
            this.mTotalTimeVisible += timeStamp - j;
            this.mLastTimeVisible = timeStamp;
        }
    }

    private void incrementServiceTimeUsed(long timeStamp) {
        long j = this.mLastTimeForegroundServiceUsed;
        if (timeStamp > j) {
            this.mTotalTimeForegroundServiceUsed += timeStamp - j;
            this.mLastTimeForegroundServiceUsed = timeStamp;
        }
    }

    private void updateActivity(String className, long timeStamp, int eventType, int instanceId) {
        if (eventType == 1 || eventType == 2 || eventType == 23 || eventType == 24) {
            int index = this.mActivities.indexOfKey(instanceId);
            if (index >= 0) {
                int lastEvent = this.mActivities.valueAt(index);
                if (lastEvent == 1) {
                    incrementTimeUsed(timeStamp);
                    incrementTimeVisible(timeStamp);
                } else if (lastEvent == 2) {
                    incrementTimeVisible(timeStamp);
                }
            }
            if (eventType == 1) {
                if (!hasVisibleActivity()) {
                    this.mLastTimeUsed = timeStamp;
                    this.mLastTimeVisible = timeStamp;
                } else if (!hasForegroundActivity()) {
                    this.mLastTimeUsed = timeStamp;
                }
                this.mActivities.put(instanceId, eventType);
            } else if (eventType == 2) {
                if (!hasVisibleActivity()) {
                    this.mLastTimeVisible = timeStamp;
                }
                this.mActivities.put(instanceId, eventType);
            } else if (eventType == 23 || eventType == 24) {
                this.mActivities.delete(instanceId);
            }
        }
    }

    private void updateForegroundService(String className, long timeStamp, int eventType) {
        int intValue;
        if (eventType == 20 || eventType == 19) {
            Integer lastEvent = this.mForegroundServices.get(className);
            if (lastEvent != null && ((intValue = lastEvent.intValue()) == 19 || intValue == 21)) {
                incrementServiceTimeUsed(timeStamp);
            }
            if (eventType == 19) {
                if (!anyForegroundServiceStarted()) {
                    this.mLastTimeForegroundServiceUsed = timeStamp;
                }
                this.mForegroundServices.put(className, Integer.valueOf(eventType));
            } else if (eventType == 20) {
                this.mForegroundServices.remove(className);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:32:? A[RETURN, SYNTHETIC] */
    public void update(String className, long timeStamp, int eventType, int instanceId) {
        if (!(eventType == 1 || eventType == 2)) {
            if (eventType != 3) {
                switch (eventType) {
                    case 19:
                    case 20:
                        updateForegroundService(className, timeStamp, eventType);
                        break;
                    case 21:
                        this.mLastTimeForegroundServiceUsed = timeStamp;
                        this.mForegroundServices.put(className, Integer.valueOf(eventType));
                        break;
                    case 22:
                        if (anyForegroundServiceStarted()) {
                            incrementServiceTimeUsed(timeStamp);
                            break;
                        }
                        break;
                    case 25:
                    case 26:
                        if (hasForegroundActivity()) {
                            incrementTimeUsed(timeStamp);
                        }
                        if (hasVisibleActivity()) {
                            incrementTimeVisible(timeStamp);
                        }
                        if (anyForegroundServiceStarted()) {
                            incrementServiceTimeUsed(timeStamp);
                            break;
                        }
                        break;
                }
            } else {
                if (hasForegroundActivity()) {
                    incrementTimeUsed(timeStamp);
                }
                if (hasVisibleActivity()) {
                    incrementTimeVisible(timeStamp);
                }
            }
            this.mEndTimeStamp = timeStamp;
            if (eventType != 1) {
                this.mLaunchCount++;
                return;
            }
            return;
        }
        updateActivity(className, timeStamp, eventType, instanceId);
        this.mEndTimeStamp = timeStamp;
        if (eventType != 1) {
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mBeginTimeStamp);
        dest.writeLong(this.mEndTimeStamp);
        dest.writeLong(this.mLastTimeUsed);
        dest.writeLong(this.mLastTimeVisible);
        dest.writeLong(this.mLastTimeForegroundServiceUsed);
        dest.writeLong(this.mTotalTimeInForeground);
        dest.writeLong(this.mTotalTimeVisible);
        dest.writeLong(this.mTotalTimeForegroundServiceUsed);
        dest.writeInt(this.mLaunchCount);
        dest.writeInt(this.mAppLaunchCount);
        dest.writeInt(this.mErrorCount);
        dest.writeInt(this.mLastEvent);
        Bundle allCounts = new Bundle();
        ArrayMap<String, ArrayMap<String, Integer>> arrayMap = this.mChooserCounts;
        if (arrayMap != null) {
            int chooserCountSize = arrayMap.size();
            for (int i = 0; i < chooserCountSize; i++) {
                String action = this.mChooserCounts.keyAt(i);
                ArrayMap<String, Integer> counts = this.mChooserCounts.valueAt(i);
                Bundle currentCounts = new Bundle();
                int annotationSize = counts.size();
                for (int j = 0; j < annotationSize; j++) {
                    currentCounts.putInt(counts.keyAt(j), counts.valueAt(j).intValue());
                }
                allCounts.putBundle(action, currentCounts);
            }
        }
        dest.writeBundle(allCounts);
        writeSparseIntArray(dest, this.mActivities);
        dest.writeBundle(eventMapToBundle(this.mForegroundServices));
    }

    private void writeSparseIntArray(Parcel dest, SparseIntArray arr) {
        int size = arr.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeInt(arr.keyAt(i));
            dest.writeInt(arr.valueAt(i));
        }
    }

    private Bundle eventMapToBundle(ArrayMap<String, Integer> eventMap) {
        Bundle bundle = new Bundle();
        int size = eventMap.size();
        for (int i = 0; i < size; i++) {
            bundle.putInt(eventMap.keyAt(i), eventMap.valueAt(i).intValue());
        }
        return bundle;
    }
}
