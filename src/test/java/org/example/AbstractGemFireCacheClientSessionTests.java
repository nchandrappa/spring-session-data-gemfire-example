package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;

import com.gemstone.gemfire.cache.Region;

/**
 * The AbstractGemFireCacheClientSessionTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public abstract class AbstractGemFireCacheClientSessionTests {

	private final Object LOCK = new Object();

	@Autowired
	private SessionRepository<ExpiringSession> sessionRepository;

	protected abstract Region<Object, ExpiringSession> getSessionRegion();

	protected SessionRepository<ExpiringSession> getSessionRepository() {
		assertThat(sessionRepository).isNotNull();
		return sessionRepository;
	}

	protected ExpiringSession load(Object sessionId) {
		return getSessionRepository().getSession(sessionId.toString());
	}

	protected ExpiringSession loadFromRegion(Object sessionId) {
		return getSessionRegion().get(sessionId);
	}

	protected ExpiringSession newSession() {
		return getSessionRepository().createSession();
	}

	protected ExpiringSession save(ExpiringSession session) {
		getSessionRepository().save(session);
		return session;
	}

	protected ExpiringSession touch(ExpiringSession session) {
		session.setLastAccessedTime(System.currentTimeMillis());
		return session;
	}

	protected void waitOnConditionForDuration(Condition condition, long duration) {
		final long timeout = (System.currentTimeMillis() + duration);

		boolean interrupted = false;

		try {
			// wait uninterrupted...
			while (!condition.evaluate() && System.currentTimeMillis() < timeout) {
				synchronized (LOCK) {
					try {
						TimeUnit.MILLISECONDS.timedWait(LOCK, 500);
					}
					catch (InterruptedException ignore) {
						interrupted = true;
					}
				}
			}
		}
		finally {
			// but, if we were interrupted, reset the interrupt!
			if (interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	protected interface Condition {
		boolean evaluate();
	}

}
