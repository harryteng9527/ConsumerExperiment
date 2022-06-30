import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int threadNumber = 15;

    private static boolean isPattern = true;

    public static void main(String[] args) throws InterruptedException {
        Random r = new Random(System.currentTimeMillis());
        ArrayList<MultipleThreadConsumer> multipleThreadConsumers = Utility.createConsumers(threadNumber, isPattern);
        multipleThreadConsumers.forEach((MultipleThreadConsumer consumer) ->
                consumer.start());
        int consumerIds = multipleThreadConsumers.size();
        int taskType = 0;
        TriggerTask trigger = new TriggerTask(multipleThreadConsumers.size());

        TimeUnit.SECONDS.sleep(10);
        trigger.killConsumers(multipleThreadConsumers);

/*
        TimeUnit.SECONDS.sleep(10);
        multipleThreadConsumers.get(4).setEnforce();
        TimeUnit.SECONDS.sleep(100);
  */
        /*
        for(int i = 1; i < 51; i++) {
            System.out.println("round "+i);
            taskType = r.nextInt(4);
            if(taskType == 0) {
                if(multipleThreadConsumers.size() > 1)
                    trigger.killConsumer(multipleThreadConsumers);
                else continue;
            }
            else if(taskType == 1) {
                System.out.println("add consumer"+ consumerIds + " into the group");
                trigger.appendNewConsumer(multipleThreadConsumers, consumerIds, isPattern);
                consumerIds++;
            }
            else if(taskType == 2) {
                trigger.modifyPartitionsCount();*/
                //TODO
                /*
                multipleThreadConsumers.forEach((v) -> {
                    v.setEnforce();
                });
                 */
          /*      multipleThreadConsumers.get(0).setEnforce();
            }

            else if(taskType == 3) { //unsubscribe
                trigger.unsubscribeAndSubscribe(multipleThreadConsumers);
            }

            TimeUnit.SECONDS.sleep(20);
        }
        */

        for(MultipleThreadConsumer consumer : multipleThreadConsumers) {
            consumer.join();
        }
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
