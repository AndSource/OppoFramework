package android.icu.impl.duration;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
/* compiled from: BasicPeriodBuilderFactory */
class FixedUnitBuilder extends PeriodBuilderImpl {
    private TimeUnit unit;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.FixedUnitBuilder.<init>(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    FixedUnitBuilder(android.icu.impl.duration.TimeUnit r1, android.icu.impl.duration.BasicPeriodBuilderFactory.Settings r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.icu.impl.duration.FixedUnitBuilder.<init>(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.FixedUnitBuilder.<init>(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.duration.FixedUnitBuilder.get(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.FixedUnitBuilder, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public static android.icu.impl.duration.FixedUnitBuilder get(android.icu.impl.duration.TimeUnit r1, android.icu.impl.duration.BasicPeriodBuilderFactory.Settings r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.duration.FixedUnitBuilder.get(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.FixedUnitBuilder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.FixedUnitBuilder.get(android.icu.impl.duration.TimeUnit, android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.FixedUnitBuilder");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.FixedUnitBuilder.handleCreate(long, long, boolean):android.icu.impl.duration.Period, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    protected android.icu.impl.duration.Period handleCreate(long r1, long r3, boolean r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.FixedUnitBuilder.handleCreate(long, long, boolean):android.icu.impl.duration.Period, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.FixedUnitBuilder.handleCreate(long, long, boolean):android.icu.impl.duration.Period");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.FixedUnitBuilder.withSettings(android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.PeriodBuilder, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    protected android.icu.impl.duration.PeriodBuilder withSettings(android.icu.impl.duration.BasicPeriodBuilderFactory.Settings r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.icu.impl.duration.FixedUnitBuilder.withSettings(android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.PeriodBuilder, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.FixedUnitBuilder.withSettings(android.icu.impl.duration.BasicPeriodBuilderFactory$Settings):android.icu.impl.duration.PeriodBuilder");
    }
}
