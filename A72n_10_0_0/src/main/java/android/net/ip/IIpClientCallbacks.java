package android.net.ip;

import android.net.DhcpResultsParcelable;
import android.net.LinkProperties;
import android.net.ip.IIpClient;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIpClientCallbacks extends IInterface {
    public static final int VERSION = 3;

    int getInterfaceVersion() throws RemoteException;

    void installPacketFilter(byte[] bArr) throws RemoteException;

    void onDhcpRenewCount() throws RemoteException;

    void onDhcpStateChange(DhcpResultsParcelable dhcpResultsParcelable) throws RemoteException;

    void onDoDupArp(String str, int i, int i2) throws RemoteException;

    void onDoGatewayDetect(String str, int i, int i2) throws RemoteException;

    void onFindDupServer(String str) throws RemoteException;

    void onFixServerFailure(String str) throws RemoteException;

    void onIpClientCreated(IIpClient iIpClient) throws RemoteException;

    void onLinkPropertiesChange(LinkProperties linkProperties) throws RemoteException;

    void onNewDhcpResults(DhcpResultsParcelable dhcpResultsParcelable) throws RemoteException;

    void onPostDhcpAction() throws RemoteException;

    void onPreDhcpAction() throws RemoteException;

    void onProvisioningFailure(LinkProperties linkProperties) throws RemoteException;

    void onProvisioningSuccess(LinkProperties linkProperties) throws RemoteException;

    void onQuit() throws RemoteException;

    void onReachabilityLost(String str) throws RemoteException;

    void onSwitchServerFailure(String str) throws RemoteException;

    void onUpdateLeaseExpriy(long j) throws RemoteException;

    void setFallbackMulticastFilter(boolean z) throws RemoteException;

    void setNeighborDiscoveryOffload(boolean z) throws RemoteException;

    void startReadPacketFilter() throws RemoteException;

    public static class Default implements IIpClientCallbacks {
        @Override // android.net.ip.IIpClientCallbacks
        public void onIpClientCreated(IIpClient ipClient) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onPreDhcpAction() throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onPostDhcpAction() throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onNewDhcpResults(DhcpResultsParcelable dhcpResults) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onProvisioningSuccess(LinkProperties newLp) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onProvisioningFailure(LinkProperties newLp) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onLinkPropertiesChange(LinkProperties newLp) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onReachabilityLost(String logMsg) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onQuit() throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void installPacketFilter(byte[] filter) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void startReadPacketFilter() throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void setFallbackMulticastFilter(boolean enabled) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void setNeighborDiscoveryOffload(boolean enable) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onFindDupServer(String server) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onUpdateLeaseExpriy(long time) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onSwitchServerFailure(String server) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onFixServerFailure(String server) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onDhcpRenewCount() throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onDoDupArp(String ifaceName, int myAddress, int target) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onDoGatewayDetect(String ifaceName, int myAddress, int target) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public void onDhcpStateChange(DhcpResultsParcelable dhcpResults) throws RemoteException {
        }

        @Override // android.net.ip.IIpClientCallbacks
        public int getInterfaceVersion() {
            return -1;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIpClientCallbacks {
        private static final String DESCRIPTOR = "android.net.ip.IIpClientCallbacks";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_installPacketFilter = 10;
        static final int TRANSACTION_onDhcpRenewCount = 18;
        static final int TRANSACTION_onDhcpStateChange = 21;
        static final int TRANSACTION_onDoDupArp = 19;
        static final int TRANSACTION_onDoGatewayDetect = 20;
        static final int TRANSACTION_onFindDupServer = 14;
        static final int TRANSACTION_onFixServerFailure = 17;
        static final int TRANSACTION_onIpClientCreated = 1;
        static final int TRANSACTION_onLinkPropertiesChange = 7;
        static final int TRANSACTION_onNewDhcpResults = 4;
        static final int TRANSACTION_onPostDhcpAction = 3;
        static final int TRANSACTION_onPreDhcpAction = 2;
        static final int TRANSACTION_onProvisioningFailure = 6;
        static final int TRANSACTION_onProvisioningSuccess = 5;
        static final int TRANSACTION_onQuit = 9;
        static final int TRANSACTION_onReachabilityLost = 8;
        static final int TRANSACTION_onSwitchServerFailure = 16;
        static final int TRANSACTION_onUpdateLeaseExpriy = 15;
        static final int TRANSACTION_setFallbackMulticastFilter = 12;
        static final int TRANSACTION_setNeighborDiscoveryOffload = 13;
        static final int TRANSACTION_startReadPacketFilter = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIpClientCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIpClientCallbacks)) {
                return new Proxy(obj);
            }
            return (IIpClientCallbacks) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DhcpResultsParcelable _arg0;
            LinkProperties _arg02;
            LinkProperties _arg03;
            LinkProperties _arg04;
            DhcpResultsParcelable _arg05;
            if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                boolean _arg06 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onIpClientCreated(IIpClient.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onPreDhcpAction();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onPostDhcpAction();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = DhcpResultsParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onNewDhcpResults(_arg0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onProvisioningSuccess(_arg02);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onProvisioningFailure(_arg03);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        onLinkPropertiesChange(_arg04);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onReachabilityLost(data.readString());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onQuit();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        installPacketFilter(data.createByteArray());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        startReadPacketFilter();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        setFallbackMulticastFilter(_arg06);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        setNeighborDiscoveryOffload(_arg06);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onFindDupServer(data.readString());
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        onUpdateLeaseExpriy(data.readLong());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onSwitchServerFailure(data.readString());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        onFixServerFailure(data.readString());
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        onDhcpRenewCount();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        onDoDupArp(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        onDoGatewayDetect(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = DhcpResultsParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        onDhcpStateChange(_arg05);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIpClientCallbacks {
            public static IIpClientCallbacks sDefaultImpl;
            private int mCachedVersion = -1;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onIpClientCreated(IIpClient ipClient) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(ipClient != null ? ipClient.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onIpClientCreated(ipClient);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onPreDhcpAction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPreDhcpAction();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onPostDhcpAction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPostDhcpAction();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onNewDhcpResults(DhcpResultsParcelable dhcpResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dhcpResults != null) {
                        _data.writeInt(1);
                        dhcpResults.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNewDhcpResults(dhcpResults);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onProvisioningSuccess(LinkProperties newLp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newLp != null) {
                        _data.writeInt(1);
                        newLp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProvisioningSuccess(newLp);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onProvisioningFailure(LinkProperties newLp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newLp != null) {
                        _data.writeInt(1);
                        newLp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProvisioningFailure(newLp);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onLinkPropertiesChange(LinkProperties newLp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newLp != null) {
                        _data.writeInt(1);
                        newLp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLinkPropertiesChange(newLp);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onReachabilityLost(String logMsg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(logMsg);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReachabilityLost(logMsg);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onQuit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuit();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void installPacketFilter(byte[] filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(filter);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().installPacketFilter(filter);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void startReadPacketFilter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startReadPacketFilter();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void setFallbackMulticastFilter(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setFallbackMulticastFilter(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void setNeighborDiscoveryOffload(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setNeighborDiscoveryOffload(enable);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onFindDupServer(String server) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(server);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFindDupServer(server);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onUpdateLeaseExpriy(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUpdateLeaseExpriy(time);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onSwitchServerFailure(String server) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(server);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSwitchServerFailure(server);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onFixServerFailure(String server) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(server);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFixServerFailure(server);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onDhcpRenewCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDhcpRenewCount();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onDoDupArp(String ifaceName, int myAddress, int target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifaceName);
                    _data.writeInt(myAddress);
                    _data.writeInt(target);
                    if (this.mRemote.transact(19, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDoDupArp(ifaceName, myAddress, target);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onDoGatewayDetect(String ifaceName, int myAddress, int target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifaceName);
                    _data.writeInt(myAddress);
                    _data.writeInt(target);
                    if (this.mRemote.transact(20, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDoGatewayDetect(ifaceName, myAddress, target);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public void onDhcpStateChange(DhcpResultsParcelable dhcpResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dhcpResults != null) {
                        _data.writeInt(1);
                        dhcpResults.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDhcpStateChange(dhcpResults);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClientCallbacks
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(IIpClientCallbacks impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIpClientCallbacks getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
