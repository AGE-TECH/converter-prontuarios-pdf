package br.com.age.converter.prontuarios.pdf.helper;

import java.util.List;

import br.com.age.converter.prontuarios.pdf.dao.ConverterProntuariosPdfDao;
import br.com.age.converter.prontuarios.pdf.exceptions.BusinessException;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.GedVo;
import br.com.age.converter.prontuarios.pdf.vo.LogSftpVo;

public class ArquivoGedHelper {

	private ConverterProntuariosPdfDao dao;
	
	public static final String NOME_DIRETORIO_PEDPROC_ARQUIVO_GED = "105";
	public static final String COD_REPL_EMPPRI = "XX_EMPPRI_XX";
	public static final String COD_REPL_EMP = "XX_EMP_XX";
	public static final String COD_DIRETORIO_GED = "XX_CODGED_XX";
	public static final String SO_LINUX = "Linux";
	
	public ArquivoGedHelper(ConverterProntuariosPdfDao dao) {
		this.dao = dao;
	}

	public void verificaExistenciaCredenciaParaGerarRegViaPedProc(String empresaPrincipal, String empresa, String codigoGed, String nomeArquivo, String codigoArquivoGed, String codigoTipoGed) throws BusinessException{
		verificaExistenciaCredenciaParaGerarReg(empresaPrincipal, empresa, codigoGed, nomeArquivo, codigoArquivoGed, codigoTipoGed, empresa);
	}
	
	private void verificaExistenciaCredenciaParaGerarReg(String empresaPrincipal, String empresaGed, String codigoGed, String nomeArquivo, String codigoArquivoGed, String tipoGed, String codEmpNavegacao) throws BusinessException{
		String codigoTipoGed = tipoGed;
		
		if(Utils.isNullOrEmptyOrZero(codigoTipoGed)){
			codigoTipoGed = getGedPorEmpresaCod(empresaPrincipal, empresaGed, codigoGed);
			
			if(codigoTipoGed == null){
				return;
			}
		}
		
		List<LogSftpVo> logsCredenciais = setDadosCredenciaisGed(empresaPrincipal, codEmpNavegacao, codigoGed, codigoArquivoGed, codigoTipoGed, false);
		
		if (!Utils.isValoresIguais(empresaPrincipal, codEmpNavegacao) && Utils.isNullOrEmptyList(logsCredenciais)){
			logsCredenciais = setDadosCredenciaisGed(empresaPrincipal, empresaPrincipal, codigoGed, codigoArquivoGed, codigoTipoGed, true);
		}
		
		
		if(!Utils.isNullOrEmptyList(logsCredenciais)){
			for(LogSftpVo vo : logsCredenciais){
				insereLogEnvioSFTP(nomeArquivo, codigoGed, empresaPrincipal, vo.getCodigoEmpresa(), empresaGed, codigoArquivoGed, codigoTipoGed, vo.getNomeServidor(), vo.getCodigoCredencial());
			}
		}
	}
	
	public void insereLogEnvioSFTP(String nomeArquivo, String codigoGed, 
								   String codEmpPri, String codEmpCredencial,
								   String codEmpGed, String codArquivoGed, 
								   String tipoGedCredencial, String nomeServidor, 
								   String codigoCredencial) {
		
		if(!Utils.isNullOrEmpty(tipoGedCredencial)){
			LogSftpVo logSftpVo = new LogSftpVo();
			logSftpVo.setCodigoEmpresaPrincipal(codEmpPri);
			logSftpVo.setCodigoEmpresa(codEmpCredencial);
			logSftpVo.setCodigoEmpresaGed(codEmpGed);
			logSftpVo.setCodigoCredencial(codigoCredencial);
			logSftpVo.setCodigoSocGed(codigoGed);
			logSftpVo.setCodigoArquivoSocGed(codArquivoGed);
			logSftpVo.setCodigoTipoGed(tipoGedCredencial);
			logSftpVo.setNomeArquivo(nomeArquivo);
			logSftpVo.setNomeServidor(nomeServidor);
			
			dao.insereLogEnvioSFTP(logSftpVo);
		}
	}
	
	private List<LogSftpVo> setDadosCredenciaisGed(String empresaPrincipal, String empresa, String codigoGed, String codigoArquivoGed, String tipoGed, boolean procuraNaPrincipal) {
		GedVo gedVo = new GedVo();
		gedVo.setCodigoEmpresaPrincipal(empresaPrincipal);
		gedVo.setCodigoEmpresa(empresa);
		gedVo.setCodigo(codigoGed);
		gedVo.setCodigoTipoGed(tipoGed);
		gedVo.setCodigoArquivoGed(codigoArquivoGed);
		
		return dao.getLogsDaCredencialDeUmGedPorEmpresa(gedVo, procuraNaPrincipal);
	}
	
	private String getGedPorEmpresaCod(String empresaPrincipal, String empresa, String codigoGed) throws BusinessException {
		GedVo gedVo = new GedVo();
		gedVo.setCodigo(codigoGed);
		gedVo.setCodigoEmpresa(empresa);
		gedVo.setCodigoEmpresaPrincipal(empresaPrincipal);
		
		gedVo = dao.getGedPeloCodigo(gedVo);
		
		if(gedVo == null){
			return null;
		}
		
		return gedVo.getCodigoTipoGed();
	}
	
}
