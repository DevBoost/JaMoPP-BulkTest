package org.emftext.language.java.test.bulk;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipFile;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.JavaClasspath.Initializer;

public class ZipFileClasspathSetter implements IClasspathSetter {
	
	private final ZipFile zipFile;

	public ZipFileClasspathSetter(ZipFile zipFile) {
		this.zipFile = zipFile;
	}

	@Override
	public void setUpClasspath(ResourceSet resourceSet) throws Exception {
		// Create custom class path setup for resource set
		boolean requiresStdLib = false;
		Initializer classpathInitializer = new CustomClasspathInitializer(requiresStdLib);
		Set<Initializer> classpathInitializers = Collections.singleton(classpathInitializer);
		JavaClasspath localClasspath = JavaClasspath.get(resourceSet, classpathInitializers);
		String plainZipFileName = zipFile.getName().substring(AbstractZipFileInputTestCase.BULK_INPUT_DIR.length());
		plainZipFileName = plainZipFileName.substring(0, plainZipFileName.length() - File.separator.length()
				- "src.zip".length());
		AbstractZipFileInputTestCase.registerLibs("input/" + plainZipFileName, localClasspath, "");
	}
}
