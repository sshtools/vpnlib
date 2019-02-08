package com.sshtools.vpnlib;

import java.io.IOException;

public abstract class AbstractVPN implements VPN {

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void addConfiguration(Profile profile) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeConfiguration(Profile profile) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Profile createConfiguration() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Profile getConfiguration(String uuid) throws IOException {
		for (Profile p : getConfigurations()) {
			if (p.getId().equals(uuid))
				return p;
		}
		return null;
	}

	@Override
	public Profile getConfigurationByName(String name) throws IOException {
		for (Profile p : getConfigurations()) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}
}
