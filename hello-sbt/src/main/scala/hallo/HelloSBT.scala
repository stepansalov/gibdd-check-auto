package hallo

import play.api.libs.json._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scala.collection.immutable._
import scalafx.Includes._
import scalafx.beans.property.BooleanProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Scene, Group, Node}


object Json4sTests extends JFXApp {
  def parseJson(rawJson:String): ListMap[String, String]= {
    val l = Json.parse(rawJson)
    return ListMap("Марка, модель" -> (l \ "RequestResult" \ "vehicle" \ "model").as[String],
                "Год выпуска" -> (l \ "RequestResult" \ "vehicle" \ "year").as[String],
                "VIN" -> (l \ "RequestResult" \ "vehicle" \ "vin").as[String],
                "Цвет" -> (l \ "RequestResult" \ "vehicle" \ "color").as[String],
                "Рабочий объем (см³)" -> (l \ "RequestResult" \ "vehicle" \ "engineVolume").as[String],
                "Мощность (л.с.)" -> (l \ "RequestResult" \ "vehicle" \ "powerHp").as[String])
  }

  val rawJson = """{
    "RequestResult": {
      "ownershipPeriods": {
        "ownershipPeriod": [
        {
          "simplePersonType": "Natural",
          "from": "2010-10-27T00:00:00.000+04:00",
          "to": "2010-10-27T00:00:00.000+04:00"
        },
        {
        "simplePersonType": "Natural",
        "from": "2012-09-03T00:00:00.000+04:00",
        "to": "2012-09-06T00:00:00.000+04:00"
        },
        {
        "simplePersonType": "Natural",
        "from": "2012-09-06T00:00:00.000+04:00",
        "to": "2012-09-06T00:00:00.000+04:00"
        },
        {
        "simplePersonType": "Natural",
        "from": "2012-09-06T00:00:00.000+04:00",
        "to": "2015-10-27T00:00:00.000+03:00"
        },
        {
        "simplePersonType": "Natural",
        "from": "2015-10-27T00:00:00.000+03:00",
        "to": "2016-01-28T00:00:00.000+03:00"
        },
        {
        "simplePersonType": "Natural",
        "from": "2016-01-28T00:00:00.000+03:00"
        }
        ]
      },
      "vehiclePassport": {},
      "vehicle": {
        "engineVolume": "2500.0",
        "color": "ТЕМНО-ГОЛУБОЙ",
        "bodyNumber": "JТНВК262102070326",
        "year": "2007",
        "engineNumber": "0427091",
        "vin": "JТНВК262102070326",
        "model": "LЕХUS IS250",
        "category": "В",
        "type": "23",
        "powerHp": "208",
        "powerKwt": "152.94"
      }
    },
    "vin": "JTHBK262102070326",
    "regnum": null,
    "message": "ver.3.1",
    "status": 200
  }"""

val l = parseJson(rawJson)

private val dragModeActiveProperty = new BooleanProperty(this, "dragModeActive", true)
private val borderStyle = "" +
  "-fx-background-color: white;" +
  "-fx-border-color: black;" +
  "-fx-border-width: 1;" +
  "-fx-border-radius: 6;" +
  "-fx-padding: 6;"

stage = new JFXApp.PrimaryStage() {
    val panelsPane = new Pane() {
      children = Seq(
        new VBox(6) {
          children = for {(k,v) <- l}
            yield new HBox(6){
              children = Seq(
                new Label(k + ": "){
                  minWidth = 150
                },
                new Label(v)
              )
            }
        }
      )
    }
    scene = new Scene(400, 300) {
      root = new BorderPane() {
        center = panelsPane
      }
    }
  }




//for ((k,v) <- m1)


println(l)
}
