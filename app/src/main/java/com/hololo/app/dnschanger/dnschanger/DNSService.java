package com.hololo.app.dnschanger.dnschanger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.hololo.app.dnschanger.DNSChangerApp;
import com.hololo.app.dnschanger.R;
import com.hololo.app.dnschanger.model.DNSModel;
import com.hololo.app.dnschanger.utils.RxBus;
import com.hololo.app.dnschanger.utils.event.GetServiceInfo;
import com.hololo.app.dnschanger.utils.event.ServiceInfo;
import com.hololo.app.dnschanger.utils.event.StartEvent;
import com.hololo.app.dnschanger.utils.event.StopEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class DNSService extends VpnService {
    public final static String DNS_MODEL = "DNSModelIntent";
    private static final String TAG = "BigTextService";

    public static final String ACTION_DISMISS =
            "com.example.android.wearable.wear.wearnotifications.handlers.action.DISMISS";
    public static final String ACTION_SNOOZE =
            "com.example.android.wearable.wear.wearnotifications.handlers.action.SNOOZE";

    private static final long SNOOZE_TIME = TimeUnit.SECONDS.toMillis(5);
    public static final int NOTIFICATION_ID = 888;
    @Inject
    RxBus rxBus;
    @Inject
    Context context;
    @Inject
    Gson gson;

    private VpnService.Builder builder = new VpnService.Builder();
    private ParcelFileDescriptor fileDescriptor;
    private Thread mThread;
    private boolean shouldRun = true;
    private DatagramChannel tunnel;
    private DNSModel dnsModel;
    private SharedPreferences preferences;

    private Disposable subscriber;

    private void stopThisService() {
        this.shouldRun = false;
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferences.edit().putBoolean("isStarted", false).apply();
        preferences.edit().remove("dnsModel").apply();
        Timber.e("Servis kapandı.");
        subscriber.dispose();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        DNSChangerApp.getApplicationComponent().inject(this);
        subscribe();

    }

    private void subscribe() {
        subscriber = rxBus.getEvents().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof StopEvent) {
                    stopThisService();
                } else if (o instanceof GetServiceInfo) {
                    rxBus.sendEvent(new ServiceInfo(dnsModel));
                }
            }
        });
    }

    private void setTunnel(DatagramChannel tunnel) {
        this.tunnel = tunnel;
    }

    private void setFileDescriptor(ParcelFileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public int onStartCommand(final Intent paramIntent, int p1, int p2) {
        mThread = new Thread(new Runnable() {
            public void run() {
                try {
                    dnsModel = paramIntent.getParcelableExtra(DNS_MODEL);

                    String modelJSON = gson.toJson(dnsModel);
                    preferences.edit().putString("dnsModel", modelJSON).apply();

                    setFileDescriptor(builder.setSession(DNSService.this.getText(R.string.app_name).toString()).
                            addAddress("192.168.0.1", 24).addDnsServer(dnsModel.getFirstDns()).addDnsServer(dnsModel.getSecondDns()).establish());
                    setTunnel(DatagramChannel.open());
                    tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                    protect(tunnel.socket());

                    createNotificationChannel();

                    while (shouldRun)
                        Thread.sleep(100L);
                } catch (Exception exception) {
                    Timber.e(exception);
                } finally {
                    if (fileDescriptor != null) {
                        try {
                            fileDescriptor.close();
                            setFileDescriptor(null);
                        } catch (IOException e) {
                            Timber.d(e);
                        }
                    }
                }
            }
        }
                , "DNS Changer");
        mThread.start();
        rxBus.sendEvent(new StartEvent());
        preferences.edit().putBoolean("isStarted", true).apply();
        return Service.START_STICKY;
    }

    private void createNotificationChannel() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationId is a unique int for each notification that you must define
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last4Str = tmpStr.substring(tmpStr.length() - 5);
        int notificationId = Integer.valueOf(last4Str);
        final int NOTIFY_ID = 0; // ID of notification
        String id = "hoan"; // default_channel_id
        String title = "notification title"; // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notificationManager == null) {
            notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(dnsModel.getName()+": "+ dnsModel.getFirstDns())                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(dnsModel.getSecondDns()) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentTitle(dnsModel.getName()+": "+ dnsModel.getFirstDns())                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
                    .setContentText(dnsModel.getSecondDns()) // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }
}
