package com.example.update.project;

public interface IProject {
	public static final  String PATH_INTERNEL = "/sdcard/update";
	public static final String INTERNEL_UPDATE_PATH = "/sdcard/update";
	public static final String COPY_UPDATE_FILE_SHELL = "update.sh";
	public static  final String FILE_UPDATE = "update.zip";
	public static  final String FILE_MD5 = "update.zip.md5";
	public static  final String FILE_VERSION = "version";
	

	boolean isOsUpdateFileExist();
	String getOsVersionFromFile();
	String getOsVersion();
	String getPath();
	
}
