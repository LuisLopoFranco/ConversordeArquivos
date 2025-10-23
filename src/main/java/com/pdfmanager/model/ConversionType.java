package com.pdfmanager.model;

/**
 * Enumeração dos tipos de conversão disponíveis na aplicação
 *
 * Define todas as operações possíveis que podem ser realizadas
 * com arquivos PDF e outros formatos suportados.
 */
public enum ConversionType {

    // Conversões de PDF para outros formatos
    PDF_TO_WORD("PDF para Word", "Converte arquivo PDF para documento Word (.docx)"),
    PDF_TO_EXCEL("PDF para Excel", "Converte tabelas de PDF para planilha Excel (.xlsx)"),
    PDF_TO_JPG("PDF para JPG", "Converte páginas de PDF para imagens JPG"),
    PDF_TO_PNG("PDF para PNG", "Converte páginas de PDF para imagens PNG"),
    PDF_TO_TEXT("PDF para Texto", "Extrai texto do PDF para arquivo .txt"),

    // Conversões de outros formatos para PDF
    WORD_TO_PDF("Word para PDF", "Converte documento Word para PDF"),
    EXCEL_TO_PDF("Excel para PDF", "Converte planilha Excel para PDF"),
    IMAGE_TO_PDF("Imagem para PDF", "Converte imagens (JPG, PNG) para PDF"),

    // Manipulação de PDFs
    MERGE_PDF("Mesclar PDFs", "Une múltiplos arquivos PDF em um único arquivo"),
    SPLIT_PDF("Dividir PDF", "Divide um PDF em múltiplos arquivos"),
    COMPRESS_PDF("Comprimir PDF", "Reduz o tamanho do arquivo PDF"),
    ROTATE_PDF("Rotacionar PDF", "Rotaciona páginas do PDF"),
    WATERMARK_PDF("Marca d'água", "Adiciona marca d'água ao PDF"),
    PROTECT_PDF("Proteger PDF", "Adiciona senha de proteção ao PDF"),
    UNLOCK_PDF("Desbloquear PDF", "Remove senha de proteção do PDF"),
    EXTRACT_PAGES("Extrair Páginas", "Extrai páginas específicas do PDF");

    private final String displayName;
    private final String description;

    /**
     * Construtor do enum
     *
     * @param displayName Nome amigável para exibição
     * @param description Descrição da funcionalidade
     */
    ConversionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Obtém o nome de exibição
     *
     * @return Nome amigável da conversão
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtém a descrição da conversão
     *
     * @return Descrição detalhada da funcionalidade
     */
    public String getDescription() {
        return description;
    }
}