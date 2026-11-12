package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.entity.Cliente;

@SpringBootTest
@Transactional
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void existsByEmailDeveEncontrarClienteSalvo() {
        repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertTrue(repository.existsByEmail("ana@exemplo.com"));
        assertFalse(repository.existsByEmail("outro@exemplo.com"));
    }

    @Test
    void existsByEmailAndIdNotDeveIgnorarOProprioRegistro() {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertFalse(repository.existsByEmailAndIdNot("ana@exemplo.com", salvo.getId()));
        assertTrue(repository.existsByEmailAndIdNot("ana@exemplo.com", salvo.getId() + 1));
    }
}
