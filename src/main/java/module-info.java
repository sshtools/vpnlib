
module vpnlib {
	requires transitive forker.client;
	requires transitive forker.common;
	requires transitive commons.io;
	requires transitive commons.lang;
	requires transitive java.prefs;
	requires transitive jna;
	exports com.hypersocket.vpnlib;
    uses com.hypersocket.vpnlib.VPN;
}