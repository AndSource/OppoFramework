package com.android.server.devicepolicy;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import com.android.internal.notification.SystemNotificationChannels;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* access modifiers changed from: package-private */
public class RemoteBugreportUtils {
    static final String BUGREPORT_MIMETYPE = "application/vnd.android.bugreport";
    static final String CTL_STOP = "ctl.stop";
    static final int NOTIFICATION_ID = 678432343;
    static final String REMOTE_BUGREPORT_SERVICE = "bugreportremote";
    static final long REMOTE_BUGREPORT_TIMEOUT_MILLIS = 600000;

    @Retention(RetentionPolicy.SOURCE)
    @interface RemoteBugreportNotificationType {
    }

    RemoteBugreportUtils() {
    }

    static Notification buildNotification(Context context, int type) {
        Intent dialogIntent = new Intent("android.settings.SHOW_REMOTE_BUGREPORT_DIALOG");
        dialogIntent.addFlags(268468224);
        dialogIntent.putExtra("android.app.extra.bugreport_notification_type", type);
        Notification.Builder builder = new Notification.Builder(context, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17303520).setOngoing(true).setLocalOnly(true).setContentIntent(PendingIntent.getActivityAsUser(context, type, dialogIntent, 0, null, UserHandle.CURRENT)).setColor(context.getColor(17170460));
        if (type == 2) {
            builder.setContentTitle(context.getString(17041014)).setProgress(0, 0, true);
        } else if (type == 1) {
            builder.setContentTitle(context.getString(17041121)).setProgress(0, 0, true);
        } else if (type == 3) {
            PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(context, NOTIFICATION_ID, new Intent("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED"), 268435456);
            builder.addAction(new Notification.Action.Builder((Icon) null, context.getString(17039838), PendingIntent.getBroadcast(context, NOTIFICATION_ID, new Intent("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED"), 268435456)).build()).addAction(new Notification.Action.Builder((Icon) null, context.getString(17041009), pendingIntentAccept).build()).setContentTitle(context.getString(17041011)).setContentText(context.getString(17041010)).setStyle(new Notification.BigTextStyle().bigText(context.getString(17041010)));
        }
        return builder.build();
    }
}
