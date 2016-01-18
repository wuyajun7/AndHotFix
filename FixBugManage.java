package com.open.mynuwa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * FixBugManage 2015-12-22 下午9:59:28
 *
 * @author 乔晓松 965266509@qq.com
 */
public class FixBugManage {

    private Context context;

    private static final int BUF_SIZE = 2048;

    private File patchs;
    private File patchsOptFile;

    public FixBugManage(Context context) {
        this.context = context;
        this.patchs = new File(this.context.getFilesDir(), "patchs");// 存放补丁文件
        this.patchsOptFile = new File(this.context.getFilesDir(), "patchsopt");// 存放预处理补丁文件压缩处理后的dex文件
    }

    /**
     * 初始化版本号
     *
     * @param versionCode
     */
    public void init(String versionCode) {
        SharedPreferences sharedPreferences = this.context
                .getSharedPreferences("fixbug", Context.MODE_PRIVATE);
        String oldVersionCode = sharedPreferences
                .getString("versionCode", null);
        if (oldVersionCode == null
                || !oldVersionCode.equalsIgnoreCase(versionCode)) {
            this.initPatchsDir();// 初始化补丁文件目录
            this.clearPaths();// 清楚所有的补丁文件
            sharedPreferences.edit().clear().putString("versionCode", versionCode)
                    .commit();// 存储版本号
        } else {
            this.loadPatchs();// 加载已经添加的补丁文件(.jar)
        }
    }

    /**
     * 读取补丁文件夹并加载
     */
    private void loadPatchs() {
        if (patchs.exists() && patchs.isDirectory()) {// 判断文件是否存在并判断是否是文件夹
            File patchFiles[] = patchs.listFiles();// 获取文件夹下的所有的文件
            for (int i = 0; i < patchFiles.length; i++) {
                if (patchFiles[i].getName().lastIndexOf(".jar") == patchFiles[i]
                        .getName().length() - 4) {// 仅处理.jar文件
                    this.loadPatch(patchFiles[i].getAbsolutePath());// 加载jar文件
                }
            }
        } else {
            this.initPatchsDir();
        }
    }

