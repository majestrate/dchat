package i2p.dchat;

import net.i2p.client.I2PSessionListener;
import net.i2p.client.I2PSessionMuxedListener;

public interface IChatManager extends I2PSessionListener {

	void broadcast(String message);
	void addMessageListener(MessageListener listener);
	void registerPeer(IPeerInfo info);
	void unicast(String nick, String message);
	
}
