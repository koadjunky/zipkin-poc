package eu.malycha.zipkin.poc.quarkus.service.fixapi;

import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/fixapi")
public class FixApiController {

    @Inject
    FixApiHandler fixApiHandler;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/order/new")
    public String send() throws Exception {
        fixApiHandler.handle();
        return "Order sent: " + UUID.randomUUID();
    }
}
