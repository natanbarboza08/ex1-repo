package br.com.cloudtech;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTest {

    private static final String MAQUINA =
            "{\"nome\":\"Maquina CNC 01\",\"tipo\":\"CNC\",\"setor\":\"Usinagem\",\"status\":\"em operacao\"}";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    // ---------- helpers ----------

    private long cadastrarMaquina() throws Exception {
        var resposta = mvc.perform(post("/maquinas/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content(MAQUINA))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
    }

    private String producao(long maquinaId, String consumo, int pecas) {
        return "{\"maquinaId\":" + maquinaId + ",\"consumoKw\":" + consumo
                + ",\"horarioAcionamento\":\"08:30\",\"pecasProduzidas\":" + pecas + "}";
    }

    private long registrarProducao(long maquinaId) throws Exception {
        var resposta = mvc.perform(post("/producao")
                        .contentType(MediaType.APPLICATION_JSON).content(producao(maquinaId, "12.5", 150)))
                .andExpect(status().isCreated()).andReturn();
        return json.readTree(resposta.getResponse().getContentAsString()).get("id").asLong();
    }

    // ---------- a) página inicial ----------

    @Test
    void paginaInicialResponde200() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    // ---------- b) status e versão ----------

    @Test
    void statusRetornaOnline() throws Exception {
        mvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void versaoRetorna200() throws Exception {
        mvc.perform(get("/versao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("1.0.0"));
    }

    // ---------- c) e d) cadastrar e listar máquinas ----------

    @Test
    void cadastraEListaMaquinas() throws Exception {
        cadastrarMaquina();
        mvc.perform(get("/maquinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo").value(hasItem("CNC")));
    }

    @Test
    void cadastroInvalidoRetorna400() throws Exception {
        mvc.perform(post("/maquinas/cadastro").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT e DELETE de máquinas ----------

    @Test
    void atualizaMaquina() throws Exception {
        long id = cadastrarMaquina();
        String novo = "{\"nome\":\"Maquina Torno 02\",\"tipo\":\"Torno\",\"setor\":\"Montagem\",\"status\":\"parada\"}";
        mvc.perform(put("/maquinas/" + id).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.tipo").value("Torno"))
                .andExpect(jsonPath("$.status").value("parada"));
    }

    @Test
    void atualizarMaquinaInexistenteRetorna404() throws Exception {
        mvc.perform(put("/maquinas/999999").contentType(MediaType.APPLICATION_JSON).content(MAQUINA))
                .andExpect(status().isNotFound());
    }

    @Test
    void excluiMaquina() throws Exception {
        long id = cadastrarMaquina();
        mvc.perform(delete("/maquinas/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/maquinas/" + id)).andExpect(status().isNotFound());
    }

    // ---------- e) registrar e consultar produção ----------

    @Test
    void registraEConsultaProducao() throws Exception {
        long maquinaId = cadastrarMaquina();
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content(producao(maquinaId, "12.5", 150)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pecasProduzidas").value(150))
                .andExpect(jsonPath("$.consumoKw").value(12.5));
        registrarProducao(maquinaId);

        mvc.perform(get("/producao").param("maquinaId", String.valueOf(maquinaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/producao")).andExpect(status().isOk());
    }

    @Test
    void atualizaEExcluiProducao() throws Exception {
        long maquinaId = cadastrarMaquina();
        long id = registrarProducao(maquinaId);

        mvc.perform(put("/producao/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(producao(maquinaId, "20.0", 300)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pecasProduzidas").value(300));

        mvc.perform(delete("/producao/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/producao/" + id)).andExpect(status().isNotFound());
    }

    // ---------- f) códigos HTTP adequados ----------

    @Test
    void producaoComDadosInvalidosRetorna400() throws Exception {
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content(producao(999999, "-1", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void producaoDeMaquinaInexistenteRetorna404() throws Exception {
        mvc.perform(post("/producao").contentType(MediaType.APPLICATION_JSON)
                        .content(producao(999999, "12.5", 150)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/producao").param("maquinaId", "999999"))
                .andExpect(status().isNotFound());
    }
}
