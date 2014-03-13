package i2p.dchat.impl;

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

			@Override
			public Destination getDestination() {return _dest;}

			@Override
			public void setDestination(Destination dest) {}

			@Override
			public String getNickname() {return _nickname;}

			@Override
			public void setNickname(String str) {}
		};
		
	}
	
	public static IChatMessage parseChatMessage(byte [] raw, IChatProtocol protocol) throws DataFormatException, I2PInvalidDatagramException {
		I2PDatagramDissector disect = new I2PDatagramDissector();
		disect.loadI2PDatagram(raw);
		disect.verifySignature();
		final Destination dest = disect.extractSender();
		final byte[] bytes= disect.extractPayload();
		final String text = protocol.extractString(bytes);
		final ChatMessageType type = protocol.extractType(bytes);
		final IPeerInfo info = new IPeerInfo(){

			@Override
			public Destination getDestination() { return dest; }

			@Override
			public void setDestination(Destination dest) {}

			@Override
			public String getNickname() { return dest.getSigningPublicKey().toBase64(); }

			@Override
			public void setNickname(String str) {}
			
		};
		return new IChatMessage(){

			@Override
			public IPeerInfo getPeerInfo() { return info; }

			@Override
			public void setPeerInfo(IPeerInfo info) {}

			@Override
			public String getText() { return text; }

			@Override
			public void setMessage(String message) {}

			@Override
			public ChatMessageType getType() { return type; }

			@Override
			public void setType(ChatMessageType type) {}

			@Override
			public byte[] serialize() { return null; }
			
		};
	}
}
