package com.conversor.model;

/**
 * Enum que representa os tipos de conversão suportados pela aplicação.
 *
 * Define quais conversões são possíveis entre diferentes formatos de arquivo.
 */
public enum ConversionType {
    // Conversões de PDF
    PDF_TO_DOCX("PDF para Word", FileFormat.PDF, FileFormat.DOCX),
    PDF_TO_TXT("PDF para Texto", FileFormat.PDF, FileFormat.TXT),
    PDF_TO_JPG("PDF para JPEG", FileFormat.PDF, FileFormat.JPG),
    PDF_TO_PNG("PDF para PNG", FileFormat.PDF, FileFormat.PNG),

    // Conversões para PDF
    DOCX_TO_PDF("Word para PDF", FileFormat.DOCX, FileFormat.PDF),
    TXT_TO_PDF("Texto para PDF", FileFormat.TXT, FileFormat.PDF),
    JPG_TO_PDF("JPEG para PDF", FileFormat.JPG, FileFormat.PDF),
    PNG_TO_PDF("PNG para PDF", FileFormat.PNG, FileFormat.PDF),

    // Conversões entre imagens
    JPG_TO_PNG("JPEG para PNG", FileFormat.JPG, FileFormat.PNG),
    PNG_TO_JPG("PNG para JPEG", FileFormat.PNG, FileFormat.JPG),

    // Outras conversões
    DOCX_TO_TXT("Word para Texto", FileFormat.DOCX, FileFormat.TXT);

    private final String description;
    private final FileFormat sourceFormat;
    private final FileFormat targetFormat;

    /**
     * Construtor do enum ConversionType.
     *
     * @param description  Descrição amigável da conversão
     * @param sourceFormat Formato de origem
     * @param targetFormat Formato de destino
     */
    ConversionType(String description, FileFormat sourceFormat, FileFormat targetFormat) {
        this.description = description;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    public String getDescription() {
        return description;
    }

    public FileFormat getSourceFormat() {
        return sourceFormat;
    }

    public FileFormat getTargetFormat() {
        return targetFormat;
    }

    /**
     * Encontra o tipo de conversão baseado nos formatos de origem e destino.
     *
     * @param source Formato de origem
     * @param target Formato de destino
     * @return ConversionType correspondente ou null se não encontrado
     */
    public static ConversionType findByFormats(FileFormat source, FileFormat target) {
        for (ConversionType type : ConversionType.values()) {
            if (type.getSourceFormat() == source && type.getTargetFormat() == target) {
                return type;
            }
        }
        return null;
    }

    /**
     * Verifica se a conversão entre os formatos é suportada.
     *
     * @param source Formato de origem
     * @param target Formato de destino
     * @return true se a conversão é suportada, false caso contrário
     */
    public static boolean isSupported(FileFormat source, FileFormat target) {
        return findByFormats(source, target) != null;
    }
}
