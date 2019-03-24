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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import de.kreth.eclipse.propertytojavaplugin.Activator;
import de.kreth.property2java.Generator;
import de.kreth.property2java.GeneratorException;

public class GeneratePropertyClassActionDelegate implements IObjectActionDelegate {

	private final ILog log;

	private static final List<String> SOURCE_NAME_ORDER = Arrays.asList("src", "main", "java", "gen", "generated");

	private ISelection selection;

	public GeneratePropertyClassActionDelegate() {
		this(Activator.getDefault().getLog());
	}

	GeneratePropertyClassActionDelegate(ILog log) {
		super();
		this.log = log;
	}

	@Override
	public void run(IAction action) {
		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof IFile) {
					IFile propertyFile = (IFile) firstElement;

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
						log.log(new Status(IStatus.ERROR, Activator.PluginID, "Error starting Generator", e));
						StringWriter out = new StringWriter();
						PrintWriter writer = new PrintWriter(out);
						e.printStackTrace(writer);
						MessageDialog.openError(null, "Generator not started!",
								"Error starting Generator for project "
										+ propertyFile.getProject().getName() + "!\n" + out.toString());
					}
				}
			}
		}
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

		return Optional.empty();
	}

	private boolean isJavaSourceFolder(IPackageFragmentRoot root) throws JavaModelException {

		boolean source = root.getKind() == IPackageFragmentRoot.K_SOURCE;
		String path = root.getPath().toString().toLowerCase();
		source &= !path.contains("test");
		source &= !path.contains("resources");
		return source;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// IWorkbenchPart not relevant.
	}

}
