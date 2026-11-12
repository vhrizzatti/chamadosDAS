package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@SpringBootTest
@Transactional
class ChamadoControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    private MockMvc mockMvc;
    private Cliente cliente;
    private Categoria categoria;
    private Tecnico tecnico;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        cliente = clienteRepository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));
        categoria = categoriaRepository.save(new Categoria("Hardware", "Problemas físicos"));
        tecnico = tecnicoRepository.save(new Tecnico("Carlos", "carlos@empresa.com", Perfil.TECNICO));
    }

    private Chamado novoChamado(Prioridade prioridade) {
        return chamadoRepository.save(
            new Chamado("Impressora parada", "Não imprime", prioridade, cliente, categoria));
    }


    // ---------- criação e leitura ----------

    @Test
    void criarChamadoDeveNascerAbertoComDataDeAbertura() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Impressora parada","descricao":"Não imprime","prioridade":"ALTA",
                     "clienteId":%d,"categoriaId":%d}
                    """.formatted(cliente.getId(), categoria.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ABERTO"))
            .andExpect(jsonPath("$.dataAbertura").exists())
            .andExpect(jsonPath("$.dataFechamento").doesNotExist())
            .andExpect(jsonPath("$.clienteNome").value("Ana Souza"));
    }

    @Test
    void criarChamadoComClienteInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Teste","prioridade":"BAIXA","clienteId":99999,"categoriaId":%d}
                    """.formatted(categoria.getId())))
            .andExpect(status().isNotFound());
    }

    @Test
    void criarChamadoSemTituloDeveRetornar400() throws Exception {
        mockMvc.perform(post("/chamados")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"","prioridade":"BAIXA","clienteId":%d,"categoriaId":%d}
                    """.formatted(cliente.getId(), categoria.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void buscarChamadoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/chamados/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void atualizarChamadoDeveRetornar200() throws Exception {
        Chamado chamado = novoChamado(Prioridade.BAIXA);

        mockMvc.perform(put("/chamados/" + chamado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"Novo título","descricao":"Nova descrição","prioridade":"CRITICA",
                     "categoriaId":%d}
                    """.formatted(categoria.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Novo título"))
            .andExpect(jsonPath("$.prioridade").value("CRITICA"));
    }

    @Test
    void excluirChamadoDeveRetornar204() throws Exception {
        Chamado chamado = novoChamado(Prioridade.BAIXA);

        mockMvc.perform(delete("/chamados/" + chamado.getId()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/chamados/" + chamado.getId()))
            .andExpect(status().isNotFound());
    }

    // ---------- regras de negócio ----------

    @Test
    void atribuirTecnicoDeveRetornar200() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/atribuir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tecnicoId\":" + tecnico.getId() + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tecnicoNome").value("Carlos"));
    }

    @Test
    void atribuirTecnicoInexistenteDeveRetornar404() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/atribuir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tecnicoId\":99999}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void transicaoValidaDeStatusDeveRetornar200() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
    }

    @Test
    void transicaoInvalidaDeStatusDeveRetornar409() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"AGUARDANDO_CLIENTE\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void statusInexistenteNoJsonDeveRetornar400() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INVENTADO\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void fecharChamadoDevePreencherDataDeFechamento() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"FECHADO\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FECHADO"))
            .andExpect(jsonPath("$.dataFechamento").isNotEmpty());
    }

    @Test
    void chamadoFechadoNaoPodeReceberTecnicoNemSerAlterado() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);
        chamado.setStatus(StatusChamado.FECHADO);

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/atribuir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tecnicoId\":" + tecnico.getId() + "}"))
            .andExpect(status().isConflict());

        mockMvc.perform(patch("/chamados/" + chamado.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isConflict());

        mockMvc.perform(put("/chamados/" + chamado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"titulo":"X","prioridade":"BAIXA","categoriaId":%d}
                    """.formatted(categoria.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void clienteComChamadosNaoPodeSerExcluido() throws Exception {
        novoChamado(Prioridade.BAIXA);

        mockMvc.perform(delete("/clientes/" + cliente.getId()))
            .andExpect(status().isConflict());
    }

    // ---------- filtros e paginação ----------

    @Test
    void filtrosCombinadosDevemRestringirOResultado() throws Exception {
        novoChamado(Prioridade.ALTA);
        novoChamado(Prioridade.BAIXA);
        Chamado fechado = novoChamado(Prioridade.ALTA);
        fechado.setStatus(StatusChamado.FECHADO);

        mockMvc.perform(get("/chamados")
                .param("status", "ABERTO")
                .param("prioridade", "ALTA")
                .param("clienteId", cliente.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(1))
            .andExpect(jsonPath("$.conteudo[0].prioridade").value("ALTA"));
    }

    @Test
    void paginacaoDeveRespeitarTamanhoDaPagina() throws Exception {
        novoChamado(Prioridade.ALTA);
        novoChamado(Prioridade.BAIXA);
        novoChamado(Prioridade.MEDIA);

        mockMvc.perform(get("/chamados").param("page", "0").param("size", "2")
                .param("sort", "dataAbertura,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.conteudo.length()").value(2))
            .andExpect(jsonPath("$.totalElementos").value(3))
            .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    void statusInvalidoNoFiltroDeveRetornar400() throws Exception {
        mockMvc.perform(get("/chamados").param("status", "XYZ"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listarChamadosDoCliente() throws Exception {
        novoChamado(Prioridade.ALTA);

        mockMvc.perform(get("/clientes/" + cliente.getId() + "/chamados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElementos").value(1));

        mockMvc.perform(get("/clientes/99999/chamados"))
            .andExpect(status().isNotFound());
    }

    // ---------- comentários ----------

    @Test
    void comentarDeveRetornar201EAparecerNoDetalhe() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(post("/chamados/" + chamado.getId() + "/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"texto":"Estou verificando o cabo.","autor":"Carlos"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.autor").value("Carlos"));

        mockMvc.perform(get("/chamados/" + chamado.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comentarios[0].texto").value("Estou verificando o cabo."));

        mockMvc.perform(get("/chamados/" + chamado.getId() + "/comentarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].autor").value("Carlos"));
    }

    @Test
    void comentarioSemTextoDeveRetornar400() throws Exception {
        Chamado chamado = novoChamado(Prioridade.ALTA);

        mockMvc.perform(post("/chamados/" + chamado.getId() + "/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"texto":"","autor":"Carlos"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
