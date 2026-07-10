package net.unit8.waitt.api.dto;

import net.unit8.waitt.api.ServerStatus;

/**
 * @author kawasima
 */
public class ServerMetadata {
    private String name;
    private ServerStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ServerStatus getStatus() { return status; }
    public void setStatus(ServerStatus status) { this.status = status; }
}
