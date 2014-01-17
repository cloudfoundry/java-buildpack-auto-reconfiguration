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

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class StandardApplicationConfigurationTest {

    private StandardApplicationConfiguration applicationConfiguration = new StandardApplicationConfiguration();

    @After
    public void tearDown() {
        System.clearProperty("config.file");
    }

    @Test
    public void getConfigurationDefault() throws IOException {
        assertExpectedKeys(this.applicationConfiguration.getConfiguration(), "alpha");
    }

    @Test
    public void getConfigurationAlternate() throws IOException {
        System.setProperty("config.file", "src/test/resources/play-alternate.conf");
        assertExpectedKeys(this.applicationConfiguration.getConfiguration(), "bravo");
    }

    @Test
    public void getConfigurationWithInclude() throws IOException {
        System.setProperty("config.file", "src/test/resources/play-with-include.conf");
        assertExpectedKeys(this.applicationConfiguration.getConfiguration(), "bravo", "charlie");
    }

    @Test
    public void getConfigurationWithMultipleIncludes() throws IOException {
        System.setProperty("config.file", "src/test/resources/play-with-includes.conf");
        assertExpectedKeys(this.applicationConfiguration.getConfiguration(), "alpha", "bravo", "delta");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationNonExistentInclude() {
        System.setProperty("config.file", "src/test/resources/play-with-non-existent-include.conf");
        this.applicationConfiguration.getConfiguration();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationNonExistentAlternate() throws IOException {
        System.setProperty("config.file", "play-non-existent.conf");
        this.applicationConfiguration.getConfiguration();
    }

    @Test
    public void getDatabaseNamesSingle() {
        System.setProperty("config.file", "src/test/resources/play-database-single.conf");
        assertDatabaseNames(this.applicationConfiguration.getDatabaseNames(), "default");
    }

    @Test
    public void getDatabaseNamesMultiple() {
        System.setProperty("config.file", "src/test/resources/play-database-multiple.conf");
        assertDatabaseNames(this.applicationConfiguration.getDatabaseNames(), "default", "other");
    }

    @Test
    public void getDatabaseNamesNone() {
        System.setProperty("config.file", "src/test/resources/play-database-none.conf");
        assertDatabaseNames(this.applicationConfiguration.getDatabaseNames());
    }

    private void assertExpectedKeys(Properties properties, String... keys) {
        assertEquals(keys.length, properties.size());

        for (String key : keys) {
            assertTrue(properties.containsKey(key));
        }
    }

    private void assertDatabaseNames(Set<String> actual, String... names) {
        assertEquals(names.length, actual.size());

        for (String name : names) {
            assertTrue(actual.contains(name));
        }
    }

}
