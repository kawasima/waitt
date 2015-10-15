package net.unit8.waitt.mojo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.maven.plugin.logging.Log;
import org.fusesource.jansi.Ansi;

/**
 *
 * @author kawasima
 */
public class WaittLogger implements Log {
    private final Log originalLog;

    public WaittLogger(Log originalLog) {
        this.originalLog = originalLog;
    }
    /**
     * @see org.apache.maven.plugin.logging.Log#debug(java.lang.CharSequence)
     */
    @Override
    public void debug(CharSequence content) {
        if (isDebugEnabled())
            print("DEBUG", content);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#debug(java.lang.CharSequence,
     * java.lang.Throwable)
     */
    @Override
    public void debug(CharSequence content, Throwable error) {
        if (isDebugEnabled())
            print("DEBUG", content, error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#debug(java.lang.Throwable)
     */
    @Override
    public void debug(Throwable error) {
        if (isDebugEnabled())
            print("DEBUG", error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#info(java.lang.CharSequence)
     */
    @Override
    public void info(CharSequence content) {
        if (isInfoEnabled())
            print("INFO", content);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#info(java.lang.CharSequence,
     * java.lang.Throwable)
     */
    @Override
    public void info(CharSequence content, Throwable error) {
        if (isInfoEnabled())
            print("INFO", content, error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#info(java.lang.Throwable)
     */
    @Override
    public void info(Throwable error) {
        if (isInfoEnabled())
            print("INFO", error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#warn(java.lang.CharSequence)
     */
    @Override
    public void warn(CharSequence content) {
        if (isWarnEnabled())
            print("WARN", content);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#warn(java.lang.CharSequence,
     * java.lang.Throwable)
     */
    @Override
    public void warn(CharSequence content, Throwable error) {
        if (isWarnEnabled())
            print("WARN", content, error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#warn(java.lang.Throwable)
     */
    @Override
    public void warn(Throwable error) {
        if (isWarnEnabled())
            print("WARN", error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#error(java.lang.CharSequence)
     */
    @Override
    public void error(CharSequence content) {
        if (isErrorEnabled())
            print("ERROR", content);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#error(java.lang.CharSequence,
     * java.lang.Throwable)
     */
    @Override
    public void error(CharSequence content, Throwable error) {
        if (isErrorEnabled())
            print("ERROR", content, error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable error) {
        if (isErrorEnabled())
            print("ERROR", error);
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return originalLog.isDebugEnabled();
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return originalLog.isInfoEnabled();
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return originalLog.isWarnEnabled();
    }

    /**
     * @see org.apache.maven.plugin.logging.Log#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return originalLog.isErrorEnabled();
    }

    private void printLevel(String level) {
        System.out.print(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
        System.out.print(" [");
        Ansi.Color color = Ansi.Color.DEFAULT;
        
        if ("INFO".equals(level)) {
            color = Ansi.Color.CYAN;
        } else if ("WARN".equals(level)) {
            color = Ansi.Color.MAGENTA;
        } else if ("ERROR".equals(level)) {
            color = Ansi.Color.RED;
        }
        System.out.print(Ansi.ansi().bold().fgBright(color).a(level).reset().toString());
        System.out.print("] ");
    }
    private void print(String prefix, CharSequence content) {
        printLevel(prefix);
        System.out.println(content.toString());
    }

    private void print(String prefix, Throwable error) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);

        error.printStackTrace(pWriter);

        printLevel(prefix);
        System.out.println(sWriter.toString());
    }

    private void print(String prefix, CharSequence content, Throwable error) {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);

        error.printStackTrace(pWriter);

        printLevel(prefix);
        System.out.println(content.toString() + "\n\n" + sWriter.toString());
    }
}
