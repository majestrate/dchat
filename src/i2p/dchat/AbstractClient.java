package i2p.dchat;

import i2p.dchat.impl.FileBootstrap;
import i2p.dchat.impl.JSONProtocol;
import i2p.dchat.impl.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.event.ListSelectionEvent;

import net.i2p.I2PException;
import net.i2p.client.I2PClient;
import net.i2p.client.I2PClientFactory;
import net.i2p.client.I2PSession;
import net.i2p.client.I2PSessionException;
import net.i2p.client.datagram.I2PDatagramMaker;
import net.i2p.client.datagram.I2PInvalidDatagramException;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;

public abstract class AbstractClient implements IChatManager {

	private final String _our_nick;
	private final I2PDatagramMaker _dgram_maker;
	private final IChatProtocol _protocol;
	private I2PSession _session;
	private I2PClient _client;
	private List<IPeerInfo> _peers;
	private Logger _log;
	private List<MessageListener> _listeners;

	public AbstractClient(String privateKeyFile, String nickname) {
		_our_nick = nickname;
		_peers = new ArrayList<IPeerInfo>();
		_listeners = new ArrayList<MessageListener>();
		_protocol = new JSONProtocol();
		_client = I2PClientFactory.createClient();
		try {
			if (!new File(privateKeyFile).exists()) {
				FileOutputStream fos = new FileOutputStream(privateKeyFile);
				_client.createDestination(fos);
				fos.flush();
				fos.close();
			}
			FileInputStream fis = new FileInputStream(privateKeyFile);
			_session = _client.createSession(fis, null);
			_session.connect();
			fis.close();
			_session.setSessionListener(this);

			_log = Logger.getLogger("Client");

		} catch (I2PException thrown) {
			throw new RuntimeException(thrown);
		} catch (IOException thrown) {
			throw new RuntimeException(thrown);
		}
		_dgram_maker = new I2PDatagramMaker(_session);
	}

	public void bootstrapFromFile(String fname) throws BootstrapException {
		List<IPeerInfo> peers = new ArrayList<IPeerInfo>();
		FileBootstrap fboot = new FileBootstrap(fname);
		fboot.boostrap(peers);
		for (IPeerInfo info : peers) {
			registerPeer(info);
		}
	}

