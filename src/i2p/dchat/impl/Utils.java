package i2p.dchat.impl;

import java.util.List;

import i2p.dchat.ChatMessageType;
import i2p.dchat.IChatMessage;
import i2p.dchat.IChatProtocol;
import i2p.dchat.IPeerInfo;
import net.i2p.client.datagram.I2PDatagramDissector;
import net.i2p.client.datagram.I2PInvalidDatagramException;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;

public class Utils {

	public static final IPeerInfo makePeerInfo(String nickname, String destination) throws DataFormatException {
		final Destination _dest = new Destination(destination); // final destination huehuehue 
		final String _nickname = new String(nickname);
		return new IPeerInfo (){
			private String _nick = _nickname;
			@Override
			public Destination getDestination() {return _dest;}

			@Override
			public void setDestination(Destination dest) {}

			@Override
			public String getNickname() {return _nick;}

			@Override
			public void setNickname(String str) {_nick = str;}
		};
		
	}
	
	public static IChatMessage parseChatMessage(byte [] raw, final IChatProtocol protocol) throws DataFormatException, I2PInvalidDatagramException {
		I2PDatagramDissector disect = new I2PDatagramDissector();
		disect.loadI2PDatagram(raw);
		disect.verifySignature();
		final Destination dest = disect.extractSender();
		final byte[] bytes= disect.extractPayload();
		final ChatMessageType type = protocol.extractType(bytes);
		final IPeerInfo info = makePeerInfo("??",dest.toBase64());
		return new IChatMessage(){

			@Override
			public IPeerInfo getPeerInfo() { return info; }

			@Override
			public void setPeerInfo(IPeerInfo info) {}

			@Override
			public String getText() { return protocol.extractString(bytes); }

			@Override
			public void setMessage(String message) {}

			@Override
			public ChatMessageType getType() { return type; }

			@Override
			public void setType(ChatMessageType type) {}

			@Override
			public List<String> getList() { return protocol.extractList(bytes); }
			
		};
	}
}
