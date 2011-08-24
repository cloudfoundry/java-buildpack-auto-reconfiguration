package com.vmware.appcloud.tomcat;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;

import org.cloudfoundry.util.StateFileHelper;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.util.logging.Logger;

public class AppCloudLifecycleListener implements LifecycleListener, NotificationListener {

    private static final String J2EE_RUNNING_STATE = "j2ee.state.running";
    private static File stateFile = null;

    private static final Logger LOGGER = Logger.getLogger(AppCloudLifecycleListener.class.getName());

    public void lifecycleEvent(LifecycleEvent event) {
        Lifecycle lifecycle = event.getLifecycle();

        if (lifecycle instanceof StandardContext) {
            StandardContext context = (StandardContext) lifecycle;
            if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
                Container grandParent = context.getParent().getParent();
                if (grandParent instanceof StandardEngine) {
                    StandardEngine engine = (StandardEngine) grandParent;
                    stateFile = StateFileHelper.readStateFile(engine.getBaseDir());
                    context.addNotificationListener(this, null, null);
                }
            } else if (event.getType().equals(Lifecycle.AFTER_STOP_EVENT)) {
                LOGGER.severe("Stopping Tomcat because the context stopped.");
                System.exit(1);
            }
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        if (J2EE_RUNNING_STATE.equals(notification.getType())) {
            StateFileHelper.createStateFile(stateFile);
        }
    }

}
