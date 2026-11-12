package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

@SpringBootTest
@Transactional
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository repository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    private Cliente cliente;
    private Cliente outroCliente;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(new Cliente("Ana", "ana@exemplo.com", null));
        outroCliente = clienteRepository.save(new Cliente("Bia", "bia@exemplo.com", null));
        categoria = categoriaRepository.save(new Categoria("Hardware", "Problemas físicos"));

        repository.save(new Chamado("Impressora parada", "d", Prioridade.ALTA, cliente, categoria));

        Chamado fechado = new Chamado("Mouse quebrado", "d", Prioridade.BAIXA, cliente, categoria);
        fechado.setStatus(StatusChamado.FECHADO);
        repository.save(fechado);

        repository.save(new Chamado("Teclado", "d", Prioridade.ALTA, outroCliente, categoria));
    }

    @Test
    void filtrarSemParametrosDeveRetornarTodos() {
        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 10));
        assertEquals(3, pagina.getTotalElements());
    }

    @Test
    void filtrarPorStatus() {
        Page<Chamado> pagina = repository.filtrar(StatusChamado.FECHADO, null, null, PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
        assertEquals("Mouse quebrado", pagina.getContent().get(0).getTitulo());
    }

    @Test
    void filtrarPorPrioridadeEClienteCombinados() {
        Page<Chamado> pagina = repository.filtrar(
            null, Prioridade.ALTA, cliente.getId(), PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
        assertEquals("Impressora parada", pagina.getContent().get(0).getTitulo());
    }

    @Test
    void paginacaoDeveRespeitarOTamanho() {
        Page<Chamado> pagina = repository.filtrar(null, null, null, PageRequest.of(0, 2));
        assertEquals(2, pagina.getContent().size());
        assertEquals(2, pagina.getTotalPages());
    }

    @Test
    void findByClienteIdDeveRetornarSomenteDoCliente() {
        Page<Chamado> pagina = repository.findByClienteId(outroCliente.getId(), PageRequest.of(0, 10));
        assertEquals(1, pagina.getTotalElements());
        assertTrue(repository.existsByClienteId(cliente.getId()));
    }
}
