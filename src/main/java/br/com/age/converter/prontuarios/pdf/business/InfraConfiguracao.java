package br.com.age.converter.prontuarios.pdf.business;

import br.com.age.converter.prontuarios.pdf.dao.ConverterProntuariosPdfDao;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.InfraConfiguracaoVo;

public class InfraConfiguracao {

	private ConverterProntuariosPdfDao dao;
	private static final String SO_LINUX = "Linux";
	
	public InfraConfiguracao(ConverterProntuariosPdfDao dao) {
		this.dao = dao;
	}
	
	public String getValorRetornoInfraConfiguracaoPeloCodigo(String codigoInfraConfiguracao) {
		return this.getInfraConfiguracaoPeloCodigo(codigoInfraConfiguracao).getValorRetorno();
	}
	
	public InfraConfiguracaoVo getInfraConfiguracaoPeloCodigo(String codigoInfraConfiguracao) {
		InfraConfiguracaoVo infraConfiguracaoVo = dao.getInfraConfiguracaoPeloCodigo(codigoInfraConfiguracao);
		configurarValueSOCorrente(infraConfiguracaoVo);
		
		return infraConfiguracaoVo;
	}
	
	private void configurarValueSOCorrente(InfraConfiguracaoVo infraConfiguracaoVo){
		infraConfiguracaoVo.setValorRetorno(infraConfiguracaoVo.getValorLinux());
		
		if(!System.getProperty("os.name").equalsIgnoreCase(SO_LINUX) || Utils.isNullOrEmpty(infraConfiguracaoVo.getValorLinux())){			
			infraConfiguracaoVo.setValorRetorno(infraConfiguracaoVo.getValorLabel());
		}
	}
	
}
