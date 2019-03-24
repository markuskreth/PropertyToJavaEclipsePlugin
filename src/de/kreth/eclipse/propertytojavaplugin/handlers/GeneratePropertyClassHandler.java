package de.kreth.eclipse.propertytojavaplugin.handlers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.kreth.eclipse.propertytojavaplugin.Activator;
import de.kreth.property2java.Generator;
import de.kreth.property2java.GeneratorException;

public class GeneratePropertyClassHandler extends AbstractHandler {

	private static final List<String> SOURCE_NAME_ORDER = Collections
			.unmodifiableList(Arrays.asList("generated", "gen", "gen", "src", "java", "main"));

	private ILog log;

	public GeneratePropertyClassHandler() {
		this(Activator.getDefault().getLog());
	}

	public GeneratePropertyClassHandler(ILog log) {
		this.log = log;
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		IFile propertyFile = extractSelectedFile(arg0);
		if (propertyFile != null) {

			Optional<File> sourcePaths = extractSources(propertyFile.getProject());

			log.log(new Status(IStatus.OK, Activator.PluginID, "Generating Java class for " + propertyFile));
			log.log(new Status(IStatus.OK, Activator.PluginID, "Generating Java class to " + sourcePaths));

			try {
				ProjectConfiguration config = createConfiguration(sourcePaths, propertyFile);
				if (config != null) {
					Generator generator = new Generator(config);
					generator.start();
				}
				else {
					MessageDialog.openWarning(null, "Generator not started!",
							"Couldn't find source path of projekt "
									+ propertyFile.getProject().getName() + "!");
				}
			}
			catch (IOException | GeneratorException e) {
				logException(propertyFile, e);
			}
		}
		return null;
	}

	public void logException(IFile propertyFile, Exception e) {
		log.log(new Status(IStatus.ERROR, Activator.PluginID, "Error starting Generator", e));
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		e.printStackTrace(writer);
		MessageDialog.openError(null, "Generator not started!",
				"Error starting Generator for project "
						+ propertyFile.getProject().getName() + "!\n" + out.toString());
	}

	private ProjectConfiguration createConfiguration(Optional<File> sourcePaths, IFile propertyFile)
			throws IOException {

		IPath propertyFileLocation = propertyFile.getLocation();
		Map<String, Reader> input = new HashMap<>();
		File file = propertyFileLocation.toFile();
		input.put(file.getName(), new FileReader(file));
		if (sourcePaths.isPresent()) {

			Path rootPath = sourcePaths.get().toPath();
			String packageName = null;
			return new ProjectConfiguration(input, rootPath, packageName);
		}
		else {
			return null;
		}
	}

	private IFile extractSelectedFile(ExecutionEvent arg0) {

		ISelection selection = HandlerUtil.getActiveMenuSelection(arg0);
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IFile) {
				return (IFile) firstElement;
			}
		}
		return null;
	}

	private Optional<File> extractSources(IProject project) {

		List<File> sourcePaths = new ArrayList<>();

		try {

			IJavaProject javaProject = JavaCore.create(project);

			try {
				for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
					if (isJavaSourceFolder(root)) {
						sourcePaths.add(root.getResource().getLocation().toFile());
					}
				}
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (!sourcePaths.isEmpty()) {
			sourcePaths.sort(GeneratePropertyClassHandler::sortSource);
			return Optional.of(sourcePaths.get(0));
		}
		return Optional.empty();
	}

	public static int sortSource(File file1, File file2) {

		int index1 = SOURCE_NAME_ORDER.indexOf(file1.getName());
		int index2 = SOURCE_NAME_ORDER.indexOf(file2.getName());
		if (index1 == index2) {
			return file1.getPath().compareTo(file2.getPath());
		}
		else {
			if (index1 < 0) {
				index1 = Integer.MAX_VALUE;
			}
			if (index2 < 0) {
				index2 = Integer.MAX_VALUE;
			}
			return Integer.compare(index1, index2);
		}
	}

	private boolean isJavaSourceFolder(IPackageFragmentRoot root) throws JavaModelException {

		boolean source = root.getKind() == IPackageFragmentRoot.K_SOURCE;
		String path = root.getPath().toString().toLowerCase();
		source &= !path.contains("test");
		source &= !path.contains("resources");
		return source;
	}

}