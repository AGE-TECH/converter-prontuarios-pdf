package br.com.age.converter.prontuarios.pdf.vo;

public class GedVo {

	private String rowid;
	private String codigoEmpresa;
	private String codigoEmpresaPrincipal;
	private String codigo;
	private String nome;
	private String dataEmissao;
	private String dataValidade;
	private String dataUpload;
	private String localFisico;
	private String revisao;
	private String codigoFuncionario;
	private String codigoTipoGed;
	private String classificacaoTipoGed;
	private String codigoMandatoCipa;
	private String codigoSequencialFicha;
	private String nomeFuncionario;
	private String codigoResponsavelAcao;
	private String codigoUnidade;
	private String codigoResponsavel;
	private String codigoTurma;
	private String codigoExportaDados;
	private String codigoEmpresaClienteSocnet;
	private String codigoRegistroCadastroDinamico;
	private String codigoCadastroDinamico;
	private String dataCartaNaoComparecimento;
	private String codigoFatura;
	private String comentarioAdicionalLog;
	private String codigoArquivoGed;
	
	private boolean realizarRegistroLogSesiGed;
	private boolean criouFicha;
	private boolean cartaNaoComparecimento;
	private boolean todasEmpresas;
	private boolean tipoGedAtivo;
	private boolean mostraCadastroFuncGed;
	private boolean acessoSocNet;
	
	public String getRowid() {
		return rowid;
	}
	
	public void setRowid(String rowid) {
		this.rowid = rowid;
	}
	
	public String getCodigoEmpresa() {
		return codigoEmpresa;
	}
	
	public void setCodigoEmpresa(String codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}
	
	public String getCodigoEmpresaPrincipal() {
		return codigoEmpresaPrincipal;
	}
	
	public void setCodigoEmpresaPrincipal(String codigoEmpresaPrincipal) {
		this.codigoEmpresaPrincipal = codigoEmpresaPrincipal;
	}
	
	public String getCodigo() {
		return codigo;
	}
	
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getDataEmissao() {
		return dataEmissao;
	}
	
	public void setDataEmissao(String dataEmissao) {
		this.dataEmissao = dataEmissao;
	}
	
	public String getDataValidade() {
		return dataValidade;
	}
	
	public void setDataValidade(String dataValidade) {
		this.dataValidade = dataValidade;
	}
	
	public String getDataUpload() {
		return dataUpload;
	}
	
	public void setDataUpload(String dataUpload) {
		this.dataUpload = dataUpload;
	}
	
	public String getLocalFisico() {
		return localFisico;
	}
	
	public void setLocalFisico(String localFisico) {
		this.localFisico = localFisico;
	}
	
	public String getRevisao() {
		return revisao;
	}
	
	public void setRevisao(String revisao) {
		this.revisao = revisao;
	}
	
	public String getCodigoFuncionario() {
		return codigoFuncionario;
	}
	
	public void setCodigoFuncionario(String codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
	}
	
	public String getCodigoTipoGed() {
		return codigoTipoGed;
	}
	
	public void setCodigoTipoGed(String codigoTipoGed) {
		this.codigoTipoGed = codigoTipoGed;
	}
	
	public boolean isMostraCadastroFuncGed() {
		return mostraCadastroFuncGed;
	}
	
	public void setMostraCadastroFuncGed(boolean mostraCadastroFuncGed) {
		this.mostraCadastroFuncGed = mostraCadastroFuncGed;
	}
	
	public String getClassificacaoTipoGed() {
		return classificacaoTipoGed;
	}
	
	public void setClassificacaoTipoGed(String classificacaoTipoGed) {
		this.classificacaoTipoGed = classificacaoTipoGed;
	}
	
	public String getCodigoMandatoCipa() {
		return codigoMandatoCipa;
	}
	
	public void setCodigoMandatoCipa(String codigoMandatoCipa) {
		this.codigoMandatoCipa = codigoMandatoCipa;
	}
	
	public boolean isTipoGedAtivo() {
		return tipoGedAtivo;
	}

	public void setTipoGedAtivo(boolean tipoGedAtivo) {
		this.tipoGedAtivo = tipoGedAtivo;
	}

	public String getCodigoSequencialFicha() {
		return codigoSequencialFicha;
	}

