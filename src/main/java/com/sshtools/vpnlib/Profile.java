package com.sshtools.vpnlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Profile {

	private String id;
	private String name;
	private Map<String, String> properties = new HashMap<>();
	private VPN vpn;
	private List<Option> options = new ArrayList<>();

	public Profile(VPN vpn) {
		this(UUID.randomUUID().toString(), vpn);
	}
	
	public Profile(String id, VPN vpn) {
		this.id = id;
		this.vpn = vpn;
	}
	
	public Profile(String id, String name, VPN vpn) {
		this.id = id;
		this.name = name;
		this.vpn = vpn;
	}
	
	public List<Option> getOptions() {
		return options;
	}
	
	public VPN getVPN() {
		return vpn;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract boolean isActive() throws IOException;

	public abstract void start() throws IOException;

	public abstract void stop() throws IOException;

	@Override
	public String toString() {
		try {
			return "[id=" + id + ", vpn=" + vpn + ", name=" + name + ", active=" + isActive() + "]";
		} catch (IOException e) {
			return "[id=" + id + ", vpn=" + vpn + ", name=" + name + ", active=<error>]";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Profile other = (Profile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
