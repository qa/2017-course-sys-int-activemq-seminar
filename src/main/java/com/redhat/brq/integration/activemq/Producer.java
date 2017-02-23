/**
 *
 */
package com.redhat.brq.integration.activemq;

import com.redhat.brq.integration.activemq.util.XmlConverter;
import com.redhat.brq.integration.activemql.model.Job;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import java.util.Random;

/**
 * Simple synchronous JMS producer.
 * 
 * @author jknetl
 *
 */
public class Producer {

	private Connection connection;
	private String destinationName;
	
	private static final String DURATION_PROPERTY = "DURATION";

	public Producer(Connection connection, String destinationName) {
		super();
		this.connection = connection;
		this.destinationName = destinationName;
	}

	/**
	 * Produces messages. Each message contains job with random duration.
	 *
	 * @param count number of message generated
	 * @throws JMSException
	 */
	public void produceMessages(int count) throws JMSException {
		Session session;
		try {
			// create non-transacted session with auto acknowledgement
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// get destination object based on name
			Destination destination = session.createQueue(destinationName);
			MessageProducer producer = session.createProducer(destination);

			// set persistent delivery mode (default option)
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

			for (int i = 0; i < count; i++) {

				// create job with random duration
				Random random = new Random(System.currentTimeMillis());
				int duration = random.nextInt(Job.MAX_DURATION) + 1;
				Job job = new Job("Job " + (i + 1), duration);

				// create Text message with XML representation of a job
				Message message = session.createTextMessage(XmlConverter.toXml(Job.class, job));
				message.setIntProperty(DURATION_PROPERTY, job.getDuration());
				System.out.println("Producer: sending: " + job.toString() + " to destination " + destinationName);

				// synchronously send message
				producer.send(message);
			}

			producer.close();
			session.close();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
