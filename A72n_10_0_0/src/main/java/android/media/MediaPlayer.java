package android.media;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioRouting;
import android.media.MediaDrm;
import android.media.MediaTimeProvider;
import android.media.SubtitleController;
import android.media.SubtitleTrack;
import android.media.VolumeShaper;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telephony.SmsManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import android.util.TimeUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.Preconditions;
import com.oppo.atlas.OppoAtlasManager;
import com.oppo.media.OppoMediaPlayer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import libcore.io.IoBridge;
import libcore.io.Streams;

public class MediaPlayer extends PlayerBase implements SubtitleController.Listener, VolumeAutomation, AudioRouting {
    public static final boolean APPLY_METADATA_FILTER = true;
    public static final String BROADCAST_ACTION = "android.intent.action.video_resulotion";
    @UnsupportedAppUsage
    public static final boolean BYPASS_METADATA_FILTER = false;
    private static final boolean DEBUG = (!"user".equals(Build.TYPE));
    private static final String IMEDIA_PLAYER = "android.media.IMediaPlayer";
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE = 2;
    private static final int INVOKE_ID_ADD_EXTERNAL_SOURCE_FD = 3;
    private static final int INVOKE_ID_DESELECT_TRACK = 5;
    private static final int INVOKE_ID_GET_SELECTED_TRACK = 7;
    private static final int INVOKE_ID_GET_TRACK_INFO = 1;
    private static final int INVOKE_ID_SELECT_TRACK = 4;
    private static final int INVOKE_ID_SET_VIDEO_SCALE_MODE = 6;
    private static final int KEY_PARAMETER_AUDIO_ATTRIBUTES = 1400;
    private static final int KEY_PARAMETER_STREAMTYPE_CHANGE = 10111;
    private static final int MEDIA_AUDIO_ROUTING_CHANGED = 10000;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_DRM_INFO = 210;
    private static final int MEDIA_ERROR = 100;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_SYSTEM = Integer.MIN_VALUE;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    private static final int MEDIA_INFO = 200;
    public static final int MEDIA_INFO_AUDIO_NOT_PLAYING = 804;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    @UnsupportedAppUsage
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    @UnsupportedAppUsage
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_NOT_PLAYING = 805;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    private static final int MEDIA_META_DATA = 202;
    public static final String MEDIA_MIMETYPE_TEXT_CEA_608 = "text/cea-608";
    public static final String MEDIA_MIMETYPE_TEXT_CEA_708 = "text/cea-708";
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final String MEDIA_MIMETYPE_TEXT_VTT = "text/vtt";
    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_NOTIFY_TIME = 98;
    private static final int MEDIA_PAUSED = 7;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_SKIPPED = 9;
    private static final int MEDIA_STARTED = 6;
    private static final int MEDIA_STOPPED = 8;
    private static final int MEDIA_SUBTITLE_DATA = 201;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_TIME_DISCONTINUITY = 211;
    @UnsupportedAppUsage
    public static final boolean METADATA_ALL = false;
    public static final boolean METADATA_UPDATE_ONLY = true;
    public static final int PLAYBACK_RATE_AUDIO_MODE_DEFAULT = 0;
    public static final int PLAYBACK_RATE_AUDIO_MODE_RESAMPLE = 2;
    public static final int PLAYBACK_RATE_AUDIO_MODE_STRETCH = 1;
    public static final int PREPARE_DRM_STATUS_PREPARATION_ERROR = 3;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_NETWORK_ERROR = 1;
    public static final int PREPARE_DRM_STATUS_PROVISIONING_SERVER_ERROR = 2;
    public static final int PREPARE_DRM_STATUS_SUCCESS = 0;
    public static final int SEEK_CLOSEST = 3;
    public static final int SEEK_CLOSEST_SYNC = 2;
    public static final int SEEK_NEXT_SYNC = 1;
    public static final int SEEK_PREVIOUS_SYNC = 0;
    private static final int STREAMTYPE_CHANGE_FLAG = 1;
    private static final int STREAMTYPE_UNCHANGE_FLAG = 0;
    private static final String TAG = "MediaPlayer";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private boolean mActiveDrmScheme;
    private Context mContext = null;
    private boolean mDrmConfigAllowed;
    private DrmInfo mDrmInfo;
    private boolean mDrmInfoResolved;
    private final Object mDrmLock = new Object();
    private MediaDrm mDrmObj;
    private boolean mDrmProvisioningInProgress;
    private ProvisioningThread mDrmProvisioningThread;
    private byte[] mDrmSessionId;
    private UUID mDrmUUID;
    @UnsupportedAppUsage
    private EventHandler mEventHandler;
    private Handler mExtSubtitleDataHandler;
    private OnSubtitleDataListener mExtSubtitleDataListener;
    private BitSet mInbandTrackIndices = new BitSet();
    private Vector<Pair<Integer, SubtitleTrack>> mIndexTrackPairs = new Vector<>();
    private final OnSubtitleDataListener mIntSubtitleDataListener = new OnSubtitleDataListener() {
        /* class android.media.MediaPlayer.AnonymousClass3 */

        @Override // android.media.MediaPlayer.OnSubtitleDataListener
        public void onSubtitleData(MediaPlayer mp, SubtitleData data) {
            int index = data.getTrackIndex();
            synchronized (MediaPlayer.this.mIndexTrackPairs) {
                Iterator it = MediaPlayer.this.mIndexTrackPairs.iterator();
                while (it.hasNext()) {
                    Pair<Integer, SubtitleTrack> p = (Pair) it.next();
                    if (!(p.first == null || p.first.intValue() != index || p.second == null)) {
                        p.second.onData(data);
                    }
                }
            }
        }
    };
    public boolean mIsEncoder = false;
    private int mListenerContext;
    private long mNativeContext;
    private long mNativeSurfaceTexture;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private final OnCompletionListener mOnCompletionInternalListener = new OnCompletionListener() {
        /* class android.media.MediaPlayer.AnonymousClass7 */

        @Override // android.media.MediaPlayer.OnCompletionListener
        public void onCompletion(MediaPlayer mp) {
            MediaPlayer.this.baseStop();
        }
    };
    @UnsupportedAppUsage
    private OnCompletionListener mOnCompletionListener;
    private OnDrmConfigHelper mOnDrmConfigHelper;
    private OnDrmInfoHandlerDelegate mOnDrmInfoHandlerDelegate;
    private OnDrmPreparedHandlerDelegate mOnDrmPreparedHandlerDelegate;
    @UnsupportedAppUsage
    private OnErrorListener mOnErrorListener;
    @UnsupportedAppUsage
    private OnInfoListener mOnInfoListener;
    private Handler mOnMediaTimeDiscontinuityHandler;
    private OnMediaTimeDiscontinuityListener mOnMediaTimeDiscontinuityListener;
    @UnsupportedAppUsage
    private OnPreparedListener mOnPreparedListener;
    @UnsupportedAppUsage
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedMetaDataAvailableListener mOnTimedMetaDataAvailableListener;
    @UnsupportedAppUsage
    private OnTimedTextListener mOnTimedTextListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private Vector<InputStream> mOpenSubtitleSources;
    public OppoMediaPlayer mOppoMediaPlayer = null;
    private AudioDeviceInfo mPreferredDevice = null;
    private boolean mPrepareDrmInProgress;
    @GuardedBy({"mRoutingChangeListeners"})
    private ArrayMap<AudioRouting.OnRoutingChangedListener, NativeRoutingEventHandlerDelegate> mRoutingChangeListeners = new ArrayMap<>();
    private boolean mScreenOnWhilePlaying;
    private int mSelectedSubtitleTrackIndex = -1;
    private boolean mStayAwake;
    private int mStreamType = Integer.MIN_VALUE;
    private SubtitleController mSubtitleController;
    private boolean mSubtitleDataListenerDisabled;
    private SurfaceHolder mSurfaceHolder;
    private TimeProvider mTimeProvider;
    private final Object mTimeProviderLock = new Object();
    private int mUsage = -1;
    public int mVideoHeight = 0;
    public int mVideoWidth = 0;
    private PowerManager.WakeLock mWakeLock = null;

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    public interface OnDrmConfigHelper {
        void onDrmConfig(MediaPlayer mediaPlayer);
    }

    public interface OnDrmInfoListener {
        void onDrmInfo(MediaPlayer mediaPlayer, DrmInfo drmInfo);
    }

