package io.opentracing.demo.rest;

import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class RestHandler {

    private Random random = new Random();
    private final Tracer tracer;

    public RestHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @GET
    @Path("/user/{userName}")
    public Response getEmail(@PathParam("userName") String userName, @Context UriInfo uriInfo) {
        Span span = tracer.buildSpan("get_user_email").withTag("tag", "hello").start();
        String user = getEmailFromDB(span.context(), userName);
        span.finish();
        return Response.ok().entity(user).build();
    }

    private String getEmailFromDB(SpanContext parent, String userName) {
        if (parent != null) {
            waitRandom();

            Span dbProcessingSpan = tracer.buildSpan("get_user_from_db")
                    .asChildOf(parent)
                    .start();

            /**
             * Business logic
             *
             * ....
             */

            long waitTime = waitRandom();
            //Adding wait time as a tag
            dbProcessingSpan.setTag("waitTime", waitTime);

            dbProcessingSpan.finish();
        }
        return userName.replaceAll(" ", "_") + "@redhat.com";
    }

    private long waitRandom() {
        long waitTime = (int) (random.nextDouble() * 1000);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return waitTime;
    }
}
