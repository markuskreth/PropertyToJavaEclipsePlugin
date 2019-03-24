package de.kreth.eclipse.propertytojavaplugin.handlers;

import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import de.kreth.property2java.Configuration;

public class ProjectConfiguration implements Configuration {

	private final Map<String, Reader> input;

	private final Path rootPath;

	private final String packageName;

	public ProjectConfiguration(Map<String, Reader> input, Path rootPath, String packageName) {
		super();
		this.input = input;
		this.rootPath = rootPath;
		this.packageName = packageName;
	}

	@Override
	public Map<String, Reader> getInput() {
		return input;
	}

	@Override
	public String getPackage() {
		return packageName;
	}

	@Override
	public Path getRootPath() {
		return rootPath;
	}
}
