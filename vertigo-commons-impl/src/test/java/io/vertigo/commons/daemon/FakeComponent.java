package io.vertigo.commons.daemon;

import io.vertigo.commons.daemon.DaemonManagerTest.SimpleDaemon;
import io.vertigo.lang.Component;

import java.util.Collections;

import javax.inject.Inject;

public class FakeComponent implements Component {
	@Inject
	public FakeComponent(final DaemonManager daemonManager) {
		daemonManager.registerDaemon("simple", SimpleDaemon.class, 2, Collections.<String, Object> emptyMap());
	}
}