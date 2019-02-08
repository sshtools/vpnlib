package com.sshtools.vpnlib;

import java.io.IOException;
import java.util.List;

public interface VPN {
	
	boolean isSystem();
	
	Profile createConfiguration() throws IOException;
	
	void addConfiguration(Profile profile) throws IOException;
	
	void removeConfiguration(Profile profile) throws IOException;
	
	boolean isAvailable();

	String getName();
	
	List<Profile> getConfigurations(Option... options) throws IOException;
 
	void configure();

	Profile getConfiguration(String uuid) throws IOException;

	Profile getConfigurationByName(String name) throws IOException;
}
