package com.lingcreative.gradle.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties fromFile(File file) {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException(String.format("Can't load properties from file %s because it "
                    + (!file.exists() ? " does not exist" : " is not a normal file"), file));
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can't load properties from %s because %s", file.toString(), e.getMessage()), e);
        }
    }

    public static Properties fromClasspath(String classpath) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can't load properties from %s because %s", classpath, e.getMessage()), e);
        }
    }
}
