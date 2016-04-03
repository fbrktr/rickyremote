package ricky.ui

import java.util.Date

import akka.actor.ActorRef
import processing.core._
import ricky.RickyRemote
import PConstants._

/**
  * Created by fbrktr on 02/04/2016.
  */
class RickyUI extends PApplet{

  val brain:ActorRef = RickyRemote.brain

  val sceneWidth = 1300
  val sceneHeight = 700

  // :(
  var rickyIcon:PShape = null
  var tools:PGraphics = null
  var logs:PGraphics = null
  var fpsDisplay:PGraphics = null
  var pixelFont:PFont = null

  override  def settings(): Unit = {
    size(sceneWidth,sceneHeight)
  }

  override def setup(): Unit ={
    println("SETUP")
    frameRate(120)
    rickyIcon = loadShape("ricky.svg")
    rickyIcon.disableStyle()
    tools = createGraphics(sceneWidth, sceneHeight)
    logs = createGraphics(sceneWidth, sceneHeight)
    fpsDisplay = createGraphics(sceneWidth, sceneHeight)
    pixelFont = createFont("Minecraftia-Regular", 8)
  }

  override def draw(): Unit = {
    background(0xffcccccc)

    drawFPS()
    drawLogs()
    drawTools()

    image(fpsDisplay, 0,0)
    image(logs, 0,0)
    image(tools, 0,0)

  }

  def drawLogs():Unit = {
    logs.noSmooth()
    logs.beginDraw()
    logs.textFont(pixelFont)
    logs.clear()
    logs.noStroke()
    logs.fill(128, 128, 128, 50)
    logs.text(RickyState.logHistory.mkString("\n"), 10, 40)
    logs.endDraw()
  }

  def drawTools():Unit = {
    val timeSinceLastHeatBeat = new Date().getTime - RickyState.lastTs
    val heartBeatRate = 10000
    val rickyAlpha:Float = 30 + Math.max(0, (heartBeatRate - timeSinceLastHeatBeat)/80)
    tools.smooth(4)
    tools.beginDraw()
    tools.clear()
    tools.strokeJoin(ROUND)
    tools.fill(128, 128, 128, rickyAlpha)
    tools.stroke(128, 128, 128, 150)
    tools.strokeWeight(3)
    tools.shape(rickyIcon, sceneWidth-40, sceneHeight - 40, 32, 32)
    tools.endDraw()
  }

  var fpCpt = 0
  var lastSec = new Date().getTime / 1000
  var fps = 0
  def drawFPS():Unit = {
    fpCpt += 1
    val currSec = new Date().getTime / 1000
    if( currSec != lastSec){
      fps = fpCpt
      fpCpt = 0
      lastSec = currSec
      fpsDisplay.noSmooth()
      fpsDisplay.beginDraw()
      fpsDisplay.textFont(pixelFont)
      fpsDisplay.clear()
      fpsDisplay.fill(128, 128, 128, 80)
      fpsDisplay.noStroke()
      fpsDisplay.text(s"$fps FPS", 10, 20)
      fpsDisplay.endDraw()
    }
  }

  override def keyPressed() ={
    keyCode match {
      case 38 => askMove("F")
      case 40 => askMove("B")
      case 37 => askMove("L")
      case 39 => askMove("R")
      case _ => println("pressed " + key + "," + keyCode)
    }
  }

  override def keyTyped() {
    key match {
      case 's' => askMove("S")
      case _ => println("typed " + key + " " + keyCode)
    }
  }

  override def keyReleased() =  {
    keyCode match {
      case s if List(38, 40, 37, 39, 83).contains(keyCode) => askMove("S")
      case _ => println("released " + key + " " + keyCode)
    }
  }

  def askMove(direction:String):Unit = {
    RickyState.log("[CMD] "+direction)
    brain ! new MoveRequest(direction)
  }

}

object RickyState {
  var lastTs = new Date().getTime
  var logHistory = List("Ricky2000")

  def heartBeat(): Unit = {
    lastTs = new Date().getTime
    log("[RICK] <3")
  }
  def log(msg:String):Unit = {
    logHistory = msg :: logHistory.take(50)
  }
}
