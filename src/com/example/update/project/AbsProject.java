package com.example.update.project;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.example.update.ZipUpdateService;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public abstract class AbsProject implements IProject {
	private String path;
	private Context mContext;

	public AbsProject(Context context, String path) {
		this.path = path;
		this.mContext = context;
	}

	@Override
	public boolean isOsUpdateFileExist() {
		return new File(path, FILE_UPDATE).exists() && new File(path, FILE_MD5).exists();
	}

	@Override
	public String getOsVersionFromFile() {
		File updateFile = new File(path, FILE_UPDATE);
		if (!updateFile.exists())
			return null;
		ZipFile mZipFile = null;
		try {
			mZipFile = new ZipFile(updateFile);
			for (Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) mZipFile.entries(); entries
					.hasMoreElements();) {
				ZipEntry entry = entries.nextElement();
				Log.d("", "file:" + entry.getName());
				if (entry.getName().equals(FILE_VERSION)) {
					InputStream in = mZipFile.getInputStream(entry);
					File desFile = new File("/tmp", FILE_VERSION + "_update_version.tmp.");
					if (desFile.exists()) {
						desFile.delete();
					}
					byte buffer[] = new byte[2048];
					int realLength = in.read(buffer);
					if (realLength != -1) {
						String ret = new String(buffer, 0, realLength);
						System.out.println("len:" + realLength + "  " + ret);
						return ret;
					}
					in.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getOsVersion() {
		return Build.DISPLAY;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	public boolean isFileExistByName(String dirPath, String fileName) {
		File dir = new File(dirPath);
		if (!dir.exists())
			return false;
		File[] list = dir.listFiles();
		String name = null;
		for (File file : list) {
			name = file.getName();
			if (name.equals(fileName))
				return true;
		}
		return false;
	}

	public String getSubStringFromFileName(String path, String prefix, int start, int subEnd) {
		File[] fileList = new File(path).listFiles();
		if (fileList != null) {
			for (File file : fileList) {
				if (file.isFile()) {
					String name = file.getName().toUpperCase();
					if (name.startsWith(prefix)) {
						return name.substring(start, name.length() - subEnd);
					}
				}
			}
		}
		return null;
	}

	public boolean isFileExistByPrefix(String dirPath, String prefix) {
		File dir = new File(dirPath);
		if (!dir.exists())
			return false;
		File[] list = dir.listFiles();
		if (list == null)
			return false;
		String name = null;
		for (File file : list) {
			name = file.getName().toUpperCase();
			if (name.startsWith(prefix)) {
				Log.d(ZipUpdateService.TAG, "Prefix:" + file.getAbsolutePath());
				return true;
			}
		}
		return false;
	}
}
