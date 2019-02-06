package com.hypersocket.vpnlib;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;

public class GenericVPN extends AbstractPreferencedBasedVPN {

	private final static String PROP_START_COMMAND = "start";
	private final static String PROP_START_WAIT = "startWait";
	private final static String PROP_STOP_COMMAND = "stop";
	private final static String PROP_STOP_WAIT = "stopWait";

	public GenericVPN() {
		super(Preferences.systemNodeForPackage(GenericVPN.class));
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public String getName() {
		return "Generic VPN";
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
		return new GenericVPNProfile(this);
	}

	class GenericVPNProfile extends AbstractExternalExecutableProfile {

		private static final int DEFAULT_START_WAIT = 0;
		private static final int DEFAULT_STOP_WAIT = 0;

		public GenericVPNProfile(VPN vpn) {
			super(vpn);
		}

		@Override
		public void start() throws IOException {
			super.start();
			int sw = Integer.parseInt(getProperties().getOrDefault(PROP_START_WAIT, String.valueOf(DEFAULT_START_WAIT)));
			if(sw > 0) {
				try {
					Thread.sleep(sw * 1000);
				} catch (InterruptedException e) {
					throw new IOException("Interrupted waiting for start.", e);
				}
			}
		}

		@Override
		public void stop() throws IOException {
			String stop = getProperties().get(PROP_STOP_COMMAND);
			if (StringUtils.isBlank(stop))
				super.stop();
			else {
				Process process = runCommand(parseCommand(stop));
				try {
					process.waitFor(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					throw new IOException("Failed to stop VPN.");
				}
			}

			int sw = Integer.parseInt(getProperties().getOrDefault(PROP_STOP_WAIT, String.valueOf(DEFAULT_STOP_WAIT)));
			if(sw > 0) {
				try {
					Thread.sleep(sw * 1000);
				} catch (InterruptedException e) {
					throw new IOException("Interrupted waiting for stop.", e);
				}
			}
		}

		@Override
		protected String[] getStartCommandArgs() {
			return parseCommand(getProperties().get(PROP_START_COMMAND));
		}

	}

}
