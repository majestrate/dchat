package i2p.dchat.impl;

import java.util.List;

import i2p.dchat.BootstrapException;
import i2p.dchat.IBootstrap;
import i2p.dchat.IPeerInfo;

public class SeedlessBootstrap implements IBootstrap {

	@Override
	public void boostrap(List<IPeerInfo> peers) throws BootstrapException {
		throw new BootstrapException("seedless bootstrap not implemented yet");
	}

}
