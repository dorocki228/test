package l2s.gameserver.network.l2.c2s;

public class RequestHardWareInfo implements IClientIncomingPacket
{
	private String _mac;
	private String _cpu;
	private String _vgaName;
	private String _driverVersion;
	private int _windowsPlatformId;
	private int _windowsMajorVersion;
	private int _windowsMinorVersion;
	private int _windowsBuildNumber;
	private int _DXVersion;
	private int _DXRevision;
	private int _cpuSpeed;
	private int _cpuCoreCount;
	private int _unk8;
	private int _unk9;
	private int _PhysMemory1;
	private int _PhysMemory2;
	private int _unk12;
	private int _videoMemory;
	private int _unk14;
	private int _vgaVersion;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_mac = packet.readS();
		_windowsPlatformId = packet.readD();
		_windowsMajorVersion = packet.readD();
		_windowsMinorVersion = packet.readD();
		_windowsBuildNumber = packet.readD();
		_DXVersion = packet.readD();
		_DXRevision = packet.readD();
		_cpu = packet.readS();
		_cpuSpeed = packet.readD();
		_cpuCoreCount = packet.readD();
		_unk8 = packet.readD();
		_unk9 = packet.readD();
		_PhysMemory1 = packet.readD();
		_PhysMemory2 = packet.readD();
		_unk12 = packet.readD();
		_videoMemory = packet.readD();
		_unk14 = packet.readD();
		_vgaVersion = packet.readD();
		_vgaName = packet.readS();
		_driverVersion = packet.readS();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
	}
}