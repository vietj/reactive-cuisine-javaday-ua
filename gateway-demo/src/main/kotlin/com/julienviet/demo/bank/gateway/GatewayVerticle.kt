package com.julienviet.demo.bank.gateway

import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import me.escoffier.demo.bank.gateway.helpers.Services

class GatewayVerticle : AbstractVerticle() {

  lateinit var service: WebClient
  lateinit var debt: WebClient
  lateinit var balance: WebClient

  override fun start() {

    service = Services.getCustomerService(vertx)
    debt = Services.getDebtService(vertx)
    balance = Services.getBalanceService(vertx)

    val router = Router.router(vertx)
    // TODO GET / -> compute
    router.get("/").handler({
      launch(vertx.dispatcher()) {
        try {
          compute(it)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    })

    vertx.createHttpServer()
        .requestHandler { router.accept(it) }
        .listen(8080)
  }

  private suspend fun compute(rc: io.vertx.ext.web.RoutingContext) {

    val accountResp = awaitResult<HttpResponse<Buffer>> {
      service
          .get("/customers/clement")
          .send(it)
    }

    val account = accountResp.bodyAsJsonObject().getString("account")

    val debtResp = awaitResult<HttpResponse<Buffer>> { debt.get("/debt/" + account).send(it) }
    val balanceResp = awaitResult<HttpResponse<Buffer>> { balance.get("/balance/" + account).send(it) }

    val debt = debtResp.bodyAsJsonObject().getDouble("level")
    val balance = balanceResp.bodyAsJsonObject().getDouble("balance")
    val json = json {
      obj(
          "account" to account,
          "debt" to debt,
          "balance" to balance
      )
    }

    rc.response().end(json.encodePrettily())
  }
}