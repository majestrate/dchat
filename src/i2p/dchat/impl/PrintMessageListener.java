package i2p.dchat.impl;

import java.io.PrintStream;

import i2p.dchat.ChatMessageType;
import i2p.dchat.IChatMessage;
import i2p.dchat.IPeerInfo;
import i2p.dchat.MessageListener;

public class PrintMessageListener implements MessageListener {

	private final PrintStream _out;
	
	public PrintMessageListener(PrintStream out) { _out = out; }
	
	@Override
	public void handleMessage(IChatMessage msg) {
		IPeerInfo info = msg.getPeerInfo();
		ChatMessageType msgType = msg.getType();
		_out.print("Got "+msgType.name()+" message from ");
		_out.print(info.getDestination());
		_out.flush();
	}
}
