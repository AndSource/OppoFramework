package com.android.internal.telephony.cat;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/* compiled from: BipCommandParams */
class ReceiveDataParams extends CommandParams {
    int channelDataLength;
    int mReceiveDataCid;
    TextMessage textMsg;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void, dex:  in method: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    ReceiveDataParams(com.android.internal.telephony.cat.CommandDetails r1, int r2, int r3, com.android.internal.telephony.cat.TextMessage r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void, dex:  in method: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.ReceiveDataParams.<init>(com.android.internal.telephony.cat.CommandDetails, int, int, com.android.internal.telephony.cat.TextMessage):void");
    }
}
