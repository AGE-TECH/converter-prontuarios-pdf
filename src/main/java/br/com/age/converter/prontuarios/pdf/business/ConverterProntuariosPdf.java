package br.com.age.converter.prontuarios.pdf.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.google.gson.JsonObject;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

import br.com.age.converter.prontuarios.pdf.dao.ConverterProntuariosPdfDao;
import br.com.age.converter.prontuarios.pdf.db.Database;
import br.com.age.converter.prontuarios.pdf.db.DatabaseReplica;
import br.com.age.converter.prontuarios.pdf.enums.ClassificacaoTipoProntuario;
import br.com.age.converter.prontuarios.pdf.exceptions.BusinessException;
import br.com.age.converter.prontuarios.pdf.exceptions.FalhaConversaoPdfException;
import br.com.age.converter.prontuarios.pdf.exceptions.FalhaGeracaoDocumentoException;
import br.com.age.converter.prontuarios.pdf.exceptions.GedMesmoNomeException;
import br.com.age.converter.prontuarios.pdf.exceptions.SemResultadoException;
import br.com.age.converter.prontuarios.pdf.helper.ArquivoGedHelper;
import br.com.age.converter.prontuarios.pdf.helper.VaultHelper;
import br.com.age.converter.prontuarios.pdf.utils.InfraConfiguracaoKey;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.ArquivoGedVo;
import br.com.age.converter.prontuarios.pdf.vo.BiometriaVo;
import br.com.age.converter.prontuarios.pdf.vo.EmpVo;
import br.com.age.converter.prontuarios.pdf.vo.FuncionarioVo;
import br.com.age.converter.prontuarios.pdf.vo.GedVo;
import br.com.age.converter.prontuarios.pdf.vo.InfraConfiguracaoVo;
import br.com.age.converter.prontuarios.pdf.vo.LogVo;
import br.com.age.converter.prontuarios.pdf.vo.ParametrosVo;
import br.com.age.converter.prontuarios.pdf.vo.QRCodeVo;
import br.com.age.converter.prontuarios.pdf.vo.SoParamVo;
import br.com.age.converter.prontuarios.pdf.vo.UsuarioVo;

public class ConverterProntuariosPdf {

	private ConverterProntuariosPdfDao dao;
	private InfraConfiguracao infraConfiguracaoBusiness;
	
	private ParametrosVo parametrosVo;
	private GedVo gedVo;
	private ArquivoGedVo arquivoGedPedProcVo;
	private SoParamVo soParamVo;
	
	private static final int TAMANHO_CAMPO_OBSERVACAO_LOGACAO = 4000;
	
	private static final String REGEX_IMG_QRCODE = "<img([^>]*(src=['\"][^\"'>]*[\"'])[^>]*data-qrcode[^>]*|[^>]*data-qrcode[^>]*(src=['\"][^\"'>]*[\"'])[^>]*)>";
	private static final String TEMPLATE_MENSAGEM_LOG_MUITO_GRANDE = "Observação de log muito grande: {0} - {1} - {2} - {3} - [{4}] {5}";
	private static final String SOURCE_SOC = "source_soc";
	private static final String CERTIFICADO_EM_PDF = "Certificado em PDF";
	private static final String GERAR_PDF_CERTIFICADO_INDIVIDUAL = "Gerar PDF Certificado Individual";
	private static final String GERAR_PDF_CERTIFICADO_COLETIVO = "Gerar PDF Certificado Coletivo";
	private static final String RELATORIO_COLETIVO_PDF = "Relatório Coletivo em PDF";
	private static final String H_SOC = "h-";
	private static final String B_SOC = "b-";
	private static final String P_SOC = "p-";
	private static final String LOCAL = "local";
	private static final String WEBSOC_PADRAO = "/WebSoc";
	private static final String MANDATO_CIPA = "CIPA_";
	private static final String TEMPLATE_NOME_ARQUIVO = "%s_(%s)";
	private static final String COD_REPL_EMPPRI	= "XX_EMPPRI_XX";
	private static final String COD_REPL_EMP = "XX_EMP_XX";
	private static final String CLASS_QRCODE_A_REMOVER = "class=\"qrcode-invisivel\"";
	private static final String TAG_STYLE = "<(style)"; 
	private static final String CAD_177 = "cad177";
	private static final String CAMINHO_IMAGEM_BIOMETRICA_HTTP = "#caminhoImagemBiometricaHttp";
	
	private String awsRequestId;
	private LambdaLogger logger;
	
	public ConverterProntuariosPdf(Context context, ParametrosVo parametrosVo, Database database, DatabaseReplica databaseReplica) {
		this.dao = new ConverterProntuariosPdfDao(database, databaseReplica);
		this.parametrosVo = parametrosVo;
		this.awsRequestId = context.getAwsRequestId();
		this.infraConfiguracaoBusiness = new InfraConfiguracao(dao);
		this.logger = context.getLogger();
	}
	
	public String converterProntuario() throws SemResultadoException, FalhaGeracaoDocumentoException, BusinessException {
		getSoParamVo();
		
		String urlvault = infraConfiguracaoBusiness.getInfraConfiguracaoPeloCodigo(InfraConfiguracaoKey.URL_VAULT).getValorRetorno();
		System.setProperty("urlRestVault", urlvault);
		
		byte[] byteArray = gerarDocumento();
		
		if(byteArray == null) {
			throw new SemResultadoException("Sem resultado. Sem bytes gerados");
		}
		
		if(!Utils.isNullOrEmptyOrZero(parametrosVo.getCodigoSequencialFicha())){
			buscarCodigoGedDocumentoProntuarioMedico();
			
		} else if (parametrosVo.getNomeGed().contains(MANDATO_CIPA)){
			buscarCodigoGedDocumentoCipa();
		}
		
		inserirGedEArquivoGed(byteArray);
		atualizarFichaXBiometria();
		
		return montarJsonResponse();
	}

	private void getSoParamVo() {
		soParamVo = dao.getSoParam();
	}

	private String montarJsonResponse() {
		JsonObject response = new JsonObject();
		
		response.addProperty("codigoEmpresa", arquivoGedPedProcVo.getCodigoEmpresa());
		response.addProperty("codigoGed", arquivoGedPedProcVo.getCodigoGed());
		response.addProperty("codigoArquivoGed", arquivoGedPedProcVo.getCodigoArquivoGed());
		response.addProperty("nomeDoArquivo", arquivoGedPedProcVo.getNomeDoArquivo());
		response.addProperty("rowid", arquivoGedPedProcVo.getRowid());
		response.addProperty("codigoTipoDocumento", arquivoGedPedProcVo.getCodigoTipoDocumento());
		response.addProperty("indice", parametrosVo.getIndexDocumento());
		response.addProperty("codigoResponsavelAcao", parametrosVo.getCodigoResponsavelAcao());
		
		return response.toString();
	}
	
	public byte[] gerarDocumento() throws FalhaGeracaoDocumentoException {
		String html = parametrosVo.getHtml();
		
		try {
			html = substituirPularLinhaPorBrETagAddTabPorEspaco(html);
			html = removeConteudoEntreAgeNaoGravar(html);
			html = removeConteudoEntreBiometriaNaoGravar(html);
			html = substituiSourceSocPorSrc(html);
			html = ativaCssExclusivoParaAsConversoesEmPDF(html);
			html = ajustaProporcaoCamposTabelaExameDataEmBranco(html);
			html = corrigeNomeFonteVerdana(html);
			
			boolean ehOPedidoDeExamesDaUnimedCampinas	= html.contains("Pedido Exames - Unimed Campinas");
			boolean ehAFichaClinicaCustomizadaMantris 	= html.contains("data-template=\"fichaClinicaDaMantris\"");
			boolean ehFichaOdontologica = html.contains("div-odontograma");
			boolean ehModeloPersonalizado = html.contains("data-template");
			boolean asoPadraoOtimizado = html.contains("asoPadraoOtimizado");
			boolean audiometria = html.contains("cabecalhoAudiometria");
			boolean imprimePaginaTamanhoLetter = html.contains("@page{size:letter!important;}");
			boolean imprimePaginaTamanhoLegal = asoPadraoOtimizado || (audiometria && !imprimePaginaTamanhoLetter);
			boolean printVertical = isLayoutImpressaoRetrato(html, ehModeloPersonalizado);
			boolean possuiPersonalizacao = parametrosVo.isPossuiPersonalizacao();

			Image imagemDeFundo = definirImagemDeFundoCertificado(printVertical, ehModeloPersonalizado);
			
			if (!ehOPedidoDeExamesDaUnimedCampinas && !ehAFichaClinicaCustomizadaMantris && !possuiPersonalizacao) {
				html = adicionaCSSDefault(html, printVertical, imprimePaginaTamanhoLegal);
				html = adicionaCssCustomizado(html, " table { word-wrap: break-word; } </style>");
			}
			
			if(ehFichaOdontologica){
				html = adicionaCssCustomizado(html, getCssOdontograma());
			}
			
			html = substituirEnderecoExternoServletMService(html);
			html = substituiEnderecoDasImagensEstaticas(html);
			html = substituirParametroQrCode(html);
			html = substituirImagemBiometrica(html);
			html = substituirEnderecoDosLinksEImagensUpload(html);
			
			byte[] pdf = criarArquivoPdf(html, ehAFichaClinicaCustomizadaMantris);
			
			if(!printVertical || imagemDeFundo != null){
				pdf = giraPaginaEIncluiImagemDeFundo(pdf, printVertical, imagemDeFundo); 
			}
			
			return pdf;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FalhaGeracaoDocumentoException(e.getMessage());
		}
	}

