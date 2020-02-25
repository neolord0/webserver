package kr.dogfoot.webserver.httpMessage.header;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class LongTest {
    public static void main(String[] args) {
        Long a = new Long(1234);
        Long b = new Long(1234);
        Long c = new Long(4321);
        System.out.println(a == b);
        System.out.println(a.equals(b));
        sort();
    }

    public static void sort() {
        TreeSet<Item> set = new TreeSet<Item>(new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (o2.a() > o1.a()) {
                    return 1;
                } else if (o2.a() < o1.a()) {
                    return -1;
                }
                return 0;
            }
        });

        set.add(new Item(12));
        set.add(new Item(15));
        set.add(new Item(3));
        set.add(new Item(23));
        set.add(new Item(5));
        set.add(new Item(16));
        set.add(new Item(34));
        set.add(new Item(65));

        HashSet<Item> removings = new HashSet<Item>();
        for (Item item : set) {
            if (item.a() / 10 == 1) {
                removings.add(item);
            }
        }
        for (Item removing : removings) {
            set.remove(removing);
        }

        for (Item item : set) {
             System.out.println(item);
        }
    }


    public static class Item {
        long a;
        String b;

        public Item(long a) {
            this.a = a;
            b = "[" + a + "]";
        }

        public long a() {
            return a;
        }

        public String toString() {
            return b;
        }
    }
}
