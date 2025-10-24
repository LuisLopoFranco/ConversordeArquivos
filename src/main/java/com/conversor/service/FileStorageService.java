package com.conversor.service;

import com.conversor.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Serviço responsável pelo armazenamento e recuperação de arquivos.
 *
 * Gerencia o upload de arquivos originais e o armazenamento de arquivos convertidos.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadLocation;
    private final Path convertedLocation;

    /**
     * Construtor que inicializa os diretórios de armazenamento.
     *
     * @param uploadDir    Diretório para arquivos enviados
     * @param convertedDir Diretório para arquivos convertidos
     */
    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${file.converted-dir}") String convertedDir) {

        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.convertedLocation = Paths.get(convertedDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadLocation);
            Files.createDirectories(this.convertedLocation);
            logger.info("Diretórios de armazenamento criados com sucesso");
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível criar os diretórios de armazenamento", ex);
        }
    }

    /**
     * Armazena um arquivo enviado pelo usuário.
     *
     * @param file Arquivo multipart enviado
     * @return Path do arquivo armazenado
     * @throws FileStorageException se houver erro no armazenamento
     */
    public Path storeUploadedFile(MultipartFile file) {
        // Normaliza o nome do arquivo
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Verifica se o nome do arquivo contém caracteres inválidos
            if (originalFilename.contains("..")) {
                throw new FileStorageException(
                        "Nome de arquivo inválido: " + originalFilename);
            }

            // Gera um nome único para o arquivo
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = this.uploadLocation.resolve(uniqueFilename);

            // Copia o arquivo para o diretório de destino
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.debug("Arquivo armazenado: {}", uniqueFilename);
            return targetLocation;

        } catch (IOException ex) {
            throw new FileStorageException(
                    "Não foi possível armazenar o arquivo " + originalFilename, ex);
        }
    }

    /**
     * Armazena um arquivo convertido.
     *
     * @param sourceFile Arquivo de origem
     * @param filename   Nome do arquivo convertido
     * @return Path do arquivo convertido
     * @throws FileStorageException se houver erro no armazenamento
     */
    public Path storeConvertedFile(File sourceFile, String filename) {
        try {
            String cleanFilename = StringUtils.cleanPath(filename);

            if (cleanFilename.contains("..")) {
                throw new FileStorageException("Nome de arquivo inválido: " + filename);
            }

            Path targetLocation = this.convertedLocation.resolve(cleanFilename);
            Files.copy(sourceFile.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.debug("Arquivo convertido armazenado: {}", cleanFilename);
            return targetLocation;

        } catch (IOException ex) {
            throw new FileStorageException(
                    "Não foi possível armazenar o arquivo convertido " + filename, ex);
        }
    }

    /**
     * Carrega um arquivo como Resource.
     *
     * @param filename   Nome do arquivo
     * @param isConverted Se o arquivo está no diretório de convertidos
     * @return Resource do arquivo
     * @throws FileStorageException se o arquivo não for encontrado
     */
    public Resource loadFileAsResource(String filename, boolean isConverted) {
        try {
            Path filePath;
            if (isConverted) {
                filePath = convertedLocation.resolve(filename).normalize();
            } else {
                filePath = uploadLocation.resolve(filename).normalize();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Arquivo não encontrado: " + filename);
            }

        } catch (MalformedURLException ex) {
            throw new FileStorageException("Arquivo não encontrado: " + filename, ex);
        }
    }

    /**
     * Deleta um arquivo.
     *
     * @param filePath Path do arquivo a ser deletado
     * @return true se deletado com sucesso, false caso contrário
     */
    public boolean deleteFile(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
            logger.debug("Arquivo deletado: {}", filePath.getFileName());
            return true;
        } catch (IOException ex) {
            logger.error("Erro ao deletar arquivo: {}", filePath.getFileName(), ex);
            return false;
        }
    }

    /**
     * Obtém o diretório de uploads.
     *
     * @return Path do diretório de uploads
     */
    public Path getUploadLocation() {
        return uploadLocation;
    }

    /**
     * Obtém o diretório de arquivos convertidos.
     *
     * @return Path do diretório de arquivos convertidos
     */
    public Path getConvertedLocation() {
        return convertedLocation;
    }
}
