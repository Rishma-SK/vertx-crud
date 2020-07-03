package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private HashMap<String, String> services = new HashMap<>();
    private DBConnector connector;
    private BackgroundPoller poller;
    private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        poller = new BackgroundPoller(vertx);
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        //services.put("https://www.kry.se", "UNKNOWN");
        connector.fetchAllServices().setHandler(result -> {
            logger.info("Fetch all service endpoints from DB");
            if (result.succeeded()) {
                vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(connector.getServices()));
                setRoutes(router);
                vertx
                        .createHttpServer()
                        .requestHandler(router)
                        .listen(8066, rs -> {
                            if (rs.succeeded()) {
                                logger.info("KRY code test service started");
                                startFuture.complete();
                            } else {
                                startFuture.fail(result.cause());
                            }
                        });
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void setRoutes(Router router) {

        setDefaultRoute(router);
        setDisplayListofServices(router);
        setAddServiceRoute(router);
        setDeleteServiceRoute(router);
    }

    private void setDefaultRoute(Router router) {
        router.route("/*").handler(StaticHandler.create());
    }

    private void setDisplayListofServices(Router router) {
        router.get("/service").handler(req -> {
            List<JsonObject> jsonServices = connector.getServices();
            logger.info("Display List:::"+jsonServices);
            req.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonArray(jsonServices).encode());
        });
    }

    private void setAddServiceRoute(Router router) {

        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            logger.info("In SetPostService::" + jsonBody);
            // services.put(jsonBody.getString("url"), "UNKNOWN");
            JsonObject service;
            try {
              String url = jsonBody.getString("url");
              String name = jsonBody.getString("name");
              if(name== null || name ==""){
                  name = url;
              }
                service = new JsonObject()
                        .put("url", new URL(url).toString())
                        .put("name", name)
                        .put("created_date", Instant.now())
                        .put("status", "UNKNOWN");
                req.response()
                        .putHeader("content-type", "text/plain")
                        .setStatusCode(200)
                        .end("OK");
                logger.info("JsonObject formed to insert::" + service);
            } catch (MalformedURLException e) {
                req.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/plain")
                        .end("Invalid url: " + jsonBody.getString("url"));
                return;
            }
            connector.insert(service).setHandler(asyncResult -> {
                if (asyncResult.succeeded()) {
                    logger.info("Post successful");

                    req.response()
                            .putHeader("content-type", "text/plain")
                            .setStatusCode(200)
                            .end("OK");
                } else {
                    logger.error("Post failed", asyncResult.cause());
                    req.response()
                            .setStatusCode(500)
                            .putHeader("content-type", "text/plain")
                            .end("Internal error");
                }
            });
        });
    }

    private void setDeleteServiceRoute(Router router) {
        router.delete("/service/:id").handler(ctx -> {
            try {
                String id = ctx.request().getParam("id");
                String service = new String(Base64.getDecoder().decode(id));

                logger.info("In setDeleteService::" + id);
                logger.info("Deleting: " + service);

                connector.remove(service).setHandler(asyncResult -> {
                    if (asyncResult.succeeded()) {
                        logger.info("Delete successful here");
                        ctx.response()
                                .putHeader("content-type", "text/plain")
                                .end("OK");

                    } else {
                        logger.error("Failed to delete", asyncResult.cause());
                        ctx.response()
                                .setStatusCode(500)
                                .putHeader("content-type", "text/plain")
                                .end("Internal error");
                    }
                });
            } catch (UnsupportedEncodingException e) {
                ctx.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/plain")
                        .end("Invalid parameter: " + ctx.pathParam("service"));
            }
        });
    }

}



