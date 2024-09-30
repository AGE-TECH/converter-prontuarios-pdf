package br.com.age.converter.prontuarios.pdf.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import br.com.age.converter.prontuarios.pdf.exceptions.FalhaConversaoPdfException;

public class MakeDocumento {
	
	private Document document = null;
	private OutputStream outputStream;
	private PdfWriter pdfWriter = null;

	protected MakeDocumento() {
	}
	
	public MakeDocumento(OutputStream outputStream) throws Exception {
		init(outputStream, PageSize.A4);
	}
	
	private void init(OutputStream outputStream, Rectangle pageSize) throws Exception {
		document = new Document(pageSize);
		
		this.outputStream = outputStream;
		this.outputStream.flush();
		
		pdfWriter = PdfWriter.getInstance(document, this.outputStream);
	}
	
	public void html2PDF(InputStream inp, OutputStream out, LambdaLogger logger, String awsRequestId) throws FalhaConversaoPdfException {
		html2PDF(inp, out, 0, 0, logger, awsRequestId);
	}
	
	public void html2PDF(InputStream inp, OutputStream out, float dotsPerPoint, int dotsPerPixel, LambdaLogger logger, String awsRequestId) throws FalhaConversaoPdfException{
		try {
		    File tmpFile = getFonteVerdanaDaTabelaTemporariaDoLambda();
			
			Tidy tidy = new Tidy();
			
			tidy.setXHTML(true);
			tidy.setTidyMark(false);		 	/* Marcação para indicar que o documento é do tipo Tidy */
			tidy.setDocType("auto");
			tidy.setWrapScriptlets(true);		/* Trata um elemento com uma descrição muito longa */
			tidy.setShowWarnings(false);
			tidy.setDropEmptyParas(true);		/* Remove tag <p> vazias e substitui por <br> */
			tidy.setDropFontTags(false);		/* Remove tag's <font> e <center> por style */
			tidy.setQuiet(true);				/* Remover sugestoes de melhorias no html - mantem a visualizacao de errosZ*/
			tidy.setShowErrors(0);
			
			org.w3c.dom.Document doc = tidy.parseDOM(inp, null);
			
			ITextRenderer renderer;
			if ( dotsPerPoint == 0 || dotsPerPixel == 0 ) {
				renderer = new ITextRenderer();
			} else {
				renderer = new ITextRenderer(dotsPerPoint, dotsPerPixel);
			}
			
			logTemporarioLevelDebug("Antes setDocument - ", logger, awsRequestId);
			
			renderer.setDocument(doc, null);  
			renderer.getFontResolver().addFont(tmpFile.getAbsolutePath(), true);

			logTemporarioLevelDebug("Antes Layout - ", logger, awsRequestId);
			
			renderer.layout();
			renderer.createPDF(out,true);
			
			tmpFile.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new FalhaConversaoPdfException("Erro no html2Pdf");
		}
	}

	private void logTemporarioLevelDebug(String msg, LambdaLogger logger, String awsRequestId) {
		logger.log(msg + awsRequestId, LogLevel.DEBUG);
	}
	
	private File getFonteVerdanaDaTabelaTemporariaDoLambda() throws IOException {
		InputStream is = getClass().getResourceAsStream("/fontes/verdana.TTF");
		File tmpFile = new File(System.getenv("ATTR_PASTA_TMP"), "verdana.TTF");
		
		try(FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)){
			byte[] buffer = new byte[4096];
		    int bytesRead;
		    
		    while ((bytesRead = is.read(buffer)) != -1) {
		    	fileOutputStream.write(buffer, 0, bytesRead);
		    }
		}
		
		return tmpFile;
	}
	
	public void setMargins(float mE, float mD, float mS, float mI){
		document.setMargins(mE, mD, mS, mI); 
	}
	
	public void setPage(Rectangle rec){
		this.document.setPageSize(rec);
	}
	
	public PdfWriter getPdfWrite(){
		return pdfWriter;
	}
	
	public void add(Element element) throws DocumentException {
		this.document.add(element);
		
		if(element instanceof Paragraph){
			this.document.add(new Phrase());
		}
	}
	
	public void setBackgroundImg(Image img) throws DocumentException {
		try {
			PdfContentByte cb = pdfWriter.getDirectContentUnder(); 
			cb.addImage(img);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}
	}
	
	public void open(){		
		this.document.open();		
	}
	
	public void newPage() {
		this.document.newPage();
	}
	
	public void close() { 
		this.document.close();
	}
	
}