	private String substituirImagemBiometrica(String html) {
		if(!html.contains(CAMINHO_IMAGEM_BIOMETRICA_HTTP)) {
			return html;
		}
		
		String imagemBiometriaFuncionario = getImagemBiometriaFuncionario();
		
		if(Utils.isNullOrEmpty(imagemBiometriaFuncionario)) {
			return html;
		}

		String htmlPostRetorno = html;
		
		htmlPostRetorno = htmlPostRetorno.replace(CAMINHO_IMAGEM_BIOMETRICA_HTTP, imagemBiometriaFuncionario);
		
		if (Utils.toInt(parametrosVo.getCodigoClassificacao()) == ClassificacaoTipoProntuario.PEDIDO_EXAME.getIndex()) {
			htmlPostRetorno = htmlPostRetorno.replace("<title>", "<head><title>");
			htmlPostRetorno = htmlPostRetorno.replace("</title>", "</title></head>");
		}
		
		return htmlPostRetorno;
	}
	
	private String getImagemBiometriaFuncionario() {
		if (parametrosVo.isUtilizaSeloAssinaturaBiometrica() || Utils.isNullOrEmpty(parametrosVo.getCodigoVaultIdFuncionarioBiometria())) {
			return parametrosVo.getPathExternoBiometriaFuncionario();
		}

		return "data:image/bmp;base64," + Base64.encodeBase64String(VaultHelper.getInstance().getArquivo(parametrosVo.getCodigoVaultIdFuncionarioBiometria()));
	}

	private String substituirEnderecoDosLinksEImagensUpload(String html) {
		String htmlReplace = html;
		
		String endereco = soParamVo.getEndereco();
		
		if (endereco.contains(H_SOC)) {
		    htmlReplace = htmlReplace.replace("sistema.h-soc.com.br", System.getenv("IP_IMAGENS_HSOC"));
		    
		}  else if (endereco.contains(B_SOC)) {
			htmlReplace = htmlReplace.replace("www.b-soc.com.br", System.getenv("IP_IMAGENS_BSOC"));
		
		} else if (endereco.contains(LOCAL)) {
			htmlReplace = htmlReplace.replace("sistema.h-soc.com.br", "sistema.soc.com.br");
			htmlReplace = htmlReplace.replace("\\\\ages29\\age_AGES13\\SOC\\", "sistema.soc.com.br");
		}
		
		return htmlReplace;
	}

	private String substituirParametroQrCode(String html) {
		if (isPedidoProcessamentoPassivelVerificacaoQrCode(html) && isDocumentoConfiguradoParaVerificacaoPorLink()) {
			return substituirUrlQrCodePorLink(html);
		}
		return html;
	}
	
	private String substituirUrlQrCodePorLink(String html) {
		Matcher m = Pattern.compile(REGEX_IMG_QRCODE).matcher(html);

		if (m.find()) {
			return substituirUrlQrCodePorLink(html, m);
		}

		return html;
	}

	private String substituirUrlQrCodePorLink(String html, Matcher m) {
		String imgOriginal = getTagImgQrCode(html, m);
		String imgQrCode = imgOriginal.replace(getSrcImgQrCode(html, m), getSrcQrCodeLink());

		return html.replace(imgOriginal, imgQrCode).replace(CLASS_QRCODE_A_REMOVER, "");
	}

	private String getSrcQrCodeLink() {
		return String.format("src=\"%s?parametro=%s\"", getUrlGeradorQrCode(), getUrlLinkVerificacaoQrCode());
	}

	private String getTagImgQrCode(String html, Matcher m) {
		return html.substring(m.start(), m.end());
	}

	private String getSrcImgQrCode(String html, Matcher m) {
		int indiceInicialSrc = m.start(2);
		int indiceFinalSrc = m.end(2);
		if (indiceInicialSrc < 0) {
			indiceInicialSrc = m.start(3);
			indiceFinalSrc = m.end(3);
		}

		return html.substring(indiceInicialSrc, indiceFinalSrc);
	}

	private String getUrlLinkVerificacaoQrCode() {
		VerificacaoDocumentos verificacaoDocumentos = new VerificacaoDocumentos();
		parametrosVo.setTokenVerificacaoDocumento(verificacaoDocumentos.gerarTokenUnicoVerificacaoDocumentos(awsRequestId));
		return verificacaoDocumentos.gerarUrlVerificacaoDocumentos(parametrosVo.getTokenVerificacaoDocumento(), infraConfiguracaoBusiness);
	}

	private String getUrlGeradorQrCode() {
		return infraConfiguracaoBusiness
				.getValorRetornoInfraConfiguracaoPeloCodigo(InfraConfiguracaoKey.ENDERECO_SERVLET_QRCODE_INTERNO)
				.replace("https://", "http://");
	}
	
	private boolean isDocumentoConfiguradoParaVerificacaoPorLink() {
		return new QRCode(dao).isDocumentoConfiguradoParaVerificacaoPorLink(getQrCodeParaBuscaVo());
	}
	
	private QRCodeVo getQrCodeParaBuscaVo() {
		QRCodeVo qrCodeParaBuscaVo = new QRCodeVo();
		
		qrCodeParaBuscaVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresaPrincipal());
		qrCodeParaBuscaVo.setCodigoDocumento(parametrosVo.getCodigoClassificacao());
		
