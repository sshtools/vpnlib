package com.sshtools.vpnlib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sshtools.forker.client.DefaultNonBlockingProcessListener;
import com.sshtools.forker.client.ForkerBuilder;
import com.sshtools.forker.client.ForkerProcess;
import com.sshtools.forker.client.impl.nonblocking.NonBlockingProcess;
import com.sshtools.forker.common.IO;

public abstract class AbstractExternalExecutableProfile extends Profile {

	protected Process process;

	public AbstractExternalExecutableProfile(VPN vpn) {
		super(vpn);
	}

	@Override
	public boolean isActive() throws IOException {
		return process != null && process.isAlive();
	}

	protected abstract String[] getStartCommandArgs();

	@Override
	public void start() throws IOException {
		process = runCommand(getStartCommandArgs());
	}

	protected ForkerProcess runCommand(String[] command) throws IOException {
		ForkerBuilder builder = new ForkerBuilder().io(IO.NON_BLOCKING).redirectErrorStream(true).command(command);
		return builder.start(new DefaultNonBlockingProcessListener() {
			@Override
			public void onStdout(NonBlockingProcess process, ByteBuffer buffer, boolean closed) {
				if (!closed) {
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					System.out.println(new String(bytes));
				}
			}
		});
	}

	@Override
	public void stop() throws IOException {
		if (process == null || !process.isAlive())
			throw new IllegalStateException("Already stopped.");
		try {
			process.destroy();
		} finally {
			try {
				process.waitFor(10, TimeUnit.SECONDS);
			} catch (Exception e) {
			} finally {
				process = null;
			}
		}
	}

	/**
	 * Parse a space separated string into a list, treating portions quotes with
	 * single quotes as a single element. Single quotes themselves and spaces can be
	 * escaped with a backslash.
	 * 
	 * @param command command to parse
	 * @return parsed command
	 */
	protected static String[] parseCommand(String command) {
		List<String> args = new ArrayList<String>();
		boolean escaped = false;
		boolean quoted = false;
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (c == '"' && !escaped) {
				if (quoted) {
					quoted = false;
				} else {
					quoted = true;
				}
			} else if (c == '\\' && !escaped) {
				escaped = true;
			} else if (c == ' ' && !escaped && !quoted) {
				if (word.length() > 0) {
					args.add(word.toString());
					word.setLength(0);
					;
				}
			} else {
				word.append(c);
			}
		}
		if (word.length() > 0)
			args.add(word.toString());
		return args.toArray(new String[0]);
	}
}
