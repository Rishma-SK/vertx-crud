package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBConnector {

  private final String DB_PATH = "poller.db";
  private final SQLClient client;
  private final HashMap<String, JsonObject> services = new HashMap<>();
  private Logger logger = LoggerFactory.getLogger(DBConnector.class);

  public DBConnector(Vertx vertx){
    JsonObject config = new JsonObject()
        .put("url", "jdbc:sqlite:" + DB_PATH)
        .put("driver_class", "org.sqlite.JDBC")
        .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }

  public Future<ResultSet> query(String query, JsonArray params) {
    if(query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if(!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    client.queryWithParams(query, params, result -> {
      if(result.failed()){
        queryResultFuture.fail(result.cause());
      } else {
        queryResultFuture.complete(result.result());
      }
    });
    return queryResultFuture;
  }

  public Future<Boolean> fetchAllServices() {
    Future<Boolean> statusFuture = Future.future();
    this.query("SELECT * FROM service;").setHandler(asyncResult -> {

      if (asyncResult.succeeded()) {
        asyncResult.result().getRows().forEach(row -> services.put(row.getString("url"), row.put("status", "UNKNOWN")));
        logger.info("List of services from DB:" + services.keySet());
        statusFuture.complete(true);
      } else {
        logger.error("DB connection issue", asyncResult.cause());
        statusFuture.fail(asyncResult.cause());
      }
    });
    return statusFuture;
  }

  public List<JsonObject> getServices() {
    return new ArrayList<>(services.values());
  }

  public Future<ResultSet> insert(JsonObject service) {
    logger.info("Insert this data :"+service+" int DB and list::"+services);
    services.put(service.getString("url"), service);
    logger.info("The list of services to be displayed::"+services);
    Future<ResultSet> resultSet = this.query("INSERT INTO service (url, name, created_date) values (?,?,?)",
            new JsonArray()
                    .add(service.getString("url"))
                    .add(service.getString("name"))
                    .add(service.getString("created_date"))
    );
    resultSet.setHandler(asyncResult -> {
      if (asyncResult.succeeded()) {
        logger.info("Insertion Success",asyncResult.result());
      } else {
        logger.error("Insertion failed", asyncResult.cause());
      }
    });

    return resultSet;
  }

  public Future<ResultSet> remove(String service) throws UnsupportedEncodingException {
    services.remove(service);
    return this.query("DELETE FROM service WHERE url=?", new JsonArray().add(service));
  }

}
