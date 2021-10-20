一个题目收录工具，为了应对题目排版、做题方式花样繁多，另一个用Egret做的做题页面适配不过来的问题，采用了录题人员直接在触摸屏上对着题目图片先做一遍题目记录下笔迹数据再和学生的笔迹进行位置匹配然后手写识别判断答案的做法。   

现在支持收录操作的题型：填空题、连线题、竖式题、递等式题、尺规作图题，随着题型增加，收录操作也越来越繁琐，填空连线题混合，连线题需要多余的对象，递等式手机屏幕手写困难并且手写识别跟不上，尺规作图题则是完全想不出来手机上体验良好的做题方式，最后只是做了个初版效果没有实际使用上。


-填空题手写识别部分   
做法和[MnistSerivice](https://github.com/CloudTsang/MnistService)一样将笔迹点比例缩小至28x28再还原成图片，避免直接放缩导致图片变糊，在手机上借助了tflite读取并使用识别模型。

    package com.meili.mnist.mnist;

    private final Interpreter mInterpreter;
    public YdMnistClassifierLite(AssetManager manager) {
        MappedByteBuffer mbb = null;
        Interpreter tmpItpt = null;
        try{
            AssetFileDescriptor fileDescriptor = manager.openFd(TF.YD_MODEL_LITE);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            tmpItpt = new Interpreter(mbb);
        }catch (Exception err){
            err.printStackTrace();
        }
        mInterpreter = tmpItpt;
    }

    public MnistData inference(ByteBuffer input){
        float[][] mResult = new float[1][TF.labels.length()];
        if(mInterpreter != null){
            try{
                mInterpreter.run(input, mResult);
            }catch (Exception err){
                err.printStackTrace();
            }
        }
        return new MnistData(mResult[0]);
    }



keras的.h5模型转换成tflite的.tflite模型的代码，由于tensorflow版本等各种问题，放在了[google drive](https://colab.research.google.com/drive/1IUIn9ffk5ICKujqPyuGaHL2irQ9Wmtpm)上运行。



- 竖式题    
有竖式填空功能app有好几个，算式转竖式的算法倒是没怎么在网上找到==自行写个工具类`com.meili.mnist.widget.VertEqCreator`可以输入算式生成直观竖式格式数据。
![图](https://raw.githubusercontent.com/CloudTsang/QRecorder/main/screenshot1.png)
![图](https://raw.githubusercontent.com/CloudTsang/QRecorder/main/screenshot2.png)
这个工具类后来为了让Egret使用，重新写了一次，在文件`verticalHandler.ts`里。
使用方法



        let veq = VerticalHandler.generate('123*123');


- 尺规作图题  
想不到合适的方案导致最终操作方式非~常怪异的尺规作图题，仅支持作高、平行线类的题目，判断答案大概可以用端点坐标和画线斜率的模糊匹配？   
垂线、平行线的实现回忆着初中的y=kx+b写了一些计算的代码在`com.meili.mnist.widget.PathCalculator`这里


![图](https://raw.githubusercontent.com/CloudTsang/QRecorder/main/screenshot3.png)