package com.example.update.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;

import android.util.Log;

public class UpdateUtils {
	public final static int UPDATE_OS = 1;
	public final static int UPDATE_ALL = 2;

	public static void update(int flag) {
		File file = new File("/tmp/yftech_run_update");
		try {
			if (file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write("sync \n");
			switch (flag) {
			case UPDATE_OS:
				fw.write("echo \"--yfupdate\" > /cache/recovery/command \n");
				fw.write("reboot recovery \n");
				break;
			case UPDATE_ALL:
				fw.write("echo \"--yfupdate\" > /cache/recovery/command \n");
				fw.write("udiskupgrade \n");
				fw.write("reboot recovery \n");
				break;
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
		}
	}

	public static void reboot(String... cmd) {
		Process process = null;
		DataOutputStream os = null;
		try {
			Log.d("shufu", "reboot cmd");
			process = Runtime.getRuntime().exec("su \n"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			for (String strCmd : cmd) {
				os.writeBytes(strCmd + " \n");
			}
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}

	public static void rebootRecovery() {
		Process process = null;
		DataOutputStream os = null;
		try {
			Log.d("shufu", "reboot recovery");
			process = Runtime.getRuntime().exec("su \n"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("reboot  recovery \n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}

	// public static boolean checkMD5(String filePath, String md5Path) {
	//
	// String updateFileMd5 = Utils.exec("md5 " + filePath);
	// if (updateFileMd5 != null) {
	// updateFileMd5 = updateFileMd5.split(" ")[0];
	// } else {
	// return false;
	// }
	// Log.i(ZipUpdateService.TAG, filePath + ",md5 code:" + updateFileMd5);
	//
	// String md5File = Utils.getTextFileLine(md5Path, null);
	// if (md5File != null) {
	// md5File = md5File.split(" ")[0];
	// } else {
	// return false;
	// }
	// Log.i(ZipUpdateService.TAG, md5Path + ",md5 code:" + md5File);
	// return updateFileMd5.trim().equals(md5File.trim());
	// }
	public static boolean rebootForUdiskUpgrade() {
		Process process = null;
		DataOutputStream os = null;
		try {
			Log.d("shufu", "rebootForUdiskUpgrade");
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("udiskupgrade \n");
			os.writeBytes("reboot \n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}
}
