package br.com.cloudtech.controller;

import br.com.cloudtech.models.CadastroMaquina;
import br.com.cloudtech.models.DadosProducao;
import br.com.cloudtech.models.Maquinas;
import br.com.cloudtech.models.Producao;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MaquinaControllers {

    // Armazenamento em memória (apagado ao reiniciar a aplicação)
    private final Map<Long, Maquinas> maquinas = new ConcurrentHashMap<>();
    private final Map<Long, Producao> historico = new ConcurrentHashMap<>();
    private final AtomicLong idsMaquinas = new AtomicLong();
    private final AtomicLong idsProducao = new AtomicLong();

    // ---------- Endpoints gerais ----------

    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of("mensagem", "Bem-vindo ao monitoramento de máquinas da CloudTech Solutions");
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "online", "mensagem", "Aplicação de monitoramento em execução");
    }

    @GetMapping("/versao")
    public Map<String, String> versao() {
        return Map.of("versao", "1.0.0");
    }

    // ---------- Máquinas ----------

    @GetMapping("/maquinas")
    public List<Maquinas> listarMaquinas() {
        return maquinas.values().stream().sorted(Comparator.comparingLong(Maquinas::id)).toList();
    }

    @PostMapping("/maquinas/cadastro")
    public ResponseEntity<Maquinas> cadastrarMaquina(@Valid @RequestBody CadastroMaquina dados) {
        long id = idsMaquinas.incrementAndGet();
        Maquinas maquina = new Maquinas(id, dados.nome(), dados.tipo(), dados.setor(), dados.status());
        maquinas.put(id, maquina);
        return ResponseEntity.status(HttpStatus.CREATED).body(maquina);
    }

    @PutMapping("/maquinas/{id}")
    public Maquinas atualizarMaquina(@PathVariable long id, @Valid @RequestBody CadastroMaquina dados) {
        verificarMaquina(id);
        Maquinas atualizada = new Maquinas(id, dados.nome(), dados.tipo(), dados.setor(), dados.status());
        maquinas.put(id, atualizada);
        return atualizada;
    }

    @DeleteMapping("/maquinas/{id}")
    public ResponseEntity<Void> excluirMaquina(@PathVariable long id) {
        verificarMaquina(id);
        maquinas.remove(id);
        historico.values().removeIf(p -> p.maquinaId() == id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Produção e consumo ----------

    @PostMapping("/producao")
    public ResponseEntity<Producao> registrarProducao(@Valid @RequestBody DadosProducao dados) {
        verificarMaquina(dados.maquinaId());
        long id = idsProducao.incrementAndGet();
        Producao producao = new Producao(id, dados.maquinaId(), dados.consumoKw(),
                dados.horarioAcionamento(), dados.pecasProduzidas(), LocalDateTime.now());
        historico.put(id, producao);
        return ResponseEntity.status(HttpStatus.CREATED).body(producao);
    }

    @GetMapping("/producao")
    public List<Producao> consultarProducao(@RequestParam(required = false) Long maquinaId) {
        if (maquinaId != null) {
            verificarMaquina(maquinaId);
        }
        return historico.values().stream()
                .filter(p -> maquinaId == null || p.maquinaId() == maquinaId)
                .sorted(Comparator.comparingLong(Producao::id))
                .toList();
    }

    @PutMapping("/producao/{id}")
    public Producao atualizarProducao(@PathVariable long id, @Valid @RequestBody DadosProducao dados) {
        Producao atual = buscarProducao(id);
        verificarMaquina(dados.maquinaId());
        Producao atualizada = new Producao(id, dados.maquinaId(), dados.consumoKw(),
                dados.horarioAcionamento(), dados.pecasProduzidas(), atual.registradoEm());
        historico.put(id, atualizada);
        return atualizada;
    }

    @DeleteMapping("/producao/{id}")
    public ResponseEntity<Void> excluirProducao(@PathVariable long id) {
        buscarProducao(id);
        historico.remove(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Auxiliares ----------

    private void verificarMaquina(long id) {
        if (!maquinas.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Máquina não encontrada");
        }
    }

    private Producao buscarProducao(long id) {
        Producao producao = historico.get(id);
        if (producao == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de produção não encontrado");
        }
        return producao;
    }
}
