import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Utility {
    private static final String bootstrapServer = "192.168.103.68:11873";
    private static final String groupId = "wllll";

    private static String pattern = "hoo.*|test*.*";
    public static Map<Integer, ArrayList<DownTime>> timeMap = new HashMap<>();

    /**
     * Create a single consumer instance.
     * */
    private static KafkaConsumer<String, String> createConsumer() {
        Properties prop = new Properties();
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(prop);

        return consumer;
    }

    /**
     * Create an ArrayList of `MultipleThreadConsumer` via `createConsumer()`
     * and subscribe topics for those `MultipleThreadConsumer`
     * Subscribe is supporting pattern subscribe and collection one.
     * */
    public static ArrayList<MultipleThreadConsumer> createConsumers(int threadNumber, boolean isPattern) {
        ArrayList<MultipleThreadConsumer> consumers = new ArrayList<>();
        Set<String> topics = findTopics();
        Pattern patternTopics = patternTopics();
        for(int i = 0; i < threadNumber; i++) {
            consumers.add(new MultipleThreadConsumer(i, createConsumer()));
            if(isPattern)
                consumers.get(i).doSubscribe(patternTopics);
            else
                consumers.get(i).doSubscribe(topics);
        }
        return consumers;
    }

    /**
     * create a new MultipleThreadConsumer and support subscribe pattern or collection
     * */
    public static MultipleThreadConsumer startUpConsumer(int id, boolean isPattern) {
        KafkaConsumer<String, String> c = createConsumer();
        Set<String> topics = findTopics();
        MultipleThreadConsumer consumer = new MultipleThreadConsumer(id, c);

        if(!isPattern)
            consumer.doSubscribe(topics);
        else
            consumer.doSubscribe(patternTopics());

        return consumer;
    }

    /**
     * List all topics in the Kafka cluster
     * */
    public static Set<String> findTopics() {
        Set<String> topics = null;
        try (AdminClient adminClient = createAdminClient()) {
            topics = adminClient.listTopics().names().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }
    public static Pattern patternTopics() {
        //Pattern pattern = Pattern.compile("test.*");
        Pattern p = Pattern.compile(pattern);
        return p;
    }

    public static AdminClient createAdminClient() {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer));
    }
/*
    public static void describeTopic(String topic) {
        Set<String> setTopic = new HashSet<>();
        try (AdminClient adminClient = createAdminClient()) {
            setTopic.add(topic);
        };
        System.out.println(admin.describeTopics(setTopic).toString());
    }
*/
    /* TODO */
    public static void findCoordinator() {
        AdminClient adminClient = createAdminClient();
    }

    /* consumer group's member info : groupId, members, isSimpleConsumerGroup(?)*/
    public static void findConsumerGroup() {
        List<String> groupIds = new ArrayList<>();
        groupIds.add(groupId);
        try (AdminClient adminClient = createAdminClient()) {
            System.out.println(adminClient.describeConsumerGroups(groupIds).all().get().toString());
        } catch (Exception e) {
            System.out.println("Exception = " + e);
        }
    }

    /**
     * for each memberId , printout every generation's downtime of consumer group.
     */
    public static void printTimes() {
        timeMap.forEach((memberId, downTimes) -> {
            System.out.println("consumer #" + memberId);
            downTimes.forEach((downTime -> System.out.println(downTime)));
        });
    }
}
