package com.redhat.brq.integration.activemq.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;

/**
 * Class with utility methods which communicates with broker using JMX.
 * 
 * @author jknetl
 */
public class JmxUtils {

	
	// prevent instantiation of utility class
	private JmxUtils(){
	}

	/**
	 * Blocking call which waits until queue is empty.
	 *
	 * @param destinationName name of a queue
	 * @throws Exception when there is problem with communication over JMX to broker
	 */
	public static void waitUntilQueueIsEmpty(String destinationName) throws Exception {
		JMXConnector jmxc = null;
		try {
			JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://0.0.0.0:44444/jndi/rmi://0.0.0.0:1099/karaf-root");
			Map<String, String[]> env = new HashMap<>();
			String[] credentials = { "admin", "admin" };
			env.put(JMXConnector.CREDENTIALS, credentials);

			jmxc = JMXConnectorFactory.connect(url, env);
			MBeanServerConnection conn = jmxc.getMBeanServerConnection();
			ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=amq");

			BrokerViewMBean mbean = MBeanServerInvocationHandler.newProxyInstance(conn, activeMQ, BrokerViewMBean.class,
					true);
			ObjectName[] queues = mbean.getQueues();
			QueueViewMBean queue = null;
			for (ObjectName name : queues) {
				QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(conn, name,
						QueueViewMBean.class, true);
				if (queueMbean.getName().equals(destinationName)) {
					queue = queueMbean;
				}
			}

			if (queue != null) {
				boolean isEmpty = false;
				while (!isEmpty) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					isEmpty = (queue.getQueueSize() == 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jmxc != null) {
				jmxc.close();
			}
		}

	}

	/**
	 * Waits for user input.
	 * 
	 * @return line with user input or null if input cannot be read.
	 */
	public static String waitForInput() {
		BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
		try {
			return is.readLine();
		} catch (IOException e) {
			return null;
		}
	}

}
