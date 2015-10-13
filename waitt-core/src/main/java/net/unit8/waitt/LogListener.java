package net.unit8.waitt;

/**
 * @author kawasima
 */
public interface LogListener {
    void info(CharSequence message, Throwable t);
    void debug(CharSequence message, Throwable t);
    void warn(CharSequence message, Throwable t);
    void error(CharSequence message, Throwable t);
}
