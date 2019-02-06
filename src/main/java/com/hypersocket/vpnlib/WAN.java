package com.hypersocket.vpnlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class WAN {

	public static List<VPN> getVPNs() {
		List<VPN> v = new ArrayList<>();
		for (VPN vpn : ServiceLoader.load(VPN.class)) {
			if (vpn.isAvailable())
				v.add(vpn);
		}
		return v;
	}

	public static VPN getVPN(String name) {
		for (VPN v : getVPNs()) {
			if (name.equals(v.getName()))
				return v;
		}
		return null;
	}

	public static Profile getConfiguration(String uuid) throws IOException {
		for (VPN vpn : getVPNs()) {
			Profile cfg = vpn.getConfiguration(uuid);
			if (cfg != null)
				return cfg;
		}
		return null;
	}

	public static Profile getConfigurationByName(String name) throws IOException {
		for (VPN vpn : getVPNs()) {
			Profile cfg = vpn.getConfigurationByName(name);
			if (cfg != null)
				return cfg;
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			for (VPN v : getVPNs()) {
				System.out.println(v);
				for (Profile p : v.getConfigurations()) {
					System.out.println("   " + p);
				}
			}
		} else {
			String op = args[0];
			String on = args.length > 1 ? args[1] : null;

			Profile profile = null;
			if (on != null) {
				profile = getConfiguration(on);
				if (profile == null)
					profile = getConfigurationByName(on);
				if (profile == null)
					throw new IllegalArgumentException("No such VPN " + on);
			}

			if (op.equals("stop")) {
				profile.stop();
			} else if (op.equals("start")) {
				profile.start();
			} else if (op.equals("show")) {
				System.out.println(profile);
			} else
				throw new IllegalArgumentException("Unknown operation.");
		}
	}
}
