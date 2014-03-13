package i2p.dchat.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import i2p.dchat.BootstrapException;
import i2p.dchat.IBootstrap;
import i2p.dchat.IPeerInfo;

public class FileBootstrap implements IBootstrap {

	private final String _fname;

	public FileBootstrap(String fname) {
		_fname = fname;
	}

	@Override
	public void boostrap(List<IPeerInfo> peers) throws BootstrapException {
		Destination dest = null;
		try {
			ArrayList<String> nicks = new ArrayList<String>();
			FileInputStream fis = new FileInputStream(_fname);
			Scanner in = new Scanner(fis);
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(":");
				if (parts.length != 2) {
					throw new BootstrapException("invalid line in file: "
							+ line);
				}
				if (nicks.contains(parts[0])) {
					throw new BootstrapException("duplicate nickname: "
							+ parts[0]);
				}
				IPeerInfo peer = Utils.makePeerInfo(parts[0], parts[1]);
				peers.add(peer);
				nicks.add(peer.getNickname());
			}
		} catch (IOException thrown) {
			throw new BootstrapException("Could not bootstrap", thrown);
		} catch (DataFormatException e) {
			throw new BootstrapException("Invalid destination in file: " + dest);
		}
	}

}
