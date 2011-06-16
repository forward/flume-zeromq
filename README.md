flume-zeromq
============

This is a zeromq sink for flume. All events sent to this sink are PUBLISHED by zeromq.

This project includes both the java classes and jars needed to get the sink up and running.

Dependencies
------------

* flume (originally developed with 0.9.3, but it will probably work with other versions)
* zeromq
* zeromq java bindings

Getting Started
---------------

Follow the instructions to install flume plugins. Basically:

1. Modify your $FLUME_HOME/conf/flume-site.xml:
       <!--- ================================================= -->
       <!--- Flume Plugins =================================== -->
       <!--- ================================================= -->
       <property>
         <name>flume.plugin.classes</name>
         <value>uk.co.forward.flume.sink.ZeromqSink</value>
         <description>Comma separated list of plugin classes</description>
       </property>

2. Ensure that both zmq.jar and flume-zmq.jar are on the FLUME_CLASSPATH
when flume master and flume nodes are started. (Or, you can drop these jars in the /usr/lib/flume/lib directory)

3. Ensure that java.library.path is set to the directory where the libzmq and libjzmq shared libraries are installed (normally /usr/local/lib on UNIX-like systems) by modifying your $FLUME_HOME/bin/flume-env.sh:

       export JAVA_LIBRARY_PATH=/usr/local/lib:$JAVA_LIBRARY_PATH

4. Set up a flume data path:

       node: tail("/var/log/syslog") | zmqSink    # binds zeromq to default port 5555
       node: tail("/var/log/syslog") | zmqSink(5556)    # example of how to specify port
       node: tail("/var/log/syslog") | zmqSink(5556, "valueDecoratorAttr") # if you provide a value decorator attribute, all flume events tagged with that attribute will be sent on a zeromq channel named after the attribute

5. Enjoy!
