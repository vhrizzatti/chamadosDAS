package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.repository.ClienteRepository;

@SpringBootTest
@Transactional
class ClienteControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ClienteRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void criarDeveRetornar201() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Ana Souza","email":"ana@exemplo.com","telefone":"47999999999"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
    }

    @Test
    void dadosInvalidosDevemRetornar400() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"","email":"email-invalido","telefone":"123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detalhes").isNotEmpty());
    }

    @Test
    void emailDuplicadoDeveRetornar409() throws Exception {
        repository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Outro Cliente","email":"ana@exemplo.com","telefone":"47977777777"}
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void buscarInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(get("/clientes/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void listarEBuscarPorIdDevemRetornar200() throws Exception {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));

        mockMvc.perform(get("/clientes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Ana Souza"));

        mockMvc.perform(get("/clientes/" + salvo.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(salvo.getId()));
    }

    @Test
    void atualizarDeveRetornar200ComDadosNovos() throws Exception {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));

        mockMvc.perform(put("/clientes/" + salvo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Ana Souza Atualizada","email":"ana@exemplo.com","telefone":"47988888888"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Ana Souza Atualizada"));
    }

    @Test
    void excluirDeveRetornar204EDepoisBuscarRetorna404() throws Exception {
        Cliente salvo = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));

        mockMvc.perform(delete("/clientes/" + salvo.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/clientes/" + salvo.getId()))
            .andExpect(status().isNotFound());
    }
}
