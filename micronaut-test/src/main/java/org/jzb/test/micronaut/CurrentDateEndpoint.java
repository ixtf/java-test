package org.jzb.test.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.management.endpoint.annotation.Selector;
import io.micronaut.management.endpoint.annotation.Write;

import java.util.Date;

/**
 * @author jzb 2020-02-14
 */
@Endpoint(id = "date",
        prefix = "custom",
        defaultEnabled = true,
        defaultSensitive = false)
public class CurrentDateEndpoint {
    private Date currentDate;

    @Read(produces = MediaType.TEXT_PLAIN)
    public String currentDatePrefix(@Selector String prefix) {
        return prefix + ": " + currentDate;
    }

    @Write
    public String reset() {
        currentDate = new Date();
        return "Current date reset";
    }
}
