package com.hkh.myz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Element;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.hkh.myz.R;
import com.hkh.myz.data.MenuData;
import com.hkh.myz.data.MoyizaRegion;
import com.hkh.myz.manager.SharedPreferenceManager;

public class Util {

	public static int getCurrentThemeResId(Context context) {
		// 테마설정
		String themeValue = SharedPreferenceManager.getInstance(context).getString("theme");
		int themeResId = R.style.Theme_White;
		if (themeValue != null && themeValue.equals("2")) {
			themeResId = R.style.Theme_Black;
		}
		return themeResId;
	}
	
	public static String getValueFromUrl(String url, String key) {
		String value = null;
		if (url != null) {
			String[] keyValues = url.split("&");
			for (int i=0; i<keyValues.length; i++) {
				String keyValue = keyValues[i];
				if (keyValue.indexOf(key) >= 0) {
					value = keyValue.split("=")[1];
					break;
				}
			}
		}
		return value;
	}
	
	/**
	 * 게시판 Menu 의 목록을 Model (UrlData) 에 담아서 리턴한다.
	 * @return List<UrlData>
	 */
	public static ArrayList<MenuData> loadMenuData(Context context,
			List<MenuData> parentNodes, List<ArrayList<MenuData>> childNodes) {
		AssetManager assetManager = context.getAssets();
		ArrayList<MenuData> urlList = new ArrayList<MenuData>();
		// parentNode, childNode 구성 favorite 추가
		parentNodes.clear();
		childNodes.clear();
		
		Gson gson = new GsonBuilder().create();
		try {
			InputStream in = assetManager.open("menu.json");
			JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
			reader.beginArray();
			ArrayList<MenuData> children = new ArrayList<MenuData>();
			while (reader.hasNext()) {
				MenuData urlMap = gson.fromJson(reader, MenuData.class);
				if (urlMap.getDepth()==0) {
					// parent node
					parentNodes.add(urlMap);
					children = new ArrayList<MenuData>();
					childNodes.add(children);
				} else {
					// child node
					children.add(urlMap);
				}
				urlList.add(urlMap);
			}
			reader.endArray();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("loadUrlMapData: ", e.getMessage());
		}
		
		return urlList;
	}
	
	/**
	 * 모이자 지역정보 로딩 FROM region.json 파일에서
	 * @param context
	 * @return
	 */
	public static ArrayList<MoyizaRegion> loadRegionData(Context context) {
		AssetManager assetManager = context.getAssets();
		ArrayList<MoyizaRegion> regionList = new ArrayList<MoyizaRegion>();
		Gson gson = new GsonBuilder().create();
		try {
			InputStream in = assetManager.open("region.json");
			JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
			reader.beginArray();
			while (reader.hasNext()) {
				MoyizaRegion region = gson.fromJson(reader, MoyizaRegion.class);
				regionList.add(region);
			}
			reader.endArray();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("loadRegionData: ", e.getMessage());
		}
		return regionList;
	}
	
	/**
	 * HTML Element e 하위에 tagName 태그가 존재하는 지 체크
	 * @param e
	 * @param tagName
	 * @return
	 */
	public static boolean checkChildrenTag(Element e, String tagName) {
		boolean hasTag = false;
		for (int i=0; i<e.children().size(); i++) {
			Element child = e.child(i);
			if (child.tagName().equals(tagName)) {
				hasTag = true;
				break;
				
			} else if (child.children().size() > 0) {
				hasTag = checkChildrenTag(child, tagName);
				
			}
		}
		return hasTag;
	}
	
	public static MenuData getMatchedUrlDataByUid(ArrayList<MenuData> menuList, int uid) {
		MenuData matched = null;
		for (MenuData menuData : menuList) {
			if (menuData.getUid() == uid) {
				matched = menuData;
				break;
			}
		}
		return matched;
	}
	
	public static void addUniqueItem(ArrayList<String> list, String str) {
		if (list == null || str == null) {
			return;
		}
		if (str.isEmpty() || list.contains(str)) {
			return;
		}
		list.add(str);
	}
	
	/**
	 * 저장할 수 있는 최대한도 설정
	 * limit을 초과할 경우 FIFO방식으로 첫번째 item을 삭제후 뒤에 추가한다.
	 * @param list
	 * @param str
	 * @param limit
	 */
	public static void addUniqueItem2LimitedSize(ArrayList<String> list, String str, int limit) {
		if (list == null || str == null) {
			return;
		}
		if (str.isEmpty() || list.contains(str)) {
			return;
		}
		if (list.size() >= limit) {
			list.remove(0);
		}
		list.add(str);
//		Collections.sort(list);
	}
	
	/**
	 * Bitmap 이미지를 외장 스토리지에 파일로 저장
	 * @param bitmap
	 */
	public static boolean saveImageToExternalStorage(Context context, Bitmap bitmap, String fname) {
		String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
	    File myDir = new File(root+"/"+context.getString(R.string.image_folder_name));
	    myDir.mkdirs();
	    File file = new File(myDir, fname+".jpg");
	    if (file.exists())
	        file.delete();
	    try {
	        FileOutputStream out = new FileOutputStream(file);
	        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
	        out.flush();
	        out.close();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    // 갤러리에서 보이도록 새로고침 한다.
	    MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
	            new MediaScannerConnection.OnScanCompletedListener() {
	                public void onScanCompleted(String path, Uri uri) {
	                }
	    });
	    
	    return true;
	}
	
	public static Intent reportBugs(Context context) {
		Bitmap bitmap = Util.takeScreenShot(context);
		Util.savePic(bitmap, "reportBug.jpg");

    	Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/email");
        email.putExtra(Intent.EXTRA_EMAIL, new String[] { "rhkdgh412@gmail.com" });
        email.putExtra(Intent.EXTRA_SUBJECT, "모이자 Report_"+new Date().toString());
        email.putExtra(Intent.EXTRA_TEXT, "");
        email.putExtra(Intent.EXTRA_STREAM, 
        		Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/moyiza/" + "reportBug.jpg"));
        
        return email;
	}
	
	public static Bitmap takeScreenShot(Context context) {
		Activity activity = (Activity)context;
		if (activity == null)
			return null;
		
		View view = activity.getWindow().getDecorView();
	    view.setDrawingCacheEnabled(true);
	    view.buildDrawingCache();
	    Bitmap b1 = view.getDrawingCache();
	    Rect frame = new Rect();
	    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
	    int statusBarHeight = frame.top;

	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    int width = metrics.widthPixels;
	    int height = metrics.heightPixels;

	    Bitmap bitmap = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height-statusBarHeight);
	    view.destroyDrawingCache();
	    return bitmap;
	}
	
	public static void savePic(Bitmap b, String strFileName)
	{
		String dirPath = Environment.getExternalStorageDirectory().getPath() + "/moyiza";
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File nFile = new File(dirPath + "/" + strFileName);
		
	    FileOutputStream fos = null;
	    try {
	        fos = new FileOutputStream(nFile);
	        if (null != fos)
	        {
	            b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
	            fos.flush();
	            fos.close();
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
}
