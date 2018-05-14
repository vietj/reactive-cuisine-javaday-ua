package reactivekitchen.iot;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.mqtt.MqttServer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;


public class IOTGateway extends AbstractVerticle {

  @Override
  public Completable rxStart() {

    KafkaProducer<String, JsonObject> stream = KafkaProducer.create(vertx, configuration());

    // TODO Create MQTT Server
    return MqttServer.create(vertx, getMqttServerOptions())
        .endpointHandler(endpoint -> {
          System.out.println("Client connected " + endpoint.clientIdentifier());

          endpoint.publishHandler(message -> {
            System.out.println("received " + message.payload() + " on " + message.topicName());
            JsonObject object = message.payload().toJsonObject();
            stream
                .write(createRecord(object.getString("uuid"), object));
          });

          endpoint.accept();

        })
        .rxListen()
        .toCompletable();

    // TODO Attach a endpoint handler
    // TODO On the endpoint register a publish handler
    // TODO accept the endpoint

    // TODO Listen
    // TODO Report
  }

  private KafkaProducerRecord<String, JsonObject> createRecord(String uuid, JsonObject data) {
    return KafkaProducerRecord.create("data", uuid, data);
  }

  private MqttServerOptions getMqttServerOptions() {
    return new MqttServerOptions()
        .setPort(1883)
        .setHost("0.0.0.0");
  }

  private Map<String, String> configuration() {
    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", "localhost:9092");
    config.put("key.serializer", StringSerializer.class.getName());
    config.put("value.serializer", JsonObjectSerializer.class.getName());
    config.put("key.deserializer", StringDeserializer.class.getName());
    config.put("value.deserializer", JsonObjectDeserializer.class.getName());
    config.put("group.id", "some-group");
    config.put("acks", "1");
    return config;

  }
}
