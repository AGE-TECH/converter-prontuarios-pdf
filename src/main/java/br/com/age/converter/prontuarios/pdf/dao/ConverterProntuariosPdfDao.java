package br.com.age.converter.prontuarios.pdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.age.converter.prontuarios.pdf.db.Database;
import br.com.age.converter.prontuarios.pdf.db.DatabaseReplica;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.ArquivoGedVo;
import br.com.age.converter.prontuarios.pdf.vo.BiometriaVo;
import br.com.age.converter.prontuarios.pdf.vo.EmpVo;
import br.com.age.converter.prontuarios.pdf.vo.FuncionarioVo;
import br.com.age.converter.prontuarios.pdf.vo.GedVo;
import br.com.age.converter.prontuarios.pdf.vo.InfraConfiguracaoVo;
import br.com.age.converter.prontuarios.pdf.vo.LogSftpVo;
import br.com.age.converter.prontuarios.pdf.vo.LogVo;
import br.com.age.converter.prontuarios.pdf.vo.ParametrosVo;
import br.com.age.converter.prontuarios.pdf.vo.QRCodeVo;
import br.com.age.converter.prontuarios.pdf.vo.SoParamVo;
import br.com.age.converter.prontuarios.pdf.vo.UsuarioVo;
import br.com.age.converter.prontuarios.pdf.vo.VerificacaoDocumentosVo;

public class ConverterProntuariosPdfDao {

	private static final String ESPACO = " ";
	private static final String REGEX_BIND_VARIABLES = "[^,()]*";
	private static final String ESPACO_INTERROGACAO = " ?";
	private static final String DOIS_ESPACOS = "  ";
	
	private Database database;
	private DatabaseReplica databaseReplica;
	
	public ConverterProntuariosPdfDao(Database database, DatabaseReplica databaseReplica) {
		this.database = database;
		this.databaseReplica = databaseReplica;
	}
	
