/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.web.spring;

import java.util.function.Supplier;

import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;

/**
 * @author Paul Ferraro
 * @deprecated Use {@link org.wildfly.clustering.spring.context.SessionAttributeMarshaller} instead.
 */
@Deprecated(forRemoval = true)
public enum SessionPersistenceGranularity implements Supplier<SessionAttributePersistenceStrategy> {
	SESSION(SessionAttributePersistenceStrategy.COARSE),
	ATTRIBUTE(SessionAttributePersistenceStrategy.FINE),
	;
	private final SessionAttributePersistenceStrategy strategy;

	SessionPersistenceGranularity(SessionAttributePersistenceStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public SessionAttributePersistenceStrategy get() {
		return this.strategy;
	}
}
