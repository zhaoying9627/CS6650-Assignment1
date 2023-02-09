import java.util.concurrent.*;

public class Consumer implements Runnable {
    private final BlockingQueue<String> buffer;

    private int successfulCount = 0;
    private int failCount = 0;

    public Consumer(BlockingQueue<String> q) {
        this.buffer = q;
    }

    public void incSuccess() {
        successfulCount++;
    }

    public void incFail() {
        failCount++;
    }

    public int getSuccessfulCount() {
        return successfulCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void run() {
        boolean active = true;
        try {
            while (active) {
                String result = buffer.take();
                if (result.equals(" ")) {
                    active = false;
                } else if (result.equals("success")) {
                    incSuccess();
                } else if (result.equals("fail")) {
                    incFail();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws
            InterruptedException {
        // set number of requests to send
        int numOfRequests = 500000;
        // set number of producers
        int numOfProducer = 200;
        // set count down latch
        CountDownLatch countDownLatch = new CountDownLatch(numOfProducer);
        // create a blocking queue
        BlockingQueue buffer = new LinkedBlockingQueue();
        // record start timestamp
        long startTime = System.currentTimeMillis();
        // create multiple producers
        for(int i = 0; i < numOfProducer; i++) {
            ClientProducer producer = new ClientProducer(buffer, countDownLatch, numOfRequests / numOfProducer);
            // start the producer
            new Thread(producer).start();
        }
        // create and start a consumer
        Consumer consumer =  new Consumer(buffer);
        new Thread(consumer).start();
        // wait until all producers are done
        countDownLatch.await();
        //record end timestamp
        long endTime = System.currentTimeMillis();
        // send termination message to the consumer
        buffer.put(" ");
        // calculate duration in seconds
        long duration = (endTime - startTime) / 1000;
        // calculate throughput
        long throughput = numOfRequests / duration;
        // print out the result
        System.out.println("The number of threads used to send requests is " + numOfProducer + ".");
        System.out.println("The response time for single request is 28ms." );
        System.out.println("The Little’s Law prediction throughput in requests per second is 7142.");
        // output for springboot server
//        System.out.println("The response time for single request is 60ms." );
//        System.out.println("The Little’s Law prediction throughput in requests per second is 3333.");
        System.out.println("The number of successful requests sent is " + consumer.getSuccessfulCount() + ".");
        System.out.println("The number of unsuccessful requests is " + consumer.getFailCount() + ".");
        System.out.println("The total run (wall) time is " + duration + " seconds.");
        System.out.println("The total throughput in requests per second is " + throughput + ".");
    }
}
