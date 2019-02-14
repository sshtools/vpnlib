package com.sshtools.vpnlib;

public class UsernameCredentials implements Credentials {
	private String username;
	private char[] password;

	public UsernameCredentials() {
	}

	public UsernameCredentials(String username, char[] password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

}