	public void setCodigoSequencialFicha(String codigoSequencialFicha) {
		this.codigoSequencialFicha = codigoSequencialFicha;
	}

	public String getNomeFuncionario() {
		return nomeFuncionario;
	}

	public void setNomeFuncionario(String nomeFuncionario) {
		this.nomeFuncionario = nomeFuncionario;
	}

	public String getCodigoResponsavelAcao() {
		return codigoResponsavelAcao;
	}

	public void setCodigoResponsavelAcao(String codigoResponsavelAcao) {
		this.codigoResponsavelAcao = codigoResponsavelAcao;
	}

	public String getCodigoUnidade() {
		return codigoUnidade;
	}

	public void setCodigoUnidade(String codigoUnidade) {
		this.codigoUnidade = codigoUnidade;
	}

	public String getCodigoResponsavel() {
		return codigoResponsavel;
	}

	public void setCodigoResponsavel(String codigoResponsavel) {
		this.codigoResponsavel = codigoResponsavel;
	}

	public boolean isAcessoSocNet() {
		return acessoSocNet;
	}

	public void setAcessoSocNet(boolean acessoSocNet) {
		this.acessoSocNet = acessoSocNet;
	}

	public String getCodigoTurma() {
		return codigoTurma;
	}

	public void setCodigoTurma(String codigoTurma) {
		this.codigoTurma = codigoTurma;
	}

	public String getCodigoEmpresaClienteSocnet() {
		return codigoEmpresaClienteSocnet;
	}

	public void setCodigoEmpresaClienteSocnet(String codigoEmpresaClienteSocnet) {
		this.codigoEmpresaClienteSocnet = codigoEmpresaClienteSocnet;
	}

	public String getCodigoRegistroCadastroDinamico() {
		return codigoRegistroCadastroDinamico;
	}

	public void setCodigoRegistroCadastroDinamico(String codigoRegistroCadastroDinamico) {
		this.codigoRegistroCadastroDinamico = codigoRegistroCadastroDinamico;
	}

	public String getCodigoCadastroDinamico() {
		return codigoCadastroDinamico;
	}

	public void setCodigoCadastroDinamico(String codigoCadastroDinamico) {
		this.codigoCadastroDinamico = codigoCadastroDinamico;
	}

	public String getCodigoExportaDados() {
		return codigoExportaDados;
	}

	public void setCodigoExportaDados(String codigoExportaDados) {
		this.codigoExportaDados = codigoExportaDados;
	}

	public String getDataCartaNaoComparecimento() {
		return dataCartaNaoComparecimento;
	}

	public void setDataCartaNaoComparecimento(String dataCartaNaoComparecimento) {
		this.dataCartaNaoComparecimento = dataCartaNaoComparecimento;
	}

	public String getCodigoFatura() {
		return codigoFatura;
	}

	public void setCodigoFatura(String codigoFatura) {
		this.codigoFatura = codigoFatura;
	}

	public boolean isTodasEmpresas() {
		return todasEmpresas;
	}

	public void setTodasEmpresas(boolean todasEmpresas) {
		this.todasEmpresas = todasEmpresas;
	}

	public boolean isCartaNaoComparecimento() {
		return cartaNaoComparecimento;
	}

	public void setCartaNaoComparecimento(boolean cartaNaoComparecimento) {
		this.cartaNaoComparecimento = cartaNaoComparecimento;
	}

	public boolean isCriouFicha() {
		return criouFicha;
	}

	public void setCriouFicha(boolean criouFicha) {
		this.criouFicha = criouFicha;
	}

	public String getComentarioAdicionalLog() {
		return comentarioAdicionalLog;
	}

	public void setComentarioAdicionalLog(String comentarioAdicionalLog) {
		this.comentarioAdicionalLog = comentarioAdicionalLog;
	}

	public boolean isRealizarRegistroLogSesiGed() {
		return realizarRegistroLogSesiGed;
	}

	public void setRealizarRegistroLogSesiGed(boolean realizarRegistroLogSesiGed) {
		this.realizarRegistroLogSesiGed = realizarRegistroLogSesiGed;
	}

	public String getCodigoArquivoGed() {
		return codigoArquivoGed;
	}

	public void setCodigoArquivoGed(String codigoArquivoGed) {
		this.codigoArquivoGed = codigoArquivoGed;
	}
	
}
