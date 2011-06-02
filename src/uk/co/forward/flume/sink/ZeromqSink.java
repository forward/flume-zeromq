package uk.co.forward.flume.sink;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zeromq.ZMQ;
import com.cloudera.util.Pair;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;

public class ZeromqSink extends EventSink.Base {
  
  private int port;
  private ZMQ.Context context;
  private ZMQ.Socket publisher;

  public ZeromqSink(int port) {
    this.port = port;
  }
  
  @Override
  public void open() throws IOException {
    this.context = ZMQ.context(1);
    this.publisher = context.socket(ZMQ.PUB);
    this.publisher.bind("tcp://*:"+this.port);
  }
  
  @Override
  public void append(Event e) throws IOException, InterruptedException {
    String body = new String(e.getBody());

    byte[] bucket = e.getAttrs().get("bucket");
    if (bucket != null) {
        body = new String(bucket) + ":" + body;
    }

    this.publisher.send(body.getBytes(), 0);
    super.append(e);
  }

  @Override
  public void close() throws IOException {
    this.publisher.close();
    this.context.term();
  }
  
  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... argv) {
        int port = 5555;
        if (argv.length >= 1) {
          port = Integer.parseInt(argv[0]);
          if(port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Illegal Port Specified: "+ argv[0]);
          }
        }
        return new ZeromqSink(port);
      }
    };
  }
  
  public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
      List<Pair<String, SinkBuilder>> builders =
              new ArrayList<Pair<String, SinkBuilder>>();
      builders.add(new Pair<String, SinkBuilder>("zeromqSink", builder()));
      return builders;
  }
}