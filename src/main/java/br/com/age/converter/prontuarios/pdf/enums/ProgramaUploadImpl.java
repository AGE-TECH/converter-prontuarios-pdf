package br.com.age.converter.prontuarios.pdf.enums;

import com.age.vault.client.ProgramaUpload;

public enum ProgramaUploadImpl implements ProgramaUpload{

	GED						("611", "GED"),
	PERSONALIZACAO			("209", "Personalização"),
	FUNCIONARIO				("511", "Foto Funcionario"),
	BACKUP_POR_EMPRESA	   	("677", "Backup por Empresa"),
	PEDIDO_PROCESSAMENTO	("271", "Pedido de Processamento"),
	UPLOAD_FOTO				("271", "Funcionario"),
	CATALOGO_DOCUMENTOS		("1403", "Catálogo de Documentos")
	;

	private String nomePrograma;
	private String codigoPrograma;
	
	private ProgramaUploadImpl(String codigoPrograma, String nomePrograma){
		this.codigoPrograma = codigoPrograma;
		this.nomePrograma = nomePrograma;
	}


	public String getCodigoPrograma() {
		return codigoPrograma;
	}
	
	public String getNomePrograma() {
		return nomePrograma;
	}
}
