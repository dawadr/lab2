package net;

import java.net.DatagramPacket;

public interface IDatagramPacketListener {

	public void packetReceived(DatagramPacket packet);

}
