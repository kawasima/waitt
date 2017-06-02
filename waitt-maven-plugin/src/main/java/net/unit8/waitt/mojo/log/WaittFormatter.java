package net.unit8.waitt.mojo.log;

import org.codehaus.plexus.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;


/**
 * @author kawasima
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class WaittFormatter extends Formatter {
    final TargetLengthBasedClassNameAbbreviator abbreviator = new TargetLengthBasedClassNameAbbreviator(35);

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(256);
        sb.append(formatDate(record.getMillis()))
                .append(" ")
                .append(formatLevel(record.getLevel()))
                .append(" ")
                .append(formatLoggerName(record.getLoggerName()))
                .append(": ")
                .append(record.getMessage())
                .append('\n');

        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        }

        return sb.toString();
    }

    protected String formatLoggerName(String loggerName) {
        return ansi().fg(GREEN)
                .a(StringUtils.leftPad(abbreviator.abbreviate(loggerName), 35, " "))
                .reset().toString();
    }

    protected String formatLevel(Level level) {
        if (level == Level.SEVERE) {
            return ansi().a('[').fgBright(RED).a("ERROR").reset().a(']').toString();
        } else if (level == Level.WARNING) {
            return ansi().a('[').fgBright(YELLOW).a("WARN ").reset().a(']').toString();
        } else if (level == Level.INFO) {
            return ansi().a('[').fgBright(CYAN).a("INFO ").reset().a(']').toString();
        } else {
            return ansi().a('[').fgBright(WHITE).a("DEBUG").reset().a(']').toString();
        }
    }

    protected String formatDate(long millis) {
        StringBuilder sb = new StringBuilder(25);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        return sb.append(cal.get(Calendar.YEAR))
                .append("-")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.MONTH) + 1), 2, "0"))
                .append("-")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2, "0"))
                .append(" ")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)), 2, "0"))
                .append(":")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.MINUTE)), 2, "0"))
                .append(":")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.SECOND)), 2, "0"))
                .append(".")
                .append(StringUtils.leftPad(Integer.toString(cal.get(Calendar.MILLISECOND)), 3, "0"))
                .toString();
    }
}
