package com.open.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Fixbuglib {

	public static void main(String[] args) throws IOException {
		if (args.length < 6) {
			throw new IllegalArgumentException(
					"参数不正确\n Format: fixbug.bat -f newClasses.jar -t oldClasses.jar -o patch.jar");
		}
		String newClasses = "";
		if ("-f".equals(args[0].trim())) {
			newClasses = args[1].trim();
		} else {
			throw new IllegalArgumentException(
					"参数不正确:-f\n Format: fixbug.bat -f newClasses.jar -t oldClasses.jar -o patch.jar");
		}
		String oldClasses = "";
		if ("-t".equals(args[2].trim())) {
			oldClasses = args[3].trim();
		} else {
			throw new IllegalArgumentException(
					"参数不正确:-t\n Format: fixbug.bat -f newClasses.jar -t oldClasses.jar -o patch.jar");
		}
		String outClasses = "";
		if ("-o".equals(args[4].trim())) {
			outClasses = args[5].trim();
		} else {
			throw new IllegalArgumentException(
					"参数不正确:-o\n Format: fixbug.bat -f newClasses.jar -t oldClasses.jar -o patch.jar");
		}

		HashMap<String, String> classNameToMD5 = new HashMap<>();
		try {
			File file = new File(oldClasses);
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> jaEnumeration = jarFile.entries();
			while (jaEnumeration.hasMoreElements()) {
				JarEntry jarEntry = jaEnumeration.nextElement();
				String className = jarEntry.getName();
				MessageDigest digests = MessageDigest.getInstance("MD5");
				InputStream is = jarFile.getInputStream(jarEntry);
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf, 0, 1024)) > 0) {
					digests.update(buf, 0, len);
				}
				is.close();
				BigInteger bi = new BigInteger(1, digests.digest());
				String result = bi.toString(16);
				classNameToMD5.put(className, result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		ArrayList<String> classNameList = new ArrayList<>();
		HashMap<String, JarEntry> classNameToJar2 = new HashMap<>();
		File file = new File(newClasses);
		JarFile jarFile = new JarFile(file);
		HashMap<String, String> patchDirMap = new HashMap<>();
		int patchFileCount = 0;
		try {
			Enumeration<JarEntry> jaEnumeration = jarFile.entries();
			System.out.println("Modify class file start:");
			System.out.println("------------------------\n");
			while (jaEnumeration.hasMoreElements()) {
				JarEntry jarEntry = jaEnumeration.nextElement();
				String className = jarEntry.getName();
				classNameToJar2.put(className, jarEntry);
				classNameList.add(className);
				MessageDigest digests = MessageDigest.getInstance("MD5");
				InputStream is = jarFile.getInputStream(jarEntry);
				BufferedInputStream buffff = new BufferedInputStream(is);
				buffff.mark(1024 * 100);
				byte[] buf = new byte[1024];
				int len;
				while ((len = buffff.read(buf, 0, 1024)) > 0) {
					digests.update(buf, 0, len);
				}
				BigInteger bi = new BigInteger(1, digests.digest());
				String result = bi.toString(16);

				String md5_1 = classNameToMD5.get(className);
				if (md5_1 == null || !md5_1.equals(result)) {
					String dir = className;
					int index = dir.lastIndexOf("/");
					String dir2 = dir.substring(0, index);
					File dirFile = new File(dir2);// "patch/" +
					if (!dirFile.exists()) {
						dirFile.mkdirs();
					}
					File diffFile = new File(dirFile, dir.substring(index + 1));
					FileOutputStream fos = new FileOutputStream(diffFile);
					try {
						buffff.reset();
					} catch (Exception e) {
						buffff = new BufferedInputStream(
								jarFile.getInputStream(jarEntry));
					}
					while ((len = buffff.read(buf, 0, 1024)) > 0) {
						fos.write(buf, 0, len);
					}
					fos.flush();
					fos.close();

					System.out.println(dir);
					patchFileCount++;
					String patchKey = dir;
					if (dir.indexOf("/") != -1) {
						patchKey = dir.substring(0, dir.indexOf("/"));
					}
					if (patchDirMap.get(patchKey) == null) {
						patchDirMap.put(patchKey, patchKey + "/*");
					}
				}
				is.close();
				buffff.close();
			}
			System.out.println("\n------------------------");
			System.out.println("Modify class file end.");
			System.out.println("总共Patch文件个数   : " + patchFileCount);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String cmd = "cmd /k jar cvf " + outClasses;
		for (Entry<String, String> entry : patchDirMap.entrySet()) {
			cmd += " " + entry.getValue();
		}
		Runtime.getRuntime().exec(cmd);
	}
}
