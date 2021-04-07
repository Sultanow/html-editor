package onlinehilfe.contentbuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.apache.batik.dom.util.XLinkSupport;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.contentbuilder.XslTransformUtil.MultiException;

public class ContentDocumentWriter {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(ContentDocumentWriter.class);
	private static final ILog LOGGER = Platform.getLog(BUNDLE);
	
	private final VelocityEngine ve;
	
	private static final String VM_TEMPLATE_COVER = "cover.vm";
	private static final String VM_TEMPLATE_TOC = "toc.vm";
	private static final String VM_TEMPLATE_FILELIST = "filelist.vm";
	private static final String VM_TEMPLATE_CONTENT = "content.vm"; //Content als Einzeldokumente Velocity-Templating
	private static final String VM_TEMPLATE_CONTENTCOLLECTION = "contentcollection.vm"; //Alle Content Elemente als ein Gesamtdokument
	
	private static final String XSLT_POST_PROCESS_COVER = "cover.postprocess.xsl";
	private static final String XSLT_POST_PROCESS_TOC = "toc.postprocess.xsl";
	private static final String XSLT_POST_PROCESS_FILELIST = "filelist.postprocess.xsl";
	private static final String XSLT_POST_PROCESS_CONTENT = "content.postprocess.xsl"; //Content als Einzeldokumente XSLT-Nachbearbeitung
	private static final String XSLT_POST_PROCESS_CONTENTCOLLECTION = "contentcollection.postprocess.xsl"; //Alle Content Elemente als ein Gesamtdokument XSLT-Nachbearbeitung
	
	private final File targetDir;
	private final File templateDir;
	
	private final Template templateIfExistsVMTemplateCover, templateIfExistsVMTemplateToc, templateIfExistsVMTemplateFilelist, templateIfExistsVMTemplateContent, templateIfExistsVMTemplateContentCollection;
	
	private final XsltProcessor xsltProcessorIfExistsXsltPostVmCover, xsltProcessorIfExistsXsltPostVmToc, xsltProcessorIfExistsXsltPostVmFilelist, xsltProcessorIfExistsXsltPostVmContent, xsltProcessorIfExistsXsltPostVmContentCollection;
	
	private final FilenameCreator filenameCreator;
	
	private Properties veProps = new Properties();
				
	public ContentDocumentWriter(File projectDir, File targetDir, String templatePrefix, FilenameCreator filenameCreator) throws IOException {
		this.targetDir = targetDir;
		this.targetDir.mkdirs();
		this.templateDir = new File(projectDir, "_templates");
		this.filenameCreator = filenameCreator;
		
		//init VelocityEngine
		
		veProps.put("file.resource.loader.path", templateDir.getCanonicalPath());
		veProps.put("input.encoding", FilesUtil.CHARSET_STRING);
		veProps.put("output.encoding", FilesUtil.CHARSET_STRING);
		ve = new VelocityEngine(veProps);
		
		templateIfExistsVMTemplateCover = createVeTemplaleIfTemplateExistsOrNull(templateDir, templatePrefix + VM_TEMPLATE_COVER);
		templateIfExistsVMTemplateToc = createVeTemplaleIfTemplateExistsOrNull(templateDir, templatePrefix + VM_TEMPLATE_TOC);
		templateIfExistsVMTemplateFilelist = createVeTemplaleIfTemplateExistsOrNull(templateDir, templatePrefix + VM_TEMPLATE_FILELIST);
		templateIfExistsVMTemplateContent = createVeTemplaleIfTemplateExistsOrNull(templateDir, templatePrefix + VM_TEMPLATE_CONTENT);
		templateIfExistsVMTemplateContentCollection = createVeTemplaleIfTemplateExistsOrNull(templateDir, templatePrefix + VM_TEMPLATE_CONTENTCOLLECTION);
		
		xsltProcessorIfExistsXsltPostVmCover = createXsltprocessorIfXsltFileExistsOrNull(templateDir, templatePrefix + XSLT_POST_PROCESS_COVER);
		xsltProcessorIfExistsXsltPostVmToc = createXsltprocessorIfXsltFileExistsOrNull(templateDir, templatePrefix + XSLT_POST_PROCESS_TOC);
		xsltProcessorIfExistsXsltPostVmFilelist = createXsltprocessorIfXsltFileExistsOrNull(templateDir, templatePrefix + XSLT_POST_PROCESS_FILELIST);
		xsltProcessorIfExistsXsltPostVmContent = createXsltprocessorIfXsltFileExistsOrNull(templateDir, templatePrefix + XSLT_POST_PROCESS_CONTENT);
		xsltProcessorIfExistsXsltPostVmContentCollection = createXsltprocessorIfXsltFileExistsOrNull(templateDir, templatePrefix + XSLT_POST_PROCESS_CONTENTCOLLECTION);
				
		//bilder und Styles übertragen
		FilesUtil.copyFilesInDirectory(new File(projectDir, "_images"), new File(targetDir, "_images"));
		FilesUtil.copyFilesInDirectory(new File(projectDir, "_styles"), new File(targetDir, "_styles"));
	}

