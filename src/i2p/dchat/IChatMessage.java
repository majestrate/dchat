package i2p.dchat;

public interface IChatMessage {


	
	IPeerInfo getPeerInfo();
	void setPeerInfo(IPeerInfo info);
	String getText();
	void setMessage(String message);
	ChatMessageType getType();
	void setType(ChatMessageType type);
	byte[] serialize();
	
}
