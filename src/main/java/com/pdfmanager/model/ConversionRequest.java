package com.pdfmanager.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Modelo para requisição de conversão de arquivos
 *
 * Encapsula todos os dados necessários para processar
 * uma solicitação de conversão ou manipulação de arquivo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    /**
     * Tipo de conversão solicitada
     */
    @NotNull(message = "O tipo de conversão é obrigatório")
    private ConversionType conversionType;

    /**
     * Arquivo único para upload
     * Usado para conversões simples
     */
    private MultipartFile file;
    /**
     * Múltiplos arquivos para upload
     * Usado para operações como merge
     */
    private List<MultipartFile> files;
    /**
     * Opções adicionais de configuração
     * Ex: qualidade de compressão, páginas específicas, etc.
     */
    private Map<String, Object> options;

    /**
     * Email do usuário para notificação (opcional)
     */
    private String userEmail;

    /**
     * ID único da sessão do usuário
     */
    private String sessionId;

    /**
     * Configurações específicas para diferentes tipos de conversão
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionOptions {

        // Opções para divisão de PDF
        private Integer splitByPages;
        private List<Integer> pageRanges;

        // Opções para compressão
        private CompressionLevel compressionLevel;

        // Opções para imagens
        private Integer dpi;
        private ImageFormat imageFormat;

        // Opções para rotação
        private Integer rotationDegrees;

        // Opções para marca d'água
        private String watermarkText;
        private WatermarkPosition watermarkPosition;
        private Float watermarkOpacity;

        // Opções para proteção
        private String password;
        private String ownerPassword;
        private List<String> permissions;
    }

    /**
     * Níveis de compressão disponíveis
     */
    public enum CompressionLevel {
        LOW("Baixa compressão - Máxima qualidade"),
        MEDIUM("Compressão média - Qualidade balanceada"),
        HIGH("Alta compressão - Menor tamanho de arquivo");

        private final String description;

        CompressionLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Formatos de imagem suportados
     */
    public enum ImageFormat {
        JPG, PNG, TIFF, BMP
    }

    /**
     * Posições para marca d'água
     */
    public enum WatermarkPosition {
        CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, DIAGONAL
    }
}
