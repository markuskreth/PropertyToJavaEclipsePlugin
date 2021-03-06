package de.kreth.eclipse.propertytojavaplugin.handlers;

import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import de.kreth.property2java.Configuration;

public class ProjectConfiguration implements Configuration {

	private final Map<String, Reader> input;

	private final Path rootPath;

	private final String packageName;

	private final Charset charset;

	public ProjectConfiguration(Map<String, Reader> input, Path rootPath, String packageName, Charset charset) {
		super();
		this.input = input;
		this.rootPath = rootPath;
		this.packageName = packageName;
		this.charset = charset;
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
		if (packageName != null) {
			File targetPath = new File(rootPath.toFile(), packageName.replace('.', File.separatorChar));
			if (!targetPath.exists()) {
				targetPath.mkdirs();
			}
			return targetPath.toPath();
		}
		return rootPath;
	}

	@Override
	public Charset outputCharset() {
		return charset;
	}
}
