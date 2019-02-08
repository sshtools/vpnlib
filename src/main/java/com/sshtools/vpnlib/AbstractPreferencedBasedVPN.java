package com.sshtools.vpnlib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public abstract class AbstractPreferencedBasedVPN extends AbstractVPN {

	private Preferences preferences;

	protected AbstractPreferencedBasedVPN(Preferences preferences) {
		this.preferences = preferences;
	}

	@Override
	public Profile getConfiguration(String uuid) throws IOException {
		try {
			if (preferences.nodeExists(uuid)) {
				return nodeToProfile(preferences.node(uuid));
			}
		} catch (BackingStoreException bse) {
			throw new IOException("Failed to get configuration.", bse);
		}
		return null;
	}

	@Override
	public Profile getConfigurationByName(String name) throws IOException {
		try {
			for (String c : preferences.childrenNames()) {
				Preferences n = preferences.node(c);
				if(n.get("name", "").equals(name))
					return nodeToProfile(n);
			}
		} catch (BackingStoreException e) {
			throw new IOException("Failed to remove profile.", e);
		}
		return null;
	}

	@Override
	public List<Profile> getConfigurations(Option... options) throws IOException {
		try {
			List<Profile> p = new ArrayList<>();
			for (String c : preferences.childrenNames())
				p.add(nodeToProfile(preferences.node(c)));
			return p;
		} catch (BackingStoreException e) {
			throw new IOException("Failed to remove profile.", e);
		}
	}

	@Override
	public void addConfiguration(Profile profile) throws IOException {
		Preferences node = preferences.node(profile.getId());
		node.put("name", profile.getName());
		for (Map.Entry<String, String> en : profile.getProperties().entrySet())
			node.put(en.getKey(), en.getValue());
	}

	@Override
	public void removeConfiguration(Profile profile) throws IOException {
		try {
			preferences.node(profile.getId()).removeNode();
		} catch (BackingStoreException e) {
			throw new IOException("Failed to remove profile.", e);
		}
	}

	protected Profile nodeToProfile(Preferences node) throws IOException {
		Profile p = createConfiguration();
		try {
			for (String k : node.keys()) {
				if (k.equals("name"))
					p.setName(node.get(k, ""));
				else
					p.getProperties().put(k, node.get(k, ""));
			}
		} catch (BackingStoreException e) {
			throw new IOException("Failed to convert preferences to a profile.", e);
		}
		return p;
	}

}
