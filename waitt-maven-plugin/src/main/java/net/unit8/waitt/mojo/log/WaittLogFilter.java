package net.unit8.waitt.mojo.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author kawasima
 */
public class WaittLogFilter implements Filter {
    private final List<LoggerMatcher> matchers;

    WaittLogFilter() {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream("waitt-loglevel.properties");
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignore) {

        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ignore2) {}
        }

        matchers = new ArrayList<LoggerMatcher>();
        for(String key : props.stringPropertyNames()) {
            String level = props.getProperty(key);
            matchers.add(new LoggerMatcher(key, level));
        }

        Collections.sort(matchers, new Comparator<LoggerMatcher>() {
            @Override
            public int compare(LoggerMatcher o1, LoggerMatcher o2) {
                return o2.getDepth() - o1.getDepth();
            }
        });
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        for (LoggerMatcher lm : matchers) {
            if (lm.match(record)) {
                return false;
            }
        }
        return true;
    }

    private static class LoggerMatcher {
        private String loggerPrefix;
        private Level level;
        private int depth;

        LoggerMatcher(String loggerPrefix, String levelName) {
            this.loggerPrefix = loggerPrefix;
            this.level = Level.parse(levelName);
            this.depth = 0;

            for (int i=0; i <  loggerPrefix.length(); i++) {
                if (loggerPrefix.charAt(i) == '.') {
                    depth++;
                }
            }
        }

        boolean match(LogRecord rec) {
            String loggerName = rec.getLoggerName();

            return loggerName != null
                    && loggerName.startsWith(loggerPrefix)
                    && rec.getLevel().intValue() <= level.intValue();
        }

        int getDepth() {
            return this.depth;
        }
    }
}
