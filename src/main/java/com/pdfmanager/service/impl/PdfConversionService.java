package com.pdfmanager.service.impl;

import com.pdfmanager.model.ConversionRequest;
import com.pdfmanager.model.ConversionRequest.ConversionOptions;
import com.pdfmanager.model.ConversionResponse;
import com.pdfmanager.model.ConversionType;
import com.pdfmanager.service.ConversionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;  // IMPORTANTE: Usar Loader ao invés de PDDocument.load()
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviço principal de conversão e manipulação de PDFs
 *
 * Implementa todas as funcionalidades de conversão usando
 * Apache PDFBox como biblioteca principal.
 *
 * CORREÇÃO: Atualizado para usar Loader.loadPDF() ao invés de PDDocument.load()
 * que é o método correto para PDFBox 3.x
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfConversionService implements ConversionService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int DEFAULT_DPI = 300;

    /**
     * Verifica se o serviço suporta o tipo de conversão
     */
    @Override
    public boolean supports(ConversionRequest request) {
        return request.getConversionType() != null;
    }

    /**
     * Executa a conversão baseada no tipo solicitado
     */
    @Override
    public ConversionResponse convert(ConversionRequest request) throws IOException {
        log.info("Iniciando conversão do tipo: {}", request.getConversionType());

        long startTime = System.currentTimeMillis();
        ConversionResponse response;

        try {
            switch (request.getConversionType()) {
                case PDF_TO_WORD:
                    response = convertPdfToWord(request);
                    break;
                case PDF_TO_JPG:
                case PDF_TO_PNG:
                    response = convertPdfToImage(request);
                    break;
                case PDF_TO_TEXT:
                    response = extractTextFromPdf(request);
                    break;
                case MERGE_PDF:
                    response = mergePdfFiles(request);
                    break;
                case SPLIT_PDF:
                    response = splitPdfFile(request);
                    break;
                case COMPRESS_PDF:
                    response = compressPdf(request);
                    break;
                case ROTATE_PDF:
                    response = rotatePdf(request);
                    break;
                case WATERMARK_PDF:
                    response = addWatermark(request);
                    break;
                case PROTECT_PDF:
                    response = protectPdf(request);
                    break;
                case IMAGE_TO_PDF:
                    response = convertImageToPdf(request);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Tipo de conversão não implementado: " + request.getConversionType()
                    );
            }

            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            response.setStatus(ConversionResponse.ProcessingStatus.COMPLETED);

            log.info("Conversão concluída em {} ms", processingTime);
            return response;

        } catch (Exception e) {
            log.error("Erro durante a conversão: ", e);
            return buildErrorResponse(e);
        }
    }

    /**
     * Converte PDF para documento Word
     * CORREÇÃO: Usando Loader.loadPDF() ao invés de PDDocument.load()
     */
    private ConversionResponse convertPdfToWord(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();
        String outputFileName = getOutputFileName(file.getOriginalFilename(), ".docx");
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        // CORREÇÃO: Convertendo InputStream para byte[] e usando Loader
        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes);
             XWPFDocument wordDoc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

            // Extrai texto do PDF
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Cria parágrafos no documento Word
            String[] paragraphs = text.split("\n\n");
            for (String paragraphText : paragraphs) {
                XWPFParagraph paragraph = wordDoc.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(paragraphText);
                run.setFontSize(11);
                run.setFontFamily("Arial");
            }

            wordDoc.write(fos);

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(file.getSize())
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message("PDF convertido para Word com sucesso")
                    .build();
        }
    }

    /**
     * Converte PDF para imagens (JPG ou PNG)
     * CORREÇÃO: Usando Loader.loadPDF()
     */
    private ConversionResponse convertPdfToImage(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();
        String format = request.getConversionType() == ConversionType.PDF_TO_JPG ? "jpg" : "png";
        List<ConversionResponse.FileDownload> downloadUrls = new ArrayList<>();
        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int numPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < numPages; pageIndex++ ){
                //Renderiza cada página como imagem
                BufferedImage image = renderer.renderImageWithDPI(
                        pageIndex,
                        DEFAULT_DPI,
                        ImageType.RGB
                );

                //Salva a imagem
                String outputFileName = String.format(
                        "%s_page_%d.%s",
                        getBaseFileName(file.getOriginalFilename()),
                        pageIndex + 1,
                        format
                );

                Path outputPath = Paths.get(TEMP_DIR, outputFileName);
                ImageIO.write(image, format, outputPath.toFile());
                downloadUrls.add(ConversionResponse.FileDownload.builder()
                        .fileName(outputFileName)
                        .downloadUrl("/download/" + outputFileName)
                        .fileSize(Files.size(outputPath))
                        .mimeType("image/" + format)
                        .pageNumber(pageIndex + 1)
                        .build());
            }
            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .downloadUrls(downloadUrls) // CORREÇÃO APLICADA
                    // .totalPages(numPages) // REMOVIDO (campo inexistente em ConversionResponse)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("PDF convertido em %d imagens", numPages))
                    .build();
        }
    }

    /**
     * Extrai texto do PDF
     * CORREÇÃO: Usando Loader.loadPDF()
     */
    private ConversionResponse extractTextFromPdf(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();
        String outputFileName = getOutputFileName(file.getOriginalFilename(), ".txt");
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();

            // CORREÇÃO: Acessando o Map diretamente
            Map<String, Object> options = request.getOptions();
            if (options != null) {
                // É mais seguro verificar o tipo antes do cast
                Object startPageObj = options.get("startPage");
                Object endPageObj = options.get("endPage");

                if (startPageObj instanceof Integer) {
                    stripper.setStartPage((Integer) startPageObj);
                }
                if (endPageObj instanceof Integer) {
                    stripper.setEndPage((Integer) endPageObj);
                }
            }

            String text = stripper.getText(document);
            Files.write(outputPath, text.getBytes());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(file.getSize())
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message("Texto extraído com sucesso")
                    // .metadata(...) // REMOVIDO (campo inexistente em ConversionResponse)
                    .build();
        }
    }

    /**
     * Mescla múltiplos PDFs
     * CORREÇÃO: Usando Loader.loadPDF() para cada arquivo
     */
    /**
     * Mescla múltiplos PDFs
     * CORREÇÃO: Usando Loader.loadPDF() para cada arquivo
     * CORREÇÃO: Removido .totalPages()
     */
    private ConversionResponse mergePdfFiles(ConversionRequest request) throws IOException {
        List<MultipartFile> files = request.getFiles();
        String outputFileName = "merged_" + System.currentTimeMillis() + ".pdf";
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        try (PDDocument mergedDoc = new PDDocument()) {
            int totalPages = 0;

            for (MultipartFile file : files) {
                byte[] pdfBytes = file.getBytes();
                try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                    for (PDPage page : doc.getPages()) {
                        mergedDoc.addPage(page);
                        totalPages++;
                    }
                }
            }

            mergedDoc.save(outputPath.toFile());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .convertedFileSize(Files.size(outputPath))
                    // .totalPages(totalPages) // REMOVIDO (campo inexistente em ConversionResponse)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("%d PDFs mesclados com sucesso", files.size()))
                    .build();
        }
    }

    /**
     * Divide arquivo PDF
     * CORREÇÃO: Usando Loader.loadPDF()
     */
    private ConversionResponse splitPdfFile(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();

        // CORREÇÃO: Acessando o Map diretamente
        Map<String, Object> options = request.getOptions();
        int pagesPerFile = 1; // Valor padrão

        if (options != null && options.get("pagesPerFile") instanceof Integer) {
            pagesPerFile = (Integer) options.get("pagesPerFile");
        }

        List<ConversionResponse.FileDownload> downloadUrls = new ArrayList<>();
        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int totalPages = document.getNumberOfPages();
            int fileCount = 0;

            for (int i = 0; i < totalPages; i += pagesPerFile) {
                fileCount++;

                try (PDDocument splitDoc = new PDDocument()) {
                    int endPage = Math.min(i + pagesPerFile, totalPages);

                    for (int j = i; j < endPage; j++) {
                        splitDoc.addPage(document.getPage(j));
                    }

                    String outputFileName = String.format(
                            "%s_part_%d.pdf",
                            getBaseFileName(file.getOriginalFilename()),
                            fileCount
                    );

                    Path outputPath = Paths.get(TEMP_DIR, outputFileName);
                    splitDoc.save(outputPath.toFile());

                    downloadUrls.add(ConversionResponse.FileDownload.builder()
                            .fileName(outputFileName)
                            .downloadUrl("/download/" + outputFileName)
                            .fileSize(Files.size(outputPath))
                            .mimeType("application/pdf")
                            // .pageRange(...) // REMOVIDO (campo inexistente em FileDownload)
                            .build());
                }
            }

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .downloadUrls(downloadUrls) // CORREÇÃO APLICADA
                    // .totalPages(totalPages) // REMOVIDO (campo inexistente em ConversionResponse)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("PDF dividido em %d arquivos", fileCount))
                    .build();
        }
    }
    /**
     * Comprime arquivo PDF
     * CORREÇÃO: Usando Loader.loadPDF()
     * CORREÇÃO: Removido .metadata()
     */
    private ConversionResponse compressPdf(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();
        String outputFileName = "compressed_" + file.getOriginalFilename();
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            // Aqui você pode adicionar lógica de compressão de imagens
            // Por exemplo, reprocessar imagens com qualidade menor

            document.save(outputPath.toFile());

            long originalSize = file.getSize();
            long compressedSize = Files.size(outputPath);
            double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(originalSize)
                    .convertedFileSize(compressedSize)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("PDF comprimido (%.1f%% de redução)", compressionRatio))
                    // .metadata(...) // REMOVIDO (campo inexistente em ConversionResponse)
                    .build();
        }
    }

    /**
     * Rotaciona páginas do PDF
     * CORREÇÃO: Usando Loader.loadPDF()
     * CORREÇÃO: Corrigido acesso ao Map de opções
     */
    private ConversionResponse rotatePdf(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();

        // CORREÇÃO: Acessando o Map diretamente
        Map<String, Object> options = request.getOptions();
        int rotationDegrees = 90; // Valor padrão

        if (options != null && options.get("degrees") instanceof Integer) {
            rotationDegrees = (Integer) options.get("degrees");
        }

        String outputFileName = "rotated_" + file.getOriginalFilename();
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            for (PDPage page : document.getPages()) {
                int currentRotation = page.getRotation();
                page.setRotation((currentRotation + rotationDegrees) % 360);
            }

            document.save(outputPath.toFile());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(file.getSize())
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("PDF rotacionado em %d graus", rotationDegrees))
                    .build();
        }
    }
    /**
     * Adiciona marca d'água ao PDF
     * CORREÇÃO: Usando Loader.loadPDF() e nova API de fontes
     * CORREÇÃO: Corrigido acesso ao Map de opções
     */
    private ConversionResponse addWatermark(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();

        // CORREÇÃO: Acessando o Map diretamente
        Map<String, Object> options = request.getOptions();
        String watermarkText = "CONFIDENTIAL"; // Valor padrão

        if (options != null && options.get("text") instanceof String) {
            watermarkText = (String) options.get("text");
        }

        String outputFileName = "watermarked_" + file.getOriginalFilename();
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            // Cria a fonte usando a nova API do PDFBox 3.x
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            for (PDPage page : document.getPages()) {
                PDRectangle pageSize = page.getMediaBox();

                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                    // Configuração da marca d'água
                    contentStream.beginText();
                    contentStream.setFont(font, 50);
                    contentStream.setNonStrokingColor(200, 200, 200); // Cinza claro

                    // Calcula posição central
                    float textWidth = font.getStringWidth(watermarkText) / 1000 * 50;
                    float x = (pageSize.getWidth() - textWidth) / 2;
                    float y = pageSize.getHeight() / 2;

                    // Rotaciona o texto em 45 graus
                    Matrix matrix = new Matrix(
                            (float) Math.cos(Math.toRadians(45)),
                            (float) Math.sin(Math.toRadians(45)),
                            (float) -Math.sin(Math.toRadians(45)),
                            (float) Math.cos(Math.toRadians(45)),
                            x,
                            y
                    );
                    contentStream.setTextMatrix(matrix);

                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }

            document.save(outputPath.toFile());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(file.getSize())
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message("Marca d'água adicionada com sucesso")
                    .build();
        }
    }
    /**
     * Protege PDF com senha
     * CORREÇÃO: Usando Loader.loadPDF()
     * CORREÇÃO: Corrigido acesso ao Map de opções
     */
    private ConversionResponse protectPdf(ConversionRequest request) throws IOException {
        MultipartFile file = request.getFile();

        // CORREÇÃO: Acessando o Map diretamente
        Map<String, Object> options = request.getOptions();
        String password = null;

        if (options != null && options.get("password") instanceof String) {
            password = (String) options.get("password");
        }

        // Validação básica da senha
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("A senha é obrigatória para proteger o PDF.");
        }

        String outputFileName = "protected_" + file.getOriginalFilename();
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        byte[] pdfBytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            // Define permissões
            AccessPermission permissions = new AccessPermission();
            permissions.setCanPrint(true);
            permissions.setCanModify(false);

            // Cria política de proteção
            StandardProtectionPolicy policy = new StandardProtectionPolicy(
                    password, password, permissions
            );
            policy.setEncryptionKeyLength(128);

            document.protect(policy);
            document.save(outputPath.toFile());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .originalFileName(file.getOriginalFilename())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .originalFileSize(file.getSize())
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message("PDF protegido com senha")
                    .build();
        }
    }

    /**
     * Converte imagens para PDF
     */
    private ConversionResponse convertImageToPdf(ConversionRequest request) throws IOException {
        List<MultipartFile> files = request.getFiles() != null
                ? request.getFiles()
                : Collections.singletonList(request.getFile());

        String outputFileName = "converted_" + System.currentTimeMillis() + ".pdf";
        Path outputPath = Paths.get(TEMP_DIR, outputFileName);

        try (PDDocument document = new PDDocument()) {
            for (MultipartFile file : files) {
                BufferedImage image = ImageIO.read(file.getInputStream());

                // Cria página com tamanho da imagem
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);

                // Adiciona imagem à página
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                        document, file.getBytes(), file.getOriginalFilename()
                );

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, 0, 0);
                }
            }

            document.save(outputPath.toFile());

            return ConversionResponse.builder()
                    .conversionId(UUID.randomUUID().toString())
                    .convertedFileName(outputFileName)
                    .downloadUrl("/download/" + outputFileName)
                    .convertedFileSize(Files.size(outputPath))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .message(String.format("%d imagens convertidas para PDF", files.size()))
                    .build();
        }
    }

    /**
     * Valida arquivo de entrada
     */
    @Override
    public boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Verifica tamanho máximo (50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            return false;
        }

        // Verifica tipo MIME
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.startsWith("image/")
        );
    }

    /**
     * Obtém extensão do arquivo de saída
     */
    @Override
    public String getOutputExtension(ConversionRequest request) {
        switch (request.getConversionType()) {
            case PDF_TO_WORD:
                return ".docx";
            case PDF_TO_EXCEL:
                return ".xlsx";
            case PDF_TO_JPG:
                return ".jpg";
            case PDF_TO_PNG:
                return ".png";
            case PDF_TO_TEXT:
                return ".txt";
            default:
                return ".pdf";
        }
    }

    /**
     * Estima tempo de processamento
     */
    @Override
    public int estimateProcessingTime(long fileSize) {
        // Estimativa: 1 segundo por MB
        return Math.max(1, (int) (fileSize / (1024 * 1024)));
    }

    /**
     * Limpa arquivos temporários
     */
    @Override
    public void cleanupTempFiles(List<File> files) {
        for (File file : files) {
            try {
                if (file.exists()) {
                    Files.delete(file.toPath());
                    log.debug("Arquivo temporário removido: {}", file.getName());
                }
            } catch (IOException e) {
                log.warn("Erro ao remover arquivo temporário: {}", file.getName(), e);
            }
        }
    }

    /**
     * Constrói resposta de erro
     */
    private ConversionResponse buildErrorResponse(Exception e) {
        return ConversionResponse.builder()
                .conversionId(UUID.randomUUID().toString())
                .status(ConversionResponse.ProcessingStatus.FAILED)
                .message("Erro durante o processamento")
                .errorDetails(ConversionResponse.ErrorDetails.builder()
                        .errorCode("CONVERSION_ERROR")
                        .errorMessage(e.getMessage())
                        .technicalDetails(e.getClass().getName())
                        .timestamp(LocalDateTime.now())
                        .build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Obtém nome base do arquivo (sem extensão)
     */
    private String getBaseFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Gera nome do arquivo de saída
     */
    private String getOutputFileName(String originalName, String newExtension) {
        return getBaseFileName(originalName) + "_converted_" + System.currentTimeMillis() + newExtension;
    }
}