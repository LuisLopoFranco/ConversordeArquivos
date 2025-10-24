package com.conversor.model;

import java.time.LocalDateTime;

/**
 * Classe que representa um arquivo convertido no sistema.
 *
 * Armazena informações sobre o arquivo original, o arquivo convertido
 * e metadados da conversão.
 */
public class ConvertedFile {

    private String id;
    private String originalFilename;
    private String convertedFilename;
    private String originalFilePath;
    private String convertedFilePath;
    private FileFormat sourceFormat;
    private FileFormat targetFormat;
    private ConversionType conversionType;
    private long originalSize;
    private long convertedSize;
    private LocalDateTime conversionDate;
    private ConversionStatus status;
    private String errorMessage;

    /**
     * Construtor padrão.
     */
    public ConvertedFile() {
        this.id = java.util.UUID.randomUUID().toString();
        this.conversionDate = LocalDateTime.now();
        this.status = ConversionStatus.PENDING;
    }

    /**
     * Construtor com parâmetros principais.
     *
     * @param originalFilename Nome do arquivo original
     * @param sourceFormat     Formato de origem
     * @param targetFormat     Formato de destino
     */
    public ConvertedFile(String originalFilename, FileFormat sourceFormat, FileFormat targetFormat) {
        this();
        this.originalFilename = originalFilename;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.conversionType = ConversionType.findByFormats(sourceFormat, targetFormat);
    }

    // Getters e Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getConvertedFilename() {
        return convertedFilename;
    }

    public void setConvertedFilename(String convertedFilename) {
        this.convertedFilename = convertedFilename;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public String getConvertedFilePath() {
        return convertedFilePath;
    }

    public void setConvertedFilePath(String convertedFilePath) {
        this.convertedFilePath = convertedFilePath;
    }

    public FileFormat getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(FileFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public FileFormat getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(FileFormat targetFormat) {
        this.targetFormat = targetFormat;
    }

    public ConversionType getConversionType() {
        return conversionType;
    }

    public void setConversionType(ConversionType conversionType) {
        this.conversionType = conversionType;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public void setOriginalSize(long originalSize) {
        this.originalSize = originalSize;
    }

    public long getConvertedSize() {
        return convertedSize;
    }

    public void setConvertedSize(long convertedSize) {
        this.convertedSize = convertedSize;
    }

    public LocalDateTime getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }

    public ConversionStatus getStatus() {
        return status;
    }

    public void setStatus(ConversionStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Enum que representa o status de uma conversão.
     */
    public enum ConversionStatus {
        PENDING("Pendente"),
        PROCESSING("Processando"),
        COMPLETED("Completo"),
        FAILED("Falhou");

        private final String description;

        ConversionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
