/*
 * Copyright 2007-2008 the original author or authors.
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
package com.lingcreative.gradle.wrapper;

import org.gradle.wrapper.WrapperConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.gradle.wrapper.WrapperExecutor.*;

public class ConfigurationLoader {

    public static WrapperConfiguration load(Properties properties) {
        WrapperConfiguration config = new WrapperConfiguration();
        try {
            config.setDistribution(readDistroUrl(properties));
            config.setDistributionBase(getProperty(properties, DISTRIBUTION_BASE_PROPERTY, config.getDistributionBase()));
            config.setDistributionPath(getProperty(properties, DISTRIBUTION_PATH_PROPERTY, config.getDistributionPath()));
            config.setDistributionSha256Sum(getProperty(properties, DISTRIBUTION_SHA_256_SUM, config.getDistributionSha256Sum(), false));
            config.setZipBase(getProperty(properties, ZIP_STORE_BASE_PROPERTY, config.getZipBase()));
            config.setZipPath(getProperty(properties, ZIP_STORE_PATH_PROPERTY, config.getZipPath()));
            return config;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not load wrapper properties."), e);
        }
    }

    private static URI readDistroUrl(Properties properties) throws URISyntaxException {
        if (properties.getProperty(DISTRIBUTION_URL_PROPERTY) == null) {
            return null;
        }
        return new URI(getProperty(properties, DISTRIBUTION_URL_PROPERTY));
    }

    private static String getProperty(Properties properties, String propertyName) {
        return getProperty(properties, propertyName, null, true);
    }

    private static String getProperty(Properties properties, String propertyName, String defaultValue) {
        return getProperty(properties, propertyName, defaultValue, true);
    }

    private static String getProperty(Properties properties, String propertyName, String defaultValue, boolean required) {
        String value = properties.getProperty(propertyName);
        if (value != null) {
            return value;
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        if (required) {
            return reportMissingProperty(propertyName);
        } else {
            return null;
        }
    }

    private static String reportMissingProperty(String propertyName) {
        throw new RuntimeException(String.format(
                "No value with key '%s' specified in wrapper properties file'.", propertyName));
    }
}
