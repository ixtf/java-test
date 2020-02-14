package org.jzb.test.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.validation.Validated;
import io.reactivex.Single;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

/**
 * @author jzb 2020-02-14
 */
@Controller("/")
@Validated
public class HelloController {
    @Inject
    private HelloService helloService;

    @Get(uri = "/hello/{name}", produces = MediaType.TEXT_PLAIN)
    public Single<String> hello(@NotBlank String name) {
        return helloService.hello(name);
    }
}
