package org.cloudfoundry.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateFileHelper {

    public static final String APPCLOUD_STATE_FILE_CONFIGURATION = "droplet.yaml";
    public static final String STATE_FILE_KEY = "state_file";

    private static final Logger LOGGER = Logger.getLogger(StateFileHelper.class.getName());

    /**
     * Returns the state file location
     *
     * @param engineBaseDir Runtime (Tomcat, Virgo, ...) base directory
     * @return File object pointing to the state file or NULL if there was a problem obtaining the state file location
     */
    public static File readStateFile(String engineBaseDir) {
        LineNumberReader reader = null;

        try {
            StringBuilder relativePath = new StringBuilder().
                    append(engineBaseDir).
                    append(File.separatorChar).
                    append("..").
                    append(File.separatorChar);
            String stateFileConfiguration = new StringBuffer(relativePath).
                    append(APPCLOUD_STATE_FILE_CONFIGURATION).
                    toString();
            reader = new LineNumberReader(new FileReader(stateFileConfiguration));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(STATE_FILE_KEY)) {
                    int index = line.indexOf(":");
                    if (index != -1) {
                        String stateFileString = new StringBuffer(relativePath).
                                append(line.substring(index + 1).trim()).
                                toString();
                        File result = new File(stateFileString).getCanonicalFile();
                        LOGGER.info("Using state file '" + result + "'");
                        return result;
                    }
                }
            }

            LOGGER.warning("Unable to determine state file location");
            return null;
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Error reading state file location", ioe);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Cannot close state file reader", e);
                }
            }
        }
    }

    /**
     * Creates a state file
     *
     * @param stateFile File object pointing to the state file
     */
    public static void createStateFile(File stateFile) {
        if (stateFile != null) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(stateFile);
                writer.println("{\"state\": \"RUNNING\"}");
                writer.close();
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "State file not found", e);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

}