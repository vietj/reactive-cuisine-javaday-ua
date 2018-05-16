package reactivekitchen.bank.gateway;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import reactivekitchen.bank.gateway.helpers.Services;


public class GatewayVerticle extends AbstractVerticle {

  private WebClient service;
  private WebClient debt;
  private WebClient balance;

  @Override
  public void start() throws Exception {

    service = Services.getCustomerService(vertx);
    debt = Services.getDebtService(vertx);
    balance = Services.getBalanceService(vertx);

    Router router = Router.router(vertx);
    router.get("/").handler(this::compute);
    // TODO GET / -> compute

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }

  private void compute(RoutingContext rc) {

    Single<JsonObject> s = service.get("/customers/clement")
        .rxSend()
        .map(HttpResponse::bodyAsJsonObject).flatMap(json -> {
          String account = json.getString("account");

          Single<Double> s1 = balance.get("/balance/" + account)
              .rxSend()
              .map(HttpResponse::bodyAsJsonObject)
              .map(j -> j.getDouble("balance"));

          Single<Double> s2 = debt.get("/debt/" + account)
              .rxSend()
              .map(HttpResponse::bodyAsJsonObject)
              .map(j -> j.getDouble("level"));


          return Single.zip(s1, s2, (balance, debt) -> new JsonObject()
              .put("account", account)
              .put("balance", balance)
              .put("debt", debt));
        });
    s.subscribe(json -> {
      rc.response().end(json.encodePrettily());
    }, err -> {
      err.printStackTrace();
      rc.response().end("ERROR " + err.getMessage());
    });

    // TODO Get customer account ("/customers/clement") -> account
    // TODO Get balance /balance/$account -> balance
    // TODO Get debt /debt/$account -> level


  }
}
