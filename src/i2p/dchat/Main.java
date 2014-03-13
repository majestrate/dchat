package i2p.dchat;

public class Main {

	public static void main(String[] args) {
		AbstractClient client = new AbstractClient("dchat_privkey.dat","unclesame"){

			@Override
			protected void onBroadcastMessage(IPeerInfo info, String message) {
				System.out.println(info.getNickname()+">> "+message);			
			}

			@Override
			protected void onUnicastMessage(IPeerInfo info, String message) {
			}
			
		};
		try {
			client.bootstrapFromFile("peers.txt");
		} catch (BootstrapException thrown) {
			thrown.printStackTrace();
			return;
		}
		client.mainloop();
	}

}
