package de.kreth.eclipse.propertytojavaplugin.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GeneratePropertySortSourcesTest {

	private File file1;

	private File file2;

	@Test
	public void testSortList() {
		List<File> sources = new ArrayList<File>();
		sources.add(new File("src/main/src"));
		sources.add(new File("src/main/java"));
		sources.add(new File("src/main/gen"));
		sources.add(new File("src/main/generated"));
		sources.add(new File("src/main/other"));
		sources.sort(GeneratePropertyClassHandler::sortSource);
		assertEquals("generated", sources.get(0).getName());
	}

}
