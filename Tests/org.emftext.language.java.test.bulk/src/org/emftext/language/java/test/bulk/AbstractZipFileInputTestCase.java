/*******************************************************************************
 * Copyright (c) 2006-2014
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.java.test.bulk;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.test.AbstractJavaParserTestCase;
import org.emftext.language.java.test.util.ThreadedSuite;

/**
 * An abstract super class for tests that read input from ZIP files.
 */
public abstract class AbstractZipFileInputTestCase extends AbstractJavaParserTestCase {

	private static class DelegatingTestCase extends TestCase {
		
		private final ZipFileEntryTestCase zipFileEntryTestCase;

		private DelegatingTestCase(ZipFileEntryTestCase zipFileEntryTestCase) {
			super("Parse " + (zipFileEntryTestCase.isExcludeFromReprint() ? "" : "and reprint: ") + zipFileEntryTestCase.getZipEntry().getName());
			this.zipFileEntryTestCase = zipFileEntryTestCase;
		}

		@Override
		protected void runTest() throws Throwable {
			zipFileEntryTestCase.runTest();
		}
	}

	private static class CustomClasspathInitializer implements
			JavaClasspath.Initializer {
		
		private final boolean requiresStdLib;
		
		public CustomClasspathInitializer(boolean requiresStdLib) {
			super();
			this.requiresStdLib = requiresStdLib;
		}

		public boolean requiresStdLib() {
			return requiresStdLib;
		}

		public boolean requiresLocalClasspath() {
			return true;
		}

		public void initialize(Resource resource) { }
	}

	protected final static String BULK_INPUT_DIR = "input/";

	/**
	 * Constructs a test suite.
	 * 
	 * @param testFolderName
	 *            name of folder containing the src.zip and additional jars
	 * @param startEntryName
	 *            name of an entry in src.zip as start position of the test
	 * @param threadNumber
	 *            number of threads to use
	 */
	protected static Test constructSuite(String testFolderName,
			String startEntryName, int threadNumber) throws CoreException,
			IOException {
		return constructSuite(testFolderName, startEntryName, threadNumber,
				new String[] {}, false);
	}

	protected static Test constructSuite(String testFolderName,
			String startEntryName, int threadNumber, String[] excludedTests)
			throws CoreException, IOException {
		
		return constructSuite(testFolderName, startEntryName, threadNumber,
				excludedTests, false);
	}

	protected static Test constructSuite(String testFolderName,
			String startEntryName, int threadNumber, String[] excludedTests,
			boolean prefixUsedInZipFile) throws CoreException, IOException {
		
		// run with 'threadNumber' threads and wait for maximal 50 minutes
		TestSuite suite = new ThreadedSuite("Suite testing all files.", 50 * 60 * 1000, threadNumber);

		List<String> inputZips = getInputZips(testFolderName);
		for (String inputZip : inputZips) {
			addToTestSuite(suite, getTestsForZipFileEntries(
					inputZip,
					false,
					startEntryName,
					excludedTests,
					prefixUsedInZipFile));
		}
		return suite;
	}

	protected static List<String> getInputZips(String testFolderName) {
		List<String> result = new ArrayList<String>();

		File dir = new File(BULK_INPUT_DIR);
		File[] folders = dir.listFiles();
		boolean srcZipExists = false;
		for (File folder : folders) {
			if (folder.isDirectory()) {
				for (File srcZipFile: folder.listFiles()) {
					if (srcZipFile.getName().equals("src.zip") && folder.getName().endsWith(testFolderName)) {
						System.out.println("TheTest.getInputZips() " + folder);
						result.add(BULK_INPUT_DIR + folder.getName() +
								File.separator +
								srcZipFile.getName());
						srcZipExists = true;
					}
				}
			}
		}

		if (!srcZipExists) {
			fail("src.zip not found in folder '" + testFolderName + "'.");
		}

		return result;
	}

	protected boolean isExcludedFromReprintTest(String filename) {
		return true;
	}

	protected String getTestInputFolder() {
		return null;
	}

	protected static Collection<Test> getTestsForZipFileEntries(
			String zipFilePath, boolean excludeFromReprint,
			boolean prefixUsedInZipFile) throws IOException, CoreException {
		
		return getTestsForZipFileEntries(zipFilePath, excludeFromReprint, null,
				new String[] {}, prefixUsedInZipFile);
	}

	protected static Collection<Test> getTestsForZipFileEntries(
			String zipFilePath, boolean excludeFromReprint, String startEntry,
			String[] excludedTests, boolean prefixUsedInZipFile)
			throws IOException, CoreException {
		
		Collection<Test> tests = new ArrayList<Test>();
		final ZipFile zipFile = new ZipFile(zipFilePath);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		if (!prefixUsedInZipFile) {
			// Reuse global classpath
			boolean requiresStdLib = false;
			JavaClasspath.getInitializers().add(new CustomClasspathInitializer(requiresStdLib));
			JavaClasspath globalCP = JavaClasspath.get();
			String plainZipFileName = zipFile.getName().substring(AbstractZipFileInputTestCase.BULK_INPUT_DIR.length());
			plainZipFileName = plainZipFileName.substring(0, plainZipFileName.length() - File.separator.length() - "src.zip".length());
			registerLibs("input/" + plainZipFileName, globalCP, "");
		} else {
			// For the JDT test file register only the standard library
			boolean requiresStdLib = true;
			JavaClasspath.getInitializers().add(new CustomClasspathInitializer(requiresStdLib));
			// Trigger initialization
			JavaClasspath.get();
		}


		mainLoop: while (entries.hasMoreElements()) {

			ZipEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (startEntry != null && !entryName.endsWith(startEntry)) {
				continue mainLoop;
			}
			else {
				startEntry = null;
			}
			for (String excludedTest : excludedTests) {
				if (entryName.endsWith(excludedTest)) {
					continue mainLoop;
				}
			}
			if (entryName.endsWith(".java")) {
				final ZipFileEntryTestCase newTest = new ZipFileEntryTestCase(zipFile, entry, excludeFromReprint, prefixUsedInZipFile);
				tests.add(new DelegatingTestCase(newTest));
			}
		}
		return tests;
	}

	protected static Collection<URI> getURIsForZipFileEntries(String zipFilePath) throws IOException, CoreException {
		Collection<URI> streams = new ArrayList<URI>();
		final ZipFile zipFile = new ZipFile(zipFilePath);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".java")) {
				URI sourceURI = URI.createURI("archive:file:///" + new File(".").getAbsoluteFile().toURI().getRawPath() + zipFile.getName().replaceAll("\\\\", "/") + "!/" + entry.getName());
				streams.add(sourceURI);
			}
		}
		return streams;
	}

	protected static void addToTestSuite(TestSuite suite, Collection<Test> tests)
			throws IOException {
		for (Test test : tests) {
			suite.addTest(test);
		}
	}
}
