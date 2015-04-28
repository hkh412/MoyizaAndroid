package com.hkh.myz.fragments;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hkh.myz.PagerActivity;
import com.hkh.myz.R;
import com.hkh.myz.ScaleImageActivity;
import com.hkh.myz.adapter.PostViewAdapter;
import com.hkh.myz.config.HTMLAttrs;
import com.hkh.myz.config.HTMLTags;
import com.hkh.myz.config.Links;
import com.hkh.myz.data.CommentData;
import com.hkh.myz.data.HeaderData;
import com.hkh.myz.data.PostItem;
import com.hkh.myz.events.OnScaleImageViewListener;
import com.hkh.myz.manager.SessionManager;
import com.hkh.myz.manager.TimerManager;
import com.hkh.myz.service.InsertCommentService;
import com.hkh.myz.service.QueryDetailPageService;
import com.hkh.myz.util.Util;

public class BoardDetailFragment extends Fragment 
	implements OnClickListener, OnScaleImageViewListener {

	private final String IMAGE_URL_KEY = "image_url_key";
	
	Context mContext;
	
	InputMethodManager imm;
	
	/**
	 * 상세뷰를 기본이 되는 ListView
	 */
	PullToRefreshListView ptrListView;
	
	/**
	 * 상세뷰 ListView Adapter
	 */
	PostViewAdapter postAdapter;
	
	/**
	 * 댓글 쓰기
	 */
	LinearLayout layoutWriteCmt;
	EditText etComment;
	Button btnSendComment;
	
	/**
	 * 이미지 url 주소
	 */
	ArrayList<String> imageUrls;
	
	/**
	 * 화면구성 데이터
	 */
	ArrayList<PostItem> items;
	
	/**
	 * 게시글  링크
	 */
	String link;
	
	/**
	 * 글 작성자 이미지
	 */
	String authorImg;
	
	/**
	 * 글 작성자
	 */
	String authorNm;
	
	/**
	 * 게시글 번호
	 */
	String postNo;
	
	/**
	 * 게시판 ID
	 */
	String bbsId;
	
	StringBuffer plainText = new StringBuffer();
	
	public BoardDetailFragment() {
		items = new ArrayList<PostItem>();
		imageUrls = new ArrayList<String>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.fragment_board_detail, container, false);
		ptrListView = (PullToRefreshListView) rootView.findViewById(R.id.layout_root);
		ptrListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				requestPage(link);
			}
		});
		registerForContextMenu(ptrListView);
		
		layoutWriteCmt = (LinearLayout) rootView.findViewById(R.id.layout_write_comment);
		etComment = (EditText) rootView.findViewById(R.id.et_comment);
		etComment.setOnFocusChangeListener(new OnFocusChangeListener() {
			/**
			 * 댓글입력 활성화 시 광고뷰를 감춘다
			 */
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showAdView(false);
				}
			}
		});
		btnSendComment = (Button) rootView.findViewById(R.id.btn_send_comment);
		btnSendComment.setOnClickListener(this);
		
		postAdapter = new PostViewAdapter(mContext,
				R.layout.list_item_post, items);
		postAdapter.setOnScaleImageViewListener(this);
		ptrListView.setAdapter(postAdapter);
		
		return rootView;
	}
	
	private void showAdView(boolean isShow) {
		if (isShow) {
			((PagerActivity)getActivity()).showAdView();
		} else {
			((PagerActivity)getActivity()).hideAdView();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		link = bundle.getString("link");
		authorImg = bundle.getString("authorImg");
		postNo = bundle.getString("postNo");
		bbsId = bundle.getString("bbs_id");
		requestPage(link);
	}

	@Override
	public void onScaleImageView(String imageUrl) {
		Intent intent = new Intent(mContext, ScaleImageActivity.class);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList(IMAGE_URL_KEY, imageUrls);
		bundle.putInt("position", imageUrls.indexOf(imageUrl));
		intent.putExtras(bundle);

		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = ((Activity) mContext).getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public void onClick(View view) {
		if (view == btnSendComment) {
			// 글쓰기 Timer reset 여부검사
			boolean isReset = TimerManager.getInstance().isTimerReset();
			if (!isReset) {
				Toast.makeText(mContext,
						mContext.getString(R.string.message_timer_not_reset), Toast.LENGTH_SHORT).show();
				return;
			}
			
			// 글자수 체크
			String comment = etComment.getText().toString().trim();
			if (comment.length() <= 0) {
				Toast.makeText(mContext,
						mContext.getString(R.string.message_alert_no_comment), Toast.LENGTH_SHORT).show();
			} else {
				sendComment(comment);
			}
		}
	}
	
	public void requestPage(String link) {
		((PagerActivity)mContext).showLoadingIndicator();
		
		QueryDetailPageService service = new QueryDetailPageService(mContext);
		service.setService(link, this, "onQueryResult");
		service.request();
	}
	
	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
		items.removeAll(items);
		
		((PagerActivity)mContext).hideLoadingIndicator();
		ptrListView.onRefreshComplete();
		
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
        Document document = Jsoup.parse(html);
        Element body = document.body();
        
        // 자바 스크립트 제거
        body.select("script, jscript").remove();
        Elements comments = body.select("div.comment-list div.comment").clone();
        Elements content = body.select("div.article-right");
        if (content.size() > 0) {
        	Element element = content.get(0);
        	parseHeaderElement(element);
        	
        	Elements conts = element.select("div.article-text");
        	if (conts.size() > 0) {
        		Element cont = conts.get(0);
        		removeTrashElement(cont);
        		parseContentElement(cont);
        	}
        }
        
        /**
         * 댓글 파싱
         */
    	parseCommentElements(comments);
    	
    	/**
    	 * 댓글쓰기 상자 보이기 처리
    	 */
    	refreshWriteCommentView();
        
		// 리스트 display
		postAdapter.notifyDataSetChanged();
		
		showAdView(true);
	}
	
	public void refreshWriteCommentView() {
		// 글쓰기 버튼 refresh
		boolean hasSession = SessionManager.getInstance().hasSession();
		int visibility = hasSession ? View.VISIBLE : View.GONE;
		layoutWriteCmt.setVisibility(visibility);
	}
	
	/**
	 * 모이자 사이트에서 동영상 object 태그가 포함될 경우
	 * 댓글 element 가 컨텐츠 영역으로 포함되는 버그가 있다.
	 * 컨텐츠 파싱하기 전에 댓글 element 를 삭제한다.
	 * @param element
	 */
	private void removeTrashElement(Element element) {
		element.select("div.article-buttons").remove();
		element.select("#common_form").remove();
		element.select("div.comment-list").remove();
		element.select("div.footer-stat").remove();
	}
	
	/**
	 * 헤더 파싱
	 * @param elements
	 */
	private void parseHeaderElement(Element element) {
        HeaderData headerData = new HeaderData();
        headerData.title = element.select(".title").text();
        Elements spans = element.select("div.other span");
        if (spans.size()>0) {
        	headerData.date = spans.get(0).text();
        }
        headerData.viewCnt = spans.select(".view").text();
        headerData.voteCnt = spans.select(".vote").text();
        authorNm = spans.select(".nick").text();
        headerData.authorNm = authorNm;
        headerData.authorImg = authorImg;
        
        PostItem headerItem = new PostItem();
        headerItem.type = PostItem.TYPE_HEADER_VIEW;
        headerItem.headerData = headerData;
        items.add(headerItem);
	}
	
	/**
	 * 컨텐츠 영역 파싱
	 * @param elements
	 */
	private void parseContentElement(Element element) {
		String[] lines = element.html().split("\n");
		for (int i=0; i<lines.length; i++) {
			Element body = Jsoup.parse(lines[i]).body();
			
			// plaintext 인 경우 text buffer에 추가
			int size = body.children().size();
			if (size <= 0) {
				plainText.append(lines[i]);
				continue;
				
			} else if (size == 1) {
				// 하위 노드가 하나뿐인경우 파싱시도
				Element e = body.child(0);
				// 태그 비교
				if (e.tagName().equals(HTMLTags.CENTER)) {
					// recursive
					parseContentElement(e);
				} else if (e.tagName().equals(HTMLTags.IMG)) {
					// 이미지 태그
					createTextView(plainText);
					String imgUrl = e.attr(HTMLAttrs.SRC);
					createImageView(imgUrl);
					
				} else if (e.tagName().equals(HTMLTags.OBJECT)) {
					// 동영상 태그
					createTextView(plainText);
					createVideoView(e);
					
				} else if (e.tagName().equals(HTMLTags.AUDIO)) {
					// 오디오 태그인 경우 WebView로 처리
					createTextView(plainText);
					createAudioView(e);
					
				} else if (e.tagName().equals(HTMLTags.P)) {
					// recursive
					parseContentElement(e);
					
				} else if (e.tagName().equals(HTMLTags.B)) {
					// recursive
					parseContentElement(e);
					
				} else if (e.tagName().equals(HTMLTags.H3)) {
					// recursive
					parseContentElement(e);
					
				} else if (e.tagName().equals(HTMLTags.FONT)) {
					if (e.select("a").size() > 0) {
						createTextView(plainText);
						createHyperLinkView(e.select("a").get(0));
					} else {
						plainText.append(lines[i]);
					}
				} 
				else if (e.tagName().equals(HTMLTags.SPAN)
						&& e.children().size() > 0 
						&& Util.checkChildrenTag(e, HTMLTags.IMG)) {
					// SPAN 하위에 IMG 태그가 있는 경우 img 파싱을 위해 재귀호출한다.
					plainText.append(e.text());
					parseContentElement(e.child(0));
				} 
				else if (e.tagName().equals(HTMLTags.A)) {
					createTextView(plainText);
					createHyperLinkView(e);
					
				} else {
					plainText.append(lines[i]);
					if (plainText.length() > 1500) {
						createTextView(plainText);
					}
				}
				
			} else {
				// 하위노드 1개이상일 경우 재귀호출
				parseContentElement(body);
			}
		} // end for
		createTextView(plainText);
	}
	
	/**
	 * 댓글 파싱
	 * @param json
	 */
	private void parseCommentElements(Elements comments) {
		
		// 댓글수 추가
		String countText = mContext.getString(R.string.comment_count);
		PostItem info = new PostItem();
		info.type = PostItem.TYPE_COMMENT_INFO;
		info.cmtCntText = String.format(countText, comments.size());
		if (comments.size() == 0) {
			info.cmtCntText = mContext.getString(R.string.message_no_comment);
		}
		items.add(info);
		
		// 댓글 추가
		for (Element comment : comments) {
			CommentData cmtData = new CommentData();
			
			cmtData.authorNm = comment.select("div.author span.nick").text();
			cmtData.authorImg = comment.select("div.icon.human img").attr("src");
			cmtData.cmtDate = comment.select("span.comment-date").text();
			cmtData.cmtMemo = comment.select("div.comment-text p").html();
			cmtData.isReply = comment.hasClass("level1");
			
			PostItem item = new PostItem();
			item.type = PostItem.TYPE_COMMENT_VIEW;
			item.cmtData = cmtData;
			item.authorNm = authorNm;
			items.add(item);
		}
	}
	
	private void createTextView(StringBuffer plainText) {
		if (plainText.length() <= 0) {
			return;
		}
		PostItem item = new PostItem();
		item.type = PostItem.TYPE_TEXT_VIEW;
		item.contText = plainText.toString().trim();
		items.add(item);
		plainText.delete(0, plainText.length());
	}
	
	private void createImageView(String imageUrl) {
		Util.addUniqueItem(imageUrls, imageUrl);
		PostItem item = new PostItem();
		item.type = PostItem.TYPE_IMAGE_NO_LINK;
		item.imageUrl = imageUrl;
		items.add(item);
	}
	
	private void createHyperLinkView(Element e) {
		PostItem item = new PostItem();
		Elements children = e.children();
		Element child = null;
		if (children.size() > 0) {
			child = children.get(0);
		}
		if (child != null && child.tagName().equals(HTMLTags.IMG)) {
			item.imageLink = child.attr("src");
			item.type = PostItem.TYPE_IMAGE_WITH_LINK;
		} else {
			item.contText = e.toString();
			item.type = PostItem.TYPE_HREF_LINK;
		}
		item.element = e;
		item.hyperLink = e.attr(HTMLAttrs.HREF);
		items.add(item);
	}
	
	@SuppressWarnings("unused")
	private void createWebView(Element e) {
		PostItem item = new PostItem();
		item.element = e;
		item.type = PostItem.TYPE_WEB_VIEW;
		items.add(item);
	}
	
	private void createAudioView(Element e) {
		PostItem item = new PostItem();
		item.type = PostItem.TYPE_AUDIO_VIEW;
		item.audioLink = e.attr("src");
		items.add(item);
	}
	
	private void createVideoView(Element e) {
		// 동영상 flash player 파싱
		final PostItem item = new PostItem();
		item.type = PostItem.TYPE_VIDEO_VIEW;
		
		String videoUrl = e.select("param[name=movie]").attr("value");
		String flashVars = e.select("param[name=flashvars]").attr("value");
		if (videoUrl.indexOf(Links.DAUM_VIDEO) >= 0) {
			String vidParam = flashVars.split("&")[0];
			String vid = vidParam.split("=")[1];
			String videoLink = "http://tvpot.daum.net/v/"+vid;
			String xmlUrl = "http://videofarm.daum.net/controller/api/open/v1_3/MovieData.apixml";
			AQuery aq = new AQuery(mContext);
			aq.ajax(xmlUrl+"?"+vidParam, String.class, new AjaxCallback<String>(){
				@Override
				public void callback(String url, String object, AjaxStatus status) {
					super.callback(url, object, status);
					Document doc = Jsoup.parse(object, "", Parser.xmlParser());
					String thumbnailUrl = doc.select("thumbnail main").get(0).attr("url");
					item.thumbnailLink = thumbnailUrl;
				}
			});
			item.videoLink = videoLink;
			items.add(item);
		}
	}
	
	/**
	 * 상세 ContextMenu operation 처리
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ClipboardManager cpm = null;
		ClipData clipData = null;
		int id = item.getItemId();
		if (R.id.action_copy_contents == id) {
			// 게시글 내용복사
        	String contents = copyPostContents();
        	cpm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        	clipData = ClipData.newPlainText("post contents", contents);
        	cpm.setPrimaryClip(clipData);
        	Toast.makeText(mContext,
        			mContext.getString(R.string.message_copy_contents_success), Toast.LENGTH_SHORT).show();
            return true;
            
		} else if (R.id.action_view_in_browser == id) {
			// 웹브라우저에서 보기
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			mContext.startActivity(intent);
            return true;
            
		} else if (R.id.action_copy_url == id) {
			// 게시글 링크복사
        	cpm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        	clipData = ClipData.newPlainText("post url", link);
        	cpm.setPrimaryClip(clipData);
        	Toast.makeText(mContext,
        			mContext.getString(R.string.message_copy_url_success), Toast.LENGTH_SHORT).show();
        	return true;
        	
		} else if (R.id.action_propose == id) {
			// 건의하기
			Intent intent = Util.reportBugs(mContext);
            startActivity(Intent.createChooser(intent, "Email 건의 및 버그 신고하기"));
        	return true;
        	
		} else {
            return super.onContextItemSelected(item);
	    }
	}
	
	public void hideSoftKeyboard(Context context) {
		if (context != null) {
			imm = (InputMethodManager)context.getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
		}
	}
	
	/**
	 * 게시글 내용복사
	 */
	private String copyPostContents() {
		StringBuffer sb = new StringBuffer();
		for (PostItem item : items) {
			if (item.type == PostItem.TYPE_TEXT_VIEW) {
				if (item.contText != null && item.contText.length() > 0) {
					sb.append(item.contText);
				}
			} else if (item.type == PostItem.TYPE_HREF_LINK) {
				if (item.hyperLink != null && item.contText.length() > 0) {
					sb.append(item.hyperLink);
				}
			}
		}
		return Html.fromHtml(sb.toString()).toString();
	}
	
	/**
	 * 댓글 전송 서비스
	 * @param comment
	 */
	private void sendComment(String comment) {
		((PagerActivity)mContext).showLoadingIndicator();
		InsertCommentService service = new InsertCommentService(mContext);
		service.setService("", this, "onCommentSent");
		service.setComment(link, comment, bbsId, postNo);
		service.request();
	}
	
	public void onCommentSent(String url, String result, AjaxStatus status) {
		((PagerActivity)mContext).hideLoadingIndicator();
		if (status.getCode() == 200) {
			String errorMsg = "";
			try {
				JSONObject json = new JSONObject(result);
				if (json.has("suc") && json.getInt("suc")==1) {
					// 댓글 작성 성공
					Toast.makeText(mContext,
							mContext.getString(R.string.message_comment_sent),
							Toast.LENGTH_SHORT).show();
					TimerManager.getInstance().setCountDown(30);
					etComment.setText("");
					requestPage(link);
					return;
					
				} else if (json.has("error")) {
					errorMsg = json.getString("msg");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
			
		} else {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_comment_sent_problem),
					Toast.LENGTH_SHORT).show();
		}
	}
}