    /**
     * 加载单个补丁文件
     *
     * @param patchPath
     */
    private void loadPatch(String patchPath) {
        try {
            injectAboveEqualApiLevel14(patchPath, patchsOptFile.getAbsolutePath());// 读取jar文件中dex内容
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * patch所在文件目录
     *
     * @param patchPath
     */
    public void addPatch(String patchPath) {
        File inFile = new File(patchPath);
        File outFile = new File(patchs, inFile.getName());
        this.copyFile(outFile, inFile);
        this.loadPatch(patchPath);
    }

    /**
     * 移除所有的patch文件
     */
    public void removeAllPatch() {
        this.clearPaths();
    }

    /**
     * 清除所有的补丁文件
     */
    private void clearPaths() {
        if (patchs.exists() && patchs.isDirectory()) {
            File patchFiles[] = patchs.listFiles();
            for (int i = 0; i < patchFiles.length; i++) {
                if (patchFiles[i].getName().lastIndexOf(".jar") == patchFiles[i]
                        .getName().length() - 4) {
                    patchFiles[i].delete();
                }
            }
        }
    }

    /**
     * 初始化存放补丁的文件目录
     */
    private void initPatchsDir() {
        if (!this.patchs.exists()) {
            this.patchs.mkdirs();
        }
        if (!this.patchsOptFile.exists()) {
            this.patchsOptFile.mkdirs();
        }
    }

    /**
     * 复制文件从inFile到outFile
     *
     * @param outFile
     * @param inFile
     * @return
     */
    private boolean copyFile(File outFile, File inFile) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        try {
            MessageDigest digests = MessageDigest.getInstance("MD5");

            bis = new BufferedInputStream(new FileInputStream(inFile));
            dexWriter = new BufferedOutputStream(new FileOutputStream(outFile));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                digests.update(buf, 0, len);
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            BigInteger bi = new BigInteger(1, digests.digest());
            String result = bi.toString(16);

            File toFile = new File(outFile.getParentFile(), result + ".jar");
            outFile.renameTo(toFile);
            return true;
        } catch (Exception e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }

    public static void patch(Context context, String patchDexFile, String patchClassName) {
        if (patchDexFile != null && new File(patchDexFile).exists()) {
            try {
                if (hasLexClassLoader()) {
                    injectInAliyunOs(context, patchDexFile, patchClassName);
                } else if (hasDexClassLoader()) {
                    injectAboveEqualApiLevel14(context, patchDexFile, patchClassName);
                } else {
                    injectBelowApiLevel14(context, patchDexFile, patchClassName);

                }
            } catch (Throwable th) {
            }
        }
    }

    private static boolean hasLexClassLoader() {
        try {
            Class.forName("dalvik.system.LexClassLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasDexClassLoader() {
        try {
            Class.forName("dalvik.system.BaseDexClassLoader");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void injectInAliyunOs(Context context, String patchDexFile, String patchClassName)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, NoSuchFieldException {
        PathClassLoader obj = (PathClassLoader) context.getClassLoader();
        String replaceAll = new File(patchDexFile).getName().replaceAll("\\.[a-zA-Z0-9]+", ".lex");
        Class cls = Class.forName("dalvik.system.LexClassLoader");
        Object newInstance =
                cls.getConstructor(new Class[]{String.class, String.class, String.class, ClassLoader.class}).newInstance(
                        new Object[]{context.getDir("dex", 0).getAbsolutePath() + File.separator + replaceAll,
                                context.getDir("dex", 0).getAbsolutePath(), patchDexFile, obj});
        cls.getMethod("loadClass", new Class[]{String.class}).invoke(newInstance, new Object[]{patchClassName});
        setField(obj, PathClassLoader.class, "mPaths",
                appendArray(getField(obj, PathClassLoader.class, "mPaths"), getField(newInstance, cls, "mRawDexPath")));
        setField(obj, PathClassLoader.class, "mFiles",
                combineArray(getField(obj, PathClassLoader.class, "mFiles"), getField(newInstance, cls, "mFiles")));
        setField(obj, PathClassLoader.class, "mZips",
                combineArray(getField(obj, PathClassLoader.class, "mZips"), getField(newInstance, cls, "mZips")));
        setField(obj, PathClassLoader.class, "mLexs",
                combineArray(getField(obj, PathClassLoader.class, "mLexs"), getField(newInstance, cls, "mDexs")));
    }

    /**
     * 支持api 14 以下
     *
     * @param dexPath
     * @param defaultDexOptPath
     */
    public static void injectDexBelowApiLevel14(String dexPath, String defaultDexOptPath) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        PathClassLoader obj = (PathClassLoader) context.getClassLoader();

        DexClassLoader dexClassLoader =
                new DexClassLoader(dexPath, defaultDexOptPath, dexPath, context.getClassLoader());

        setField(obj, PathClassLoader.class, "mPaths",
                appendArray(getField(obj, PathClassLoader.class, "mPaths"),
                        getField(dexClassLoader, DexClassLoader.class, "mRawDexPath")
                ));
        setField(obj, PathClassLoader.class, "mFiles",
                combineArray(getField(dexClassLoader, DexClassLoader.class, "mFiles"),
                        getField(obj, PathClassLoader.class, "mFiles")));

        setField(obj, PathClassLoader.class, "mZips",
                combineArray(getField(dexClassLoader, DexClassLoader.class, "mZips"),
                        getField(obj, PathClassLoader.class, "mZips")));

        setField(obj, PathClassLoader.class, "mDexs",
                combineArray(getField(dexClassLoader, DexClassLoader.class, "mDexs"),
                        getField(obj, PathClassLoader.class, "mDexs")));
    }

    /**
     * 支持api 14 等于及以上
     *
     * @param dexPath
     * @param defaultDexOptPath
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static void injectAboveEqualApiLevel14(String dexPath, String defaultDexOptPath)
            throws NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException {
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath,
                defaultDexOptPath, dexPath, getPathClassLoader());// 把dexPath文件补丁处理后放入到defaultDexOptPath目录中
        Object baseDexElements = getDexElements(getPathList(getPathClassLoader()));// 获取当前应用Dex的内容
        Object newDexElements = getDexElements(getPathList(dexClassLoader));// 获取补丁文件Dex的内容
        Object allDexElements = combineArray(newDexElements, baseDexElements);// 把当前apk的dex和补丁文件的dex进行合并
        Object pathList = getPathList(getPathClassLoader());// 获取当前的patchList对象
        setField(pathList, pathList.getClass(), "dexElements", allDexElements);// 利用反射设置对象的值
    }

    private static PathClassLoader getPathClassLoader() {
        PathClassLoader pathClassLoader = (PathClassLoader) FixBugManage.class
                .getClassLoader();// 获取类加载器
        return pathClassLoader;
    }

    private static Object getDexElements(Object paramObject)
            throws IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");// 利用反射获取到dexElements属性
    }

    private static Object getPathList(Object baseDexClassLoader)
            throws IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader,
                Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");// 利用反射获取到pathList属性
    }

    /**
     * 此方法是合并2个数组，把补丁dex中的内容放到数组最前，达到修复bug的目的
     *
     * @param firstArray
     * @param secondArray
     * @return
     */
    private static Object combineArray(Object firstArray, Object secondArray) {
        Class<?> localClass = firstArray.getClass().getComponentType();
        int firstArrayLength = Array.getLength(firstArray);
        int allLength = firstArrayLength + Array.getLength(secondArray);
        Object result = Array.newInstance(localClass, allLength);
        for (int k = 0; k < allLength; ++k) {
            if (k < firstArrayLength) {
                Array.set(result, k, Array.get(firstArray, k));
            } else {
                Array.set(result, k,
                        Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }

    private static Object appendArray(Object obj, Object obj2) {
        Class componentType = obj.getClass().getComponentType();
        int length = Array.getLength(obj);
        Object newInstance = Array.newInstance(componentType, length + 1);
        Array.set(newInstance, 0, obj2);
        for (int i = 1; i < length + 1; i++) {
            Array.set(newInstance, i, Array.get(obj, i - 1));
        }
        return newInstance;
    }

    public static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);// 强制反射
        return localField.get(obj);// 获取值
    }

    public static void setField(Object obj, Class<?> cl, String field,
                                Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);// 强制反射
        localField.set(obj, value);// 设置值
    }
}