	public void mainloop() {
		for (IPeerInfo info : _peers) {
			byte[] raw = _protocol.buildMessage(ChatMessageType.PEERS,
					new ArrayList<String>());
			sendRaw(info, raw);
			raw = _protocol.buildMessage(ChatMessageType.NICK, (String) null);
		}
		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine()) {
			broadcast(scan.nextLine());
		}
		end();
	}

	//TODO: this is bad, use hashtable
	private boolean hasPeer(Destination dest) {
		for(IPeerInfo info : _peers) {
			if (info.getDestination().equals(dest)) { return true; }
		}
		return false;
	}
	
	@Override
	public void messageAvailable(I2PSession session, int msgId, long size) {

		try {
			byte[] msg = session.receiveMessage(msgId);
			_log.info("got bytes length=" + msg.length + " id=" + msgId);
			IChatMessage chatMsg = Utils.parseChatMessage(msg, _protocol);
			IPeerInfo info = chatMsg.getPeerInfo();
			ChatMessageType type = chatMsg.getType();
			Destination dest = info.getDestination();
			if(dest.equals(_session.getMyDestination())) {
				return;
			}
			if(!hasPeer(dest)) {
				byte [] raw = _protocol.buildMessage(ChatMessageType.NICK, (String) null);
				sendRaw(info, raw);
			}
			switch (type) {
			case BROADCAST_CHAT:
				
				onBroadcastMessage(info, chatMsg.getText());
				break;
			case IDENT:
				onIdentMessage(info, chatMsg.getText());
				break;
			case NICK:
				onNickMessage(info, chatMsg.getText());
				break;
			case PEERS:
				onPeersMessage(info, chatMsg.getList());
				break;
			case UNICAST_CHAT:
				onUnicastMessage(info, chatMsg.getText());
				break;
			default:
				return;
			}
			informListeners(chatMsg);
		} catch (I2PSessionException thrown) {
			thrown.printStackTrace();
		} catch (DataFormatException thrown) {
			thrown.printStackTrace();
		} catch (I2PInvalidDatagramException thrown) {
			thrown.printStackTrace();
		}
	}

	private void informListeners(IChatMessage chatMsg) {
		for (MessageListener listen : _listeners) {
			listen.handleMessage(chatMsg);
		}
	}

	@Override
	public void reportAbuse(I2PSession session, int severity) {
	}

	public void end() {
		try {
			_session.destroySession();
		} catch (I2PSessionException thrown) {
			thrown.printStackTrace();
		}
	}

	@Override
	public void disconnected(I2PSession session) {
		_log.info("session disconnected");
	}

	@Override
	public void errorOccurred(I2PSession session, String message,
			Throwable error) {
		throw new RuntimeException(message, error);
	}

	@Override
	public void broadcast(String message) {
		_log.info("broadcast >> " + message);
		byte[] raw = _protocol.buildMessage(ChatMessageType.BROADCAST_CHAT,
				message);
		for (IPeerInfo info : _peers) {

			sendRaw(info, raw);

		}
	}

	private void sendRaw(IPeerInfo info, byte[] raw) {
		if (_session.isClosed()) {
			return;
		}
		byte[] msg = _dgram_maker.makeI2PDatagram(raw);
		_log.info("send " + msg.length + " bytes to "
				+ info.getDestination().toBase64());
		try {
			while (!_session.sendMessage(info.getDestination(), msg)) {
				Thread.sleep(100);
			}
		} catch (InterruptedException thrown) {
		} catch (I2PSessionException thrown) {
			thrown.printStackTrace();
		}
	}

	@Override
	public void addMessageListener(MessageListener listener) {
		if (_listeners.contains(listener)) {
			return;
		}
		_listeners.add(listener);
	}

	@Override
	public void registerPeer(IPeerInfo info) {
		if (_peers.contains(info)) {
			_log.info("duplicate peer: " + info.getNickname());
			return;
		}
		_peers.add(info);
		_log.info("added peer: " + info.getNickname());
	}

	@Override
	public void unicast(String nick, String message) {
		_log.info("unicast -> " + nick + " >> " + message);
		byte[] raw = _protocol.buildMessage(ChatMessageType.BROADCAST_CHAT,
				message);
		for (IPeerInfo info : _peers) {
			if (info.getNickname().equals(nick)) {

				sendRaw(info, raw);

				return;
			}
		}
		_log.warning("nick not found: " + nick);
	}

	protected abstract void onBroadcastMessage(IPeerInfo info, String message);

	protected abstract void onUnicastMessage(IPeerInfo info, String message);

	private void onPeersMessage(IPeerInfo info, List<String> peers) {
		// TODO: handle unsolicitied messages
		List<String> ls = new ArrayList<String>();
		if (peers.isEmpty()) {
			for (IPeerInfo _info : _peers) {
				ls.add(_info.getDestination().toBase64());
			}
			byte[] bytes = _protocol.buildMessage(ChatMessageType.PEERS, ls);
			sendRaw(info, bytes);
		}
		

	}

	private void onIdentMessage(IPeerInfo info, String ident) {
		if(!hasPeer(info.getDestination())) {
			_peers.add(info);
		}
		info.setNickname(ident);
	}

	private void onNickMessage(IPeerInfo info, String nick) {
		String reply = _our_nick;
		byte[] bytes = _protocol.buildMessage(ChatMessageType.IDENT, reply);

		sendRaw(info, bytes);

	}

	public String getDestString() {
		return _session.getMyDestination().toBase64();
	}
}
