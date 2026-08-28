package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.dto.UploadResponse;
import br.senai.ctiinsights.service.ImportacaoService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Recebe a planilha da CTI e devolve o resumo do processamento. */
@RestController
@RequestMapping("/api/planilhas")
public class UploadController {

    private final ImportacaoService importacao;

    public UploadController(ImportacaoService importacao) {
        this.importacao = importacao;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse enviar(@RequestParam("arquivo") MultipartFile arquivo) {
        return importacao.importar(arquivo);
    }
}
