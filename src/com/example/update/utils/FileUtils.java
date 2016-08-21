package com.example.update.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.MessageDigest;

import com.example.update.ZipUpdateService;
import com.example.update.project.IProject;

import android.os.Handler;
import android.util.Log;

public class FileUtils {
	private String srcMd5 = null;
	private long totalLen = 0;
	private long curLen = 0;
	private Handler mHandler = null;
	public FileUtils(Handler handler)
	{
		this.mHandler = handler;
	}
	public void copy(final String path)
	{
		new Thread(){
			public void run() {
				try {
					_onCopyStart();
					byte buf[] = new byte[115200];
					File fileSrcMd5 = new File(path, IProject.FILE_MD5);
					File fileDstMd5 = new File(IProject.PATH_INTERNEL, IProject.FILE_MD5);
					File fileSrcUpdate = new File(path, IProject.FILE_UPDATE);
					totalLen = fileSrcUpdate.length();
					_onCopyStart();
					if(!fileSrcMd5.exists() || !fileSrcUpdate.exists())
					{
						_onCopyEnd("升级文件不存在！");
						return;
					}
					if(fileDstMd5.exists())
						fileDstMd5.delete();
					fileDstMd5.createNewFile();
					FileInputStream fis = new FileInputStream(fileSrcMd5);
					FileOutputStream fos = new FileOutputStream(fileDstMd5);
					int len = fis.read(buf);
					fos.write(buf, 0, len);
					fos.flush();
					fos.close();
					fis.close();
					/////////////////////////////////
					File fileDstUpdate = new File(IProject.PATH_INTERNEL, IProject.FILE_UPDATE);
					if(fileDstUpdate.exists())
						fileDstUpdate.delete();
					fileDstUpdate.createNewFile();
					fis = new FileInputStream(fileSrcUpdate);
					fos = new FileOutputStream(fileDstUpdate);
					while((len = fis.read(buf)) != -1)
					{
						fos.write(buf, 0, len);
						curLen += len;
						_onCopyProgress();
						System.out.println("len:" + curLen);
					}
					fos.flush();
					fos.close();
					fis.close();
					_onCopyEnd(null);
				} catch (Exception e) {
					e.printStackTrace();
					_onCopyEnd("拷贝软件出错！");
				}
			};
		}.start();
	}
	public void check(String path)
	{
		new Thread(){
			public void run() {
				try {
					File fileUpdate = new File(IProject.PATH_INTERNEL, IProject.FILE_UPDATE);
					File FileMd5 = new File(IProject.PATH_INTERNEL, IProject.FILE_MD5);
					if(!fileUpdate.exists() || !FileMd5.exists())
					{
						_onCheckEnd("校验文件不存在！");
						return ;
					}
					totalLen = fileUpdate.length();
					curLen = 0;
					_onCheckStart();
					FileReader fr = new FileReader(FileMd5);
					BufferedReader br = new BufferedReader(fr);
					String strMd5 = br.readLine();
					br.close();
					fr.close();
					Log.d(ZipUpdateService.TAG, "lineMd5:" + strMd5);
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					
					FileInputStream fis = new FileInputStream(fileUpdate);
					byte buf[] = new byte[115200];
					int len = -1;
					while((len = fis.read(buf)) != -1)
					{
						md5.update(buf, 0, len);
						curLen += len;
						_onCheckProgress();
					}
					String calMd5 = toHexString(md5.digest());
					Log.d(ZipUpdateService.TAG, "calMd5 :" + calMd5);
					_onCheckEnd(strMd5.contains(calMd5) ?  null : "校验失败!");
				} catch (Exception e) {
					e.printStackTrace();
					_onCheckEnd("校验出错！");
				}
			};
		}.start();
	}
	private char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f' };
	public String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}
	private void _onCopyEnd(final String msg)
	{
		if(mHandler != null && mOnCopyListener != null)
		{
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mOnCopyListener.onCopyEnd(msg);
				}
			});
		}
	}
	private void _onCopyStart()
	{
		if(mHandler != null && mOnCopyListener != null)
		{
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mOnCopyListener.onCopyStart(totalLen);
				}
			});
		}
	}
	private void _onCopyProgress()
	{
		if(mHandler != null && mOnCopyListener != null)
		{
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mOnCopyListener.onCopyProgress(curLen);
				}
			});
		}
	}
	OnCopyListener mOnCopyListener;
	public void setOnCopyListener(OnCopyListener onCopyListener)
	{
		this.mOnCopyListener = onCopyListener;
	}
	OnCheckListener mOnCheckListener;
	public void setOnCheckListener(OnCheckListener onCheckListener)
	{
		this.mOnCheckListener = onCheckListener;
	}
	public interface OnCopyListener
	{
		void  onCopyStart(long length);
		void  onCopyProgress(long progress);
		void onCopyEnd(String  msg);
	}
	private void _onCheckStart()
	{
		if(mHandler != null && mOnCheckListener!= null)
		{
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mOnCheckListener.onCheckStart(totalLen);
				}
			});
		}
	}
	private void _onCheckProgress()
	{
		if(mHandler != null && mOnCheckListener!= null)
		{
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mOnCheckListener.onProgress(curLen);
				}
			});
		}
	}
	private void _onCheckEnd(final String msg)
	{
		if(mHandler != null && mOnCheckListener!= null)
		{
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mOnCheckListener.onEnd(msg);
				}
			});
		}
	}
	public interface OnCheckListener
	{
		void onCheckStart(long length);
		void onProgress(long progress);
		void onEnd(String  msg);
	}
}
