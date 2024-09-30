package br.com.age.converter.prontuarios.pdf.vo;

import br.com.age.converter.prontuarios.pdf.enums.ClassificacaoTipoProntuario;
import br.com.age.converter.prontuarios.pdf.utils.Utils;

public class BiometriaVo {
	
	private String tipoProntuario;
	private String codigoGed;
	private String codigoArquivoGed;
	private String codigoDedo;
	private String codigoSequencialFicha;
	private String codigoEmpresa;
	private String codigoFuncionario;
	
	public String getTipoProntuario() {
		return tipoProntuario;
	}
	
	public void setTipoProntuario(String tipoProntuario) {
		this.tipoProntuario = tipoProntuario;
	}
	
	public String getCodigoGed() {
		return codigoGed;
	}
	
	public void setCodigoGed(String codigoGed) {
		this.codigoGed = codigoGed;
	}
	
	public String getCodigoArquivoGed() {
		return codigoArquivoGed;
	}
	
	public void setCodigoArquivoGed(String codigoArquivoGed) {
		this.codigoArquivoGed = codigoArquivoGed;
	}
	
	public String getCodigoDedo() {
		return codigoDedo;
	}
	
	public void setCodigoDedo(String codigoDedo) {
		this.codigoDedo = codigoDedo;
	}

	public String getCodigoSequencialFicha() {
		return codigoSequencialFicha;
	}

	public void setCodigoSequencialFicha(String codigoSequencialFicha) {
		this.codigoSequencialFicha = codigoSequencialFicha;
	}

	public String getCodigoEmpresa() {
		return codigoEmpresa;
	}

	public void setCodigoEmpresa(String codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}

	public String getCodigoFuncionario() {
		return codigoFuncionario;
	}

	public void setCodigoFuncionario(String codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
	}

	public String getNomeTipoProntuario() {
		return ClassificacaoTipoProntuario.getnomeClassificacaoTipoProntario(Utils.toInt(tipoProntuario));
	}
	
}
