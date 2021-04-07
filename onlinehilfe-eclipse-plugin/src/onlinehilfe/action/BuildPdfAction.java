package onlinehilfe.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import onlinehilfe.CurrentPropertiesStore;
import onlinehilfe.contentbuilder.ContentDocumentBuilder;
import onlinehilfe.contentbuilder.FilesUtil;
import onlinehilfe.contentbuilder.Html2Pdf;
import onlinehilfe.contentbuilder.XslTransformUtil.MultiException;
import onlinehilfe.contentbuilder.MetadataEscapedTitleFilenameCreator;
import onlinehilfe.dialogs.MessageBoxUtil;

public class BuildPdfAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	@Override
	public void init(IWorkbenchWindow arg0) {

	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	@Override
	public void run(IAction action) {
		try {
			//Prüfen ob Projekt geöffnet und selektiert wurde
			if (CurrentPropertiesStore.getInstance().getProject()==null || CurrentPropertiesStore.getInstance().getProject().getLocation() == null) {
				MessageBoxUtil.displayError("Sie müssen ein Projekt geöffnet haben!");
				return;
			}
			
			//Bereite Contentbuilder vor 
			ContentDocumentBuilder documentBuilder = new ContentDocumentBuilder(new MetadataEscapedTitleFilenameCreator(), "pdf.", "_target-pdf", "html");
			
			//Ermittle Arbeits- und Zielverzeichniss
			IPath innerTargetLocation = documentBuilder.getTargetLocation();
			File targetLocation = innerTargetLocation.toFile().getParentFile();
			
			//Arbeitsverzeichnis löschen
			FilesUtil.deleteDirectory(innerTargetLocation.toFile());
			
			//Erstelle HTML-Content für PDF-generierung
			documentBuilder.build();
			
			//Zielausgabedatei definieren und löschen wenn existierend
			File outputDebugFile = new File(innerTargetLocation.toFile(), "output-debug.fo");
			if (outputDebugFile.exists() && !outputDebugFile.delete()) {
				MessageBoxUtil.displayError("Die Debug-ausgabe ist blockiert. Wenn diese noch geöffnet ist, müssen sie diese schießen!");
				return;
			}
			File outputFileDst = new File(targetLocation, "output.pdf");
			if (outputFileDst.exists() && !outputFileDst.delete()) {
				MessageBoxUtil.displayError("Die Zielausgabedatei ist blockiert. Wenn diese noch geöffnet ist, müssen sie diese schießen!");
				return;
			}
			
			IPath projectLocation = CurrentPropertiesStore.getInstance().getProject().getLocation();
			File transformationXsl = projectLocation.append("_templates").append("pdf.xhtml2fo.xsl").toFile(); 
			
			//bereite PDFGenerator vor
			Properties documentProperties = new Properties();
			Html2Pdf h2p = new Html2Pdf(
					innerTargetLocation.toFile(),
					transformationXsl,
					documentProperties);
			File contentHtmlFile = innerTargetLocation.append("_contentcollection.html").toFile();
			
			//generiere PDF
			try (OutputStream fopDebugStream = new FileOutputStream(outputDebugFile); OutputStream outputStream = new FileOutputStream(outputFileDst)) {
				h2p.generatePdf(contentHtmlFile, fopDebugStream, outputStream);	
			}
			
			//Arbeitsverzeichnis löschen
			FilesUtil.deleteDirectory(innerTargetLocation.toFile());
						
			MessageBoxUtil.displayMessage("Die Ausleitung als Pdf wurde abgeschlossen und in \""+targetLocation.getCanonicalFile()+"\" abgelegt.\n");
		} catch (CoreException|IOException|TransformerException|FOPException|MultiException e) {
			e.printStackTrace();
			MessageBoxUtil.displayError("Fehler!", e);
		}

	}

}
