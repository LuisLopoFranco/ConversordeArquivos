package com.pdfmanager.controller;

import com.pdfmanager.model.ConversionRequest;
import com.pdfmanager.model.ConversionResponse;
import com.pdfmanager.model.ConversionType;
import com.pdfmanager.service.ConversionService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para operações de conversão de PDF
 *
 * Fornece endpoints para upload, conversão e download de arquivos,
 * seguindo os padrões RESTful e as melhores práticas de API.
 */
@RestController
@RequestMapping("/api/v1/pdf")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class PdfConversionController {

    private final ConversionService conversionService;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Endpoint para listar tipos de conversão disponíveis
     *
     * @return Lista de tipos de conversão suportados
     */
    @GetMapping("/conversion-types")
    public ResponseEntity<List<ConversionTypeDTO>> getConversionTypes() {
        List<ConversionTypeDTO> types = Arrays.stream(ConversionType.values())
                .map(type -> ConversionTypeDTO.builder()
                        .type(type.name())
                        .displayName(type.getDisplayName())
                        .description(type.getDescription())
                        .build())
                .toList();

        return ResponseEntity.ok(types);
    }

    /**
     * Endpoint principal para conversão de arquivo único
     *
     * @param file Arquivo para conversão
     * @param conversionType Tipo de conversão desejada
     * @param options Opções adicionais (opcional)
     * @return Resposta com informações do arquivo convertido
     */
    @PostMapping("/convert")
    public ResponseEntity<ConversionResponse> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversionType") String conversionType,
            @RequestParam(value = "options", required = false) Map<String, Object> options) {

        log.info("Recebendo requisição de conversão: {} - {}",
                file.getOriginalFilename(), conversionType);

        try {
            // Valida o arquivo
            if (!conversionService.validateFile(file)) {
                return ResponseEntity.badRequest()
                        .body(buildErrorResponse("Arquivo inválido ou formato não suportado"));
            }

            // Cria requisição de conversão
            ConversionRequest request = ConversionRequest.builder()
                    .file(file)
                    .conversionType(ConversionType.valueOf(conversionType))
                    .options(options)
                    .sessionId(UUID.randomUUID().toString())
                    .build();

            // Executa conversão
            ConversionResponse response = conversionService.convert(request);

            log.info("Conversão concluída com sucesso: {}", response.getConversionId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Tipo de conversão inválido: {}", conversionType);
            return ResponseEntity.badRequest()
                    .body(buildErrorResponse("Tipo de conversão inválido"));

        } catch (IOException e) {
            log.error("Erro ao processar arquivo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Erro ao processar arquivo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para conversão de múltiplos arquivos (merge, batch)
     *
     * @param files Lista de arquivos
     * @param conversionType Tipo de operação
     * @param options Opções adicionais
     * @return Resposta com resultado da operação
     */
    @PostMapping("/convert-multiple")
    public ResponseEntity<ConversionResponse> convertMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("conversionType") String conversionType,
            @RequestParam(value = "options", required = false) Map<String, Object> options) {

        log.info("Recebendo {} arquivos para operação: {}", files.size(), conversionType);

        try {
            // Valida todos os arquivos
            for (MultipartFile file : files) {
                if (!conversionService.validateFile(file)) {
                    return ResponseEntity.badRequest()
                            .body(buildErrorResponse("Arquivo inválido: " + file.getOriginalFilename()));
                }
            }

            // Cria requisição
            ConversionRequest request = ConversionRequest.builder()
                    .files(files)
                    .conversionType(ConversionType.valueOf(conversionType))
                    .options(options)
                    .sessionId(UUID.randomUUID().toString())
                    .build();

            // Executa operação
            ConversionResponse response = conversionService.convert(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao processar múltiplos arquivos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Erro ao processar arquivos: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para download do arquivo convertido
     *
     * @param fileName Nome do arquivo para download
     * @param response Resposta HTTP
     * @return Arquivo para download
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            HttpServletResponse response) {

        try {
            Path filePath = Paths.get(TEMP_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Detecta tipo MIME
            String contentType = detectContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Erro ao localizar arquivo: {}", fileName, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para verificar status da conversão (para operações assíncronas)
     *
     * @param conversionId ID da conversão
     * @return Status atual da conversão
     */
    @GetMapping("/status/{conversionId}")
    public ResponseEntity<ConversionStatusDTO> getConversionStatus(
            @PathVariable String conversionId) {

        // Implementação simplificada - em produção, consultar banco/cache
        ConversionStatusDTO status = ConversionStatusDTO.builder()
                .conversionId(conversionId)
                .status("COMPLETED")
                .progress(100)
                .message("Conversão concluída")
                .build();

        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint para estimar tempo de processamento
     *
     * @param fileSize Tamanho do arquivo em bytes
     * @param conversionType Tipo de conversão
     * @return Estimativa em segundos
     */
    @GetMapping("/estimate-time")
    public ResponseEntity<EstimateDTO> estimateProcessingTime(
            @RequestParam long fileSize,
            @RequestParam String conversionType) {

        int estimatedSeconds = conversionService.estimateProcessingTime(fileSize);

        EstimateDTO estimate = EstimateDTO.builder()
                .estimatedSeconds(estimatedSeconds)
                .formattedTime(formatTime(estimatedSeconds))
                .build();

        return ResponseEntity.ok(estimate);
    }

    /**
     * Endpoint de health check
     *
     * @return Status da aplicação
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PDF Conversion Service",
                "version", "1.0.0"
        ));
    }

    /**
     * Constrói resposta de erro padrão
     */
    private ConversionResponse buildErrorResponse(String message) {
        return ConversionResponse.builder()
                .conversionId(UUID.randomUUID().toString())
                .status(ConversionResponse.ProcessingStatus.FAILED)
                .message(message)
                .build();
    }

    /**
     * Detecta tipo MIME do arquivo
     */
    private String detectContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }

    /**
     * Formata tempo em formato legível
     */
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " segundos";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d min %d seg", minutes, remainingSeconds);
        }
    }

    /**
     * DTO para tipos de conversão
     */
    @lombok.Data
    @lombok.Builder
    static class ConversionTypeDTO {
        private String type;
        private String displayName;
        private String description;
    }

    /**
     * DTO para status de conversão
     */
    @lombok.Data
    @lombok.Builder
    static class ConversionStatusDTO {
        private String conversionId;
        private String status;
        private int progress;
        private String message;
    }

    /**
     * DTO para estimativa de tempo
     */
    @lombok.Data
    @lombok.Builder
    static class EstimateDTO {
        private int estimatedSeconds;
        private String formattedTime;
    }
}