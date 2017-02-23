/**
 *
 */
package com.redhat.brq.integration.activemq;

import javax.jms.JMSException;

/**
 * Producer which uses MQTT protocol to communicate with ActiveMQ broker.
 * 
 * @author jknetl
 */
public class MqttProducer {
	private String destinationName;
	private String brokerUrl;

	public MqttProducer(String brokerUrl, String destinationName) {
		super();
		this.destinationName = destinationName;
		this.brokerUrl = brokerUrl;
	}

	/**
	 * Produces messages using MQTT. Each message contains job with random duration.
	 *
	 * @param count number of message generated
	 * @throws JMSException
	 */
	public void produceMessages(int count) {
	}
}
