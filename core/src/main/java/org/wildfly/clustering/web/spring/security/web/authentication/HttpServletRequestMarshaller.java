/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.web.spring.security.web.authentication;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.infinispan.protostream.descriptors.WireType;
import org.wildfly.clustering.marshalling.protostream.FieldSetMarshaller;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamReader;
import org.wildfly.clustering.marshalling.protostream.ProtoStreamWriter;

/**
 * @author Paul Ferraro
 */
public enum HttpServletRequestMarshaller implements FieldSetMarshaller.Supplied<HttpServletRequest, HttpServletRequestBuilder> {
	INSTANCE;

	private static final int REMOTE_ADDRESS_INDEX = 0;
	private static final int SESSION_ID_INDEX = 1;
	private static final int FIELDS = 2;

	@Override
	public HttpServletRequestBuilder createInitialValue() {
		return new HttpServletRequestBuilder();
	}

	@Override
	public HttpServletRequestBuilder readFrom(ProtoStreamReader reader, int index, WireType type, HttpServletRequestBuilder builder) throws IOException {
		switch (index) {
			case REMOTE_ADDRESS_INDEX:
				return builder.setRemoteAddress(reader.readString());
			case SESSION_ID_INDEX:
				return builder.setSessionId(reader.readString());
			default:
				reader.skipField(type);
				return builder;
		}
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, HttpServletRequest request) throws IOException {
		String remoteAddress = request.getRemoteAddr();
		if (remoteAddress != null) {
			writer.writeString(REMOTE_ADDRESS_INDEX, remoteAddress);
		}
		HttpSession session = request.getSession(false);
		if (session != null) {
			writer.writeString(SESSION_ID_INDEX, session.getId());
		}
	}

	@Override
	public int getFields() {
		return FIELDS;
	}
}
