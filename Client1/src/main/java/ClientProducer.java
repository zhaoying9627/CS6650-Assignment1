import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ClientProducer implements Runnable {
    private final BlockingQueue<String> buffer;

    private CountDownLatch countDownLatchWatch;

    private final int RETRY_TIMES = 5;

    private final int SWIPER_BOUND = 5000;

    private final int SWIPEE_BOUND= 1000000;

    private final int COMMENT_LENGTH = 256;

    private int requestsNum;

    private final String BASE_PATH = "http://35.90.13.127:8080/Assignment1_war";

    public ClientProducer(BlockingQueue<String> q, CountDownLatch countDownLatch, int requestsNum) {
        this.buffer = q;
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
            int swiper  = rand.nextInt(SWIPER_BOUND) + 1;
            // generate swipee randomly
            int swipee  = rand.nextInt(SWIPEE_BOUND) + 1;
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
            try {
                ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, leftorright);
                int statusCode = response.getStatusCode();
                // put "success" into queue
                if (statusCode >= 200 && statusCode < 300) {
                    buffer.put("success");
                }
            } catch (ApiException e) {
                //  System.err.println("Exception when calling SwipeApi#swipe");
                //  e.printStackTrace();
                // retry 5 times
                boolean retrySuccess = false;
                for (int i = 0; i < RETRY_TIMES; i++) {
                    ApiResponse<Void> newResponse = null;
                    try {
                        newResponse = apiInstance.swipeWithHttpInfo(body, leftorright);
                        int newStatusCode = newResponse.getStatusCode();
                        if (newStatusCode >= 200 && newStatusCode < 300) {
                            retrySuccess = true;
                            break;
                        }
                    } catch (ApiException ex) {
                        continue;
                    }
                }
                if (retrySuccess) {
                    try {
                        buffer.put("success");
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        buffer.put("fail");
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        countDownLatchWatch.countDown();
    }
}
