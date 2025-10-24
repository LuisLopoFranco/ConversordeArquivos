package com.conversor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Conversor de Arquivos.
 *
 * Esta aplicação permite conversão de documentos entre diferentes formatos,
 * similar ao ILovePDF e Sejda.
 *
 * @author Conversor de Arquivos Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ConversorApplication {

    /**
     * Método principal que inicia a aplicação Spring Boot.
     *
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(ConversorApplication.class, args);
    }
}
