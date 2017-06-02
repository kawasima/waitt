package net.unit8.waitt.feature.tracer.entry;

import lombok.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author kawasima
 */
public class ResponseEntry extends EntryBase {
    @NonNull
    private String requestUri;

    @NonNull
    private int statusCode;

    @NonNull
    private String responseBody;

    private String hostName;

    @java.beans.ConstructorProperties({"requestUri", "statusCode", "responseBody"})
    public ResponseEntry(String requestUri, int statusCode, String responseBody) {
        super();
        this.requestUri = requestUri;
        this.statusCode = statusCode;
        this.responseBody = responseBody;

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "UNKNOWN";
        }

    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    protected boolean canEqual(Object other) {
        return other instanceof ResponseEntry;
    }

    @NonNull
    public String getRequestUri() {
        return this.requestUri;
    }

    @NonNull
    public int getStatusCode() {
        return this.statusCode;
    }

    @NonNull
    public String getResponseBody() {
        return this.responseBody;
    }

    public void setRequestUri(@NonNull String requestUri) {
        this.requestUri = requestUri;
    }

    public void setStatusCode(@NonNull int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponseBody(@NonNull String responseBody) {
        this.responseBody = responseBody;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ResponseEntry)) return false;
        final ResponseEntry other = (ResponseEntry) o;
        if (!other.canEqual(this)) return false;
        final Object this$requestUri = this.requestUri;
        final Object other$requestUri = other.requestUri;
        if (this$requestUri == null ? other$requestUri != null : !this$requestUri.equals(other$requestUri))
            return false;
        if (this.statusCode != other.statusCode) return false;
        final Object this$responseBody = this.responseBody;
        final Object other$responseBody = other.responseBody;
        if (this$responseBody == null ? other$responseBody != null : !this$responseBody.equals(other$responseBody))
            return false;
        final Object this$hostName = this.getHostName();
        final Object other$hostName = other.getHostName();
        return this$hostName == null ? other$hostName == null : this$hostName.equals(other$hostName);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $requestUri = this.requestUri;
        result = result * PRIME + ($requestUri == null ? 0 : $requestUri.hashCode());
        result = result * PRIME + this.statusCode;
        final Object $responseBody = this.responseBody;
        result = result * PRIME + ($responseBody == null ? 0 : $responseBody.hashCode());
        final Object $hostName = this.getHostName();
        result = result * PRIME + ($hostName == null ? 0 : $hostName.hashCode());
        return result;
    }

    public String toString() {
        return "net.unit8.waitt.feature.tracer.entry.ResponseEntry(requestUri=" + this.requestUri + ", statusCode=" + this.statusCode + ", responseBody=" + this.responseBody + ", hostName=" + this.getHostName() + ")";
    }
}
