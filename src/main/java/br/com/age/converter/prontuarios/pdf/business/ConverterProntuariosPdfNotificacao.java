package br.com.age.converter.prontuarios.pdf.business;

import java.net.URI;

import br.com.age.converter.prontuarios.pdf.dao.DynamoDBConnection;
import br.com.age.converter.prontuarios.pdf.exceptions.FalhaBuscarConexaoWebSocketException;
import br.com.age.converter.prontuarios.pdf.exceptions.FalhaNotificacaoWebSocketException;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.ParametrosVo;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

public class ConverterProntuariosPdfNotificacao {
	private ParametrosVo parametrosVo;
	
	public ConverterProntuariosPdfNotificacao(ParametrosVo parametrosVo) {
		this.parametrosVo = parametrosVo;
	}

	public void notificarConexaoUsuario(String jsonResponse) throws FalhaNotificacaoWebSocketException {
		try {
			DynamoDBConnection dynamoDBConnection = new DynamoDBConnection(System.getenv("TABELA"));
			String connectionId = dynamoDBConnection.buscarConexaoPorIdConversao(parametrosVo.getIdConexaoSoc());
			
			if(Utils.isNullOrEmpty(connectionId)) {
				throw new FalhaBuscarConexaoWebSocketException("Websocket - ConnectionId não encontrado");
			}
			
			String endpointUrl = System.getenv("URL_API_GATEWAY");
            URI endpointUri = new URI(endpointUrl);

            ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
                    .endpointOverride(endpointUri)
                    .region(Region.of(System.getenv("REGION")))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            SdkBytes dados = SdkBytes.fromByteArray(jsonResponse.getBytes());

            PostToConnectionRequest request = PostToConnectionRequest.builder()
            		.connectionId(connectionId)
            		.data(dados)
            		.build();

           client.postToConnection(request);
		} catch (Exception e) {
        	e.printStackTrace();
        	throw new FalhaNotificacaoWebSocketException(e.getMessage());
        }
	}
	
}
