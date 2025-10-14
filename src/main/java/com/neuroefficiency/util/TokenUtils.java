package com.neuroefficiency.util;

import org.apache.commons.codec.digest.DigestUtils;
import lombok.experimental.UtilityClass;

import java.util.UUID;

/**
 * Utilitários para geração e hash de tokens de reset de senha
 * 
 * IMPORTANTE: Usa SHA-256 ao invés de BCrypt porque:
 * - BCrypt usa salt aleatório, impossibilitando lookup direto no banco
 * - SHA-256 é determinístico (mesmo input = mesmo hash)
 * - SHA-256 é unidirecional e seguro para tokens temporários
 * - BCrypt deve ser usado APENAS para senhas de usuário
 * 
 * @author Neuroefficiency Team
 * @version 1.0
 * @since 2025-10-14
 */
@UtilityClass
public class TokenUtils {

    /**
     * Gera token seguro de 64 caracteres hexadecimais (256 bits de entropia)
     * 
     * Exemplo de saída: "a1b2c3d4e5f6...64 chars"
     * 
     * @return token seguro em formato hexadecimal
     */
    public static String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Gera hash SHA-256 do token
     * 
     * Usa SHA-256 porque é determinístico (mesmo input = mesmo hash),
     * permitindo lookup direto no banco, diferente de BCrypt que usa salt aleatório.
     * 
     * Exemplo:
     * - Input: "abc123"
     * - Output: "6ca13d52ca70c883e0f0bb101e425a89e8624de51db2d2392593af6a84118090"
     * 
     * @param token o token em plain text
     * @return hash SHA-256 do token (64 caracteres hexadecimais)
     */
    public static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser nulo ou vazio");
        }
        return DigestUtils.sha256Hex(token);
    }

    /**
     * Verifica se token corresponde ao hash
     * 
     * @param token o token em plain text
     * @param hash o hash SHA-256 armazenado no banco
     * @return true se o token corresponde ao hash
     */
    public static boolean matches(String token, String hash) {
        if (token == null || hash == null) {
            return false;
        }
        return hashToken(token).equals(hash);
    }
}

