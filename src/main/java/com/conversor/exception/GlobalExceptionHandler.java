package com.conversor.exception;

import com.conversor.dto.ConversionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tratador global de exceções da aplicação.
 *
 * Centraliza o tratamento de erros e fornece respostas apropriadas
 * para diferentes tipos de exceções.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de conversão de arquivos.
     *
     * @param ex Exceção de conversão
     * @return ResponseEntity com mensagem de erro
     */
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<ConversionResponse> handleFileConversionException(FileConversionException ex) {
        ConversionResponse response = ConversionResponse.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Trata exceções de armazenamento de arquivos.
     *
     * @param ex Exceção de armazenamento
     * @return ResponseEntity com mensagem de erro
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ConversionResponse> handleFileStorageException(FileStorageException ex) {
        ConversionResponse response = ConversionResponse.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Trata exceções de tamanho de arquivo excedido.
     *
     * @param ex                 Exceção de tamanho excedido
     * @param redirectAttributes Atributos para redirecionamento
     * @return String com URL de redirecionamento
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException ex,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error",
                "O arquivo é muito grande! Tamanho máximo permitido: 50MB");
        return "redirect:/";
    }

    /**
     * Trata exceções genéricas não previstas.
     *
     * @param ex Exceção genérica
     * @return ResponseEntity com mensagem de erro
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ConversionResponse> handleGenericException(Exception ex) {
        ConversionResponse response = ConversionResponse.error(
                "Ocorreu um erro inesperado: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
