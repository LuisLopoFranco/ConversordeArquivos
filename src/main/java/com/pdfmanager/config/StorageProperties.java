package com.pdfmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configurações de armazenamento de arquivos
 *
 * Define propriedades relacionadas ao upload e armazenamento de arquivos,
 * incluindo diretórios, tamanhos máximos e tipos permitidos.
 */
@Configuration
@ConfigurationProperties
@Data

public class StorageProperties {
    /**
     * Diretório base para armazenamento de arquivos temporários
     */
    private String location = "upload-dir";
    /**
     * Diretório para arquivos processados
     */
    private String processedLocation = "processed-dir";
    /**
     * Tamanho máximo do arquivo em MB
     */
    private long maxFileSize = 50;
    /**
     * Tempo de retenção dos arquivos em minutos
     */
    private String[] allowedFileTypes = {
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };




}
