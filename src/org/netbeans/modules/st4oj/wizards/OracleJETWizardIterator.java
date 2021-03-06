package org.netbeans.modules.st4oj.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.openide.WizardDescriptor;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

@TemplateRegistrations({
    @TemplateRegistration(
            folder = "ClientSide",
            displayName = "#OracleJETWizardIterator_empty_displayName",
            iconBase = "org/netbeans/modules/st4oj/resources/ojet-icon.png",
            description = "JETModule.html",
            content = "../resources/empty/empty.js",
            position = 0,
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            content = "../resources/empty/empty.html",
            position = 10,
            category = "hidden",
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            displayName = "#OracleJETWizardIterator_table_displayName",
            iconBase = "org/netbeans/modules/st4oj/resources/ojet-icon.png",
            description = "JETModule.html",
            content = "../resources/table/table.js",
            position = 15,
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            content = "../resources/table/table.html",
            position = 20,
            category = "hidden",
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            displayName = "#OracleJETWizardIterator_ajax_displayName",
            iconBase = "org/netbeans/modules/st4oj/resources/ojet-icon.png",
            description = "JETModule.html",
            content = "../resources/ajax/ajax.js",
            position = 25,
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            content = "../resources/ajax/cd_catalog.xml",
            position = 55,
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            content = "../resources/ajax/ajax.html",
            position = 30,
            category = "hidden",
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            displayName = "#OracleJETWizardIterator_kohelloworld_displayName",
            iconBase = "org/netbeans/modules/st4oj/resources/ojet-icon.png",
            description = "JETModule.html",
            content = "../resources/kohelloworld/home.js",
            position = 35,
            scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "ClientSide",
            content = "../resources/kohelloworld/home.html",
            position = 40,
            category = "hidden",
            scriptEngine = "freemarker")
})
public final class OracleJETWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();

            // Change to default new file panel and add our panel at bottom
            Project project = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);

            panels.add(new OracleJETWizardPanel(project, groups));

            String[] steps = createSteps();
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public Set<?> instantiate() throws IOException {
        //Get the class:
        String className = Templates.getTargetName(wizard);

        // FreeMarker Template will get its variables from HashMap.
        // HashMap key is the variable name.
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("className", className); // NOI18N

        LinkedHashSet<DataObject> files = new LinkedHashSet<DataObject>();

        //Get the source folder
        FileObject dir = Templates.getTargetFolder(wizard);
        DataFolder targetFolderName = DataFolder.findFolder(dir);

        FileObject htmlDir = targetFolderName.getPrimaryFile().getParent().getFileObject("views");
        if (htmlDir == null) {
            htmlDir = targetFolderName.getPrimaryFile().getParent().createFolder("views");
        }
        DataFolder targetHtmlFolderName = DataFolder.findFolder(htmlDir);

        //Get the template and convert it:
        FileObject sourceTemplate = Templates.getTemplate(wizard);
        FileObject headerTemplate = FileUtil.findBrother(sourceTemplate, "html"); // NOI18N
        if (headerTemplate != null) {
            DataObject dobjHeader = DataObject.find(headerTemplate);
            files.add(dobjHeader.createFromTemplate(targetHtmlFolderName, className, args));
        }

        DataObject dobjSource = DataObject.find(sourceTemplate);

        //Define the template from the above,
        //passing the package, the file name, and the map of strings to the template:
        DataObject dobj = dobjSource.createFromTemplate(targetFolderName, className, args);
        files.add(dobj);

        return files;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = (String[]) wizard.getProperty("WizardPanel_contentData");
        assert beforeSteps != null : "This wizard may only be used embedded in the template wizard";
        String[] res = new String[(beforeSteps.length - 1) + panels.size()];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels.get(i - beforeSteps.length + 1).getComponent().getName();
            }
        }
        return res;
    }
}
