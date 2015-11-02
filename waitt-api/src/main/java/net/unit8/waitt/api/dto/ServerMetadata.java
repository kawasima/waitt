package net.unit8.waitt.api.dto;

import lombok.Data;
import net.unit8.waitt.api.ServerStatus;

/**
 * @author kawasima
 */
@Data
public class ServerMetadata {
    private String name;
    private ServerStatus status;
}
