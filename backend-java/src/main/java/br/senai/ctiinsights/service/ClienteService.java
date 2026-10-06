package br.senai.ctiinsights.service;

import br.senai.ctiinsights.domain.Cliente;
import br.senai.ctiinsights.domain.Consultor;
import br.senai.ctiinsights.domain.NivelCliente;
import br.senai.ctiinsights.dto.ClienteRequest;
import br.senai.ctiinsights.dto.ClienteResponse;
import br.senai.ctiinsights.exception.ClienteDuplicadoException;
import br.senai.ctiinsights.exception.RecursoNaoEncontradoException;
import br.senai.ctiinsights.repository.ClienteRepository;
import br.senai.ctiinsights.repository.ConsultorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de negocio do CRUD de clientes consumido pelo Front-end. */
@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final ConsultorRepository consultores;

    public ClienteService(ClienteRepository clientes, ConsultorRepository consultores) {
        this.clientes = clientes;
        this.consultores = consultores;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String segmento) {
        List<Cliente> encontrados = (segmento == null || segmento.isBlank())
                ? clientes.findAll()
                : clientes.findBySegmentoIgnoreCase(segmento);
        return encontrados.stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest requisicao) {
        if (clientes.existsByCodigoCti(requisicao.codigoCti().trim())) {
            throw new ClienteDuplicadoException(requisicao.codigoCti());
        }
        Cliente cliente = new Cliente(
                requisicao.codigoCti().trim(),
                requisicao.segmento().trim(),
                NivelCliente.of(requisicao.nivel()),
                requisicao.faturamentoAnual());
        cliente.setConsultor(resolverConsultor(requisicao.consultor()));
        return ClienteResponse.de(clientes.save(cliente));
    }

    /** PUT: substitui os dados do cliente mantendo o mesmo id. */
    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest requisicao) {
        Cliente cliente = buscarEntidade(id);
        clientes.findByCodigoCti(requisicao.codigoCti().trim())
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new ClienteDuplicadoException(requisicao.codigoCti());
                });
        cliente.setCodigoCti(requisicao.codigoCti().trim());
        cliente.setSegmento(requisicao.segmento().trim());
        cliente.setNivel(NivelCliente.of(requisicao.nivel()));
        cliente.setFaturamentoAnual(requisicao.faturamentoAnual());
        cliente.setConsultor(resolverConsultor(requisicao.consultor()));
        return ClienteResponse.de(cliente);
    }

    /** Atualizacao parcial usada pela tabela do dashboard (reclassificar nivel). */
    @Transactional
    public ClienteResponse atualizarNivel(Long id, String nivel) {
        Cliente cliente = buscarEntidade(id);
        cliente.setNivel(NivelCliente.of(nivel));
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void remover(Long id) {
        Cliente cliente = buscarEntidade(id);
        clientes.delete(cliente);
    }

    private Cliente buscarEntidade(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    private Consultor resolverConsultor(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return consultores.findByNomeIgnoreCase(nome.trim())
                .orElseGet(() -> consultores.save(new Consultor(nome.trim(), null)));
    }
}
