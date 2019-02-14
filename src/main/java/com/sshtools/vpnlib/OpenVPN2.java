package com.sshtools.vpnlib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.prefs.Preferences;

import com.sshtools.forker.client.OSCommand;

public class OpenVPN2 extends AbstractPreferencedBasedVPN {
	private final static String PROP_CONFIGURATION_FILE = "configurationFile";
	private final static String PROP_OPEN_VPN_COMMAND = "openVPNCommand";

	public OpenVPN2() {
		super(Preferences.systemNodeForPackage(OpenVPN2.class).node("OpenVPN2"));
	}

	public OpenVPN2(Preferences preferences) {
		super(preferences);
	}

	@Override
	public boolean isAvailable() {
		try {
			/*
			 * TODO this shows OSCommand needs more convenience functions, namely capturing
			 * output (stderr) and ignoring exit codes (don't throw exception)
			 */
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int ret = OSCommand.runCommand(bos,
					Arrays.asList(preferences.get(PROP_OPEN_VPN_COMMAND, "openvpn"), "--version"));
			if (ret != 1)
				throw new IllegalStateException("Unexpected return code.");
			BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
			String l = r.readLine();
			if (l != null && l.matches(".*OpenVPN 2.*"))
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public String getName() {
		return "OpenVPN2";
	}

	@Override
	public void configure() {
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public Profile createConfiguration() throws IOException {
		return new OpenVPNProfile(this);
	}

	class OpenVPNProfile extends AbstractExternalExecutableProfile {
		public OpenVPNProfile(VPN vpn) {
			super(vpn);
		}

		@Override
		protected String[] getStartCommandArgs() {
			return new String[] { preferences.get(PROP_OPEN_VPN_COMMAND, "openvpn"), getProperties().getOrDefault(
					PROP_CONFIGURATION_FILE, System.getProperty("user.home", "") + File.separator + ".vpnlib.ovpn") };
		}
	}
}
