package com.conversor.dto;

import com.conversor.model.FileFormat;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) para requisição de conversão de arquivo.
 *
 * Usado para receber dados do cliente sobre qual conversão realizar.
 */
public class ConversionRequest {

    @NotNull(message = "O formato de destino é obrigatório")
    private String targetFormat;

    private String sourceFormat;

    /**
     * Construtor padrão.
     */
    public ConversionRequest() {
    }

    /**
     * Construtor com parâmetros.
     *
     * @param sourceFormat Formato de origem
     * @param targetFormat Formato de destino
     */
    public ConversionRequest(String sourceFormat, String targetFormat) {
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    // Getters e Setters

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    /**
     * Converte a string do formato de destino para o enum FileFormat.
     *
     * @return FileFormat correspondente
     */
    public FileFormat getTargetFormatEnum() {
        try {
            return FileFormat.valueOf(targetFormat.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converte a string do formato de origem para o enum FileFormat.
     *
     * @return FileFormat correspondente
     */
    public FileFormat getSourceFormatEnum() {
        if (sourceFormat == null) {
            return null;
        }
        try {
            return FileFormat.valueOf(sourceFormat.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
