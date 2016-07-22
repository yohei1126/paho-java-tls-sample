import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import javax.net.ssl.SSLSocketFactory;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.Properties;

public class MqttPubSubSample {

    public static void main(String[] args) {

        String topic = "test";
        Hoge hoge = new Hoge();
        hoge.id = 10;
        hoge.name = "hoge";
        int qos             = 2;
        String broker       = "ssl://ec2-54-69-226-235.us-west-2.compute.amazonaws.com:1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();
        //for json
        final ObjectMapper mapper = new ObjectMapper();
        //for message pack
        //final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            Properties sslProperties = new Properties();
            sslProperties.setProperty("com.ibm.ssl.protocol", "TLS");
            sslProperties.setProperty("com.ibm.ssl.trustStore", "/Users/yohei/work/paho-java-tls-sample/conf/ca.crt.jks");

            connOpts.setSSLProperties(sslProperties);

            sampleClient.setCallback(new MyCallback(mapper));

            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            sampleClient.subscribe("test");
            Thread.sleep(1000);

            System.out.println("Publishing message: " + hoge);
            byte[] json = mapper.writeValueAsBytes(hoge);
            MqttMessage message = new MqttMessage(json);
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");

            Thread.sleep(10000);

            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

class MyCallback implements MqttCallback {
    private ObjectMapper mapper_;
    MyCallback(ObjectMapper mapper) {mapper_ = mapper;}
    @Override
    public void connectionLost(Throwable cause) {
    System.out.println("connection lost " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("messageArrived topic = " + topic + ", message = " + mapper_.readValue(message.getPayload(), Hoge.class));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete token = " + token);
    }
}