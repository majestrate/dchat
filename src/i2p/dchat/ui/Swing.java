package i2p.dchat.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.Document;

import i2p.dchat.AbstractClient;
import i2p.dchat.BootstrapException;
import i2p.dchat.IPeerInfo;

public class Swing extends JFrame {

	
	// begin shitty code
	class _Frame extends JPanel {

		private AbstractClient _client;
		private JTextField _nick_text;
		private JTextField _fname_text;
		private final JTextArea _broadcast_area;
		private final JTextField _input_text;
		private JButton _connect_button;
		private JButton _send_button;

		_Frame() {
			super();
			_nick_text = new JTextField("nickname");
			_fname_text = new JTextField("dchat_privkey.dat");
			_input_text = new JTextField("chat here");

			_broadcast_area = new JTextArea(10, 100);
			_broadcast_area.setEditable(false);
			_broadcast_area.setLineWrap(true);

			_connect_button = new JButton("Connect");
			_connect_button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					connect();
				}

			});
			_send_button = new JButton("Send");
			_send_button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (_client != null) {
						_client.broadcast(_input_text.getText());
						_input_text.setText("");
					}
				}

			});
			add(_nick_text);
			add(_fname_text);
			add(_broadcast_area);
			add(_connect_button);
			add(_input_text);
			add(_send_button);
			pack();
		}

		void connect() {
			if (_client == null) {
				String nick = _nick_text.getText().trim();
				String fname = _fname_text.getText().trim();
				_client = new AbstractClient(fname, nick) {

					@Override
					protected void onUnicastMessage(IPeerInfo info,
							String message) {
						// TODO: Implement unicast in GUI
					}

					@Override
					protected void onBroadcastMessage(IPeerInfo info,
							String message) {
						
						String str = "<" + info.getNickname() + "> "
								+ message.trim();
						_broadcast_area.append(str);
						_broadcast_area.append("\n");
					}
				};
				_connect_button.setText("Disconnect");
				_broadcast_area.append("bootstraping\n");
				try {
					_client.bootstrapFromFile("peers.txt");
				} catch (BootstrapException thrown) {
					_broadcast_area.append("failed bootstrap\n");
					return;
				}
				_broadcast_area.append("Okay");
				_broadcast_area.append("your dest is: "
						+ _client.getDestString());
			} else {
				_client.end();
				_client = null;
				_broadcast_area.setText("");
			}
		}
	}
	
	//end shitty code
	
	Swing() {
		super("DChat");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(new _Frame());
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		new Swing();
	}

}
