package org.emftext.language.java.test.bulk;

import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.JavaClasspath;

public class CustomClasspathInitializer implements JavaClasspath.Initializer {

	private final boolean requiresStdLib;

	public CustomClasspathInitializer(boolean requiresStdLib) {
		this.requiresStdLib = requiresStdLib;
	}

	@Override
	public boolean requiresStdLib() {
		return requiresStdLib;
	}

	@Override
	public boolean requiresLocalClasspath() {
		return true;
	}

	@Override
	public void initialize(Resource resource) {
		// Do nothing
	}
}