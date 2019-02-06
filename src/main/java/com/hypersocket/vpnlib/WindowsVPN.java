package com.hypersocket.vpnlib;

import static com.sshtools.forker.client.OSCommand.runCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.sshtools.forker.client.PowerShellBuilder;

public class WindowsVPN extends AbstractVPN {
	private final static int DEFAULT_CONNECT_TIMEOUT = 60;
	private final static String PROP_CONNECT_TIMEOUT = "connectTimeout";
	private final static int DEFAULT_DISCONNECT_TIMEOUT = 20;
	private final static String PROP_DISCONNECT_TIMEOUT = "disconnectTimeout";

	public class WindowsVPNProfile extends Profile {
		public WindowsVPNProfile(VPN vpn) {
			super(vpn);
		}

		@Override
		public void stop() throws IOException {
			runCommand("rasdial", getName(), "/DISCONNECT");
			waitForStateChange(Integer.parseInt(
					getProperties().getOrDefault(PROP_DISCONNECT_TIMEOUT, String.valueOf(DEFAULT_DISCONNECT_TIMEOUT))),
					false);
		}

		@Override
		public void start() throws IOException {
			runCommand("rasdial", getName());
			waitForStateChange(Integer.parseInt(
					getProperties().getOrDefault(PROP_CONNECT_TIMEOUT, String.valueOf(DEFAULT_CONNECT_TIMEOUT))), true);
		}

		protected void waitForStateChange(int to, boolean reqState) throws IOException {
			try {
				for (int i = 0; i < to; i++) {
					Thread.sleep(1000);
					if (isActive() == reqState)
						return;
				}
			} catch (Exception e) {
				throw new IOException("Interrupted waiting ");
			}

			throw new IOException("Timed-out waiting for VPN connection.");
		}

		@Override
		public boolean isActive() throws IOException {
			String l;
			try (BufferedReader r = new BufferedReader(new InputStreamReader(
					new PowerShellBuilder("Get-VpnConnection", "-Name", getName()).start().getInputStream()))) {
				while ((l = r.readLine()) != null) {
					int idx = l.indexOf(':');
					if (idx != -1) {
						if (l.substring(0, idx).trim().equals("ConnectionStatus"))
							return l.substring(idx + 2).equals("Connected");
					}
				}
			}
			return false;
		}
	}

	@Override
	public String getName() {
		return "Windows VPN";
	}

	@Override
	public void configure() {
	}

	@Override
	public List<Profile> getConfigurations() throws IOException {
		List<Profile> l = new ArrayList<>();
		Profile p = null;

		try (BufferedReader r = new BufferedReader(new InputStreamReader(
				new PowerShellBuilder("Get-VpnConnection", "-Name", getName()).start().getInputStream()))) {
			String line;
			while ((line = r.readLine()) != null) {
				System.out.println(line);
				if (StringUtils.isBlank(line)) {
					p = null;
				} else {
					if (p == null) {
						p = new WindowsVPNProfile(this);
						l.add(p);
					}
					int idx = line.indexOf(':');
					if (idx != -1) {
						String n = line.substring(0, idx).trim();
						String v = line.substring(idx + 2);
						if (n.equals("Name")) {
							p.setName(v);
						} else if (n.equals("Guid")) {
							p.setId(v.substring(1, v.length() - 1));
						}
					}
				}
			}
		}
		return l;
	}

	@Override
	public boolean isAvailable() {
		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				IOUtils.copy(new PowerShellBuilder("[Enviornment]::OSVersion").start().getInputStream(),
						new NullOutputStream());
				return true;
			}
		} catch (IOException psne) {
		}
		return false;
	}

	@Override
	public String toString() {
		return "Windows VPN";
	}

	@Override
	public boolean isSystem() {
		return true;
	}

}
