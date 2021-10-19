package com.meili.mnist.mnist;

import com.meili.mnist.TF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author zijiao
 * @version 17/8/2
 */
public class MnistData {

    private final List<Item> items = new ArrayList<>(TF.labels.length());

    public MnistData(float[] data) {
        for (int i = 0; i < data.length; i++) {
            items.add(new Item(data[i], i));
        }
        Collections.sort(items);
    }

    public String top(int topSize) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < topSize; i++) {
            Item item = items.get(i);
            builder.append(item.index)
                    .append(": ")
                    .append(String.format(Locale.getDefault(), "%.1f%%", item.value * 100))
                    .append("\n");
        }
        return builder.toString();
    }

    public String topWithoutValue(int topSize){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < topSize; i++) {
            Item item = items.get(i);
            builder.append((int)item.index);
        }
        return builder.toString();
    }

    public int topIndex(){
        Item item = items.get(0);
        return (int)item.index;
    }

    public String topStr(){
        int idx = topIndex();
        return TF.labels.substring(idx,idx+1);
    }

    public float topValue(){
        Item item = items.get(0);
        return item.value;
    }

    public String output() {
        return String.valueOf(items.get(0).index);
    }

    @Override
    public String toString() {
        return output();
    }

    @SuppressWarnings("NullableProblems")
    private static class Item implements Comparable<Item> {
        final float value;
        final float index;

        private Item(float value, float index) {
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(Item o) {
            return value < o.value ? 1 : -1;
        }
    }

}
