package com.conversor.controller;

import com.conversor.model.ConversionType;
import com.conversor.model.FileFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelas páginas web da aplicação.
 *
 * Gerencia as views Thymeleaf e fornece dados para renderização.
 */
@Controller
public class WebController {

    /**
     * Página inicial da aplicação.
     *
     * @param model Modelo para a view
     * @return Nome da view index
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fileFormats", FileFormat.values());
        model.addAttribute("conversionTypes", ConversionType.values());
        return "index";
    }

    /**
     * Página sobre a aplicação.
     *
     * @return Nome da view about
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /**
     * Página de conversões suportadas.
     *
     * @param model Modelo para a view
     * @return Nome da view conversions
     */
    @GetMapping("/conversions")
    public String conversions(Model model) {
        model.addAttribute("conversionTypes", ConversionType.values());
        return "conversions";
    }
}
