package cl.dgt.consolidado.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tramite")
public class Tramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDate fecha;

    // OJO CON EL NOMBRE. Aquí dentro el campo se llama `monto`. En el JSON que pide el brief se
    // llama `montoDeclarado`. No es un descuido: el nombre de dentro es del modelo y el de fuera
    // es del contrato de la API, y conviene que puedan cambiar por separado. La traducción se
    // hace en el servicio, al armar el DTO.
    //
    // Si devuelves la entidad —o un DTO cuyo campo se llame `monto`— el JSON sale con "monto" y
    // NADIE te avisa: la petición responde 200, los números están bien y el formato está mal.
    // Es el error de formato más común de este encargo.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    // LAZY: esto no es el contribuyente todavía, es un proxy, y se carga con la PRIMERA llamada
    // a `getContribuyente()`. Si al mapear la lista de trámites a DTO pasas por aquí, sale un
    // SELECT por cada trámite — el N+1 del Lab 06, y en silencio: la respuesta es correcta y lo
    // único que lo delata es el log de SQL, que es justo lo que mira el criterio 5 de la rúbrica.
    // Se evita con un `join fetch` en la consulta, o no pasando por aquí: la razón social ya la
    // tienes del contribuyente que buscaste por RUT.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contribuyente_id", nullable = false)
    private Contribuyente contribuyente;

    // La oficina que lo tramitó. La usa el ejemplo resuelto de `ejemplo/`.
    @Column(name = "oficina_codigo", nullable = false)
    private String oficinaCodigo;

    protected Tramite() {
    }

    /**
     * Constructor SÓLO para tests. La aplicación no lo usa: los trámites los crea Hibernate al
     * leer la tabla. Está aquí para que un test de servicio pueda armar un trámite sin levantar
     * la base ni recurrir a reflexión.
     */
    public Tramite(Long id, String tipo, String estado, java.time.LocalDate fecha,
                   java.math.BigDecimal monto, String oficinaCodigo) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
        this.monto = monto;
        this.oficinaCodigo = oficinaCodigo;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public String getOficinaCodigo() {
        return oficinaCodigo;
    }
}
