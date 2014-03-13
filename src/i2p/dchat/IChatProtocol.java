package i2p.dchat;

import java.util.List;


public interface IChatProtocol {

	byte [] buildMessage(ChatMessageType type, String text);
	String extractString(byte [] bytes);
	List<String> extractList(byte [] bytes);
	ChatMessageType extractType(byte [] bytes);
	byte[] buildMessage(ChatMessageType type, List<String> ls);
}
