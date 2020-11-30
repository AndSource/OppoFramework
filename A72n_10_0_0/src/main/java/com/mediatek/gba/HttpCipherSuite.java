package com.mediatek.gba;

public enum HttpCipherSuite {
    TLS_RSA_WITH_NULL_MD5("SSL_RSA_WITH_NULL_MD5", 1),
    TLS_RSA_WITH_NULL_SHA("SSL_RSA_WITH_NULL_SHA", 2),
    TLS_RSA_EXPORT_WITH_RC4_40_MD5("SSL_RSA_EXPORT_WITH_RC4_40_MD5", 3),
    TLS_RSA_WITH_RC4_128_MD5("SSL_RSA_WITH_RC4_128_MD5", 4),
    TLS_RSA_WITH_RC4_128_SHA("SSL_RSA_WITH_RC4_128_SHA", 5),
    TLS_RSA_EXPORT_WITH_DES40_CBC_SHA("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", 8),
    TLS_RSA_WITH_DES_CBC_SHA("SSL_RSA_WITH_DES_CBC_SHA", 9),
    TLS_RSA_WITH_3DES_EDE_CBC_SHA("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 10),
    TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", 17),
    TLS_DHE_DSS_WITH_DES_CBC_SHA("SSL_DHE_DSS_WITH_DES_CBC_SHA", 18),
    TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 19),
    TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", 20),
    TLS_DHE_RSA_WITH_DES_CBC_SHA("SSL_DHE_RSA_WITH_DES_CBC_SHA", 21),
    TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 22),
    TLS_DH_anon_EXPORT_WITH_RC4_40_MD5("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", 23),
    TLS_DH_anon_WITH_RC4_128_MD5("SSL_DH_anon_WITH_RC4_128_MD5", 24),
    TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", 25),
    TLS_DH_anon_WITH_DES_CBC_SHA("SSL_DH_anon_WITH_DES_CBC_SHA", 26),
    TLS_DH_anon_WITH_3DES_EDE_CBC_SHA("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", 27),
    TLS_KRB5_WITH_DES_CBC_SHA("TLS_KRB5_WITH_DES_CBC_SHA", 30),
    TLS_KRB5_WITH_3DES_EDE_CBC_SHA("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 31),
    TLS_KRB5_WITH_RC4_128_SHA("TLS_KRB5_WITH_RC4_128_SHA", 32),
    TLS_KRB5_WITH_DES_CBC_MD5("TLS_KRB5_WITH_DES_CBC_MD5", 34),
    TLS_KRB5_WITH_3DES_EDE_CBC_MD5("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 35),
    TLS_KRB5_WITH_RC4_128_MD5("TLS_KRB5_WITH_RC4_128_MD5", 36),
    TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", 38),
    TLS_KRB5_EXPORT_WITH_RC4_40_SHA("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 40),
    TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", 41),
    TLS_KRB5_EXPORT_WITH_RC4_40_MD5("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 43),
    TLS_RSA_WITH_AES_128_CBC_SHA("TLS_RSA_WITH_AES_128_CBC_SHA", 47),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 50),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 51),
    TLS_DH_anon_WITH_AES_128_CBC_SHA("TLS_DH_anon_WITH_AES_128_CBC_SHA", 52),
    TLS_RSA_WITH_AES_256_CBC_SHA("TLS_RSA_WITH_AES_256_CBC_SHA", 53),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 56),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 57),
    TLS_DH_anon_WITH_AES_256_CBC_SHA("TLS_DH_anon_WITH_AES_256_CBC_SHA", 58),
    TLS_RSA_WITH_NULL_SHA256("TLS_RSA_WITH_NULL_SHA256", 59),
    TLS_RSA_WITH_AES_128_CBC_SHA256("TLS_RSA_WITH_AES_128_CBC_SHA256", 60),
    TLS_RSA_WITH_AES_256_CBC_SHA256("TLS_RSA_WITH_AES_256_CBC_SHA256", 61),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", 64),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", 103),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", 106),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", 107),
    TLS_DH_anon_WITH_AES_128_CBC_SHA256("TLS_DH_anon_WITH_AES_128_CBC_SHA256", 108),
    TLS_DH_anon_WITH_AES_256_CBC_SHA256("TLS_DH_anon_WITH_AES_256_CBC_SHA256", 109),
    TLS_RSA_WITH_AES_128_GCM_SHA256("TLS_RSA_WITH_AES_128_GCM_SHA256", 156),
    TLS_RSA_WITH_AES_256_GCM_SHA384("TLS_RSA_WITH_AES_256_GCM_SHA384", 157),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", 158),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", 162),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", 163),
    TLS_DH_anon_WITH_AES_128_GCM_SHA256("TLS_DH_anon_WITH_AES_128_GCM_SHA256", 166),
    TLS_DH_anon_WITH_AES_256_GCM_SHA384("TLS_DH_anon_WITH_AES_256_GCM_SHA384", 167),
    TLS_EMPTY_RENEGOTIATION_INFO_SCSV("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", 255),
    TLS_ECDH_ECDSA_WITH_NULL_SHA("TLS_ECDH_ECDSA_WITH_NULL_SHA", 49153),
    TLS_ECDH_ECDSA_WITH_RC4_128_SHA("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 49154),
    TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", 49155),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", 49156),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", 49157),
    TLS_ECDHE_ECDSA_WITH_NULL_SHA("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 49158),
    TLS_ECDHE_ECDSA_WITH_RC4_128_SHA("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 49159),
    TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", 49160),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", 49161),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", 49162),
    TLS_ECDH_RSA_WITH_NULL_SHA("TLS_ECDH_RSA_WITH_NULL_SHA", 49163),
    TLS_ECDH_RSA_WITH_RC4_128_SHA("TLS_ECDH_RSA_WITH_RC4_128_SHA", 49164),
    TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", 49165),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", 49166),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", 49167),
    TLS_ECDHE_RSA_WITH_NULL_SHA("TLS_ECDHE_RSA_WITH_NULL_SHA", 49168),
    TLS_ECDHE_RSA_WITH_RC4_128_SHA("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 49169),
    TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", 49170),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", 49171),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", 49172),
    TLS_ECDH_anon_WITH_NULL_SHA("TLS_ECDH_anon_WITH_NULL_SHA", 49173),
    TLS_ECDH_anon_WITH_RC4_128_SHA("TLS_ECDH_anon_WITH_RC4_128_SHA", 49174),
    TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", 49175),
    TLS_ECDH_anon_WITH_AES_128_CBC_SHA("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", 49176),
    TLS_ECDH_anon_WITH_AES_256_CBC_SHA("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", 49177),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 49187),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", 49188),
    TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", 49189),
    TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", 49190),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", 49191),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", 49192),
    TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", 49193),
    TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", 49194),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", 49195),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 49196),
    TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", 49197),
    TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", 49198),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", 787185),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", 49200),
    TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", 49201),
    TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", 49202);
    
    final String javaName;
    final byte[] mCipherSuiteCode;

    private HttpCipherSuite(String javaName2, int code) {
        this.javaName = javaName2;
        this.mCipherSuiteCode = intToByteArray(code);
    }

    public static HttpCipherSuite forJavaName(String javaName2) {
        if (!javaName2.startsWith("SSL_")) {
            return valueOf(javaName2);
        }
        return valueOf("TLS_" + javaName2.substring(4));
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{(byte) (value >>> 8), (byte) value};
    }
}
