package org.cloudfoundry.reconfiguration.test;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * FactoryBean that will cause an Exception if eagerly initialized through a
 * call to BeanFactory.getBeansOfType or BeanFactory.getBeanNamesForType. The
 * premature instantiation of FactoryBeans done by these methods (in order to
 * call getObjectType() for a possible type match) blows up due to the lack of
 * no-arg constructor and the inability to autowire the constructor dependency.
 * This issue was observed in the field.
 *
 * @author Jennifer Hickey
 *
 */
@SuppressWarnings("rawtypes")
@Component
public class FakeFactoryBean implements FactoryBean {

	@SuppressWarnings("unused")
	private FakeFactoryBeanDependency fakeDep;

	@Autowired
	public FakeFactoryBean(FakeFactoryBeanDependency fakeDep) {
		this.fakeDep = fakeDep;
	}

	@Override
	public String getObject() throws Exception {
		return "Testing";
	}

	@Override
	public Class<?> getObjectType() {
		return String.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