	private Template createVeTemplaleIfTemplateExistsOrNull(File dir, String filename) {
		if ((new File(dir, filename)).exists()) {
			return ve.getTemplate(filename, FilesUtil.CHARSET_STRING);	
		} else {
			return null;
		}
	}
	
	private static XsltProcessor createXsltprocessorIfXsltFileExistsOrNull(File dir, String filename) {
		File file = new File(dir, filename);
		if (file.exists()) {
			return new XsltProcessor(() -> {
				try {
					return new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			});	
		} else {
			return null;
		}
	}
	
	public void buildCover(Charset targetCharset) throws IOException, TransformerException, MultiException {
		
		if (templateIfExistsVMTemplateCover==null) {
			LOGGER.info("Skip. Kein Cover-Template.");
			return;
		}
		
		LOGGER.info("Erstelle Cover-Document.");
			
		Map<String, Object> context = new HashMap<>();
		writeContentToFile(buildOutputFileName("_cover"), templateIfExistsVMTemplateCover, context, targetCharset, xsltProcessorIfExistsXsltPostVmCover);
	}

	public void buildFilelist(ContentMetadata contentMetadata, Charset targetCharset) throws IOException, TransformerException, MultiException {
		
		if (templateIfExistsVMTemplateFilelist == null) {
			LOGGER.info("Skip. Kein Filelist-Template.");
			return;
		}
		
		LOGGER.info("Erstelle FileList-Document.");
		
		List<String> filelist = buildFilelistInternal(contentMetadata);
		
		for (String string : filelist) {
			LOGGER.info(string);
		}
		
		Map<String, Object> context = new HashMap<>();
		context.put("filelist", filelist);
		
		writeContentToFile(buildOutputFileName("_filelist"), templateIfExistsVMTemplateFilelist, context, targetCharset, xsltProcessorIfExistsXsltPostVmFilelist);
	}
	
	private List<String> buildFilelistInternal(ContentMetadata contentMetadata) throws IOException, TransformerException, MultiException {
		List<String> filelist = new ArrayList<String>();
		
		if (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null) {
			filelist.add(buildOutputFileName(contentMetadata).getName());
		}
		
		if (contentMetadata.getSubContent() !=null) {
			for (ContentMetadata subContent : contentMetadata.getSubContent()) {
				filelist.addAll(buildFilelistInternal(subContent));
			}
		}
		
		return filelist;
	}
	
	public void buildToc(ContentMetadata contentMetadata, Charset targetCharset) throws IOException, TransformerException, MultiException {
		
		if (templateIfExistsVMTemplateToc == null) {
			LOGGER.info("Skip. Kein ToC-Template.");
			return;
		}
		
		LOGGER.info("Erstelle ToC-Document.");

		//ToC --> Table Of Contents, ich will nicht jedes mal Inhaltsverzeichnis schreiben
		
		TocEntry toc = buildTocInternal(contentMetadata);
		
		Map<String, Object> context = new HashMap<>();
		context.put("toc", toc);
		
		writeContentToFile(buildOutputFileName("_toc"), templateIfExistsVMTemplateToc, context, targetCharset, xsltProcessorIfExistsXsltPostVmToc);
	}
	
	private TocEntry buildTocInternal(ContentMetadata contentMetadata) throws IOException, TransformerException, MultiException {
		TocEntry tocEntry = new TocEntry();
		tocEntry.setId(contentMetadata.getId());
		tocEntry.setTitle(contentMetadata.getTitle());
		tocEntry.setFilename(buildOutputFileName(contentMetadata).getName());
		
		if (contentMetadata.getSubContent() !=null) {
			for (ContentMetadata subContent : contentMetadata.getSubContent()) {
				TocEntry tocSubEntry = buildTocInternal(subContent);
				tocEntry.addSubEntries(tocSubEntry);
			}
		}
		return tocEntry;
	}
	
	public void buildContent(ContentMetadata contentMetadata, Charset targetCharset) throws IOException, TransformerException, MultiException {
		LOGGER.info("Erstelle Content-Dokumente: " + contentMetadata.getTitle());
		
		buildContent(contentMetadata, null, null, null, null, 0, targetCharset);
	}
		
	public void buildContentcollection(ContentMetadata contentMetadata, Charset targetCharset) throws IOException, TransformerException, MultiException {
		if (templateIfExistsVMTemplateContentCollection == null) {
			LOGGER.info("Skip. Kein ContentCollection-Template.");
			return;
		}
		
		LOGGER.info("Erstelle ContentCollection-Dokument als Sammeldokument der Content-Dokumente: " + contentMetadata.getTitle());
		
		List<ContentMetadata> collectionList = new ArrayList<ContentMetadata>();
		buildContent(contentMetadata, null, null, null, collectionList, 0, targetCharset);
		
		Map<String, Object> context = new HashMap<>();
		context.put("contents", collectionList );
		
		writeContentToFile(buildOutputFileName("_contentcollection"), templateIfExistsVMTemplateContentCollection, context, targetCharset, xsltProcessorIfExistsXsltPostVmContentCollection);
	}
	
	private void initContentMetadata (ContentMetadata contentMetadata) {
		if (contentMetadata!=null) {
			contentMetadata.setFilename(buildOutputFileName(contentMetadata).getName());
		}
	}
	
	private void buildContent(ContentMetadata contentMetadata, ContentMetadata parent, ContentMetadata prev, ContentMetadata next, List<ContentMetadata> collectionList, final int navLevel, Charset targetCharset) throws IOException, TransformerException, MultiException {
		
		int currentNavLevel = navLevel;
		
		// herausfiltern des Root-Containers
		if (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null) {
		
			initContentMetadata(contentMetadata);
			contentMetadata.setNavLevel(++currentNavLevel);
			
			
			
			if (collectionList==null) {
				//wenn ich das template hier einmal brauche und es nicht da ist, dann sind die nachfolgenden schritte auch egal...
				if (templateIfExistsVMTemplateContent == null) {
					LOGGER.info("Skip. Kein Content-Template.");
					return;
				}
				
				LOGGER.info("Erstelle Content-Dokument: " + contentMetadata.getTitle());
				
				Map<String, Object> context = new HashMap<>();
				context.put("content", contentMetadata);
				context.put("contentParent", parent);
				context.put("contentPrev", prev);
				
				initContentMetadata(next); //minimal Vorbefüllung weil ich u.U Daten brauche
				context.put("contentNext", next);
				
				for (ContentMetadata subContent : contentMetadata.getSubContent()) {
					initContentMetadata(subContent);
				}
				context.put("contentSubcontent", contentMetadata.getSubContent());
							
				//XSLT_POST_PROCESS_CONTENT
				writeContentToFile(buildOutputFileName(contentMetadata), templateIfExistsVMTemplateContent, context, targetCharset, xsltProcessorIfExistsXsltPostVmContent);
			} else {
				LOGGER.info("Füge an Content-Dokument: " + contentMetadata.getTitle());
				collectionList.add(contentMetadata);
			}
		}
		
		List<ContentMetadata> subcontent = contentMetadata.getSubContent();
		
		if (subcontent !=null) {
			
			for (int i = 0; i < subcontent.size(); i++) {
				ContentMetadata subContent = subcontent.get(i);
				ContentMetadata subContentPerv = (i>0)?(subcontent.get(i-1)):null;
				ContentMetadata subContentNext = (i+1<subcontent.size())?(subcontent.get(i+1)):null;
				buildContent(subContent, (contentMetadata.getTitle()!=null && contentMetadata.getContentFile()!=null)?contentMetadata:null, subContentPerv, subContentNext, collectionList, currentNavLevel, targetCharset);
			}
		}
	}
			
	private File buildOutputFileName(ContentMetadata contentMetadata, String suffix) {
		return buildOutputFileName(filenameCreator.buildOutputFileName(contentMetadata), suffix);
	}
	
	private File buildOutputFileName(ContentMetadata contentMetadata) {
		return buildOutputFileName(filenameCreator.buildOutputFileName(contentMetadata));
	}
	
	private File buildOutputFileName(String filename) {
		return buildOutputFileName(filename, ".html");
	}
	
	private File buildOutputFileName(String filename, String suffix) {
		return new File(targetDir, filename + suffix);
	}
		
	private static void writeContentToFile(File file, Template contenTemplate, Map<String, Object> context, Charset targetCharset, XsltProcessor postContenTemplateXsltProcessor) throws IOException, TransformerException, MultiException {
		try {
			IContainer contentFolder = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(file.getParentFile().toURI())[0];
			Properties projectDataProperties = FilesUtil.readProjectProperties((IFolder)contentFolder);
			for (String key : projectDataProperties.stringPropertyNames()) {
			    String value = projectDataProperties.getProperty(key);
			    
			    if (value!=null) {
			    	value = value.replace("^\"?([^\\\"]*)\"?$", "$1");
			    }
			    
			    //title -> projectTitle ("." und "-" mapping funktionieren nicht bei der PDF erstellung)
			    context.put("project" + key.substring(0, 1).toUpperCase() + key.substring(1), value);
			}
			
		} catch (IOException|CoreException e) {
			e.printStackTrace();
		}
		context.put("ContentMetadata", ContentMetadata.class); //um statische Methoden zu callen
		context.put("currentdate", new SimpleDateFormat("dd.MMMM YYYY").format(new Date()));
		
		VelocityContext vc = new VelocityContext(context);
		StringWriter sw = new StringWriter();
		contenTemplate.merge(vc, sw);
		String outputContent = sw.toString();
				
		try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			if (postContenTemplateXsltProcessor!=null) {
				InputStream xhtmlInputStream = XslTransformUtil.preConvertHtml2XhtmlInputStream(outputContent, file.getAbsolutePath());
				postContenTemplateXsltProcessor.transform(xhtmlInputStream, outputStream);
			} else {
				outputStream.write(outputContent.getBytes(targetCharset));
			}
		}
	}
}
