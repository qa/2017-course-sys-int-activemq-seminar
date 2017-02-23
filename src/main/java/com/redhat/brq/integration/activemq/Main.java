/**
 *
 */
package com.redhat.brq.integration.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.redhat.brq.integration.activemq.util.JmxUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

/**
 * Main class foro launching the code in the seminar.
 * 
 * Arguments:
 * -h				prints help
 * -p				launches producer
 * -c				launches consumer
 * -q				launches mqtt producer
 * -m NUMBER 		specify number of messages to send for consumer (Default is 3)
 * -d DESTINATION 	specify name of destination (default is seminar.jobs)
 * -u URL			specify broker url (default is tcp://localhost:61616)
 * 
 * @author jknetl
 *
 */
public class Main {
	public static void main(String[] args) throws JMSException {
 
		CommandLine cmd = parseCommandLine(args);

		// storing arguments from command line
		String brokerUrl = cmd.hasOption("u") ? cmd.getOptionValue("u") : "tcp://localhost:61616";
		String destinationName = cmd.hasOption("d") ? cmd.getOptionValue("d") : "seminar.jobs";
		int messageCount = cmd.hasOption("m") ? Integer.valueOf(cmd.getOptionValue("m")) : 3;

		// ConnecctionFactory creation for all consumers/producers
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", brokerUrl);
		Connection connection = null;
		try {
			// create a JMS connection if required
			if (isJmsClinet(cmd)){
				connection = connectionFactory.createConnection();
				connection.start();
			}

			// producer execution
			if (cmd.hasOption("q")) {
				MqttProducer producer = new MqttProducer(brokerUrl, destinationName);
				producer.produceMessages(messageCount);
			}

			// producer execution
			if (cmd.hasOption("p")) {
				Producer producer = new Producer(connection, destinationName);
				producer.produceMessages(messageCount);
			}

			// consumer execution
			if (cmd.hasOption("c")) {
				Consumer consumer = new Consumer("Consumer-1", connection, destinationName);
				consumer.consumeMessages();
				JmxUtils.waitUntilQueueIsEmpty(destinationName);
			}

		} catch (JMSException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}

	}

	/**
	 * Parses command line arguments using Apache commons CLI
	 * 
	 * @param args array of arguments
	 * @return CommandLine object representing arguments
	 */
	private static CommandLine parseCommandLine(String[] args) {

		// Definition of possible command line options
		Options options = new Options();
		options.addOption("p", "producer", false, "launches producer");
		options.addOption("c", "consumer", false, "launches consumer");
		options.addOption("q", "mqttProducer", false, "launches MQTT consumer");
		options.addOption("u", "url", true, "broker url");
		options.addOption("m", "messageCount", true, "Number of messages");
		options.addOption("d", "destination", true, "destination name");
		options.addOption("h", "help", false, "show help");

		// parsing of arguments
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			formatter.printHelp("activemq-seminar", options);
			System.exit(1);
		}

		if (cmd.hasOption("h")) {
			formatter.printHelp("activemq-seminar", options);
			System.exit(0);
		}

		return cmd;
	}

	/**
	 * Returns true if JMS connection needs to be created.
	 * 
	 * @param cmd Command line options.
	 * 
	 * @return true if JMS producer or JMS consumer will be started.
	 */
	private static boolean isJmsClinet(CommandLine cmd) {
		return (cmd.hasOption("p") || cmd.hasOption("c"));
	}
}
