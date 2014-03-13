package i2p.dchat;

import net.i2p.data.Destination;

public interface IPeerInfo {

	Destination getDestination();
	void setDestination(Destination dest);
	String getNickname();
	void setNickname(String str);
	
}
