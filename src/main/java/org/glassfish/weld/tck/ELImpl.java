/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.weld.tck;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.cdi.tck.spi.EL;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.module.web.el.WeldELContextListener;
import org.jboss.weld.module.web.el.WeldExpressionFactory;
import org.jboss.weld.manager.BeanManagerImpl;


public class ELImpl implements EL {

    public static final ExpressionFactory EXPRESSION_FACTORY = new WeldExpressionFactory(ExpressionFactory.newInstance());

    public static final ELContextListener[] EL_CONTEXT_LISTENERS = { new WeldELContextListener() };

    @SuppressWarnings("unchecked")
    public <T> T evaluateValueExpression(BeanManager beanManager, String expression, Class<T> expectedType) {
        ELContext elContext = createELContext(beanManager);
        return (T) EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluateMethodExpression(BeanManager beanManager, String expression, Class<T> expectedType,
            Class<?>[] expectedParamTypes, Object[] expectedParams) {
        ELContext elContext = createELContext(beanManager);
        return (T) EXPRESSION_FACTORY.createMethodExpression(elContext, expression, expectedType, expectedParamTypes).invoke(
                elContext, expectedParams);
    }

    public ELContext createELContext(BeanManager beanManager) {
        if (beanManager instanceof BeanManagerProxy) {
            BeanManagerProxy proxy = (BeanManagerProxy) beanManager;
            beanManager = proxy.delegate();
        }
        if (beanManager instanceof BeanManagerImpl) {
            return createELContext((BeanManagerImpl) beanManager);
        } else {
            throw new IllegalStateException("Wrong manager");
        }
    }

    private ELContext createELContext(BeanManagerImpl beanManagerImpl) {

        final ELResolver resolver = createELResolver(beanManagerImpl);

        ELContext context = new ELContext() {

            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return null;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return null;
            }

        };
        callELContextListeners(context);
        return context;
    }

    private ELResolver createELResolver(BeanManagerImpl beanManagerImpl) {
        CompositeELResolver resolver = new CompositeELResolver();
        resolver.add(beanManagerImpl.getELResolver());
        resolver.add(new MapELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new ArrayELResolver());
        resolver.add(new ResourceBundleELResolver());
        resolver.add(new BeanELResolver());
        return resolver;
    }

    private void callELContextListeners(ELContext context) {
        ELContextEvent event = new ELContextEvent(context);
        for (ELContextListener listener : EL_CONTEXT_LISTENERS) {
            listener.contextCreated(event);
        }
    }

}
