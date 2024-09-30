package br.com.age.converter.prontuarios.pdf.business;

import br.com.age.converter.prontuarios.pdf.dao.ConverterProntuariosPdfDao;
import br.com.age.converter.prontuarios.pdf.utils.InfraConfiguracaoKey;
import br.com.age.converter.prontuarios.pdf.utils.SecurityUtils;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.ParametrosVo;
import br.com.age.converter.prontuarios.pdf.vo.VerificacaoDocumentosVo;

public class VerificacaoDocumentos {
	
	private static final String REPLACE_TOKEN = "{TOKEN}";
	private ConverterProntuariosPdfDao dao;
	
	public VerificacaoDocumentos() {
	}
	
	public VerificacaoDocumentos(ConverterProntuariosPdfDao dao) {
		this.dao = dao;
	}

	public String gerarTokenUnicoVerificacaoDocumentos(String awsRequestId) {
		return SecurityUtils.defaults().hash(awsRequestId + System.currentTimeMillis());
	}
		
	public void inserirVerificacaoDocumentos(ParametrosVo parametrosVo) {
		if (!Utils.isNullOrEmpty(parametrosVo.getTokenVerificacaoDocumento())) {
			dao.insertVerificacaoDocumento(getVerificacaoDocumentosVo(parametrosVo));
		}
	}

	private VerificacaoDocumentosVo getVerificacaoDocumentosVo(ParametrosVo parametrosVo) {
		VerificacaoDocumentosVo verificacaoDocumentosVo = new VerificacaoDocumentosVo();
		
		verificacaoDocumentosVo.setCodigoEmpresaPrincipal(parametrosVo.getCodigoEmpresaPrincipal());
		verificacaoDocumentosVo.setCodigoEmpresa(parametrosVo.getCodigoEmpresa());
		verificacaoDocumentosVo.setCodigoGed(parametrosVo.getCodigoSocGed());
		verificacaoDocumentosVo.setCodigoArquivoGed(parametrosVo.getCodigoArquivoSocGed());
		verificacaoDocumentosVo.setDescricaoToken(parametrosVo.getTokenVerificacaoDocumento());
		
		return verificacaoDocumentosVo;
	}
	
	public String gerarUrlVerificacaoDocumentos(String token, InfraConfiguracao infraConfiguracaoBusiness) {				
		return getPadraoUrlVerificacaoArquivos(infraConfiguracaoBusiness).replace(REPLACE_TOKEN, token);
	}

	private String getPadraoUrlVerificacaoArquivos(InfraConfiguracao infraConfiguracaoBusiness) {
		return infraConfiguracaoBusiness.getValorRetornoInfraConfiguracaoPeloCodigo(InfraConfiguracaoKey.URL_VERIFICACAO_DOCUMENTOS);
	}

}
