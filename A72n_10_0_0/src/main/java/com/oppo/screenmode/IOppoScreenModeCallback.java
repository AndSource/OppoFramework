package com.oppo.screenmode;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoScreenModeCallback extends IInterface {
    void requestRefreshRate(String str, int i) throws RemoteException;

    public static class Default implements IOppoScreenModeCallback {
        @Override // com.oppo.screenmode.IOppoScreenModeCallback
        public void requestRefreshRate(String pkg, int rate) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoScreenModeCallback {
        private static final String DESCRIPTOR = "com.oppo.screenmode.IOppoScreenModeCallback";
        static final int TRANSACTION_requestRefreshRate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoScreenModeCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoScreenModeCallback)) {
                return new Proxy(obj);
            }
            return (IOppoScreenModeCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "requestRefreshRate";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                requestRefreshRate(data.readString(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoScreenModeCallback {
            public static IOppoScreenModeCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.oppo.screenmode.IOppoScreenModeCallback
            public void requestRefreshRate(String pkg, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(rate);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestRefreshRate(pkg, rate);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoScreenModeCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoScreenModeCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
