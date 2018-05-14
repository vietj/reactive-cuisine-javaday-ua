package me.escoffier.demo.bank.gateway;


import io.vertx.core.Vertx;

public class Main {

  public static void main(String[] args) {
    // TODO Create Vert.x
    Vertx vertx = Vertx.vertx();

    // TODO Deploy verticle
    vertx.deployVerticle(com.julienviet.demo.bank.gateway.GatewayVerticle.class.getName());
  }

}
