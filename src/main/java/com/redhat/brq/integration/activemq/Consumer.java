package com.redhat.brq.integration.activemq;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.JMSException;

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
		/*
		 * TODO:
		 * 1) create Session, Destination and MessageConsumer objects
		 * 2) start the connection
		 * 3) synchronously receive messages in loop until no message is received in TIMEOUT.
		 *    -- Do not forget to specify timeout for receive method or application won't end.
		 * 4) extract job from message using XmlConverter and then execute the job in the message using executeJob method.
		 * 5) close the session
		 */
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
