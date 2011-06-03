package uk.co.forward.flume.sink;

import org.zeromq.ZMQ;

public class Foo {
    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context context;
        ZMQ.Socket publisher;

        context = ZMQ.context(1);
        publisher = context.socket(ZMQ.PUB);
        publisher.bind("tcp://*:5555");

        while(true) {
            publisher.send("Hello".getBytes(), 0);
            Thread.sleep(10000);
        }
    }
}
