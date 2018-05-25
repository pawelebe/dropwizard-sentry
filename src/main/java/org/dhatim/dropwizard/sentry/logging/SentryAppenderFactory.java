package org.dhatim.dropwizard.sentry.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import static com.google.common.base.Preconditions.checkNotNull;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.sentry.DefaultSentryClientFactory;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.logback.SentryAppender;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.dhatim.dropwizard.sentry.filters.DroppingSentryLoggingFilter;

@JsonTypeName("sentry")
public class SentryAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    private static final String APPENDER_NAME = "dropwizard-sentry";

    @NotNull
    @JsonProperty
    private String dsn = null;

    @JsonProperty
    private Optional<String> environment = Optional.empty();

    @JsonProperty
    private Optional<Map<String, String>> tags = Optional.empty();

    @JsonProperty
    private Optional<Set<String>> mdcTags = Optional.empty();

    @JsonProperty
    private Optional<String> sentryClientFactory = Optional.empty();

    @JsonProperty
    private Optional<String> release = Optional.empty();

    @JsonProperty
    private Optional<String> serverName = Optional.empty();

    @JsonProperty
    private Optional<Map<String, Object>> extra = Optional.empty();

    public String getDsn() {
        return dsn;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public Optional<String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Optional<String> environment) {
        this.environment = environment;
    }

    public Optional<Map<String, String>> getTags() {
        return tags;
    }

    public void setTags(Optional<Map<String, String>> tags) {
        this.tags = tags;
    }

    public Optional<Set<String>> getMdcTags() {
        return mdcTags;
    }

    public void setMdcTags(Optional<Set<String>> mdcTags) {
        this.mdcTags = mdcTags;
    }

    public Optional<String> getSentryClientFactory() {
        return sentryClientFactory;
    }

    public void setSentryClientFactory(Optional<String> sentryClientFactory) {
        this.sentryClientFactory = sentryClientFactory;
    }

    public Optional<String> getRelease() {
        return release;
    }

    public void setRelease(Optional<String> release) {
        this.release = release;
    }

    public Optional<String> getServerName() {
        return serverName;
    }

    public void setServerName(Optional<String> serverName) {
        this.serverName = serverName;
    }

    public Optional<Map<String, Object>> getExtra() {
        return extra;
    }

    public void setExtra(Optional<Map<String, Object>> extra) {
        this.extra = extra;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context,
            String applicationName,
            LayoutFactory<ILoggingEvent> layoutFactory,
            LevelFilterFactory<ILoggingEvent> levelFilterFactory,
            AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        checkNotNull(context);

        SentryClientFactory factory;
        try {
            String factoryClassName = sentryClientFactory.orElse(DefaultSentryClientFactory.class.getCanonicalName());
            Class<? extends SentryClientFactory> factoryClass = Class.forName(factoryClassName).asSubclass(SentryClientFactory.class);
            factory = factoryClass.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        SentryClient sentryClient = SentryClientFactory.sentryClient(dsn, factory);

        final SentryAppender appender = new SentryAppender();
        appender.setName(APPENDER_NAME);
        appender.setContext(context);

        environment.ifPresent(sentryClient::setEnvironment);
        tags.ifPresent(sentryClient::setTags);
        mdcTags.ifPresent(sentryClient::setMdcTags);
        release.ifPresent(sentryClient::setRelease);
        serverName.ifPresent(sentryClient::setServerName);
        extra.ifPresent(sentryClient::setExtra);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().stream().forEach(f -> appender.addFilter(f.build()));
        appender.start();

        final Appender<ILoggingEvent> asyncAppender = wrapAsync(appender, asyncAppenderFactory, context);
        addDroppingRavenLoggingFilter(asyncAppender);

        return asyncAppender;
    }

    private void addDroppingRavenLoggingFilter(Appender<ILoggingEvent> appender) {
        final Filter<ILoggingEvent> filter = new DroppingSentryLoggingFilter();
        filter.start();
        appender.addFilter(filter);
    }

}
