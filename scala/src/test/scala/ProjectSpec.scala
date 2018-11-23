import org.scalatest.{FreeSpec, Matchers}
import Modelo._
import MovimientosBasicos._

class ProjectSpec extends FreeSpec with Matchers {

  "DragonBall" - {

    //Movimientos custom
    val ataque1: Movimiento = Movimiento("GolpeSuave", (g1: Guerrero, g2: Guerrero) => (g1.copy(), g2.copy(ki = g2.ki - 10)))
    val ataque2: Movimiento = Movimiento("GolpeMedio", (g1: Guerrero, g2: Guerrero) => (g1.copy(), g2.copy(ki = g2.ki - 20)))

    val kamehameHa: Movimiento = onda(10)
    val macenko: Movimiento = onda(30)
    val golpearConMaza: Movimiento = usarItem(Arma(Roma))
    val usarEspada: Movimiento = usarItem(Arma(Filosa))
    val comerSemillaDelErmitanio: Movimiento = usarItem(SemillaHermitanio)
    val disparar: Movimiento = usarItem(Arma(Fuego(10)))
    val muchosGolpesDeNinja = muchosGolpesNinja(Fisico)

    val todosLosAtaques: List[Movimiento] = List(comerSemillaDelErmitanio,usarEspada, golpearConMaza, macenko, kamehameHa, disparar  )

    val vegeta = Guerrero("vegeta", 1000, 1200, todosLosAtaques, Sayajin(1, false, false), List(SemillaHermitanio), Normal)
    val nro18 = Guerrero("nro18", 100, 1200, todosLosAtaques, Androide(1000), List(SemillaHermitanio), Normal)
    val kiabe = Guerrero("kiabe", 500, 1600, List(kamehameHa,convertirseEnSSJ), Sayajin(0, false, false), List(SemillaHermitanio), Normal)

    var krilin = Guerrero("krilin", 100, 1200, List(kamehameHa), Humano, List(SemillaHermitanio), Normal)

    "vegetaTieneUnaSemillaDelErmitanio?" in {

      vegeta.tieneElItem(SemillaHermitanio) shouldEqual true
    }

    "Saiyajin busca ataque mas conveniente para un Monstruo" in {

      var vegeta = Guerrero("vegeta", 100, 1200, List(kamehameHa,convertirseEnSSJ, muchosGolpesDeNinja), Sayajin(12, false, false), List(SemillaHermitanio), Normal)
      val posibleMovimiento: Option[Movimiento] = vegeta.movimientoMasEfectivoContra(krilin)(prioridadAtaque)
      var movimientoQueHaceMasDanio: Movimiento = comerSemillaDelErmitanio
      posibleMovimiento match{
        case Some(mov) => movimientoQueHaceMasDanio = mov
        case None => None
      }
      movimientoQueHaceMasDanio shouldEqual  muchosGolpesDeNinja
    }

    "Pelear 1 Round: estado inicial: Krilin 100ki Vegeta 100ki - final Krilin 40ki(-60 macenko) Vegeta 60ki (-10 ataque suave -30 macenko)" in {

      //final Krilin 40ki(recibe -60ki del ataque macenko)
      // Vegeta 60ki (recibe -10ki del ataque y pierde 30ki por usar el macenko)
      val vegeta = Guerrero("vegeta", 100, 1200, List(macenko), Sayajin(12, false, false), List(SemillaHermitanio), Normal)

      val primerRound = krilin.pelearRound(ataque1)(vegeta)
      // Probar despues krilin.planDeAtaqueContra(vegeta, 4)(prioridadAtaque)
      // Probar movimientos complejos como usar item
      (primerRound._1.ki, primerRound._2.ki) shouldEqual(40, 60)

    }
    "Vegeta come una semilla del hermitaño pero krilin le hace 10 de daño" in {


      var krilin2 = Guerrero("krilin", 100, 1200, List(ataque1), Humano, List(SemillaHermitanio), Normal)
      var vegeta2 = Guerrero("vegeta", 100, 1200, List(usarItem(SemillaHermitanio)), Sayajin(12, false, false), List(SemillaHermitanio), Normal)


      //var superVegeta = usarItem(SemillaHermitanio).ejecutarMov(vegeta2, krilin2)

      val superVegeta = vegeta2.pelearRound(usarItem(SemillaHermitanio))(krilin2)

      superVegeta._1.ki shouldEqual 1190

    }

    "Cel pelea 1 round con krilin y se lo come. Cel Aprende el KamehameHa y krilin muere" in {

      val cel: Guerrero = Guerrero("cel", 100, 1200, List(comerOponente), Monstruo(List(ataque1),sumarMovimientos), List(SemillaHermitanio), Normal)

      val (celPelado,krilinMuerto): (Guerrero, Guerrero) = cel.pelearRound(comerOponente)(krilin)

      krilinMuerto.estado shouldEqual Muerto



    }

    "kiabe ataca a krilin con macenko + kamehameHa" in {

     // kiabe: Tiene 500ki - 30 por usar macenko y -10 por usar kamehameHa. Krilin le tira x1 kamehameHa (-20)
      //Krilin: Solo sabe hacer el kamehameHa.
      // Krilin recibe -10 por usar kamehameHa y recibe -80 (60 macenko + 20 kamehameHa) de kiabe
      // Krilin en el segundo turno no puede contestar con el kamehameHa porque tiene solo 10 de Ki
      val (kiabeCansado, krilinMatado): (Guerrero, Guerrero) = kiabe.pelearContra(krilin)(List(macenko,kamehameHa))


      val (kiKiabe, kiDeKrilin) =  (kiabeCansado.ki, krilinMatado.ki)

      (kiKiabe, kiDeKrilin) shouldBe (440,10)
    }

    "planDeAtaqueContra si yo estoy Muerto" in {

      val yajirobe = Guerrero("yajirobe", 0, 1200, todosLosAtaques, Humano, List[Item](), Muerto)

      val movsYaji : Option[List[Movimiento]] = yajirobe.planDeAtaqueContra(krilin,3)(prioridadAtaque)

      movsYaji shouldEqual None

    }

    "planDeAtaqueContra si el otro está muerto" in {

      val yajirobe = Guerrero("yajirobe",300, 1200, todosLosAtaques, Humano, List[Item](), Normal)

      val chen = Guerrero("yajirobe", 0, 1200, todosLosAtaques, Humano, List[Item](), Muerto)

      val movsYaji = yajirobe.planDeAtaqueContra(chen,3)(prioridadAtaque)

      movsYaji shouldEqual None
    }

        "planDeAtaqueContra si el plan que estoy armando con este criterio me deja muerto antes de N rounds" in {

          val krilin3 = Guerrero("krilin", 9000, 12000, List(kamehameHa), Humano, List(SemillaHermitanio), Normal)
          val vegeta3 = Guerrero("vegeta", 9000, 12000, List(kamehameHa), Humano, List(SemillaHermitanio), Normal)
          val h = krilin.planDeAtaqueContra(vegeta, 5)(prioridadAtaque)

          println(h)

          h.size shouldEqual 5

        }
    /*
           "planDeAtaqueContra si no puedo conseguir movimientos para los N rounds" in {

           }

           "planDeAtaqueContra si pido N rounds tengo que tener N movimientos" in {

           }

           */
  }


  /*
      "probando Plan de Ataque" in {

        var krilin = Guerrero("krilin", 10000, 12000, List(ataque1, ataque3, usarItem(SemillaHermitanio)), Humano, List(SemillaHermitanio), Normal)
        var vegeta = Guerrero("vegeta", 10000, 12000, List(ataque1, ataque2, ataque3), Sayajin(12, false, false), List(SemillaHermitanio), Normal)
        var h = krilin.planDeAtaqueContra(vegeta, 10)(prioridadAtaque)

        h.size shouldEqual 10

      }
      */

}
