package org.jzb.test.rapidoid;

import org.rapidoid.commons.Dates;
import org.rapidoid.http.Req;
import org.rapidoid.job.Jobs;
import org.rapidoid.setup.App;
import org.rapidoid.setup.My;
import org.rapidoid.setup.On;
import org.rapidoid.u.U;
import org.rapidoid.util.Tokens;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author jzb 2019-12-14
 */
public class Main {
    public static void main(String[] args) {
        App.bootstrap(args);
        My.rolesProvider((req, username) -> username.equals("bob") ? U.set("manager") : U.set());

        On.defaults().wrappers((req, next) -> {
//            req.async();
//            req.done();
//            return next.invoke();
            return next.invokeAndTransformResult(result -> "Hey: " + result);
        });

        On.get("/hey").roles("manager").json(() -> U.map("msg", "ok"));
        System.out.println(On.config());
        On.get("/block").json((req, resp) -> {
//            req.async();
//            Mono.fromCallable(() -> "mono")
//                    .map(String::getBytes)
//                    .doOnSuccess(resp::chunk)
//                    .subscribe(it -> {
//                        req.done();
//                        resp.done();
//                    });
//            return resp;


            req.async(); // mark asynchronous request processing
            // send part 1
            resp.chunk("part 1".getBytes());
            // after some time, send part 2 and finish
            Jobs.after(100).milliseconds(() -> {
                resp.chunk(" & part 2".getBytes());
                resp.done();
            });
            return resp;
        });
        On.get("/test").json((Req req) -> {
            throw U.rte("test");
        });

        // generate a token
        String token = Tokens.serialize(U.map("_user", "bob"));
        System.out.println(token);


        DateFormat df = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        df.setTimeZone(Dates.UTC);
        System.out.println(df.format(new Date()));
        df.setTimeZone(Dates.GMT);
        System.out.println(df.format(new Date()));

//        DslJson<Object> dslJson = new DslJson<>(Settings.basicSetup());
        // demo request, prints {"msg":"ok"}
//        Self.get("/hey?_token=" + token).print();
    }
}
