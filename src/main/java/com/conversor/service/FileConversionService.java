package com.conversor.service;

import com.conversor.exception.FileConversionException;
import com.conversor.model.ConversionType;
import com.conversor.model.ConvertedFile;
import com.conversor.model.FileFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Serviço responsável pela conversão de arquivos entre diferentes formatos.
 *
 * Implementa diversas conversões como PDF para DOCX, PDF para imagem,
 * imagem para PDF, entre outras.
 */
@Service
public class FileConversionService {

    private static final Logger logger = LoggerFactory.getLogger(FileConversionService.class);

    private final FileStorageService fileStorageService;
    private final Map<String, ConvertedFile> conversionHistory;

    /**
     * Construtor do serviço de conversão.
     *
     * @param fileStorageService Serviço de armazenamento de arquivos
     */
    public FileConversionService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.conversionHistory = new HashMap<>();
    }

    /**
     * Converte um arquivo para o formato desejado.
     *
     * @param file         Arquivo a ser convertido
     * @param targetFormat Formato de destino
     * @return ConvertedFile com informações da conversão
     * @throws FileConversionException se houver erro na conversão
     */
    public ConvertedFile convertFile(MultipartFile file, FileFormat targetFormat) {
        logger.info("Iniciando conversão: {} -> {}", file.getOriginalFilename(), targetFormat);

        // Determina o formato de origem
        FileFormat sourceFormat = FileFormat.fromFilename(file.getOriginalFilename());
        if (sourceFormat == null) {
            throw new FileConversionException("Formato de arquivo não suportado");
        }

        // Verifica se a conversão é suportada
        if (!ConversionType.isSupported(sourceFormat, targetFormat)) {
            throw new FileConversionException(
                    String.format("Conversão de %s para %s não é suportada",
                            sourceFormat, targetFormat));
        }

        // Armazena o arquivo original
        Path uploadedFilePath = fileStorageService.storeUploadedFile(file);

        // Cria o objeto ConvertedFile
        ConvertedFile convertedFile = new ConvertedFile(
                file.getOriginalFilename(),
                sourceFormat,
                targetFormat
        );
        convertedFile.setOriginalFilePath(uploadedFilePath.toString());
        convertedFile.setOriginalSize(file.getSize());
        convertedFile.setStatus(ConvertedFile.ConversionStatus.PROCESSING);

        try {
            // Realiza a conversão baseada no tipo
            File convertedTempFile = performConversion(
                    uploadedFilePath.toFile(),
                    sourceFormat,
                    targetFormat
            );

            // Gera nome para o arquivo convertido
            String convertedFilename = generateConvertedFilename(
                    file.getOriginalFilename(),
                    targetFormat
            );

            // Armazena o arquivo convertido
            Path convertedFilePath = fileStorageService.storeConvertedFile(
                    convertedTempFile,
                    convertedFilename
            );

            // Atualiza informações do arquivo convertido
            convertedFile.setConvertedFilename(convertedFilename);
            convertedFile.setConvertedFilePath(convertedFilePath.toString());
            convertedFile.setConvertedSize(convertedTempFile.length());
            convertedFile.setStatus(ConvertedFile.ConversionStatus.COMPLETED);

            // Limpa arquivo temporário
            convertedTempFile.delete();

            // Armazena no histórico
            conversionHistory.put(convertedFile.getId(), convertedFile);

            logger.info("Conversão concluída com sucesso: {}", convertedFilename);
            return convertedFile;

        } catch (Exception ex) {
            convertedFile.setStatus(ConvertedFile.ConversionStatus.FAILED);
            convertedFile.setErrorMessage(ex.getMessage());
            logger.error("Erro na conversão: {}", ex.getMessage(), ex);
            throw new FileConversionException("Erro ao converter arquivo: " + ex.getMessage(), ex);
        }
    }

    /**
     * Realiza a conversão baseada nos formatos de origem e destino.
     *
     * @param sourceFile   Arquivo de origem
     * @param sourceFormat Formato de origem
     * @param targetFormat Formato de destino
     * @return Arquivo temporário convertido
     * @throws IOException                se houver erro de I/O
     * @throws FileConversionException    se a conversão falhar
     */
    private File performConversion(File sourceFile, FileFormat sourceFormat, FileFormat targetFormat)
            throws IOException {

        // PDF para outros formatos
        if (sourceFormat == FileFormat.PDF) {
            switch (targetFormat) {
                case TXT:
                    return convertPdfToTxt(sourceFile);
                case JPG:
                    return convertPdfToImage(sourceFile, "jpg");
                case PNG:
                    return convertPdfToImage(sourceFile, "png");
                default:
                    throw new FileConversionException("Conversão não implementada");
            }
        }

        // Outros formatos para PDF
        if (targetFormat == FileFormat.PDF) {
            switch (sourceFormat) {
                case TXT:
                    return convertTxtToPdf(sourceFile);
                case JPG:
                case PNG:
                    return convertImageToPdf(sourceFile);
                default:
                    throw new FileConversionException("Conversão não implementada");
            }
        }

        // Conversões entre imagens
        if ((sourceFormat == FileFormat.JPG || sourceFormat == FileFormat.PNG) &&
                (targetFormat == FileFormat.JPG || targetFormat == FileFormat.PNG)) {
            return convertImageToImage(sourceFile, targetFormat);
        }

        throw new FileConversionException("Conversão não suportada");
    }

    /**
     * Converte PDF para TXT.
     *
     * @param pdfFile Arquivo PDF
     * @return Arquivo TXT temporário
     * @throws IOException se houver erro na conversão
     */
    private File convertPdfToTxt(File pdfFile) throws IOException {
        logger.debug("Convertendo PDF para TXT");

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            File txtFile = File.createTempFile("converted_", ".txt");
            try (FileWriter writer = new FileWriter(txtFile)) {
                writer.write(text);
            }

            return txtFile;
        }
    }

    /**
     * Converte PDF para imagem (JPG ou PNG).
     *
     * @param pdfFile      Arquivo PDF
     * @param imageFormat  Formato da imagem (jpg ou png)
     * @return Arquivo de imagem temporário
     * @throws IOException se houver erro na conversão
     */
    private File convertPdfToImage(File pdfFile, String imageFormat) throws IOException {
        logger.debug("Convertendo PDF para {}", imageFormat.toUpperCase());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            // Converte apenas a primeira página
            BufferedImage image = renderer.renderImageWithDPI(0, 300);

            File imageFile = File.createTempFile("converted_", "." + imageFormat);
            ImageIO.write(image, imageFormat, imageFile);

            return imageFile;
        }
    }

    /**
     * Converte TXT para PDF.
     *
     * @param txtFile Arquivo TXT
     * @return Arquivo PDF temporário
     * @throws IOException se houver erro na conversão
     */
    private File convertTxtToPdf(File txtFile) throws IOException {
        logger.debug("Convertendo TXT para PDF");

        File pdfFile = File.createTempFile("converted_", ".pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Lê o conteúdo do arquivo TXT
            String content = Files.readString(txtFile.toPath());

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 750);

                // Escreve o texto linha por linha
                String[] lines = content.split("\n");
                for (String line : lines) {
                    // Limita o tamanho da linha para caber na página
                    if (line.length() > 80) {
                        line = line.substring(0, 80);
                    }
                    contentStream.showText(line);
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            document.save(pdfFile);
        }

        return pdfFile;
    }

    /**
     * Converte imagem para PDF.
     *
     * @param imageFile Arquivo de imagem
     * @return Arquivo PDF temporário
     * @throws IOException se houver erro na conversão
     */
    private File convertImageToPdf(File imageFile) throws IOException {
        logger.debug("Convertendo imagem para PDF");

        File pdfFile = File.createTempFile("converted_", ".pdf");

        try (PDDocument document = new PDDocument()) {
            BufferedImage bufferedImage = ImageIO.read(imageFile);

            PDPage page = new PDPage(new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight()));
            document.addPage(page);

            PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            document.save(pdfFile);
        }

        return pdfFile;
    }

    /**
     * Converte imagem para outro formato de imagem.
     *
     * @param sourceFile   Arquivo de imagem de origem
     * @param targetFormat Formato de destino
     * @return Arquivo de imagem temporário
     * @throws IOException se houver erro na conversão
     */
    private File convertImageToImage(File sourceFile, FileFormat targetFormat) throws IOException {
        logger.debug("Convertendo imagem para {}", targetFormat);

        BufferedImage image = ImageIO.read(sourceFile);

        String formatName = targetFormat == FileFormat.JPG ? "jpg" : "png";
        File outputFile = File.createTempFile("converted_", targetFormat.getExtension());

        ImageIO.write(image, formatName, outputFile);

        return outputFile;
    }

    /**
     * Gera o nome do arquivo convertido.
     *
     * @param originalFilename Nome do arquivo original
     * @param targetFormat     Formato de destino
     * @return Nome do arquivo convertido
     */
    private String generateConvertedFilename(String originalFilename, FileFormat targetFormat) {
        String baseName = originalFilename;
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalFilename.substring(0, dotIndex);
        }

        return UUID.randomUUID().toString() + "_" + baseName + targetFormat.getExtension();
    }

    /**
     * Recupera um arquivo convertido do histórico.
     *
     * @param fileId ID do arquivo
     * @return ConvertedFile ou null se não encontrado
     */
    public ConvertedFile getConvertedFile(String fileId) {
        return conversionHistory.get(fileId);
    }

    /**
     * Obtém todos os arquivos convertidos.
     *
     * @return Lista de arquivos convertidos
     */
    public List<ConvertedFile> getAllConvertedFiles() {
        return new ArrayList<>(conversionHistory.values());
    }
}
