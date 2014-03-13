package i2p.dchat;

import java.util.List;

public interface IChatMessage {


	
	IPeerInfo getPeerInfo();
	void setPeerInfo(IPeerInfo info);
	String getText();
	List<String> getList();
	void setMessage(String message);
	ChatMessageType getType();
	void setType(ChatMessageType type);
	
}
