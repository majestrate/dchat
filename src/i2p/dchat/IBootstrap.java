package i2p.dchat;

import java.util.List;

public interface IBootstrap {

	void boostrap(List<IPeerInfo> peers) throws BootstrapException;
}
