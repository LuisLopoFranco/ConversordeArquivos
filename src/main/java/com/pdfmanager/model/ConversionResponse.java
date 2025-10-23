package com.pdfmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para resposta de conversão
 *
 * Contém informações sobre o resultado do processamento
 * e links para download dos arquivos convertidos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    /**
     * ID único da conversão
     */
    private String conversionId;

    /**
     * Status do processamento
     */
    private ProcessingStatus status;

    /**
     * Mensagem descritiva do resultado
     */
    private String message;

    /**
     * Nome do arquivo original
     */
    private String originalFileName;

    /**
     * Nome do arquivo convertido
     */
    private String convertedFileName;

    /**
     * URL para download do arquivo convertido
     */
    private String downloadUrl;

    /**
     * URLs para download (quando há múltiplos arquivos)
     */
    private List<FileDownload> downloadUrls;

    /**
     * Tamanho do arquivo original em bytes
     */
    private Long originalFileSize;

    /**
     * Tamanho do arquivo convertido em bytes
     */
    private Long convertedFileSize;

    /**
     * Percentual de redução/aumento do tamanho
     */
    private Double sizeChangePercentage;

    /**
     * Tempo de processamento em milissegundos
     */
    private Long processingTime;

    /**
     * Data/hora de criação
     */
    private LocalDateTime createdAt;

    /**
     * Data/hora de expiração do arquivo
     */
    private LocalDateTime expiresAt;

    /**
     * Detalhes de erro (se houver)
     */
    private ErrorDetails errorDetails;

    /**
     * Status possíveis do processamento
     */
    public enum ProcessingStatus {
        PENDING("Aguardando processamento"),
        PROCESSING("Processando"),
        COMPLETED("Concluído com sucesso"),
        FAILED("Falha no processamento"),
        CANCELLED("Cancelado pelo usuário");

        private final String description;

        ProcessingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Informações de download de arquivo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDownload {
        private String fileName;
        private String downloadUrl;
        private Long fileSize;
        private String mimeType;
        private Integer pageNumber; // Para arquivos divididos
    }

    /**
     * Detalhes de erro
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String errorCode;
        private String errorMessage;
        private String technicalDetails;
        private LocalDateTime timestamp;
    }
}
