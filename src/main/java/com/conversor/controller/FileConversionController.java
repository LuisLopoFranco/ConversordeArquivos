package com.conversor.controller;

import com.conversor.dto.ConversionRequest;
import com.conversor.dto.ConversionResponse;
import com.conversor.exception.FileConversionException;
import com.conversor.model.ConvertedFile;
import com.conversor.model.FileFormat;
import com.conversor.service.FileConversionService;
import com.conversor.service.FileStorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller REST para operações de conversão de arquivos.
 *
 * Fornece endpoints para upload, conversão e download de arquivos.
 */
@RestController
@RequestMapping("/api/files")
public class FileConversionController {

    private static final Logger logger = LoggerFactory.getLogger(FileConversionController.class);

    private final FileConversionService conversionService;
    private final FileStorageService storageService;

    /**
     * Construtor do controller.
     *
     * @param conversionService Serviço de conversão
     * @param storageService    Serviço de armazenamento
     */
    public FileConversionController(FileConversionService conversionService,
                                    FileStorageService storageService) {
        this.conversionService = conversionService;
        this.storageService = storageService;
    }

    /**
     * Endpoint para upload e conversão de arquivo.
     *
     * @param file              Arquivo a ser convertido
     * @param conversionRequest Dados da conversão (formato de destino)
     * @return ResponseEntity com resultado da conversão
     */
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConversionResponse> convertFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute ConversionRequest conversionRequest) {

        logger.info("Recebida requisição de conversão: {} -> {}",
                file.getOriginalFilename(), conversionRequest.getTargetFormat());

        // Validações básicas
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error("O arquivo não pode estar vazio"));
        }

        try {
            // Determina o formato de destino
            FileFormat targetFormat = conversionRequest.getTargetFormatEnum();
            if (targetFormat == null) {
                return ResponseEntity.badRequest()
                        .body(ConversionResponse.error("Formato de destino inválido"));
            }

            // Realiza a conversão
            ConvertedFile convertedFile = conversionService.convertFile(file, targetFormat);

            // Retorna resposta de sucesso
            ConversionResponse response = new ConversionResponse(convertedFile);
            return ResponseEntity.ok(response);

        } catch (FileConversionException ex) {
            logger.error("Erro na conversão: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(ConversionResponse.error(ex.getMessage()));

        } catch (Exception ex) {
            logger.error("Erro inesperado na conversão", ex);
            return ResponseEntity.internalServerError()
                    .body(ConversionResponse.error("Erro inesperado: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint para download do arquivo convertido.
     *
     * @param fileId ID do arquivo convertido
     * @return ResponseEntity com o arquivo para download
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        logger.info("Recebida requisição de download para arquivo: {}", fileId);

        try {
            // Recupera informações do arquivo convertido
            ConvertedFile convertedFile = conversionService.getConvertedFile(fileId);

            if (convertedFile == null) {
                return ResponseEntity.notFound().build();
            }

            // Carrega o arquivo como Resource
            Resource resource = storageService.loadFileAsResource(
                    convertedFile.getConvertedFilename(), true);

            // Define o tipo de conteúdo
            String contentType = convertedFile.getTargetFormat().getMimeType();

            // Retorna o arquivo com headers apropriados
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + convertedFile.getConvertedFilename() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            logger.error("Erro ao fazer download do arquivo", ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para verificar status de uma conversão.
     *
     * @param fileId ID do arquivo
     * @return ResponseEntity com informações do arquivo
     */
    @GetMapping("/status/{fileId}")
    public ResponseEntity<ConversionResponse> getConversionStatus(@PathVariable String fileId) {
        logger.info("Consultando status da conversão: {}", fileId);

        ConvertedFile convertedFile = conversionService.getConvertedFile(fileId);

        if (convertedFile == null) {
            return ResponseEntity.notFound().build();
        }

        ConversionResponse response = new ConversionResponse(convertedFile);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para listar formatos suportados.
     *
     * @return ResponseEntity com lista de formatos
     */
    @GetMapping("/formats")
    public ResponseEntity<FileFormat[]> getSupportedFormats() {
        return ResponseEntity.ok(FileFormat.values());
    }
}
