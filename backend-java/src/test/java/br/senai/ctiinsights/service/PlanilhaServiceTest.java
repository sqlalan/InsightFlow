package br.senai.ctiinsights.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.senai.ctiinsights.exception.ColunaObrigatoriaException;
import br.senai.ctiinsights.exception.ExcelInvalidoException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlanilhaServiceTest {

    private static final String[] CABECALHO_DA_AULA = {
        "codigo_cliente", "nome_cliente", "consultor", "segmento", "nivel_cliente",
        "faturamento_anual", "servicos_contratados", "data_contratacao", "cidade", "uf"
    };

    private final PlanilhaService service = new PlanilhaService();

    @TempDir
    Path pasta;

    @Test
    void normalizaCabecalhoEscritoDeFormasDiferentes() {
        assertEquals("faturamento_anual", PlanilhaService.normalizar("Faturamento Anual"));
        assertEquals("faturamento_anual", PlanilhaService.normalizar("  FATURAMENTO  ANUAL "));
        assertEquals("servicos_contratados", PlanilhaService.normalizar("Serviços Contratados"));
    }

    @Test
    void traduzCabecalhoDoModeloDaAula() {
        assertEquals("cliente_cti", PlanilhaService.normalizar("codigo_cliente"));
        assertEquals("nivel", PlanilhaService.normalizar("nivel_cliente"));
        assertEquals("data_inicio", PlanilhaService.normalizar("data_contratacao"));
    }

    @Test
    void aceitaPlanilhaNoModeloDaAula() throws IOException {
        Path arquivo = criarPlanilha(CABECALHO_DA_AULA, 3);

        assertEquals(3, service.validar(arquivo));
    }

    @Test
    void apontaAsColunasQueFaltam() throws IOException {
        Path arquivo = criarPlanilha(new String[] {"codigo_cliente", "consultor", "segmento"}, 1);

        ColunaObrigatoriaException erro =
                assertThrows(ColunaObrigatoriaException.class, () -> service.validar(arquivo));

        assertTrue(erro.getColunasFaltantes().contains("Nível do cliente"));
        assertTrue(erro.getColunasFaltantes().contains("Faturamento anual"));
        assertEquals(3, erro.getColunasFaltantes().size());
    }

    @Test
    void recusaPlanilhaSoComCabecalho() throws IOException {
        Path arquivo = criarPlanilha(CABECALHO_DA_AULA, 0);

        assertThrows(ExcelInvalidoException.class, () -> service.validar(arquivo));
    }

    @Test
    void recusaArquivoQueNaoEExcel() throws IOException {
        Path arquivo = Files.writeString(pasta.resolve("falso.xlsx"), "nao sou uma planilha");

        assertThrows(ExcelInvalidoException.class, () -> service.validar(arquivo));
    }

    /** Monta um .xlsx com o cabecalho informado e N linhas de dados ficticios. */
    private Path criarPlanilha(String[] cabecalho, int linhasDeDados) throws IOException {
        Path arquivo = pasta.resolve("planilha.xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream saida = Files.newOutputStream(arquivo)) {
            Sheet aba = workbook.createSheet("clientes");
            Row primeira = aba.createRow(0);
            for (int i = 0; i < cabecalho.length; i++) {
                primeira.createCell(i).setCellValue(cabecalho[i]);
            }
            for (int linha = 1; linha <= linhasDeDados; linha++) {
                aba.createRow(linha).createCell(0).setCellValue("CTI00" + linha);
            }
            workbook.write(saida);
        }
        return arquivo;
    }
}
