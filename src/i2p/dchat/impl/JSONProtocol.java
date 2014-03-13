package i2p.dchat.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import i2p.dchat.ChatMessageType;
import i2p.dchat.IChatProtocol;
import i2p.dchat.IPeerInfo;

public class JSONProtocol implements IChatProtocol {

	private static final String KEY_TEXT = "DATA";
	private static final String KEY_TYPE = "TYPE";
	
	
	
	public byte [] buildMessage(ChatMessageType type, String text) {
		JSONObject jobj = new JSONObject();
		jobj.put(KEY_TEXT, text);
		jobj.put(KEY_TYPE, type.name());
		return jobj.toString().getBytes();
	}
	
	@Override
	public byte[] buildMessage(ChatMessageType type, List<String> ls) {
		JSONObject jobj = new JSONObject();
		jobj.put(KEY_TEXT, ls);
		jobj.put(KEY_TYPE, type.name());
		return jobj.toString().getBytes();
	}
	
	public List<String> extractList(byte [] bytes) {
		try {
			String str = new String(bytes,"UTF-8");
			JSONArray jarr = new JSONObject(str).getJSONArray(KEY_TEXT);
			List<String> ls = new ArrayList<String>();
			int j_len = jarr.length();
			for(int ind = 0; ind < j_len; ind ++ ){
				ls.add(jarr.getString(ind));
			}
			return ls;
		} catch (UnsupportedEncodingException thrown) {
			throw new RuntimeException(thrown);
		}
	}
	
	@Override
	public String extractString(byte[] bytes) {
		try {
			String str = new String(bytes,"UTF-8");
			return new JSONObject(str).getString(KEY_TEXT);
		} catch (UnsupportedEncodingException thrown) {
			throw new RuntimeException(thrown);
		}
	}

	@Override
	public ChatMessageType extractType(byte[] bytes) {
		try {
			String str = new String(bytes,"UTF-8");
			String type = new JSONObject(str).getString(KEY_TYPE);
			return ChatMessageType.valueOf(type);
		} catch (UnsupportedEncodingException thrown) {
			throw new RuntimeException(thrown);
		}
	}


}
