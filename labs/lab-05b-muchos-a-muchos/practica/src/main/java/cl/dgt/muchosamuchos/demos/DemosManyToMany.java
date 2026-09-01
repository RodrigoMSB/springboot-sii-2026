package cl.dgt.muchosamuchos.demos;

import cl.dgt.muchosamuchos.repositories.DocumentoRepository;
import cl.dgt.muchosamuchos.repositories.TramiteRepository;
import cl.dgt.muchosamuchos.soporte.ContadorDeSentencias;
import cl.dgt.muchosamuchos.soporte.MiradorDeLaIntermedia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemosManyToMany {

    private final TramiteRepository tramites;
    private final DocumentoRepository documentos;
    private final MiradorDeLaIntermedia mirador;
    private final ContadorDeSentencias contador;

    private Long tramiteInicioId;

    public DemosManyToMany(TramiteRepository tramites,
                           DocumentoRepository documentos,
                           MiradorDeLaIntermedia mirador,
                           ContadorDeSentencias contador) {
        this.tramites = tramites;
        this.documentos = documentos;
        this.mirador = mirador;
        this.contador = contador;
    }

    public void laRelacionYSuTabla() {
        seccion(1, "LA RELACIÓN · @ManyToMany y @JoinTable");
        // Siembra 5 documentos y 3 trámites que los comparten, y mira la tabla intermedia.
        // escribe aquí
    }

    @Transactional
    public void agregarYQuitar() {
        seccion(2, "AGREGAR Y QUITAR · los INSERT y DELETE de la intermedia");
        // Adjunta un documento a un trámite, quítalo, y mira la intermedia entre medio.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void elLadoEspejo() {
        seccion(3, "LADO ESPEJO · @ManyToMany(mappedBy)");
        // Trae un documento y recorre los trámites que lo piden.
        // escribe aquí
    }

    @Transactional
    public void setContraList() {
        seccion(4, "SET CONTRA LIST · el mismo cambio, medido");
        // Adjunta y quita un documento con el contador en marcha, y cuenta las sentencias.
        // escribe aquí
    }

    @Transactional(readOnly = true)
    public void consultaQueNavega() {
        seccion(5, "CONSULTA QUE NAVEGA · findByDocumentosCodigo()");
        // Llama a findByDocumentosCodigo() y mira los dos join que genera.
        // escribe aquí
    }

    public void cuandoDejaDeServir() {
        seccion(6, "CUÁNDO @ManyToMany DEJA DE SERVIR");
        // Pregúntale a la tabla intermedia por un dato que no tiene, y dibuja en qué se convierte.
        // escribe aquí
    }

    private void informe(String que) {
        System.out.println("  <<<<<< FIN DEL CONTEO · " + que);
        System.out.println("      SENTENCIAS CONTRA LA BASE: " + contador.sentencias());
    }

    private void seccion(int numero, String titulo) {
        System.out.println();
        System.out.println("=== " + numero + " · " + titulo + " ===");
    }
}
