package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.Logger;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import java.util.Optional;
import java.util.Set;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;
import org.slf4j.LoggerFactory;

/**
 * A class adding a configured
 * {@link io.sentry.logback.SentryAppender} to the root logger.
 */
public final class SentryBootstrap {

    private SentryBootstrap() {
        /* No instance methods */ }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     */
    public static void bootstrap(final String dsn) {
        bootstrap(dsn, true);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     * @param cleanRootLogger If true, detach and stop all other appenders from
     * the root logger
     */
    public static void bootstrap(final String dsn, boolean cleanRootLogger) {
        bootstrap(dsn, Optional.empty(), cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     * @param mdcTags Tag names to be extracted from logging MDC
     * @param cleanRootLogger If true, detach and stop all other appenders from
     * the root logger
     */
    public static void bootstrap(final String dsn, Optional<Set<String>> mdcTags, boolean cleanRootLogger) {
        bootstrap(dsn, mdcTags, Optional.empty(), Optional.empty(), cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     * @param mdcTags Tag names to be extracted from logging MDC
     * @param environment The environment name to pass to Sentry
     * @param release The release name to pass to Sentry
     * @param cleanRootLogger If true, detach and stop all other appenders from
     * the root logger
     */
    public static void bootstrap(final String dsn, Optional<Set<String>> mdcTags,
            Optional<String> environment, Optional<String> release, boolean cleanRootLogger) {
        bootstrap(dsn, mdcTags, environment, release, Optional.empty(), cleanRootLogger);
    }

    /**
     * Bootstrap the SLF4J root logger with a configured
     * {@link io.sentry.logback.SentryAppender}.
     *
     * @param dsn The DSN (Data Source Name) for your project
     * @param mdcTags Tag names to be extracted from logging MDC
     * @param environment The environment name to pass to Sentry
     * @param release The release name to pass to Sentry
     * @param serverName The server name to pass to Sentry
     * @param cleanRootLogger If true, detach and stop all other appenders from
     * the root logger
     */
    public static void bootstrap(final String dsn, Optional<Set<String>> mdcTags,
            Optional<String> environment, Optional<String> release, Optional<String> serverName,
            boolean cleanRootLogger) {
        final SentryAppenderFactory factory = new SentryAppenderFactory();
        factory.setDsn(dsn);
        factory.setMdcTags(mdcTags);
        factory.setEnvironment(environment);
        factory.setRelease(release);
        factory.setServerName(serverName);

        registerAppender(dsn, cleanRootLogger, factory);
    }

    private static void registerAppender(String dsn, boolean cleanRootLogger,
            SentryAppenderFactory factory) {
        final Logger root = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

        if (cleanRootLogger) {
            root.detachAndStopAllAppenders();
        }

        final ThresholdLevelFilterFactory levelFilterFactory = new ThresholdLevelFilterFactory();
        final DropwizardLayoutFactory layoutFactory = new DropwizardLayoutFactory();
        final AsyncLoggingEventAppenderFactory asyncAppenderFactory
                = new AsyncLoggingEventAppenderFactory();
        root.addAppender(factory.build(root.getLoggerContext(), dsn, layoutFactory, levelFilterFactory,
                asyncAppenderFactory));
    }
}
