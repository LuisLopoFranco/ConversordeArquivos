package com.conversor.model;

/**
 * Enum que representa os formatos de arquivo suportados pela aplicação.
 *
 * Cada formato possui uma descrição amigável e as extensões de arquivo associadas.
 */
public enum FileFormat {
    PDF("PDF", "application/pdf", ".pdf"),
    DOCX("Word Document", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    DOC("Word Document (Legacy)", "application/msword", ".doc"),
    XLSX("Excel Spreadsheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    XLS("Excel Spreadsheet (Legacy)", "application/vnd.ms-excel", ".xls"),
    PPTX("PowerPoint Presentation", "application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"),
    TXT("Text File", "text/plain", ".txt"),
    JPG("JPEG Image", "image/jpeg", ".jpg"),
    PNG("PNG Image", "image/png", ".png");

    private final String description;
    private final String mimeType;
    private final String extension;

    /**
     * Construtor do enum FileFormat.
     *
     * @param description Descrição amigável do formato
     * @param mimeType    Tipo MIME do formato
     * @param extension   Extensão do arquivo (incluindo o ponto)
     */
    FileFormat(String description, String mimeType, String extension) {
        this.description = description;
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Obtém o formato a partir da extensão do arquivo.
     *
     * @param filename Nome do arquivo
     * @return FileFormat correspondente ou null se não encontrado
     */
    public static FileFormat fromFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        for (FileFormat format : FileFormat.values()) {
            if (format.getExtension().equalsIgnoreCase(extension)) {
                return format;
            }
        }

        return null;
    }
}
