package cl.dgt.muchosamuchos.demos;

import cl.dgt.muchosamuchos.entities.Documento;
import cl.dgt.muchosamuchos.entities.Tramite;
import cl.dgt.muchosamuchos.repositories.DocumentoRepository;
import cl.dgt.muchosamuchos.repositories.TramiteRepository;
import cl.dgt.muchosamuchos.soporte.ContadorDeSentencias;
import cl.dgt.muchosamuchos.soporte.MiradorDeLaIntermedia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

        System.out.println("  al arrancar había " + tramites.count() + " trámites y "
                + documentos.count() + " documentos de la vez anterior");

        tramites.deleteAll();
        documentos.deleteAll();

        Documento cedula = documentos.save(
                new Documento("CEDULA", "Cédula de identidad del representante"));
        Documento escritura = documentos.save(
                new Documento("ESCRITURA", "Escritura de constitución"));
        Documento poder = documentos.save(
                new Documento("PODER", "Poder simple ante notario"));
        Documento balance = documentos.save(
                new Documento("BALANCE", "Balance del último ejercicio"));
        Documento vigencia = documentos.save(
                new Documento("VIGENCIA", "Certificado de vigencia de la sociedad"));
        System.out.println("  5 documentos guardados");

        Tramite inicio = new Tramite("Inicio de actividades", "76.543.210-K", LocalDate.of(2026, 3, 10));
        inicio.adjuntar(cedula);
        inicio.adjuntar(escritura);
        inicio.adjuntar(balance);
        inicio.adjuntar(vigencia);
        this.tramiteInicioId = tramites.save(inicio).getId();

        Tramite representante = new Tramite("Cambio de representante legal", "77.111.222-3", LocalDate.of(2026, 4, 5));
        representante.adjuntar(cedula);
        representante.adjuntar(escritura);
        representante.adjuntar(poder);
        tramites.save(representante);

        Tramite termino = new Tramite("Término de giro", "78.999.888-1", LocalDate.of(2026, 6, 30));
        termino.adjuntar(cedula);
        termino.adjuntar(balance);
        tramites.save(termino);

        System.out.println("  3 trámites guardados · el " + tramiteInicioId + " lleva 4 documentos");
        System.out.println("  y CEDULA la piden los tres");

        mirador.imprimirTodo("recién sembrada");
    }

    @Transactional
    public void agregarYQuitar() {
        seccion(2, "AGREGAR Y QUITAR · los INSERT y DELETE de la intermedia");

        Tramite tramite = tramites.findById(tramiteInicioId).orElseThrow();
        Documento poder = documentos.findByCodigo("PODER").orElseThrow();
        System.out.println("  trámite " + tramite.getId() + " · " + tramite.getTipo()
                + " · lleva " + tramite.getDocumentos().size() + " documentos");

        System.out.println("  --- se ADJUNTA el poder ---");
        tramite.adjuntar(poder);
        tramites.flush();
        mirador.imprimirDelTramite(tramiteInicioId, "tras adjuntar");

        System.out.println("  --- se QUITA el poder ---");
        tramite.quitar(poder);
        tramites.flush();
        mirador.imprimirDelTramite(tramiteInicioId, "tras quitar");
    }

    @Transactional(readOnly = true)
    public void elLadoEspejo() {
        seccion(3, "LADO ESPEJO · @ManyToMany(mappedBy)");

        Documento cedula = documentos.findByCodigo("CEDULA").orElseThrow();
        System.out.println("  documento: " + cedula.getNombre());
        System.out.println("  --- todavía NO se ha tocado la colección ---");

        Set<Tramite> suyos = cedula.getTramites();
        System.out.println("  aparece en " + suyos.size() + " trámites:");
        suyos.forEach(t -> System.out.println("    " + t));
    }

    @Transactional
    public void setContraList() {
        seccion(4, "SET CONTRA LIST · el mismo cambio, medido");

        Tramite tramite = tramites.findById(tramiteInicioId).orElseThrow();
        Documento poder = documentos.findByCodigo("PODER").orElseThrow();
        System.out.println("  trámite " + tramite.getId() + " con "
                + tramite.getDocumentos().size() + " documentos ya cargados en memoria");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO · ADJUNTAR UN DOCUMENTO");
        contador.reiniciar();
        tramite.adjuntar(poder);
        tramites.flush();
        informe("ADJUNTAR UN DOCUMENTO");

        System.out.println("  >>>>>> EMPIEZA EL CONTEO · QUITAR ESE MISMO DOCUMENTO");
        contador.reiniciar();
        tramite.quitar(poder);
        tramites.flush();
        informe("QUITAR ESE MISMO DOCUMENTO");
    }

    @Transactional(readOnly = true)
    public void consultaQueNavega() {
        seccion(5, "CONSULTA QUE NAVEGA · findByDocumentosCodigo()");

        List<Tramite> conPoder = tramites.findByDocumentosCodigo("PODER");
        System.out.println("  trámites que piden PODER -> " + conPoder.size());
        conPoder.forEach(t -> System.out.println("    " + t));

        List<Tramite> conCedula = tramites.findByDocumentosCodigo("CEDULA");
        System.out.println("  trámites que piden CEDULA -> " + conCedula.size());
        conCedula.forEach(t -> System.out.println("    " + t));
    }

    public void cuandoDejaDeServir() {
        seccion(6, "CUÁNDO @ManyToMany DEJA DE SERVIR");

        System.out.println("  la pregunta: ¿en qué fecha se adjuntó CEDULA al trámite "
                + tramiteInicioId + ", y quién la subió?");
        System.out.println("  columnas de tramite_documento -> " + mirador.columnas());
        System.out.println("  la respuesta no está, y no cabe: @ManyToMany manda una tabla de dos claves");
        System.out.println();
        System.out.println("  EN QUÉ SE CONVIERTE (no se implementa hoy):");
        System.out.println("    @Entity @Table(name = \"adjunto\")");
        System.out.println("    class Adjunto {");
        System.out.println("        @Id @GeneratedValue  Long id");
        System.out.println("        @ManyToOne(LAZY)     Tramite tramite");
        System.out.println("        @ManyToOne(LAZY)     Documento documento");
        System.out.println("        LocalDate            fechaAdjunto     <-- el dato propio");
        System.out.println("        String               subidoPor        <-- y este");
        System.out.println("    }");
        System.out.println("    y en Tramite:  @OneToMany(mappedBy = \"tramite\") Set<Adjunto> adjuntos");
        System.out.println();
        System.out.println("  la tabla intermedia deja de ser un @JoinTable y pasa a ser una @Entity.");
        System.out.println("  Y entonces esto ya no es muchos-a-muchos: son dos @ManyToOne, que es el lab 05 dos veces.");
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
