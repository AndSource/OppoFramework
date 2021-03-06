package com.android.server.oppo;

import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.PrintWriter;
import java.lang.reflect.Field;

public class DumpObject {
    private static final boolean DEBUG = false;
    private static final String TAG = DumpObject.class.getSimpleName();
    private static final Object mInvalidObject = new Object();
    private final Object mLock = new Object();

    public void dumpValue(PrintWriter pw, Object obj) {
        dumpValue(pw, obj, "");
    }

    public void dumpValue(PrintWriter pw, Object obj, String variable) {
        synchronized (this.mLock) {
            try {
                Object obj_find = findObject(obj, variable);
                if (obj_find == mInvalidObject) {
                    pw.println("dump object fail,can not find object!");
                } else if (obj_find == null) {
                    pw.println("value is: null");
                } else {
                    if (isBaseType(obj_find.getClass())) {
                        dumpBaseTypeObject(pw, obj_find);
                    } else {
                        dumpComplexTypeObject(pw, obj_find);
                    }
                }
            } catch (Exception e) {
                pw.println("dump object variable fail!");
            }
        }
    }

    private void log(String msg) {
    }

    private boolean isBaseType(Class<?> cls) {
        boolean bBaseType;
        try {
            if (!cls.equals(String.class) && !cls.equals(Integer.class) && !cls.equals(Byte.class) && !cls.equals(Long.class) && !cls.equals(Double.class) && !cls.equals(Float.class) && !cls.equals(Character.class) && !cls.equals(Short.class) && !cls.equals(Boolean.class)) {
                if (!cls.isPrimitive()) {
                    bBaseType = false;
                    log("isBaseType, cls:" + cls + " bBaseType:" + bBaseType);
                    return bBaseType;
                }
            }
            bBaseType = true;
            log("isBaseType, cls:" + cls + " bBaseType:" + bBaseType);
            return bBaseType;
        } catch (Exception e) {
            log("isBaseType Exception, return false");
            return false;
        }
    }

    private void dumpBaseTypeObject(PrintWriter pw, Object obj) {
        pw.println(obj.getClass() + " value is:" + obj);
    }

    private void dumpComplexTypeObject(PrintWriter pw, Object obj) {
        for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    pw.println(cls + StringUtils.SPACE + field.getType() + StringUtils.SPACE + field.getName() + "=" + field.get(obj));
                    field.setAccessible(false);
                } catch (Exception e) {
                    pw.println(cls + "cannot access field.getName():" + field.getName());
                }
            }
        }
    }

    private Object findObject(Object obj, String variable) {
        if (variable == null || variable.isEmpty()) {
            return obj;
        }
        log("findObject, variable:" + variable);
        String[] variables = variable.split("\\.");
        for (int i = 0; i < variables.length; i++) {
            log("findObject, i:" + i + " variables[i]:" + variables[i]);
            boolean findField = false;
            try {
                for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
                    Field[] fields = cls.getDeclaredFields();
                    int length = fields.length;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            break;
                        }
                        Field field = fields[i2];
                        try {
                            field.setAccessible(true);
                            log("findObject, i:" + i + " found field.getName():" + field.getName());
                            if (field.getName().equals(variables[i])) {
                                obj = field.get(obj);
                                findField = true;
                                break;
                            }
                            field.setAccessible(false);
                            i2++;
                        } catch (Exception e) {
                        }
                    }
                    if (findField) {
                        break;
                    }
                }
                log("findObject, i:" + i + " findField:" + findField);
                if (!findField) {
                    return mInvalidObject;
                }
            } catch (Exception e2) {
                return mInvalidObject;
            }
        }
        return obj;
    }
}
