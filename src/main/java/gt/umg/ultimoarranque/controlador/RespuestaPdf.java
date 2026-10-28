package gt.umg.ultimoarranque.controlador;

import gt.umg.ultimoarranque.vista.pdf.DocumentoPdf;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class RespuestaPdf {

    private RespuestaPdf() {
    }

    static ResponseEntity<byte[]> de(DocumentoPdf documento) {
        byte[] contenido = documento.generar();
        ContentDisposition disposicion = ContentDisposition.inline().filename(documento.nombreArchivo()).build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .contentLength(contenido.length)
                .body(contenido);
    }
}
