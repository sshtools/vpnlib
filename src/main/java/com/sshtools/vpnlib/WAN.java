package com.sshtools.vpnlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.lang.StringUtils;

/**
 * Static methods to access the list of discovered VPNs and configurrations, and
 * a simple commandline tool to list vpns, profiles and start and stop them.
 */
public class WAN {

	public static List<VPN> getVPNs() {
		List<VPN> v = new ArrayList<>();
		final ServiceLoader<VPN> services = ServiceLoader.load(VPN.class);
		for (VPN vpn : services) {
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
		} else if (args.length == 1 && args[0].equals("profiles")) {
			for (VPN v : getVPNs()) {
				for (Profile p : v.getConfigurations()) {
					System.out.println(p);
				}
			}
		} else if (args.length == 1 && args[0].equals("vpns")) {
			for (VPN v : getVPNs()) {
				System.out.println(v);
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
				String username = null;
				String pw = null;
				for (int i = 2; i < args.length; i++) {
					if (args[i].startsWith("--username="))
						username = args[i].substring(11);
					if (args[i].startsWith("--password="))
						pw = args[i].substring(11);
				}
				if (StringUtils.isNotBlank(username)) {
					if (!profile.getRequiredCredentials().contains(UsernameCredentials.class))
						throw new IllegalArgumentException("This VPN does not require credentials.");
					profile.start(new UsernameCredentials(username, pw.toCharArray()));
				} else
					profile.start();
			} else if (op.equals("show")) {
				System.out.println(profile);
			} else
				throw new IllegalArgumentException("Unknown operation.");
		}
	}
}
