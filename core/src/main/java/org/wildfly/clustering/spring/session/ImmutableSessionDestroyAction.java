/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.clustering.spring.session;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.Session;
import org.wildfly.clustering.cache.batch.Batch;
import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.user.User;
import org.wildfly.clustering.session.user.UserManager;

/**
 * @author Paul Ferraro
 */
public class ImmutableSessionDestroyAction<B extends Batch> implements BiConsumer<ImmutableSession, BiFunction<Object, Session, ApplicationEvent>> {

	private final ApplicationEventPublisher publisher;
	private final ServletContext context;
	private final IndexingConfiguration<B> indexing;

	public ImmutableSessionDestroyAction(ApplicationEventPublisher publisher, ServletContext context, IndexingConfiguration<B> indexing) {
		this.publisher = publisher;
		this.context = context;
		this.indexing = indexing;
	}

	@Override
	public void accept(ImmutableSession session, BiFunction<Object, Session, ApplicationEvent> eventFactory) {
		ApplicationEvent event = eventFactory.apply(this, new DistributableImmutableSession(session));
		this.publisher.publishEvent(event);
		HttpSession httpSession = JakartaServletFacadeProvider.INSTANCE.asSession(session, this.context);
		for (Map.Entry<String, HttpSessionBindingListener> entry : session.getAttributes().getAttributes(HttpSessionBindingListener.class).entrySet()) {
			HttpSessionBindingListener listener = entry.getValue();
			try {
				listener.valueUnbound(new HttpSessionBindingEvent(httpSession, entry.getKey(), listener));
			} catch (Throwable e) {
				this.context.log(e.getMessage(), e);
			}
		}

		// Remove any associated indexes
		Map<String, String> indexes = this.indexing.getIndexResolver().resolveIndexesFor(new DistributableImmutableSession(session));
		for (Map.Entry<String, String> entry : indexes.entrySet()) {
			UserManager<Void, Void, String, String, B> manager = this.indexing.getUserManagers().get(entry.getKey());
			if (manager != null) {
				try (B batch = manager.getBatcher().createBatch()) {
					User<Void, Void, String, String> sso = manager.findUser(entry.getValue());
					if (sso != null) {
						sso.invalidate();
					}
				}
			}
		}
	}
}