	public InfraConfiguracaoVo getInfraConfiguracaoPeloCodigo(String codigoInfraConfiguracao) {
		InfraConfiguracaoVo infraConfiguracaoVo = null;
		int i = 1;
		
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT rowid, cd_configuracao, ds_label, ds_valor, tp_configuracao, ic_situacao, ds_valor_linux ").
			append(" FROM infra_configuracao  ").
			append(" WHERE cd_configuracao = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			
			ps.setInt(i++, Utils.toInt(codigoInfraConfiguracao));
			
			try(ResultSet rs = ps.executeQuery()){
				if (rs.next()) {
					infraConfiguracaoVo = new InfraConfiguracaoVo();
					
					infraConfiguracaoVo.setRowid(rs.getString("rowid"));
					infraConfiguracaoVo.setCodigo(rs.getString("cd_configuracao"));
					infraConfiguracaoVo.setNomeLabel(rs.getString("ds_label"));
					infraConfiguracaoVo.setValorLabel(rs.getString("ds_valor"));
					infraConfiguracaoVo.setTipoConfiguracao(rs.getString("tp_configuracao"));
					infraConfiguracaoVo.setSituacao(rs.getBoolean("ic_situacao"));
					infraConfiguracaoVo.setValorLinux(rs.getString("ds_valor_linux"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return infraConfiguracaoVo;
	}

	public SoParamVo getSoParam() {
		SoParamVo soParamVo = null;
		
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT ds_url_codigo_barra, ds_url_codigo_barra_interno, endereco, javamenu ").
			append(" FROM soparam ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			try(ResultSet rs = ps.executeQuery()){
				if (rs.next()) {
					soParamVo = new SoParamVo();
					
					soParamVo.setUrlCodigoBarra(rs.getString("ds_url_codigo_barra"));
					soParamVo.setUrlCodigoBarraInterno(rs.getString("ds_url_codigo_barra_interno"));
					soParamVo.setEndereco(rs.getString("endereco"));
					soParamVo.setJavamenu(rs.getString("javamenu"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return soParamVo;
	}
	
	public GedVo getGedDeUmProntuarioPeloCodigoSequencialComTipoGed(GedVo gedVo) {
		GedVo gedVoTemp = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT g.cd_ged ").
			append(" FROM ged g ").
			append(" JOIN tipo_ged tg ON tg.cd_empresa = ? AND g.cd_tipo_ged = tg.cd_tipo_ged  ").
			append(" WHERE g.cd_empresa = ? ");
		
		if(!Utils.isNullOrEmpty(gedVo.getCodigoSequencialFicha())){
			qry.append(" AND g.cd_seq_ficha = ? ");
		}
		
		qry.append(" AND tg.cd_tipo_ged = ? ").
			append(" AND g.cd_funcionario = ? ").
			append(" AND tg.ic_ativo = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			
			if(!Utils.isNullOrEmpty(gedVo.getCodigoSequencialFicha())){
				ps.setInt(i++, Utils.toInt(gedVo.getCodigoSequencialFicha()));
			}
			
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoTipoGed()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoFuncionario()));
			ps.setBoolean(i++, true);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					gedVoTemp = new GedVo();
					
					gedVoTemp.setCodigo(rs.getString("cd_ged"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return gedVoTemp;
	}

	public GedVo getGedComMandato(GedVo gedVo) {
		GedVo gedVoTemp = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT g.cd_ged ").
			append(" FROM ged g ").
			append(" INNER JOIN tipo_ged tg ON tg.cd_empresa = ? AND g.cd_tipo_ged = tg.cd_tipo_ged ").
			append(" WHERE g.cd_empresa = ?  ").
			append(" AND g.cd_mandato_cipa = ? ").
			append(" AND tg.ic_ativo = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoMandatoCipa()));
			ps.setBoolean(i++, true);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					gedVoTemp = new GedVo();
					
					gedVoTemp.setCodigo(rs.getString("cd_ged"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return gedVoTemp;
	}

	public GedVo getGedDeUmProntuarioPeloCodigoMandatoCipaComTipoGed(GedVo gedVo) {
		GedVo gedVoTemp = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT g.cd_ged ").
			append(" FROM ged g ").
			append(" JOIN tipo_ged tg ON tg.cd_empresa = ? AND g.cd_tipo_ged = tg.cd_tipo_ged  ").
			append(" WHERE g.cd_empresa = ? ");
		
		if(!Utils.isNullOrEmpty(gedVo.getCodigoMandatoCipa())){
			qry.append(" AND g.cd_mandato_cipa = ? ");
		}
		
		qry.append(" AND tg.cd_tipo_ged = ? ").
			append(" AND g.cd_funcionario = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			
			if(!Utils.isNullOrEmpty(gedVo.getCodigoMandatoCipa())){
				ps.setInt(i++, Utils.toInt(gedVo.getCodigoMandatoCipa()));
			}
			
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoTipoGed()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoFuncionario()));
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					gedVoTemp = new GedVo();

					gedVoTemp.setCodigo(rs.getString("cd_ged"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return gedVoTemp;
	}

	public UsuarioVo getUser(int codigoUsuarioAcao) {
		UsuarioVo usuarioVo = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT subcod ").
			append(" FROM usu ").
			append(" WHERE codigo = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, codigoUsuarioAcao);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					usuarioVo = new UsuarioVo();
					
					usuarioVo.setSubCod(rs.getString("subcod"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return usuarioVo;
	}

	public EmpVo getEmp(int codigoEmpresa) {
		EmpVo empVo = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT resp, cd_empresa_principal ").
			append(" FROM emp ").
			append(" WHERE cod = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, codigoEmpresa);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					empVo = new EmpVo();
					
					empVo.setResp(rs.getString("resp"));
					empVo.setCodigoEmpresaPrincipal(rs.getString("cd_empresa_principal"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return empVo;
	}

	public FuncionarioVo getFuncionarioPeloCodigoSimples(FuncionarioVo funcionarioVo) {
		int i = 1;
		FuncionarioVo funcionarioRetornoVo = null;
		
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT cod, nome, site ").
			append(" FROM funciona ").
			append(" WHERE emp = ? ").
			append(" AND cod = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(funcionarioVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(funcionarioVo.getCodigoFuncionario()));
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					funcionarioRetornoVo = new FuncionarioVo();
					
					funcionarioRetornoVo.setCodigoEmpresa(funcionarioVo.getCodigoEmpresa());
					funcionarioRetornoVo.setCodigoFuncionario(rs.getString("cod"));
					funcionarioRetornoVo.setNomeFuncionario(rs.getString("nome"));
					funcionarioRetornoVo.setCodigoUnidade(rs.getString("site"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return funcionarioRetornoVo;
	}

	public int getCodigoEmpresaPrincipalFromEmpresaCliente(int codigoEmpresaCliente) {
	    int codigoEmpresaPrincipal = 0;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT cod ").
			append(" FROM emp  ").
			append(" WHERE resp = (SELECT resp FROM emp WHERE cod = ?) ").
			append(" AND empresp = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {

			ps.setInt(i++, codigoEmpresaCliente);
			ps.setInt(i++, 1);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					codigoEmpresaPrincipal = rs.getInt("cod");
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return codigoEmpresaPrincipal;
	}

	public GedVo getGedPorNome(GedVo gedVo) {
		GedVo gedVoTemp = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT g.rowid, g.cd_empresa, g.cd_ged, g.nm_ged ").
			append(" FROM ged g ").
			append(" JOIN emp e ON g.cd_empresa = e.cod ").
			append(" JOIN tipo_ged tg ON e.cd_empresa_principal = tg.cd_empresa and g.cd_tipo_ged = tg.cd_tipo_ged ").
			append(" WHERE g.cd_empresa = ? ").
			append(" AND g.nm_ged = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			ps.setString(i++, gedVo.getNome());
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					gedVoTemp = new GedVo();

					gedVoTemp.setCodigo(rs.getString("cd_ged"));
					gedVoTemp.setNome(rs.getString("nm_ged"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return gedVoTemp;
	}

	public String getResponsavelPeloCodigoDaEmpresa(int codigoEmpresa) {
		String codigoResponsavel = "";
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT resp FROM emp WHERE cod = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {

			ps.setInt(i++, codigoEmpresa);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					codigoResponsavel = rs.getString("resp");
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return codigoResponsavel;
	}

	public String getSequenceGed() {
		String sequenceGed = "";
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" select NextValArquivosGed(?) ");
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {

			ps.setString(i++, "_seq_arquivos_ged");
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					sequenceGed = String.valueOf(rs.getInt(1));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sequenceGed;
	}

	public void insertGed(GedVo gedVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append(" INSERT INTO ged ").
	 		append(" (cd_empresa , ").
	 		append(" cd_ged , ").
	 		append(" cd_unidade , ").
	 		append(" nm_ged , ").
	 		append(" ds_local_fisico , ").
	 		append(" dt_validade, ").
	 		append(" dt_emissao, ").
	 		append(" dt_upload , ").
	 		append(" ds_revisao, ").
	 		append(" cd_funcionario, ").
	 		append(" cd_tipo_ged, ").
	 		append(" cd_seq_ficha, ").
	 		append(" ic_todas_empresas, ").
	 		append(" cd_mandato_cipa , ").
	 		append(" cd_exporta_dados , ").
	 		append(" ic_criado_socnet , ").
	 		append(" cd_empresa_cliente_socnet, ").
	 		append(" cd_cadastro_dinamico, ").
	 		append(" cd_registro_cadastro_dinamico, ").
	 		append(" ic_carta_nao_comparecimento, ").
	 		append(" dt_carta_nao_comparecimento, ").
	 		append(" ic_criou_ficha, ").
	 		append(" cd_turma, ").
	 		append(" cd_fatura ").
	 		append(" ) ");

 		qry.append(getValues(qry));
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			ps.setLong(i++, Utils.toLong(gedVo.getCodigo()));
			ps.setString(i++, gedVo.getCodigoUnidade());
			ps.setString(i++, gedVo.getNome());
			ps.setString(i++, gedVo.getLocalFisico());
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(gedVo.getDataValidade())));
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(gedVo.getDataEmissao())));
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(gedVo.getDataUpload())));
			ps.setString(i++, gedVo.getRevisao());
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoFuncionario()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoTipoGed()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoSequencialFicha()));
			ps.setBoolean(i++, gedVo.isTodasEmpresas());
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoMandatoCipa()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoExportaDados()));
			ps.setBoolean(i++, gedVo.isAcessoSocNet());
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaClienteSocnet()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoCadastroDinamico()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoRegistroCadastroDinamico()));
			ps.setBoolean(i++, gedVo.isCartaNaoComparecimento());
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(gedVo.getDataCartaNaoComparecimento())));
			ps.setBoolean(i++, gedVo.isCriouFicha());
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoTurma()));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoFatura()));
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public final String getValues(CharSequence qry) throws IllegalArgumentException {
		String busca = qry.toString();
		int abertura = busca.indexOf('(');
		int fechamento = busca.indexOf(')');

		if(abertura == -1 || fechamento == -1){
			throw new IllegalArgumentException("Falta algum parentese na qry");
		}

		busca = busca.substring(abertura, fechamento+1);

		abertura = busca.replaceAll("[^(]*", "").length();
		fechamento = busca.replaceAll("[^)]*", "").length();

		if(abertura != 1  || fechamento != 1){
			throw new IllegalArgumentException("Verifique os colcehetes de abertura e fechamento da qry ( )");
		}

		busca = busca.replaceAll(REGEX_BIND_VARIABLES,ESPACO).replace(DOIS_ESPACOS, ESPACO_INTERROGACAO);

		busca = "values".concat(busca);

		return busca;
	}

	public FuncionarioVo getFuncionarioPeloRowidSimples(FuncionarioVo funcionarioVo) {
		int i = 1;
		FuncionarioVo funcionarioRetornoVo = null;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT emp, cod, nome ").
			append(" FROM funciona ").
			append(" WHERE rowid = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(funcionarioVo.getRowid()));
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					funcionarioRetornoVo = new FuncionarioVo();
					
					funcionarioRetornoVo.setCodigoEmpresa(rs.getString("emp"));
					funcionarioRetornoVo.setCodigoFuncionario(rs.getString("cod"));
					funcionarioRetornoVo.setNomeFuncionario(rs.getString("nome"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return funcionarioRetornoVo;
	}

	public long getSequencialLog() {
		long sequenceLog = 0;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" select NextValLogAcaoNova(?) ");
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {

			ps.setString(i++, "seq_log_acao");
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					sequenceLog = rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sequenceLog;
	}

	public void insertLogAcao(LogVo logVo, ParametrosVo parametrosVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append(" INSERT INTO logacao ( ").
			append(" cd_log , ").
			append(" usu, ").
			append(" programa, ").
			append(" nm_programa, ").
			append(" emp, ").
			append(" obs, ").
			append(" acao, ").
			append(" data,  ").
			append(" hora, ").
			append(" ic_exibe_log_navegacao,  ").
			append(" ic_socnet, ").
			append(" cd_responsavel_usuario)  ").
			append(getValues(qry));

		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setLong(i++, Utils.toLong(logVo.getCodigoLog()));
			ps.setInt(i++, logVo.getCodigoUsuario());
			ps.setString(i++, logVo.getPrograma());
			ps.setString(i++, logVo.getNomePrograma());
			ps.setInt(i++, logVo.getCodigoEmpresa());
			ps.setString(i++, logVo.getObs());
			ps.setString(i++, logVo.getAcao());
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(parametrosVo.getDataDoDia())));
			ps.setInt(i++, parametrosVo.getHoraAtual());
			ps.setBoolean(i++, logVo.isExibeLogNavegacao());
			ps.setBoolean(i++, logVo.isSocnet());
			ps.setInt(i++, logVo.getCodigoResponsavelUsuario());
			
			ps.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public ArquivoGedVo getArquivoGedPeloNome(String codigoEmpresa, String codigoGed, String nomeArquivo) {
		int i = 1;
		ArquivoGedVo arquivoGedVo = null;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT rowid, cd_empresa, cd_empresa_principal, cd_ged, cd_arquivo_ged, nm_arquivos_ged, nm_diretorio, dt_upload ").
			append(" , cd_tipo_documento, ic_assinado_digitalmente, hr_upload, cd_vault_id, qtd_assinatura_permitida, qtd_assinatura ").
			append(" FROM arquivos_ged ").
			append(" WHERE cd_empresa = ? ").
			append(" AND cd_ged = ? ").
			append(" AND nm_arquivos_ged = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(codigoEmpresa));
     		ps.setLong(i++, Utils.toLong(codigoGed));
			ps.setString(i++, nomeArquivo);
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					arquivoGedVo = new ArquivoGedVo();
					
					arquivoGedVo.setRowid(rs.getString("rowid"));
					arquivoGedVo.setCodigoGed(rs.getString("cd_ged"));
					arquivoGedVo.setCodigoArquivoGed(rs.getString("cd_arquivo_ged"));
					arquivoGedVo.setCodigoEmpresa(rs.getString("cd_empresa"));
					arquivoGedVo.setCodigoEmpresaPrincipal(rs.getString("cd_empresa_principal"));
					arquivoGedVo.setNomeDoArquivo(rs.getString("nm_arquivos_ged"));
					arquivoGedVo.setNomeDoDiretorio(rs.getString("nm_diretorio"));
					arquivoGedVo.setDataUpload(Utils.dataFormatada(rs.getDate("dt_upload")));
					arquivoGedVo.setHoraUpload(Utils.formataHora(rs.getString("hr_upload")));
					arquivoGedVo.setCodigoTipoDocumento(rs.getString("cd_tipo_documento"));
					arquivoGedVo.setAssinadoDigitalmente(rs.getBoolean("ic_assinado_digitalmente"));
					arquivoGedVo.setVaultId(rs.getString("cd_vault_id"));
					arquivoGedVo.setQuantidadeAssinaturasPermitidas(rs.getInt("qtd_assinatura_permitida"));
					arquivoGedVo.setQuantidadeAssinaturas(rs.getInt("qtd_assinatura"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return arquivoGedVo;
	}

	public ArquivoGedVo getArquiVoGedPorCodigo(ArquivoGedVo arquivoGedVo) {
		int i = 1;
		ArquivoGedVo arquivoGedRetornoVo = null;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT rowid, cd_empresa, cd_empresa_principal, cd_ged, cd_arquivo_ged, nm_arquivos_ged, nm_diretorio, dt_upload ").
			append(" , cd_tipo_documento, ic_assinado_digitalmente, hr_upload, cd_vault_id ").
			append(" , qtd_assinatura_permitida, qtd_assinatura, cd_funcionario ").
			append(" FROM arquivos_ged ").
			append(" WHERE cd_empresa = ? ").
			append(" AND cd_ged = ? ").
			append(" AND cd_arquivo_ged = ? ");
		
		if (!Utils.isNullOrEmpty(arquivoGedVo.getCodigoEmpresaPrincipal())) {
			qry.append(" and cd_empresa_principal = ? ");
		}
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresa()));
			ps.setLong(i++, Utils.toLong(arquivoGedVo.getCodigoGed()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoArquivoGed()));
			
			if (!Utils.isNullOrEmpty(arquivoGedVo.getCodigoEmpresaPrincipal())) {
				ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresaPrincipal()));
			}
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					arquivoGedRetornoVo = new ArquivoGedVo();
					
					arquivoGedRetornoVo.setRowid(rs.getString("rowid"));
					arquivoGedRetornoVo.setCodigoGed(rs.getString("cd_ged"));
					arquivoGedRetornoVo.setCodigoArquivoGed(rs.getString("cd_arquivo_ged"));
					arquivoGedRetornoVo.setCodigoEmpresa(rs.getString("cd_empresa"));
					arquivoGedRetornoVo.setCodigoEmpresaPrincipal(rs.getString("cd_empresa_principal"));
					arquivoGedRetornoVo.setNomeDoArquivo(rs.getString("nm_arquivos_ged"));
					arquivoGedRetornoVo.setNomeDoDiretorio(rs.getString("nm_diretorio"));
					arquivoGedRetornoVo.setDataUpload(Utils.dataFormatada(rs.getDate("dt_upload")));
					arquivoGedRetornoVo.setHoraUpload(Utils.formataHora(rs.getString("hr_upload")));
					arquivoGedRetornoVo.setCodigoTipoDocumento(rs.getString("cd_tipo_documento"));
					arquivoGedRetornoVo.setAssinadoDigitalmente(rs.getBoolean("ic_assinado_digitalmente"));
					arquivoGedRetornoVo.setVaultId(rs.getString("cd_vault_id"));
					arquivoGedRetornoVo.setQuantidadeAssinaturasPermitidas(rs.getInt("qtd_assinatura_permitida"));
					arquivoGedRetornoVo.setQuantidadeAssinaturas(rs.getInt("qtd_assinatura"));
					arquivoGedRetornoVo.setCodigoFuncionario(rs.getString("cd_funcionario"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return arquivoGedRetornoVo;
	}

	public List<ArquivoGedVo> getArquivosDeUmGeddList(ArquivoGedVo arquivoGedVo) {
		int i = 1;
		List<ArquivoGedVo> lista = new ArrayList<>();
		ArquivoGedVo arquivoGedRetornoVo;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT rowid, cd_empresa, cd_empresa_principal, cd_ged, cd_arquivo_ged,  nm_arquivos_ged, nm_diretorio ").
			append(" , dt_upload, cd_tipo_documento, ic_assinado_digitalmente, hr_upload , cd_vault_id ").
			append(" , qtd_assinatura_permitida, qtd_assinatura ").
			append(" FROM arquivos_ged ").
			append(" WHERE cd_empresa = ? ").
			append(" AND cd_ged = ? ").
			append(" AND cd_empresa_principal = ? ");
		
		if(arquivoGedVo.isBuscaGedPorDataUpload()){
			qry.append(" and dt_upload >= ? ").
				append(" and dt_upload <= ? ");
		}
		
		qry.append(" order by ").append(arquivoGedVo.getOrderBy());
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoGed()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresaPrincipal()));
			
			if(arquivoGedVo.isBuscaGedPorDataUpload()){
				ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(arquivoGedVo.getDataUpload())));
				ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(arquivoGedVo.getDataFinal())));
			}
			
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					arquivoGedRetornoVo = new ArquivoGedVo();
					
					arquivoGedRetornoVo.setRowid(rs.getString("rowid"));
					arquivoGedRetornoVo.setCodigoGed(rs.getString("cd_ged"));
					arquivoGedRetornoVo.setCodigoArquivoGed(rs.getString("cd_arquivo_ged"));
					arquivoGedRetornoVo.setCodigoEmpresa(rs.getString("cd_empresa"));
					arquivoGedRetornoVo.setCodigoEmpresaPrincipal(rs.getString("cd_empresa_principal"));
					arquivoGedRetornoVo.setNomeDoArquivo(rs.getString("nm_arquivos_ged"));
					arquivoGedRetornoVo.setNomeDoDiretorio(rs.getString("nm_diretorio"));
					arquivoGedRetornoVo.setDataUpload(Utils.dataFormatada(rs.getDate("dt_upload")));
					arquivoGedRetornoVo.setHoraUpload(Utils.formataHora(rs.getString("hr_upload")));
					arquivoGedRetornoVo.setCodigoTipoDocumento(rs.getString("cd_tipo_documento"));
					arquivoGedRetornoVo.setAssinadoDigitalmente(rs.getBoolean("ic_assinado_digitalmente"));
					arquivoGedRetornoVo.setVaultId(rs.getString("cd_vault_id"));
					arquivoGedRetornoVo.setQuantidadeAssinaturasPermitidas(rs.getInt("qtd_assinatura_permitida"));
					arquivoGedRetornoVo.setQuantidadeAssinaturas(rs.getInt("qtd_assinatura"));
					arquivoGedRetornoVo.setNomeDoArquivo(Utils.replaceAllAccent(arquivoGedRetornoVo.getNomeDoArquivo()));
					
					lista.add(arquivoGedRetornoVo);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return lista;
	}

	public void updateArquivoGed(ArquivoGedVo arquivoGedVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append("  UPDATE arquivos_ged SET ").
	 		append("  nm_arquivos_ged = ?, ").
	 		append("  dt_upload = ?, ").
	 		append("  hr_upload = ?, ").
	 		append("  ic_criado_socnet = ? ").
	 		append("  ,cd_vault_id = ? ").
	 		append("  , qtd_assinatura_permitida = ? ").
	 		append("  , qtd_assinatura = ? ").
	 		append("  where cd_empresa_principal = ? ").
	 		append("  and   cd_empresa = ? ").
	 		append("  and   cd_ged = ? ").
	 		append("  and   cd_arquivo_ged = ? ");

		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setString(i++, arquivoGedVo.getNomeDoArquivo());
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(arquivoGedVo.getDataUpload())));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getHoraUpload()));
			ps.setBoolean(i++, arquivoGedVo.isAcessoSocNet());
			ps.setString(i++, arquivoGedVo.getVaultId());
			
			if(arquivoGedVo.isPossuiQuantidadeAsssinaturaAutomaticaSesi()) {
				ps.setInt(i++, arquivoGedVo.getQuantidadeAssinaturasPermitidas());
			} else {
				ps.setInt(i++, Utils.toInt("1"));
			}
			
			ps.setInt(i++, Utils.toInt("0"));
			
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoGed()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoArquivoGed()));
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public long getSequenceArquivoGed() {
		long sequenceArquivoGed = 0;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" select NextValApoioGed(?) ");
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {

			ps.setString(i++, "_seq_apoio_ged");
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					sequenceArquivoGed = rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return sequenceArquivoGed;
	}

	public long insertArquivoGed(ArquivoGedVo arquivoGedVo, ParametrosVo parametrosVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append(" insert into arquivos_ged ").
	 		append(" (cd_empresa_principal, ").
	 		append("  cd_empresa, ").
	 		append("  cd_ged, ").
	 		append("  cd_arquivo_ged, ").
	 		append("  nm_arquivos_ged, ").
	 		append("  nm_diretorio, ").
	 		append("  dt_upload, ").
	 		append("  ic_criado_socnet ,").
	 		append("  cd_tipo_documento, "). 
	 		append("  ic_assinado_digitalmente,").
	 		append("  cd_funcionario,").
	 		append("  cd_vault_id,").
	 		append("  hr_upload ").
	 		append(" , qtd_assinatura_permitida ").
	 		append(" , qtd_assinatura ").
	 		append(" , cd_flag_antivirus ").
	 		append(" ) ").
	 		append(getValues(qry));

		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString(), Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoEmpresa()));
			ps.setLong(i++, Utils.toLong(arquivoGedVo.getCodigoGed()));
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoArquivoGed()));
			ps.setString(i++, arquivoGedVo.getNomeDoArquivo());
			ps.setString(i++, arquivoGedVo.getNomeDoDiretorio());
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(parametrosVo.getDataDoDia())));
			ps.setBoolean(i++, arquivoGedVo.isAcessoSocNet());
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoTipoDocumento()));
			ps.setBoolean(i++, arquivoGedVo.isDownloadPortal());
			ps.setInt(i++, Utils.toInt(arquivoGedVo.getCodigoFuncionario()));
			ps.setString(i++, arquivoGedVo.getVaultId());
			ps.setInt(i++, parametrosVo.getHoraAtual());
			
			if(!arquivoGedVo.isPossuiQuantidadeAsssinaturaAutomaticaSesi()) {
				ps.setInt(i++, Utils.toInt("1"));
			} else {
				ps.setInt(i++, arquivoGedVo.getQuantidadeAssinaturasPermitidas());
			}
			
			ps.setInt(i++, arquivoGedVo.isDownloadPortal() ? Utils.toInt("1") : Utils.toInt("0"));
			ps.setInt(i++, 0);
			
			ps.executeUpdate();

			try(ResultSet rs = ps.getGeneratedKeys()){
				if(rs.next()) {
					return rs.getLong(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void updateDataUploadGed(GedVo gedVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append("  UPDATE ged SET ").
	 		append("  dt_upload = ? ").
	 		append("  WHERE cd_empresa = ? ").
	 		append("  AND cd_ged = ? ");

		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setDate(i++, Utils.getSqlDateFromUtilDate2(Utils.toDate(gedVo.getDataUpload())));
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			ps.setLong(i++, Utils.toLong(gedVo.getCodigo()));
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public GedVo getGedPeloCodigo(GedVo gedVo) {
		GedVo gedVoTemp = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT g.rowid, g.cd_empresa, g.cd_ged, g.nm_ged, g.cd_tipo_ged ").
			append(" FROM ged g ").
			append(" JOIN emp e ON g.cd_empresa = e.cod ").
			append(" JOIN tipo_ged tg ON e.cd_empresa_principal = tg.cd_empresa and g.cd_tipo_ged = tg.cd_tipo_ged ").
			append(" WHERE g.cd_empresa = ? ").
			append(" AND g.cd_ged = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			ps.setString(i++, gedVo.getCodigo());
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					gedVoTemp = new GedVo();

					gedVoTemp.setCodigo(rs.getString("cd_ged"));
					gedVoTemp.setNome(rs.getString("nm_ged"));
					gedVoTemp.setCodigoTipoGed(rs.getString("cd_tipo_ged"));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return gedVoTemp;
	}

	public List<LogSftpVo> getLogsDaCredencialDeUmGedPorEmpresa(GedVo gedVo, boolean procuraNaPrincipal) {
		int i = 1;
		List<LogSftpVo> lista = new ArrayList<>();
		LogSftpVo logSftpVo;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT cs.cd_empresa empresa, cs.ds_servidor servidor, csg.cd_tipo_socged tipoged, cs.cd_credencial credencial ").
			append(" FROM credencial_sftp cs ").
			append(" JOIN credencial_sftpxtipo_socged csg ON csg.cd_empresa_principal = cs.cd_empresa_principal AND csg.cd_empresa = cs.cd_empresa AND csg.cd_credencial = cs.cd_credencial AND csg.cd_tipo_socged = ? ");
		
		if( procuraNaPrincipal ){
			qry.append(" LEFT JOIN (SELECT ee.cd_empresa, ee.cd_empresa_credencial, ee.cd_credencial")
				.append(" FROM empresas_excecao_credencial ee  ")
				.append(" WHERE ee.cd_empresa_principal = ?) ")
				.append(" cc ON ( cc.cd_credencial = cs.cd_credencial AND cc.cd_empresa_credencial = cs.cd_empresa) ");
		}
		
		qry.append(" WHERE cs.cd_empresa_principal = ? ");
		
		if(!procuraNaPrincipal) {
			qry.append(" AND cs.cd_empresa = ? ");
		} else {
			qry.append(" AND cs.cd_empresa = ? AND cs.ic_todas_empresas = 1 ");
			qry.append(" AND cc.cd_credencial IS NULL ");
		}
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoTipoGed()));
			
			if( procuraNaPrincipal ){
				ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			}
			
			ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			
			if(!procuraNaPrincipal){
				ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresa()));
			} else{
				ps.setInt(i++, Utils.toInt(gedVo.getCodigoEmpresaPrincipal()));
			}
			
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					logSftpVo = new LogSftpVo();
					
					logSftpVo.setCodigoEmpresa(rs.getString("empresa"));
					logSftpVo.setCodigoCredencial(rs.getString("credencial"));
					logSftpVo.setCodigoTipoGed(rs.getString("tipoged"));
					logSftpVo.setNomeServidor(rs.getString("servidor"));
					
					lista.add(logSftpVo);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return lista;
	}

	public void insereLogEnvioSFTP(LogSftpVo logSftpVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append(" INSERT INTO log_envio_sftp ").
	 		append(" (cd_empresa_principal, cd_empresa, cd_empresa_ged, cd_socged, cd_arquivo_socged, ds_nome_arquivo, cd_tipo_socged, nm_servidor, cd_credencial) ");
 		qry.append(getValues(qry));
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(logSftpVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(logSftpVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(logSftpVo.getCodigoEmpresaGed()));
			ps.setLong(i++, Utils.toLong(logSftpVo.getCodigoSocGed()));
			ps.setLong(i++, Utils.toLong(logSftpVo.getCodigoArquivoSocGed()));
			ps.setString(i++, logSftpVo.getNomeArquivo());
			ps.setInt(i++, Utils.toInt(logSftpVo.getCodigoTipoGed()));
			ps.setString(i++, logSftpVo.getNomeServidor());
			ps.setInt(i++, Utils.toInt(logSftpVo.getCodigoCredencial()));
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateBiometriaGed(BiometriaVo biometriaVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
		qry.append(" UPDATE fichaxbiometria SET ").
			append(" cd_ged = ?, ").
			append(" cd_arquivo_ged = ? ").
			append(" WHERE cd_sequencial_ficha = ? ").			
			append(" AND tp_documento = ? ").
			append(" AND cd_dedo = ? ");

		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setLong(i++, Utils.toLong(biometriaVo.getCodigoGed()));
			ps.setLong(i++, Utils.toLong(biometriaVo.getCodigoArquivoGed()));
			ps.setInt(i++, Utils.toInt(biometriaVo.getCodigoSequencialFicha()));
			ps.setInt(i++, Utils.toInt(biometriaVo.getTipoProntuario()));
			ps.setInt(i, Utils.toInt(biometriaVo.getCodigoDedo()));
			
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public QRCodeVo getQrCodePorCodigoDocumento(QRCodeVo qrcodeVo) {
		QRCodeVo qrCodeVo2 = null;
		int i = 1;
		StringBuilder qry = new StringBuilder();
		
		qry.append(" SELECT ic_qrcode_link ").
			append(" FROM empxqrcode  ").
			append(" WHERE cd_empresa = ? ").
			append(" AND cd_documento = ? ");
		
		try(Connection connection = databaseReplica.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(qrcodeVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(qrcodeVo.getCodigoDocumento()));
			
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()) {
					qrCodeVo2 = new QRCodeVo();

					qrCodeVo2.setPermitirVerificacaoDadosQrcode(rs.getBoolean("ic_qrcode_link"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return qrCodeVo2;
	}

	public void insertVerificacaoDocumento(VerificacaoDocumentosVo verificacaoDocumentosVo) {
		StringBuilder qry = new StringBuilder();
		int i = 1;
		
			qry.append(" INSERT INTO verificacao_documentos ( ").
				append(" cd_empresa_principal ").
				append(" ,cd_empresa ").
				append(" ,cd_ged ").
				append(" ,cd_arquivo_ged ").
				append(" ,ds_token ").
				append(" ) ").
				append(getValues(qry));
		
		try(Connection connection = database.connection(); PreparedStatement ps = connection.prepareStatement(qry.toString())) {
			ps.setInt(i++, Utils.toInt(verificacaoDocumentosVo.getCodigoEmpresaPrincipal()));
			ps.setInt(i++, Utils.toInt(verificacaoDocumentosVo.getCodigoEmpresa()));
			ps.setInt(i++, Utils.toInt(verificacaoDocumentosVo.getCodigoGed()));
			ps.setInt(i++, Utils.toInt(verificacaoDocumentosVo.getCodigoArquivoGed()));
			ps.setString(i++, verificacaoDocumentosVo.getDescricaoToken());
			
			ps.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
