import java.util.Calendar;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class FxQuickstart implements MqttCallback {
	MqttClient myClient;
	MqttConnectOptions connOpt;
	
	static final String BROKER_URL = "tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";
	static final String M2MIO_THING = "net.mybluemix.fx.mqtt.publish"; //. deviceId
	
	static int interval = 5;

	@Override
	public void connectionLost(Throwable t) {
		// code to reconnect to the broker would go here if desired
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic);
		System.out.println("| Message: " + new String(message.getPayload()));
		System.out.println("-------------------------------------------------");
	}

	public static void main(String[] args) {
		try{
			if( args.length > 0 ){
				interval = Integer.parseInt( args[0] );
			}
		}catch( Exception e ){
		}
		
		FxQuickstart fxq = new FxQuickstart();
		fxq.runClient();
	}


	public void runClient() {
		// TODO Auto-generated method stub
		String clientID = "d:quickstart:MyDevice:" + M2MIO_THING;
		System.out.println( "clientID=" + clientID + "(" + clientID.length() + ")" );
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession( true );
		connOpt.setKeepAliveInterval( 30 );

		// Connect to Broker
		try{
			myClient = new MqttClient( BROKER_URL, clientID );
			myClient.setCallback( this );
			myClient.connect( connOpt );
		}catch( MqttException e ){
			e.printStackTrace();
			System.exit( -1 );
		}

		String myTopic = "iot-2/evt/dotnsffxrate/fmt/json";
		MqttTopic topic = myClient.getTopic( myTopic );
		
		while( true ){
			try{
				HttpClient client = new HttpClient();
				GetMethod get = new GetMethod( "http://fx.mybluemix.net/" );
				int sc = client.executeMethod( get );
				String out = get.getResponseBodyAsString();
				
				//. MQTT Publish
		   		int pubQoS = 0;
				MqttMessage message = new MqttMessage( out.getBytes() );
		    	message.setQos( pubQoS );
		    	message.setRetained( false );

		    	// Publish the message
		    	System.out.println( "Publishing to topic \"" + topic + "\" qos " + pubQoS );
		    	MqttDeliveryToken token = null;
		    	try{
		    		// publish message to broker
					token = topic.publish( message );
			    	// Wait until the message has been delivered to the broker
					token.waitForCompletion();
					Thread.sleep( 1000 );
				}catch( Exception e ){
					e.printStackTrace();
				}

				//. 次の実行タイミングを待つ
				Calendar c0 = Calendar.getInstance();
				int s0 = ( c0.get( Calendar.SECOND ) % interval );
				int w = 1000 * ( interval - s0 );
				Thread.sleep( w );
			}catch( Exception e ){
			}
		}
	}

}
