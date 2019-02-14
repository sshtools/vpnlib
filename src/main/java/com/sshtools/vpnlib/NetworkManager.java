package com.sshtools.vpnlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;

import com.sshtools.forker.client.OSCommand;

public class NetworkManager extends AbstractVPN {
	private final static int DEFAULT_CONNECT_TIMEOUT = 60;
	private final static String PROP_CONNECT_TIMEOUT = "connectTimeout";
	private final static int DEFAULT_DISCONNECT_TIMEOUT = 20;
	private final static String PROP_DISCONNECT_TIMEOUT = "disconnectTimeout";

	@Override
	public String getName() {
		return "NetworkManager";
	}

	@Override
	public void configure() {
	}

	@Override
	public List<Profile> getConfigurations(Option... options) throws IOException {
		List<Profile> p = new ArrayList<>();
		for (String line : OSCommand.runCommandAndCaptureOutput("nmcli", "-t", "connection", "show")) {
			String[] args = line.split(":");
			if (args.length > 0 && args[2].equals("vpn")) {
				final Profile profile = new Profile(args[1], args[0], this) {
					@Override
					public boolean isActive() throws IOException {
						return OSCommand
								.runCommandAndCaptureOutput("nmcli", "-t", "connection", "show", "--active", getId())
								.size() > 0;
					}

					@Override
					public void start(Credentials... credentials) throws IOException {
						OSCommand.runCommand("nmcli", "-t", "connection", "up", getId());
						waitForStateChange(Integer.parseInt(getProperties().getOrDefault(PROP_CONNECT_TIMEOUT,
								String.valueOf(DEFAULT_CONNECT_TIMEOUT))), true);
					}

					@Override
					public void stop() throws IOException {
						OSCommand.runCommand("nmcli", "-t", "connection", "down", getId());
						waitForStateChange(Integer.parseInt(getProperties().getOrDefault(PROP_DISCONNECT_TIMEOUT,
								String.valueOf(DEFAULT_DISCONNECT_TIMEOUT))), false);
					}

					protected void waitForStateChange(int to, boolean reqState) throws IOException {
						try {
							for (int i = 0; i < to; i++) {
								Thread.sleep(1000);
								if (isActive())
									return;
							}
						} catch (Exception e) {
							throw new IOException("Interrupted waiting ");
						}

						throw new IOException("Timed-out waiting for VPN connection.");
					}
				};

				/* Get extra properties for this VPN */
				for (String cline : OSCommand.runCommandAndCaptureOutput("nmcli", "-t", "connection", "show",
						profile.getId())) {
					if (cline.equals("connection.permissions:")) {
						profile.getOptions().add(Option.PUBLIC);
					}
					else if (cline.startsWith("connection.permissions:")) {
						profile.getOptions().add(Option.USER);
					}
				}

				p.add(profile);
			}
		}
		return p;
	}

	@Override
	public boolean isAvailable() {
		if (!SystemUtils.IS_OS_LINUX)
			return false;

		try {
			OSCommand.runCommandAndCaptureOutput("nmcli", "--version");
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSystem() {
		return true;
	}
}
