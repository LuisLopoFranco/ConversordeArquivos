package com.conversor.dto;

import com.conversor.model.ConvertedFile;

/**
 * DTO (Data Transfer Object) para resposta de conversão de arquivo.
 *
 * Usado para enviar informações sobre o resultado da conversão ao cliente.
 */
public class ConversionResponse {

    private boolean success;
    private String message;
    private String fileId;
    private String originalFilename;
    private String convertedFilename;
    private String downloadUrl;
    private long originalSize;
    private long convertedSize;
    private String conversionType;

    /**
     * Construtor padrão.
     */
    public ConversionResponse() {
    }

    /**
     * Construtor para resposta de sucesso.
     *
     * @param convertedFile Arquivo convertido
     */
    public ConversionResponse(ConvertedFile convertedFile) {
        this.success = true;
        this.message = "Conversão realizada com sucesso!";
        this.fileId = convertedFile.getId();
        this.originalFilename = convertedFile.getOriginalFilename();
        this.convertedFilename = convertedFile.getConvertedFilename();
        this.downloadUrl = "/api/files/download/" + convertedFile.getId();
        this.originalSize = convertedFile.getOriginalSize();
        this.convertedSize = convertedFile.getConvertedSize();
        this.conversionType = convertedFile.getConversionType().getDescription();
    }

    /**
     * Construtor para resposta de erro.
     *
     * @param success Indica se foi sucesso
     * @param message Mensagem de erro
     */
    public ConversionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Cria uma resposta de erro.
     *
     * @param message Mensagem de erro
     * @return ConversionResponse com erro
     */
    public static ConversionResponse error(String message) {
        return new ConversionResponse(false, message);
    }

    // Getters e Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
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

    public String getConversionType() {
        return conversionType;
    }

    public void setConversionType(String conversionType) {
        this.conversionType = conversionType;
    }
}
