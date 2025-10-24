package com.conversor.exception;

/**
 * Exceção customizada para erros durante a conversão de arquivos.
 *
 * Lançada quando ocorre algum problema durante o processo de conversão.
 */
public class FileConversionException extends RuntimeException {

    /**
     * Construtor com mensagem.
     *
     * @param message Mensagem descritiva do erro
     */
    public FileConversionException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa.
     *
     * @param message Mensagem descritiva do erro
     * @param cause   Causa raiz da exceção
     */
    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
