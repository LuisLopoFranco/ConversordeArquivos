package com.conversor.exception;

/**
 * Exceção customizada para erros de armazenamento de arquivos.
 *
 * Lançada quando ocorre algum problema ao salvar ou recuperar arquivos.
 */
public class FileStorageException extends RuntimeException {

    /**
     * Construtor com mensagem.
     *
     * @param message Mensagem descritiva do erro
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa.
     *
     * @param message Mensagem descritiva do erro
     * @param cause   Causa raiz da exceção
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
