package com.redhat.brq.integration.activemq;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
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
	private String selector;

	public Consumer(String id, Connection connection, String destinationName, String selector) {
		super();
		this.id = id;
		this.connection = connection;
		this.destinationName = destinationName;
		this.selector = selector;
	}

	public void consumeMessages() throws JMSException, InterruptedException {
		try {
			// create non-transacted session with auto acknowledge mode
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// get destination object
			Destination destination = session.createQueue(destinationName);

			// create consumer
			MessageConsumer consumer = session.createConsumer(destination, selector);

			// add  listener
			consumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					TextMessage jobMessage = (TextMessage) message;
					try {
						Job job = XmlConverter.toObject(Job.class, jobMessage.getText());
						executeJob(job);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (JMSException e) {
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
