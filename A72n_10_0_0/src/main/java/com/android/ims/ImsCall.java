package com.android.ims;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.telecom.ConferenceParticipant;
import android.telephony.CallQuality;
import android.telephony.Rlog;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsCallSession;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.util.Log;
import com.android.ims.internal.ICall;
import com.android.ims.internal.ImsStreamMediaSession;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ImsCall implements ICall {
    private static final boolean CONF_DBG = true;
    private static final boolean DBG = true;
    private static final boolean FORCE_DEBUG = true;
    private static final String TAG = "ImsCall";
    private static final int UPDATE_EXTEND_TO_CONFERENCE = 5;
    private static final int UPDATE_HOLD = 1;
    private static final int UPDATE_HOLD_MERGE = 2;
    protected static final int UPDATE_MERGE = 4;
    protected static final int UPDATE_NONE = 0;
    private static final int UPDATE_RESUME = 3;
    private static final int UPDATE_UNSPECIFIED = 6;
    public static final int USSD_MODE_NOTIFY = 0;
    public static final int USSD_MODE_REQUEST = 1;
    private static final boolean VDBG = true;
    private static final AtomicInteger sUniqueIdGenerator = new AtomicInteger();
    private boolean mAnswerWithRtt = DBG;
    protected ImsCallProfile mCallProfile = null;
    private boolean mCallSessionMergePending = DBG;
    private CopyOnWriteArrayList<ConferenceParticipant> mConferenceParticipants;
    protected Context mContext;
    protected boolean mHold = DBG;
    protected ImsCallSessionListenerProxy mImsCallSessionListenerProxy;
    private boolean mInCall = DBG;
    protected boolean mIsConferenceHost = DBG;
    private boolean mIsMerged = DBG;
    private ImsReasonInfo mLastReasonInfo = null;
    protected Listener mListener = null;
    protected Object mLockObj = new Object();
    private ImsStreamMediaSession mMediaSession = null;
    protected ImsCall mMergeHost = null;
    protected ImsCall mMergePeer = null;
    private boolean mMergeRequestedByConference = DBG;
    private boolean mMute = DBG;
    private int mOverrideReason = 0;
    private ImsCallProfile mProposedCallProfile = null;
    public ImsCallSession mSession = null;
    private boolean mSessionEndDuringMerge = DBG;
    private ImsReasonInfo mSessionEndDuringMergeReasonInfo = null;
    protected boolean mTerminationRequestPending = DBG;
    protected ImsCallSession mTransientConferenceSession = null;
    public int mUpdateRequest = 0;
    private boolean mWasVideoCall = DBG;
    public final int uniqueId;

    public static class Listener {
        public void onCallProgressing(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStarted(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallStartFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallTerminated(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallStateChanged(call);
        }

        public void onCallHeld(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallHoldFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallHoldReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumed(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallResumeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallResumeReceived(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallMerged(ImsCall call, ImsCall peerCall, boolean swapCalls) {
            onCallStateChanged(call);
        }

        public void onCallMergeFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdated(ImsCall call) {
            onCallStateChanged(call);
        }

        public void onCallUpdateFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallUpdateReceived(ImsCall call) {
        }

        public void onCallConferenceExtended(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallConferenceExtendFailed(ImsCall call, ImsReasonInfo reasonInfo) {
            onCallError(call, reasonInfo);
        }

        public void onCallConferenceExtendReceived(ImsCall call, ImsCall newCall) {
            onCallStateChanged(call);
        }

        public void onCallInviteParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallInviteParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallRemoveParticipantsRequestDelivered(ImsCall call) {
        }

        public void onCallRemoveParticipantsRequestFailed(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallConferenceStateUpdated(ImsCall call, ImsConferenceState state) {
        }

        public void onConferenceParticipantsStateChanged(ImsCall call, List<ConferenceParticipant> list) {
        }

        public void onCallUssdMessageReceived(ImsCall call, int mode, String ussdMessage) {
        }

        public void onCallError(ImsCall call, ImsReasonInfo reasonInfo) {
        }

        public void onCallStateChanged(ImsCall call) {
        }

        public void onCallStateChanged(ImsCall call, int state) {
        }

        public void onCallSuppServiceReceived(ImsCall call, ImsSuppServiceNotification suppServiceInfo) {
        }

        public void onCallSessionTtyModeReceived(ImsCall call, int mode) {
        }

        public void onCallHandover(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onRttModifyRequestReceived(ImsCall imsCall) {
        }

        public void onRttModifyResponseReceived(ImsCall imsCall, int status) {
        }

        public void onRttMessageReceived(ImsCall imsCall, String message) {
        }

        public void onCallHandoverFailed(ImsCall imsCall, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
        }

        public void onMultipartyStateChanged(ImsCall imsCall, boolean isMultiParty) {
        }

        public void onRttAudioIndicatorChanged(ImsCall imsCall, ImsStreamMediaProfile profile) {
        }

        public void onCallQualityChanged(ImsCall imsCall, CallQuality callQuality) {
        }
    }

    public ImsCall(Context context, ImsCallProfile profile) {
        this.mContext = context;
        setCallProfile(profile);
        this.uniqueId = sUniqueIdGenerator.getAndIncrement();
    }

    @Override // com.android.ims.internal.ICall
    public void close() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.close();
                this.mSession = null;
            } else {
                logi("close :: Cannot close Null call session!");
            }
            this.mCallProfile = null;
            this.mProposedCallProfile = null;
            this.mLastReasonInfo = null;
            this.mMediaSession = null;
        }
    }

    @Override // com.android.ims.internal.ICall
    public boolean checkIfRemoteUserIsSame(String userId) {
        if (userId == null) {
            return DBG;
        }
        return userId.equals(this.mCallProfile.getCallExtra("remote_uri", ""));
    }

    @Override // com.android.ims.internal.ICall
    public boolean equalsTo(ICall call) {
        if (call != null && (call instanceof ImsCall)) {
            return equals(call);
        }
        return DBG;
    }

    public static boolean isSessionAlive(ImsCallSession session) {
        if (session == null || !session.isAlive()) {
            return DBG;
        }
        return true;
    }

    public ImsCallProfile getCallProfile() {
        ImsCallProfile imsCallProfile;
        synchronized (this.mLockObj) {
            imsCallProfile = this.mCallProfile;
        }
        return imsCallProfile;
    }

    @VisibleForTesting
    public void setCallProfile(ImsCallProfile profile) {
        synchronized (this.mLockObj) {
            this.mCallProfile = profile;
            trackVideoStateHistory(this.mCallProfile);
        }
    }

    public ImsCallProfile getLocalCallProfile() throws ImsException {
        ImsCallProfile localCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    localCallProfile = this.mSession.getLocalCallProfile();
                } catch (Throwable t) {
                    loge("getLocalCallProfile :: ", t);
                    throw new ImsException("getLocalCallProfile()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
            }
        }
        return localCallProfile;
    }

    public ImsCallProfile getRemoteCallProfile() throws ImsException {
        ImsCallProfile remoteCallProfile;
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    remoteCallProfile = this.mSession.getRemoteCallProfile();
                } catch (Throwable t) {
                    loge("getRemoteCallProfile :: ", t);
                    throw new ImsException("getRemoteCallProfile()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
            }
        }
        return remoteCallProfile;
    }

    public ImsCallProfile getProposedCallProfile() {
        synchronized (this.mLockObj) {
            if (!isInCall()) {
                return null;
            }
            return this.mProposedCallProfile;
        }
    }

    public List<ConferenceParticipant> getConferenceParticipants() {
        synchronized (this.mLockObj) {
            logi("getConferenceParticipants :: mConferenceParticipants" + this.mConferenceParticipants);
            if (this.mConferenceParticipants == null) {
                return null;
            }
            if (this.mConferenceParticipants.isEmpty()) {
                return new ArrayList(0);
            }
            return new ArrayList(this.mConferenceParticipants);
        }
    }

    public int getState() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return 0;
            }
            return this.mSession.getState();
        }
    }

    public ImsCallSession getCallSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    public ImsStreamMediaSession getMediaSession() {
        ImsStreamMediaSession imsStreamMediaSession;
        synchronized (this.mLockObj) {
            imsStreamMediaSession = this.mMediaSession;
        }
        return imsStreamMediaSession;
    }

    public String getCallExtra(String name) throws ImsException {
        String property;
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    property = this.mSession.getProperty(name);
                } catch (Throwable t) {
                    loge("getCallExtra :: ", t);
                    throw new ImsException("getCallExtra()", t, 0);
                }
            } else {
                throw new ImsException("No call session", 148);
            }
        }
        return property;
    }

    public ImsReasonInfo getLastReasonInfo() {
        ImsReasonInfo imsReasonInfo;
        synchronized (this.mLockObj) {
            imsReasonInfo = this.mLastReasonInfo;
        }
        return imsReasonInfo;
    }

    public boolean hasPendingUpdate() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mUpdateRequest != 0 ? true : DBG;
        }
        return z;
    }

    public boolean isPendingHold() {
        boolean z;
        synchronized (this.mLockObj) {
            z = true;
            if (this.mUpdateRequest != 1) {
                z = DBG;
            }
        }
        return z;
    }

    public boolean isInCall() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mInCall;
        }
        return z;
    }

    public boolean isMuted() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMute;
        }
        return z;
    }

    public boolean isOnHold() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mHold;
        }
        return z;
    }

    public boolean isMultiparty() {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                return DBG;
            }
            return this.mSession.isMultiparty();
        }
    }

    public boolean isConferenceHost() {
        boolean z;
        synchronized (this.mLockObj) {
            z = (!isMultiparty() || !this.mIsConferenceHost) ? DBG : true;
        }
        return z;
    }

    public void setIsMerged(boolean isMerged) {
        this.mIsMerged = isMerged;
    }

    public boolean isMerged() {
        return this.mIsMerged;
    }

    public void setListener(Listener listener) {
        setListener(listener, DBG);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r7.onCallError(r6, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001d, code lost:
        if (r1 == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        if (r2 == false) goto L_0x0025;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        r7.onCallHeld(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0025, code lost:
        r7.onCallStarted(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002a, code lost:
        if (r3 == com.android.ims.ImsCall.UPDATE_RESUME) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002e, code lost:
        if (r3 == 8) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0031, code lost:
        r7.onCallTerminated(r6, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0035, code lost:
        r7.onCallProgressing(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003a, code lost:
        loge("setListener() :: ", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0015, code lost:
        if (r4 == null) goto L_0x001d;
     */
    public void setListener(Listener listener, boolean callbackImmediately) {
        synchronized (this.mLockObj) {
            this.mListener = listener;
            if (listener != null) {
                if (callbackImmediately) {
                    boolean inCall = this.mInCall;
                    boolean onHold = this.mHold;
                    int state = getState();
                    ImsReasonInfo lastReasonInfo = this.mLastReasonInfo;
                }
            }
        }
    }

    public void setMute(boolean muted) throws ImsException {
        synchronized (this.mLockObj) {
            if (this.mMute != muted) {
                StringBuilder sb = new StringBuilder();
                sb.append("setMute :: turning mute ");
                sb.append(muted ? "on" : "off");
                logi(sb.toString());
                this.mMute = muted;
                try {
                    this.mSession.setMute(muted);
                } catch (Throwable t) {
                    loge("setMute :: ", t);
                    throwImsException(t, 0);
                }
            }
        }
    }

    public void attachSession(ImsCallSession session) throws ImsException {
        logi("attachSession :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                this.mSession.setListener(createCallSessionListener());
            } catch (Throwable t) {
                loge("attachSession :: ", t);
                throwImsException(t, 0);
            }
        }
    }

    public void start(ImsCallSession session, String callee) throws ImsException {
        logi("start(1) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                session.setListener(createCallSessionListener());
                session.start(callee, this.mCallProfile);
            } catch (Throwable t) {
                loge("start(1) :: ", t);
                throw new ImsException("start(1)", t, 0);
            }
        }
    }

    public void start(ImsCallSession session, String[] participants) throws ImsException {
        logi("start(n) :: session=" + session);
        synchronized (this.mLockObj) {
            this.mSession = session;
            try {
                session.setListener(createCallSessionListener());
                session.start(participants, this.mCallProfile);
            } catch (Throwable t) {
                loge("start(n) :: ", t);
                throw new ImsException("start(n)", t, 0);
            }
        }
    }

    public void accept(int callType) throws ImsException {
        accept(callType, new ImsStreamMediaProfile());
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws ImsException {
        logi("accept :: callType=" + callType + ", profile=" + profile);
        if (this.mAnswerWithRtt) {
            profile.mRttMode = 1;
            logi("accept :: changing media profile RTT mode to full");
        }
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    this.mSession.accept(callType, profile);
                    if (this.mInCall && this.mProposedCallProfile != null) {
                        if (DBG) {
                            logi("accept :: call profile will be updated");
                        }
                        this.mCallProfile = this.mProposedCallProfile;
                        trackVideoStateHistory(this.mCallProfile);
                        this.mProposedCallProfile = null;
                    }
                    if (this.mInCall && this.mUpdateRequest == UPDATE_UNSPECIFIED) {
                        this.mUpdateRequest = 0;
                    }
                } catch (Throwable t) {
                    loge("accept :: ", t);
                    throw new ImsException("accept()", t, 0);
                }
            } else {
                throw new ImsException("No call to answer", 148);
            }
        }
    }

    public void deflect(String number) throws ImsException {
        logi("deflect :: session=" + this.mSession + ", number=" + Rlog.pii(TAG, number));
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                try {
                    this.mSession.deflect(number);
                } catch (Throwable t) {
                    loge("deflect :: ", t);
                    throw new ImsException("deflect()", t, 0);
                }
            } else {
                throw new ImsException("No call to deflect", 148);
            }
        }
    }

    public void reject(int reason) throws ImsException {
        logi("reject :: reason=" + reason);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.reject(reason);
            }
            if (this.mInCall && this.mProposedCallProfile != null) {
                if (DBG) {
                    logi("reject :: call profile is not updated; destroy it...");
                }
                this.mProposedCallProfile = null;
            }
            if (this.mInCall && this.mUpdateRequest == UPDATE_UNSPECIFIED) {
                this.mUpdateRequest = 0;
            }
        }
    }

    public void terminate(int reason, int overrideReason) {
        logi("terminate :: reason=" + reason + " ; overrideReason=" + overrideReason);
        this.mOverrideReason = overrideReason;
        terminate(reason);
    }

    public void terminate(int reason) {
        logi("terminate :: reason=" + reason);
        synchronized (this.mLockObj) {
            this.mHold = DBG;
            this.mInCall = DBG;
            this.mTerminationRequestPending = true;
            if (this.mSession != null) {
                this.mSession.terminate(reason);
            }
        }
    }

    public void hold() throws ImsException {
        logi("hold :: ");
        if (!isOnHold()) {
            synchronized (this.mLockObj) {
                if (this.mUpdateRequest != 0) {
                    loge("hold :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                    throw new ImsException("Call update is in progress", 102);
                } else if (this.mSession != null) {
                    this.mSession.hold(createHoldMediaProfile());
                    this.mHold = true;
                    this.mUpdateRequest = 1;
                } else {
                    throw new ImsException("No call session", 148);
                }
            }
        } else if (DBG) {
            logi("hold :: call is already on hold");
        }
    }

    public void resume() throws ImsException {
        logi("resume :: ");
        if (isOnHold()) {
            synchronized (this.mLockObj) {
                if (this.mUpdateRequest != 0) {
                    loge("resume :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                    throw new ImsException("Call update is in progress", 102);
                } else if (this.mSession != null) {
                    this.mUpdateRequest = UPDATE_RESUME;
                    this.mSession.resume(createResumeMediaProfile());
                } else {
                    loge("resume :: ");
                    throw new ImsException("No call session", 148);
                }
            }
        } else if (DBG) {
            logi("resume :: call is not being held");
        }
    }

    private boolean isUpdatePending(ImsCall imsCall) {
        if (imsCall == null || imsCall.mUpdateRequest == 0) {
            return DBG;
        }
        loge("merge :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
        return true;
    }

    private void merge() throws ImsException {
        logi("merge :: ");
        synchronized (this.mLockObj) {
            if (isUpdatePending(this)) {
                setCallSessionMergePending(DBG);
                if (this.mMergePeer != null) {
                    this.mMergePeer.setCallSessionMergePending(DBG);
                }
                if (this.mMergeHost != null) {
                    this.mMergeHost.setCallSessionMergePending(DBG);
                }
                throw new ImsException("Call update is in progress", 102);
            }
            if (!isUpdatePending(this.mMergePeer)) {
                if (!isUpdatePending(this.mMergeHost)) {
                    if (this.mSession != null) {
                        if (!this.mHold) {
                            if (!this.mContext.getResources().getBoolean(17891615)) {
                                this.mSession.hold(createHoldMediaProfile());
                                this.mHold = true;
                                this.mUpdateRequest = 2;
                            }
                        }
                        if (this.mMergePeer != null && !this.mMergePeer.isMultiparty() && !isMultiparty()) {
                            this.mUpdateRequest = UPDATE_MERGE;
                            this.mMergePeer.mUpdateRequest = UPDATE_MERGE;
                        } else if (this.mMergeHost == null || this.mMergeHost.isMultiparty() || isMultiparty()) {
                            setPendingUpdateMerge();
                        } else {
                            this.mUpdateRequest = UPDATE_MERGE;
                            this.mMergeHost.mUpdateRequest = UPDATE_MERGE;
                        }
                        this.mSession.merge();
                    } else {
                        loge("merge :: no call session");
                        throw new ImsException("No call session", 148);
                    }
                }
            }
            setCallSessionMergePending(DBG);
            if (this.mMergePeer != null) {
                this.mMergePeer.setCallSessionMergePending(DBG);
            }
            if (this.mMergeHost != null) {
                this.mMergeHost.setCallSessionMergePending(DBG);
            }
            throw new ImsException("Peer or host call update is in progress", 102);
        }
    }

    public void merge(ImsCall bgCall) throws ImsException {
        logi("merge(1) :: bgImsCall=" + bgCall);
        if (bgCall != null) {
            synchronized (this.mLockObj) {
                setCallSessionMergePending(true);
                bgCall.setCallSessionMergePending(true);
                if ((isMultiparty() || bgCall.isMultiparty()) && !isMultiparty()) {
                    setMergeHost(bgCall);
                } else {
                    setMergePeer(bgCall);
                }
            }
            if (isMultiparty()) {
                this.mMergeRequestedByConference = true;
            } else {
                logi("merge : mMergeRequestedByConference not set");
            }
            merge();
            return;
        }
        throw new ImsException("No background call", (int) ImsManager.INCOMING_CALL_RESULT_CODE);
    }

    public void update(int callType, ImsStreamMediaProfile mediaProfile) throws ImsException {
        logi("update :: callType=" + callType + ", mediaProfile=" + mediaProfile);
        if (isOnHold()) {
            if (DBG) {
                logi("update :: call is on hold");
            }
            throw new ImsException("Not in a call to update call", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                if (DBG) {
                    logi("update :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                }
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession != null) {
                this.mSession.update(callType, mediaProfile);
                this.mUpdateRequest = UPDATE_UNSPECIFIED;
            } else {
                loge("update :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void extendToConference(String[] participants) throws ImsException {
        logi("extendToConference ::");
        if (isOnHold()) {
            if (DBG) {
                logi("extendToConference :: call is on hold");
            }
            throw new ImsException("Not in a call to extend a call to conference", 102);
        }
        synchronized (this.mLockObj) {
            if (this.mUpdateRequest != 0) {
                logi("extendToConference :: update is in progress; request=" + updateRequestToString(this.mUpdateRequest));
                throw new ImsException("Call update is in progress", 102);
            } else if (this.mSession != null) {
                this.mSession.extendToConference(participants);
                this.mUpdateRequest = UPDATE_EXTEND_TO_CONFERENCE;
            } else {
                loge("extendToConference :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void inviteParticipants(String[] participants) throws ImsException {
        logi("inviteParticipants ::");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.inviteParticipants(participants);
            } else {
                loge("inviteParticipants :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void removeParticipants(String[] participants) throws ImsException {
        logi("removeParticipants :: session=" + this.mSession);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.removeParticipants(participants);
            } else {
                loge("removeParticipants :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void sendDtmf(char c, Message result) {
        logi("sendDtmf :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.sendDtmf(c, result);
            }
        }
    }

    public void startDtmf(char c) {
        logi("startDtmf :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.startDtmf(c);
            }
        }
    }

    public void stopDtmf() {
        logi("stopDtmf :: ");
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.stopDtmf();
            }
        }
    }

    public void sendUssd(String ussdMessage) throws ImsException {
        logi("sendUssd :: ussdMessage=" + ussdMessage);
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                this.mSession.sendUssd(ussdMessage);
            } else {
                loge("sendUssd :: ");
                throw new ImsException("No call session", 148);
            }
        }
    }

    public void sendRttMessage(String rttMessage) {
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttMessage::no session");
            }
            if (!this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttMessage::Not an rtt call, ignoring");
            } else {
                this.mSession.sendRttMessage(rttMessage);
            }
        }
    }

    public void sendRttModifyRequest(boolean rttOn) {
        logi("sendRttModifyRequest");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttModifyRequest::no session");
            }
            if (rttOn && this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttModifyRequest::Already RTT call, ignoring request to turn on.");
            } else if (rttOn || this.mCallProfile.mMediaProfile.isRttCall()) {
                Parcel p = Parcel.obtain();
                int i = 0;
                this.mCallProfile.writeToParcel(p, 0);
                p.setDataPosition(0);
                ImsCallProfile requestedProfile = new ImsCallProfile();
                ImsStreamMediaProfile imsStreamMediaProfile = requestedProfile.mMediaProfile;
                if (rttOn) {
                    i = 1;
                }
                imsStreamMediaProfile.setRttMode(i);
                this.mSession.sendRttModifyRequest(requestedProfile);
            } else {
                logi("sendRttModifyRequest::Not RTT call, ignoring request to turn off.");
            }
        }
    }

    public void sendRttModifyResponse(boolean status) {
        logi("sendRttModifyResponse");
        synchronized (this.mLockObj) {
            if (this.mSession == null) {
                loge("sendRttModifyResponse::no session");
            }
            if (this.mCallProfile.mMediaProfile.isRttCall()) {
                logi("sendRttModifyResponse::Already RTT call, ignoring.");
            } else {
                this.mSession.sendRttModifyResponse(status);
            }
        }
    }

    public void setAnswerWithRtt() {
        this.mAnswerWithRtt = true;
    }

    private void clear(ImsReasonInfo lastReasonInfo) {
        this.mInCall = DBG;
        this.mHold = DBG;
        this.mUpdateRequest = 0;
        this.mLastReasonInfo = lastReasonInfo;
    }

    /* access modifiers changed from: protected */
    public ImsCallSession.Listener createCallSessionListener() {
        this.mImsCallSessionListenerProxy = new ImsCallSessionListenerProxy();
        return this.mImsCallSessionListenerProxy;
    }

    @VisibleForTesting
    public ImsCallSessionListenerProxy getImsCallSessionListenerProxy() {
        return this.mImsCallSessionListenerProxy;
    }

    /* access modifiers changed from: protected */
    public ImsCall createNewCall(ImsCallSession session, ImsCallProfile profile) {
        ImsCall call = new ImsCall(this.mContext, profile);
        try {
            call.attachSession(session);
            return call;
        } catch (ImsException e) {
            call.close();
            return null;
        }
    }

    private ImsStreamMediaProfile createHoldMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        ImsCallProfile imsCallProfile = this.mCallProfile;
        if (imsCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = imsCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = 2;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = 2;
        }
        return mediaProfile;
    }

    private ImsStreamMediaProfile createResumeMediaProfile() {
        ImsStreamMediaProfile mediaProfile = new ImsStreamMediaProfile();
        ImsCallProfile imsCallProfile = this.mCallProfile;
        if (imsCallProfile == null) {
            return mediaProfile;
        }
        mediaProfile.mAudioQuality = imsCallProfile.mMediaProfile.mAudioQuality;
        mediaProfile.mVideoQuality = this.mCallProfile.mMediaProfile.mVideoQuality;
        mediaProfile.mAudioDirection = UPDATE_RESUME;
        if (mediaProfile.mVideoQuality != 0) {
            mediaProfile.mVideoDirection = UPDATE_RESUME;
        }
        return mediaProfile;
    }

    private void enforceConversationMode() {
        if (this.mInCall) {
            this.mHold = DBG;
            this.mUpdateRequest = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void mergeInternal() {
        logi("mergeInternal :: ");
        this.mSession.merge();
        this.mUpdateRequest = UPDATE_MERGE;
    }

    private void notifyConferenceSessionTerminated(ImsReasonInfo reasonInfo) {
        Listener listener = this.mListener;
        clear(reasonInfo);
        if (listener != null) {
            try {
                listener.onCallTerminated(this, reasonInfo);
            } catch (Throwable t) {
                loge("notifyConferenceSessionTerminated :: ", t);
            }
        }
    }

    private void notifyConferenceStateUpdated(ImsConferenceState state) {
        Set<Map.Entry<String, Bundle>> participants;
        Listener listener;
        String endpoint;
        if (state != null && state.mParticipants != null && (participants = state.mParticipants.entrySet()) != null) {
            this.mConferenceParticipants = new CopyOnWriteArrayList<>();
            for (Map.Entry<String, Bundle> entry : participants) {
                Bundle confInfo = entry.getValue();
                String status = confInfo.getString("status");
                String user = confInfo.getString("user");
                String displayName = confInfo.getString("display-text");
                String endpoint2 = confInfo.getString("endpoint");
                logi("notifyConferenceStateUpdated :: key=" + Rlog.pii(TAG, entry.getKey()) + ", status=" + status + ", user=" + Rlog.pii(TAG, user) + ", displayName= " + Rlog.pii(TAG, displayName) + ", endpoint=" + endpoint2);
                Uri handle = Uri.parse(user);
                if (endpoint2 == null) {
                    endpoint = "";
                } else {
                    endpoint = endpoint2;
                }
                Uri endpointUri = Uri.parse(endpoint);
                int connectionState = ImsConferenceState.getConnectionStateForStatus(status);
                if (connectionState != UPDATE_UNSPECIFIED) {
                    this.mConferenceParticipants.add(new ConferenceParticipant(handle, displayName, endpointUri, connectionState, -1));
                }
            }
            CopyOnWriteArrayList<ConferenceParticipant> copyOnWriteArrayList = this.mConferenceParticipants;
            if (copyOnWriteArrayList != null && (listener = this.mListener) != null) {
                try {
                    listener.onConferenceParticipantsStateChanged(this, copyOnWriteArrayList);
                } catch (Throwable t) {
                    loge("notifyConferenceStateUpdated :: ", t);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        if (r1 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r1.onCallTerminated(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004f, code lost:
        loge("processCallTerminated :: ", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    private void processCallTerminated(ImsReasonInfo reasonInfo) {
        logi("processCallTerminated :: reason=" + reasonInfo + " userInitiated = " + this.mTerminationRequestPending);
        synchronized (this) {
            if (isCallSessionMergePending() && !this.mTerminationRequestPending) {
                logi("processCallTerminated :: burying termination during ongoing merge.");
                this.mSessionEndDuringMerge = true;
                this.mSessionEndDuringMergeReasonInfo = reasonInfo;
            } else if (isMultiparty()) {
                notifyConferenceSessionTerminated(reasonInfo);
            } else {
                Listener listener = this.mListener;
                clear(reasonInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTransientConferenceSession(ImsCallSession session) {
        if (session == null || session == this.mSession || session != this.mTransientConferenceSession) {
            return DBG;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setTransientSessionAsPrimary(ImsCallSession transientSession) {
        synchronized (this) {
            this.mSession.setListener((ImsCallSession.Listener) null);
            this.mSession.close();
            this.mSession = transientSession;
            this.mSession.setListener(createCallSessionListener());
        }
    }

    private void markCallAsMerged(boolean playDisconnectTone) {
        String reasonInfo;
        int reasonCode;
        if (!isSessionAlive(this.mSession)) {
            logi("markCallAsMerged");
            setIsMerged(!playDisconnectTone);
            this.mSessionEndDuringMerge = true;
            if (playDisconnectTone) {
                reasonCode = 510;
                reasonInfo = "Call ended by network";
            } else {
                reasonCode = 108;
                reasonInfo = "Call ended during conference merge process.";
            }
            this.mSessionEndDuringMergeReasonInfo = new ImsReasonInfo(reasonCode, 0, reasonInfo);
        }
    }

    public boolean isMergeRequestedByConf() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mMergeRequestedByConference;
        }
        return z;
    }

    public void resetIsMergeRequestedByConf(boolean value) {
        synchronized (this.mLockObj) {
            this.mMergeRequestedByConference = value;
        }
    }

    public ImsCallSession getSession() {
        ImsCallSession imsCallSession;
        synchronized (this.mLockObj) {
            imsCallSession = this.mSession;
        }
        return imsCallSession;
    }

    /* JADX INFO: Multiple debug info for r4v0 com.android.ims.ImsCall$Listener: [D('finalHostCall' com.android.ims.ImsCall), D('listener' com.android.ims.ImsCall$Listener)] */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00fb, code lost:
        if (r4 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r4.onCallMerged(r1, r3, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0101, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0102, code lost:
        loge("processMergeComplete :: ", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        return;
     */
    public void processMergeComplete() {
        ImsCall finalPeerCall;
        ImsCall finalHostCall;
        Listener listener;
        ImsCall finalHostCall2;
        logi("processMergeComplete :: ");
        if (!isMergeHost()) {
            loge("processMergeComplete :: We are not the merge host!");
            return;
        }
        boolean swapRequired = DBG;
        synchronized (this) {
            if (isMultiparty()) {
                setIsMerged(DBG);
                if (!this.mMergeRequestedByConference) {
                    this.mHold = DBG;
                    swapRequired = true;
                }
                this.mMergePeer.markCallAsMerged(DBG);
                finalHostCall = this;
                finalPeerCall = this.mMergePeer;
            } else if (this.mTransientConferenceSession == null) {
                loge("processMergeComplete :: No transient session!");
                return;
            } else if (this.mMergePeer == null) {
                loge("processMergeComplete :: No merge peer!");
                return;
            } else {
                ImsCallSession transientConferenceSession = this.mTransientConferenceSession;
                this.mTransientConferenceSession = null;
                transientConferenceSession.setListener((ImsCallSession.Listener) null);
                if (isSessionAlive(this.mSession) && !isSessionAlive(this.mMergePeer.getCallSession())) {
                    this.mMergePeer.mHold = DBG;
                    this.mHold = true;
                    if (this.mConferenceParticipants != null && !this.mConferenceParticipants.isEmpty()) {
                        this.mMergePeer.mConferenceParticipants = this.mConferenceParticipants;
                    }
                    finalHostCall2 = this.mMergePeer;
                    swapRequired = true;
                    setIsMerged(DBG);
                    this.mMergePeer.setIsMerged(DBG);
                    logi("processMergeComplete :: transient will transfer to merge peer");
                    finalPeerCall = this;
                } else if (isSessionAlive(this.mSession) || !isSessionAlive(this.mMergePeer.getCallSession())) {
                    finalHostCall2 = this;
                    ImsCall finalPeerCall2 = this.mMergePeer;
                    this.mMergePeer.markCallAsMerged(DBG);
                    swapRequired = DBG;
                    setIsMerged(DBG);
                    this.mMergePeer.setIsMerged(true);
                    logi("processMergeComplete :: transient will stay with us (I'm the host).");
                    finalPeerCall = finalPeerCall2;
                } else {
                    finalHostCall2 = this;
                    ImsCall finalPeerCall3 = this.mMergePeer;
                    swapRequired = DBG;
                    setIsMerged(DBG);
                    this.mMergePeer.setIsMerged(DBG);
                    logi("processMergeComplete :: transient will stay with the merge host");
                    finalPeerCall = finalPeerCall3;
                }
                logi("processMergeComplete :: call=" + finalHostCall2 + " is the final host");
                finalHostCall2.setTransientSessionAsPrimary(transientConferenceSession);
                finalHostCall = finalHostCall2;
            }
            listener = finalHostCall.mListener;
            updateCallProfile(finalPeerCall);
            updateCallProfile(finalHostCall);
            clearMergeInfo();
            finalPeerCall.notifySessionTerminatedDuringMerge();
            finalHostCall.clearSessionTerminationFlags();
            finalHostCall.mIsConferenceHost = true;
        }
        CopyOnWriteArrayList<ConferenceParticipant> copyOnWriteArrayList = this.mConferenceParticipants;
        if (copyOnWriteArrayList != null && !copyOnWriteArrayList.isEmpty()) {
            try {
                listener.onConferenceParticipantsStateChanged(finalHostCall, this.mConferenceParticipants);
            } catch (Throwable t) {
                loge("processMergeComplete :: ", t);
            }
        }
    }

    private static void updateCallProfile(ImsCall call) {
        if (call != null) {
            call.updateCallProfile();
        }
    }

    private void updateCallProfile() {
        synchronized (this.mLockObj) {
            if (this.mSession != null) {
                setCallProfile(this.mSession.getCallProfile());
            }
        }
    }

    private void notifySessionTerminatedDuringMerge() {
        Listener listener;
        boolean notifyFailure = DBG;
        ImsReasonInfo notifyFailureReasonInfo = null;
        synchronized (this) {
            listener = this.mListener;
            if (this.mSessionEndDuringMerge) {
                logi("notifySessionTerminatedDuringMerge ::reporting terminate during merge");
                notifyFailure = true;
                notifyFailureReasonInfo = this.mSessionEndDuringMergeReasonInfo;
            }
            clearSessionTerminationFlags();
        }
        if (listener != null && notifyFailure) {
            try {
                processCallTerminated(notifyFailureReasonInfo);
            } catch (Throwable t) {
                loge("notifySessionTerminatedDuringMerge :: ", t);
            }
        }
    }

    private void clearSessionTerminationFlags() {
        this.mSessionEndDuringMerge = DBG;
        this.mSessionEndDuringMergeReasonInfo = null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        if (r0 == null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r0.onCallMergeFailed(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005e, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005f, code lost:
        loge("processMergeFailed :: ", r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    public void processMergeFailed(ImsReasonInfo reasonInfo) {
        logi("processMergeFailed :: reason=" + reasonInfo);
        synchronized (this) {
            if (!isMergeHost()) {
                loge("processMergeFailed :: We are not the merge host!");
                return;
            }
            if (this.mTransientConferenceSession != null) {
                this.mTransientConferenceSession.setListener((ImsCallSession.Listener) null);
                this.mTransientConferenceSession = null;
            }
            Listener listener = this.mListener;
            markCallAsMerged(true);
            setCallSessionMergePending(DBG);
            notifySessionTerminatedDuringMerge();
            if (this.mMergePeer != null) {
                this.mMergePeer.markCallAsMerged(true);
                this.mMergePeer.setCallSessionMergePending(DBG);
                this.mMergePeer.notifySessionTerminatedDuringMerge();
            } else {
                loge("processMergeFailed :: No merge peer!");
            }
            clearMergeInfo();
        }
    }

    @VisibleForTesting
    public class ImsCallSessionListenerProxy extends ImsCallSession.Listener {
        public ImsCallSessionListenerProxy() {
        }

        public void callSessionProgressing(ImsCallSession session, ImsStreamMediaProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionProgressing :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionProgressing :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.copyCallProfileIfNecessary(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallProgressing(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionProgressing :: ", t);
                }
            }
        }

        public void callSessionStarted(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionStarted :: session=" + session + " profile=" + profile);
            if (!ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
                if (!ImsCall.this.isTransientConferenceSession(session)) {
                    synchronized (ImsCall.this) {
                        listener = ImsCall.this.mListener;
                        ImsCall.this.setCallProfile(profile);
                    }
                    if (listener != null) {
                        try {
                            listener.onCallStarted(ImsCall.this);
                        } catch (Throwable t) {
                            ImsCall.this.loge("callSessionStarted :: ", t);
                        }
                    }
                }
            } else {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionStarted :: on transient session=" + session);
            }
        }

        public void callSessionStartFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionStartFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionStartFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mLastReasonInfo = reasonInfo;
            }
            if (listener != null) {
                try {
                    listener.onCallStartFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionStarted :: ", t);
                }
            }
        }

        public void callSessionTerminated(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionTerminated :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionTerminated :: on transient session=" + session);
                ImsCall.this.processMergeFailed(reasonInfo);
                return;
            }
            ImsCall.this.checkIfConferenceMerge(reasonInfo);
            if (ImsCall.this.mOverrideReason != 0) {
                ImsCall imsCall3 = ImsCall.this;
                imsCall3.logi("callSessionTerminated :: overrideReasonInfo=" + ImsCall.this.mOverrideReason);
                reasonInfo = new ImsReasonInfo(ImsCall.this.mOverrideReason, reasonInfo.getExtraCode(), reasonInfo.getExtraMessage());
            }
            ImsCall.this.processCallTerminated(reasonInfo);
            ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0051, code lost:
            if (r1 == null) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
            r1.onCallHeld(r4.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0059, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
            r4.this$0.loge("callSessionHeld :: ", r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
            return;
         */
        public void callSessionHeld(ImsCallSession session, ImsCallProfile profile) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHeld :: session=" + session + "profile=" + profile);
            synchronized (ImsCall.this) {
                if (!ImsCall.this.shouldSkipResetMergePending()) {
                    ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
                }
                ImsCall.this.setCallProfile(profile);
                if (ImsCall.this.mUpdateRequest == 2) {
                    ImsCall.this.mergeInternal();
                    return;
                }
                ImsCall.this.mUpdateRequest = 0;
                Listener listener = ImsCall.this.mListener;
                ImsCall.this.updateHoldStateIfNecessary(true);
            }
        }

        public void callSessionHoldFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionHoldFailed :: session" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionHoldFailed :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall imsCall3 = ImsCall.this;
            imsCall3.logi("callSessionHoldFailed :: session=" + session + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = ImsCall.DBG;
            }
            synchronized (ImsCall.this) {
                if (ImsCall.this.mUpdateRequest == 2) {
                }
                ImsCall.this.mUpdateRequest = 0;
                listener = ImsCall.this.mListener;
                ImsCall.this.updateHoldStateIfNecessary(ImsCall.DBG);
            }
            if (listener != null) {
                try {
                    listener.onCallHoldFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldFailed :: ", t);
                }
            }
        }

        public void callSessionHoldReceived(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHoldReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionHoldReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallHoldReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHoldReceived :: ", t);
                }
            }
        }

        public void callSessionResumed(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionResumed :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumed :: not supported for transient conference session=" + session);
                return;
            }
            if (!ImsCall.this.shouldSkipResetMergePending()) {
                ImsCall.this.setCallSessionMergePending(ImsCall.DBG);
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
                ImsCall.this.mUpdateRequest = 0;
                ImsCall.this.mHold = ImsCall.DBG;
            }
            if (listener != null) {
                try {
                    listener.onCallResumed(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumed :: ", t);
                }
            }
        }

        public void callSessionResumeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionResumeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumeFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this.mLockObj) {
                ImsCall.this.mHold = true;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallResumeFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeFailed :: ", t);
                }
            }
        }

        public void callSessionResumeReceived(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionResumeReceived :: session=" + session + "profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionResumeReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallResumeReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionResumeReceived :: ", t);
                }
            }
        }

        public void callSessionMergeStarted(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionMergeStarted :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
        }

        /* access modifiers changed from: protected */
        public boolean doesCallSessionExistsInMerge(ImsCallSession cs) {
            String callId = cs.getCallId();
            if ((!ImsCall.this.isMergeHost() || !Objects.equals(ImsCall.this.mMergePeer.mSession.getCallId(), callId)) && ((!ImsCall.this.isMergePeer() || !Objects.equals(ImsCall.this.mMergeHost.mSession.getCallId(), callId)) && !Objects.equals(ImsCall.this.mSession.getCallId(), callId))) {
                return ImsCall.DBG;
            }
            return true;
        }

        public void callSessionMergeComplete(ImsCallSession newSession) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionMergeComplete :: newSession =" + newSession);
            if (!ImsCall.this.isMergeHost()) {
                ImsCall.this.mMergeHost.processMergeComplete();
                return;
            }
            if (newSession != null) {
                ImsCall.this.mTransientConferenceSession = doesCallSessionExistsInMerge(newSession) ? null : newSession;
            }
            ImsCall.this.processMergeComplete();
        }

        public void callSessionMergeFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionMergeFailed :: session=" + session + "reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                if (ImsCall.this.isMergeHost()) {
                    ImsCall.this.processMergeFailed(reasonInfo);
                } else if (ImsCall.this.mMergeHost != null) {
                    ImsCall.this.mMergeHost.processMergeFailed(reasonInfo);
                } else {
                    ImsCall.this.loge("callSessionMergeFailed :: No merge host for this conference!");
                }
            }
        }

        public void callSessionUpdated(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUpdated :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdated :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.setCallProfile(profile);
            }
            if (listener != null) {
                try {
                    listener.onCallUpdated(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdated :: ", t);
                }
            }
        }

        public void callSessionUpdateFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionUpdateFailed :: session=" + session + " reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdateFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateFailed :: ", t);
                }
            }
        }

        public void callSessionUpdateReceived(ImsCallSession session, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUpdateReceived :: session=" + session + " profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUpdateReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mProposedCallProfile = profile;
                ImsCall.this.mUpdateRequest = ImsCall.UPDATE_UNSPECIFIED;
            }
            if (listener != null) {
                try {
                    listener.onCallUpdateReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUpdateReceived :: ", t);
                }
            }
        }

        public void callSessionConferenceExtended(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceExtended :: session=" + session + " newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtended :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall == null) {
                callSessionConferenceExtendFailed(session, new ImsReasonInfo());
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtended(ImsCall.this, newCall);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtended :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionConferenceExtendFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtendFailed :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
                ImsCall.this.mUpdateRequest = 0;
            }
            if (listener != null) {
                try {
                    listener.onCallConferenceExtendFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionConferenceExtendFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceExtendReceived(ImsCallSession session, ImsCallSession newSession, ImsCallProfile profile) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceExtendReceived :: newSession=" + newSession + ", profile=" + profile);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionConferenceExtendReceived :: not supported for transient conference session" + session);
                return;
            }
            ImsCall newCall = ImsCall.this.createNewCall(newSession, profile);
            if (newCall != null) {
                synchronized (ImsCall.this) {
                    listener = ImsCall.this.mListener;
                }
                if (listener != null) {
                    try {
                        listener.onCallConferenceExtendReceived(ImsCall.this, newCall);
                    } catch (Throwable t) {
                        ImsCall.this.loge("callSessionConferenceExtendReceived :: ", t);
                    }
                }
            }
        }

        public void callSessionInviteParticipantsRequestDelivered(ImsCallSession session) {
            Listener listener;
            ImsCall.this.logi("callSessionInviteParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionInviteParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                ImsCall.this.resetConferenceMergingFlag();
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionInviteParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionInviteParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionInviteParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                ImsCall.this.resetConferenceMergingFlag();
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallInviteParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionInviteParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestDelivered(ImsCallSession session) {
            Listener listener;
            ImsCall.this.logi("callSessionRemoveParticipantsRequestDelivered ::");
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionRemoveParticipantsRequestDelivered :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestDelivered(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestDelivered :: ", t);
                }
            }
        }

        public void callSessionRemoveParticipantsRequestFailed(ImsCallSession session, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionRemoveParticipantsRequestFailed :: reasonInfo=" + reasonInfo);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionRemoveParticipantsRequestFailed :: not supported for conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallRemoveParticipantsRequestFailed(ImsCall.this, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRemoveParticipantsRequestFailed :: ", t);
                }
            }
        }

        public void callSessionConferenceStateUpdated(ImsCallSession session, ImsConferenceState state) {
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionConferenceStateUpdated :: state=" + state);
            ImsCall.this.conferenceStateUpdated(state);
        }

        public void callSessionUssdMessageReceived(ImsCallSession session, int mode, String ussdMessage) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionUssdMessageReceived :: mode=" + mode + ", ussdMessage=" + ussdMessage);
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall2 = ImsCall.this;
                imsCall2.logi("callSessionUssdMessageReceived :: not supported for transient conference session=" + session);
                return;
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallUssdMessageReceived(ImsCall.this, mode, ussdMessage);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionUssdMessageReceived :: ", t);
                }
            }
        }

        public void callSessionTtyModeReceived(ImsCallSession session, int mode) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionTtyModeReceived :: mode=" + mode);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSessionTtyModeReceived(ImsCall.this, mode);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionTtyModeReceived :: ", t);
                }
            }
        }

        public void callSessionMultipartyStateChanged(ImsCallSession session, boolean isMultiParty) {
            Listener listener;
            String str;
            if (ImsCall.VDBG) {
                ImsCall imsCall = ImsCall.this;
                StringBuilder sb = new StringBuilder();
                sb.append("callSessionMultipartyStateChanged isMultiParty: ");
                if (isMultiParty) {
                    str = "Y";
                } else {
                    str = "N";
                }
                sb.append(str);
                imsCall.logi(sb.toString());
            }
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onMultipartyStateChanged(ImsCall.this, isMultiParty);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionMultipartyStateChanged :: ", t);
                }
            }
        }

        public void callSessionHandover(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionHandover :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandover(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandover :: ", t);
                }
            }
        }

        public void callSessionHandoverFailed(ImsCallSession session, int srcAccessTech, int targetAccessTech, ImsReasonInfo reasonInfo) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.loge("callSessionHandoverFailed :: session=" + session + ", srcAccessTech=" + srcAccessTech + ", targetAccessTech=" + targetAccessTech + ", reasonInfo=" + reasonInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallHandoverFailed(ImsCall.this, srcAccessTech, targetAccessTech, reasonInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionHandoverFailed :: ", t);
                }
            }
        }

        public void callSessionSuppServiceReceived(ImsCallSession session, ImsSuppServiceNotification suppServiceInfo) {
            Listener listener;
            if (ImsCall.this.isTransientConferenceSession(session)) {
                ImsCall imsCall = ImsCall.this;
                imsCall.logi("callSessionSuppServiceReceived :: not supported for transient conference session=" + session);
                return;
            }
            ImsCall imsCall2 = ImsCall.this;
            imsCall2.logi("callSessionSuppServiceReceived :: session=" + session + ", suppServiceInfo" + suppServiceInfo);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallSuppServiceReceived(ImsCall.this, suppServiceInfo);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionSuppServiceReceived :: ", t);
                }
            }
        }

        public void callSessionRttModifyRequestReceived(ImsCallSession session, ImsCallProfile callProfile) {
            Listener listener;
            ImsCall.this.logi("callSessionRttModifyRequestReceived");
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (!callProfile.mMediaProfile.isRttCall()) {
                ImsCall.this.logi("callSessionRttModifyRequestReceived:: ignoring request, requested profile is not RTT.");
            } else if (listener != null) {
                try {
                    listener.onRttModifyRequestReceived(ImsCall.this);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttModifyRequestReceived:: ", t);
                }
            }
        }

        public void callSessionRttModifyResponseReceived(int status) {
            Listener listener;
            ImsCall imsCall = ImsCall.this;
            imsCall.logi("callSessionRttModifyResponseReceived: " + status);
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onRttModifyResponseReceived(ImsCall.this, status);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttModifyResponseReceived:: ", t);
                }
            }
        }

        public void callSessionRttMessageReceived(String rttMessage) {
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onRttMessageReceived(ImsCall.this, rttMessage);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttMessageReceived:: ", t);
                }
            }
        }

        public void callSessionRttAudioIndicatorChanged(ImsStreamMediaProfile profile) {
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onRttAudioIndicatorChanged(ImsCall.this, profile);
                } catch (Throwable t) {
                    ImsCall.this.loge("callSessionRttAudioIndicatorChanged:: ", t);
                }
            }
        }

        public void callQualityChanged(CallQuality callQuality) {
            Listener listener;
            synchronized (ImsCall.this) {
                listener = ImsCall.this.mListener;
            }
            if (listener != null) {
                try {
                    listener.onCallQualityChanged(ImsCall.this, callQuality);
                } catch (Throwable t) {
                    ImsCall.this.loge("callQualityChanged:: ", t);
                }
            }
        }
    }

    @VisibleForTesting
    public void conferenceStateUpdated(ImsConferenceState state) {
        Listener listener;
        synchronized (this) {
            notifyConferenceStateUpdated(state);
            listener = this.mListener;
        }
        if (listener != null) {
            try {
                listener.onCallConferenceStateUpdated(this, state);
            } catch (Throwable t) {
                loge("callSessionConferenceStateUpdated :: ", t);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String updateRequestToString(int updateRequest) {
        switch (updateRequest) {
            case 0:
                return "NONE";
            case 1:
                return "HOLD";
            case 2:
                return "HOLD_MERGE";
            case UPDATE_RESUME /* 3 */:
                return "RESUME";
            case UPDATE_MERGE /* 4 */:
                return "MERGE";
            case UPDATE_EXTEND_TO_CONFERENCE /* 5 */:
                return "EXTEND_TO_CONFERENCE";
            case UPDATE_UNSPECIFIED /* 6 */:
                return "UNSPECIFIED";
            default:
                return "UNKNOWN";
        }
    }

    private void clearMergeInfo() {
        logi("clearMergeInfo :: clearing all merge info");
        ImsCall imsCall = this.mMergeHost;
        if (imsCall != null) {
            imsCall.mMergePeer = null;
            imsCall.mUpdateRequest = 0;
            imsCall.mCallSessionMergePending = DBG;
        }
        ImsCall imsCall2 = this.mMergePeer;
        if (imsCall2 != null) {
            imsCall2.mMergeHost = null;
            imsCall2.mUpdateRequest = 0;
            imsCall2.mCallSessionMergePending = DBG;
        }
        this.mMergeHost = null;
        this.mMergePeer = null;
        this.mUpdateRequest = 0;
        this.mCallSessionMergePending = DBG;
    }

    private void setMergePeer(ImsCall mergePeer) {
        this.mMergePeer = mergePeer;
        this.mMergeHost = null;
        mergePeer.mMergeHost = this;
        mergePeer.mMergePeer = null;
    }

    public void setMergeHost(ImsCall mergeHost) {
        this.mMergeHost = mergeHost;
        this.mMergePeer = null;
        mergeHost.mMergeHost = null;
        mergeHost.mMergePeer = this;
    }

    private boolean isMerging() {
        if (this.mMergePeer == null && this.mMergeHost == null) {
            return DBG;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMergeHost() {
        if (this.mMergePeer == null || this.mMergeHost != null) {
            return DBG;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isMergePeer() {
        if (this.mMergePeer != null || this.mMergeHost == null) {
            return DBG;
        }
        return true;
    }

    public boolean isCallSessionMergePending() {
        return this.mCallSessionMergePending;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCallSessionMergePending(boolean callSessionMergePending) {
        this.mCallSessionMergePending = callSessionMergePending;
    }

    private boolean shouldProcessConferenceResult() {
        boolean areMergeTriggersDone = DBG;
        synchronized (this) {
            boolean isMergeHost = isMergeHost();
            boolean areMergeTriggersDone2 = DBG;
            if (isMergeHost || isMergePeer()) {
                if (isMergeHost()) {
                    logi("shouldProcessConferenceResult :: We are a merge host");
                    logi("shouldProcessConferenceResult :: Here is the merge peer=" + this.mMergePeer);
                    if (!isCallSessionMergePending() && !this.mMergePeer.isCallSessionMergePending()) {
                        areMergeTriggersDone2 = true;
                    }
                    areMergeTriggersDone = areMergeTriggersDone2;
                    if (!isMultiparty()) {
                        areMergeTriggersDone &= isSessionAlive(this.mTransientConferenceSession);
                    }
                } else if (isMergePeer()) {
                    logi("shouldProcessConferenceResult :: We are a merge peer");
                    logi("shouldProcessConferenceResult :: Here is the merge host=" + this.mMergeHost);
                    if (!isCallSessionMergePending() && !this.mMergeHost.isCallSessionMergePending()) {
                        areMergeTriggersDone2 = true;
                    }
                    areMergeTriggersDone = !this.mMergeHost.isMultiparty() ? areMergeTriggersDone2 & isSessionAlive(this.mMergeHost.mTransientConferenceSession) : !isCallSessionMergePending();
                } else {
                    loge("shouldProcessConferenceResult : merge in progress but call is neither host nor peer.");
                }
                StringBuilder sb = new StringBuilder();
                sb.append("shouldProcessConferenceResult :: returning:");
                sb.append(areMergeTriggersDone ? ImsManager.TRUE : ImsManager.FALSE);
                logi(sb.toString());
                return areMergeTriggersDone;
            }
            loge("shouldProcessConferenceResult :: no merge in progress");
            return DBG;
        }
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        StringBuilder sb = new StringBuilder();
        sb.append("[ImsCall objId:");
        sb.append(System.identityHashCode(this));
        sb.append(" onHold:");
        String str10 = "Y";
        sb.append(isOnHold() ? str10 : "N");
        sb.append(" mute:");
        if (isMuted()) {
            str = str10;
        } else {
            str = "N";
        }
        sb.append(str);
        ImsCallProfile imsCallProfile = this.mCallProfile;
        if (imsCallProfile != null) {
            sb.append(" mCallProfile:" + imsCallProfile);
            sb.append(" tech:");
            sb.append(imsCallProfile.getCallExtra("CallRadioTech"));
        }
        sb.append(" updateRequest:");
        sb.append(updateRequestToString(this.mUpdateRequest));
        sb.append(" merging:");
        if (isMerging()) {
            str2 = str10;
        } else {
            str2 = "N";
        }
        sb.append(str2);
        if (isMerging()) {
            if (isMergePeer()) {
                sb.append("P");
            } else {
                sb.append("H");
            }
        }
        sb.append(" merge action pending:");
        if (isCallSessionMergePending()) {
            str3 = str10;
        } else {
            str3 = "N";
        }
        sb.append(str3);
        sb.append(" merged:");
        if (isMerged()) {
            str4 = str10;
        } else {
            str4 = "N";
        }
        sb.append(str4);
        sb.append(" multiParty:");
        if (isMultiparty()) {
            str5 = str10;
        } else {
            str5 = "N";
        }
        sb.append(str5);
        sb.append(" confHost:");
        if (isConferenceHost()) {
            str6 = str10;
        } else {
            str6 = "N";
        }
        sb.append(str6);
        sb.append(" buried term:");
        if (this.mSessionEndDuringMerge) {
            str7 = str10;
        } else {
            str7 = "N";
        }
        sb.append(str7);
        sb.append(" isVideo: ");
        if (isVideoCall()) {
            str8 = str10;
        } else {
            str8 = "N";
        }
        sb.append(str8);
        sb.append(" wasVideo: ");
        if (this.mWasVideoCall) {
            str9 = str10;
        } else {
            str9 = "N";
        }
        sb.append(str9);
        sb.append(" isWifi: ");
        if (!isWifiCall()) {
            str10 = "N";
        }
        sb.append(str10);
        sb.append(" session:");
        sb.append(this.mSession);
        sb.append(" transientSession:");
        sb.append(this.mTransientConferenceSession);
        sb.append("]");
        return sb.toString();
    }

    private void throwImsException(Throwable t, int code) throws ImsException {
        if (t instanceof ImsException) {
            throw ((ImsException) t);
        }
        throw new ImsException(String.valueOf(code), t, code);
    }

    private String appendImsCallInfoToString(String s) {
        return s + " ImsCall=" + this;
    }

    private void trackVideoStateHistory(ImsCallProfile profile) {
        this.mWasVideoCall = (this.mWasVideoCall || profile.isVideoCall()) ? true : DBG;
    }

    public boolean wasVideoCall() {
        return this.mWasVideoCall;
    }

    public boolean isVideoCall() {
        boolean z;
        synchronized (this.mLockObj) {
            z = (this.mCallProfile == null || !this.mCallProfile.isVideoCall()) ? DBG : true;
        }
        return z;
    }

    public boolean isWifiCall() {
        synchronized (this.mLockObj) {
            ImsCallProfile imsCallProfile = this.mCallProfile;
            boolean z = DBG;
            if (imsCallProfile == null) {
                return DBG;
            }
            if (getRadioTechnology() == 18) {
                z = true;
            }
            return z;
        }
    }

    public int getRadioTechnology() {
        int radioTechnology;
        synchronized (this.mLockObj) {
            if (this.mCallProfile == null) {
                return 0;
            }
            String callType = this.mCallProfile.getCallExtra("CallRadioTech");
            if (callType == null || callType.isEmpty()) {
                callType = this.mCallProfile.getCallExtra("callRadioTech");
            }
            try {
                radioTechnology = Integer.parseInt(callType);
            } catch (NumberFormatException e) {
                radioTechnology = 0;
            }
            return radioTechnology;
        }
    }

    /* access modifiers changed from: protected */
    public void logi(String s) {
        Log.i(TAG, appendImsCallInfoToString(s));
    }

    /* access modifiers changed from: protected */
    public void logd(String s) {
        Log.d(TAG, appendImsCallInfoToString(s));
    }

    private void logv(String s) {
        Log.v(TAG, appendImsCallInfoToString(s));
    }

    /* access modifiers changed from: protected */
    public void loge(String s) {
        Log.e(TAG, appendImsCallInfoToString(s));
    }

    /* access modifiers changed from: protected */
    public void loge(String s, Throwable t) {
        Log.e(TAG, appendImsCallInfoToString(s), t);
    }

    /* access modifiers changed from: protected */
    public void copyCallProfileIfNecessary(ImsStreamMediaProfile profile) {
        this.mCallProfile.mMediaProfile.copyFrom(profile);
    }

    /* access modifiers changed from: protected */
    public void checkIfConferenceMerge(ImsReasonInfo reasonInfo) {
    }

    /* access modifiers changed from: protected */
    public void updateHoldStateIfNecessary(boolean hold) {
    }

    /* access modifiers changed from: protected */
    public boolean shouldSkipResetMergePending() {
        return DBG;
    }

    /* access modifiers changed from: protected */
    public void resetConferenceMergingFlag() {
    }

    /* access modifiers changed from: protected */
    public void setPendingUpdateMerge() {
    }

    public boolean isPendingResume() {
        boolean z;
        synchronized (this.mLockObj) {
            z = this.mUpdateRequest == UPDATE_RESUME ? true : DBG;
        }
        return z;
    }
}
