package br.com.age.converter.prontuarios.pdf.enums;

public enum ClassificacaoTipoProntuario {

	OUTROS(0,"Outros"),
	PPP(1,"PPP"),
	PCMSO(2,"PCMSO"),
	PPRA(3,"PPRA"),
	LTCAT(4,"LTCAT"),
	RELATORIO_ANUAL(5,"Relat�rio Anual"),
	ASO(6,"ASO"),
	ASO_BRANCO(7,"ASO em Branco"),
	FICHA_CLINICA(8,"Ficha Clinica"),	
	FICHA_CLINICA_BRANCO(9,"Ficha Cl�nica em Branco"),
	PEDIDO_EXAME(10,"Pedido de Exame"),
	FUNCIONARIO(11,"Funcion�rio"),
	RESULTADO_EXAME(12,"Resultado de Exame"),
	ENFERMAGEM(13,"Enfermagem"),
	CONSULTA_ASSISTENCIAL(14,"Consulta Assistencial"),
	LICENCA_MEDICA(15,"Licen�a M�dica"),
	FICHA_PERSONALIZADA(16,"Ficha Personalizada"),
	RECEITA_MEDICA(17,"Receita M�dica"),
	IMPORTACAO(18,"Importa��o"),
	ESOCIAL_ASO(19,"eSocial S-2220 Monitoramento da Sa�de do Trabalhador"),
	ESOCIAL_CAT(20,"eSocial S-2210 Comunica��o de Acidente de Trabalho"),
	ESOCIAL_AFASTAMENTO_TEMPORARIO(21,"eSocial S-2230 Afastamento Tempor�rio"),
	ENTREGA_EPI(22,"Entrega de EPI"),
	AVALIACAO_FISICA(23,"Ficha de Avalia��o F�sica"),
	CONDICOES_AMBIENTAIS_TRABALHO(25,"eSocial S-2240 Condi��es Ambientais do Trabalho - Agentes Nocivos"),
	QUALIDADE_VIDA(26,"Ficha de Qualidade de Vida"),
	QUALIDADE_VIDA_EM_BRANCO(27,"Ficha de Qualidade de Vida em Branco"),
	RESULTADO_EXAME_ESPIROMETRIA(51, "Espirometria"),
	RESULTADO_EXAME_ROTINA_URINA(52, "Rotina Urina"),
	RESULTADO_EXAME_HEMOGRAMA(53, "Hemograma"),
	RESULTADO_EXAME_AUDIOMETRIA(54, "Audiometria"),
	RESULTADO_EXAME_PERSONALIZADO(55, "Exame Personalizado"),
	ACIDENTE(56,"Acidente"),
	EXPORTA_DADOS(57,"Exporta Dados"),
	FICHA_ODONTOLOGICA(58,"Ficha Odontol�gica"),
	CADASTRO_DINAMICO(59,"Cadastro Din�mico"),
	MANUTENCAO_PPP(60,"Manuten��o de PPP"),
	FICHA_INSPECAO(61,"Ficha de Inspe��o"),
	PCA(62,"PCA"),
	MANDATO_CIPA(63,"Mandato CIPA"),
	MEDICAO_SESI(64, "Medi��o SESI"),
	SESI_AET(65, "AET - An�lise Ergon�mica do Trabalho"),
	SESI_ANALISE_PRELIMINAR_VIBRACAO(66, "An�lise Preliminar de Vibra��o"),
	SESI_LTCAT(67, "LTCAT SESI"),
	SESI_LAUDO_INSALUBRIDADE(68, "Laudo de Insalubridade"),
	LAUDO_PERICULOSIDADE(69, "Laudo de Periculosidade"),
	SESI_LEVANTAMENTO_FATORES_RISCOS_ACIDENTES(70, "Levantamento de Fatores de Riscos de Acidentes"),
	SESI_PCMSO(71, "PCMSO SESI"),
	SESI_PPRA(72, "PPRA SESI"),
	SESI_RELATORIO_ANUAL_PPPA(73, "Relat�rio Anual PPPA"),
	SESI_RELATORIO_AVALIACAO_QUANTITATIVA(74, "Relat�rio Avalia��o Quantitativa"),
	SESI_RELATORIO_ANUAL(75, "Relat�rio Anual SESI"),
	SESI_PCMAT(76, "PCMAT SESI"),
	SESI_AGRUPAMENTO_ERGONOMICO(77, "Agrupamento Ergon�mico"),
	CARTA_NAO_COMPARECIMENTO(78, "Carta de N�o Comparecimento"),
	ATESTADO(79, "Atestado"),
	TREINAMENTO(80,"Treinamento"),
	CONTESTACAO_FAP(81,"Contesta��o FAP"),	
	CONTROLE_ATENDIMENTOS(82,"Controle de atendimentos"),
	TERMO_ACEITE_ASO(83, "Termo de Aceite"),
	VIDEOCHAMADA(84,"Videochamada"),
	ORDEM_SERVICO(85, "Ordem de Servi�o"),
	AFASTAMENTO_PREVIDENCIARIO_INSS(86, "Afastamento Previdenci\u00e1rio INSS"),
	SESI_ATIVIDADES(87, "Atividades"),
	FATURA_DO_PRESTADOR(88, "Fatura do Prestador"),
	RECIBO_ENTREGA_EXAMES(89, "Recibo de Entrega de Exames"),
	DEFICIENCIA(90,"Defici�ncia"),
	RELATORIO_ANALITICO(91, "Relat�rio Anal�tico"),
	PGR(92, "PGR"),
	SESI_PGR(93, "PGR SESI"),
	GRO_PLANO_DE_ACAO(100, "GRO - Plano de A��o"),
	ENCAMINHAMENTO(101, "Encaminhamento"),
	LAUDO_INSALUBRIDADE(102, "Laudo de Insalubridade"),
	RELATORIO_FATURA(103, "Relat�rio de Fatura"),
	NR18_PROJETOS(104, "NR18 - Projetos");
	
	private int index;
	private String nome;
	
	private ClassificacaoTipoProntuario(int index, String nome) {
		this.index = index;
		this.nome = nome;
	}

	public String valueOf(){
		return String.valueOf(index);
	}
	
	public String nameOf(){
		return String.valueOf(nome);
	}
	
	public static String getnomeClassificacaoTipoProntario(int tipoClass){
		String nomeTipoGed = null;
		boolean econtrado;

		for(ClassificacaoTipoProntuario tipo : ClassificacaoTipoProntuario.values()){
			econtrado = tipo.index == tipoClass;
			if(econtrado){
				return tipo.nome;
			}
		}
		
		return nomeTipoGed;
	}
	
	public String getNome() {
		return nome;
	}
	
	public int getIndex() {
		return index;
	}

	
}
