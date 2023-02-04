import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ClientProducer implements Runnable{
    private final BlockingQueue<Long> calculateBuffer;

    private final BlockingQueue<Record> recordBuffer;

    private CountDownLatch countDownLatchWatch;

    private final int SWPIERBOUND = 5000;

    private final int SWIPEEBOUND = 1000000;

    private final int COMMENT_LENGTH = 256;

    private final int RETRY_TIMES = 5;

    private int requestsNum;

    private final String BASE_PATH = "http://35.90.13.127:8080/Assignment1_war";

    public ClientProducer(BlockingQueue<Long> calculateBuffer, BlockingQueue<Record> recordBuffer,
                          CountDownLatch countDownLatch, int requestsNum) {

        this.calculateBuffer = calculateBuffer;
        this.recordBuffer = recordBuffer;
        this.countDownLatchWatch = countDownLatch;
        this.requestsNum = requestsNum;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BASE_PATH);
        SwipeApi apiInstance = new SwipeApi(apiClient);
        for(int request = 0; request < requestsNum; request++) {
            SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
            Random rand = new Random();
            // generate swiper randomly
            int swiper  = rand.nextInt(SWPIERBOUND) + 1;
            // generate swipee randomly
            int swipee  = rand.nextInt(SWIPEEBOUND) + 1;
            // generate comment with length 256 randomly
            char[] commentCharArray = new char[COMMENT_LENGTH];
            for(int i = 0; i < COMMENT_LENGTH; i++) {
                commentCharArray[i] = (char)('a' + rand.nextInt(26));
            }
            String comment = String.valueOf(commentCharArray);
            body.setSwiper(String.valueOf(swiper));
            body.setSwipee(String.valueOf(swipee));
            body.setComment(comment);
            // generate leftOrRight randomly
            int randomLeftOrRight = rand.nextInt(2);
            String leftorright = randomLeftOrRight == 0 ? "left" : "right"; // String | I like or dislike user
            // record start timestamp
            long startTimestamp = System.currentTimeMillis();
            // initialize end timestamp
            long endTimestamp = 0;
            // initialize response code
            int responseCode = 0;
            // initialize latency
            long latency = 0;
            try {
                // apiInstance.swipe(body, leftorright);
                ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, leftorright);
                // record end timestamp
                endTimestamp = System.currentTimeMillis();
                responseCode = response.getStatusCode();
            } catch (ApiException e) {
                //  System.err.println("Exception when calling SwipeApi#swipe");
                //  e.printStackTrace();
                //  process status code with 4XX and 5XX
                // retry 5 times
                ApiResponse<Void> newResponse = null;
                for (int i = 0; i < RETRY_TIMES; i++) {
                    try {
                        newResponse = apiInstance.swipeWithHttpInfo(body, leftorright);
                        int newStatusCode = newResponse.getStatusCode();
                        if (newStatusCode >= 200 && newStatusCode < 300) {
                            break;
                        }
                    } catch (ApiException ex) {
                        continue;
                    }
                }
                endTimestamp = System.currentTimeMillis();
                responseCode = newResponse == null ? 500 : newResponse.getStatusCode();
            }
            latency = endTimestamp - startTimestamp;
            // create record
            Record record = new Record(startTimestamp, "POST", latency, responseCode);
            // write into record consumer
            try {
                recordBuffer.put(record);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // write latency into calculator consumer
            try {
                calculateBuffer.put(latency);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        countDownLatchWatch.countDown();
    }
}
