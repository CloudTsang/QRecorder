package com.meili.mnist.mnist;
import android.content.res.AssetManager;
import android.util.Log;

import com.meili.mnist.TF;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


public class YdMnistClassifier {
    private final TensorFlowInferenceInterface inference;

    public YdMnistClassifier(AssetManager assetManager) {
        inference = new TensorFlowInferenceInterface();
        inference.initializeTensorFlow(assetManager, TF.YD_MODEL);
    }

    public MnistData inference(float[] input) {
        if (input == null || input.length != 28 * 28) {
            throw new RuntimeException("Input data is error.");
        }
        inference.fillNodeFloat(TF.YD_INPUT_NAME, TF.INPUT_TYPE, input);
        inference.runInference(new String[]{TF.OUTPUT_NAME});
        float[] output = new float[10];
        inference.readNodeFloat(TF.OUTPUT_NAME, output);
        Log.i("mnist:", Arrays.toString(output));
        return new MnistData(output);
    }

}


