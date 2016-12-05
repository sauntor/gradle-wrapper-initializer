package com.lingcreative.gradle.wrapper;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;
import org.gradle.cli.SystemPropertiesCommandLineConverter;
import org.gradle.wrapper.*;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.gradle.wrapper.GradleWrapperMain.*;

/**
 * Main entry point for Gradle Initializer.
 * It supports gradle wrapper's cli options and plus the followings:
 * <code>
 *     -d | --distributions-dir=
 *             [Required] the directory contains local copies of gradle distributions
 *     -p | --project-dir=
 *             [Optional] the project directory for which a local copy of gradle will be installed to gradle user home(used by gradle wrapper),
 *             all distributions found in the distributions dir(specified by previous option) will be installed if this option is omitted
 *     -v | --gradle-version=
 *             [Optional] only initialize the specified version of gradle wrapper
 * </code>
 * @author sauntor
 * @since 1.0
 */
public class GradleWrapperInitializer {
    public static final String GRADLE_DOWNLOADED_ZIPS_DIR_OPTION = "d";
    public static final String GRADLE_DOWNLOADED_ZIPS_DIR_DETAILED_OPTION = "distributions-dir";
    public static final String GRADLE_VERSION_OPTION = "v";
    public static final String GRADLE_VERSION_DETAILED_OPTION = "gradle-version";
    public static final String GRADLE_PROJECT_DIR_OPTION = "p";
    public static final String GRADLE_PROJECT_DIR_DETAILED_OPTION = "project-dir";


    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new CommandLineParser();
        parser.allowUnknownOptions();
        parser.option(GRADLE_QUIET_OPTION, GRADLE_QUIET_DETAILED_OPTION);
        parser.option(GRADLE_USER_HOME_OPTION, GRADLE_USER_HOME_DETAILED_OPTION).hasArgument();
        parser.option(GRADLE_DOWNLOADED_ZIPS_DIR_OPTION, GRADLE_DOWNLOADED_ZIPS_DIR_DETAILED_OPTION).hasArgument();
        parser.option(GRADLE_VERSION_OPTION, GRADLE_VERSION_DETAILED_OPTION).hasArgument();
        parser.option(GRADLE_PROJECT_DIR_OPTION, GRADLE_PROJECT_DIR_DETAILED_OPTION).hasArgument();

        SystemPropertiesCommandLineConverter converter = new SystemPropertiesCommandLineConverter();
        converter.configure(parser);

        ParsedCommandLine options = parser.parse(args);

        Properties properties = wrapperProperties(options);

        Properties systemProperties = System.getProperties();
        systemProperties.putAll(converter.convert(options, new HashMap<String, String>()));

        File gradleUserHome = gradleUserHome(options);

        File downloadedDistributionsDir = downloadedDistributionsDir(options);

        Logger logger = logger(options);

        boolean hasDistributionUrl = properties.containsKey(WrapperExecutor.DISTRIBUTION_URL_PROPERTY);
        WrapperConfiguration configuration = ConfigurationLoader.load(properties);
        Install install = new Install(logger, new LocalDownload(logger, downloadedDistributionsDir), new PathAssembler(gradleUserHome));
        if (!hasDistributionUrl) {
            final Pattern pattern = matchPattern(options);
            String[] filenames = downloadedDistributionsDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return pattern.matcher(name).matches();
                }
            });
            if (filenames == null || filenames.length == 0) {
                throw new RuntimeException("No gradle distribution zip file found in " + downloadedDistributionsDir);
            }
            for (String filename : filenames) {
                configuration.setDistribution(URI.create(String.format("https://services.gradle.org/distributions/%s", filename)));
                File gradleHome = install.createDist(configuration);
                logger.log(String.format("Gradle distribution '%s' is installed at '%s'", filename, gradleHome));
            }
        } else if (hasDistributionUrl) {
            File gradleHome = install.createDist(configuration);
            logger.log(String.format("Gradle distribution '%s' is installed at '%s'", configuration.getDistribution(), gradleHome));
        } else {
            throw new RuntimeException("Neither distributionUrl property nor --distributions-dir option is specified!");
        }
    }

    private static Pattern matchPattern(ParsedCommandLine options) {
        Pattern pattern;
        if (options.hasOption(GRADLE_VERSION_OPTION) && options.option(GRADLE_VERSION_OPTION).hasValue()) {
            pattern = Pattern.compile("^gradle.*" + options.option(GRADLE_VERSION_OPTION).getValue().replace(".", "\\.") + ".*\\.zip$");
        } else {
            pattern = Pattern.compile("^gradle.*\\.zip$");
        }
        return pattern;
    }

    private static File downloadedDistributionsDir(ParsedCommandLine options) {
        String downloadedDirString;
        if (options.hasOption(GRADLE_DOWNLOADED_ZIPS_DIR_OPTION)) {
            downloadedDirString = options.option(GRADLE_DOWNLOADED_ZIPS_DIR_OPTION).getValue();
        } else {
            downloadedDirString = "./distributions";
        }
        File downloadedDir = new File(downloadedDirString);
        if (!downloadedDir.exists() || !downloadedDir.isDirectory()) {
            throw new RuntimeException("Bad path for downloaded distribution zips: " + downloadedDirString);
        }
        return downloadedDir;
    }

    private static Properties wrapperProperties(ParsedCommandLine options) {
        if (options.hasOption(GRADLE_PROJECT_DIR_OPTION)) {
            String projectDirOption = options.option(GRADLE_PROJECT_DIR_OPTION).getValue();
            File wrapperPropertiesFile = new File(projectDirOption, "gradle/wrapper/gradle-wrapper.properties");
            return PropertiesLoader.fromFile(wrapperPropertiesFile);
        } else {
            return PropertiesLoader.fromClasspath("gradle-wrapper-default.properties");
        }
    }

    private static File gradleUserHome(ParsedCommandLine options) {
        if (options.hasOption(GRADLE_USER_HOME_OPTION)) {
            return new File(options.option(GRADLE_USER_HOME_OPTION).getValue());
        }
        return GradleUserHomeLookup.gradleUserHome();
    }

    private static Logger logger(ParsedCommandLine options) {
        return new Logger(options.hasOption(GRADLE_QUIET_OPTION));
    }
}
