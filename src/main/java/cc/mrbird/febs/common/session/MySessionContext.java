package cc.mrbird.febs.common.session;
/**
 * session管理器 上下文MySessionContext
 * @author weizihao
 *
 */

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;


public class MySessionContext {
	
	private static MySessionContext context;
	
	private Map<String, HttpSession>map;
	
	private MySessionContext() {
		map=new HashMap<>();
	}
	
	/**
	 * 得到MySessionContext对象
	 * @return
	 */
	public static MySessionContext getMySessionContext() {
		if(context==null) {
			return new MySessionContext();
		}
		return context;
	}
	
	//添加
	public synchronized void addSession(HttpSession session) {
		if(session!=null) {
			map.put(session.getId(), session);
		}
	}
	
	//获取session
	public synchronized HttpSession getSession(String sessionID) {
		if(sessionID==null) {
			return null;
		}
		return map.get(sessionID);
	}
	
	//删除
	public synchronized void delSession(HttpSession session) {
		if(session!=null) {
			map.remove(session.getId());
		}
	}
	
	//获取map长度
	public synchronized String getMapSize() {
		return String.valueOf(map.size());
	}
	
	
	
	
	
	
	
	
	
	
	
}
