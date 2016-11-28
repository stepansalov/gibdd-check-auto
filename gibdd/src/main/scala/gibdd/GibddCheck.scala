package gibdd_check

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.beans.property.BooleanProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._
import scalafx.scene.{Scene, Group, Node}
import javafx.event.EventHandler
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.application.{JFXApp, Platform}

import scala.collection.immutable._
import scalafx.scene.control.Alert.AlertType

import dispatch.{ Defaults, Http, Req, as, implyRequestHandlerTuple, url }
import scala.concurrent.{Await, Future, Awaitable}
import scala.concurrent.duration._
import java.io._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scalafx.beans.property.StringProperty
import scala.util.{Try, Success, Failure}

import com.ning.http.client.cookie.Cookie
import scala.collection.JavaConversions._
import play.api.libs.json._

/**
 * Example of using mouse event filters.
 * Most important part is in `makeDraggable(Node)` method.
 *
 * Based on example from JavaFX tutorial [[http://docs.oracle.com/javafx/2/events/filters.htm Handling JavaFX Events]].
 */
object GibddCheck extends JFXApp {

  private val vin = StringProperty("")
  private val captcha = StringProperty("")
  private val response = StringProperty("")

  private val borderStyle = "" +
    "-fx-background-color: white;" +
    "-fx-border-color: black;" +
    "-fx-border-width: 1;" +
    "-fx-border-radius: 6;" +
    "-fx-padding: 6;"

  private val message = new Pane()
  stage = new JFXApp.PrimaryStage() {

    val panelsPane = new Pane() {
      val vinPanel = createVinPanel()
      
      children = Seq(new VBox(2) {
        children = Seq(vinPanel, message)
      })
      alignmentInParent = Pos.TopLeft
    }

    title = "Проверка Транспортного Средства"
    scene = new Scene(400, 300) {
      root = new BorderPane() {
        center = panelsPane
      }
    }
  }

  private def createVinPanel(): Node = new HBox(5) {
    val vinLabel = new Label("Введите VIN")
    val vinInput = new TextField() {
      prefColumnCount = 10
      promptText = "VIN"
    }
    val button = new Button("Запросить Проверку") {
      onAction = handle {
        onSendRequest()
      }
    }
    vin <== vinInput.text
    children = Seq(vinLabel, vinInput, button)
  }

  def onSendRequest(): Unit = {
    val (cookies, fileName) = getCaptchaImage()
    showDialogWindow(fileName)
    sendCaptcha(cookies)
  }

  def showDialogWindow(fileName: String): Unit = {
    val dialog = new Dialog[String]() {
      initOwner(stage)
      title = "Каптча"
      headerText = "Введите Каптчу"
    }

    val checkCaptchaButtonType = new ButtonType("отправить", ButtonData.OKDone)
    dialog.dialogPane().buttonTypes = Seq(checkCaptchaButtonType)

    val captchaText = new TextField() {
      promptText = "Captcha"
    }

    val captchaImageView = createCaptchaImageView(fileName)

    val grid = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10);

      add(captchaImageView, 0, 0)
      add(new Label("Captcha"), 0, 1)
      add(captchaText, 1, 1)
    }

    dialog.dialogPane().content = grid

    Platform.runLater(captchaText.requestFocus())


    dialog.resultConverter = dialogButton => {
      captchaText.text().toString()
    }

    val result = dialog.showAndWait()
    result match {
      case Some(str) => {
        new File(fileName).delete()
        captcha() = str.toString()
      }
      case None => println("nope")
    }
  }

  private def getCaptchaImage(): (List[Cookie], String) = {
    val now = System.currentTimeMillis
    val prefix = getClass.getResource("/").getPath
    val name = s"captcha_${now}.png"
    val fileName = s"$prefix$name"
    val req = url(s"http://check.gibdd.ru/proxy/captcha.jpg?${now}")
    val res = Http(req)
    val response = for (r <- res) yield {
      val m = r.getCookies.toList
      val image = r.getResponseBodyAsBytes
      val bos = new BufferedOutputStream(new FileOutputStream(fileName))
      Stream.continually(bos.write(image))
      bos.close()
      (m, name)
    }
    response onFailure {
      case t => println("An error has occured: " + t.getMessage)
    }
    return Await.result(response, Duration(5000, "millis"))
  }

  def sendCaptcha(cookies: List[Cookie]): Unit = {
    val vinText = vin()
    val captchaText = captcha()
    val req = url("http://check.gibdd.ru/proxy/check/auto/history").POST
    val reqWithParams = req << Map("vin" -> vinText, "captchaWord" -> captchaText, "checkType" -> "history")
    val reqWithCookies = cookies.foldLeft(reqWithParams) ((req, cookie) => req.addCookie(cookie))
    val resFuture = Http(reqWithCookies OK as.String)
    val res = for (r <- resFuture) yield r
    val stringified = Await.result(res, Duration(5000, "millis"))
    val (status, errorMessage) = checkStatus(stringified)
    if (status != 200)
      showInfoWindow(errorMessage, status)
    else {
      val panel = createInfoPane(parseJson(stringified))
      message.children = Seq(panel)
    }

  }

  private def createCaptchaImageView(fileName: String): Node = {
    val url = getClass.getResource(s"/$fileName")
    val image = new Image(url.toExternalForm)
    new ImageView(image) {
      fitWidth = 150
    }
  }

  private def showInfoWindow(message: String, status: Int): Unit = {
    new Alert(AlertType.Information) {
      initOwner(stage)
      title = "Information Dialog"
      headerText = s"Произошла ошибка $status"
      contentText = message
    }.showAndWait()
  }

  private def checkStatus(rawJson: String): (Int, String) = {
    val l = Json.parse(rawJson)
    val status = (l \ "status").as[Int]
    val message = (l \ "message").as[String]
    return (status, message)
  }

  private def parseJson(rawJson:String): ListMap[String, String]= {
    val l = Json.parse(rawJson)
    return ListMap("Марка, модель" -> (l \ "RequestResult" \ "vehicle" \ "model").as[String],
                "Год выпуска" -> (l \ "RequestResult" \ "vehicle" \ "year").as[String],
                "VIN" -> (l \ "RequestResult" \ "vehicle" \ "vin").as[String],
                "Цвет" -> (l \ "RequestResult" \ "vehicle" \ "color").as[String],
                "Рабочий объем (см³)" -> (l \ "RequestResult" \ "vehicle" \ "engineVolume").as[String],
                "Мощность (л.с.)" -> (l \ "RequestResult" \ "vehicle" \ "powerHp").as[String])
  }

  private def createInfoPane(l: ListMap[String, String]): Node = {
      return new Pane() {
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
  }
}
