# ActiveMQ seminar

In this seminar we will sends some simple jobs representation between applications over A-MQ broker using JMS client and MQTT client. Additionally, we will configure A-MQ to run master-slave mode. Lastly, we will configure 2 A-MQ instances to be connected into Network of Brokers.

## Useful links

* [JMS 1.1 specification](http://download.oracle.com/otndocs/jcp/7195-jms-1.1-fr-spec-oth-JSpec/)
  * contains also section with examples at the end
* [JBoss A-MQ documentation](https://access.redhat.com/documentation/en/red-hat-jboss-a-mq/?version=6.3)
* [JBoss Fuse documentation](https://access.redhat.com/documentation/en/red-hat-jboss-fuse/?version=6.3)

## Task 1) Preparing environment

1. download [JBoss Fuse](https://developers.redhat.com/products/fuse/download/).
  * JBoss Fuse contains broker ActiveMQ broker same as JBoss A-MQ
2. extract downloaded archive
3. add admin user by commenting out last line in
    ```
    ${INSTALLATION-FOLDER}/etc/users.properties
    ```
4. start JBoss Fuse/A-MQ  by executing:
    ```
    # for unix environment
    ${INSTALLATION-FOLDER}/bin/start

    # for windows environment
    ${INSTALLATION-FOLDER}\bin\start.bat
    ```
5. clone this repository
    ```
    git clone https://github.com/jknetl/activemq-course
    ```
5. use maven to build seminar code:
    ```
    mvn clean install
    ```
6. import this repository as maven project into JBoss Developer Studio

## Task 2) Discovering useful tools

1. look around the source code in the repository. It contains simple JMS client whose producer sends Jobs to the broker.
  * files
    1. Main.java - contains main class. If you want to run application you should provides switches (without switches application does nothing)
      * `-p` laumches producer
      * `-c` launches consumer (you will need to implement consumer first)
    2. Producer.java - implemented JMS synchronous producer which sends Jobs to the broker.
    2. Consumer.java - JMS Consumer which has not been fully implemented yet
    3. JmxUtils.java - contains helper method which can block until some queue is empty (not necessary)
    4. XmlConverter.java - converts between XML representation of an object and its Java representation using JAXB
    5. Job.java - represents a Job. Execution of a Job is time-consuming.
  * code should be commented but ask immediately if you don't understand anything
2. execute producer by executing Main.java class with `-p` option:
  * from command line using maven exec plugin:

    ```
    mvn exec:java -Dexec.args="-p"
    ```

  * from the JBoss Developer Studio by selecting from menu bar:
    1. Run -> Run Configurations...
    2. Add Java application
    3. On Arguments pane add "-p" option
    4. then click Run
  * if you've done everything properly the client should send messages with Jobs to broker. You should see something like this:

    ```
    Producer: sending: Job [name=Job 1, duration=6] to destination seminar.jobs
    Producer: sending: Job [name=Job 2, duration=5] to destination seminar.jobs
    Producer: sending: Job [name=Job 3, duration=7] to destination seminar.jobs
    ```

3. inspecting Karaf commands
  1. open Karaf console

    ```
    # just execute client script:
    # on linux
    ${INSTALLATION-FOLDER}/bin/client
    # on windows
    ${INSTALLATION-FOLDER}\bin\client.bat
    ```

  2. execute following commands

    ```
    # for broker statistics:
    activemq:bstat
    # for destinations statistics:
    activemq:dstat

    # if you want to delete messages in the broker, then execute
    activemq:purge
    ```

4. ActiveMQ tab in Hawtio console
  1. navigate to [Hawtio console](http://localhost:8181/)
  2. open ActiveMQ tab
  3. browse messages in the destination "seminar.jobs" and see their content
  4. delete the destination using the web-console (if you haven't done it already in karaf console)

## Task 3) Implementing consumer

In this task you will implement JMS consumer, which will receive jobs from "seminar.jobs" queue and it will execute them.

1. execute application with `-p`  argument again so that you have some jobs in the "seminar.jobs" queue
2. implement `Consumer#consumeMessages()` method using synchronous receive (see comments in consumer for help)
  * execute every received job
3. build and execute app with -c argument only:

    ```
    mvn clean install exec:java -Dexec.args="-c"
    ```

  * it will run consumer you've just implemented and it should start consuming messages (you should see job execution on stdout):

        ```
        Executing Job [name=Job 1, duration=6] It will take 6 seconds
        ...
        ```

## Task 4) Changing consumer to asynchronous

In this exercise you will change consumer to asynchronous (non-blocking) api.

1. change consumer that it will use asynchronous receive:
  1. create connection in the Main class and pass it to Consumer using constructor (you will have to modify consumer class also)
  2. change consumer class so it use asynchronous receive
    * Hint: remove receive loop and add message listener instead
    * Hint 2: do not close connection in the Consumer class.
2. add `waitUntilDestinationEmpty()` method call to Main class after consumer.ConsumeMessages();
3. build and execute app with '-c -p' arguments:

    ```
    mvn clean package exec:java -Dexec.args="-c -p"
    ```

  * it should start producer first and then consumer

## Task 5) Message selectors

In this exercise you will configure consumer so that it receives only some subset of messages based on selector criteria.

1. extend producer so that it put duration of a job into Message property named DURATION.
2. extend consumer so class so that you can pass message selector (String):
  1. add String selector property
  2. update constructor to set also new selector property
3. change consumeMessages method so that it consumes only messages according to selector
4. In the Main class create two consumers one will consume only jobs with short duration (less than 3 seconds) and other with long duration (3 seconds and more)
  * for examples on selectors see [JMS specification](http://download.oracle.com/otndocs/jcp/7195-jms-1.1-fr-spec-oth-JSpec/) or [web examples](hAttp://timjansen.github.io/jarfiller/guide/jms/selectors.xhtml)

5. run example app with more messages:

    ```
    mvn clean package exec:java -Dexec.args="-c -p --messageCount 6"
    ```
  * you should see that longer jobs are executed one client and slower jobs by the other one.


## Task 6) Publish/Subscribe
Client application will comunicate using Publish/Subscribe domain in this exercise.

1. pass null or emtpy selectors to the consumer so that it receives all messages
2. change both consumer and producer so it uses topic instead of queues
3. replace waitUntill `JmxUtils.waitUntilQueueIsEmpty(destinationName)` with `JmxUtils.waitForInput();`
  * from now on if you will execute consumer it will be running until you press enter key
3. execute both producer and consumer in one call using:
    ```
    mvn clean package exec:java -Dexec.args="-c -p"
    ```
  * why no jobs are executed? Hints: Publish/subscribe domain and ordering of actions
  * terminate consumer with enter key
4.  launch consumer in one shell window using:

    ```
    mvn clean install exec:java -Dexec.args="-c"
    ```

5. launch producer in another shell window:

    ```
    mvn clean install exec:java -Dexec.args="-p -m 5"
    ```
  * you should see that both consumers will receive same messages

## Task 7) MQTT

You won't use JMS specificaiton to finish this task, but you will connect to broker
using MQTT client. Documentation of the MQTT client [may be found on GitHub](https://github.com/fusesource/mqtt-client).

1. configure broker so that it listens for MQTT connections. You will need to add transport connector for MQTT on port 1883 (edit conf file etc/activemq.xml)
  * add following line between `<transportConnectors> </transportConnectors>` tags

    ```
    <transportConnector name="mqtt" uri="mqtt://localhost:1883"/>
    ```

2. Finish method produceMessages in class MqttProducer to publish message into topic
  * configure:
    1. brokerUrl
    2. username
    3. password
    4. create blocking connection and connect it
    5. publish message to topic
    6. disconnect from broker
  * MQTT uses different destination naming so "." is converted to "/". See [documentation](http://activemq.apache.org/mqtt.html#MQTT-WorkingwithDestinationswithMQTT))
3. Activemq converts MQTT message to JMS Bytes message so change consumer to convert bytes content to String
4. run consumer

    ```
    mvn clean install exec:java -Dexec.args="-c -d mqtt.topic"
    ```

5. run producer

    ```
    mvn clean install exec:java -Dexec.args="-p -u tcp://localhost:1883 -d mqtt/topic"
    ```

## Task 8) Master slave

In this task you will configure ActiveMQ in master-slave mode. So that two A-MQ instances will be running. But only one of them will be active (the master instance). The other instance will be in passive mode and it will be ready to take over master role, when other instance encounters any troubles. See [Fault tolerant messaging guide](https://access.redhat.com/documentation/en-us/red_hat_jboss_a-mq/6.3/html-single/fault_tolerant_messaging/) for more details on master slave.

1. Firstly, change the application source code so that it uses queues instead of topics (change Producer and Consumer class)
2. stop your Fuse/A-MQ instance
3. install another Fuse/A-MQ instance
4. It is not expected to run multiple instances on one host, so you will have to change configuration of one instance to avoid ports conflict:
  1. change ports number in `etc/system.properties` to:
    * org.osgi.service.http.port=8182
    * activemq.port = 61617
  2. change `etc/org.apache.karaf.management.cfg`:
    * rmiRegistryPort = 1199
    * rmiServerPort = 44544
  3. change `etc/org.apache.karaf.shell.cfg`:
    * sshPort = 8102
  4. change `etc/org.ops4j.pax.web.cfg`:
    * org.osgi.service.http.port=8182
5. now it should be possible to start both instances simultaneously. However, don't launch them yet. We need to configure it in filesystem master/slave mode:
  1. you need to change persistence adapter configuration of both brokers in the `etc/activemq.xml` file, so that KahaDB uses same location on your filesystem in both instances. For example:

  ```
    <persistenceAdapter>
        <kahaDB directory="/PATH/TO/SHARED/LOCATION" lockKeepAlivePeriod="2000" />
    </persistenceAdapter>
  ```

6. now you can start both instances now
7. run producer with failover protocol and large number of messages

    ```
    mvn clean install exec:java -Dexec.args="-p -m 100000 -u failover:(tcp://localhost:61616,tcp://localhost:61617) -d example.masterslave"
    ```

7. Find out which of the broker is master
  * you can simply view logs or open Hawtio console (on first broker it runs on port 8181, and on the second broker it runs on 8182)
8. Simulate master broker error by stopping it using `bin/stop` script (or `bin\stop.bat` if you are on windows)
  * you should see that the other instance will become new master quickly
  * producer should not be affected at all and it should continue sending messages
  * all messages should be present in the queue (even those which was sent to broker which is now stopped)

## (Optional) Task 9) Network of Brokers

You will configure two brokers instances to be running simultaneously in this exercise. The brokers will be able to pass messages through the network of brokers (in this case only network of 2 brokers). Therefore, you will be able to send message to a broker and receive it on the other broker. More details can be found in [Using Network of Brokers Guide](https://access.redhat.com/documentation/en-us/red_hat_jboss_a-mq/6.3/html-single/using_networks_of_brokers/).

1. stop both brokers from exercise 8
2. change KahaDB configuration so that brokers are not configured in master-slave mode:

    ```
    <kahaDB directory="${data}/kahadb"/>
    ```

3. add duplex network connector to one of the brokers with url to the other broker. For example:

    ```
    <networkConnectors>
      <networkConnector name="to-broker-2" uri="static:(tcp://localhost:61616)" duplex="true" userName="admin" password="admin" />
    </networkConnectors>
    ```

4. start both brokers
5. delete all messages in the brokers
6. start producer to send message to one broker

    ```
    mvn clean install exec:java -Dexec.args="-p -u tcp://localhost:61616 -d example.network"
    ```
  * verify in web console or karaf client that:
    1. messages are present in the broker to which they were sended
    2. messages are not present in the other broker

7. consume message from the other broker

    ```
    mvn clean install exec:java -Dexec.args="-c -u tcp://localhost:61617 -d example.network"
    ```
  * messages should be routed through network of broker and delivered to client on the second broker

