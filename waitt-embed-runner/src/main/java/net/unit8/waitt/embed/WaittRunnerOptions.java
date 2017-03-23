package net.unit8.waitt.embed;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author kawasima
 */
public class WaittRunnerOptions extends Options {
    public WaittRunnerOptions() {
        addOption(Option.builder("p")
                .desc("Listen port")
                .longOpt("port")
                .hasArg()
                .type(Short.TYPE)
                .build());

        addOption(Option.builder()
                .desc("Context path")
                .longOpt("prefix")
                .hasArg()
                .build());

        addOption(Option.builder("d")
                .desc("Web application directory")
                .longOpt("appdir")
                .hasArg()
                .build());

    }
}
