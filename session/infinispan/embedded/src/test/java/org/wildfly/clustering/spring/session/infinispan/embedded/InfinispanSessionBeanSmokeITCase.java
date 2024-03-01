/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.spring.session.infinispan.embedded;

import java.net.URI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.clustering.spring.session.context.xml.XmlContextLoaderListener;
import org.wildfly.clustering.spring.session.servlet.SessionServlet;

/**
 * @author Paul Ferraro
 */
@ExtendWith(ArquillianExtension.class)
public class InfinispanSessionBeanSmokeITCase extends AbstractInfinispanSessionSmokeITCase {

	@Deployment(name = DEPLOYMENT_1, testable = false)
	@TargetsContainer(CONTAINER_1)
	public static Archive<?> deployment1() {
		return deployment();
	}

	@Deployment(name = DEPLOYMENT_2, testable = false)
	@TargetsContainer(CONTAINER_2)
	public static Archive<?> deployment2() {
		return deployment();
	}

	private static Archive<?> deployment() {
		return deployment(InfinispanSessionBeanSmokeITCase.class)
				.addPackage(XmlContextLoaderListener.class.getPackage())
				.addAsWebInfResource(InfinispanSessionBeanSmokeITCase.class.getPackage(), "applicationContext.xml", "applicationContext.xml")
				;
	}

	@ArquillianResource(SessionServlet.class)
	@OperateOnDeployment(DEPLOYMENT_1)
	private URI baseURI1;

	@ArquillianResource(SessionServlet.class)
	@OperateOnDeployment(DEPLOYMENT_2)
	private URI baseURI2;

	@Test
	@RunAsClient
	public void test() throws Exception {
		this.accept(this.baseURI1, this.baseURI2);
	}
}