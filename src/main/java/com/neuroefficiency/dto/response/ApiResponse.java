package com.neuroefficiency.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper padrão para respostas da API
 * 
 * Formato esperado pelo frontend:
 * {
 *   "success": true,
 *   "data": {...},
 *   "message": "Operação realizada com sucesso"
 * }
 * 
 * IMPORTANTE: Este wrapper é usado APENAS para novos endpoints de reset de senha.
 * Endpoints existentes (login/register) mantêm formato antigo para compatibilidade.
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 * @param <T> tipo do payload de dados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Omitir campos null no JSON
public class ApiResponse<T> {

    /**
     * Indica se a operação foi bem-sucedida
     */
    private Boolean success;

    /**
     * Dados da resposta (pode ser null em caso de erro)
     */
    private T data;

    /**
     * Mensagem descritiva (sucesso ou erro)
     */
    private String message;

    /**
     * Factory method para resposta de sucesso com dados
     * 
     * @param data dados da resposta
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ApiResponse de sucesso
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Factory method para resposta de sucesso sem dados
     * 
     * @param message mensagem de sucesso
     * @param <T> tipo dos dados
     * @return ApiResponse de sucesso sem payload
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(null)
                .message(message)
                .build();
    }

    /**
     * Factory method para resposta de erro
     * 
     * @param message mensagem de erro
     * @param <T> tipo dos dados
     * @return ApiResponse de erro
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .build();
    }

    /**
     * Factory method para resposta de erro com dados adicionais
     * 
     * @param message mensagem de erro
     * @param data dados adicionais de erro (ex: validações)
     * @param <T> tipo dos dados
     * @return ApiResponse de erro com payload
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(message)
                .build();
    }
}

