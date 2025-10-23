package com.pdfmanager.service;

import com.pdfmanager.model.ConversionRequest;
import com.pdfmanager.model.ConversionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface base para todos os conversores
 *
 * Define o contrato que todos os serviços de conversão
 * devem implementar, garantindo consistência na aplicação.
 */

public interface ConversionService {

    /**
     * Verifica se o serviço suporta o tipo de conversão solicitado
     *
     * @param request Requisição de conversão
     * @return true se o serviço pode processar a conversão
     */
    boolean supports(ConversionRequest request);

    /**
     * Executa a conversão do arquivo
     *
     * @param request Requisição contendo arquivo e opções
     * @return Resposta com resultado da conversão
     * @throws IOException Erro durante o processamento
     */
    ConversionResponse convert(ConversionRequest request) throws IOException;

    /**
     * Valida o arquivo antes da conversão
     *
     * @param file Arquivo a ser validado
     * @return true se o arquivo é válido
     */
    boolean validateFile(MultipartFile file);

    /**
     * Obtém a extensão esperada do arquivo convertido
     *
     * @param request Requisição de conversão
     * @return Extensão do arquivo de saída
     */
    String getOutputExtension(ConversionRequest request);

    /**
     * Estima o tempo de processamento em segundos
     *
     * @param fileSize Tamanho do arquivo em bytes
     * @return Tempo estimado em segundos
     */
    int estimateProcessingTime(long fileSize);

    /**
     * Limpa arquivos temporários após o processamento
     *
     * @param files Lista de arquivos temporários
     */
    void cleanupTempFiles(List<File> files);

}
