package org.cloudfoundry.util;

import java.io.*;

public class StateFileHelper {

    public static final String APPCLOUD_STATE_FILE_CONFIGURATION = "droplet.yaml";
    public static final String STATE_FILE_KEY = "state_file";

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
                    append("auto-reconfiguration/src/main").
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
                        return new File(stateFileString).getAbsoluteFile();
                    }
                }
            }

            return null;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

}