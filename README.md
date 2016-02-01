FixBug
===

概述
---
基于ClassLoader的Android热修复框架。

原理
---
1.参考 腾讯bugly博客：[【新技能get】让App像Web一样发布新版本](http://bugly.qq.com/blog/?p=781 "FixBug")

2.Google提供的Android分包原理

> 简答的解说：
在应用启动的Application中把补丁文件的dex文件Elements列表放到Apk的dex文件Elements列表之前，组成一个新的数组并赋值给应用，当应用加载这个类时，就会从数组下表为0的位置开始查找，以此到达修复bug的目的。

> Android的分包原理：
原理类似以上，打包的时候会把dex文件拆分成几个dex，然后全部打包到apk中，当应用运行的时候会把几个dex组合到一起，和上面类似也是组合到一个数组中，然后解决Android中方法数达到最大值65535的限制不能正常打包的问题。

使用方式
---
首先，需要我们`自定义Application`,重写父类Application的`attachBaseContext`方法，在此方法中初始化`FixBug`:<br/>

    	try {
            this.fixBugManage = new FixBugManage(this);//初始化FixBugMange
            this.fixBugManage.init("1.0");//补丁版本号管理
        } catch (FixBugException e) {//FixBug自定义异常，FixBug出现的所有的异常都是FixBugException，只不过内容不同
            e.printStackTrace();
        }


然后，添加补丁文件：<br/>

    fixBugManage.addPatch(patchPath);//patchPath是补丁文件的路径

清空所有的补丁文件：<br/>

    fixBugManage.removeAllPatch();

ps:还有一种清除所有的补丁文件方式就是，`fixBugManage.init("1.0")` 修改初始化位置的版本号

对使用方式不懂得，请参考博客：[Android-FixBug热修复框架的使用及源码分析(不发版修复bug)](http://blog.csdn.net/qxs965266509/article/details/50390325 "这就是你想要的......")

兼容性
---
目前支持Dalvik和Art虚拟机.

##测试通过的机型

###模拟器
---

|机型|系统版本|Api|是否测试通过|
|:----|:-------:|:---:|:----------:|
|Samsung Galaxy S2|2.3.7|10|yes|
|Samsung Galaxy|4.1.1|16|yes|
|Samsung Galaxy S4|4.3|18|yes|
|Samsung Galaxy Note3|4.4.4|19|yes|
|Google Nexus 4|5.0.0|21|yes|
|Google Nexus 5|5.1.0|22|yes|
|Google Nexus 6P|6.0.0|23|yes|



###真机
---
|机型|系统版本|Api|是否测试通过|
|:----|:-------:|:---:|:----------:|
|Samsung GT-I9100|2.3.6|10|yes|
|Xiaomi MI2 |4.1.1|16|yes|
|Samsung GT-I9300|4.1.2|16|yes|
|Nubia NX507J|4.4.2|19|yes|
|Xiaomi HM 1S|4.4.4|19|yes|
|Xiaomi MI3|4.4.4|19|yes|
|Xiaomi MI 4LTE|4.4.4|19|yes|
|HUAWEI SCL-TL00H|5.1.1|22|yes|
|Huawei Nexus 6P|6.0.1|23|yes|

<br/>
其他热修复文章：
---
1. [Alibaba-AndFix Bug热修复框架的使用](http://blog.csdn.net/qxs965266509/article/details/49802429 "qxs965266509")<br/>
2. [Alibaba-AndFix Bug热修复框架原理及源码解析](http://blog.csdn.net/qxs965266509/article/details/49816007 "qxs965266509")<br/>
3. [Alibaba-Dexposed框架在线热补丁修复的使用](http://blog.csdn.net/qxs965266509/article/details/49821413 "qxs965266509")<br/>
4. [Alibaba-Dexposed Bug框架原理及源码解析](http://blog.csdn.net/qxs965266509/article/details/50117137 "qxs965266509")<br/>
