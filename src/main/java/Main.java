import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int threadNumber = 8;

    private static boolean isPattern = false;

    public static void main(String[] args) throws InterruptedException {
        Random r = new Random(System.currentTimeMillis());
        ArrayList<MultipleThreadConsumer> multipleThreadConsumers = Utility.createConsumers(threadNumber, isPattern);
        multipleThreadConsumers.forEach((MultipleThreadConsumer consumer) ->
                consumer.start());
        int consumerIds = multipleThreadConsumers.size();
        int taskType = 0;
        TriggerTask trigger = new TriggerTask(multipleThreadConsumers.size());

        for(int i = 0; i < 20; i++) {
            System.out.println("round "+i);
            taskType = r.nextInt(4);
            if(taskType == 0) {
                trigger.killConsumer(multipleThreadConsumers);
            }
            else if(taskType == 1) {
                System.out.println("add consumer"+ consumerIds + " into the group");
                trigger.appendNewConsumer(multipleThreadConsumers, consumerIds, isPattern);
                consumerIds++;
            }
            else if(taskType == 2) {
                trigger.modifyPartitionsCount();
                multipleThreadConsumers.get(0).setEnforce();
            }
            else if(taskType == 3) { //unsubscribe

            }
            TimeUnit.SECONDS.sleep(10);
        }
        /*
        for(MultipleThreadConsumer consumer : multipleThreadConsumers) {
            consumer.join();
        }
 */
        /*
        for(int i = 0; i < 8; i++) {
            System.out.println("round #"+i);
            TimeUnit.SECONDS.sleep(10);
            trigger.killConsumer(multipleThreadConsumers,i);
        }
        for(MultipleThreadConsumer consumer : multipleThreadConsumers)
            consumer.join();
*/
        Utility.printTimes();
    }

}
