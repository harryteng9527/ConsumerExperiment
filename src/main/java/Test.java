import java.util.ArrayList;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("s");
        list.add("ss");
        list.add("sss");
        list.add("ssss");
        list.add("sssss");
        list.add("ssssss");
        list.forEach((s) -> {
            System.out.println(s);
        });
        System.out.println(list.size());
        list.remove(4);
        list.remove(2);
        list.forEach((s) -> {
            System.out.println(s);
        });
        System.out.println(list.size());
        ArrayList<MultipleThreadConsumer> multipleThreadConsumers = new ArrayList<>();
        multipleThreadConsumers = Utility.createConsumers(3, true);
        multipleThreadConsumers.forEach((MultipleThreadConsumer consumer) ->
                consumer.start());
        Utility.findConsumerGroup();
    }

}
