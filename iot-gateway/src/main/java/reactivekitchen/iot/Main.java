package reactivekitchen.iot;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Main {

  public static void main(String[] args) throws IOException {
    init();

    Vertx vertx = Vertx.vertx();

    vertx.rxDeployVerticle(IOTGateway.class.getName())
        .flatMap(x -> vertx.rxDeployVerticle(DataProcessorVerticle.class.getName()))
        .flatMap(x -> vertx.rxDeployVerticle(WebVerticle.class.getName()))
        .flatMap(x -> vertx.rxDeployVerticle(Sensor.class.getName(), new DeploymentOptions().setInstances(5)))
        .subscribe();
  }

  private static void init() throws IOException {
    File dataDir = Testing.Files
        .createTestingDirectory("cluster");
    dataDir.deleteOnExit();
    new KafkaCluster()
        .usingDirectory(dataDir)
        .withPorts(2181, 9092)
        .addBrokers(1)
        .deleteDataPriorToStartup(true)
        .startup();
  }
}
