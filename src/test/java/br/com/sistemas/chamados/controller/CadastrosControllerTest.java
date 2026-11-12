package br.com.sistemas.chamados.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

/** Testes de Categoria e Técnico (casos de sucesso e duplicidade). */
@SpringBootTest
@Transactional
class CadastrosControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void criarCategoriaDeveRetornar201() throws Exception {
        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Rede","descricao":"Problemas de conectividade"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Rede"));
    }

    @Test
    void categoriaDuplicadaDeveRetornar409() throws Exception {
        categoriaRepository.save(new Categoria("Rede", null));

        mockMvc.perform(post("/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Rede","descricao":"outra"}
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void excluirCategoriaDeveRetornar204() throws Exception {
        Categoria salva = categoriaRepository.save(new Categoria("Rede", null));

        mockMvc.perform(delete("/categorias/" + salva.getId()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/categorias/" + salva.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void criarTecnicoDeveRetornar201() throws Exception {
        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Carlos","email":"carlos@empresa.com","perfil":"TECNICO"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.perfil").value("TECNICO"));
    }

    @Test
    void tecnicoComEmailDuplicadoDeveRetornar409() throws Exception {
        tecnicoRepository.save(new Tecnico("Carlos", "carlos@empresa.com", Perfil.TECNICO));

        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Outro","email":"carlos@empresa.com","perfil":"ADMIN"}
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void tecnicoComPerfilInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/tecnicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Carlos","email":"carlos@empresa.com","perfil":"CHEFE"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
