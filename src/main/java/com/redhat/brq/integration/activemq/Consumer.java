package com.redhat.brq.integration.activemq;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import com.redhat.brq.integration.activemq.util.XmlConverter;
import com.redhat.brq.integration.activemql.model.Job;

/**
 * JMS consumer class.
 * 
 * @author jknetl
 */
public class Consumer {
	private static final int TIMEOUT = 1 * 1000;

	private String id;
	private Connection connection;
	private String destinationName;

	public Consumer(String id, Connection connection, String destinationName) {
		super();
		this.id = id;
		this.connection = connection;
		this.destinationName = destinationName;
	}

	public void consumeMessages() throws JMSException, InterruptedException {
		try {
			// create non-transacted session with auto acknowledge mode
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// get destination object
			Destination destination = session.createQueue(destinationName);

			// create consumer
			MessageConsumer consumer = session.createConsumer(destination);

			// synchronously receive messages until there are no messages in
			boolean lastMessageWasNull = false;
			while (!lastMessageWasNull) {
				TextMessage jobMessage = (TextMessage) consumer.receive(TIMEOUT);
				if (jobMessage == null) {
					lastMessageWasNull = true;
				} else {
					Job job = XmlConverter.toObject(Job.class, jobMessage.getText());
					executeJob(job);
				}
			}
			session.close();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simulates execution of the job by sleeping for job duration.
	 *
	 * @param job
	 *            job to be executed
	 * @throws InterruptedException
	 */
	protected void executeJob(Job job) throws InterruptedException {
		StringBuilder str = new StringBuilder("Consumer: " + id)
				.append(" Executing " + job.toString()).append(" It will take " + job.getDuration())
				.append(" seconds.");
		System.out.println(str.toString());
		TimeUnit.SECONDS.sleep(job.getDuration());
	}
}
