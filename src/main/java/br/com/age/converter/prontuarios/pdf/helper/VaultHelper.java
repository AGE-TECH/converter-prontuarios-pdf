package br.com.age.converter.prontuarios.pdf.helper;

import java.util.regex.Pattern;

import com.age.vault.client.Bucket;
import com.age.vault.client.VaultArquivoBuilder;
import com.age.vault.client.VaultClient;
import com.age.vault.client.vo.VaultArquivoVo;

import br.com.age.converter.prontuarios.pdf.enums.ProgramaUploadImpl;
import br.com.age.converter.prontuarios.pdf.exceptions.BusinessException;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.ArquivoGedVo;

public class VaultHelper {
	
	private static final Pattern PATTERN_SERIAL_ID = Pattern.compile("^(\\d*\\w*)-(\\d*\\w*)-(\\d*\\w*)-(\\d*\\w*)-(\\d*\\w*)$");
	
	protected VaultHelper() {}
	
	public static VaultHelper getInstance(){
		return new VaultHelper();
	}
	
	public byte[] getArquivo(String vaultId) {
		try {
			return VaultClient.downloadArquivo(vaultId.trim());
		} catch (com.age.vault.client.BusinessException e) {
        	e.printStackTrace();
		}
		
		return null;
	}
	
	public void deletarArquivo(String vaultId) throws BusinessException {
		if(!isPatternSerialIdValid(vaultId)){
			throw new IllegalArgumentException("Vault ID invalido!");
		}
		
		try {
            VaultClient.deleteArquivo(vaultId.trim());
        } catch (com.age.vault.client.BusinessException e) {
            throw new BusinessException(e);
        }
	}
	
	public static boolean isPatternSerialIdValid(final String vaultId) {
        if (Utils.isNullOrEmpty(vaultId)) {
            return false;
        }
        
		return PATTERN_SERIAL_ID.matcher(vaultId.trim()).matches();
	}

	public String uploadArquivoGed(ArquivoGedVo arquivoGedVo) throws BusinessException {
	    validarCampoUpload(arquivoGedVo == null, "ArquivoGed não pode ser null");
        validarCamposObrigatoriosUpload(arquivoGedVo.getArquivo(), arquivoGedVo.getNomeDoArquivo(), arquivoGedVo.getCodigoEmpresaPrincipal(), arquivoGedVo.getCodigoEmpresa());
        
	    return uploadArquivo(VaultArquivoVo.builderSoc()
			.bucket(Bucket.PERMANENTE)
			.programaUpload(ProgramaUploadImpl.GED)
			.codigoEmpresaPrincipal(arquivoGedVo.getCodigoEmpresaPrincipal())
			.codigoEmpresaCliente(arquivoGedVo.getCodigoEmpresa())
			.nomeArquivo(arquivoGedVo.getNomeDoArquivo())
			.arquivo(arquivoGedVo.getArquivo())
		);
	}
	
	protected String uploadArquivo(VaultArquivoBuilder vaultArquivoBuilder) throws BusinessException {
		try {
			return VaultClient.uploadArquivo(vaultArquivoBuilder);
		} catch (com.age.vault.client.BusinessException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}
	
	protected void validarCamposObrigatoriosUpload(byte [] arquivo, String nomeArquivo, String codigoEmpresaPrincipal, String codigoEmpresa) throws BusinessException {
        validarCampoUpload(arquivo == null || arquivo.length == 0, "Tamanho do arquivo é invalido");
        validarCampoUpload(Utils.isNullOrEmpty(nomeArquivo), "Nome do arquivo é obrigatorio");
        validarCampoUpload(Utils.isNullOrEmpty(codigoEmpresaPrincipal), "Código de empresa principal é obrigatorio");
        validarCampoUpload(Utils.isNullOrEmpty(codigoEmpresa), "Código de empresa é obrigatorio");
	}
	
	private void validarCampoUpload(boolean condicao, String mensagem) throws BusinessException {
	    if(condicao) {
           throw new BusinessException(mensagem);
        }
	}

}
