package org.jzb.test.micronaut;

import io.reactivex.Single;

import javax.inject.Singleton;

/**
 * @author jzb 2020-02-14
 */
@Singleton
public class HelloService {
    public Single<String> hello(String name) {
        return Single.just("Hello " + name + "!");
    }
}
