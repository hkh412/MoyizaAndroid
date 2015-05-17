package com.hkh.myz.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hkh.myz.config.Links;
import com.hkh.myz.data.DataHashMap;

public class ParseUtil {
	String TAG = ParseUtil.class.getSimpleName();

	public static ArrayList<DataHashMap> parseBoardJson(String jsonstr) {
		ArrayList<DataHashMap> list = new ArrayList<DataHashMap>();
		DataHashMap map = null;
		try {
			JSONObject result = new JSONObject(jsonstr);
			if (result.getInt("suc") == 1) {
				JSONArray data = result.getJSONArray("data");
				for (int i=0; i<data.length(); i++) {
					JSONObject json = data.getJSONObject(i);
					map = new DataHashMap();
					String title = json.optString("title");
					if (title.length() == 0 || title.equals("null")) {
						// title 없는 게시글 무시
						continue;
					}
					
					map.put("postNo", json.optString("no"));
					map.put("title", json.optString("title"));
					map.put("content", json.optString("content"));
					map.put("authorNm", json.optString("nick"));
					map.put("authorImg", json.optString("thumb"));
					map.put("date", json.optString("date"));
					map.put("link", Links.MOBILE_BBS+json.optString("url"));
					map.put("cmtCnt", json.optString("comment"));
					map.put("viewCnt", json.optString("hit"));
					map.put("voteCnt", json.optString("vote"));
					map.put("category", json.optString("category"));
					map.put("region", json.optString("region_name"));
					
					list.add(map);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 일반 게시판 파싱
	 * @param rows
	 * @return
	 */
	public static ArrayList<DataHashMap> parseBoardElements(List<Element> rows) {
		ArrayList<DataHashMap> list = new ArrayList<DataHashMap>();
		DataHashMap map = null;
		for (Iterator<Element> iterator=rows.iterator(); iterator.hasNext();) {
			Element row = iterator.next();
			map = new DataHashMap();
			
			String postNo = getPostNo(row);
			String authorNm = row.select("span.nick").text();
			String authorImg = null;
			Elements authorElms = row.select("div.icon.human img");
			if (authorElms.size()>0) {
				authorImg = authorElms.get(0).attr("src");
			}
			
			String region = row.select("a span.title-region").text();
			row.select("a span.title-region").remove();
			String title = row.select("a span.title").text();
			String content = row.select("a p.content").text();
			String date = row.select("a span.date").text();
			String link = row.select("a.link").attr("href");
			String viewCnt = row.select("a span.view").text();
			String cmtCnt = row.select("a span.comment").text();
			String voteCnt = row.select("a span.vote").text();
			String category = row.select("a span.category").text();
			
			if (title.length() == 0 || title.equals("null")) {
				// title 없는 게시글 무시
				continue;
			}
			
			map.put("postNo", postNo);
			map.put("title", title);
			map.put("content", content);
			map.put("authorNm", authorNm);
			map.put("authorImg", authorImg);
			map.put("date", date);
			map.put("link", Links.MOBILE_BBS+link);
			map.put("cmtCnt", cmtCnt);
			map.put("viewCnt", viewCnt);
			map.put("voteCnt", voteCnt);
			map.put("category", category);
			map.put("region", region);
			list.add(map);
		}
		return list;
	}
	
	/**
	 * list 형식의 사진 갤러리 화면 파싱로직 (회원사진첩, 자작요리)
	 * @param rows
	 * @return
	 */
	public static ArrayList<DataHashMap> parseGalleryElementsByList(List<Element> rows) {
		ArrayList<DataHashMap> list = new ArrayList<DataHashMap>();
		DataHashMap map = null;
		for (Iterator<Element> iterator=rows.iterator(); iterator.hasNext();) {
			Element row = iterator.next();
			map = new DataHashMap();
			
			Elements spanTitle = row.select("td.field_title span.title");
			String title = spanTitle.text();
			String link = spanTitle.select("a.document_title").attr("href");
			String postNo = Util.getValueFromUrl(link, "document_srl");
			String mobileLink = Links.MOBILE_BBS
					+"/"+Util.getValueFromUrl(link, "mid")
					+"/"+Util.getValueFromUrl(link, "document_srl");
			
			String date = row.select("td.field_reg").text();
			Element spanAuthor = row.select("td.field_author span.nick_area").get(0);
			String authorNm = spanAuthor.text();
			if (spanAuthor.children().size() > 0) {
				authorNm = spanAuthor.select("img").attr("title");
			}
			
			String thumbUrl = row.select("td.field_title span.thumb img").attr("src");
//			thumbUrl = thumbUrl.replace("60x60", "120x120");
			
			map.put("authorNm", authorNm);
			map.put("postNo", postNo);
			map.put("title", title);
			map.put("date", date);
			map.put("link", mobileLink);
			map.put("imgUrl", Links.DESKTOP_BBS+thumbUrl);
			list.add(map);
		}
		return list;
	}
	
	/**
	 * Tile 형식의 사진 갤러리 화면 파싱로직 (사진방)
	 * @param rows
	 * @return
	 */
	public static ArrayList<DataHashMap> parseGalleryElementsByTile(List<Element> rows) {
		ArrayList<DataHashMap> list = new ArrayList<DataHashMap>();
		DataHashMap map = null;
		for (Iterator<Element> iterator=rows.iterator(); iterator.hasNext();) {
			Element row = iterator.next();
			map = new DataHashMap();

			Element spanAuthor = row.select("span.nick_area").get(0);
			String authorNm = spanAuthor.text();
			if (spanAuthor.children().size() > 0) {
				authorNm = spanAuthor.select("img").attr("title");
			}
			
			Elements titleTag = row.select("h3 a");
			String title = titleTag.text();
			int splitIndex = title.indexOf("(");
			if (splitIndex > 0) {
				title = title.substring(0, splitIndex);
			}
			String link = titleTag.attr("href");
			String postNo = Util.getValueFromUrl(link, "document_srl");
			String date = row.select("span.date").text();
			String mobileLink = Links.MOBILE_BBS
					+"/"+Util.getValueFromUrl(link, "mid")
					+"/"+Util.getValueFromUrl(link, "document_srl");
			
			String thumbUrl = row.select("div.img img").attr("src");
			
			map.put("postNo", postNo);
			map.put("authorNm", authorNm);
			map.put("title", title);
			map.put("date", date);
			map.put("link", mobileLink);
			map.put("imgUrl", Links.DESKTOP_BBS+thumbUrl);
			list.add(map);
		}
		return list;
	}
	
	/**
	 * 일반 게시판 data-no 파싱
	 * @param row
	 * @return
	 */
	private static String getPostNo(Element row) {
		return row.attr("data-no");
	}
	
	/**
	 * 개인정보 페이지 파싱
	 * @param body
	 * @return
	 */
	public static DataHashMap parsePersonalInfo(Element body) {
		DataHashMap map = new DataHashMap();
		String nickName = body.select("div.user-nick span").text();
		String userLevel = body.select("div.user-level").text();
		String articleCnt = "";
		String commentCnt = "";
		String userPoint = "";
		Elements tds = body.select("div.my-stat tbody td");
		if (tds.size() >= 3) {
			articleCnt = tds.get(0).text();
			commentCnt = tds.get(1).text();
			userPoint = tds.get(2).text();
		}
		
		// 사용자 데이터 저장
		map.put("nick-name", nickName);
		map.put("user-level", userLevel);
		map.put("article-cnt", articleCnt);
		map.put("comment-cnt", commentCnt);
		map.put("user-point", userPoint);
		return map;
	}
}
