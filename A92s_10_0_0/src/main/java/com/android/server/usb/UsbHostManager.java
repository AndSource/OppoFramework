package com.android.server.usb;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.dump.DumpUtils;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.usb.descriptors.UsbDescriptor;
import com.android.server.usb.descriptors.UsbDescriptorParser;
import com.android.server.usb.descriptors.UsbDeviceDescriptor;
import com.android.server.usb.descriptors.UsbInterfaceDescriptor;
import com.android.server.usb.descriptors.report.TextReportCanvas;
import com.android.server.usb.descriptors.tree.UsbDescriptorsTree;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class UsbHostManager {
    private static final boolean DEBUG = true;
    private static final int LINUX_FOUNDATION_VID = 7531;
    private static final int MAX_CONNECT_RECORDS = 32;
    private static final String TAG = UsbHostManager.class.getSimpleName();
    static final SimpleDateFormat sFormat = new SimpleDateFormat("MM-dd HH:mm:ss:SSS");
    private final ArrayMap<String, ConnectionRecord> mConnected = new ArrayMap<>();
    private final LinkedList<ConnectionRecord> mConnections = new LinkedList<>();
    private final Context mContext;
    @GuardedBy({"mSettingsLock"})
    private UsbProfileGroupSettingsManager mCurrentSettings;
    @GuardedBy({"mLock"})
    private final HashMap<String, UsbDevice> mDevices = new HashMap<>();
    private Object mHandlerLock = new Object();
    private final String[] mHostBlacklist;
    private ConnectionRecord mLastConnect;
    private final Object mLock = new Object();
    private int mNumConnects;
    private Object mSettingsLock = new Object();
    private final UsbSettingsManager mSettingsManager;
    private final UsbAlsaManager mUsbAlsaManager;
    @GuardedBy({"mHandlerLock"})
    private ComponentName mUsbDeviceConnectionHandler;

    /* access modifiers changed from: private */
    public native void monitorUsbHostBus();

    private native ParcelFileDescriptor nativeOpenDevice(String str);

    class ConnectionRecord {
        static final int CONNECT = 0;
        static final int CONNECT_BADDEVICE = 2;
        static final int CONNECT_BADPARSE = 1;
        static final int DISCONNECT = -1;
        private static final int kDumpBytesPerLine = 16;
        final byte[] mDescriptors;
        String mDeviceAddress;
        final int mMode;
        long mTimestamp = System.currentTimeMillis();

        ConnectionRecord(String deviceAddress, int mode, byte[] descriptors) {
            this.mDeviceAddress = deviceAddress;
            this.mMode = mode;
            this.mDescriptors = descriptors;
        }

        private String formatTime() {
            return new StringBuilder(UsbHostManager.sFormat.format(new Date(this.mTimestamp))).toString();
        }

        /* access modifiers changed from: package-private */
        public void dump(DualDumpOutputStream dump, String idName, long id) {
            long token = dump.start(idName, id);
            dump.write("device_address", 1138166333441L, this.mDeviceAddress);
            dump.write("mode", 1159641169922L, this.mMode);
            dump.write(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, 1112396529667L, this.mTimestamp);
            if (this.mMode != -1) {
                UsbDescriptorParser parser = new UsbDescriptorParser(this.mDeviceAddress, this.mDescriptors);
                UsbDeviceDescriptor deviceDescriptor = parser.getDeviceDescriptor();
                dump.write("manufacturer", 1120986464260L, deviceDescriptor.getVendorID());
                dump.write("product", 1120986464261L, deviceDescriptor.getProductID());
                long isHeadSetToken = dump.start("is_headset", 1146756268038L);
                dump.write("in", 1133871366145L, parser.isInputHeadset());
                dump.write("out", 1133871366146L, parser.isOutputHeadset());
                dump.end(isHeadSetToken);
            }
            dump.end(token);
        }

        /* access modifiers changed from: package-private */
        public void dumpShort(IndentingPrintWriter pw) {
            if (this.mMode != -1) {
                pw.println(formatTime() + " Connect " + this.mDeviceAddress + " mode:" + this.mMode);
                UsbDescriptorParser parser = new UsbDescriptorParser(this.mDeviceAddress, this.mDescriptors);
                UsbDeviceDescriptor deviceDescriptor = parser.getDeviceDescriptor();
                pw.println("manfacturer:0x" + Integer.toHexString(deviceDescriptor.getVendorID()) + " product:" + Integer.toHexString(deviceDescriptor.getProductID()));
                pw.println("isHeadset[in: " + parser.isInputHeadset() + " , out: " + parser.isOutputHeadset() + "]");
                return;
            }
            pw.println(formatTime() + " Disconnect " + this.mDeviceAddress);
        }

        /* access modifiers changed from: package-private */
        public void dumpTree(IndentingPrintWriter pw) {
            if (this.mMode != -1) {
                pw.println(formatTime() + " Connect " + this.mDeviceAddress + " mode:" + this.mMode);
                UsbDescriptorParser parser = new UsbDescriptorParser(this.mDeviceAddress, this.mDescriptors);
                StringBuilder stringBuilder = new StringBuilder();
                UsbDescriptorsTree descriptorTree = new UsbDescriptorsTree();
                descriptorTree.parse(parser);
                descriptorTree.report(new TextReportCanvas(parser, stringBuilder));
                stringBuilder.append("isHeadset[in: " + parser.isInputHeadset() + " , out: " + parser.isOutputHeadset() + "]");
                pw.println(stringBuilder.toString());
                return;
            }
            pw.println(formatTime() + " Disconnect " + this.mDeviceAddress);
        }

        /* access modifiers changed from: package-private */
        public void dumpList(IndentingPrintWriter pw) {
            if (this.mMode != -1) {
                pw.println(formatTime() + " Connect " + this.mDeviceAddress + " mode:" + this.mMode);
                UsbDescriptorParser parser = new UsbDescriptorParser(this.mDeviceAddress, this.mDescriptors);
                StringBuilder stringBuilder = new StringBuilder();
                TextReportCanvas canvas = new TextReportCanvas(parser, stringBuilder);
                Iterator<UsbDescriptor> it = parser.getDescriptors().iterator();
                while (it.hasNext()) {
                    it.next().report(canvas);
                }
                pw.println(stringBuilder.toString());
                pw.println("isHeadset[in: " + parser.isInputHeadset() + " , out: " + parser.isOutputHeadset() + "]");
                return;
            }
            pw.println(formatTime() + " Disconnect " + this.mDeviceAddress);
        }

        /* access modifiers changed from: package-private */
        public void dumpRaw(IndentingPrintWriter pw) {
            if (this.mMode != -1) {
                pw.println(formatTime() + " Connect " + this.mDeviceAddress + " mode:" + this.mMode);
                int length = this.mDescriptors.length;
                StringBuilder sb = new StringBuilder();
                sb.append("Raw Descriptors ");
                sb.append(length);
                sb.append(" bytes");
                pw.println(sb.toString());
                int dataOffset = 0;
                for (int line = 0; line < length / 16; line++) {
                    StringBuilder sb2 = new StringBuilder();
                    int offset = 0;
                    while (offset < 16) {
                        sb2.append("0x");
                        sb2.append(String.format("0x%02X", Byte.valueOf(this.mDescriptors[dataOffset])));
                        sb2.append(StringUtils.SPACE);
                        offset++;
                        dataOffset++;
                    }
                    pw.println(sb2.toString());
                }
                StringBuilder sb3 = new StringBuilder();
                while (dataOffset < length) {
                    sb3.append("0x");
                    sb3.append(String.format("0x%02X", Byte.valueOf(this.mDescriptors[dataOffset])));
                    sb3.append(StringUtils.SPACE);
                    dataOffset++;
                }
                pw.println(sb3.toString());
                return;
            }
            pw.println(formatTime() + " Disconnect " + this.mDeviceAddress);
        }
    }

    public UsbHostManager(Context context, UsbAlsaManager alsaManager, UsbSettingsManager settingsManager) {
        this.mContext = context;
        this.mHostBlacklist = context.getResources().getStringArray(17236087);
        this.mUsbAlsaManager = alsaManager;
        this.mSettingsManager = settingsManager;
        String deviceConnectionHandler = context.getResources().getString(17039669);
        if (!TextUtils.isEmpty(deviceConnectionHandler)) {
            setUsbDeviceConnectionHandler(ComponentName.unflattenFromString(deviceConnectionHandler));
        }
    }

    public void setCurrentUserSettings(UsbProfileGroupSettingsManager settings) {
        synchronized (this.mSettingsLock) {
            this.mCurrentSettings = settings;
        }
    }

    private UsbProfileGroupSettingsManager getCurrentUserSettings() {
        UsbProfileGroupSettingsManager usbProfileGroupSettingsManager;
        synchronized (this.mSettingsLock) {
            usbProfileGroupSettingsManager = this.mCurrentSettings;
        }
        return usbProfileGroupSettingsManager;
    }

    public void setUsbDeviceConnectionHandler(ComponentName usbDeviceConnectionHandler) {
        synchronized (this.mHandlerLock) {
            this.mUsbDeviceConnectionHandler = usbDeviceConnectionHandler;
        }
    }

    private ComponentName getUsbDeviceConnectionHandler() {
        ComponentName componentName;
        synchronized (this.mHandlerLock) {
            componentName = this.mUsbDeviceConnectionHandler;
        }
        return componentName;
    }

    private boolean isBlackListed(String deviceAddress) {
        int count = this.mHostBlacklist.length;
        for (int i = 0; i < count; i++) {
            if (deviceAddress.startsWith(this.mHostBlacklist[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlackListed(int clazz, int subClass) {
        if (clazz == 9) {
            return true;
        }
        if (clazz == 3 && subClass == 1) {
            return true;
        }
        return false;
    }

    private void addConnectionRecord(String deviceAddress, int mode, byte[] rawDescriptors) {
        this.mNumConnects++;
        while (this.mConnections.size() >= 32) {
            this.mConnections.removeFirst();
        }
        ConnectionRecord rec = new ConnectionRecord(deviceAddress, mode, rawDescriptors);
        this.mConnections.add(rec);
        if (mode != -1) {
            this.mLastConnect = rec;
        }
        if (mode == 0) {
            this.mConnected.put(deviceAddress, rec);
        } else if (mode == -1) {
            this.mConnected.remove(deviceAddress);
        }
    }

    private void logUsbDevice(UsbDescriptorParser descriptorParser) {
        int vid = 0;
        int pid = 0;
        String mfg = "<unknown>";
        String product = "<unknown>";
        String version = "<unknown>";
        String serial = "<unknown>";
        UsbDeviceDescriptor deviceDescriptor = descriptorParser.getDeviceDescriptor();
        if (deviceDescriptor != null) {
            vid = deviceDescriptor.getVendorID();
            pid = deviceDescriptor.getProductID();
            mfg = deviceDescriptor.getMfgString(descriptorParser);
            product = deviceDescriptor.getProductString(descriptorParser);
            version = deviceDescriptor.getDeviceReleaseString();
            serial = deviceDescriptor.getSerialString(descriptorParser);
        }
        if (vid != LINUX_FOUNDATION_VID) {
            boolean hasAudio = descriptorParser.hasAudioInterface();
            boolean hasHid = descriptorParser.hasHIDInterface();
            boolean hasStorage = descriptorParser.hasStorageInterface();
            Slog.d(TAG, (("USB device attached: " + String.format("vidpid %04x:%04x", Integer.valueOf(vid), Integer.valueOf(pid))) + String.format(" mfg/product/ver/serial %s/%s/%s/%s", mfg, product, version, serial)) + String.format(" hasAudio/HID/Storage: %b/%b/%b", Boolean.valueOf(hasAudio), Boolean.valueOf(hasHid), Boolean.valueOf(hasStorage)));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0122, code lost:
        r0 = com.android.server.usb.UsbHostManager.TAG;
        android.util.Slog.d(r0, "beginUsbDeviceAdded(" + r24 + ") end");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x013e, code lost:
        return true;
     */
    private boolean usbDeviceAdded(String deviceAddress, int deviceClass, int deviceSubclass, byte[] descriptors) {
        String str = TAG;
        Slog.d(str, "usbDeviceAdded(" + deviceAddress + ") - start");
        if (isBlackListed(deviceAddress)) {
            Slog.d(TAG, "device address is black listed");
            return false;
        } else if (isBlackListed(deviceClass, deviceSubclass)) {
            Slog.d(TAG, "device class is black listed");
            return false;
        } else {
            UsbDescriptorParser parser = new UsbDescriptorParser(deviceAddress, descriptors);
            if (deviceClass == 0 && !checkUsbInterfacesBlackListed(parser)) {
                return false;
            }
            logUsbDevice(parser);
            synchronized (this.mLock) {
                if (this.mDevices.get(deviceAddress) != null) {
                    String str2 = TAG;
                    Slog.w(str2, "device already on mDevices list: " + deviceAddress);
                    return false;
                }
                UsbDevice.Builder newDeviceBuilder = parser.toAndroidUsbDevice();
                if (newDeviceBuilder == null) {
                    Slog.e(TAG, "Couldn't create UsbDevice object.");
                    addConnectionRecord(deviceAddress, 2, parser.getRawDescriptors());
                } else {
                    UsbSerialReader serialNumberReader = new UsbSerialReader(this.mContext, this.mSettingsManager, newDeviceBuilder.serialNumber);
                    UsbDevice newDevice = newDeviceBuilder.build(serialNumberReader);
                    serialNumberReader.setDevice(newDevice);
                    this.mDevices.put(deviceAddress, newDevice);
                    String str3 = TAG;
                    Slog.d(str3, "Added device " + newDevice);
                    HashMap<String, String> data = new HashMap<>();
                    data.put("usbDeviceManufacturerName", newDevice.getManufacturerName());
                    data.put("usbDeviceProductName", newDevice.getProductName());
                    data.put("usbDeviceProductVersion", newDevice.getProductName());
                    ActivityThread.sendCommonDcsUploader(DcsFingerprintStatisticsUtil.DCS_LOG_TAG, "usbDeviceInfo", data);
                    ComponentName usbDeviceConnectionHandler = getUsbDeviceConnectionHandler();
                    if (usbDeviceConnectionHandler == null) {
                        getCurrentUserSettings().deviceAttached(newDevice);
                    } else {
                        getCurrentUserSettings().deviceAttachedForFixedHandler(newDevice, usbDeviceConnectionHandler);
                    }
                    this.mUsbAlsaManager.usbDeviceAdded(deviceAddress, newDevice, parser);
                    addConnectionRecord(deviceAddress, 0, parser.getRawDescriptors());
                    StatsLog.write(77, newDevice.getVendorId(), newDevice.getProductId(), parser.hasAudioInterface(), parser.hasHIDInterface(), parser.hasStorageInterface(), 1, 0);
                }
            }
        }
    }

    private void usbDeviceRemoved(String deviceAddress) {
        String str = TAG;
        Slog.d(str, "usbDeviceRemoved(" + deviceAddress + ") end");
        synchronized (this.mLock) {
            UsbDevice device = this.mDevices.remove(deviceAddress);
            if (device != null) {
                String str2 = TAG;
                Slog.d(str2, "Removed device at " + deviceAddress + ": " + device.getProductName());
                this.mUsbAlsaManager.usbDeviceRemoved(deviceAddress);
                this.mSettingsManager.usbDeviceRemoved(device);
                getCurrentUserSettings().usbDeviceRemoved(device);
                ConnectionRecord current = this.mConnected.get(deviceAddress);
                addConnectionRecord(deviceAddress, -1, null);
                if (current != null) {
                    UsbDescriptorParser parser = new UsbDescriptorParser(deviceAddress, current.mDescriptors);
                    StatsLog.write(77, device.getVendorId(), device.getProductId(), parser.hasAudioInterface(), parser.hasHIDInterface(), parser.hasStorageInterface(), 0, System.currentTimeMillis() - current.mTimestamp);
                }
            } else {
                String str3 = TAG;
                Slog.d(str3, "Removed device at " + deviceAddress + " was already gone");
            }
        }
    }

    public void systemReady() {
        synchronized (this.mLock) {
            new Thread(null, new Runnable() {
                /* class com.android.server.usb.$$Lambda$UsbHostManager$XT3F5aQci4H6VWSBYBQQNSzpnvs */

                public final void run() {
                    UsbHostManager.this.monitorUsbHostBus();
                }
            }, "UsbService host thread").start();
        }
    }

    public void getDeviceList(Bundle devices) {
        synchronized (this.mLock) {
            for (String name : this.mDevices.keySet()) {
                devices.putParcelable(name, this.mDevices.get(name));
            }
        }
    }

    public ParcelFileDescriptor openDevice(String deviceAddress, UsbUserSettingsManager settings, String packageName, int uid) {
        ParcelFileDescriptor nativeOpenDevice;
        synchronized (this.mLock) {
            if (!isBlackListed(deviceAddress)) {
                UsbDevice device = this.mDevices.get(deviceAddress);
                if (device != null) {
                    settings.checkPermission(device, packageName, uid);
                    nativeOpenDevice = nativeOpenDevice(deviceAddress);
                } else {
                    throw new IllegalArgumentException("device " + deviceAddress + " does not exist or is restricted");
                }
            } else {
                throw new SecurityException("USB device is on a restricted bus");
            }
        }
        return nativeOpenDevice;
    }

    public void dump(DualDumpOutputStream dump, String idName, long id) {
        long token = dump.start(idName, id);
        synchronized (this.mHandlerLock) {
            if (this.mUsbDeviceConnectionHandler != null) {
                DumpUtils.writeComponentName(dump, "default_usb_host_connection_handler", 1146756268033L, this.mUsbDeviceConnectionHandler);
            }
        }
        synchronized (this.mLock) {
            for (String name : this.mDevices.keySet()) {
                com.android.internal.usb.DumpUtils.writeDevice(dump, "devices", 2246267895810L, this.mDevices.get(name));
            }
            dump.write("num_connects", 1120986464259L, this.mNumConnects);
            Iterator<ConnectionRecord> it = this.mConnections.iterator();
            while (it.hasNext()) {
                it.next().dump(dump, "connections", 2246267895812L);
            }
        }
        dump.end(token);
    }

    public void dumpDescriptors(IndentingPrintWriter pw, String[] args) {
        if (this.mLastConnect != null) {
            pw.println("Last Connected USB Device:");
            if (args.length <= 1 || args[1].equals("-dump-short")) {
                this.mLastConnect.dumpShort(pw);
            } else if (args[1].equals("-dump-tree")) {
                this.mLastConnect.dumpTree(pw);
            } else if (args[1].equals("-dump-list")) {
                this.mLastConnect.dumpList(pw);
            } else if (args[1].equals("-dump-raw")) {
                this.mLastConnect.dumpRaw(pw);
            }
        } else {
            pw.println("No USB Devices have been connected.");
        }
    }

    private boolean checkUsbInterfacesBlackListed(UsbDescriptorParser parser) {
        boolean shouldIgnoreDevice = false;
        Iterator<UsbDescriptor> it = parser.getDescriptors().iterator();
        while (it.hasNext()) {
            UsbDescriptor descriptor = it.next();
            if (descriptor instanceof UsbInterfaceDescriptor) {
                UsbInterfaceDescriptor iface = (UsbInterfaceDescriptor) descriptor;
                shouldIgnoreDevice = isBlackListed(iface.getUsbClass(), iface.getUsbSubclass());
                if (!shouldIgnoreDevice) {
                    break;
                }
            }
        }
        if (!shouldIgnoreDevice) {
            return true;
        }
        Slog.d(TAG, "usb interface class is black listed");
        return false;
    }
}
