package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import oracle.jrockit.jfr.VMJFR;
import se.kry.codetest.DBConnector;



public class DBMigration {

  private static Logger logger = LoggerFactory.getLogger(DBMigration.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
    connector.query("CREATE TABLE IF NOT EXISTS service (url VARCHAR(128) PRIMARY KEY NOT NULL, name VARCHAR(32) NOT NULL, created_date DATETIME NOT NULL)").setHandler(done -> {
      if(done.succeeded()){
        logger.info("completed db migrations");
      } else {
        done.cause().printStackTrace();
        logger.error("DB migration failed");
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
