package net.unit8.waitt.feature.tracer.entry;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kawasima
 */
public class EntryBase implements Serializable {
    private Date datetime;

    protected EntryBase() {
        setDatetime(new Date());
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