    public interface OnDrmPreparedListener {
        void onDrmPrepared(MediaPlayer mediaPlayer, int i);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i, int i2);
    }

    public interface OnMediaTimeDiscontinuityListener {
        void onMediaTimeDiscontinuity(MediaPlayer mediaPlayer, MediaTimestamp mediaTimestamp);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnSubtitleDataListener {
        void onSubtitleData(MediaPlayer mediaPlayer, SubtitleData subtitleData);
    }

    public interface OnTimedMetaDataAvailableListener {
        void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData);
    }

    public interface OnTimedTextListener {
        void onTimedText(MediaPlayer mediaPlayer, TimedText timedText);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PlaybackRateAudioMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrepareDrmStatusCode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekMode {
    }

    private native int _getAudioStreamType() throws IllegalStateException;

    private native void _notifyAt(long j);

    private native void _pause() throws IllegalStateException;

    private native void _prepare() throws IOException, IllegalStateException;

    private native void _prepareDrm(byte[] bArr, byte[] bArr2);

    private native void _release();

    private native void _releaseDrm();

    private native void _reset();

    private final native void _seekTo(long j, int i);

    private native void _setAudioStreamType(int i);

    private native void _setAuxEffectSendLevel(float f);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException, IllegalStateException;

    private native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IOException, IllegalArgumentException, IllegalStateException;

    private native void _setVideoSurface(Surface surface);

    private native void _setVolume(float f, float f2);

    private native void _start() throws IllegalStateException;

    private native void _stop() throws IllegalStateException;

    private native void nativeSetDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    private native int native_applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation);

    private final native void native_enableDeviceCallback(boolean z);

    private final native void native_finalize();

    private final native boolean native_getMetadata(boolean z, boolean z2, Parcel parcel);

    private native PersistableBundle native_getMetrics();

    private final native int native_getRoutedDeviceId();

    private native VolumeShaper.State native_getVolumeShaperState(int i);

    private static final native void native_init();

    private final native int native_invoke(Parcel parcel, Parcel parcel2);

    public static native int native_pullBatteryData(Parcel parcel);

    private final native int native_setMetadataFilter(Parcel parcel);

    private final native boolean native_setOutputDevice(int i);

    private final native int native_setRetransmitEndpoint(String str, int i);

    private final native void native_setup(Object obj);

    @UnsupportedAppUsage
    private native boolean setParameter(int i, Parcel parcel);

    public native void _attachAuxEffect(int i);

    public native int _getAudioSessionId();

    public native int _getCurrentPosition();

    public native int _getDuration();

    public native int _getVideoHeight();

    public native int _getVideoWidth();

    public native boolean _isLooping();

    public native boolean _isPlaying();

    public native void _prepareAsync() throws IllegalStateException;

    public native void _setAudioSessionId(int i) throws IllegalArgumentException, IllegalStateException;

    public native void _setLooping(boolean z);

    public native PlaybackParams getPlaybackParams();

    public native SyncParams getSyncParams();

    public native void setNextMediaPlayer(MediaPlayer mediaPlayer);

    public native void setPlaybackParams(PlaybackParams playbackParams);

    public native void setSyncParams(SyncParams syncParams);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public void send_broadcast(int isplaying, int width, int height) {
        String ctx = String.valueOf(this);
        Context mBcContext = ActivityThread.currentApplication();
        if (mBcContext != null && width > 0 && height > 0) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.video_resulotion");
            intent.putExtra("width", width);
            intent.putExtra("height", height);
            intent.putExtra("context", ctx);
            intent.putExtra("isplaying", isplaying);
            intent.putExtra("isencoder", this.mIsEncoder);
            intent.putExtra("package", mBcContext.getPackageName());
            mBcContext.sendBroadcast(intent);
        }
    }

    public MediaPlayer() {
        super(new AudioAttributes.Builder().build(), 2);
        Looper looper = Looper.myLooper();
        if (looper != null) {
            this.mEventHandler = new EventHandler(this, looper);
        } else {
            Looper looper2 = Looper.getMainLooper();
            if (looper2 != null) {
                this.mEventHandler = new EventHandler(this, looper2);
            } else {
                this.mEventHandler = null;
            }
        }
        if (this.mOppoMediaPlayer == null) {
            this.mOppoMediaPlayer = new OppoMediaPlayer();
        }
        this.mTimeProvider = new TimeProvider(this);
        this.mOpenSubtitleSources = new Vector<>();
        native_setup(new WeakReference(this));
        baseRegisterPlayer();
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }

    @UnsupportedAppUsage
    public Parcel newRequest() {
        Parcel parcel = Parcel.obtain();
        parcel.writeInterfaceToken(IMEDIA_PLAYER);
        return parcel;
    }

    @UnsupportedAppUsage
    public void invoke(Parcel request, Parcel reply) {
        int retcode = native_invoke(request, reply);
        reply.setDataPosition(0);
        if (retcode != 0) {
            throw new RuntimeException("failure code: " + retcode);
        }
    }

    public void setDisplay(SurfaceHolder sh) {
        Surface surface;
        this.mSurfaceHolder = sh;
        if (!this.mOppoMediaPlayer.setDisplayWithOppoIfNeeded(sh)) {
            this.mSurfaceHolder = sh;
            if (sh != null) {
                surface = sh.getSurface();
            } else {
                surface = null;
            }
            _setVideoSurface(surface);
            updateSurfaceScreenOn();
        }
    }

    public void setSurface(Surface surface) {
        if (!this.mOppoMediaPlayer.setSurfaceWithOppoIfNeeded(surface)) {
            if (this.mScreenOnWhilePlaying && surface != null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface");
            }
            this.mSurfaceHolder = null;
            _setVideoSurface(surface);
            updateSurfaceScreenOn();
        }
    }

    public void setVideoScalingMode(int mode) {
        if (this.mOppoMediaPlayer.setVideoScalingModeWithOppoIfNeeded(mode)) {
            return;
        }
        if (isVideoScalingModeSupported(mode)) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                request.writeInterfaceToken(IMEDIA_PLAYER);
                request.writeInt(6);
                request.writeInt(mode);
                invoke(request, reply);
            } finally {
                request.recycle();
                reply.recycle();
            }
        } else {
            throw new IllegalArgumentException("Scaling mode " + mode + " is not supported");
        }
    }

    public static MediaPlayer create(Context context, Uri uri) {
        return create(context, uri, null);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder) {
        int s = AudioSystem.newAudioSessionId();
        return create(context, uri, holder, null, s > 0 ? s : 0);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder holder, AudioAttributes audioAttributes, int audioSessionId) {
        AudioAttributes aa;
        Log.d(TAG, "MediaPlayer create called");
        try {
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new AudioAttributes.Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
            mp.setDataSource(context, uri);
            if (holder != null) {
                mp.setDisplay(holder);
            }
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public static MediaPlayer create(Context context, int resid) {
        int s = AudioSystem.newAudioSessionId();
        return create(context, resid, null, s > 0 ? s : 0);
    }

    public static MediaPlayer create(Context context, int resid, AudioAttributes audioAttributes, int audioSessionId) {
        AudioAttributes aa;
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(resid);
            if (afd == null) {
                return null;
            }
            MediaPlayer mp = new MediaPlayer();
            if (audioAttributes != null) {
                aa = audioAttributes;
            } else {
                aa = new AudioAttributes.Builder().build();
            }
            mp.setAudioAttributes(aa);
            mp.setAudioSessionId(audioSessionId);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mp.prepare();
            return mp;
        } catch (IOException ex) {
            Log.d(TAG, "create failed:", ex);
            return null;
        } catch (IllegalArgumentException ex2) {
            Log.d(TAG, "create failed:", ex2);
            return null;
        } catch (SecurityException ex3) {
            Log.d(TAG, "create failed:", ex3);
            return null;
        }
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mOppoMediaPlayer.preSetDataSource(context, uri);
        this.mContext = context;
        setDataSource(context, uri, (Map<String, String>) null, (List<HttpCookie>) null);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers, List<HttpCookie> cookies) throws IOException {
        CookieHandler cookieHandler;
        if (context == null) {
            throw new NullPointerException("context param can not be null.");
        } else if (uri == null) {
            throw new NullPointerException("uri param can not be null.");
        } else if (cookies == null || (cookieHandler = CookieHandler.getDefault()) == null || (cookieHandler instanceof CookieManager)) {
            this.mOppoMediaPlayer.preSetDataSource(context, uri, headers);
            this.mContext = context;
            ContentResolver resolver = context.getContentResolver();
            String scheme = uri.getScheme();
            String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
            if (ContentResolver.SCHEME_FILE.equals(scheme) || scheme == null) {
                setDataSource(uri.getPath());
            } else if ("content".equals(scheme) && "settings".equals(authority)) {
                int type = RingtoneManager.getDefaultType(uri);
                Uri cacheUri = RingtoneManager.getCacheForType(type, context.getUserId());
                Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
                if (!attemptDataSource(resolver, cacheUri) && !attemptDataSource(resolver, actualUri)) {
                    setDataSource(uri.toString(), headers, cookies);
                }
            } else if (!attemptDataSource(resolver, uri)) {
                setDataSource(uri.toString(), headers, cookies);
            }
        } else {
            throw new IllegalArgumentException("The cookie handler has to be of CookieManager type when cookies are provided.");
        }
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, headers, (List<HttpCookie>) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        if (r0 != null) goto L_0x0017;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001c, code lost:
        r1 = new java.lang.StringBuilder();
        r1.append("Couldn't open ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        if (r5 == null) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0028, code lost:
        r2 = "null uri";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002c, code lost:
        r2 = r5.toSafeString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0030, code lost:
        r1.append(r2);
        android.util.Log.w(android.media.MediaPlayer.TAG, r1.toString(), r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003d, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x001b A[ExcHandler: IOException | NullPointerException | SecurityException (r0v2 'ex' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:5:0x000e] */
    private boolean attemptDataSource(ContentResolver resolver, Uri uri) {
        AssetFileDescriptor afd = resolver.openAssetFileDescriptor(uri, "r");
        setDataSource(afd);
        if (afd != null) {
            try {
                $closeResource(null, afd);
            } catch (IOException | NullPointerException | SecurityException ex) {
            }
        }
        return true;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mOppoMediaPlayer.preSetDataSource(path);
        setDataSource(path, (Map<String, String>) null, (List<HttpCookie>) null);
    }

    @UnsupportedAppUsage
    public void setDataSource(String path, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, headers, (List<HttpCookie>) null);
    }

    @UnsupportedAppUsage
    private void setDataSource(String path, Map<String, String> headers, List<HttpCookie> cookies) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        this.mOppoMediaPlayer.preSetDataSource(path, headers);
        String[] keys = null;
        String[] values = null;
        if (headers != null) {
            keys = new String[headers.size()];
            values = new String[headers.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                keys[i] = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
        }
        setDataSource(path, keys, values, cookies);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0076, code lost:
        if (r7.contains("://") == false) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009f, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a2, code lost:
        throw r5;
     */
    @UnsupportedAppUsage
    private void setDataSource(String path, String[] keys, String[] values, List<HttpCookie> cookies) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (scheme != null) {
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("widevine://") || path.startsWith("rtsp://") || path.endsWith(".sdp") || path.endsWith(".sdp?")) {
                this.mOppoMediaPlayer.mNotUsingOppoMedia = true;
            } else if (!path.startsWith("file://") || !path.endsWith(".m3u8")) {
                this.mOppoMediaPlayer.mNotUsingOppoMedia = false;
                if (path.startsWith("/storage/")) {
                }
            } else {
                this.mOppoMediaPlayer.mNotUsingOppoMedia = true;
            }
            nativeSetDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(path, cookies), path, keys, values);
            return;
        }
        FileInputStream is = new FileInputStream(new File(path));
        setDataSource(is.getFD());
        $closeResource(null, is);
    }

    public void setDataSource(AssetFileDescriptor afd) throws IOException, IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(afd);
        if (afd.getDeclaredLength() < 0) {
            setDataSource(afd.getFileDescriptor());
        } else {
            setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
        }
    }

    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mOppoMediaPlayer.preSetDataSource(fd);
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mOppoMediaPlayer.preSetDataSource(fd, offset, length);
        _setDataSource(fd, offset, length);
        Parcel tmpParcel = this.mOppoMediaPlayer.checkZenMode();
        if (tmpParcel != null) {
            setParameter(10011, tmpParcel);
            tmpParcel.recycle();
        }
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        if (!this.mOppoMediaPlayer.preSetDataSource(dataSource)) {
            _setDataSource(dataSource);
        }
    }

    public void prepare() throws IOException, IllegalStateException {
        if (!this.mOppoMediaPlayer.prepareWithOppoIfNeeded()) {
            try {
                _prepare();
                scanInternalSubtitleTracks();
                synchronized (this.mDrmLock) {
                    this.mDrmInfoResolved = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (!this.mOppoMediaPlayer.handleMediaPlayerError(this)) {
                    Log.i(TAG, "prepare failed ,throw IOException to app");
                    throw e;
                }
            }
        }
    }

    public void prepareAsync() throws IllegalStateException {
        if (!this.mOppoMediaPlayer.prepareAsyncWithOppoIfNeeded()) {
            _prepareAsync();
        }
    }

    public void start() throws IllegalStateException {
        this.mVideoWidth = getVideoWidth();
        this.mVideoHeight = getVideoHeight();
        send_broadcast(1, this.mVideoWidth, this.mVideoHeight);
        Log.d(TAG, "start()");
        final int delay = getStartDelayMs();
        if (delay == 0) {
            startImpl();
        } else {
            new Thread() {
                /* class android.media.MediaPlayer.AnonymousClass1 */

                public void run() {
                    try {
                        Thread.sleep((long) delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MediaPlayer.this.baseSetStartDelayMs(0);
                    try {
                        MediaPlayer.this.startImpl();
                    } catch (IllegalStateException e2) {
                    }
                }
            }.start();
        }
        if (DEBUG) {
            Log.d(TAG, "MediaPlayer start end");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startImpl() {
        if (!this.mOppoMediaPlayer.startImplWithOppoIfNeeded(this)) {
            baseStart();
            stayAwake(true);
            _start();
        }
    }

    private int getAudioStreamType() {
        if (this.mStreamType == Integer.MIN_VALUE) {
            this.mStreamType = _getAudioStreamType();
        }
        return this.mStreamType;
    }

    public void stop() throws IllegalStateException {
        if (!this.mOppoMediaPlayer.stopWithOppoIfNeeded()) {
            stayAwake(false);
            _stop();
            baseStop();
        }
    }

    public void pause() throws IllegalStateException {
        if (!this.mOppoMediaPlayer.pauseWithOppoIfNeeded()) {
            stayAwake(false);
            _pause();
            basePause();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public void playerStart() {
        start();
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public void playerPause() {
        pause();
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public void playerStop() {
        stop();
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public int playerApplyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) {
        return native_applyVolumeShaper(configuration, operation);
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public VolumeShaper.State playerGetVolumeShaperState(int id) {
        return native_getVolumeShaperState(id);
    }

    @Override // android.media.VolumeAutomation
    public VolumeShaper createVolumeShaper(VolumeShaper.Configuration configuration) {
        return new VolumeShaper(configuration, this);
    }

    @Override // android.media.AudioRouting
    public boolean setPreferredDevice(AudioDeviceInfo deviceInfo) {
        int preferredDeviceId = 0;
        if (deviceInfo != null && !deviceInfo.isSink()) {
            return false;
        }
        if (deviceInfo != null) {
            preferredDeviceId = deviceInfo.getId();
        }
        boolean status = native_setOutputDevice(preferredDeviceId);
        if (status) {
            synchronized (this) {
                this.mPreferredDevice = deviceInfo;
            }
        }
        return status;
    }

    @Override // android.media.AudioRouting
    public AudioDeviceInfo getPreferredDevice() {
        AudioDeviceInfo audioDeviceInfo;
        synchronized (this) {
            audioDeviceInfo = this.mPreferredDevice;
        }
        return audioDeviceInfo;
    }

    @Override // android.media.AudioRouting
    public AudioDeviceInfo getRoutedDevice() {
        int deviceId = native_getRoutedDeviceId();
        if (deviceId == 0) {
            return null;
        }
        AudioDeviceInfo[] devices = AudioManager.getDevicesStatic(2);
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getId() == deviceId) {
                return devices[i];
            }
        }
        return null;
    }

    @GuardedBy({"mRoutingChangeListeners"})
    private void enableNativeRoutingCallbacksLocked(boolean enabled) {
        if (this.mRoutingChangeListeners.size() == 0) {
            native_enableDeviceCallback(enabled);
        }
    }

    @Override // android.media.AudioRouting
    public void addOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener, Handler handler) {
        synchronized (this.mRoutingChangeListeners) {
            if (listener != null) {
                if (!this.mRoutingChangeListeners.containsKey(listener)) {
                    enableNativeRoutingCallbacksLocked(true);
                    this.mRoutingChangeListeners.put(listener, new NativeRoutingEventHandlerDelegate(this, listener, handler != null ? handler : this.mEventHandler));
                }
            }
        }
    }

    @Override // android.media.AudioRouting
    public void removeOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener) {
        synchronized (this.mRoutingChangeListeners) {
            if (this.mRoutingChangeListeners.containsKey(listener)) {
                this.mRoutingChangeListeners.remove(listener);
                enableNativeRoutingCallbacksLocked(false);
            }
        }
    }

    public void setWakeMode(Context context, int mode) {
        if (!this.mOppoMediaPlayer.setWakeModeWithOppoIfNeeded(context, mode)) {
            boolean washeld = false;
            if (SystemProperties.getBoolean("audio.offload.ignore_setawake", false)) {
                Log.w(TAG, "IGNORING setWakeMode " + mode);
                return;
            }
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    washeld = true;
                    this.mWakeLock.release();
                }
                this.mWakeLock = null;
            }
            this.mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(536870912 | mode, MediaPlayer.class.getName());
            this.mWakeLock.setReferenceCounted(false);
            if (washeld) {
                this.mWakeLock.acquire();
            }
        }
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (!this.mOppoMediaPlayer.setScreenOnWhilePlayingWithOppoIfNeeded(screenOn) && this.mScreenOnWhilePlaying != screenOn) {
            if (screenOn && this.mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            this.mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stayAwake(boolean awake) {
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            if (awake && !wakeLock.isHeld()) {
                this.mWakeLock.acquire();
            } else if (!awake && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }
        this.mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        SurfaceHolder surfaceHolder = this.mSurfaceHolder;
        if (surfaceHolder != null) {
            surfaceHolder.setKeepScreenOn(this.mScreenOnWhilePlaying && this.mStayAwake);
        }
    }

    public int getVideoWidth() {
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            return this.mOppoMediaPlayer.getVideoWidthWithOppo();
        }
        return _getVideoWidth();
    }

    public int getVideoHeight() {
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            return this.mOppoMediaPlayer.getVideoHeightWithOppo();
        }
        return _getVideoHeight();
    }

    public PersistableBundle getMetrics() {
        return native_getMetrics();
    }

    public boolean isPlaying() {
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            return this.mOppoMediaPlayer.isPlaying();
        }
        return _isPlaying();
    }

    public PlaybackParams easyPlaybackParams(float rate, int audioMode) {
        PlaybackParams params = new PlaybackParams();
        params.allowDefaults();
        if (audioMode == 0) {
            params.setSpeed(rate).setPitch(1.0f);
        } else if (audioMode == 1) {
            params.setSpeed(rate).setPitch(1.0f).setAudioFallbackMode(2);
        } else if (audioMode == 2) {
            params.setSpeed(rate).setPitch(rate);
        } else {
            throw new IllegalArgumentException("Audio playback mode " + audioMode + " is not supported");
        }
        return params;
    }

    public void seekTo(long msec, int mode) {
        Log.d(TAG, "seekTo() msec = " + msec + ", seekMode = " + mode + " isOppoCreate = " + this.mOppoMediaPlayer.isOppoCreate);
        if (mode < 0 || mode > 3) {
            throw new IllegalArgumentException("Illegal seek mode: " + mode);
        }
        if (msec > 2147483647L) {
            Log.w(TAG, "seekTo offset " + msec + " is too large, cap to 2147483647");
            msec = 2147483647L;
        } else if (msec < -2147483648L) {
            Log.w(TAG, "seekTo offset " + msec + " is too small, cap to -2147483648");
            msec = -2147483648L;
        }
        if (!this.mOppoMediaPlayer.seekToWithOppoIfNeeded(msec, mode)) {
            _seekTo(msec, mode);
        }
    }

    public void seekTo(int msec) throws IllegalStateException {
        seekTo((long) msec, 0);
    }

    public MediaTimestamp getTimestamp() {
        try {
            return new MediaTimestamp(((long) getCurrentPosition()) * 1000, System.nanoTime(), isPlaying() ? getPlaybackParams().getSpeed() : 0.0f);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public int getCurrentPosition() {
        OppoMediaPlayer oppoMediaPlayer = this.mOppoMediaPlayer;
        if (oppoMediaPlayer == null || !oppoMediaPlayer.needGetFromOppo()) {
            return _getCurrentPosition();
        }
        return this.mOppoMediaPlayer.getCurrentPositionWithOppo();
    }

    public int getDuration() {
        if (this.mOppoMediaPlayer.needGetFromOppo()) {
            return this.mOppoMediaPlayer.getDurationWithOppo();
        }
        return _getDuration();
    }

    @UnsupportedAppUsage
    public Metadata getMetadata(boolean update_only, boolean apply_filter) {
        Parcel reply = Parcel.obtain();
        Metadata data = new Metadata();
        if (!native_getMetadata(update_only, apply_filter, reply)) {
            reply.recycle();
            return null;
        } else if (data.parse(reply)) {
            return data;
        } else {
            reply.recycle();
            return null;
        }
    }

    public int setMetadataFilter(Set<Integer> allow, Set<Integer> block) {
        Parcel request = newRequest();
        int capacity = request.dataSize() + ((allow.size() + 1 + 1 + block.size()) * 4);
        if (request.dataCapacity() < capacity) {
            request.setDataCapacity(capacity);
        }
        request.writeInt(allow.size());
        for (Integer t : allow) {
            request.writeInt(t.intValue());
        }
        request.writeInt(block.size());
        for (Integer t2 : block) {
            request.writeInt(t2.intValue());
        }
        return native_setMetadataFilter(request);
    }

    public void release() {
        OppoMediaPlayer oppoMediaPlayer = this.mOppoMediaPlayer;
        if (oppoMediaPlayer != null) {
            oppoMediaPlayer.releaseWithOppo();
        }
        this.mContext = null;
        send_broadcast(0, this.mVideoWidth, this.mVideoHeight);
        baseRelease();
        stayAwake(false);
        updateSurfaceScreenOn();
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnTimedTextListener = null;
        synchronized (this.mTimeProviderLock) {
            if (this.mTimeProvider != null) {
                this.mTimeProvider.close();
                this.mTimeProvider = null;
            }
        }
        synchronized (this) {
            this.mSubtitleDataListenerDisabled = false;
            this.mExtSubtitleDataListener = null;
            this.mExtSubtitleDataHandler = null;
            this.mOnMediaTimeDiscontinuityListener = null;
            this.mOnMediaTimeDiscontinuityHandler = null;
        }
        this.mOnDrmConfigHelper = null;
        this.mOnDrmInfoHandlerDelegate = null;
        this.mOnDrmPreparedHandlerDelegate = null;
        resetDrmState();
        Log.d(TAG, "_release native called");
        _release();
        Log.d(TAG, "_release native finished");
    }

    public void reset() {
        if (!this.mOppoMediaPlayer.resetWithOppoIfNeeded()) {
            this.mSelectedSubtitleTrackIndex = -1;
            synchronized (this.mOpenSubtitleSources) {
                Iterator<InputStream> it = this.mOpenSubtitleSources.iterator();
                while (it.hasNext()) {
                    try {
                        it.next().close();
                    } catch (IOException e) {
                    }
                }
                this.mOpenSubtitleSources.clear();
            }
            SubtitleController subtitleController = this.mSubtitleController;
            if (subtitleController != null) {
                subtitleController.reset();
            }
            synchronized (this.mTimeProviderLock) {
                if (this.mTimeProvider != null) {
                    this.mTimeProvider.close();
                    this.mTimeProvider = null;
                }
            }
            stayAwake(false);
            _reset();
            EventHandler eventHandler = this.mEventHandler;
            if (eventHandler != null) {
                eventHandler.removeCallbacksAndMessages(null);
            }
            synchronized (this.mIndexTrackPairs) {
                this.mIndexTrackPairs.clear();
                this.mInbandTrackIndices.clear();
            }
        }
        this.mOppoMediaPlayer.resetZenModeFlag();
        resetDrmState();
    }

    public void notifyAt(long mediaTimeUs) {
        _notifyAt(mediaTimeUs);
    }

    public void setAudioStreamType(int streamtype) {
        if (streamtype == 0) {
            String callerName = ActivityThread.currentPackageName();
            if (callerName != null && callerName.length() > 0) {
                String value = OppoAtlasManager.getInstance(null).getParameters("check_listinfo_byname=change-streamtype=" + callerName);
                if (!(this.mContext == null || value == null || !value.equals("true"))) {
                    Log.i(TAG, "setAudioStreamType, caller = " + callerName + ", for change wechat message notification!");
                    try {
                        AudioManager localAudioManager = (AudioManager) this.mContext.getSystemService("audio");
                        boolean bChangeStreamType = false;
                        int currentMode = localAudioManager.getMode();
                        if (localAudioManager.isMusicActive() && localAudioManager.isWiredHeadsetOn() && currentMode != 2 && currentMode != 3) {
                            Log.i(TAG, "Playing music with headphone, should change wechat message notification");
                            bChangeStreamType = true;
                        }
                        Parcel parcel = Parcel.obtain();
                        parcel.writeInt(bChangeStreamType ? 1 : 0);
                        setParameter(10111, parcel);
                        parcel.recycle();
                    } catch (Exception e) {
                        Log.e(TAG, "start Exception for change wechat message notification!");
                    }
                }
            } else {
                return;
            }
        }
        if (!this.mOppoMediaPlayer.setAudioStreamTypeWithOppoIfNeeded(streamtype)) {
            deprecateStreamTypeForPlayback(streamtype, TAG, "setAudioStreamType()");
            baseUpdateAudioAttributes(new AudioAttributes.Builder().setInternalLegacyStreamType(streamtype).build());
            _setAudioStreamType(streamtype);
            this.mStreamType = streamtype;
        }
        Parcel tmpParcel = this.mOppoMediaPlayer.checkWechatMute();
        if (tmpParcel != null) {
            setParameter(10011, tmpParcel);
            tmpParcel.recycle();
        }
    }

    private int audioAttributesToStreamType(AudioAttributes attr) {
        if ((attr.getAllFlags() & 1) == 1) {
            return 7;
        }
        if ((attr.getAllFlags() & 4) == 4) {
            return 6;
        }
        switch (attr.getUsage()) {
            case 1:
            case 12:
            case 14:
            case 16:
                return 3;
            case 2:
                return 0;
            case 3:
                return 8;
            case 4:
                return 4;
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
                return 5;
            case 6:
                return 2;
            case 11:
                return 10;
            case 13:
                return 1;
            case 15:
            default:
                return 3;
        }
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (this.mOppoMediaPlayer.setAudioAttributesWithOppoIfNeeded()) {
            return;
        }
        if (attributes != null) {
            baseUpdateAudioAttributes(attributes);
            this.mUsage = attributes.getUsage();
            Parcel pattributes = Parcel.obtain();
            attributes.writeToParcel(pattributes, 1);
            setParameter(1400, pattributes);
            pattributes.recycle();
            this.mOppoMediaPlayer.checkAudioStreamType(attributes);
            return;
        }
        throw new IllegalArgumentException("Cannot set AudioAttributes to null");
    }

    public void setLooping(boolean looping) {
        if (!this.mOppoMediaPlayer.setLoopingWithOppoIfNeeded(looping)) {
            _setLooping(looping);
        }
    }

    public boolean isLooping() {
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            return this.mOppoMediaPlayer.isLooping();
        }
        return _isLooping();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (!this.mOppoMediaPlayer.setVolumeWithOppoIfNeeded(leftVolume, rightVolume)) {
            baseSetVolume(leftVolume, rightVolume);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public void playerSetVolume(boolean muting, float leftVolume, float rightVolume) {
        float f = 0.0f;
        float f2 = muting ? 0.0f : leftVolume;
        if (!muting) {
            f = rightVolume;
        }
        _setVolume(f2, f);
    }

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    public void setAudioSessionId(int sessionId) throws IllegalArgumentException, IllegalStateException {
        if (!this.mOppoMediaPlayer.setAudioSessionIdWithOppoIfNeeded(sessionId)) {
            _setAudioSessionId(sessionId);
        }
    }

    public int getAudioSessionId() {
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            OppoMediaPlayer oppoMediaPlayer = this.mOppoMediaPlayer;
            oppoMediaPlayer.mAudioSessionId = oppoMediaPlayer.getAudioSessionId();
        } else {
            this.mOppoMediaPlayer.mAudioSessionId = _getAudioSessionId();
        }
        return this.mOppoMediaPlayer.mAudioSessionId;
    }

    public void attachAuxEffect(int effectId) {
        if (!this.mOppoMediaPlayer.attachAuxEffectWithOppoIfNeeded(effectId)) {
            _attachAuxEffect(effectId);
        }
    }

    public void setAuxEffectSendLevel(float level) {
        if (!this.mOppoMediaPlayer.setAuxEffectSendLevelWithOppoIfNeeded(level)) {
            baseSetAuxEffectSendLevel(level);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.media.PlayerBase
    public int playerSetAuxEffectSendLevel(boolean muting, float level) {
        _setAuxEffectSendLevel(muting ? 0.0f : level);
        return 0;
    }

    public static class TrackInfo implements Parcelable {
        @UnsupportedAppUsage
        static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {
            /* class android.media.MediaPlayer.TrackInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public TrackInfo createFromParcel(Parcel in) {
                return new TrackInfo(in);
            }

            @Override // android.os.Parcelable.Creator
            public TrackInfo[] newArray(int size) {
                return new TrackInfo[size];
            }
        };
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_METADATA = 5;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;
        final MediaFormat mFormat;
        final int mTrackType;

        @Retention(RetentionPolicy.SOURCE)
        public @interface TrackType {
        }

        public int getTrackType() {
            return this.mTrackType;
        }

        public String getLanguage() {
            String language = this.mFormat.getString("language");
            return language == null ? "und" : language;
        }

        public MediaFormat getFormat() {
            int i = this.mTrackType;
            if (i == 3 || i == 4) {
                return this.mFormat;
            }
            return null;
        }

        TrackInfo(Parcel in) {
            this.mTrackType = in.readInt();
            this.mFormat = MediaFormat.createSubtitleFormat(in.readString(), in.readString());
            if (this.mTrackType == 4) {
                this.mFormat.setInteger(MediaFormat.KEY_IS_AUTOSELECT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_DEFAULT, in.readInt());
                this.mFormat.setInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE, in.readInt());
            }
        }

        TrackInfo(int type, MediaFormat format) {
            this.mTrackType = type;
            this.mFormat = format;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mTrackType);
            dest.writeString(this.mFormat.getString(MediaFormat.KEY_MIME));
            dest.writeString(getLanguage());
            if (this.mTrackType == 4) {
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_AUTOSELECT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_DEFAULT));
                dest.writeInt(this.mFormat.getInteger(MediaFormat.KEY_IS_FORCED_SUBTITLE));
            }
        }

        public String toString() {
            StringBuilder out = new StringBuilder(128);
            out.append(getClass().getName());
            out.append('{');
            int i = this.mTrackType;
            if (i == 1) {
                out.append("VIDEO");
            } else if (i == 2) {
                out.append("AUDIO");
            } else if (i == 3) {
                out.append("TIMEDTEXT");
            } else if (i != 4) {
                out.append(IccCardConstants.INTENT_VALUE_ICC_UNKNOWN);
            } else {
                out.append("SUBTITLE");
            }
            out.append(", " + this.mFormat.toString());
            out.append("}");
            return out.toString();
        }
    }

    public TrackInfo getTrackInfo(Parcel parcel) {
        return new TrackInfo(parcel);
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        TrackInfo[] allTrackInfo;
        if (this.mOppoMediaPlayer.isOppoCreate()) {
            return this.mOppoMediaPlayer.getTrackInfoWithOppo(this);
        }
        TrackInfo[] trackInfo = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            allTrackInfo = new TrackInfo[this.mIndexTrackPairs.size()];
            for (int i = 0; i < allTrackInfo.length; i++) {
                Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(i);
                if (p.first != null) {
                    allTrackInfo[i] = trackInfo[p.first.intValue()];
                } else {
                    SubtitleTrack track = p.second;
                    allTrackInfo[i] = new TrackInfo(track.getTrackType(), track.getFormat());
                }
            }
        }
        return allTrackInfo;
    }

    private TrackInfo[] getInbandTrackInfo() throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(1);
            invoke(request, reply);
            return (TrackInfo[]) reply.createTypedArray(TrackInfo.CREATOR);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    private static boolean availableMimeTypeForExternalSource(String mimeType) {
        if ("application/x-subrip".equals(mimeType)) {
            return true;
        }
        return false;
    }

    @UnsupportedAppUsage
    public void setSubtitleAnchor(SubtitleController controller, SubtitleController.Anchor anchor) {
        if (!this.mOppoMediaPlayer.setSubtitleAnchorWithOppoIfNeeded()) {
            this.mSubtitleController = controller;
            this.mSubtitleController.setAnchor(anchor);
        }
    }

    private synchronized void setSubtitleAnchor() {
        if (this.mSubtitleController == null && ActivityThread.currentApplication() != null) {
            final TimeProvider timeProvider = (TimeProvider) getMediaTimeProvider();
            final HandlerThread thread = new HandlerThread("SetSubtitleAnchorThread");
            thread.start();
            new Handler(thread.getLooper()).post(new Runnable() {
                /* class android.media.MediaPlayer.AnonymousClass2 */

                public void run() {
                    Context context = ActivityThread.currentApplication();
                    MediaPlayer mediaPlayer = MediaPlayer.this;
                    mediaPlayer.mSubtitleController = new SubtitleController(context, timeProvider, mediaPlayer);
                    MediaPlayer.this.mSubtitleController.setAnchor(new SubtitleController.Anchor() {
                        /* class android.media.MediaPlayer.AnonymousClass2.AnonymousClass1 */

                        @Override // android.media.SubtitleController.Anchor
                        public void setSubtitleWidget(SubtitleTrack.RenderingWidget subtitleWidget) {
                        }

                        @Override // android.media.SubtitleController.Anchor
                        public Looper getSubtitleLooper() {
                            return timeProvider.mEventHandler.getLooper();
                        }
                    });
                    thread.getLooper().quitSafely();
                }
            });
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.w(TAG, "failed to join SetSubtitleAnchorThread");
            }
        }
    }

    @Override // android.media.SubtitleController.Listener
    public void onSubtitleTrackSelected(SubtitleTrack track) {
        if (!this.mOppoMediaPlayer.onSubtitleTrackSelectedWithOppoIfNeeded()) {
            int i = this.mSelectedSubtitleTrackIndex;
            if (i >= 0) {
                try {
                    selectOrDeselectInbandTrack(i, false);
                } catch (IllegalStateException e) {
                }
                this.mSelectedSubtitleTrackIndex = -1;
            }
            synchronized (this) {
                this.mSubtitleDataListenerDisabled = true;
            }
            if (track != null) {
                synchronized (this.mIndexTrackPairs) {
                    Iterator<Pair<Integer, SubtitleTrack>> it = this.mIndexTrackPairs.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Pair<Integer, SubtitleTrack> p = it.next();
                        if (p.first != null && p.second == track) {
                            this.mSelectedSubtitleTrackIndex = p.first.intValue();
                            break;
                        }
                    }
                }
                int i2 = this.mSelectedSubtitleTrackIndex;
                if (i2 >= 0) {
                    try {
                        selectOrDeselectInbandTrack(i2, true);
                    } catch (IllegalStateException e2) {
                    }
                    synchronized (this) {
                        this.mSubtitleDataListenerDisabled = false;
                    }
                }
            }
        }
    }

    @UnsupportedAppUsage
    public void addSubtitleSource(final InputStream is, final MediaFormat format) throws IllegalStateException {
        if (!this.mOppoMediaPlayer.addSubtitleSourceWithOppoIfNeeded()) {
            if (is != null) {
                synchronized (this.mOpenSubtitleSources) {
                    this.mOpenSubtitleSources.add(is);
                }
            } else {
                Log.w(TAG, "addSubtitleSource called with null InputStream");
            }
            getMediaTimeProvider();
            final HandlerThread thread = new HandlerThread("SubtitleReadThread", 9);
            thread.start();
            new Handler(thread.getLooper()).post(new Runnable() {
                /* class android.media.MediaPlayer.AnonymousClass4 */

                private int addTrack() {
                    SubtitleTrack track;
                    if (is == null || MediaPlayer.this.mSubtitleController == null || (track = MediaPlayer.this.mSubtitleController.addTrack(format)) == null) {
                        return 901;
                    }
                    Scanner scanner = new Scanner(is, "UTF-8");
                    String contents = scanner.useDelimiter("\\A").next();
                    synchronized (MediaPlayer.this.mOpenSubtitleSources) {
                        MediaPlayer.this.mOpenSubtitleSources.remove(is);
                    }
                    scanner.close();
                    synchronized (MediaPlayer.this.mIndexTrackPairs) {
                        MediaPlayer.this.mIndexTrackPairs.add(Pair.create(null, track));
                    }
                    synchronized (MediaPlayer.this.mTimeProviderLock) {
                        if (MediaPlayer.this.mTimeProvider != null) {
                            Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
                            h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(track, contents.getBytes())));
                        }
                    }
                    return 803;
                }

                public void run() {
                    int res = addTrack();
                    if (MediaPlayer.this.mEventHandler != null) {
                        MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(200, res, 0, null));
                    }
                    thread.getLooper().quitSafely();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scanInternalSubtitleTracks() {
        setSubtitleAnchor();
        populateInbandTracks();
        SubtitleController subtitleController = this.mSubtitleController;
        if (subtitleController != null) {
            subtitleController.selectDefaultTrack();
        }
    }

    private void populateInbandTracks() {
        TrackInfo[] tracks = getInbandTrackInfo();
        synchronized (this.mIndexTrackPairs) {
            for (int i = 0; i < tracks.length; i++) {
                if (!this.mInbandTrackIndices.get(i)) {
                    this.mInbandTrackIndices.set(i);
                    if (tracks[i] == null) {
                        Log.w(TAG, "unexpected NULL track at index " + i);
                    }
                    if (tracks[i] == null || tracks[i].getTrackType() != 4) {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), null));
                    } else {
                        this.mIndexTrackPairs.add(Pair.create(Integer.valueOf(i), this.mSubtitleController.addTrack(tracks[i].getFormat())));
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0026, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        throw r3;
     */
    public void addTimedTextSource(String path, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mOppoMediaPlayer.addTimedTextSourceWithOppoIfNeeded()) {
            return;
        }
        if (availableMimeTypeForExternalSource(mimeType)) {
            FileInputStream is = new FileInputStream(new File(path));
            addTimedTextSource(is.getFD(), mimeType);
            $closeResource(null, is);
            return;
        }
        throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mimeType);
    }

    public void addTimedTextSource(Context context, Uri uri, String mimeType) throws IOException, IllegalArgumentException, IllegalStateException {
        if (!this.mOppoMediaPlayer.addTimedTextSourceWithOppoIfNeeded(context, uri, mimeType)) {
            String scheme = uri.getScheme();
            if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
                addTimedTextSource(uri.getPath(), mimeType);
                return;
            }
            AssetFileDescriptor fd = null;
            try {
                AssetFileDescriptor fd2 = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                if (fd2 != null) {
                    addTimedTextSource(fd2.getFileDescriptor(), mimeType);
                    fd2.close();
                } else if (fd2 != null) {
                    fd2.close();
                }
            } catch (SecurityException e) {
                if (0 == 0) {
                    return;
                }
                fd.close();
            } catch (IOException e2) {
                if (0 == 0) {
                    return;
                }
                fd.close();
            } catch (Throwable th) {
                if (0 != 0) {
                    fd.close();
                }
                throw th;
            }
        }
    }

    public void addTimedTextSource(FileDescriptor fd, String mimeType) throws IllegalArgumentException, IllegalStateException {
        if (!this.mOppoMediaPlayer.addTimedTextSourceWithOppoIfNeeded(fd, mimeType)) {
            addTimedTextSource(fd, 0, 576460752303423487L, mimeType);
        }
    }

    public void addTimedTextSource(FileDescriptor fd, final long offset, final long length, String mime) throws IllegalArgumentException, IllegalStateException {
        if (this.mOppoMediaPlayer.addTimedTextSourceWithOppoIfNeeded(fd, offset, length, mime)) {
            return;
        }
        if (availableMimeTypeForExternalSource(mime)) {
            try {
                final FileDescriptor dupedFd = Os.dup(fd);
                MediaFormat fFormat = new MediaFormat();
                fFormat.setString(MediaFormat.KEY_MIME, mime);
                fFormat.setInteger(MediaFormat.KEY_IS_TIMED_TEXT, 1);
                if (this.mSubtitleController == null) {
                    setSubtitleAnchor();
                }
                if (!this.mSubtitleController.hasRendererFor(fFormat)) {
                    this.mSubtitleController.registerRenderer(new SRTRenderer(ActivityThread.currentApplication(), this.mEventHandler));
                }
                final SubtitleTrack track = this.mSubtitleController.addTrack(fFormat);
                synchronized (this.mIndexTrackPairs) {
                    this.mIndexTrackPairs.add(Pair.create(null, track));
                }
                getMediaTimeProvider();
                final HandlerThread thread = new HandlerThread("TimedTextReadThread", 9);
                thread.start();
                new Handler(thread.getLooper()).post(new Runnable() {
                    /* class android.media.MediaPlayer.AnonymousClass5 */

                    private int addTrack() {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try {
                            Os.lseek(dupedFd, offset, OsConstants.SEEK_SET);
                            byte[] buffer = new byte[4096];
                            long total = 0;
                            while (true) {
                                if (total >= length) {
                                    break;
                                }
                                int bytes = IoBridge.read(dupedFd, buffer, 0, (int) Math.min((long) buffer.length, length - total));
                                if (bytes < 0) {
                                    break;
                                }
                                bos.write(buffer, 0, bytes);
                                total += (long) bytes;
                            }
                            synchronized (MediaPlayer.this.mTimeProviderLock) {
                                if (MediaPlayer.this.mTimeProvider != null) {
                                    Handler h = MediaPlayer.this.mTimeProvider.mEventHandler;
                                    h.sendMessage(h.obtainMessage(1, 4, 0, Pair.create(track, bos.toByteArray())));
                                }
                            }
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e) {
                                Log.e(MediaPlayer.TAG, e.getMessage(), e);
                            }
                            return 803;
                        } catch (Exception e2) {
                            Log.e(MediaPlayer.TAG, e2.getMessage(), e2);
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e3) {
                                Log.e(MediaPlayer.TAG, e3.getMessage(), e3);
                            }
                            return 900;
                        } catch (Throwable th) {
                            try {
                                Os.close(dupedFd);
                            } catch (ErrnoException e4) {
                                Log.e(MediaPlayer.TAG, e4.getMessage(), e4);
                            }
                            throw th;
                        }
                    }

                    public void run() {
                        int res = addTrack();
                        if (MediaPlayer.this.mEventHandler != null) {
                            MediaPlayer.this.mEventHandler.sendMessage(MediaPlayer.this.mEventHandler.obtainMessage(200, res, 0, null));
                        }
                        thread.getLooper().quitSafely();
                    }
                });
            } catch (ErrnoException ex) {
                Log.e(TAG, ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        } else {
            throw new IllegalArgumentException("Illegal mimeType for timed text source: " + mime);
        }
    }

    public int getSelectedTrack(int trackType) throws IllegalStateException {
        SubtitleTrack subtitleTrack;
        if (this.mOppoMediaPlayer.getSelectedTrackWithOppoIfNeeded(trackType)) {
            return -1;
        }
        if (this.mSubtitleController != null && ((trackType == 4 || trackType == 3) && (subtitleTrack = this.mSubtitleController.getSelectedTrack()) != null)) {
            synchronized (this.mIndexTrackPairs) {
                for (int i = 0; i < this.mIndexTrackPairs.size(); i++) {
                    if (this.mIndexTrackPairs.get(i).second == subtitleTrack && subtitleTrack.getTrackType() == trackType) {
                        return i;
                    }
                }
            }
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(7);
            request.writeInt(trackType);
            invoke(request, reply);
            int inbandTrackIndex = reply.readInt();
            synchronized (this.mIndexTrackPairs) {
                for (int i2 = 0; i2 < this.mIndexTrackPairs.size(); i2++) {
                    Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(i2);
                    if (p.first != null && p.first.intValue() == inbandTrackIndex) {
                        return i2;
                    }
                }
                request.recycle();
                reply.recycle();
                return -1;
            }
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    public void selectTrack(int index) throws IllegalStateException {
        if (!this.mOppoMediaPlayer.selectTrackWithOppoIfNeeded(index)) {
            selectOrDeselectTrack(index, true);
        }
    }

    public void deselectTrack(int index) throws IllegalStateException {
        if (!this.mOppoMediaPlayer.deselectTrackWithOppoIfNeeded(index)) {
            selectOrDeselectTrack(index, false);
        }
    }

    private void selectOrDeselectTrack(int index, boolean select) throws IllegalStateException {
        populateInbandTracks();
        try {
            Pair<Integer, SubtitleTrack> p = this.mIndexTrackPairs.get(index);
            SubtitleTrack track = p.second;
            if (track == null) {
                selectOrDeselectInbandTrack(p.first.intValue(), select);
                return;
            }
            SubtitleController subtitleController = this.mSubtitleController;
            if (subtitleController != null) {
                if (select) {
                    if (track.getTrackType() == 3) {
                        int ttIndex = getSelectedTrack(3);
                        synchronized (this.mIndexTrackPairs) {
                            if (ttIndex >= 0) {
                                if (ttIndex < this.mIndexTrackPairs.size()) {
                                    Pair<Integer, SubtitleTrack> p2 = this.mIndexTrackPairs.get(ttIndex);
                                    if (p2.first != null && p2.second == null) {
                                        selectOrDeselectInbandTrack(p2.first.intValue(), false);
                                    }
                                }
                            }
                        }
                    }
                    this.mSubtitleController.selectTrack(track);
                } else if (subtitleController.getSelectedTrack() == track) {
                    this.mSubtitleController.selectTrack(null);
                } else {
                    Log.w(TAG, "trying to deselect track that was not selected");
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void selectOrDeselectInbandTrack(int index, boolean select) throws IllegalStateException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(select ? 4 : 5);
            request.writeInt(index);
            invoke(request, reply);
        } finally {
            request.recycle();
            reply.recycle();
        }
    }

    @UnsupportedAppUsage
    public void setRetransmitEndpoint(InetSocketAddress endpoint) throws IllegalStateException, IllegalArgumentException {
        if (!this.mOppoMediaPlayer.setRetransmitEndpointWithOppoIfNeeded(endpoint)) {
            String addrString = null;
            int port = 0;
            if (endpoint != null) {
                addrString = endpoint.getAddress().getHostAddress();
                port = endpoint.getPort();
            }
            int ret = native_setRetransmitEndpoint(addrString, port);
            if (ret != 0) {
                throw new IllegalArgumentException("Illegal re-transmit endpoint; native ret " + ret);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        baseRelease();
        Log.d(TAG, "finalize() native_finalize called");
        native_finalize();
        Log.d(TAG, "finalize() native_finalize finished");
    }

    @UnsupportedAppUsage
    public MediaTimeProvider getMediaTimeProvider() {
        synchronized (this.mTimeProviderLock) {
            if (this.mOppoMediaPlayer.getMediaTimeProviderWithOppoIfNeeded()) {
                return null;
            }
            if (this.mTimeProvider == null) {
                this.mTimeProvider = new TimeProvider(this);
            }
            return this.mTimeProvider;
        }
    }

    /* access modifiers changed from: private */
    public class EventHandler extends Handler {
        private MediaPlayer mMediaPlayer;

        public EventHandler(MediaPlayer mp, Looper looper) {
            super(looper);
            this.mMediaPlayer = mp;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARNING: Code restructure failed: missing block: B:173:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:174:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:175:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:176:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x008c, code lost:
            if ((r18.obj instanceof android.os.Parcel) == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x008e, code lost:
            r4 = (android.os.Parcel) r18.obj;
            r5 = new android.media.SubtitleData(r4);
            r4.recycle();
            r17.this$0.mIntSubtitleDataListener.onSubtitleData(r17.mMediaPlayer, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a5, code lost:
            if (r0 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a7, code lost:
            if (r3 != null) goto L_0x00af;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a9, code lost:
            r0.onSubtitleData(r17.mMediaPlayer, r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00af, code lost:
            r3.post(new android.media.MediaPlayer.EventHandler.AnonymousClass1(r17));
         */
        /* JADX WARNING: Removed duplicated region for block: B:178:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:61:0x012d  */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            OnDrmInfoHandlerDelegate onDrmInfoHandlerDelegate;
            final OnMediaTimeDiscontinuityListener mediaTimeListener;
            Handler mediaTimeHandler;
            final MediaTimestamp timestamp;
            OnInfoListener onInfoListener;
            if (this.mMediaPlayer.mNativeContext == 0) {
                Log.w(MediaPlayer.TAG, "mediaplayer went away with unhandled events");
                return;
            }
            int i = msg.what;
            if (i != 210) {
                boolean z = false;
                if (i == 211) {
                    synchronized (this) {
                        mediaTimeListener = MediaPlayer.this.mOnMediaTimeDiscontinuityListener;
                        mediaTimeHandler = MediaPlayer.this.mOnMediaTimeDiscontinuityHandler;
                    }
                    if (mediaTimeListener != null && (msg.obj instanceof Parcel)) {
                        Parcel parcel = (Parcel) msg.obj;
                        parcel.setDataPosition(0);
                        long anchorMediaUs = parcel.readLong();
                        long anchorRealUs = parcel.readLong();
                        float playbackRate = parcel.readFloat();
                        parcel.recycle();
                        if (anchorMediaUs == -1 || anchorRealUs == -1) {
                            timestamp = MediaTimestamp.TIMESTAMP_UNKNOWN;
                        } else {
                            timestamp = new MediaTimestamp(anchorMediaUs, anchorRealUs * 1000, playbackRate);
                        }
                        if (mediaTimeHandler == null) {
                            mediaTimeListener.onMediaTimeDiscontinuity(this.mMediaPlayer, timestamp);
                        } else {
                            mediaTimeHandler.post(new Runnable() {
                                /* class android.media.MediaPlayer.EventHandler.AnonymousClass2 */

                                public void run() {
                                    mediaTimeListener.onMediaTimeDiscontinuity(EventHandler.this.mMediaPlayer, timestamp);
                                }
                            });
                        }
                    }
                } else if (i != 10000) {
                    switch (i) {
                        case 0:
                            return;
                        case 1:
                            try {
                                MediaPlayer.this.scanInternalSubtitleTracks();
                            } catch (RuntimeException e) {
                                sendMessage(obtainMessage(100, 1, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                            }
                            OnPreparedListener onPreparedListener = MediaPlayer.this.mOnPreparedListener;
                            if (onPreparedListener != null) {
                                onPreparedListener.onPrepared(this.mMediaPlayer);
                                return;
                            }
                            return;
                        case 2:
                            MediaPlayer.this.mOnCompletionInternalListener.onCompletion(this.mMediaPlayer);
                            OnCompletionListener onCompletionListener = MediaPlayer.this.mOnCompletionListener;
                            if (onCompletionListener != null) {
                                onCompletionListener.onCompletion(this.mMediaPlayer);
                            }
                            MediaPlayer.this.stayAwake(false);
                            return;
                        case 3:
                            OnBufferingUpdateListener onBufferingUpdateListener = MediaPlayer.this.mOnBufferingUpdateListener;
                            if (onBufferingUpdateListener != null) {
                                onBufferingUpdateListener.onBufferingUpdate(this.mMediaPlayer, msg.arg1);
                                return;
                            }
                            return;
                        case 4:
                            OnSeekCompleteListener onSeekCompleteListener = MediaPlayer.this.mOnSeekCompleteListener;
                            if (onSeekCompleteListener != null) {
                                onSeekCompleteListener.onSeekComplete(this.mMediaPlayer);
                                break;
                            }
                            break;
                        case 5:
                            OnVideoSizeChangedListener onVideoSizeChangedListener = MediaPlayer.this.mOnVideoSizeChangedListener;
                            if (onVideoSizeChangedListener != null) {
                                onVideoSizeChangedListener.onVideoSizeChanged(this.mMediaPlayer, msg.arg1, msg.arg2);
                                return;
                            }
                            return;
                        case 6:
                        case 7:
                            TimeProvider timeProvider = MediaPlayer.this.mTimeProvider;
                            if (timeProvider != null) {
                                if (msg.what == 7) {
                                    z = true;
                                }
                                timeProvider.onPaused(z);
                                return;
                            }
                            return;
                        case 8:
                            TimeProvider timeProvider2 = MediaPlayer.this.mTimeProvider;
                            if (timeProvider2 != null) {
                                timeProvider2.onStopped();
                                return;
                            }
                            return;
                        case 9:
                            break;
                        default:
                            switch (i) {
                                case 98:
                                    TimeProvider timeProvider3 = MediaPlayer.this.mTimeProvider;
                                    if (timeProvider3 != null) {
                                        timeProvider3.onNotifyTime();
                                        return;
                                    }
                                    return;
                                case 99:
                                    OnTimedTextListener onTimedTextListener = MediaPlayer.this.mOnTimedTextListener;
                                    if (onTimedTextListener == null) {
                                        return;
                                    }
                                    if (msg.obj == null) {
                                        onTimedTextListener.onTimedText(this.mMediaPlayer, null);
                                        return;
                                    } else if (msg.obj instanceof Parcel) {
                                        Parcel parcel2 = (Parcel) msg.obj;
                                        TimedText text = new TimedText(parcel2);
                                        parcel2.recycle();
                                        onTimedTextListener.onTimedText(this.mMediaPlayer, text);
                                        return;
                                    } else {
                                        return;
                                    }
                                case 100:
                                    Log.e(MediaPlayer.TAG, "Error (" + msg.arg1 + SmsManager.REGEX_PREFIX_DELIMITER + msg.arg2 + ")");
                                    if (!MediaPlayer.this.mOppoMediaPlayer.handleError(msg, this.mMediaPlayer)) {
                                        boolean error_was_handled = false;
                                        OnErrorListener onErrorListener = MediaPlayer.this.mOnErrorListener;
                                        if (onErrorListener != null) {
                                            error_was_handled = onErrorListener.onError(this.mMediaPlayer, msg.arg1, msg.arg2);
                                        }
                                        MediaPlayer.this.mOnCompletionInternalListener.onCompletion(this.mMediaPlayer);
                                        OnCompletionListener onCompletionListener2 = MediaPlayer.this.mOnCompletionListener;
                                        if (onCompletionListener2 != null && !error_was_handled) {
                                            onCompletionListener2.onCompletion(this.mMediaPlayer);
                                        }
                                        MediaPlayer.this.stayAwake(false);
                                        return;
                                    }
                                    return;
                                default:
                                    switch (i) {
                                        case 200:
                                            int i2 = msg.arg1;
                                            if (i2 == 802) {
                                                try {
                                                    MediaPlayer.this.scanInternalSubtitleTracks();
                                                } catch (RuntimeException e2) {
                                                    sendMessage(obtainMessage(100, 1, MediaPlayer.MEDIA_ERROR_UNSUPPORTED, null));
                                                }
                                            } else if (i2 != 803) {
                                                switch (i2) {
                                                    case 700:
                                                        Log.i(MediaPlayer.TAG, "Info (" + msg.arg1 + SmsManager.REGEX_PREFIX_DELIMITER + msg.arg2 + ")");
                                                        break;
                                                    case 701:
                                                    case 702:
                                                        TimeProvider timeProvider4 = MediaPlayer.this.mTimeProvider;
                                                        if (timeProvider4 != null) {
                                                            if (msg.arg1 == 701) {
                                                                z = true;
                                                            }
                                                            timeProvider4.onBuffering(z);
                                                            break;
                                                        }
                                                        break;
                                                }
                                                onInfoListener = MediaPlayer.this.mOnInfoListener;
                                                if (onInfoListener == null) {
                                                    onInfoListener.onInfo(this.mMediaPlayer, msg.arg1, msg.arg2);
                                                    return;
                                                }
                                                return;
                                            }
                                            msg.arg1 = 802;
                                            if (MediaPlayer.this.mSubtitleController != null) {
                                                MediaPlayer.this.mSubtitleController.selectDefaultTrack();
                                            }
                                            onInfoListener = MediaPlayer.this.mOnInfoListener;
                                            if (onInfoListener == null) {
                                            }
                                        case 201:
                                            synchronized (this) {
                                                if (!MediaPlayer.this.mSubtitleDataListenerDisabled) {
                                                    final OnSubtitleDataListener extSubtitleListener = MediaPlayer.this.mExtSubtitleDataListener;
                                                    Handler extSubtitleHandler = MediaPlayer.this.mExtSubtitleDataHandler;
                                                    break;
                                                } else {
                                                    return;
                                                }
                                            }
                                        case 202:
                                            OnTimedMetaDataAvailableListener onTimedMetaDataAvailableListener = MediaPlayer.this.mOnTimedMetaDataAvailableListener;
                                            if (onTimedMetaDataAvailableListener != null && (msg.obj instanceof Parcel)) {
                                                Parcel parcel3 = (Parcel) msg.obj;
                                                TimedMetaData data = TimedMetaData.createTimedMetaDataFromParcel(parcel3);
                                                parcel3.recycle();
                                                onTimedMetaDataAvailableListener.onTimedMetaDataAvailable(this.mMediaPlayer, data);
                                                return;
                                            }
                                            return;
                                        default:
                                            Log.e(MediaPlayer.TAG, "Unknown message type " + msg.what);
                                            return;
                                    }
                                    break;
                            }
                    }
                    TimeProvider timeProvider5 = MediaPlayer.this.mTimeProvider;
                    if (timeProvider5 != null) {
                        timeProvider5.onSeekComplete(this.mMediaPlayer);
                    }
                } else {
                    AudioManager.resetAudioPortGeneration();
                    synchronized (MediaPlayer.this.mRoutingChangeListeners) {
                        for (NativeRoutingEventHandlerDelegate delegate : MediaPlayer.this.mRoutingChangeListeners.values()) {
                            delegate.notifyClient();
                        }
                    }
                }
            } else {
                Log.v(MediaPlayer.TAG, "MEDIA_DRM_INFO " + MediaPlayer.this.mOnDrmInfoHandlerDelegate);
                if (msg.obj == null) {
                    Log.w(MediaPlayer.TAG, "MEDIA_DRM_INFO msg.obj=NULL");
                } else if (msg.obj instanceof Parcel) {
                    DrmInfo drmInfo = null;
                    synchronized (MediaPlayer.this.mDrmLock) {
                        if (!(MediaPlayer.this.mOnDrmInfoHandlerDelegate == null || MediaPlayer.this.mDrmInfo == null)) {
                            drmInfo = MediaPlayer.this.mDrmInfo.makeCopy();
                        }
                        onDrmInfoHandlerDelegate = MediaPlayer.this.mOnDrmInfoHandlerDelegate;
                    }
                    if (onDrmInfoHandlerDelegate != null) {
                        onDrmInfoHandlerDelegate.notifyClient(drmInfo);
                    }
                } else {
                    Log.w(MediaPlayer.TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + msg.obj);
                }
            }
        }
    }

    private static void postEventFromNative(Object mediaplayer_ref, int what, int arg1, int arg2, Object obj) {
        MediaPlayer mp = (MediaPlayer) ((WeakReference) mediaplayer_ref).get();
        if (mp != null) {
            if (what == 1) {
                synchronized (mp.mDrmLock) {
                    mp.mDrmInfoResolved = true;
                }
            } else if (what != 200) {
                if (what == 210) {
                    Log.v(TAG, "postEventFromNative MEDIA_DRM_INFO");
                    if (obj instanceof Parcel) {
                        DrmInfo drmInfo = new DrmInfo((Parcel) obj);
                        synchronized (mp.mDrmLock) {
                            mp.mDrmInfo = drmInfo;
                        }
                    } else {
                        Log.w(TAG, "MEDIA_DRM_INFO msg.obj of unexpected type " + obj);
                    }
                }
            } else if (arg1 == 2) {
                new Thread(new Runnable() {
                    /* class android.media.MediaPlayer.AnonymousClass6 */

                    public void run() {
                        MediaPlayer.this.start();
                    }
                }).start();
                Thread.yield();
            }
            EventHandler eventHandler = mp.mEventHandler;
            if (eventHandler != null) {
                mp.mEventHandler.sendMessage(eventHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    public OnPreparedListener getOnPreparedListener() {
        return this.mOnPreparedListener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    public OnCompletionListener getOnCompletionListener() {
        return this.mOnCompletionListener;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        this.mOnBufferingUpdateListener = listener;
    }

    public OnBufferingUpdateListener getOnBufferingUpdateListener() {
        return this.mOnBufferingUpdateListener;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    public OnSeekCompleteListener getOnSeekCompleteListener() {
        return this.mOnSeekCompleteListener;
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    public OnVideoSizeChangedListener getOnVideoSizeChangedListener() {
        return this.mOnVideoSizeChangedListener;
    }

    public void setOnTimedTextListener(OnTimedTextListener listener) {
        this.mOnTimedTextListener = listener;
    }

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("Illegal null listener");
        } else if (handler != null) {
            setOnSubtitleDataListenerInt(listener, handler);
        } else {
            throw new IllegalArgumentException("Illegal null handler");
        }
    }

    public void setOnSubtitleDataListener(OnSubtitleDataListener listener) {
        if (listener != null) {
            setOnSubtitleDataListenerInt(listener, null);
            return;
        }
        throw new IllegalArgumentException("Illegal null listener");
    }

    public void clearOnSubtitleDataListener() {
        setOnSubtitleDataListenerInt(null, null);
    }

    private void setOnSubtitleDataListenerInt(OnSubtitleDataListener listener, Handler handler) {
        synchronized (this) {
            this.mExtSubtitleDataListener = listener;
            this.mExtSubtitleDataHandler = handler;
        }
    }

    public void setOnMediaTimeDiscontinuityListener(OnMediaTimeDiscontinuityListener listener, Handler handler) {
        if (listener == null) {
            throw new IllegalArgumentException("Illegal null listener");
        } else if (handler != null) {
            setOnMediaTimeDiscontinuityListenerInt(listener, handler);
        } else {
            throw new IllegalArgumentException("Illegal null handler");
        }
    }

    public void setOnMediaTimeDiscontinuityListener(OnMediaTimeDiscontinuityListener listener) {
        if (listener != null) {
            setOnMediaTimeDiscontinuityListenerInt(listener, null);
            return;
        }
        throw new IllegalArgumentException("Illegal null listener");
    }

    public void clearOnMediaTimeDiscontinuityListener() {
        setOnMediaTimeDiscontinuityListenerInt(null, null);
    }

    private void setOnMediaTimeDiscontinuityListenerInt(OnMediaTimeDiscontinuityListener listener, Handler handler) {
        synchronized (this) {
            this.mOnMediaTimeDiscontinuityListener = listener;
            this.mOnMediaTimeDiscontinuityHandler = handler;
        }
    }

    public void setOnTimedMetaDataAvailableListener(OnTimedMetaDataAvailableListener listener) {
        this.mOnTimedMetaDataAvailableListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    public OnErrorListener getOnErrorListener() {
        return this.mOnErrorListener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    public OnInfoListener getOnInfoListener() {
        return this.mOnInfoListener;
    }

    public void setOnDrmConfigHelper(OnDrmConfigHelper listener) {
        synchronized (this.mDrmLock) {
            this.mOnDrmConfigHelper = listener;
        }
    }

    public void setOnDrmInfoListener(OnDrmInfoListener listener) {
        setOnDrmInfoListener(listener, null);
    }

    public void setOnDrmInfoListener(OnDrmInfoListener listener, Handler handler) {
        synchronized (this.mDrmLock) {
            if (listener != null) {
                this.mOnDrmInfoHandlerDelegate = new OnDrmInfoHandlerDelegate(this, listener, handler);
            } else {
                this.mOnDrmInfoHandlerDelegate = null;
            }
        }
    }

    public void setOnDrmPreparedListener(OnDrmPreparedListener listener) {
        setOnDrmPreparedListener(listener, null);
    }

    public void setOnDrmPreparedListener(OnDrmPreparedListener listener, Handler handler) {
        synchronized (this.mDrmLock) {
            if (listener != null) {
                this.mOnDrmPreparedHandlerDelegate = new OnDrmPreparedHandlerDelegate(this, listener, handler);
            } else {
                this.mOnDrmPreparedHandlerDelegate = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnDrmInfoHandlerDelegate {
        private Handler mHandler;
        private MediaPlayer mMediaPlayer;
        private OnDrmInfoListener mOnDrmInfoListener;

        OnDrmInfoHandlerDelegate(MediaPlayer mp, OnDrmInfoListener listener, Handler handler) {
            this.mMediaPlayer = mp;
            this.mOnDrmInfoListener = listener;
            if (handler != null) {
                this.mHandler = handler;
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyClient(final DrmInfo drmInfo) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.media.MediaPlayer.OnDrmInfoHandlerDelegate.AnonymousClass1 */

                    public void run() {
                        OnDrmInfoHandlerDelegate.this.mOnDrmInfoListener.onDrmInfo(OnDrmInfoHandlerDelegate.this.mMediaPlayer, drmInfo);
                    }
                });
            } else {
                this.mOnDrmInfoListener.onDrmInfo(this.mMediaPlayer, drmInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnDrmPreparedHandlerDelegate {
        private Handler mHandler;
        private MediaPlayer mMediaPlayer;
        private OnDrmPreparedListener mOnDrmPreparedListener;

        OnDrmPreparedHandlerDelegate(MediaPlayer mp, OnDrmPreparedListener listener, Handler handler) {
            this.mMediaPlayer = mp;
            this.mOnDrmPreparedListener = listener;
            if (handler != null) {
                this.mHandler = handler;
            } else if (MediaPlayer.this.mEventHandler != null) {
                this.mHandler = MediaPlayer.this.mEventHandler;
            } else {
                Log.e(MediaPlayer.TAG, "OnDrmPreparedHandlerDelegate: Unexpected null mEventHandler");
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyClient(final int status) {
            Handler handler = this.mHandler;
            if (handler != null) {
                handler.post(new Runnable() {
                    /* class android.media.MediaPlayer.OnDrmPreparedHandlerDelegate.AnonymousClass1 */

                    public void run() {
                        OnDrmPreparedHandlerDelegate.this.mOnDrmPreparedListener.onDrmPrepared(OnDrmPreparedHandlerDelegate.this.mMediaPlayer, status);
                    }
                });
            } else {
                Log.e(MediaPlayer.TAG, "OnDrmPreparedHandlerDelegate:notifyClient: Unexpected null mHandler");
            }
        }
    }

    public DrmInfo getDrmInfo() {
        DrmInfo drmInfo = null;
        synchronized (this.mDrmLock) {
            if (!this.mDrmInfoResolved) {
                if (this.mDrmInfo == null) {
                    Log.v(TAG, "The Player has not been prepared yet");
                    throw new IllegalStateException("The Player has not been prepared yet");
                }
            }
            if (this.mDrmInfo != null) {
                drmInfo = this.mDrmInfo.makeCopy();
            }
        }
        return drmInfo;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0062, code lost:
        if (0 != 0) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0064, code lost:
        cleanDrmObj();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00cb, code lost:
        if (0 != 0) goto L_0x0064;
     */
    public void prepareDrm(UUID uuid) throws UnsupportedSchemeException, ResourceBusyException, ProvisioningNetworkErrorException, ProvisioningServerErrorException {
        OnDrmPreparedHandlerDelegate onDrmPreparedHandlerDelegate;
        Log.v(TAG, "prepareDrm: uuid: " + uuid + " mOnDrmConfigHelper: " + this.mOnDrmConfigHelper);
        boolean allDoneWithoutProvisioning = false;
        synchronized (this.mDrmLock) {
            if (this.mDrmInfo == null) {
                Log.e(TAG, "prepareDrm(): Wrong usage: The player must be prepared and DRM info be retrieved before this call.");
                throw new IllegalStateException("prepareDrm(): Wrong usage: The player must be prepared and DRM info be retrieved before this call.");
            } else if (this.mActiveDrmScheme) {
                String msg = "prepareDrm(): Wrong usage: There is already an active DRM scheme with " + this.mDrmUUID;
                Log.e(TAG, msg);
                throw new IllegalStateException(msg);
            } else if (this.mPrepareDrmInProgress) {
                Log.e(TAG, "prepareDrm(): Wrong usage: There is already a pending prepareDrm call.");
                throw new IllegalStateException("prepareDrm(): Wrong usage: There is already a pending prepareDrm call.");
            } else if (!this.mDrmProvisioningInProgress) {
                cleanDrmObj();
                this.mPrepareDrmInProgress = true;
                onDrmPreparedHandlerDelegate = this.mOnDrmPreparedHandlerDelegate;
                try {
                    prepareDrm_createDrmStep(uuid);
                    this.mDrmConfigAllowed = true;
                } catch (Exception e) {
                    Log.w(TAG, "prepareDrm(): Exception ", e);
                    this.mPrepareDrmInProgress = false;
                    throw e;
                }
            } else {
                Log.e(TAG, "prepareDrm(): Unexpectd: Provisioning is already in progress.");
                throw new IllegalStateException("prepareDrm(): Unexpectd: Provisioning is already in progress.");
            }
        }
        OnDrmConfigHelper onDrmConfigHelper = this.mOnDrmConfigHelper;
        if (onDrmConfigHelper != null) {
            onDrmConfigHelper.onDrmConfig(this);
        }
        synchronized (this.mDrmLock) {
            this.mDrmConfigAllowed = false;
            try {
                prepareDrm_openSessionStep(uuid);
                this.mDrmUUID = uuid;
                this.mActiveDrmScheme = true;
                allDoneWithoutProvisioning = true;
                if (!this.mDrmProvisioningInProgress) {
                    this.mPrepareDrmInProgress = false;
                }
            } catch (IllegalStateException e2) {
                Log.e(TAG, "prepareDrm(): Wrong usage: The player must be in the prepared state to call prepareDrm().");
                throw new IllegalStateException("prepareDrm(): Wrong usage: The player must be in the prepared state to call prepareDrm().");
            } catch (NotProvisionedException e3) {
                Log.w(TAG, "prepareDrm: NotProvisionedException");
                int result = HandleProvisioninig(uuid);
                if (result != 0) {
                    if (result == 1) {
                        Log.e(TAG, "prepareDrm: Provisioning was required but failed due to a network error.");
                        throw new ProvisioningNetworkErrorException("prepareDrm: Provisioning was required but failed due to a network error.");
                    } else if (result != 2) {
                        Log.e(TAG, "prepareDrm: Post-provisioning preparation failed.");
                        throw new IllegalStateException("prepareDrm: Post-provisioning preparation failed.");
                    } else {
                        Log.e(TAG, "prepareDrm: Provisioning was required but the request was denied by the server.");
                        throw new ProvisioningServerErrorException("prepareDrm: Provisioning was required but the request was denied by the server.");
                    }
                } else if (!this.mDrmProvisioningInProgress) {
                    this.mPrepareDrmInProgress = false;
                }
            } catch (Exception e4) {
                Log.e(TAG, "prepareDrm: Exception " + e4);
                throw e4;
            } catch (Throwable th) {
                if (!this.mDrmProvisioningInProgress) {
                    this.mPrepareDrmInProgress = false;
                }
                if (0 != 0) {
                    cleanDrmObj();
                }
                throw th;
            }
        }
        if (allDoneWithoutProvisioning && onDrmPreparedHandlerDelegate != null) {
            onDrmPreparedHandlerDelegate.notifyClient(0);
        }
    }

    public void releaseDrm() throws NoDrmSchemeException {
        Log.v(TAG, "releaseDrm:");
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                try {
                    _releaseDrm();
                    cleanDrmObj();
                    this.mActiveDrmScheme = false;
                } catch (IllegalStateException e) {
                    Log.w(TAG, "releaseDrm: Exception ", e);
                    throw new IllegalStateException("releaseDrm: The player is not in a valid state.");
                } catch (Exception e2) {
                    Log.e(TAG, "releaseDrm: Exception ", e2);
                }
            } else {
                Log.e(TAG, "releaseDrm(): No active DRM scheme to release.");
                throw new NoDrmSchemeException("releaseDrm: No active DRM scheme to release.");
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: android.media.MediaDrm */
    /* JADX WARN: Multi-variable type inference failed */
    public MediaDrm.KeyRequest getKeyRequest(byte[] keySetId, byte[] initData, String mimeType, int keyType, Map<String, String> optionalParameters) throws NoDrmSchemeException {
        byte[] scope;
        HashMap hashMap;
        MediaDrm.KeyRequest request;
        Log.v(TAG, "getKeyRequest:  keySetId: " + keySetId + " initData:" + initData + " mimeType: " + mimeType + " keyType: " + keyType + " optionalParameters: " + optionalParameters);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keyType != 3) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "getKeyRequest NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("getKeyRequest: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "getKeyRequest Exception " + e2);
                        throw e2;
                    }
                } else {
                    scope = keySetId;
                }
                if (optionalParameters != null) {
                    hashMap = new HashMap(optionalParameters);
                } else {
                    hashMap = null;
                }
                request = this.mDrmObj.getKeyRequest(scope, initData, mimeType, keyType, hashMap);
                Log.v(TAG, "getKeyRequest:   --> request: " + request);
            } else {
                Log.e(TAG, "getKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeException("getKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return request;
    }

    public byte[] provideKeyResponse(byte[] keySetId, byte[] response) throws NoDrmSchemeException, DeniedByServerException {
        byte[] scope;
        byte[] keySetResult;
        Log.v(TAG, "provideKeyResponse: keySetId: " + keySetId + " response: " + response);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                if (keySetId == null) {
                    try {
                        scope = this.mDrmSessionId;
                    } catch (NotProvisionedException e) {
                        Log.w(TAG, "provideKeyResponse NotProvisionedException: Unexpected. Shouldn't have reached here.");
                        throw new IllegalStateException("provideKeyResponse: Unexpected provisioning error.");
                    } catch (Exception e2) {
                        Log.w(TAG, "provideKeyResponse Exception " + e2);
                        throw e2;
                    }
                } else {
                    scope = keySetId;
                }
                keySetResult = this.mDrmObj.provideKeyResponse(scope, response);
                Log.v(TAG, "provideKeyResponse: keySetId: " + keySetId + " response: " + response + " --> " + keySetResult);
            } else {
                Log.e(TAG, "getKeyRequest NoDrmSchemeException");
                throw new NoDrmSchemeException("getKeyRequest: Has to set a DRM scheme first.");
            }
        }
        return keySetResult;
    }

    public void restoreKeys(byte[] keySetId) throws NoDrmSchemeException {
        Log.v(TAG, "restoreKeys: keySetId: " + keySetId);
        synchronized (this.mDrmLock) {
            if (this.mActiveDrmScheme) {
                try {
                    this.mDrmObj.restoreKeys(this.mDrmSessionId, keySetId);
                } catch (Exception e) {
                    Log.w(TAG, "restoreKeys Exception " + e);
                    throw e;
                }
            } else {
                Log.w(TAG, "restoreKeys NoDrmSchemeException");
                throw new NoDrmSchemeException("restoreKeys: Has to set a DRM scheme first.");
            }
        }
    }

    public String getDrmPropertyString(String propertyName) throws NoDrmSchemeException {
        String value;
        Log.v(TAG, "getDrmPropertyString: propertyName: " + propertyName);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "getDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeException("getDrmPropertyString: Has to prepareDrm() first.");
                }
            }
            try {
                value = this.mDrmObj.getPropertyString(propertyName);
            } catch (Exception e) {
                Log.w(TAG, "getDrmPropertyString Exception " + e);
                throw e;
            }
        }
        Log.v(TAG, "getDrmPropertyString: propertyName: " + propertyName + " --> value: " + value);
        return value;
    }

    public void setDrmPropertyString(String propertyName, String value) throws NoDrmSchemeException {
        Log.v(TAG, "setDrmPropertyString: propertyName: " + propertyName + " value: " + value);
        synchronized (this.mDrmLock) {
            if (!this.mActiveDrmScheme) {
                if (!this.mDrmConfigAllowed) {
                    Log.w(TAG, "setDrmPropertyString NoDrmSchemeException");
                    throw new NoDrmSchemeException("setDrmPropertyString: Has to prepareDrm() first.");
                }
            }
            try {
                this.mDrmObj.setPropertyString(propertyName, value);
            } catch (Exception e) {
                Log.w(TAG, "setDrmPropertyString Exception " + e);
                throw e;
            }
        }
    }

    public static final class DrmInfo {
        private Map<UUID, byte[]> mapPssh;
        private UUID[] supportedSchemes;

        public Map<UUID, byte[]> getPssh() {
            return this.mapPssh;
        }

        public UUID[] getSupportedSchemes() {
            return this.supportedSchemes;
        }

        private DrmInfo(Map<UUID, byte[]> Pssh, UUID[] SupportedSchemes) {
            this.mapPssh = Pssh;
            this.supportedSchemes = SupportedSchemes;
        }

        private DrmInfo(Parcel parcel) {
            Log.v(MediaPlayer.TAG, "DrmInfo(" + parcel + ") size " + parcel.dataSize());
            int psshsize = parcel.readInt();
            byte[] pssh = new byte[psshsize];
            parcel.readByteArray(pssh);
            Log.v(MediaPlayer.TAG, "DrmInfo() PSSH: " + arrToHex(pssh));
            this.mapPssh = parsePSSH(pssh, psshsize);
            Log.v(MediaPlayer.TAG, "DrmInfo() PSSH: " + this.mapPssh);
            int supportedDRMsCount = parcel.readInt();
            this.supportedSchemes = new UUID[supportedDRMsCount];
            for (int i = 0; i < supportedDRMsCount; i++) {
                byte[] uuid = new byte[16];
                parcel.readByteArray(uuid);
                this.supportedSchemes[i] = bytesToUUID(uuid);
                Log.v(MediaPlayer.TAG, "DrmInfo() supportedScheme[" + i + "]: " + this.supportedSchemes[i]);
            }
            Log.v(MediaPlayer.TAG, "DrmInfo() Parcel psshsize: " + psshsize + " supportedDRMsCount: " + supportedDRMsCount);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private DrmInfo makeCopy() {
            return new DrmInfo(this.mapPssh, this.supportedSchemes);
        }

        private String arrToHex(byte[] bytes) {
            String out = "0x";
            for (int i = 0; i < bytes.length; i++) {
                out = out + String.format("%02x", Byte.valueOf(bytes[i]));
            }
            return out;
        }

        private UUID bytesToUUID(byte[] uuid) {
            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb |= (((long) uuid[i]) & 255) << ((7 - i) * 8);
                lsb |= (((long) uuid[i + 8]) & 255) << ((7 - i) * 8);
            }
            return new UUID(msb, lsb);
        }

        private Map<UUID, byte[]> parsePSSH(byte[] pssh, int psshsize) {
            int datalen;
            Map<UUID, byte[]> result = new HashMap<>();
            int len = psshsize;
            int numentries = 0;
            int i = 0;
            while (len > 0) {
                if (len < 16) {
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse UUID: (%d < 16) pssh: %d", Integer.valueOf(len), Integer.valueOf(psshsize)));
                    return null;
                }
                UUID uuid = bytesToUUID(Arrays.copyOfRange(pssh, i, i + 16));
                int i2 = i + 16;
                int len2 = len - 16;
                if (len2 < 4) {
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse datalen: (%d < 4) pssh: %d", Integer.valueOf(len2), Integer.valueOf(psshsize)));
                    return null;
                }
                byte[] subset = Arrays.copyOfRange(pssh, i2, i2 + 4);
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    datalen = ((subset[3] & 255) << 24) | ((subset[2] & 255) << 16) | ((subset[1] & 255) << 8) | (subset[0] & 255);
                } else {
                    datalen = ((subset[0] & 255) << 24) | ((subset[1] & 255) << 16) | ((subset[2] & 255) << 8) | (subset[3] & 255);
                }
                int i3 = i2 + 4;
                int len3 = len2 - 4;
                if (len3 < datalen) {
                    Log.w(MediaPlayer.TAG, String.format("parsePSSH: len is too short to parse data: (%d < %d) pssh: %d", Integer.valueOf(len3), Integer.valueOf(datalen), Integer.valueOf(psshsize)));
                    return null;
                }
                byte[] data = Arrays.copyOfRange(pssh, i3, i3 + datalen);
                i = i3 + datalen;
                len = len3 - datalen;
                Log.v(MediaPlayer.TAG, String.format("parsePSSH[%d]: <%s, %s> pssh: %d", Integer.valueOf(numentries), uuid, arrToHex(data), Integer.valueOf(psshsize)));
                numentries++;
                result.put(uuid, data);
            }
            return result;
        }
    }

    public static final class NoDrmSchemeException extends MediaDrmException {
        public NoDrmSchemeException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ProvisioningNetworkErrorException extends MediaDrmException {
        public ProvisioningNetworkErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static final class ProvisioningServerErrorException extends MediaDrmException {
        public ProvisioningServerErrorException(String detailMessage) {
            super(detailMessage);
        }
    }

    private void prepareDrm_createDrmStep(UUID uuid) throws UnsupportedSchemeException {
        Log.v(TAG, "prepareDrm_createDrmStep: UUID: " + uuid);
        try {
            this.mDrmObj = new MediaDrm(uuid);
            Log.v(TAG, "prepareDrm_createDrmStep: Created mDrmObj=" + this.mDrmObj);
        } catch (Exception e) {
            Log.e(TAG, "prepareDrm_createDrmStep: MediaDrm failed with " + e);
            throw e;
        }
    }

    private void prepareDrm_openSessionStep(UUID uuid) throws NotProvisionedException, ResourceBusyException {
        Log.v(TAG, "prepareDrm_openSessionStep: uuid: " + uuid);
        try {
            this.mDrmSessionId = this.mDrmObj.openSession();
            Log.v(TAG, "prepareDrm_openSessionStep: mDrmSessionId=" + this.mDrmSessionId);
            _prepareDrm(getByteArrayFromUUID(uuid), this.mDrmSessionId);
            Log.v(TAG, "prepareDrm_openSessionStep: _prepareDrm/Crypto succeeded");
        } catch (Exception e) {
            Log.e(TAG, "prepareDrm_openSessionStep: open/crypto failed with " + e);
            throw e;
        }
    }

    /* access modifiers changed from: private */
    public class ProvisioningThread extends Thread {
        public static final int TIMEOUT_MS = 60000;
        private Object drmLock;
        private boolean finished;
        private MediaPlayer mediaPlayer;
        private OnDrmPreparedHandlerDelegate onDrmPreparedHandlerDelegate;
        private int status;
        private String urlStr;
        private UUID uuid;

        private ProvisioningThread() {
        }

        public int status() {
            return this.status;
        }

        public ProvisioningThread initialize(MediaDrm.ProvisionRequest request, UUID uuid2, MediaPlayer mediaPlayer2) {
            this.drmLock = mediaPlayer2.mDrmLock;
            this.onDrmPreparedHandlerDelegate = mediaPlayer2.mOnDrmPreparedHandlerDelegate;
            this.mediaPlayer = mediaPlayer2;
            this.urlStr = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
            this.uuid = uuid2;
            this.status = 3;
            Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread is initialised url: " + this.urlStr);
            return this;
        }

        public void run() {
            byte[] response = null;
            boolean provisioningSucceeded = false;
            try {
                URL url = new URL(this.urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(false);
                    connection.setDoInput(true);
                    connection.setConnectTimeout(TIMEOUT_MS);
                    connection.setReadTimeout(TIMEOUT_MS);
                    connection.connect();
                    response = Streams.readFully(connection.getInputStream());
                    Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread run: response " + response.length + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + response);
                } catch (Exception e) {
                    this.status = 1;
                    Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: connect " + e + " url: " + url);
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                this.status = 1;
                Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: openConnection " + e2);
            }
            if (response != null) {
                try {
                    MediaPlayer.this.mDrmObj.provideProvisionResponse(response);
                    Log.v(MediaPlayer.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse SUCCEEDED!");
                    provisioningSucceeded = true;
                } catch (Exception e3) {
                    this.status = 2;
                    Log.w(MediaPlayer.TAG, "HandleProvisioninig: Thread run: provideProvisionResponse " + e3);
                }
            }
            boolean succeeded = false;
            int i = 3;
            if (this.onDrmPreparedHandlerDelegate != null) {
                synchronized (this.drmLock) {
                    if (provisioningSucceeded) {
                        succeeded = this.mediaPlayer.resumePrepareDrm(this.uuid);
                        if (succeeded) {
                            i = 0;
                        }
                        this.status = i;
                    }
                    this.mediaPlayer.mDrmProvisioningInProgress = false;
                    this.mediaPlayer.mPrepareDrmInProgress = false;
                    if (!succeeded) {
                        MediaPlayer.this.cleanDrmObj();
                    }
                }
                this.onDrmPreparedHandlerDelegate.notifyClient(this.status);
            } else {
                if (provisioningSucceeded) {
                    succeeded = this.mediaPlayer.resumePrepareDrm(this.uuid);
                    if (succeeded) {
                        i = 0;
                    }
                    this.status = i;
                }
                this.mediaPlayer.mDrmProvisioningInProgress = false;
                this.mediaPlayer.mPrepareDrmInProgress = false;
                if (!succeeded) {
                    MediaPlayer.this.cleanDrmObj();
                }
            }
            this.finished = true;
        }
    }

    private int HandleProvisioninig(UUID uuid) {
        if (this.mDrmProvisioningInProgress) {
            Log.e(TAG, "HandleProvisioninig: Unexpected mDrmProvisioningInProgress");
            return 3;
        }
        MediaDrm.ProvisionRequest provReq = this.mDrmObj.getProvisionRequest();
        if (provReq == null) {
            Log.e(TAG, "HandleProvisioninig: getProvisionRequest returned null.");
            return 3;
        }
        Log.v(TAG, "HandleProvisioninig provReq  data: " + provReq.getData() + " url: " + provReq.getDefaultUrl());
        this.mDrmProvisioningInProgress = true;
        this.mDrmProvisioningThread = new ProvisioningThread().initialize(provReq, uuid, this);
        this.mDrmProvisioningThread.start();
        if (this.mOnDrmPreparedHandlerDelegate != null) {
            return 0;
        }
        try {
            this.mDrmProvisioningThread.join();
        } catch (Exception e) {
            Log.w(TAG, "HandleProvisioninig: Thread.join Exception " + e);
        }
        int result = this.mDrmProvisioningThread.status();
        this.mDrmProvisioningThread = null;
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean resumePrepareDrm(UUID uuid) {
        Log.v(TAG, "resumePrepareDrm: uuid: " + uuid);
        try {
            prepareDrm_openSessionStep(uuid);
            this.mDrmUUID = uuid;
            this.mActiveDrmScheme = true;
            return true;
        } catch (Exception e) {
            Log.w(TAG, "HandleProvisioninig: Thread run _prepareDrm resume failed with " + e);
            return false;
        }
    }

    private void resetDrmState() {
        synchronized (this.mDrmLock) {
            Log.v(TAG, "resetDrmState:  mDrmInfo=" + this.mDrmInfo + " mDrmProvisioningThread=" + this.mDrmProvisioningThread + " mPrepareDrmInProgress=" + this.mPrepareDrmInProgress + " mActiveDrmScheme=" + this.mActiveDrmScheme);
            this.mDrmInfoResolved = false;
            this.mDrmInfo = null;
            if (this.mDrmProvisioningThread != null) {
                try {
                    this.mDrmProvisioningThread.join();
                } catch (InterruptedException e) {
                    Log.w(TAG, "resetDrmState: ProvThread.join Exception " + e);
                }
                this.mDrmProvisioningThread = null;
            }
            this.mPrepareDrmInProgress = false;
            this.mActiveDrmScheme = false;
            cleanDrmObj();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanDrmObj() {
        Log.v(TAG, "cleanDrmObj: mDrmObj=" + this.mDrmObj + " mDrmSessionId=" + this.mDrmSessionId);
        byte[] bArr = this.mDrmSessionId;
        if (bArr != null) {
            this.mDrmObj.closeSession(bArr);
            this.mDrmSessionId = null;
        }
        MediaDrm mediaDrm = this.mDrmObj;
        if (mediaDrm != null) {
            mediaDrm.release();
            this.mDrmObj = null;
        }
    }

    private static final byte[] getByteArrayFromUUID(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] uuidBytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            uuidBytes[i] = (byte) ((int) (msb >>> ((7 - i) * 8)));
            uuidBytes[i + 8] = (byte) ((int) (lsb >>> ((7 - i) * 8)));
        }
        return uuidBytes;
    }

    private boolean isVideoScalingModeSupported(int mode) {
        return mode == 1 || mode == 2;
    }

    /* access modifiers changed from: package-private */
    public static class TimeProvider implements OnSeekCompleteListener, MediaTimeProvider {
        private static final long MAX_EARLY_CALLBACK_US = 1000;
        private static final long MAX_NS_WITHOUT_POSITION_CHECK = 5000000000L;
        private static final int NOTIFY = 1;
        private static final int NOTIFY_SEEK = 3;
        private static final int NOTIFY_STOP = 2;
        private static final int NOTIFY_TIME = 0;
        private static final int NOTIFY_TRACK_DATA = 4;
        private static final String TAG = "MTP";
        private static final long TIME_ADJUSTMENT_RATE = 2;
        public boolean DEBUG = (!"user".equals(Build.TYPE));
        private boolean mBuffering;
        private Handler mEventHandler;
        private HandlerThread mHandlerThread;
        private long mLastReportedTime;
        private long mLastTimeUs = 0;
        private MediaTimeProvider.OnMediaTimeListener[] mListeners;
        private boolean mPaused = true;
        private boolean mPausing = false;
        private MediaPlayer mPlayer;
        private boolean mRefresh = false;
        private boolean mSeeking = false;
        private boolean mStopped = true;
        private long[] mTimes;

        public TimeProvider(MediaPlayer mp) {
            this.mPlayer = mp;
            try {
                getCurrentTimeUs(true, false);
            } catch (IllegalStateException e) {
                this.mRefresh = true;
            }
            Looper myLooper = Looper.myLooper();
            Looper looper = myLooper;
            if (myLooper == null) {
                Looper mainLooper = Looper.getMainLooper();
                looper = mainLooper;
                if (mainLooper == null) {
                    this.mHandlerThread = new HandlerThread("MediaPlayerMTPEventThread", -2);
                    this.mHandlerThread.start();
                    looper = this.mHandlerThread.getLooper();
                }
            }
            this.mEventHandler = new EventHandler(looper);
            this.mListeners = new MediaTimeProvider.OnMediaTimeListener[0];
            this.mTimes = new long[0];
            this.mLastTimeUs = 0;
        }

        private void scheduleNotification(int type, long delayUs) {
            if (!this.mSeeking || type != 0) {
                if (this.DEBUG) {
                    Log.v(TAG, "scheduleNotification " + type + " in " + delayUs);
                }
                this.mEventHandler.removeMessages(1);
                this.mEventHandler.sendMessageDelayed(this.mEventHandler.obtainMessage(1, type, 0), (long) ((int) (delayUs / 1000)));
            }
        }

        public void close() {
            this.mEventHandler.removeMessages(1);
            HandlerThread handlerThread = this.mHandlerThread;
            if (handlerThread != null) {
                handlerThread.quitSafely();
                this.mHandlerThread = null;
            }
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            HandlerThread handlerThread = this.mHandlerThread;
            if (handlerThread != null) {
                handlerThread.quitSafely();
            }
        }

        public void onNotifyTime() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onNotifyTime: ");
                }
                scheduleNotification(0, 0);
            }
        }

        public void onPaused(boolean paused) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onPaused: " + paused);
                }
                if (this.mStopped) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    scheduleNotification(3, 0);
                } else {
                    this.mPausing = paused;
                    this.mSeeking = false;
                    scheduleNotification(0, 0);
                }
            }
        }

        public void onBuffering(boolean buffering) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onBuffering: " + buffering);
                }
                this.mBuffering = buffering;
                scheduleNotification(0, 0);
            }
        }

        public void onStopped() {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "onStopped");
                }
                this.mPaused = true;
                this.mStopped = true;
                this.mSeeking = false;
                this.mBuffering = false;
                scheduleNotification(2, 0);
            }
        }

        @Override // android.media.MediaPlayer.OnSeekCompleteListener
        public void onSeekComplete(MediaPlayer mp) {
            synchronized (this) {
                this.mStopped = false;
                this.mSeeking = true;
                scheduleNotification(3, 0);
            }
        }

        public void onNewPlayer() {
            if (this.mRefresh) {
                synchronized (this) {
                    this.mStopped = false;
                    this.mSeeking = true;
                    this.mBuffering = false;
                    scheduleNotification(3, 0);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void notifySeek() {
            this.mSeeking = false;
            try {
                long timeUs = getCurrentTimeUs(true, false);
                if (this.DEBUG) {
                    Log.d(TAG, "onSeekComplete at " + timeUs);
                }
                MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
                int length = onMediaTimeListenerArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    MediaTimeProvider.OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                    if (listener == null) {
                        break;
                    }
                    listener.onSeek(timeUs);
                    i++;
                }
            } catch (IllegalStateException e) {
                if (this.DEBUG) {
                    Log.d(TAG, "onSeekComplete but no player");
                }
                this.mPausing = true;
                notifyTimedEvent(false);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void notifyTrackData(Pair<SubtitleTrack, byte[]> trackData) {
            trackData.first.onData((byte[]) trackData.second, true, -1);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void notifyStop() {
            MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
            int length = onMediaTimeListenerArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                MediaTimeProvider.OnMediaTimeListener listener = onMediaTimeListenerArr[i];
                if (listener == null) {
                    break;
                }
                listener.onStop();
                i++;
            }
        }

        private int registerListener(MediaTimeProvider.OnMediaTimeListener listener) {
            int i = 0;
            while (true) {
                MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr = this.mListeners;
                if (i >= onMediaTimeListenerArr.length || onMediaTimeListenerArr[i] == listener || onMediaTimeListenerArr[i] == null) {
                    MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr2 = this.mListeners;
                } else {
                    i++;
                }
            }
            MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr22 = this.mListeners;
            if (i >= onMediaTimeListenerArr22.length) {
                MediaTimeProvider.OnMediaTimeListener[] newListeners = new MediaTimeProvider.OnMediaTimeListener[(i + 1)];
                long[] newTimes = new long[(i + 1)];
                System.arraycopy(onMediaTimeListenerArr22, 0, newListeners, 0, onMediaTimeListenerArr22.length);
                long[] jArr = this.mTimes;
                System.arraycopy(jArr, 0, newTimes, 0, jArr.length);
                this.mListeners = newListeners;
                this.mTimes = newTimes;
            }
            MediaTimeProvider.OnMediaTimeListener[] onMediaTimeListenerArr3 = this.mListeners;
            if (onMediaTimeListenerArr3[i] == null) {
                onMediaTimeListenerArr3[i] = listener;
                this.mTimes[i] = -1;
            }
            return i;
        }

        @Override // android.media.MediaTimeProvider
        public void notifyAt(long timeUs, MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "notifyAt " + timeUs);
                }
                this.mTimes[registerListener(listener)] = timeUs;
                scheduleNotification(0, 0);
            }
        }

        @Override // android.media.MediaTimeProvider
        public void scheduleUpdate(MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                if (this.DEBUG) {
                    Log.d(TAG, "scheduleUpdate");
                }
                int i = registerListener(listener);
                if (!this.mStopped) {
                    this.mTimes[i] = 0;
                    scheduleNotification(0, 0);
                }
            }
        }

        @Override // android.media.MediaTimeProvider
        public void cancelNotifications(MediaTimeProvider.OnMediaTimeListener listener) {
            synchronized (this) {
                int i = 0;
                while (true) {
                    if (i >= this.mListeners.length) {
                        break;
                    } else if (this.mListeners[i] == listener) {
                        System.arraycopy(this.mListeners, i + 1, this.mListeners, i, (this.mListeners.length - i) - 1);
                        System.arraycopy(this.mTimes, i + 1, this.mTimes, i, (this.mTimes.length - i) - 1);
                        this.mListeners[this.mListeners.length - 1] = null;
                        this.mTimes[this.mTimes.length - 1] = -1;
                        break;
                    } else if (this.mListeners[i] == null) {
                        break;
                    } else {
                        i++;
                    }
                }
                scheduleNotification(0, 0);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private synchronized void notifyTimedEvent(boolean refreshTime) {
            long nowUs;
            long nowUs2;
            long nowUs3;
            try {
                nowUs = getCurrentTimeUs(refreshTime, true);
            } catch (IllegalStateException e) {
                this.mRefresh = true;
                this.mPausing = true;
                nowUs = getCurrentTimeUs(refreshTime, true);
            }
            long nextTimeUs = nowUs;
            if (!this.mSeeking) {
                if (this.DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("notifyTimedEvent(");
                    sb.append(this.mLastTimeUs);
                    sb.append(" -> ");
                    sb.append(nowUs);
                    sb.append(") from {");
                    long[] jArr = this.mTimes;
                    int length = jArr.length;
                    boolean first = true;
                    int i = 0;
                    while (i < length) {
                        long time = jArr[i];
                        if (time != -1) {
                            if (!first) {
                                sb.append(", ");
                            }
                            sb.append(time);
                            first = false;
                        }
                        i++;
                        nowUs = nowUs;
                    }
                    nowUs2 = nowUs;
                    sb.append("}");
                    Log.d(TAG, sb.toString());
                } else {
                    nowUs2 = nowUs;
                }
                Vector<MediaTimeProvider.OnMediaTimeListener> activatedListeners = new Vector<>();
                int ix = 0;
                while (ix < this.mTimes.length && this.mListeners[ix] != null) {
                    if (this.mTimes[ix] > -1) {
                        if (this.mTimes[ix] <= nowUs2 + 1000) {
                            activatedListeners.add(this.mListeners[ix]);
                            if (this.DEBUG) {
                                Log.d(TAG, Environment.MEDIA_REMOVED);
                            }
                            this.mTimes[ix] = -1;
                        } else if (nextTimeUs == nowUs2 || this.mTimes[ix] < nextTimeUs) {
                            nextTimeUs = this.mTimes[ix];
                        }
                    }
                    ix++;
                }
                if (nextTimeUs <= nowUs2 || this.mPaused) {
                    nowUs3 = nowUs2;
                    this.mEventHandler.removeMessages(1);
                } else {
                    if (this.DEBUG) {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("scheduling for ");
                        sb2.append(nextTimeUs);
                        sb2.append(" and ");
                        nowUs3 = nowUs2;
                        sb2.append(nowUs3);
                        Log.d(TAG, sb2.toString());
                    } else {
                        nowUs3 = nowUs2;
                    }
                    this.mPlayer.notifyAt(nextTimeUs);
                }
                Iterator<MediaTimeProvider.OnMediaTimeListener> it = activatedListeners.iterator();
                while (it.hasNext()) {
                    it.next().onTimedEvent(nowUs3);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x002f A[Catch:{ IllegalStateException -> 0x007f }] */
        @Override // android.media.MediaTimeProvider
        public long getCurrentTimeUs(boolean refreshTime, boolean monotonic) throws IllegalStateException {
            boolean z;
            synchronized (this) {
                if (!this.mPaused || refreshTime) {
                    try {
                        this.mLastTimeUs = ((long) this.mPlayer.getCurrentPosition()) * 1000;
                        if (this.mPlayer.isPlaying()) {
                            if (!this.mBuffering) {
                                z = false;
                                this.mPaused = z;
                                if (this.DEBUG) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(this.mPaused ? "paused" : "playing");
                                    sb.append(" at ");
                                    sb.append(this.mLastTimeUs);
                                    Log.v(TAG, sb.toString());
                                }
                                if (monotonic || this.mLastTimeUs >= this.mLastReportedTime) {
                                    this.mLastReportedTime = this.mLastTimeUs;
                                } else if (this.mLastReportedTime - this.mLastTimeUs > TimeUtils.NANOS_PER_MS) {
                                    this.mStopped = false;
                                    this.mSeeking = true;
                                    scheduleNotification(3, 0);
                                }
                                return this.mLastReportedTime;
                            }
                        }
                        z = true;
                        this.mPaused = z;
                        if (this.DEBUG) {
                        }
                        if (monotonic) {
                        }
                        this.mLastReportedTime = this.mLastTimeUs;
                        return this.mLastReportedTime;
                    } catch (IllegalStateException e) {
                        if (this.mPausing) {
                            this.mPausing = false;
                            if (!monotonic || this.mLastReportedTime < this.mLastTimeUs) {
                                this.mLastReportedTime = this.mLastTimeUs;
                            }
                            this.mPaused = true;
                            if (this.DEBUG) {
                                Log.d(TAG, "illegal state, but pausing: estimating at " + this.mLastReportedTime);
                            }
                            return this.mLastReportedTime;
                        }
                        throw e;
                    }
                } else {
                    return this.mLastReportedTime;
                }
            }
        }

        private class EventHandler extends Handler {
            public EventHandler(Looper looper) {
                super(looper);
            }

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (TimeProvider.this.DEBUG) {
                    Log.d(TimeProvider.TAG, "handleMessage_notify msg:(" + msg.what + ", " + msg.arg1 + ", " + msg.arg2 + ")");
                }
                if (msg.what == 1) {
                    int i = msg.arg1;
                    if (i == 0) {
                        TimeProvider.this.notifyTimedEvent(true);
                    } else if (i == 2) {
                        TimeProvider.this.notifyStop();
                    } else if (i == 3) {
                        TimeProvider.this.notifySeek();
                    } else if (i == 4) {
                        TimeProvider.this.notifyTrackData((Pair) msg.obj);
                    }
                }
            }
        }
    }

    public static final class MetricsConstants {
        public static final String CODEC_AUDIO = "android.media.mediaplayer.audio.codec";
        public static final String CODEC_VIDEO = "android.media.mediaplayer.video.codec";
        public static final String DURATION = "android.media.mediaplayer.durationMs";
        public static final String ERRORS = "android.media.mediaplayer.err";
        public static final String ERROR_CODE = "android.media.mediaplayer.errcode";
        public static final String FRAMES = "android.media.mediaplayer.frames";
        public static final String FRAMES_DROPPED = "android.media.mediaplayer.dropped";
        public static final String HEIGHT = "android.media.mediaplayer.height";
        public static final String MIME_TYPE_AUDIO = "android.media.mediaplayer.audio.mime";
        public static final String MIME_TYPE_VIDEO = "android.media.mediaplayer.video.mime";
        public static final String PLAYING = "android.media.mediaplayer.playingMs";
        public static final String WIDTH = "android.media.mediaplayer.width";

        private MetricsConstants() {
        }
    }
}
