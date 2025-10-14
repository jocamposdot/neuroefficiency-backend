package com.neuroefficiency.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Configuração de Internacionalização (i18n)
 * 
 * Suporta múltiplos idiomas baseado em header Accept-Language.
 * 
 * Idiomas suportados:
 * - pt-BR (Português Brasil) - padrão
 * - en-US (English US)
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@Configuration
public class I18nConfig {

    /**
     * Configuração do MessageSource para i18n
     * 
     * Arquivos esperados:
     * - src/main/resources/messages.properties (fallback)
     * - src/main/resources/messages_pt_BR.properties
     * - src/main/resources/messages_en_US.properties
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * Resolver de locale baseado em Accept-Language header
     * 
     * Exemplos:
     * - Accept-Language: pt-BR → Português
     * - Accept-Language: en-US → English
     * - Accept-Language: es-ES → pt-BR (fallback)
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        resolver.setSupportedLocales(List.of(
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("en-US")
        ));
        return resolver;
    }
}

