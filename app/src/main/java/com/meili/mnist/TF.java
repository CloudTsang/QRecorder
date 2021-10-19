package com.meili.mnist;

import java.util.List;

/**
 * @author zijiao
 * @version 17/8/2
 */
public class TF {
    public static final String VERSION = "1.0.0";
	public static final String MODEL = "file:///android_asset/mnist.pb";
    public static final String YD_MODEL = "file:///android_asset/20190118v4.pb";

    public static final String YD_MODEL_LITE = "model.tflite";
    public static final String labels = " 0123456789+-×÷=≈≠><.()%[]ABCDEF√个百千万亿三四五六七八九零①②③④⑤⑥⑦⑧⑨⑩⑪⑫mπ";
//    public static final String YD_MODEL_LITE = "tf_1.tflite";
//    public static final String labels = "0123456789";

    public static final int MNIST_SIZE = 28;
    public static final int DRAW_SIZE = 56;
    public static final int DRAW_FULL_SIZE = 140;

    public static final int THICKNESS = 20;
    public static final float DRAW_THICKNESS = 1;

    public static final int[] INPUT_TYPE = new int[]{1, 28 * 28};
    public static final String INPUT_NAME = "input";
    public static final String YD_INPUT_NAME = "flatten_input";
    public static final String KEEP_PROB_NAME = "keep_prob";
    public static final String OUTPUT_NAME = "output";
//    public static final String OUTPUT_NAME = "dense_2/Softmax";

    /*public static final String MODEL = "file:///android_asset/mnist-tf1.0.1.pb";

    public static final int[] INPUT_TYPE = new int[]{1, 28 * 28};
    public static final String INPUT_NAME = "input";
    public static final String KEEP_PROB_NAME = "out_softmax";
    public static final String OUTPUT_NAME = "keep_prob_placeholder";*/

}
