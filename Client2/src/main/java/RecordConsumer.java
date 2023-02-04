import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class RecordConsumer implements Runnable{

    private final BlockingQueue<Record> recordBuffer;

    private BufferedWriter writeText;

    public RecordConsumer(BlockingQueue<Record> recordBuffer, String filePath) throws IOException {
        this.recordBuffer = recordBuffer;
        File writeFile = new File(filePath);
        this.writeText = new BufferedWriter(new FileWriter(writeFile));
        this.writeText.write("startTime,requestType,latency,responseCode");
    }
    @Override
    public void run() {
        boolean active = true;
        try {
            while (active) {
                Record curRecord = recordBuffer.take();
                // end condition
                if (curRecord.getStartTime() < 0) {
                    active = false;
                    writeText.flush();
                    writeText.close();
                } else {
                    long startTime = curRecord.getStartTime();
                    String requestType = curRecord.getRequestType();
                    long latency = curRecord.getLatency();
                    int responseCode = curRecord.getResponseCode();
                    // to a new line
                    writeText.newLine();
                    // write into file
                    writeText.write(startTime + "," + requestType + "," + latency +
                            "," + responseCode);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
