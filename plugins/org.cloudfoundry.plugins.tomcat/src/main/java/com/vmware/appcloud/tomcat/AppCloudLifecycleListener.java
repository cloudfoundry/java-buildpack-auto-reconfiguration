package com.vmware.appcloud.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.*;

public class AppCloudLifecycleListener implements LifecycleListener, NotificationListener {

    private String stateFile;

    private static final String APPCLOUD_STATE_FILE = "tomcat.state";
    private static final String J2EE_RUNNING_STATE = "j2ee.state.running";

    public void lifecycleEvent(LifecycleEvent event) {
        Lifecycle lifecycle = event.getLifecycle();

        if (lifecycle instanceof StandardContext) {
            StandardContext context = (StandardContext) lifecycle;
            if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
                Container grandParent = context.getParent().getParent();
                if (grandParent instanceof StandardEngine) {
                    StandardEngine engine = (StandardEngine) grandParent;
                    String relativePath = new StringBuilder()
                            .append("..")
                            .append(File.separator)
                            .append(APPCLOUD_STATE_FILE)
                            .toString();
                    stateFile = new File(engine.getBaseDir(), relativePath).getAbsolutePath();
                    context.addNotificationListener(this, null, null);
                }
            } else if (event.getType().equals(Lifecycle.AFTER_STOP_EVENT)) {
                System.err.println("Stopping Tomcat because the context stopped.");
                System.exit(1);
            }
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        if (J2EE_RUNNING_STATE.equals(notification.getType())) {
            try {
                PrintWriter writer = new PrintWriter(stateFile);
                writer.println("{\"state\": \"RUNNING\"}");
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
