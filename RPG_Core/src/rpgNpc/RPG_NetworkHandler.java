package rpgNpc;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PlayerConnection;

public class RPG_NetworkHandler extends PlayerConnection
{
	public RPG_NetworkHandler(MinecraftServer minecraftserver, NetworkManager netMgr, EntityPlayer entityplayer)
	{
		super(minecraftserver, netMgr, entityplayer);
	}
	
	public void sendPacket(Packet packet)
	{}
}
