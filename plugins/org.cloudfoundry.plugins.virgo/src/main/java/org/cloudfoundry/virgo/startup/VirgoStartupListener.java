package org.cloudfoundry.virgo.startup;

import org.eclipse.virgo.kernel.deployer.Deployer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;

import org.cloudfoundry.util.StateFileHelper;

public class VirgoStartupListener implements BundleActivator {

    private File stateFile = StateFileHelper.readStateFile(".");

    public void start(BundleContext context) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = ObjectName.getInstance("org.eclipse.virgo.kernel:category=Control,type=Deployer");
        Deployer deployer = JMX.newMXBeanProxy(server, objectName, Deployer.class);

        File[] artifacts = new File("artifacts").listFiles();
        if (artifacts != null) {
            for (File artifact : artifacts) {
                deployer.deploy(artifact.toURI().toString(), true);
            }
            StateFileHelper.createStateFile(stateFile != null ? stateFile : new File("..", "virgo.state"));
        }
    }

    public void stop(BundleContext context) throws Exception {
        // nothing to do
    }


}