package reactivekitchen.bank.gateway

import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import reactivekitchen.bank.gateway.helpers.Services

class GatewayRevisitVerticle : AbstractVerticle() {

  lateinit var service: WebClient
  lateinit var debt: WebClient
  lateinit var balance: WebClient

  override fun start() {

    service = Services.getCustomerService(vertx)
    debt = Services.getDebtService(vertx)
    balance = Services.getBalanceService(vertx)

    // TODO GET / -> compute
    val router = Router.router(vertx)

    router.get("/").handler({ rc ->
      launch(vertx.dispatcher()) {
        compute(rc)
      }
    })

    vertx.createHttpServer().requestHandler({
      req -> router.handle(req)
    }).listen(8080)

  }

  private suspend fun compute(rc: io.vertx.ext.web.RoutingContext) {

    // TODO Get customer account ("/customers/clement") -> account
    // TODO Get balance /balance/$account -> balance
    // TODO Get debt /debt/$account -> level

    val resp = awaitResult<HttpResponse<Buffer>> { service.get("/customers/clement").send(it) }

    val account = resp.bodyAsJsonObject().getString("account");

    val resp1 = awaitResult<HttpResponse<Buffer>> { balance.get("/balance/$account").send(it) }
    val resp2 = awaitResult<HttpResponse<Buffer>> { debt.get("/debt/$account").send(it) }

    val debt = resp2.bodyAsJsonObject().getDouble("level")
    val balance = resp1.bodyAsJsonObject().getDouble("balance")

    rc.response().end(JsonObject()
        .put("account", account)
        .put("debt", debt)
        .put("balance", balance)
        .encodePrettily())
  }
}