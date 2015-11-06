package net.unit8.waitt.feature.tracer.entry;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * @author kawasima
 */
@Data
@RequiredArgsConstructor
public class ResponseEntry implements Serializable {
    private Date requestedAt = new Date();

    private String hostName;

    @NonNull
    private String requestUri;

    @NonNull
    private String responseBody;

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNKNOWN";
        }
    }
}
