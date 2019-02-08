module com.sshtools.vpnlib {
	requires transitive forker.client;
	requires transitive forker.common;
	requires transitive commons.io;
	requires transitive commons.lang;
	requires transitive java.prefs;
	requires transitive jna;
	exports com.sshtools.vpnlib;
    uses com.sshtools.vpnlib.VPN;
    provides com.sshtools.vpnlib.VPN with com.sshtools.vpnlib.NetworkManager,com.sshtools.vpnlib.WindowsVPN,com.sshtools.vpnlib.OpenVPN2,com.sshtools.vpnlib.GenericVPN;

}