package br.senai.ctiinsights.service;

import br.senai.ctiinsights.exception.ColunaObrigatoriaException;
import br.senai.ctiinsights.exception.ExcelInvalidoException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Primeira barreira de qualidade: confere se o arquivo abre como Excel e se as
 * colunas que o script Python espera estao presentes. Falhar aqui e mais barato
 * do que descobrir o problema no meio do tratamento.
 */
@Service
public class PlanilhaService {

    /** Colunas minimas, ja normalizadas (sem acento, minusculas, com underline). */
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "consultor", "cliente_cti", "segmento", "nivel", "faturamento_anual", "servicos_contratados");

    /**
     * Sinonimos de cabecalho aceitos. Espelha o dicionario `equivalentes` de
     * limpeza.py: se uma variacao for aceita la e recusada aqui, a planilha
     * seria barrada antes mesmo de o tratamento tentar resolver o problema.
     * Ao mexer em um dos dois, mexer no outro.
     */
    private static final Map<String, String> EQUIVALENTES = Map.ofEntries(
            // Cabecalhos do modelo oficial da aula (CTI_Insights_modelo_upload_aula).
            Map.entry("codigo_cliente", "cliente_cti"),
            Map.entry("nivel_cliente", "nivel"),
            Map.entry("data_contratacao", "data_inicio"),
            // Variacoes da planilha original da CTI.
            Map.entry("cliente", "cliente_cti"),
            Map.entry("codigo", "cliente_cti"),
            Map.entry("codigo_cti", "cliente_cti"),
            Map.entry("cliente_cti_codigo", "cliente_cti"),
            Map.entry("consultor_responsavel", "consultor"),
            Map.entry("segmento_de_atuacao", "segmento"),
            Map.entry("nivel_do_cliente", "nivel"),
            Map.entry("classificacao", "nivel"),
            Map.entry("faixa_de_faturamento", "faturamento_anual"),
            Map.entry("faixa_faturamento_anual", "faturamento_anual"),
            Map.entry("faturamento", "faturamento_anual"),
            Map.entry("servicos", "servicos_contratados"),
            Map.entry("servico_contratado", "servicos_contratados"),
            Map.entry("servicos_contratado", "servicos_contratados"));

    private static final Set<String> EXTENSOES = Set.of(".xlsx", ".xls");

    /** Marcas de acentuacao separadas pela normalizacao NFD. */
    private static final Pattern ACENTOS = Pattern.compile("\\p{M}");

    /** Espaco, ponto e hifen viram underline no nome normalizado da coluna. */
    private static final Pattern SEPARADORES = Pattern.compile("[\\s.\\-]+");

    public Path salvar(MultipartFile arquivo, Path diretorio) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ExcelInvalidoException("Nenhum arquivo foi enviado.");
        }
        String nomeOriginal = arquivo.getOriginalFilename() == null ? "planilha" : arquivo.getOriginalFilename();
        String extensao = nomeOriginal.contains(".")
                ? nomeOriginal.substring(nomeOriginal.lastIndexOf('.')).toLowerCase(Locale.ROOT)
                : "";
        if (!EXTENSOES.contains(extensao)) {
            throw new ExcelInvalidoException("Envie um arquivo .xlsx ou .xls.");
        }
        // Caminho absoluto: transferTo() resolveria um caminho relativo contra o
        // diretorio temporario do multipart, nao contra a pasta do projeto.
        Path pasta = diretorio.toAbsolutePath().normalize();
        try (InputStream origem = arquivo.getInputStream()) {
            Files.createDirectories(pasta);
            Path destino = pasta.resolve(System.currentTimeMillis() + extensao);
            Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
            return destino;
        } catch (IOException e) {
            throw new ExcelInvalidoException("Falha ao gravar o arquivo no servidor.", e);
        }
    }

    /** Abre a planilha, valida o cabecalho e devolve quantas linhas de dados existem. */
    public int validar(Path arquivo) {
        try (InputStream in = Files.newInputStream(arquivo); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet aba = workbook.getSheetAt(0);
            if (aba == null || aba.getPhysicalNumberOfRows() < 2) {
                throw new ExcelInvalidoException("A planilha esta vazia ou so tem cabecalho.");
            }
            conferirCabecalho(aba.getRow(aba.getFirstRowNum()));
            return aba.getLastRowNum() - aba.getFirstRowNum();
        } catch (ColunaObrigatoriaException | ExcelInvalidoException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new ExcelInvalidoException("O arquivo nao pode ser lido como Excel.", e);
        }
    }

    private void conferirCabecalho(Row cabecalho) {
        if (cabecalho == null) {
            throw new ExcelInvalidoException("A planilha nao tem linha de cabecalho.");
        }
        Set<String> presentes = new HashSet<>();
        for (Cell celula : cabecalho) {
            presentes.add(normalizar(celula.toString()));
        }
        List<String> faltantes = new ArrayList<>();
        for (String obrigatoria : COLUNAS_OBRIGATORIAS) {
            if (!presentes.contains(obrigatoria)) {
                faltantes.add(obrigatoria);
            }
        }
        if (!faltantes.isEmpty()) {
            throw new ColunaObrigatoriaException(faltantes);
        }
    }

    /**
     * "Faturamento Anual", "FATURAMENTO  ANUAL" e "Faixa de Faturamento" viram o
     * mesmo "faturamento_anual": primeiro a forma canonica, depois o sinonimo.
     */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = ACENTOS.matcher(Normalizer.normalize(texto.trim(), Normalizer.Form.NFD))
                .replaceAll("");
        String base = SEPARADORES.matcher(semAcento.toLowerCase(Locale.ROOT)).replaceAll("_");
        base = base.replaceAll("^_+|_+$", "");
        return EQUIVALENTES.getOrDefault(base, base);
    }
}
