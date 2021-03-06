package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Message;
import android.util.SparseArray;
import com.android.internal.telephony.IOppoAdnRecordCache;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import java.util.ArrayList;
import java.util.Iterator;

public class AdnRecordCache extends AbstractAdnRecordCache implements IccConstants {
    protected static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 1;
    protected static final int EVENT_UPDATE_ADN_DONE = 2;
    protected SparseArray<ArrayList<AdnRecord>> mAdnLikeFiles = new SparseArray<>();
    @UnsupportedAppUsage
    protected SparseArray<ArrayList<Message>> mAdnLikeWaiters = new SparseArray<>();
    @UnsupportedAppUsage
    protected IccFileHandler mFh;
    @UnsupportedAppUsage
    protected SparseArray<Message> mUserWriteResponse = new SparseArray<>();
    @UnsupportedAppUsage
    protected UsimPhoneBookManager mUsimPhoneBookManager;

    public AdnRecordCache(IccFileHandler fh) {
        this.mFh = fh;
        this.mUsimPhoneBookManager = new UsimPhoneBookManager(this.mFh, this);
        this.mReference = (IOppoAdnRecordCache) OppoTelephonyFactory.getInstance().getFeature(IOppoAdnRecordCache.DEFAULT, this);
    }

    @UnsupportedAppUsage
    public void reset() {
        this.mAdnLikeFiles.clear();
        this.mUsimPhoneBookManager.reset();
        clearWaiters();
        clearUserWriters();
    }

    /* access modifiers changed from: protected */
    public void clearWaiters() {
        int size = this.mAdnLikeWaiters.size();
        for (int i = 0; i < size; i++) {
            notifyWaiters(this.mAdnLikeWaiters.valueAt(i), new AsyncResult((Object) null, (Object) null, new RuntimeException("AdnCache reset")));
        }
        this.mAdnLikeWaiters.clear();
    }

    private void clearUserWriters() {
        int size = this.mUserWriteResponse.size();
        for (int i = 0; i < size; i++) {
            sendErrorResponse(this.mUserWriteResponse.valueAt(i), "AdnCace reset");
        }
        this.mUserWriteResponse.clear();
    }

    @UnsupportedAppUsage
    public ArrayList<AdnRecord> getRecordsIfLoaded(int efid) {
        return this.mAdnLikeFiles.get(efid);
    }

    @UnsupportedAppUsage
    public int extensionEfForEf(int efid) {
        if (efid == 20272) {
            return 0;
        }
        if (efid == 28480) {
            return IccConstants.EF_EXT1;
        }
        if (efid == 28489) {
            return IccConstants.EF_EXT3;
        }
        if (efid == 28615) {
            return IccConstants.EF_EXT6;
        }
        if (efid == 28474) {
            return IccConstants.EF_EXT1;
        }
        if (efid != 28475) {
            return -1;
        }
        return IccConstants.EF_EXT2;
    }

    @UnsupportedAppUsage
    private void sendErrorResponse(Message response, String errString) {
        if (response != null) {
            AsyncResult.forMessage(response).exception = new RuntimeException(errString);
            response.sendToTarget();
        }
    }

    @UnsupportedAppUsage
    public void updateAdnByIndex(int efid, AdnRecord adn, int recordIndex, String pin2, Message response) {
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else if (this.mUserWriteResponse.get(efid) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
        }
    }

    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int index;
        int extensionEF;
        int efid2 = efid;
        int extensionEF2 = extensionEfForEf(efid);
        if (extensionEF2 < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        if (efid2 == 20272) {
            oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            sendErrorResponse(response, "Adn list not exist for EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        int index2 = -1;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        int count = 1;
        while (true) {
            if (!it.hasNext()) {
                break;
            } else if (oldAdn.isEqual(it.next())) {
                index2 = count;
                break;
            } else {
                count++;
            }
        }
        if (index2 == -1) {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
            return;
        }
        if (efid2 == 20272) {
            AdnRecord foundAdn = oldAdnList.get(index2 - 1);
            efid2 = foundAdn.mEfid;
            int extensionEF3 = foundAdn.mExtRecord;
            int index3 = foundAdn.mRecordNumber;
            newAdn.mEfid = efid2;
            newAdn.mExtRecord = extensionEF3;
            newAdn.mRecordNumber = index3;
            extensionEF = extensionEF3;
            index = index3;
        } else {
            extensionEF = extensionEF2;
            index = index2;
        }
        if (this.mUserWriteResponse.get(efid2) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid2).toUpperCase());
            return;
        }
        this.mUserWriteResponse.put(efid2, response);
        new AdnRecordLoader(this.mFh).updateEF(newAdn, efid2, extensionEF, index, pin2, obtainMessage(2, efid2, index, newAdn));
    }

    public void requestLoadAllAdnLike(int efid, int extensionEf, Message response) {
        ArrayList<AdnRecord> result;
        if (efid == 20272) {
            result = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
        } else {
            result = getRecordsIfLoaded(efid);
        }
        if (result == null) {
            ArrayList<Message> waiters = this.mAdnLikeWaiters.get(efid);
            if (waiters != null) {
                waiters.add(response);
                return;
            }
            ArrayList<Message> waiters2 = new ArrayList<>();
            waiters2.add(response);
            this.mAdnLikeWaiters.put(efid, waiters2);
            if (extensionEf >= 0) {
                new AdnRecordLoader(this.mFh).loadAllFromEF(efid, extensionEf, obtainMessage(1, efid, 0));
            } else if (response != null) {
                AsyncResult forMessage = AsyncResult.forMessage(response);
                forMessage.exception = new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
                response.sendToTarget();
            }
        } else if (response != null) {
            AsyncResult.forMessage(response).result = result;
            response.sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWaiters(ArrayList<Message> waiters, AsyncResult ar) {
        if (waiters != null) {
            int s = waiters.size();
            for (int i = 0; i < s; i++) {
                Message waiter = waiters.get(i);
                AsyncResult.forMessage(waiter, ar.result, ar.exception);
                waiter.sendToTarget();
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int efid = msg.arg1;
            ArrayList<Message> waiters = this.mAdnLikeWaiters.get(efid);
            this.mAdnLikeWaiters.delete(efid);
            if (ar.exception == null) {
                this.mAdnLikeFiles.put(efid, (ArrayList) ar.result);
            }
            notifyWaiters(waiters, ar);
        } else if (i == 2) {
            AsyncResult ar2 = (AsyncResult) msg.obj;
            int efid2 = msg.arg1;
            int index = msg.arg2;
            AdnRecord adn = (AdnRecord) ar2.userObj;
            if (ar2.exception == null) {
                this.mAdnLikeFiles.get(efid2).set(index - 1, adn);
                this.mUsimPhoneBookManager.invalidateCache();
            }
            Message response = this.mUserWriteResponse.get(efid2);
            this.mUserWriteResponse.delete(efid2);
            if (response != null) {
                AsyncResult.forMessage(response, (Object) null, ar2.exception);
                response.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.AbstractAdnRecordCache
    public Message getUserWriteResponse(int efid) {
        return this.mUserWriteResponse.get(efid);
    }

    @Override // com.android.internal.telephony.uicc.AbstractAdnRecordCache
    public void putUserWriteResponse(int efid, Message response) {
        this.mUserWriteResponse.put(efid, response);
    }
}
