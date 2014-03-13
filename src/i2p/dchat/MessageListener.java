package i2p.dchat;

public interface MessageListener {

	/**
	 * handle incomming chat message
	 * @param msg
	 */
	void handleMessage(IChatMessage msg);
	
}