		return qrCodeParaBuscaVo;
	}
	
	private boolean isPedidoProcessamentoPassivelVerificacaoQrCode(String html) {
		return !Utils.isNullOrEmpty(html);
	}

	private String substituirPularLinhaPorBrETagAddTabPorEspaco(String html) {
		String htmlReplace = html;
		
		if (CERTIFICADO_EM_PDF.equals(parametrosVo.getTituloDocumento()) || GERAR_PDF_CERTIFICADO_INDIVIDUAL.equals(parametrosVo.getTituloDocumento())){
			htmlReplace = htmlReplace.replace("_pularLinha_", " <br> ").replace("_addTab_", " &nbsp;&nbsp;&nbsp;&nbsp;");
		}
		
		return htmlReplace;
	}

	public void inserirGedEArquivoGed(byte[] byteArray) throws BusinessException {
		UsuarioVo usuarioVo = dao.getUser(Utils.toInt(parametrosVo.getCodigoResponsavelAcao()));
		EmpVo empVo = dao.getEmp(Utils.toInt(parametrosVo.getCodigoEmpresa()));

		boolean criadoViaSocnet = !Utils.isValoresIguais(usuarioVo.getSubCod(), String.valueOf(empVo.getResp()));
		
		if(criadoViaSocnet){
			parametrosVo.setCriadoViaSocnet(true);
			parametrosVo.setCodigoEmpresaPrincipal(empVo.getCodigoEmpresaPrincipal());
		}
		
		if(Utils.isNullOrEmptyOrZero(parametrosVo.getCodigoSocGed())){
			inserirGed();
			parametrosVo.setCodigoSocGed(gedVo.getCodigo());
		}
		
		insertArquivoGed(byteArray);
	}
	
	private void insertArquivoGed(byte[] arquivo) throws BusinessException {
		List<String> listaCodigoGed = getListaCodigoGed();
		
		for(String codigoGed : listaCodigoGed){
			inserirArquivoGed(codigoGed, arquivo);
			enviaArquivoParaSFTP();
		}
	}
	
	private List<String> getListaCodigoGed() {
		List<String> listaCodigoGed  = new ArrayList<>();
		
		if(!Utils.isNullOrEmpty(parametrosVo.getCodigoSocGed())){
			listaCodigoGed = Arrays.asList(parametrosVo.getCodigoSocGed().split(","));
		}
		
		return listaCodigoGed;
	}
	
	private void enviaArquivoParaSFTP() throws BusinessException{
		new ArquivoGedHelper(dao).verificaExistenciaCredenciaParaGerarRegViaPedProc(arquivoGedPedProcVo.getCodigoEmpresaPrincipal(), 
																				 arquivoGedPedProcVo.getCodigoEmpresa(),
																				arquivoGedPedProcVo.getCodigoGed(),
																				arquivoGedPedProcVo.getNomeDoArquivo(),
																				arquivoGedPedProcVo.getCodigoArquivoGed(), 
																				arquivoGedPedProcVo.getCodigoTipoGed());
			
	}
	
	private void inserirArquivoGed(String codigoGed, byte[] arquivo) throws BusinessException {
		String nomeArquivo = getNomeArquivoGed(parametrosVo.getCodigoEmpresa(), codigoGed);
		
		ArquivoGedVo arquivoGedVo = new ArquivoGedVo();
		
		arquivoGedVo.setCodigoGed(codigoGed);
		arquivoGedVo.setCodigoEmpresaPrincipal(parametrosVo.getCodigoEmpresaPrincipal());
		arquivoGedVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
		arquivoGedVo.setNomeDoArquivo(getNomeComExtensao(nomeArquivo));
		arquivoGedVo.setCodigoResponsavelAcao(Utils.toInt(parametrosVo.getCodigoResponsavelAcao()));
		arquivoGedVo.setCodigoTipoDocumento(parametrosVo.getCodigoClassificacao());
		arquivoGedVo.setCodigoSequencialFicha(parametrosVo.getCodigoSequencialFicha());
		arquivoGedVo.setCodigoTipoGed(parametrosVo.getCodigoTipoGed());
		arquivoGedVo.setEnvioAutomatico(parametrosVo.isEnviarAssinaturaDigital());
		
		if(Utils.isNullOrEmpty(parametrosVo.getJsonAssinaturaDigital())){
			arquivoGedVo.setCodigoUsuarioAssinatura(parametrosVo.getCodigoResponsavelAcao());
		}
		
		arquivoGedVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
		arquivoGedVo.setArquivo(arquivo);
		
		arquivoGedVo = insertArquivoGed(arquivoGedVo);
		arquivoGedPedProcVo = arquivoGedVo;
		
		parametrosVo.setCodigoArquivoSocGed(arquivoGedVo.getCodigoArquivoGed());
		
		new VerificacaoDocumentos(dao).inserirVerificacaoDocumentos(parametrosVo);
	}
	
	private ArquivoGedVo insertArquivoGed(ArquivoGedVo arquivoGedVo) throws BusinessException {
		final VaultHelper vaultHelper = VaultHelper.getInstance();
	    String vaultId = null;
	   
	    try{
			if(arquivoGedVo.getArquivo() == null) {
				throw new BusinessException("Arquivo não informado");
			}
			
			String codArquivoGed = "";
			String comentarioLog = "";
			
			if(!Utils.isNullOrEmptyOrZero(arquivoGedVo.getCodigoArquivoGed())){
				codArquivoGed = arquivoGedVo.getCodigoArquivoGed();
				comentarioLog = comentarioLogSubstituicaoArquivo(arquivoGedVo);
			} else {
				codArquivoGed = verificaExistenciaArquivoGed(arquivoGedVo);
			}
			
			vaultId = vaultHelper.uploadArquivoGed(arquivoGedVo);
		
			if(codArquivoGed != null){
				arquivoGedVo.setCodigoArquivoGed(codArquivoGed);
				ArquivoGedVo arquivoGedVoPorCodigo = dao.getArquiVoGedPorCodigo(arquivoGedVo);
				
				if(VaultHelper.isPatternSerialIdValid(arquivoGedVoPorCodigo.getVaultId())) {
				    vaultHelper.deletarArquivo(arquivoGedVoPorCodigo.getVaultId());
				}
				
				arquivoGedVo.setVaultId(vaultId);
				arquivoGedVo.setCodigoArquivoGed(codArquivoGed);
				arquivoGedVo.setDataUpload(parametrosVo.getDataDoDia());
				arquivoGedVo.setHoraUpload(String.valueOf(parametrosVo.getHoraAtual()));
				dao.updateArquivoGed(arquivoGedVo);
				
			} else {
				arquivoGedVo.setVaultId(vaultId);
				arquivoGedVo.setCodigoArquivoGed(String.valueOf(dao.getSequenceArquivoGed()));
				long rowid = dao.insertArquivoGed(arquivoGedVo, parametrosVo);
				arquivoGedVo.setRowid(String.valueOf(rowid));
			}
			
			arquivoGedVo.setCodigoEmpresaNavegacao(arquivoGedVo.getCodigoEmpresa());
			
			GedVo gedAuxVo = new GedVo();
			gedAuxVo.setCodigoEmpresa(arquivoGedVo.getCodigoEmpresa());
			gedAuxVo.setCodigo(arquivoGedVo.getCodigoGed());
			gedAuxVo.setDataUpload(parametrosVo.getDataDoDia());
			gedAuxVo.setCodigoResponsavelAcao(String.valueOf(arquivoGedVo.getCodigoResponsavelAcao()));
			updateDataUploadGed(gedAuxVo);
			
			if(Utils.isNullOrEmpty(comentarioLog)){
				comentarioLog = "GED: " + arquivoGedVo.getCodigoGed()+", arquivo: "+arquivoGedVo.getNomeDoArquivo();
			}
			
			if(!Utils.isNullOrEmpty(arquivoGedVo.getComentarioAdicionalLog())){
				comentarioLog = comentarioLog.concat(", "+ arquivoGedVo.getComentarioAdicionalLog());
			}
			
			logaAcao(arquivoGedVo.getCodigoEmpresa(), "Inclusão", "Arquivo GED", CAD_177, null, null, comentarioLog, arquivoGedVo.getCodigoResponsavelAcao());
			
		} catch(BusinessException e){
		    if(VaultHelper.isPatternSerialIdValid(vaultId)) {
		        vaultHelper.deletarArquivo(vaultId);
		    }
		    
		    e.printStackTrace();
			throw new BusinessException("insertArquivoGed -> " + e);
		} 
		return arquivoGedVo;
	}
	
	private void updateDataUploadGed(GedVo gedVo) {
		dao.updateDataUploadGed(gedVo);
		logaAcao(gedVo.getCodigoEmpresa(), "Alteração", "Cadastro GED", CAD_177, null, null, "Atualização data de upload " + gedVo.getDataUpload()+ ", do Ged " + gedVo.getCodigo(), Utils.toInt(gedVo.getCodigoResponsavelAcao()));
	}

	public String verificaExistenciaArquivoGed(ArquivoGedVo arquivoGedVo) throws BusinessException {
		String codigo = null;
		
		try{
			String nomeArquivo = arquivoGedVo.getNomeDoArquivo();
			List<ArquivoGedVo> lista = dao.getArquivosDeUmGeddList(arquivoGedVo);
			
			for(ArquivoGedVo arquivoGedVo2 : lista){
				if(arquivoGedVo2.getNomeDoArquivo().equalsIgnoreCase(nomeArquivo)){
					codigo = arquivoGedVo2.getCodigoArquivoGed();
					break;
				}
			}
		} catch(Exception e){
			throw new BusinessException("verificaExistenciaArquivoGed -> " + e);
		}
		
		return codigo;
	}
	
	private String comentarioLogSubstituicaoArquivo(ArquivoGedVo arquivoGedVo) {
		ArquivoGedVo arquivoGedVoAnterior = dao.getArquiVoGedPorCodigo(arquivoGedVo);
		return "GED: " + arquivoGedVo.getCodigoGed()+", arquivo " + arquivoGedVoAnterior.getNomeDoArquivo() + " substituído por "+arquivoGedVo.getNomeDoArquivo();
	}

	private String getNomeComExtensao(String nomeArquivo) {
		return String.format("%s%s",nomeArquivo, ".pdf");
	}
	
	private String getNomeArquivoGed(String codigoEmpresa, String codigoGed) {
		parametrosVo.setNomeArquivo(formataNomeArquivoGed(parametrosVo.getNomeArquivo()));
		
		String nomeArquivo = parametrosVo.getNomeArquivo();
		
		String extensao = ".pdf";
		ArquivoGedVo arquivoGedPeloNomeVo = dao.getArquivoGedPeloNome(codigoEmpresa, codigoGed, nomeArquivo+extensao);
		
		int i = 1;
		
		while(arquivoGedPeloNomeVo != null){
			nomeArquivo = parametrosVo.getNomeArquivo()+"("+ i++ +")";
			arquivoGedPeloNomeVo = dao.getArquivoGedPeloNome(codigoEmpresa, codigoGed, nomeArquivo+extensao);
		}
		
		return nomeArquivo;
	}
	
	public String formataNomeArquivoGed(String nomeArquivoGed){
		return Utils.normalizarTexto(nomeArquivoGed, parametrosVo.getDataDoDia()).replace(" ","_");
	}
	
	private void inserirGed() {
		FuncionarioVo funcionarioVo = null;
		
		if(!Utils.isNullOrEmpty(parametrosVo.getCodigoFuncionario())){
			funcionarioVo = new FuncionarioVo();
			
			funcionarioVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
			funcionarioVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
			funcionarioVo = dao.getFuncionarioPeloCodigoSimples(funcionarioVo);
		}

		try {
			gedVo = new GedVo();
			
			if(funcionarioVo != null){
				gedVo.setNomeFuncionario(funcionarioVo.getNomeFuncionario());
				gedVo.setCodigoUnidade(funcionarioVo.getCodigoUnidade());
			}
			
			gedVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
			gedVo.setCodigoEmpresaPrincipal(parametrosVo.getCodigoEmpresaPrincipal());
			gedVo.setCodigoTipoGed(parametrosVo.getCodigoTipoGed());
			gedVo.setCodigoResponsavel(parametrosVo.getCodigoResponsavelAcao());
			gedVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
			gedVo.setCodigoSequencialFicha(parametrosVo.getCodigoSequencialFicha());
			gedVo.setNome(parametrosVo.getNomeGed());
			gedVo.setCodigoResponsavelAcao(parametrosVo.getCodigoResponsavelAcao());
			gedVo.setAcessoSocNet(parametrosVo.isCriadoViaSocnet());
			gedVo.setDataEmissao(Utils.getDataDoDia());
			gedVo.setDataUpload(Utils.getDataDoDia());
			gedVo.setNome(getNomeGedComControleSequencial(gedVo));
			gedVo.setCodigoTurma(parametrosVo.getCodigoTurma());
			
			if(parametrosVo.getNomeGed().contains(MANDATO_CIPA)){
				gedVo.setCodigoMandatoCipa(parametrosVo.getCodigoMandatoCipa());
			}
				
			insertGed(gedVo);
			
		} catch (GedMesmoNomeException e) {
			e.printStackTrace();
			inserirGed();
		}
	}
	
	private void insertGed(GedVo gedVo) throws GedMesmoNomeException {
		String codigoEmpresaPrincipal = gedVo.getCodigoEmpresaPrincipal();
		
		if(Utils.isNullOrEmptyOrZero(codigoEmpresaPrincipal)) {
			codigoEmpresaPrincipal = String.valueOf(dao.getCodigoEmpresaPrincipalFromEmpresaCliente(Utils.toInt(gedVo.getCodigoEmpresa())));
		}
		
		if(verificaGedPorNome(gedVo)){
			throw new GedMesmoNomeException("Já existe um Documento com esse nome");
		}									

		if (Utils.isNullOrEmptyOrZero(String.valueOf(gedVo.getCodigoResponsavel()))) {
			String codigoResponsavel = dao.getResponsavelPeloCodigoDaEmpresa(Utils.toInt(codigoEmpresaPrincipal));
			gedVo.setCodigoResponsavel(codigoResponsavel);
		}
		
		if(Utils.isNullOrEmptyOrZero(String.valueOf(gedVo.getCodigoResponsavelAcao()))){
			gedVo.setCodigoResponsavelAcao(gedVo.getCodigoResponsavel());
		}
		
		if (Utils.isNullOrEmptyOrZero(gedVo.getCodigo())) {
			gedVo.setCodigo(dao.getSequenceGed());
		}
		
		if(Utils.isNullOrEmpty(gedVo.getDataEmissao())){
			gedVo.setDataEmissao(parametrosVo.getDataDoDia());
		}
		
		dao.insertGed(gedVo);
		gedVo.setCodigoEmpresaPrincipal(codigoEmpresaPrincipal);
		
		String comentario = getComentarioLog(gedVo);
		
		if(!gedVo.isRealizarRegistroLogSesiGed()){
			logaAcao(gedVo.getCodigoEmpresa(), "Inclusão", "Cadastro GED", CAD_177, null, null, comentario, Utils.toInt(gedVo.getCodigoResponsavelAcao()));
		}
	}
	
	private void logaAcao(String emp, String acao, String nmprg, String prg, String rowIddoFuncionario, String codFuncionario, String comentario, int usu) throws IllegalArgumentException {
		StringBuilder coment = new StringBuilder();
		
		FuncionarioVo funcionarioVo = new FuncionarioVo();
		funcionarioVo.setRowid(rowIddoFuncionario);
		funcionarioVo.setCodigoEmpresa(emp);
		funcionarioVo.setCodigoFuncionario(codFuncionario);

		if (rowIddoFuncionario != null){
			funcionarioVo = dao.getFuncionarioPeloRowidSimples(funcionarioVo);
		
		} else if (codFuncionario != null){
			funcionarioVo = dao.getFuncionarioPeloCodigoSimples(funcionarioVo);
		}

		LogVo log = new LogVo();		   
		log.setCodigoEmpresa(Utils.toInt(emp));
		log.setAcao(acao);
		log.setNomePrograma(nmprg);

		if (funcionarioVo != null && funcionarioVo.getCodigoFuncionario() != null && funcionarioVo.getNomeFuncionario()!= null){
			coment.append(funcionarioVo.getCodigoFuncionario()+" - "+funcionarioVo.getNomeFuncionario()+" - ");	
		}
		
		coment.append(comentario);
		log.setObs(coment.toString());
		log.setPrograma(prg);
		log.setCodigoUsuario(usu);
		
		if (log.getCodigoEmpresa() != 0){
			log.setCodigoLog(String.valueOf(dao.getSequencialLog()));
			gravaLog(log);
		}
	}

	private void gravaLog(LogVo logVo) throws IllegalArgumentException {
		verificaTamanhoCampoObservacao(logVo);
		
		dao.insertLogAcao(logVo, parametrosVo);    		
	}

	private void verificaTamanhoCampoObservacao(LogVo logVo) throws IllegalArgumentException {
		if(logVo == null){
			return;
		}

		String obs = logVo.getObs();

		if(Utils.isNullOrEmpty(obs)){
			return;
		}
		
		int length = obs.length();

		if(length > TAMANHO_CAMPO_OBSERVACAO_LOGACAO){
			logVo.setObs(obs.substring(0, TAMANHO_CAMPO_OBSERVACAO_LOGACAO));
			
			String mensagem = MessageFormat.format(TEMPLATE_MENSAGEM_LOG_MUITO_GRANDE, logVo.getCodigoEmpresa(), logVo.getCodigoUsuario(), logVo.getPrograma(), logVo.getAcao(), logVo.getObs(), length);
			
			throw new IllegalArgumentException(mensagem);
		}
	}
	
	private String getComentarioLog(GedVo gedVo) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(getPropriedade(" SOCGED: ", gedVo.getNome()));
		builder.append(getPropriedade(" - Unidade: ", gedVo.getCodigoUnidade()));
		builder.append(getPropriedade(" - Tipo Ged: ", gedVo.getCodigoTipoGed()));
		builder.append(getPropriedade(" - Nome do Funcionário: ", gedVo.getNomeFuncionario()));
		builder.append(getPropriedade(" - Data da Emissão: ", gedVo.getDataEmissao()));
		builder.append(getPropriedade(" - Data da Validade: ", gedVo.getDataValidade()));
		builder.append(getPropriedade(" - Revisão: ", gedVo.getRevisao()));
		builder.append(getPropriedade(" - Comentário: ", gedVo.getComentarioAdicionalLog()));

		return builder.toString();
	}
	
	private String getPropriedade(String texto, String valor) {
		if (Utils.isNullOrEmpty(valor)) {
			return "";
		}
		return texto.concat(valor);
	}

	private boolean verificaGedPorNome(GedVo gedVo) {
		GedVo gedTempVo = new GedVo();
		gedTempVo.setCodigoEmpresa(gedVo.getCodigoEmpresa());
		gedTempVo.setNome(Utils.replaceAllAccent(gedVo.getNome()));
		return dao.getGedPorNome(gedTempVo) != null;
	}

	private String getNomeGedComControleSequencial(GedVo gedVo) {
		String nomeGedOriginal = gedVo.getNome();
		String nomeGed = gedVo.getNome();
		
		int indiceVersao = 1;
		
		GedVo gedPeloNome = this.getGedPeloNome(gedVo);
		
		while (gedPeloNome != null) {
			nomeGed = String.format(TEMPLATE_NOME_ARQUIVO, nomeGedOriginal, indiceVersao++);
			gedVo.setNome(nomeGed);
			
			gedPeloNome = this.getGedPeloNome(gedVo);
		}

		return nomeGed;
	}

	private GedVo getGedPeloNome(GedVo gedVo) {
		return dao.getGedPorNome(gedVo);
	}

	public void buscarCodigoGedDocumentoProntuarioMedico() {
		GedVo gedAuxVo = new GedVo();
		
		gedAuxVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
		gedAuxVo.setCodigoEmpresaPrincipal(parametrosVo.getCodigoEmpresaPrincipal());
		gedAuxVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
		gedAuxVo.setCodigoSequencialFicha(parametrosVo.getCodigoSequencialFicha());
		gedAuxVo.setCodigoTipoGed(parametrosVo.getCodigoTipoGed());
		gedAuxVo = dao.getGedDeUmProntuarioPeloCodigoSequencialComTipoGed(gedAuxVo);
		
		if (gedAuxVo != null && !Utils.isNullOrEmpty(gedAuxVo.getCodigo())) {
			parametrosVo.setCodigoSocGed(gedAuxVo.getCodigo());
		}
	}
	
	public void buscarCodigoGedDocumentoCipa() {
		GedVo gedAuxVo = new GedVo();

		gedAuxVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
		gedAuxVo.setCodigoEmpresaPrincipal(parametrosVo.getCodigoEmpresaPrincipal());
		gedAuxVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
		gedAuxVo.setCodigoTipoGed(parametrosVo.getCodigoTipoGed());
		
		if (Utils.isNullOrEmptyOrZero(parametrosVo.getCodigoMandatoCipa())) {
			gedAuxVo.setCodigoMandatoCipa(parametrosVo.getCodigoMandato());
			gedAuxVo = dao.getGedDeUmProntuarioPeloCodigoMandatoCipaComTipoGed(gedAuxVo);
		} else {
			gedAuxVo.setCodigoMandatoCipa(parametrosVo.getCodigoMandatoCipa());
			gedAuxVo = dao.getGedComMandato(gedAuxVo);
		}
		
		if (gedAuxVo != null && !Utils.isNullOrEmpty(gedAuxVo.getCodigo())) {
			parametrosVo.setCodigoSocGed(gedAuxVo.getCodigo());
		}
	}
	
	private Image definirImagemDeFundoCertificado(boolean printVertical, boolean ehModeloPersonalizado) {
		if(!CERTIFICADO_EM_PDF.equals(parametrosVo.getTituloDocumento())
			&& !GERAR_PDF_CERTIFICADO_INDIVIDUAL.equals(parametrosVo.getTituloDocumento())
			&& !RELATORIO_COLETIVO_PDF.equals(parametrosVo.getTituloDocumento())
			&& !GERAR_PDF_CERTIFICADO_COLETIVO.equals(parametrosVo.getTituloDocumento())){
			return null;
		}
		
		if(Utils.isNullOrEmpty(parametrosVo.getNomeArquivoPapelDeCarta())){
			return null;
		}
		
		InfraConfiguracaoVo infraVo = new InfraConfiguracaoVo();
		infraVo.setCodigo(ehModeloPersonalizado ? InfraConfiguracaoKey.DIRETORIO_UPLOAD_IMAGEM_PAPEL_DE_CARTA_MODELO_PERSONALIZADO : InfraConfiguracaoKey.DIRETORIO_UPLOAD_IMAGEM_PAPEL_DE_CARTA);
		try {
			infraVo = infraConfiguracaoBusiness.getInfraConfiguracaoPeloCodigo(infraVo.getCodigo());
			
			String diretorio = infraVo.getValorRetorno().replace(COD_REPL_EMPPRI, parametrosVo.getCodigoEmpresaPrincipal())
														.replace(COD_REPL_EMP,  parametrosVo.getCodigoEmpresa());
			
			Image image = Image.getInstance(diretorio.concat(parametrosVo.getNomeArquivoPapelDeCarta()));
			
			if(printVertical){
				image.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			} else {
				image.scaleAbsolute(PageSize.A4.getHeight(), PageSize.A4.getWidth());
			}
			
			image.setAbsolutePosition(0, 0);
			
			return image;
			
		} catch (Exception e) {
			return null;
		}
	}
	
	private byte[] giraPaginaEIncluiImagemDeFundo(byte[] pdfByteArray, boolean printVertical, Image imagemDeFundo) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(5000);
		
		try{
			MakeDocumento makeDocumento = new MakeDocumento(byteArrayOutputStream);
			makeDocumento.setMargins(28.3f, 28.3f, 28.3f, 28.3f);

			if(printVertical){
				makeDocumento.setPage(PageSize.A4);
			} else {
				makeDocumento.setPage(PageSize.A4.rotate());
			}
			
			makeDocumento.open();
		
			PdfReader pdfReader = new PdfReader(pdfByteArray);
			
			for (int pageCounter = 1; pageCounter < pdfReader.getNumberOfPages() + 1; pageCounter++){
				PdfImportedPage page = makeDocumento.getPdfWrite().getImportedPage(pdfReader, pageCounter); 
				Image image = Image.getInstance(page);
				
				if(printVertical){
					image.scaleAbsolute(PageSize.A4.getWidth(), PageSize.A4.getHeight());
				} else {
					image.scaleAbsolute(PageSize.A4.getHeight(), PageSize.A4.getWidth());
				}
				
				image.setAbsolutePosition(0, 0);
				
				makeDocumento.add(image);
				
				if(imagemDeFundo != null){
					makeDocumento.setBackgroundImg(imagemDeFundo);
				}
				
				makeDocumento.newPage();
			}
			
			makeDocumento.close();
			
			return byteArrayOutputStream.toByteArray();
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("erro giraPaginaEIncluiImagemDeFundo");
		}
	}
	
	private boolean isLayoutImpressaoRetrato(String html, boolean ehModeloPersonalizado) {
		if (ehModeloPersonalizado) {
			return !html.contains("@page{size: landscape;}");
		}
		
		if (html.contains("landscape")){
			return false;
		}
		
		return true;
	}
	
	private static String getCssOdontograma() {
		return " .titulo-odontograma {font-weight:bold;font-family: Tahoma,Verdana,Arial;font-size: 9px;margin-left:5px}"
									 + ".div-codigo-dente{font-size:9pt;text-align:center}.div-imagem{width:30px;margin:auto}.img-dente{height:40px}.div-dente-img .div-imagem-cores{width:15px;height:15px;border:9px solid}"
									 + ".div-dente-img .div-imagem-template{background:url(/WebSoc/imagens/dentes/dente-template.png) no-repeat;background-size:cover;width:33px;height:33px;margin-top:-33px}"
									 + ".table-odontograma td{padding:5px}.table-odontograma{margin-left:auto;margin-right:auto;text-align:center}#div-odontograma{padding-bottom:15px}"
									 + ".table-detalhes-procedimentos{font-size:8pt;width:100%;border-collapse:collapse;}.table-detalhes-procedimentos *{border:solid 1px #000}"
									 + ".table-detalhes-procedimentos .table-header{font-weight:700;text-align:center}.table-detalhes-procedimentos td{padding:5px}.cor-legenda{height:.5em;width:.5em;border:1px solid #ACACAC;padding:5px} "
									 + " table.tabela-odontograma-procedimentos {table-layout: fixed}"
									 + " @media print {.table-odontograma td {padding: 1px !important;}}"
									 + " .tr-deciduo div.div-imagem img {height: 30px;}"
									 + "</style>";
	}
	
	private byte[] criarArquivoPdf(String html, boolean ehAFichaClinicaDaMantris) throws Exception {
		String htmlReplace = html;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(5000);				
		
		try {
			htmlReplace = Utils.verificaUrlImagem(htmlReplace, soParamVo);

			html2PdfComInputStream(htmlReplace, ehAFichaClinicaDaMantris, baos);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new FalhaGeracaoDocumentoException(e.getMessage());
		}
		
		return baos.toByteArray();
	}
	
	private void html2PdfComInputStream(String html, boolean ehAFichaClinicaDaMantris, ByteArrayOutputStream baos) throws FalhaConversaoPdfException {
		String htmlReplace = html;
		htmlReplace = aplicaLinkCssLayout(htmlReplace);
		
		InputStream fis = new ByteArrayInputStream(htmlReplace.getBytes());
		
		logTemporario(true, "Inicio conversao");
		
		if (ehAFichaClinicaDaMantris) {
			new MakeDocumento().html2PDF(fis, baos, 1.1111f, 1, logger, awsRequestId);
		} else {
			new MakeDocumento().html2PDF(fis, baos, logger, awsRequestId);
		}
		
		logTemporario(false, "Fim conversao");
	}

	private void logTemporario(boolean logInicial, String msgComplemento) {
		if(parametrosVo != null && logInicial) {
			StringBuilder log = new StringBuilder();
			log.append(msgComplemento).append(" - ");
			log.append("Empresa: " + parametrosVo.getCodigoEmpresa()).append(" - ");
			log.append("Ficha: " + parametrosVo.getCodigoSequencialFicha()).append(" - ");
			log.append("Tipo: " + parametrosVo.getCodigoClassificacao()).append(" - ");
			log.append("Biometria: " + isExisteBiometriaDocumento()).append(" - ");
			log.append("AwsReqId: " + awsRequestId);
			
			logger.log(log.toString(), LogLevel.INFO);
		}
		
		if(!logInicial) {
			StringBuilder log = new StringBuilder();
			log.append(msgComplemento).append(" - ");
			log.append("AwsReqId: " + awsRequestId);
			
			logger.log(log.toString(), LogLevel.INFO);
		}
	}
	
	private String aplicaLinkCssLayout(String html) {
		String htmlReplace = html;
		
		if (!htmlReplace.contains("padrao/css/layout")) {
			htmlReplace = htmlReplace.replace("<head>", "<head><link rel=\"stylesheet\" href=\"https://sistema.soc.com.br/estatico/webcontext/padrao/css/layout/0/0.css\" type=\"text/css\"> ");
		}
		
		return htmlReplace;
	}
	
	public String adicionaCssCustomizado(String html, String css) {
		String htmlReplace = html;
		String strRepleced = null;
		StringBuilder bufferCss = new StringBuilder(css);
		
		if (Utils.find(htmlReplace, TAG_STYLE, false)) {
			strRepleced = "</style>";
		} else {
			strRepleced = "</head>";
			bufferCss.insert(0, "</head><style type=\"text/css\">");
		}
		
		if(!Utils.find(htmlReplace, TAG_STYLE, false) && !Utils.find(htmlReplace, "<(head)", false)){
			bufferCss.insert(0, "<style type=\"text/css\">");
			htmlReplace = bufferCss.toString().concat(htmlReplace);
		}
		
		return htmlReplace.replace(strRepleced, bufferCss);
	}
	
	private String substituiEnderecoDasImagensEstaticas(String html) {
		String htmlReplace = html;
		String regex = "<img (.+)?src=";
		Pattern patter = Pattern.compile(regex);
		Matcher matcher = patter.matcher(html);
		
		if (matcher.find()) {
			String caminhoImagem = WEBSOC_PADRAO.concat("/imagens/");
			htmlReplace = htmlReplace.replaceAll(caminhoImagem, getUrl());
			htmlReplace = htmlReplace.replace("https", "http");
		}
		
		return htmlReplace;
	}
	
	private String getUrl() {
		String endereco = soParamVo.getEndereco();
		      
		if (endereco.contains(LOCAL)) {
			return "file:../AgeSoc/deploy/Soc.ear/WebSoc.war/imagens/";
		}
		
		if (endereco.contains(H_SOC)) {
		    return "http://172.29.5.60/estatico/pdf/";
		}
		
		if (endereco.contains(P_SOC)) {
			return "http://www.p-soc.com.br/estatico/pdf/";
		}
		
		return "https://sistema.soc.com.br/estatico/pdf/";
	}
	
	private String substituirEnderecoExternoServletMService(String html) {
		if(Utils.isNullOrEmpty(html)){
			return html;
		}
		
		String htmlReplace = html;
		
		String urlServletGrafico = infraConfiguracaoBusiness.getInfraConfiguracaoPeloCodigo(InfraConfiguracaoKey.URL_SERVLET_GRAFICO_AUDIOMETRIA).getValorRetorno();
		String urlServletGraficoInterno = infraConfiguracaoBusiness.getInfraConfiguracaoPeloCodigo(InfraConfiguracaoKey.URL_SERVLET_GRAFICO_AUDIOMETRIA_INTERNO).getValorRetorno();

		htmlReplace =  htmlReplace.replace(urlServletGrafico, urlServletGraficoInterno);
		
		String urlCodigoBarra = soParamVo.getUrlCodigoBarra();
		String urlCodigoBarraExterno = soParamVo.getUrlCodigoBarraInterno();

		return htmlReplace.replace(urlCodigoBarra, urlCodigoBarraExterno);
	}
	
	public String adicionaCSSDefault(String html, boolean printVertical, boolean imprimePaginaTamanhoLegal) {
		String htmlReplace = html;
		String strRepleced = null;
		StringBuilder bufferCss = getCSSPadrao(printVertical, imprimePaginaTamanhoLegal);

		if (Utils.find(htmlReplace, TAG_STYLE, false)) {
			strRepleced = "</style>";
		} else {
			strRepleced = "</head>";
			bufferCss.insert(0, "</head><style type=\"text/css\">");
		}
		
		if(!Utils.find(htmlReplace, TAG_STYLE, false) && !Utils.find(htmlReplace, "<(head)", false)){
			bufferCss.insert(0, "<style type=\"text/css\">");
			htmlReplace = bufferCss.toString().concat(htmlReplace);
		}
		
		return htmlReplace.replace(strRepleced, bufferCss);
	}
	
	private StringBuilder getCSSPadrao(boolean printVertical, boolean imprimePaginaTamanhoLegal) {
		StringBuilder strCSS = new StringBuilder();

		strCSS.append(".manualTitulo {margin-left: 30px;*margin-left: 30px;margin-right: 30px;width: 90%;*width: 100%;position: relative;padding: 10px;border-bottom: 1px solid #67A5AE;border-top: 1px solid #67A5AE;text-align: center;font-weight: bold;font-size: 18px;color: #000000;}");
		strCSS.append(".textoManual {font-family:Arial;font-size:x-small;color:#000000}");
		strCSS.append(".campo{font-family:sans-serif,Verdana,arial;font-size:x-small;color:#000000;padding: 5px;}");
		strCSS.append(".campo2{font-family:sans-serif,Verdana,arial;font-size: 0.7em;color:#000000;}");
		strCSS.append(".camrel{font-family:sans-serif,Verdana,arial;font-size:13px;color:#000}");
		strCSS.append(".s0000{border-top:none;border-right:none;border-bottom:none;border-left:none;font-family:sans-serif,Verdana,arial; font-size:10px}");
		strCSS.append(".s0010{border-top:none;border-right:none;border-bottom:1px solid black;border-left:none;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s0001{border-top:none;border-right:none;border-bottom:none;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s0011{border-color:black;border-top:none;border-right:none;border-bottom:1px solid;border-left:1px solid;font-family:sans-serif;font-size:10px}");
		strCSS.append(".s0110{border-top:none;border-right:1px solid black;border-bottom:1px solid black;border-left:none;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s1110{border-top:1px solid black;border-right:1px solid black;border-bottom:1px solid black;border-left:none;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s0111{border-top:none;border-right:1px solid black;border-bottom:1px solid black;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s1111{border-top:1px solid black;border-right:1px solid black;border-bottom:1px solid black;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:10px}");
		strCSS.append(".s0100{border-top:none;border-right:1px solid black;border-bottom:none;border-left:none;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s0101{border-top:none;border-right:1px solid black;border-bottom:none;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1000{border-top:1px solid black;border-right:none;border-bottom:none;border-left:none;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1001{border-top:1px solid black;border-right:none;border-bottom:none;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1010{border-top:1px solid black;border-right:none;border-bottom:1px solid black;border-left:none;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1011{border-top:1px solid black;border-right:none;border-bottom:1px solid black;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1100{border-top:1px solid black;border-right:1px solid black;border-bottom:none;border-left:none;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".s1101{border-top:1px solid black;border-right:1px solid black;border-bottom:none;border-left:1px solid black;font-family:sans-serif,Verdana,arial;font-size:8pt}");
		strCSS.append(".m0000{border-top:none;border-right:none;border-bottom:none;border-left:none;font-family:Verdana,arial; font-size:8pt}");
		strCSS.append(".m0001{border-top:none;border-right:none;border-bottom:none;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0010{border-top:none;border-right:none;border-bottom:1px solid black;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0011{border-color:black;border-top:none;border-right:none;border-bottom:1px solid;border-left:1px solid;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0100{border-top:none;border-right:1px solid black;border-bottom:none;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0101{border-top:none;border-right:1px solid black;border-bottom:none;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0110{border-top:none;border-right:1px solid black;border-bottom:1px solid black;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m0111{border-top:none;border-right:1px solid black;border-bottom:1px solid black;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1000{border-top:1px solid black;border-right:none;border-bottom:none;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1001{border-top:1px solid black;border-right:none;border-bottom:none;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1010{border-top:1px solid black;border-right:none;border-bottom:1px solid black;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1011{ border-top:1px solid black;border-right:none;border-bottom:1px solid black;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1100{ border-top:1px solid black;border-right:1px solid black;border-bottom:none;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1101{ border-top:1px solid black;border-right:1px solid black;border-bottom:none;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1110{border-top:1px solid black;border-right:1px solid black;border-bottom:1px solid black;border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".m1111{ border-top:1px solid black;border-right:1px solid black;border-bottom:1px solid black;border-left:1px solid black;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".corpo{ background:#FFFFFF ;font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:15; min-height:150px;}");
		strCSS.append(".pagina{page-break-after: always;}");
		strCSS.append(".cb_e1 { font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:12px;color:#000;font-weight:700;font-variant:small-caps;letter-spacing:1px;}");
		strCSS.append(".cb_e1P{ font-family: arial, helvetica, Verdana; font-size: 12px; color: #000000; font-weight: bold;}");
		strCSS.append(".cb_e2 { font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:24px;color:#2D646C;font-weight:700;font-variant:small-caps;letter-spacing:4px;text-decoration:underline;}");
		strCSS.append(".cb_e3{ font-family: arial, helvetica, Verdana; font-size: 12px; color: #000000; font-weight: bold;}");
		strCSS.append(".cb_e4{ font-family: arial, helvetica, Verdana; font-size: 18px; color: #006666;}");
		strCSS.append(".relreviver  { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:x-small;color:#000000;font-weight: bold;}");
		strCSS.append(".relreviver1 { background:#FFFFFF;font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:15;}");
		strCSS.append(".relreviver2 { background:#FFFFFF;font-family:arial, helvetica;font-size:x-small;color:#000000;}");
		strCSS.append(".relreviver3 { background:#FFFFFF;font-family:arial, helvetica;font-size:xx-small;color:#000000;}");
		strCSS.append(".relsollo  { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:xx-small;color:#65B634;font-weight: bold;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".relsollo1 { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:x-small;color:#000000;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".relsollo2 { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:xx-small;color:#000000;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".relsollo3 { background:#FFFFFF;font-family:arial, helvetica;font-size:xx-small;color:#000000;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".relsollo4 { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:xx-small;color:#000000; border-top:none; border-right:1px solid black; border-bottom:none; border-left:none;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".relsafety { background:#FFFFFF;font-family:Verdana, arial, helvetica;font-size:xx-small;color:#333399;font-weight: bold;font-family:Verdana,arial;font-size:8pt}");
		strCSS.append(".personalsub   { background:#ffffff; font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:xx-small;color:#000099;font-weight:bold}");
		strCSS.append(".personalitem  { background:#ffffff; font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:xx-small;color:#3333FF}");
		
		strCSS.append(".label{position: relative;display: block;	min-width: 150px;*width: 150px; min-height: 20px; float: left; color: #999; font-family: 'MS Reference Sans Serif','Verdana','Arial'; font-size: 10pt;  padding: 3px; white-space: nowrap;  margin-right: 5px;  height: 100%}");
		strCSS.append(".campo{font-family:'MS Reference Sans Serif','Verdana','Arial';	font-size:9pt; color:#000;padding:5px}");
		strCSS.append(".titclaro {border:1px solid silver;	background:#EAEAEA;	font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:0.7em;color:#000}");
		strCSS.append(".corpo{ background:#FFF;font-family:'MS Reference Sans Serif','Verdana','Arial';font-size:15px;	min-height:140px}");
		strCSS.append(".borda_inset {font-family:'MS Reference Sans Serif','Verdana','Arial';-size:x-small;color:#000}");
		strCSS.append(".s1111 { font-family:Verdana, arial;font-size:9px;border-color:#000!important}");
		strCSS.append(".p0000, .s0000, .g0000, .m0000 { border-top:none; border-right:none; border-bottom:none; border-left:none;}");
		strCSS.append(".p1000, .s1000, .g1000, .m1000 { border-color:#000; border-top:1px solid; border-right:none; border-bottom:none; border-left:none; }");
		strCSS.append(".p0100, .s0100, .g0100, .m0100 { border-color:#000; border-top:none; border-right:1px solid; border-bottom:none; border-left:none;}");
		strCSS.append(".p0010, .s0010, .g0010, .m0010 { border-color:#000; border-top:none; border-right:none; border-bottom:1px solid; border-left:none;}");
		strCSS.append(".p0001, .s0001, .g0001, .m0001 { border-color:#000; border-top:none; border-right:none; border-bottom:none; border-left:1px solid;}");
		strCSS.append(".p1100, .s1100, .g1100, .m1100 { border-color:#000; border-top:1px solid; border-right:1px solid; border-bottom:none; border-left:none;}");
		strCSS.append(".p1010, .s1010, .g1010, .m1010 { border-color:#000; border-top:1px solid; border-right:none; border-bottom:1px solid; border-left:none;}");
		strCSS.append(".p1001, .s1001, .g1001, .m1001 { border-color:#000; border-top:1px solid; border-right:none; border-bottom:none; border-left:1px solid;}");
		strCSS.append(".p0110, .s0110, .g0110, .m0110 { border-color:#000; border-top:none; border-right:1px solid; border-bottom:1px solid; border-left:none;}");
		strCSS.append(".p0101, .s0101, .g0101, .m0101 { border-color:#000; border-top:none; border-right:1px solid; border-bottom:none; border-left:1px solid;}");
		strCSS.append(".p0011, .s0011, .g0011, .m0011 { border-color:#000; border-top:none; border-right:none; border-bottom:1px solid; border-left:1px solid;}");
		strCSS.append(".p1110, .s1110, .g1110, .m1110 { border-color:#000; border-top:1px solid; border-right:1px solid; border-bottom:1px solid; border-left:none;}");
		strCSS.append(".p1101, .s1101, .g1101, .m1101 { border-color:#000; border-top:1px solid; border-right:1px solid; border-bottom:none; border-left:1px solid;}");
		strCSS.append(".p1011, .s1011, .g1011, .m1011 { border-color:#000; border-top:1px solid; border-right:none; border-bottom:1px solid; border-left:1px solid;}");
		strCSS.append(".p0111, .s0111, .g0111, .m0111 { border-color:#000; border-top:none; border-right:1px solid; border-bottom:1px solid; border-left:1px solid;}");
		strCSS.append(".p1111, .s1111, .g1111, .m1111 { border-color:#000; border-top:1px solid; border-right:1px solid; border-bottom:1px solid; border-left:1px solid;}");

		if (printVertical && imprimePaginaTamanhoLegal) {
			strCSS.append("@page{size: legal;");
		} else if (printVertical){
			strCSS.append("@page{size: letter;");
		}else{
			strCSS.append("@page{size: landscape;");
		}

		strCSS.append("margin: 0.25in;");
		strCSS.append("-fs-flow-top: \"header\";-fs-flow-bottom: \"footer\";-fs-flow-left: \"left\";-fs-flow-right:\"right\";}");
		strCSS.append("#header{font-size: 60%; font-style: italic; position: absolute; top: 0; left: 0;-fs-move-to-flow: \"header\";}");
		strCSS.append("#footer{font-size: 60%; font-style: italic; text-align:center; position: absolute; top: 0; right:0 ;-fs-move-to-flow: \"footer\";}");
		strCSS.append("#footerCenterComBorda{font-size: 12pt; border:solid thin black; font-weight: bold; text-align:center; position: absolute; top: 0; left:0 ; height:400; width:100%; -fs-move-to-flow: \"footer\";}");
		strCSS.append("#pagenumber:before {content: counter(page);}");
		strCSS.append("#pagecount:before {content: counter(pages);}");
		
		strCSS.append(".relCabTable { border-bottom:1px solid gray; }");
		strCSS.append(".relCabTable tr td { padding: 0px; margin: 0px; }");
		strCSS.append(".relCabTable tr td p { position: relative; display: block; font-family:Verdana, helvetica, sans-serif; }");
		strCSS.append(".cabecalhoAudiometria { margin-top: 5px !important; }");
		strCSS.append(".relCabTit { font-size:18px; color:gray; font-weight:700; font-variant: small-caps; letter-spacing:1px; margin: 0 0 10px 10px; }");
		strCSS.append(".relCabEmpresa { font-size:14px; color:#000; font-weight:700; font-variant:small-caps; letter-spacing:1px;	margin: 0 0 0 10px; }");
		strCSS.append(".relCabData	{ font-size:10px; color:#000; font-weight:700; font-variant:small-caps; letter-spacing:1px; margin: 5px 0 10px 0; }");
		strCSS.append(".relDestaque, .relCampos, .relCampos p, .relCamposP, .relCamposP p, .relCamposTable tr td, .relCamposTable tr td p, .relCamposTableItem, .relCamposTableSub, .relCamposTableL, .relCamposTableL tr td, .relCamposTableL tr th, .relCamposAss, .relCamposAss tr td { font-family:Verdana, helvetica, sans-serif; font-size:8pt;}");
		strCSS.append(".relDestaque	{ border-top:1px solid gray; padding-top:5px; font-weight:700; font-size:9pt; }");
		strCSS.append(".relTitulo, .relTituloS{padding:3px;font-size:14pt;color:#808080;font-variant:small-caps;text-align:center;}");
		strCSS.append(".relTituloS{border-top: 2px solid #808080;}");
		strCSS.append(".relCampos{padding:5px;padding-left:30px;text-align:justify;}");
		strCSS.append(".relCamposCenter{text-align: center;}");
		strCSS.append(".relCamposAss{width:100%;border:0;margin-top: 35px;}");
		strCSS.append(".relCamposAss tr td{width:33%;text-align: center;padding-right: 10px;}");
		strCSS.append(".relCamposTable{width:100%;border:0;}");
		strCSS.append(".relCamposTableL{width:100%;border:0;border-collapse:collapse;}");
		strCSS.append(".relCamposTableL tr td.vermelho{color: #F00;}");
		strCSS.append(".relCamposTableL tr td.azul{color: #00F;}");
		strCSS.append(".relCamposTableL, .relCamposTableL tr td, .relCamposTableL tr th{border: 1px solid #000;}");
		strCSS.append(".relCamposTableL tr th{background-color: #EAEAEA;text-align: center;font-weight: bold;}");
		strCSS.append(".relCamposTableL tr th.Destaque{background-color: #C0C0C0;text-align: center;font-weight: bold;}");
		strCSS.append(".relCamposTableSemR{font-weight: bold;padding: 10px;}");
		strCSS.append(".relCamposTable tr td:not(.conteudo-editor-texto *){width:50%}.relCamposTable tr td{padding-right:10px}");
		strCSS.append(".relCamposTable tr td p, .relCamposTableL tr td p{margin:0;padding:0;}");
		strCSS.append(".relCamposP p{margin:0;line-height: 150%;padding:5px;padding-left:30px;text-align:justify;}");
		strCSS.append(".relCamposTable span, .relCamposTableItem, .relCampos span, .relCamposP span{font-weight:700;}");
		strCSS.append(".relCamposTable .relCamposTableDes{text-decoration:underline;}");
		strCSS.append(".deciduoOn{width:700px;height:616px;}");
		strCSS.append(".deciduoOff{width:550px;height:266px;}");
		strCSS.append(".titescuro {border: 1px solid #297b83;background-color: #297b83;font-family: Tahoma,Verdana,Arial;font-size: 8pt;color: #fff;}");
		strCSS.append(".grupo2 {border-collapse: collapse;}");
		
		strCSS.append(".examesQuebraPagina {width: 97.6%; margin-top: 5px;}");
		strCSS.append("#tableCabecalho {margin-top: 0px !important;}");
		
		strCSS.append(".p0000, .p1000, .p0100, .p0010, .p0001, .p1100, .p1010, .p1001 , .p0110, .p0101, .p0011, .p1110, .p1101, .p1011, .p0111, .p1111 { font-family:Tahoma,Verdana,Arial;font-size:8px;}");
		strCSS.append(".cb_e5{ font-family:Tahoma,Verdana,Arial;font-size:12px;color:#297b83;font-weight:700;font-variant:small-caps;}");
		strCSS.append(".certiAssinatura{width: 350px;float: right;}");
		strCSS.append(".tabela{width: 100%;border: 0;border-spacing: 0;}");
		strCSS.append(".tb-padrao, .tamanho11{font-size:11px;font-family: Tahoma,Verdana,Arial;}");
		strCSS.append(".tb-padrao { border-spacing: 0px; width:100%; border: 1px solid;}");
		strCSS.append(".nome-tabela { background-color: #eeeeee; border-bottom: 1px solid; width: 100%;}");
		strCSS.append(".width-20 { width: 20%;}");
		strCSS.append("</style>");

		return strCSS;
	}
	
	private String corrigeNomeFonteVerdana(String html) {
		return html.replace("'verdana'", "'Verdana'");
	}
	
	private String ajustaProporcaoCamposTabelaExameDataEmBranco( String html ) {
		Document document = Jsoup.parse(html);
		List<Element> tableExames = document.select("table[id=T25]");

		if(tableExames == null) {
			return document.html();
		}
		
		Document documentExames = Jsoup.parse(tableExames.toString());
		List<Element> elements = documentExames.select("span:containsOwn(__/__/____)");

		String stringStyle = "style";
		String stringWidth = "width";

		for (Element element : elements) {
			List<Attribute> tdParentAttributes = element.parent().parent().parent().parent().parent().attributes().asList();

			for (Attribute attributeParent : tdParentAttributes) {
				if (attributeParent.getKey().equalsIgnoreCase(stringStyle) && attributeParent.getValue().contains(stringWidth) && !isWidthLessOrEqualThanXPercent( attributeParent.getValue(), 50) ) {
					return document.html();
				}
			}

			Element tdDate = element.parent();
			List<Attribute> tdDateAttributes = tdDate.attributes().asList();

			Element tdExame = tdDate.nextElementSibling();
			List<Attribute> tdExameAttributes = tdExame.attributes().asList();

			formatarTdDateAttributes(stringStyle, stringWidth, tdDateAttributes, tdExameAttributes);
		}

		return document.html();
	}

	private void formatarTdDateAttributes(String stringStyle, String stringWidth, List<Attribute> tdDateAttributes, List<Attribute> tdExameAttributes) {
		for (Attribute attribute : tdDateAttributes) {
			if (attribute.getKey().equalsIgnoreCase(stringStyle) && attribute.getValue().contains(stringWidth) && isWidthLessOrEqualThanXPercent(attribute.getValue(), 20)) {
				attribute.setValue("width:20%");

				for (Attribute attributeExame : tdExameAttributes) {
					if (attributeExame.getKey().equalsIgnoreCase(stringStyle) && attributeExame.getValue().contains(stringWidth)) {
						attributeExame.setValue("width:80%" );
					}
				}
			}
		}
	}

	private static boolean isWidthLessOrEqualThanXPercent( String width, int percent ) {
		int percentWidth = Integer.parseInt(width.substring(width.indexOf( ":" ) + 1, width.indexOf( "%" )));
		return percentWidth <= percent;
	}
	
	private String ativaCssExclusivoParaAsConversoesEmPDF(String html) {
		return html.replace("@media conversaopdf", "@media print");
	}
	
	private String substituiSourceSocPorSrc(String html) {
		return html.replace(SOURCE_SOC, "src");
	}
	
	private String removeConteudoEntreBiometriaNaoGravar(String html){
		String htmlReplace = html;
		
		if(htmlReplace.contains("<BIOMETRIA_NAO_GRAVAR>")){
			htmlReplace = Utils.retiraConteudoStringEntreDuasPalavras(htmlReplace, "<BIOMETRIA_NAO_GRAVAR>", "</BIOMETRIA_NAO_GRAVAR>");
			
		} else if(htmlReplace.contains("<biometria_nao_gravar>")){
			htmlReplace = Utils.retiraConteudoStringEntreDuasPalavras(htmlReplace, "<biometria_nao_gravar>", "</biometria_nao_gravar>");
		}
			
		return htmlReplace;
	}
	
	private String removeConteudoEntreAgeNaoGravar(String html){
		String htmlReplace = html;
		
		if(htmlReplace.indexOf("<html".toLowerCase()) >= 0){
			htmlReplace = htmlReplace.substring(htmlReplace.indexOf("<html".toLowerCase()), htmlReplace.length());
		}
		
		if(htmlReplace.contains("<AGE_NAO_GRAVAR>")){
			htmlReplace = Utils.retiraConteudoStringEntreDuasPalavras(htmlReplace, "<AGE_NAO_GRAVAR>", "</AGE_NAO_GRAVAR>");
			
		} else if(htmlReplace.contains("<age_nao_gravar>")){
			htmlReplace = Utils.retiraConteudoStringEntreDuasPalavras(htmlReplace, "<age_nao_gravar>", "</age_nao_gravar>");
		}
			
		return htmlReplace;
	}
	
	public boolean isExisteBiometriaDocumento(){
		String html = parametrosVo.getHtml();

		if(Utils.isNullOrEmpty(html)){
			return false;
		}

		return html.indexOf("Assinado Biometricamente") > -1;
	}
	
	private void updateBiometriaGed(BiometriaVo biometriaVo) {
		dao.updateBiometriaGed(biometriaVo);

		logaAcao(biometriaVo.getCodigoEmpresa(), "Alteração", "Biometria","cad225",null,null,biometriaVo.getCodigoFuncionario()+
				" - Ficha: Tipo : "+ biometriaVo.getNomeTipoProntuario()+" Ged: " +biometriaVo.getCodigoGed()+" Arquivo Ged " + biometriaVo.getCodigoArquivoGed(), Utils.toInt(parametrosVo.getCodigoResponsavelAcao()));
	}
	
	private void atualizarFichaXBiometria() {
		BiometriaVo biometriaVo = new BiometriaVo();
		biometriaVo.setTipoProntuario(parametrosVo.getCodigoClassificacao()); 
		biometriaVo.setCodigoGed(parametrosVo.getCodigoSocGed());  
		biometriaVo.setCodigoArquivoGed(arquivoGedPedProcVo.getCodigoArquivoGed()); 
		biometriaVo.setCodigoDedo(parametrosVo.getCodigoDedo());
		
		if(ClassificacaoTipoProntuario.FUNCIONARIO.getIndex() != Utils.toInt(biometriaVo.getTipoProntuario()) && isExisteBiometriaDocumento()){
			biometriaVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
			biometriaVo.setCodigoFuncionario(parametrosVo.getCodigoFuncionario());
			biometriaVo.setCodigoSequencialFicha(parametrosVo.getCodigoSequencialFicha());
			updateBiometriaGed(biometriaVo);
		}
	}
	
}
