package i2p.dchat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import i2p.dchat.impl.FileBootstrap;
import i2p.dchat.impl.JSONProtocol;
import i2p.dchat.impl.PrintMessageListener;
import i2p.dchat.impl.Utils;
import net.i2p.client.I2PClient;
import net.i2p.client.I2PClientFactory;
import net.i2p.client.I2PSession;
import net.i2p.client.I2PSessionException;
import net.i2p.client.datagram.I2PDatagramMaker;
import net.i2p.client.datagram.I2PInvalidDatagramException;
import net.i2p.data.DataFormatException;

public class Client implements IChatManager {
	private final I2PDatagramMaker _dgram_maker;
	private final IChatProtocol _protocol;
	private I2PSession _session;
	private I2PClient _client;
	private List<IPeerInfo> _peers;
	private Logger _log;
	private List<MessageListener> _listeners;

	public Client(String privateKeyFile) throws Exception {
		_peers = new ArrayList<IPeerInfo>();
		_listeners = new ArrayList<MessageListener>();

		_client = I2PClientFactory.createClient();
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
		_dgram_maker = new I2PDatagramMaker(_session);
		_protocol = new JSONProtocol();
		_log = Logger.getLogger(getClass().getCanonicalName());
		bootstrapFromFile("peers.txt");

	}

	public void bootstrapFromFile(String fname) throws Exception {
		List<IPeerInfo> peers = new ArrayList<IPeerInfo>();
		FileBootstrap fboot = new FileBootstrap(fname);
		fboot.boostrap(peers);
		for (IPeerInfo info : peers) {
			registerPeer(info);
		}
	}

	public void mainloop() throws InterruptedException {
		broadcast("I am online :3");
		Scanner in = new Scanner(System.in);
		PrintStream out = System.out;
		out.println("<your_username_here>:"
				+ _session.getMyDestination().toBase64());
		do {
			if (in.hasNextLine()) {
				out.print("bcast>> ");
				broadcast(in.nextLine());
			}
		} while (in.hasNextLine());
		try {
			_session.destroySession();
		} catch (I2PSessionException thrown) {
			thrown.printStackTrace();
		}
	}

	@Override
	public void messageAvailable(I2PSession session, int msgId, long size) {

		try {
			byte[] msg = session.receiveMessage(msgId);
			_log.info("got bytes length=" + msg.length + " id=" + msgId);
			IChatMessage chatMsg = Utils.parseChatMessage(msg, _protocol);
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

	@Override
	public void disconnected(I2PSession session) {
		_log.info("session disconnected");
		System.exit(0);
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
			try {
				sendRaw(info, raw);
			} catch (I2PSessionException thrown) {
				_log.warning("broadcast error: " + thrown.getMessage());
				thrown.printStackTrace();
			}
		}
	}

	private void sendRaw(IPeerInfo info, byte[] raw) throws I2PSessionException {
		byte[] msg = _dgram_maker.makeI2PDatagram(raw);
		_log.info("send " + msg.length + " bytes to "
				+ info.getDestination().toBase64());
		try {
			while (!_session.sendMessage(info.getDestination(), msg)) {
				Thread.sleep(100);
			}
		} catch (InterruptedException thrown) {
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
				try {
					sendRaw(info, raw);
				} catch (I2PSessionException thrown) {
					_log.warning("unicast error: " + thrown.getMessage());
					thrown.printStackTrace();
				}
				return;
			}
		}
		_log.warning("nick not found: " + nick);
	}

	public static void main(String[] args) {
		PrintStream out = System.err;
		String kfile;
		if (args.length == 0) {
			kfile = "dchat_privkey.dat";
		} else {
			kfile = args[0];
		}
		Client dchat = null;
		try {
			dchat = new Client(kfile);
			dchat.addMessageListener(new PrintMessageListener(out));
			dchat.mainloop();
		} catch (Exception thrown) {
			throw new RuntimeException(thrown);
		}

	}

}
