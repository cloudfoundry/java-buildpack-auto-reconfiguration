/*
 * Copyright 2011-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.reconfiguration.play;

import org.cloudfoundry.reconfiguration.util.IoUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StandardApplicationConfiguration implements ApplicationConfiguration {

    private static final Pattern DATABASE_PATTERN = Pattern.compile("db.(.*).driver");

    private static final Pattern INCLUDE_PATTERN = Pattern.compile("include \"(.*)\"");

    private final Object monitor = new Object();

    private volatile Properties configuration;

    @Override
    public Properties getConfiguration() {
        synchronized (this.monitor) {
            if (this.configuration == null) {
                this.configuration = loadConfiguration();
            }

            return this.configuration;
        }
    }

    @Override
    public Set<String> getDatabaseNames() {
        Set<String> names = new HashSet<String>();

        for (Object key : getConfiguration().keySet()) {
            Matcher matcher = DATABASE_PATTERN.matcher((String) key);

            if (matcher.find()) {
                names.add(matcher.group(1));
            }
        }

        return names;
    }

    private Properties loadConfiguration() {
        String configFile = System.getProperty("config.file");

        InputStream in = null;
        try {
            if (configFile != null) {
                in = new FileInputStream(configFile);
            } else if (hasResource("application.conf")) {
                in = getResource("application.conf");
            } else if (hasResource("application.properties")) {
                in = getResource("application.properties");
            } else {
                in = new ByteArrayInputStream("".getBytes(Charset.forName("UTF-8")));
            }

            return loadConfiguration(in);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format("Unable to read configuration from '%s', " +
                    "specified by config.file system property", configFile), e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private Properties loadConfiguration(InputStream in) {
        Properties properties = new Properties();

        BufferedReader reader = null;
        StringBuilder cache = new StringBuilder();
        String line;
        try {
            reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                Matcher matcher = INCLUDE_PATTERN.matcher(line);

                if (matcher.find()) {
                    properties.putAll(loadFromInclude(matcher.group(1)));
                } else {
                    cache.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration", e);
        } finally {
            IoUtils.closeQuietly(reader);
        }

        properties.putAll(loadFromCache(cache));

        return properties;
    }

    private Properties loadFromInclude(String name) {
        if (hasResource(name)) {
            return loadConfiguration(getResource(name));
        } else {
            throw new IllegalArgumentException(String.format("Unable to load configuration from '%s'", name));
        }
    }

    private Properties loadFromCache(StringBuilder cache) {
        StringReader in = null;
        try {
            in = new StringReader(cache.toString());

            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration", e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    private boolean hasResource(String name) {
        return getClassLoader().getResource(name) != null;
    }

    private InputStream getResource(String name) {
        return getClassLoader().getResourceAsStream(name);
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
