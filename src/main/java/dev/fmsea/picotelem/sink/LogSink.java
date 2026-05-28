package dev.fmsea.picotelem.sink;

import dev.fmsea.picotelem.model.TEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSink implements EventSink {

    private static final Logger LOG = LoggerFactory.getLogger(LogSink.class);

    public void process(TEvent event) {
        LOG.info("{}: {}", event.getEventName(), event.toRepr());
    }

    public void flush() {
        // noop since the Logger will handle this...
    }
}
