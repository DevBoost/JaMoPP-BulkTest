package org.emftext.language.java.test.bulk;

import junit.framework.TestCase;

public class DelegatingTestCase extends TestCase {

	private final ZipFileEntryTestCase zipFileEntryTestCase;

	public DelegatingTestCase(ZipFileEntryTestCase zipFileEntryTestCase) {
		super("Parse " + (zipFileEntryTestCase.isExcludeFromReprint() ? "" : "and reprint: ")
				+ zipFileEntryTestCase.getZipEntry().getName());
		this.zipFileEntryTestCase = zipFileEntryTestCase;
	}

	@Override
	protected void runTest() throws Throwable {
		zipFileEntryTestCase.runTest();
	}
	
	@Override
	protected void tearDown() throws Exception {
		zipFileEntryTestCase.tearDown();
	}
}