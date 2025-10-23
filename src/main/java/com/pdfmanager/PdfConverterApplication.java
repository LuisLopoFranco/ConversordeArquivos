package com.pdfmanager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
/**
 * Classe principal da aplicação PDF Converter
 *
 * Esta aplicação oferece funcionalidades de conversão e manipulação de PDFs
 * similar ao ILovePDF e Sedja, permitindo aos usuários converter, mesclar,
 * dividir e comprimir arquivos PDF.
 *
 * @author PDF Manager Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAsync

public class PdfConverterApplication {
    /**
     * Método principal que inicia a aplicação Spring Boot
     *
     * @param args Argumentos de linha de comando
     */
    public static void main(String[] args){
        SpringApplication.run(PdfConverterApplication.class,args);
    }
    /**
     * Configura o executor de threads para processamento assíncrono
     * Permite o processamento paralelo de múltiplas conversões
     *
     * @return Executor configurado para tarefas assíncronas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("PDFConverter-");
        executor.initialize();
        return executor;
    }
}
