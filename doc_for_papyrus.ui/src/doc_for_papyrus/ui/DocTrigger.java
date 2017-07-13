package doc_for_papyrus.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.papyrus.infra.onefile.model.IPapyrusFile;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;
import org.xmlgen.Xmlgen;
import org.xmlgen.notifications.Notification;
import org.xmlgen.notifications.Notification.Gravity;
import org.xmlgen.notifications.Notifications;
import org.xmlgen.notifications.Notified;

public class DocTrigger extends AbstractHandler implements Notified {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		TreeSelection selections = (TreeSelection) HandlerUtil.getActiveWorkbenchWindow(event)
                .getActivePage().getSelection();	
		
		Object selection = selections.getFirstElement();
		
		if (selection instanceof IPapyrusFile)
		{
			IPapyrusFile papyrusFile = (IPapyrusFile) selection;
			IResource[] resources = papyrusFile.getAssociatedResources();
					
			Xmlgen xmlgen = new Xmlgen();
			
			String notation = "'" + resources[1].getFullPath().toString().trim() + "'";
			String uml = "'" + resources[2].getFullPath().toString().trim() + "'";
			
			URL fileURL = bundle.getEntry("/config");
			URL resolvedFileURL = null;
			try {
				resolvedFileURL = FileLocator.toFileURL(fileURL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String cdir = resolvedFileURL.toString();
			String template = "'" + cdir + "docbook_template.orig.xml'";
			String schema = "'http://docbook.org/xml/5.0/xsd/docbook.xsd'";
			String info = "'" + cdir + "info.xml'";			
			
			IPath fullPath = resources[2].getParent().getLocation();
			
			String outputDir = fullPath.toOSString();
			String output = "'" + outputDir + File.separator + "output.xml'";
			String imagesPath = "\"" + "images" + "\"";
			outputDir = "\"" + outputDir + "\"";
			
			String[] vargs = {"uml=", uml,
					          "notation=", notation,
					          "info=", info,					          
					          "outputDir=", outputDir,
					          "imagesPath=", imagesPath,
					          "--template", template,
					          "--schema", schema,
					          "--output", output,
					          "--services", "doc_for_papyrus.ExpansionServices",
					          "--trace"};						
			
			notifications.clearNotifiedQueue();
			notifications.add(this);
			xmlgen.perform(vargs, getClass().getClassLoader());
		}
		
		return null;
	}
	
	@Override
	public void notification(Notification notification) 
	{
    	IStatus status = new Status(StatusSeverity(notification.getGravity()), bundle.getSymbolicName(), notification.toString());
    	log.log(status);	
	}
	
	protected int StatusSeverity(Gravity gravity)
	{
		switch (gravity)
		{
		case Warning:     return IStatus.WARNING;
		case Error:       return IStatus.ERROR;
		case Information: return IStatus.INFO;
		case Fatal:       return IStatus.CANCEL;
		default: 		  return IStatus.OK;
		}
	}
	
	private	Bundle bundle = Platform.getBundle("doc_for_papyrus.ui");
	private Notifications notifications = Notifications.getInstance();		
	private ILog log = Platform.getLog(bundle);
}
