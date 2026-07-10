package net.unit8.waitt.api.observability;

/**
 * One JDBC statement execution captured for a request. Recorded directly into the
 * shared {@link TraceStore} by a DataSource/Connection proxy running in the webapp
 * realm — a plain {@code waitt-api} call, no reflection.
 *
 * @author kawasima
 */
public final class SqlEvent {
    private final long startEpochMillis;
    private final long durationMillis;
    private final String sql;
    private final int rowCount;
    private final boolean success;
    private final String error;

    public SqlEvent(long startEpochMillis, long durationMillis, String sql,
                    int rowCount, boolean success, String error) {
        this.startEpochMillis = startEpochMillis;
        this.durationMillis = durationMillis;
        this.sql = sql;
        this.rowCount = rowCount;
        this.success = success;
        this.error = error;
    }

    public long getStartEpochMillis() { return startEpochMillis; }
    public long getDurationMillis() { return durationMillis; }
    public String getSql() { return sql; }
    /** Affected/returned rows, or {@code -1} when unknown. */
    public int getRowCount() { return rowCount; }
    public boolean isSuccess() { return success; }
    public String getError() { return error; }
}
