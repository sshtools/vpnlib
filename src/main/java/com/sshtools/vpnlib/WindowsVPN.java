package com.sshtools.vpnlib;

import static com.sshtools.forker.client.OSCommand.runCommandAndCaptureOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.sshtools.forker.client.ForkerProcess;
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
			for (String s : runCommandAndCaptureOutput("rasdial", getName(), "/DISCONNECT")) {
				if (s.toLowerCase().contains("remote access error"))
					throw new IOException(s);
			}
			waitForStateChange(Integer.parseInt(
					getProperties().getOrDefault(PROP_DISCONNECT_TIMEOUT, String.valueOf(DEFAULT_DISCONNECT_TIMEOUT))),
					false);
		}

		@Override
		public void start() throws IOException {
			for (String s : runCommandAndCaptureOutput("rasdial", getName())) {
				if (s.toLowerCase().contains("remote access error"))
					throw new IOException(s);
			}
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
			boolean a = false;
			String[] cmd;
			if (getOptions().contains(Option.PUBLIC))
				cmd = new String[] { "Get-VpnConnection", "-AllUserConnection", "-Name", getName() };
			else
				cmd = new String[] { "Get-VpnConnection", "-Name", getName() };
			ForkerProcess process = new PowerShellBuilder(cmd).start();
			try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				while ((l = r.readLine()) != null) {
					if (!a) {
						int idx = l.indexOf(':');
						if (idx != -1) {
							if (l.substring(0, idx).trim().equals("ConnectionStatus"))
								a = l.substring(idx + 2).equals("Connected");
						}
					}
				}
			}
			return a;
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
	public List<Profile> getConfigurations(Option... options) throws IOException {
		List<Profile> l = new ArrayList<>();
		List<Option> ol = Arrays.asList(options);
		if (ol.isEmpty() || ol.contains(Option.USER))
			populate(l, Arrays.asList(Option.USER), "Get-VpnConnection");
		if (ol.isEmpty() || ol.contains(Option.PUBLIC))
			populate(l, Arrays.asList(Option.PUBLIC), "Get-VpnConnection", "-AllUserConnection");
		return l;
	}

	protected void populate(List<Profile> l, List<Option> options, String... command) throws IOException {
		ForkerProcess process = new PowerShellBuilder(command).start();
		Profile p = null;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (StringUtils.isBlank(line)) {
					p = null;
				} else {
					if (p == null) {
						p = new WindowsVPNProfile(this);
						p.getOptions().addAll(options);
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
						} else if (n.equals("Guid")) {
							p.setId(v.substring(1, v.length() - 1));
						}
					}
				}
			}
			try {
				process.waitFor(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new IOException("Timed out waiting for VPN configurations.");
			}
		}
	}

	@Override
	public boolean isAvailable() {
		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				IOUtils.copy(new PowerShellBuilder("[Environment]::OSVersion").redirectErrorStream(true).start()
						.getInputStream(), new NullOutputStream());
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
