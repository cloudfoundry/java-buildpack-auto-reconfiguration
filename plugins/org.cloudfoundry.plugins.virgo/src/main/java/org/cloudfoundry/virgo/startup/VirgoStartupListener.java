package org.cloudfoundry.virgo.startup;

import org.cloudfoundry.util.StateFileHelper;
import org.eclipse.virgo.kernel.deployer.Deployer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.logging.Logger;

public class VirgoStartupListener implements BundleActivator {

    private File stateFile = StateFileHelper.readStateFile(".");

    private static final Logger LOGGER = Logger.getLogger(VirgoStartupListener.class.getName());

    public void start(BundleContext context) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = ObjectName.getInstance("org.eclipse.virgo.kernel:category=Control,type=Deployer");
        Deployer deployer = JMX.newMXBeanProxy(server, objectName, Deployer.class);

        File[] artifacts = new File("artifacts").listFiles();
        if (artifacts == null) {
            LOGGER.info("Nothing to deploy");
            return;
        }

        LOGGER.info("Starting deployment of " + Arrays.toString(artifacts));
        for (File artifact : artifacts) {
            LOGGER.info("Deploying artifact '" + artifact.getAbsolutePath() + "'");
            deployer.deploy(artifact.toURI().toString(), true);
        }
        LOGGER.info("All artifacts deployed successfully");
        StateFileHelper.createStateFile(stateFile != null ? stateFile : new File("..", "virgo.state"));
    }

    public void stop(BundleContext context) throws Exception {
        // nothing to do
    }


}