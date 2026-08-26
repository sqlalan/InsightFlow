package br.senai.ctiinsights.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduz as excecoes do dominio em respostas HTTP claras para o Front-end.
 * Sem isso, um erro de planilha chegaria na tela como 500 sem explicacao.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExcelInvalidoException.class)
    public ResponseEntity<ApiError> excelInvalido(ExcelInvalidoException ex) {
        return ResponseEntity.badRequest().body(ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "PLANILHA_INVALIDA",
                "Nao foi possivel ler a planilha enviada. " + ex.getMessage()));
    }

    @ExceptionHandler(ColunaObrigatoriaException.class)
    public ResponseEntity<ApiError> colunaObrigatoria(ColunaObrigatoriaException ex) {
        return ResponseEntity.unprocessableEntity().body(ApiError.of(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "COLUNA_OBRIGATORIA",
                "A planilha esta incompleta. Confira o modelo antes de enviar de novo.",
                ex.getColunasFaltantes()));
    }

    @ExceptionHandler(ClienteDuplicadoException.class)
    public ResponseEntity<ApiError> clienteDuplicado(ClienteDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(
                HttpStatus.CONFLICT.value(),
                "CLIENTE_DUPLICADO",
                ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "NAO_ENCONTRADO",
                ex.getMessage()));
    }

    @ExceptionHandler(AnaliseException.class)
    public ResponseEntity<ApiError> analise(AnaliseException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiError.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "FALHA_NA_ANALISE",
                "A planilha foi recebida, mas o tratamento dos dados falhou. " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "DADOS_INVALIDOS",
                "Revise os campos enviados.",
                detalhes));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> arquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiError.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "ARQUIVO_MUITO_GRANDE",
                "A planilha passou do limite de 10 MB."));
    }
}
