package br.com.sistemas.chamados.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/** Envelope simples e estável para respostas paginadas. */
public record PaginaResposta<T>(
    List<T> conteudo,
    int pagina,
    int tamanho,
    long totalElementos,
    int totalPaginas
) {
    public static <T> PaginaResposta<T> de(Page<T> page) {
        return new PaginaResposta<>(
            page.getContent(), page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages());
    }
}
