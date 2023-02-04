import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class CalculateConsumer implements Runnable {
    private final BlockingQueue<Long> calculateBuffer;

    private int requestNum = 0;
    private List<Long> latencies = new ArrayList<>();

    private long minLatency = Long.MAX_VALUE;

    private long maxLatency = Long.MIN_VALUE;

    private long latencySum = 0;

    public CalculateConsumer(BlockingQueue<Long> calculateBuffer) {
        this.calculateBuffer = calculateBuffer;
    }

    public long getMinLatency() {
        return minLatency;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public long getMedianLatency() {
        int p1 = requestNum / 2;
        int p2 = p1 - 1;
        return (latencies.get(p1) + latencies.get(p2)) / 2;
    }

    public long getP99Latency() {
        int position = Math.min((int)(requestNum * 0.99), requestNum - 1);
        return latencies.get(position);
    }

    public long getMeanLatency() {
        return latencySum / requestNum;
    }

    public void incRequestNum() {
        requestNum++;
    }

    public void addLatency(long latency) {
        latencies.add(latency);
    }

    public void updateLatencySum(long latency) {
        latencySum += latency;
    }

    public void run() {
        boolean active = true;
        try {
            while (active) {
                Long latency = calculateBuffer.take();
                // end condition
                if (latency < 0) {
                    active = false;
                    // sort latencies
                    Collections.sort(latencies);
                } else {
                    // add number of requests
                    incRequestNum();
                    // add latency
                    addLatency(latency);
                    // update latency sum
                    updateLatencySum(latency);
                    // update min latency
                    minLatency = Math.min(minLatency, latency);
                    // update max latency
                    maxLatency = Math.max(maxLatency, latency);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws
            InterruptedException, IOException {
        // set number of requests to send
        int numOfRequests = 500000;
        // set number of producers
        int numOfProducer = 200;
        // set count down latch
        CountDownLatch countDownLatch = new CountDownLatch(numOfProducer);
        // create a blocking queue for calculate
        BlockingQueue calculateBuffer = new LinkedBlockingQueue();
        // create a blocking queue for record
        BlockingQueue recordBuffer = new LinkedBlockingQueue();
        // record start timestamp
        long startTime = System.currentTimeMillis();
        // create multiple producers
        for(int i = 0; i < numOfProducer; i++) {
            ClientProducer producer = new ClientProducer(calculateBuffer, recordBuffer, countDownLatch,
                    numOfRequests / numOfProducer);
            // start the producer
            new Thread(producer).start();
        }
        // create and start a calculate consumer
        CalculateConsumer calculateConsumer =  new CalculateConsumer(calculateBuffer);
        new Thread(calculateConsumer).start();
        // create and start a record consumer
        RecordConsumer recordConsumer = new RecordConsumer(recordBuffer, "./records.csv");
        new Thread(recordConsumer).start();
        // wait until all producers are done
        countDownLatch.await();
        //record end timestamp
        long endTime = System.currentTimeMillis();
        // send termination message to the calculate consumer
        calculateBuffer.put((long)(-1));
        // send termination message to the record consumer
        recordBuffer.put(new Record(-1, "POST", -1, 201));
        // calculate duration in seconds
        long duration = (endTime - startTime) / 1000;
        // calculate throughput
        long throughput = numOfRequests / duration;
        // print out the result
        System.out.println("The number of threads used to send requests is " + numOfProducer + ".");
        System.out.println("The total run (wall) time is " + duration + " seconds.");
        System.out.println("The total throughput in requests per second is " + throughput + ".");
        System.out.println("The mean response time (millisecs) is " +
                calculateConsumer.getMeanLatency() + ".");
        System.out.println("The median response time (millisecs) is " +
                calculateConsumer.getMedianLatency() + ".");
        System.out.println("The max response time (millisecs) is " +
                calculateConsumer.getMaxLatency() + ".");
        System.out.println("The min response time (millisecs) is " +
                calculateConsumer.getMinLatency() + ".");
        System.out.println("The p99 (99th percentile) response time (millisecs) is "
                + calculateConsumer.getP99Latency() + ".");
    }
}
