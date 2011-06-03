package uk.co.forward.flume.sink;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.zeromq.ZMQ;
import com.cloudera.util.Pair;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;

public class ZeromqSink extends EventSink.Base {
  static final Logger LOG = LoggerFactory.getLogger(ZeromqSink.class);

  private int port;
  private String valueDecoratorAttr;
    
  private ZMQ.Context context;
  private ZMQ.Socket publisher;

  public ZeromqSink(int port, String valueDecoratorAttr) {
    this.port = port;
    this.valueDecoratorAttr = valueDecoratorAttr;
  }
  
  @Override
  public void open() throws IOException {
    this.context = ZMQ.context(1);
    this.publisher = context.socket(ZMQ.PUB);
    this.publisher.bind("tcp://*:"+this.port);
    LOG.info("0MQ sink successfully opened");
  }
  
  @Override
  public void append(Event e) throws IOException, InterruptedException {
    if (this.valueDecoratorAttr != null) {
      byte[] channel = e.getAttrs().get(this.valueDecoratorAttr);
      if (channel != null) {
        this.publisher.send(channel, ZMQ.SNDMORE);
      }
    }
    this.publisher.send(e.getBody(), 0);
    super.append(e);
  }

  @Override
  public void close() throws IOException {
    this.publisher.close();
    this.context.term();
    LOG.info("0MQ sink successfully closed");
  }
  
  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... argv) {
        int port = 5555;
        String valueDecoratorAttr = null;
        
        if (argv.length == 1) {
          port = Integer.parseInt(argv[0]);
          if(port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Illegal Port Specified: "+ argv[0]);
          }
        } else if (argv.length >= 2) {
          valueDecoratorAttr = argv[1].trim();
          if(valueDecoratorAttr.equals("")) {
            throw new IllegalArgumentException("Value Decorator Attribute cannot be empty");
          }
        }
        return new ZeromqSink(port, valueDecoratorAttr);
      }
    };
  }
  
  public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
      List<Pair<String, SinkBuilder>> builders =
              new ArrayList<Pair<String, SinkBuilder>>();
      builders.add(new Pair<String, SinkBuilder>("zmqSink", builder()));
      return builders;
  }
}